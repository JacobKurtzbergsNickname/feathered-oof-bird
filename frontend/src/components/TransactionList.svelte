<script lang="ts">
  import { onMount } from 'svelte';
  import { Table, Button, Spinner, Alert } from 'flowbite-svelte';
  import { transactionService } from '../services/transactionService';
  import type { Transaction } from '../lib/types';

  export let onEdit: (transaction: Transaction) => void;
  export let onDelete: (id: string) => void;

  let transactions: Transaction[] = [];
  let loading = true;
  let error: string | null = null;

  onMount(async () => {
    await loadTransactions();
  });

  export async function loadTransactions() {
    try {
      loading = true;
      error = null;
      transactions = await transactionService.getAll();
    } catch (err) {
      error = err instanceof Error ? err.message : 'Unable to load transactions.';
    } finally {
      loading = false;
    }
  }

  function formatDate(dateString: string) {
    return new Date(dateString).toLocaleString();
  }

  function formatAmount(amount: number, currency: string) {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(amount);
  }
</script>

<div class="w-full">
  <h2 class="text-2xl font-bold mb-4">Transactions</h2>

  {#if loading}
    <div class="flex justify-center items-center p-8">
      <Spinner size="8" />
    </div>
  {:else if error}
    <Alert color="red" class="mb-4">
      {error}
    </Alert>
  {:else if transactions.length === 0}
    <Alert color="blue">No transactions found. Create your first transaction!</Alert>
  {:else}
    <Table hoverable={true} striped={true}>
      <thead>
        <tr>
          <th>ID</th>
          <th>Sender</th>
          <th>Receiver</th>
          <th>Amount</th>
          <th>Status</th>
          <th>Date</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {#each transactions as transaction}
          <tr>
            <td>{transaction.id}</td>
            <td>{transaction.sender}</td>
            <td>{transaction.receiver}</td>
            <td>{formatAmount(transaction.amount, transaction.currency)}</td>
            <td>
              <span class="px-2 py-1 text-xs font-semibold rounded-full 
                {transaction.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 
                 transaction.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' : 
                 transaction.status === 'FAILED' ? 'bg-red-100 text-red-800' : 
                 'bg-gray-100 text-gray-800'}">
                {transaction.status}
              </span>
            </td>
            <td>{formatDate(transaction.createdAt)}</td>
            <td class="space-x-2">
              <Button size="xs" color="blue" on:click={() => onEdit(transaction)}>
                Edit
              </Button>
              <Button size="xs" color="red" on:click={() => onDelete(transaction.id)}>
                Delete
              </Button>
            </td>
          </tr>
        {/each}
      </tbody>
    </Table>
  {/if}
</div>
