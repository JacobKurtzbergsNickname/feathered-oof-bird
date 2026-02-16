export type TransactionStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export interface Transaction {
  id: string;
  sender: string;
  receiver: string;
  amount: number;
  currency: string;
  description?: string;
  status: TransactionStatus;
  createdAt: string;
}

export interface TransactionInput {
  sender: string;
  receiver: string;
  amount: number;
  currency: string;
  description?: string;
  status: TransactionStatus;
}
