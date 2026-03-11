param(
  [ValidateSet("once", "continuous")]
  [string]$RunMode = "once",
  [int]$MaxIterations = 20,
  [string]$PrdPath = ".ralph/prd.json",
  [string]$ProgressPath = ".ralph/progress.txt",
  [ValidateSet("manual", "copilot", "claude", "custom")]
  [string]$AgentProfile = "manual",
  [string]$AgentCommand = "",
  [string]$AgentProfilesPath = ".ralph/agent-profiles.json",
  [string[]]$ProtectedBranches = @("main", "master"),
  [switch]$AllowProtectedBranch,
  [switch]$AutoCreateBranch
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$rootDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$resolvedPrdPath = Join-Path $rootDir $PrdPath
$resolvedProgressPath = Join-Path $rootDir $ProgressPath
$resolvedAgentProfilesPath = Join-Path $rootDir $AgentProfilesPath

if ($RunMode -eq "once") {
  $MaxIterations = 1
}

function Write-Log {
  param([string]$Message)
  $ts = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
  Write-Host "[$ts] $Message"
}

function Append-Progress {
  param([string]$Message)
  $ts = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
  Add-Content -Path $resolvedProgressPath -Value "[$ts] $Message"
}

function Load-Prd {
  if (-not (Test-Path $resolvedPrdPath)) {
    throw "PRD file not found at $resolvedPrdPath"
  }
  $json = Get-Content $resolvedPrdPath -Raw | ConvertFrom-Json
  if (-not $json.features) {
    throw "PRD file must contain a features array"
  }
  return $json
}

function Save-Prd {
  param([object]$Prd)
  $Prd | ConvertTo-Json -Depth 100 | Set-Content -Path $resolvedPrdPath
}

function Get-CurrentBranch {
  Push-Location $rootDir
  try {
    $branch = (git branch --show-current).Trim()
    return $branch
  } finally {
    Pop-Location
  }
}

function Ensure-BranchSafety {
  $current = Get-CurrentBranch
  if (-not $current) {
    throw "Could not resolve current git branch"
  }

  if ($ProtectedBranches -contains $current -and -not $AllowProtectedBranch) {
    if ($AutoCreateBranch) {
      Write-Log "Protected branch detected ($current). Auto-branch will be attempted per selected feature."
      return
    }

    throw "Refusing to run on protected branch '$current'. Use -AllowProtectedBranch or -AutoCreateBranch."
  }
}

function Get-PriorityScore {
  param([string]$Priority)
  switch ($Priority) {
    "critical" { return 0 }
    "high" { return 1 }
    "medium" { return 2 }
    "low" { return 3 }
    default { return 9 }
  }
}

function Find-FeatureById {
  param([object]$Prd, [string]$Id)
  return $Prd.features | Where-Object { $_.id -eq $Id } | Select-Object -First 1
}

function Is-Unblocked {
  param([object]$Prd, [object]$Feature)

  if ($Feature.blocked_reason) {
    return $false
  }

  if (-not $Feature.depends_on -or $Feature.depends_on.Count -eq 0) {
    return $true
  }

  foreach ($depId in $Feature.depends_on) {
    $dep = Find-FeatureById -Prd $Prd -Id $depId
    if (-not $dep -or -not $dep.passes) {
      return $false
    }
  }

  return $true
}

function Select-NextFeature {
  param([object]$Prd)

  $candidates = @()

  foreach ($feature in $Prd.features) {
    if (-not $feature.passes -and (Is-Unblocked -Prd $Prd -Feature $feature)) {
      $candidates += $feature
    }
  }

  if ($candidates.Count -eq 0) {
    return $null
  }

  $sorted = $candidates | Sort-Object `
    @{ Expression = { Get-PriorityScore -Priority $_.priority } }, `
    @{ Expression = { [int]$_.id } }

  return $sorted | Select-Object -First 1
}

function Get-Slug {
  param([string]$Text)
  $slug = $Text.ToLowerInvariant() -replace "[^a-z0-9]+", "-"
  $slug = $slug.Trim("-")
  if ($slug.Length -gt 40) {
    $slug = $slug.Substring(0, 40).Trim("-")
  }
  if (-not $slug) {
    $slug = "story"
  }
  return $slug
}

function Ensure-FeatureBranch {
  param([object]$Feature)

  $current = Get-CurrentBranch
  if ($ProtectedBranches -notcontains $current) {
    return
  }

  if (-not $AutoCreateBranch) {
    return
  }

  $prefix = "feature"
  if ($Feature.type -eq "bug") { $prefix = "bugfix" }
  if ($Feature.type -eq "refactor") { $prefix = "refactor" }
  if ($Feature.type -eq "test") { $prefix = "test" }

  $slug = Get-Slug -Text $Feature.description
  $branchName = "$prefix/$($Feature.id)-$slug"

  Push-Location $rootDir
  try {
    Write-Log "Creating and switching to branch $branchName"
    git checkout -b $branchName | Out-Null
  } finally {
    Pop-Location
  }
}

function Run-CheckedCommand {
  param(
    [string]$WorkingDir,
    [string]$Command,
    [string]$FailureMessage
  )

  Push-Location $WorkingDir
  try {
    Write-Log "Running: $Command"
    Invoke-Expression $Command
    if ($LASTEXITCODE -ne 0) {
      throw $FailureMessage
    }
  } finally {
    Pop-Location
  }
}

function Get-FrontendScripts {
  $packagePath = Join-Path $rootDir "frontend/package.json"
  if (-not (Test-Path $packagePath)) {
    return @{}
  }

  $package = Get-Content $packagePath -Raw | ConvertFrom-Json
  $scripts = @{}
  if ($package.scripts) {
    $package.scripts.PSObject.Properties | ForEach-Object {
      $scripts[$_.Name] = $_.Value
    }
  }

  return $scripts
}

function Invoke-QualityGates {
  $backendDir = Join-Path $rootDir "backend"
  $frontendDir = Join-Path $rootDir "frontend"

  Run-CheckedCommand -WorkingDir $backendDir -Command ".\\mvnw.cmd verify" -FailureMessage "Backend gate failed: mvnw verify"

  Run-CheckedCommand -WorkingDir $frontendDir -Command "npm run build" -FailureMessage "Frontend gate failed: npm run build"

  $scripts = Get-FrontendScripts

  if ($scripts.ContainsKey("lint")) {
    Run-CheckedCommand -WorkingDir $frontendDir -Command "npm run lint" -FailureMessage "Frontend gate failed: npm run lint"
  } else {
    Write-Log "Skipping optional frontend lint gate (no lint script found)."
  }

  if ($scripts.ContainsKey("test")) {
    Run-CheckedCommand -WorkingDir $frontendDir -Command "npm run test" -FailureMessage "Frontend gate failed: npm run test"
  } else {
    Write-Log "Skipping optional frontend test gate (no test script found)."
  }
}

function Ensure-ProgressFile {
  if (-not (Test-Path $resolvedProgressPath)) {
    $dir = Split-Path -Parent $resolvedProgressPath
    if (-not (Test-Path $dir)) {
      New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
    Set-Content -Path $resolvedProgressPath -Value "=== Ralph Progress Log ==="
  }
}

function Resolve-AgentCommand {
  if ($AgentCommand) {
    return $AgentCommand
  }

  if ($AgentProfile -eq "manual") {
    return ""
  }

  if ($AgentProfile -eq "custom") {
    throw "AgentProfile 'custom' requires -AgentCommand to be provided."
  }

  if (-not (Test-Path $resolvedAgentProfilesPath)) {
    throw "Agent profiles file not found at $resolvedAgentProfilesPath. Create it from docs/ralph-agent-profiles.example.json."
  }

  $profilesJson = Get-Content $resolvedAgentProfilesPath -Raw | ConvertFrom-Json
  if (-not $profilesJson.$AgentProfile) {
    throw "Agent profile '$AgentProfile' is not defined in $resolvedAgentProfilesPath."
  }

  $profileCommand = [string]$profilesJson.$AgentProfile
  if (-not $profileCommand.Trim()) {
    throw "Agent profile '$AgentProfile' is empty in $resolvedAgentProfilesPath."
  }

  return $profileCommand
}

function Stage-And-Commit {
  param([object]$Feature)

  Push-Location $rootDir
  try {
    git add -A
    git diff --cached --quiet
    if ($LASTEXITCODE -eq 0) {
      throw "No staged changes found. Refusing to mark story complete."
    }

    $description = $Feature.description
    if ($description.Length -gt 90) {
      $description = $description.Substring(0, 90).Trim()
    }
    $msg = "feat(ralph): [$($Feature.id)] $description"
    git commit -m $msg | Out-Null
  } finally {
    Pop-Location
  }
}

Ensure-ProgressFile
Ensure-BranchSafety

$effectiveAgentCommand = Resolve-AgentCommand

Write-Log "Starting Ralph loop in '$RunMode' mode. Max iterations: $MaxIterations"
Write-Log "Agent profile: $AgentProfile"

for ($i = 1; $i -le $MaxIterations; $i++) {
  $prd = Load-Prd
  $feature = Select-NextFeature -Prd $prd

  if (-not $feature) {
    Write-Log "No remaining unblocked features. Ralph loop complete."
    Append-Progress "COMPLETE: no remaining unblocked features"
    break
  }

  Ensure-FeatureBranch -Feature $feature

  Write-Log "Iteration ${i}: selected [$($feature.id)] $($feature.description)"
  Append-Progress "START [$($feature.id)] $($feature.description)"

  $storyContextPath = Join-Path $rootDir ".ralph/current-story.json"
  $feature | ConvertTo-Json -Depth 100 | Set-Content -Path $storyContextPath

  try {
    if ($effectiveAgentCommand) {
      Run-CheckedCommand -WorkingDir $rootDir -Command $effectiveAgentCommand -FailureMessage "Agent command failed"
    } else {
      Write-Log "Manual mode: implement the story, then press Enter to run quality gates."
      Write-Host "Current story JSON: $storyContextPath"
      [void](Read-Host "Press Enter when ready")
    }

    Invoke-QualityGates

    $target = Find-FeatureById -Prd $prd -Id $feature.id
    $target.passes = $true
    $target.blocked_reason = $null
    $target.iterations_taken = [int]$target.iterations_taken + 1

    Save-Prd -Prd $prd
    Stage-And-Commit -Feature $feature

    Append-Progress "PASS [$($feature.id)] committed successfully"
    Write-Log "Story [$($feature.id)] completed and committed."
  } catch {
    $target = Find-FeatureById -Prd $prd -Id $feature.id
    $target.iterations_taken = [int]$target.iterations_taken + 1
    $target.blocked_reason = "Last failure: $($_.Exception.Message)"
    Save-Prd -Prd $prd

    Append-Progress "FAIL [$($feature.id)] $($_.Exception.Message)"
    Write-Log "Iteration failed for [$($feature.id)]: $($_.Exception.Message)"

    if ($RunMode -eq "once") {
      throw
    }
  }
}

Write-Log "Ralph loop finished."
