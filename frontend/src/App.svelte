<script>
  import { Button, Modal } from 'flowbite-svelte';
  import TransactionList from './components/TransactionList.svelte';
  import TransactionForm from './components/TransactionForm.svelte';
  import './app.css';

  let showModal = false;
  let editingTransaction = null;
  let transactionListRef;

  function openCreateModal() {
    editingTransaction = null;
    showModal = true;
  }

  function openEditModal(transaction) {
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

  async function handleDelete(id) {
    if (confirm('Are you sure you want to delete this transaction?')) {
      try {
        const { transactionService } = await import('./services/transactionService');
        await transactionService.delete(id);
        if (transactionListRef) {
          await transactionListRef.loadTransactions();
        }
      } catch (err) {
        alert('Failed to delete transaction: ' + err.message);
      }
    }
  }
</script>

<div class="min-h-screen bg-gray-50 dark:bg-gray-900">
  <div class="container mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-4xl font-bold text-gray-900 dark:text-white">
        PayPal Clone - Transaction Manager
      </h1>
      <Button color="blue" on:click={openCreateModal}>
        Create Transaction
      </Button>
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
