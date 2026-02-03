const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const transactionService = {
    async getAll() {
        const response = await fetch(`${API_URL}/api/transactions`);
        if (!response.ok) throw new Error('Failed to fetch transactions');
        return response.json();
    },

    async getById(id) {
        const response = await fetch(`${API_URL}/api/transactions/${id}`);
        if (!response.ok) throw new Error('Failed to fetch transaction');
        return response.json();
    },

    async create(transaction) {
        const response = await fetch(`${API_URL}/api/transactions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(transaction),
        });
        if (!response.ok) throw new Error('Failed to create transaction');
        return response.json();
    },

    async update(id, transaction) {
        const response = await fetch(`${API_URL}/api/transactions/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(transaction),
        });
        if (!response.ok) throw new Error('Failed to update transaction');
        return response.json();
    },

    async delete(id) {
        const response = await fetch(`${API_URL}/api/transactions/${id}`, {
            method: 'DELETE',
        });
        if (!response.ok) throw new Error('Failed to delete transaction');
    },
};
