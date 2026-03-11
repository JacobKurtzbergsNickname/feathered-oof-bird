/**
 * Asks Auth0 for an access token using the Client Credentials grant.
 *
 * By default, prints only the bare access token so the output can be piped
 * directly into test-api.ts:
 *
 *   npx tsx src/test-api.ts --token $(npx tsx src/get-token.ts)
 *
 * Pass --verbose to see the full token response (expires_in, scopes, etc.).
 */

import "dotenv/config";

// ---------------------------------------------------------------------------
// Config
// ---------------------------------------------------------------------------

interface Auth0Config {
  domain: string;
  clientId: string;
  clientSecret: string;
  audience: string;
}

function loadConfig(): Auth0Config {
  const domain = process.env.AUTH0_DOMAIN;
  const clientId = process.env.AUTH0_CLIENT_ID;
  const clientSecret = process.env.AUTH0_CLIENT_SECRET;
  const audience = process.env.AUTH0_AUDIENCE;

  const missing: string[] = [];
  if (!domain) missing.push("AUTH0_DOMAIN");
  if (!clientId) missing.push("AUTH0_CLIENT_ID");
  if (!clientSecret) missing.push("AUTH0_CLIENT_SECRET");
  if (!audience) missing.push("AUTH0_AUDIENCE");

  if (missing.length > 0) {
    console.error(
      `Missing required environment variables: ${missing.join(", ")}\n` +
        "Copy .env.example to .env and fill in your Auth0 credentials."
    );
    process.exit(1);
  }

  return { domain: domain!, clientId: clientId!, clientSecret: clientSecret!, audience: audience! };
}

// ---------------------------------------------------------------------------
// Token request
// ---------------------------------------------------------------------------

interface TokenResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
  scope?: string;
}

interface TokenErrorResponse {
  error: string;
  error_description: string;
}

async function requestToken(config: Auth0Config): Promise<TokenResponse> {
  const url = `https://${config.domain}/oauth/token`;

  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      grant_type: "client_credentials",
      client_id: config.clientId,
      client_secret: config.clientSecret,
      audience: config.audience,
    }),
  });

  if (!res.ok) {
    const body = (await res.json()) as TokenErrorResponse;
    throw new Error(
      `Auth0 returned ${res.status}: ${body.error} – ${body.error_description}`
    );
  }

  return (await res.json()) as TokenResponse;
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

async function main(): Promise<void> {
  const verbose = process.argv.includes("--verbose");
  const config = loadConfig();

  try {
    const token = await requestToken(config);

    if (verbose) {
      console.error("--- Auth0 Token Response ---");
      console.error(`  token_type : ${token.token_type}`);
      console.error(`  expires_in : ${token.expires_in}s`);
      console.error(`  scope      : ${token.scope ?? "(none)"}`);
      console.error("----------------------------");
    }

    // Always write the bare token to stdout so it can be captured via $().
    process.stdout.write(token.access_token);
  } catch (err) {
    console.error(
      `Failed to obtain token: ${err instanceof Error ? err.message : err}`
    );
    process.exit(1);
  }
}

main();
