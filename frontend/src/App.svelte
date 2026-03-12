<script lang="ts">
  import { Button, Modal } from 'flowbite-svelte';
  import AuthForm from './components/AuthForm.svelte';
  import TransactionList from './components/TransactionList.svelte';
  import TransactionForm from './components/TransactionForm.svelte';
  import { authService } from './services/authService';
  import { transactionService } from './services/transactionService';
  import { authSession } from './stores/authStore';
  import type { Transaction } from './lib/types';
  import './app.css';

  type TransactionListHandle = {
    loadTransactions: () => Promise<void>;
  };

  let showModal = false;
  let editingTransaction: Transaction | null = null;
  let transactionListRef: TransactionListHandle | null = null;

  function handleLogout() {
    closeModal();
    authService.logout();
  }

  function openCreateModal() {
    editingTransaction = null;
    showModal = true;
  }

  function openEditModal(transaction: Transaction) {
    editingTransaction = transaction;
    showModal = true;
  }

  function closeModal() {
    showModal = false;
    editingTransaction = null;
  }

  async function handleFormSuccess() {
    closeModal();
    if (transactionListRef) {
      await transactionListRef.loadTransactions();
    }
  }

  async function handleDelete(id: string) {
    if (window.confirm('Are you sure you want to delete this transaction?')) {
      try {
        await transactionService.delete(id);
        if (transactionListRef) {
          await transactionListRef.loadTransactions();
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unknown error';
        window.alert('Failed to delete transaction: ' + message);
      }
    }
  }
</script>

{#if !$authSession}
  <AuthForm />
{:else}
  <div class="min-h-screen bg-gray-50">
    <div class="container mx-auto px-4 py-8">
      <div class="mb-8 flex flex-col gap-4 rounded-3xl bg-white p-6 shadow-sm md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-sm uppercase tracking-[0.3em] text-slate-500">Signed In</p>
          <h1 class="text-4xl font-bold text-gray-900">Transaction Manager</h1>
          <p class="mt-2 text-sm text-slate-600">
            {$authSession.user.email} · {$authSession.user.role}
          </p>
        </div>
        <div class="flex gap-3">
          <Button color="blue" onclick={openCreateModal}>
            Create Transaction
          </Button>
          <Button color="alternative" onclick={handleLogout}>
            Logout
          </Button>
        </div>
      </div>

      <TransactionList
        bind:this={transactionListRef}
        onEdit={openEditModal}
        onDelete={handleDelete}
      />

      <Modal bind:open={showModal} size="lg" autoclose={false}>
        <TransactionForm
          transaction={editingTransaction}
          onSuccess={handleFormSuccess}
          onCancel={closeModal}
        />
      </Modal>
    </div>
  </div>
{/if}
