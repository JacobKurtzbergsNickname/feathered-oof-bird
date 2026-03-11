<script lang="ts">
  import { Alert, Button, Card, Input, Label, Select } from 'flowbite-svelte';
  import { authService } from '../services/authService';
  import type { RegisterRequest } from '../lib/auth';

  type AuthMode = 'login' | 'register';

  let mode: AuthMode = 'login';
  let email = '';
  let password = '';
  let role: RegisterRequest['role'] = 'PERSONAL';
  let loading = false;
  let error: string | null = null;

  function switchMode(nextMode: AuthMode) {
    mode = nextMode;
    error = null;
  }

  async function handleSubmit() {
    try {
      loading = true;
      error = null;

      if (mode === 'register') {
        await authService.register({ email, password, role });
      } else {
        await authService.login({ email, password });
      }
    } catch (err) {
      error = err instanceof Error ? err.message : 'Authentication failed.';
    } finally {
      loading = false;
    }
  }
</script>

<div class="min-h-screen bg-slate-100 px-4 py-10">
  <div class="mx-auto w-full max-w-md">
    <Card class="border border-slate-200 bg-white p-6 shadow-lg sm:p-7">
      <div class="space-y-3">
        <p class="text-sm uppercase tracking-[0.3em] text-slate-500">Access</p>
        <h1 class="text-3xl font-bold text-slate-900">Feathered Oof Bird</h1>
        <p class="text-sm text-slate-600">
          {mode === 'register'
            ? 'Create a local development account and continue straight into the app.'
            : 'Sign in with the local JWT provider used for development.'}
        </p>
      </div>

      <div class="mt-7 flex gap-2 rounded-xl bg-slate-100 p-1.5">
        <Button color={mode === 'login' ? 'dark' : 'alternative'} class="flex-1" onclick={() => switchMode('login')}>
          Login
        </Button>
        <Button color={mode === 'register' ? 'dark' : 'alternative'} class="flex-1" onclick={() => switchMode('register')}>
          Register
        </Button>
      </div>

      {#if error}
        <Alert color="red" class="mt-5">{error}</Alert>
      {/if}

      <form class="mt-7 space-y-5" on:submit|preventDefault={handleSubmit}>
        <div>
          <Label for="email" class="mb-2">Email</Label>
          <Input id="email" type="email" bind:value={email} autocomplete="email" required />
        </div>

        <div>
          <Label for="password" class="mb-2">Password</Label>
          <Input
            id="password"
            type="password"
            bind:value={password}
            autocomplete={mode === 'register' ? 'new-password' : 'current-password'}
            minlength={8}
            required
          />
        </div>

        {#if mode === 'register'}
          <div>
            <Label for="role" class="mb-2">Account Type</Label>
            <Select id="role" bind:value={role}>
              <option value="PERSONAL">Personal</option>
              <option value="BUSINESS">Business</option>
            </Select>
          </div>
        {/if}

        <Button type="submit" color="dark" class="w-full" disabled={loading}>
          {#if loading}
            Working...
          {:else if mode === 'register'}
            Create Account
          {:else}
            Login
          {/if}
        </Button>
      </form>
    </Card>
  </div>
</div>