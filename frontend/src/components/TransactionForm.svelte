<script>
  import { Button, Input, Label, Select, Textarea, Alert } from 'flowbite-svelte';
  import { transactionService } from '../services/transactionService';

  export let transaction = null;
  export let onSuccess;
  export let onCancel;

  let formData = transaction ? { ...transaction } : {
    sender: '',
    receiver: '',
    amount: '',
    currency: 'USD',
    description: '',
    status: 'PENDING'
  };

  let loading = false;
  let error = null;

  const currencies = [
    { value: 'USD', name: 'US Dollar' },
    { value: 'EUR', name: 'Euro' },
    { value: 'GBP', name: 'British Pound' },
    { value: 'JPY', name: 'Japanese Yen' },
  ];

  const statuses = [
    { value: 'PENDING', name: 'Pending' },
    { value: 'COMPLETED', name: 'Completed' },
    { value: 'FAILED', name: 'Failed' },
    { value: 'CANCELLED', name: 'Cancelled' },
  ];

  async function handleSubmit() {
    try {
      loading = true;
      error = null;

      const data = {
        ...formData,
        amount: parseFloat(formData.amount)
      };

      if (transaction) {
        await transactionService.update(transaction.id, data);
      } else {
        await transactionService.create(data);
      }

      onSuccess();
    } catch (err) {
      error = err.message;
    } finally {
      loading = false;
    }
  }
</script>

<div class="w-full max-w-2xl">
  <h2 class="text-2xl font-bold mb-4">
    {transaction ? 'Edit Transaction' : 'Create Transaction'}
  </h2>

  {#if error}
    <Alert color="red" class="mb-4">
      {error}
    </Alert>
  {/if}

  <form on:submit|preventDefault={handleSubmit} class="space-y-4">
    <div>
      <Label for="sender" class="mb-2">Sender</Label>
      <Input
        id="sender"
        type="text"
        bind:value={formData.sender}
        placeholder="Enter sender name"
        required
      />
    </div>

    <div>
      <Label for="receiver" class="mb-2">Receiver</Label>
      <Input
        id="receiver"
        type="text"
        bind:value={formData.receiver}
        placeholder="Enter receiver name"
        required
      />
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div>
        <Label for="amount" class="mb-2">Amount</Label>
        <Input
          id="amount"
          type="number"
          step="0.01"
          bind:value={formData.amount}
          placeholder="0.00"
          required
        />
      </div>

      <div>
        <Label for="currency" class="mb-2">Currency</Label>
        <Select id="currency" bind:value={formData.currency} required>
          {#each currencies as currency}
            <option value={currency.value}>{currency.name}</option>
          {/each}
        </Select>
      </div>
    </div>

    <div>
      <Label for="status" class="mb-2">Status</Label>
      <Select id="status" bind:value={formData.status} required>
        {#each statuses as status}
          <option value={status.value}>{status.name}</option>
        {/each}
      </Select>
    </div>

    <div>
      <Label for="description" class="mb-2">Description (Optional)</Label>
      <Textarea
        id="description"
        bind:value={formData.description}
        placeholder="Enter transaction description"
        rows="3"
      />
    </div>

    <div class="flex space-x-2">
      <Button type="submit" color="blue" disabled={loading}>
        {loading ? 'Saving...' : transaction ? 'Update' : 'Create'}
      </Button>
      <Button type="button" color="alternative" on:click={onCancel}>
        Cancel
      </Button>
    </div>
  </form>
</div>
