import type { Transaction, TransactionInput } from '../lib/types';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export const transactionService = {
  async getAll(): Promise<Transaction[]> {
    const response = await fetch(`${API_URL}/api/transactions`);
    if (!response.ok) throw new Error('Failed to fetch transactions');
    return response.json() as Promise<Transaction[]>;
  },

  async getById(id: string): Promise<Transaction> {
    const response = await fetch(`${API_URL}/api/transactions/${id}`);
    if (!response.ok) throw new Error('Failed to fetch transaction');
    return response.json() as Promise<Transaction>;
  },

  async create(transaction: TransactionInput): Promise<Transaction> {
    const response = await fetch(`${API_URL}/api/transactions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(transaction),
    });
    if (!response.ok) throw new Error('Failed to create transaction');
    return response.json() as Promise<Transaction>;
  },

  async update(id: string, transaction: TransactionInput): Promise<Transaction> {
    const response = await fetch(`${API_URL}/api/transactions/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(transaction),
    });
    if (!response.ok) throw new Error('Failed to update transaction');
    return response.json() as Promise<Transaction>;
  },

  async delete(id: string): Promise<void> {
    const response = await fetch(`${API_URL}/api/transactions/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) throw new Error('Failed to delete transaction');
  },
};
