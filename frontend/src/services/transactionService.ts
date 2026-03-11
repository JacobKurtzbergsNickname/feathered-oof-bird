import type { Transaction, TransactionInput } from '../lib/types';
import { apiFetch, readErrorMessage } from './httpClient';

export const transactionService = {
  async getAll(): Promise<Transaction[]> {
    const response = await apiFetch('/api/transactions');
    if (!response.ok) throw new Error(await readErrorMessage(response, 'Failed to fetch transactions'));
    return response.json() as Promise<Transaction[]>;
  },

  async getById(id: string): Promise<Transaction> {
    const response = await apiFetch(`/api/transactions/${id}`);
    if (!response.ok) throw new Error(await readErrorMessage(response, 'Failed to fetch transaction'));
    return response.json() as Promise<Transaction>;
  },

  async create(transaction: TransactionInput): Promise<Transaction> {
    const response = await apiFetch('/api/transactions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(transaction),
    });
    if (!response.ok) throw new Error(await readErrorMessage(response, 'Failed to create transaction'));
    return response.json() as Promise<Transaction>;
  },

  async update(id: string, transaction: TransactionInput): Promise<Transaction> {
    const response = await apiFetch(`/api/transactions/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(transaction),
    });
    if (!response.ok) throw new Error(await readErrorMessage(response, 'Failed to update transaction'));
    return response.json() as Promise<Transaction>;
  },

  async delete(id: string): Promise<void> {
    const response = await apiFetch(`/api/transactions/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) throw new Error(await readErrorMessage(response, 'Failed to delete transaction'));
  },
};
