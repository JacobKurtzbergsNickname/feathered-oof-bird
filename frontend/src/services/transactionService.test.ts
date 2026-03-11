import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { transactionService } from './transactionService';

describe('transactionService', () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
  });

  it('getAll returns parsed transactions when response is ok', async () => {
    const payload = [
      {
        id: 'tx-1',
        sender: 'alice',
        receiver: 'bob',
        amount: 10,
        currency: 'USD',
        description: 'test',
        status: 'PENDING',
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z',
      },
    ];

    globalThis.fetch = vi.fn(async () =>
      new Response(JSON.stringify(payload), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    ) as typeof fetch;

    const data = await transactionService.getAll();

    expect(data).toEqual(payload);
    expect(globalThis.fetch).toHaveBeenCalledTimes(1);
  });

  it('create throws when backend responds with non-ok status', async () => {
    globalThis.fetch = vi.fn(async () => new Response(null, { status: 500 })) as typeof fetch;

    await expect(
      transactionService.create({
        sender: 'alice',
        receiver: 'bob',
        amount: 10,
        currency: 'USD',
        description: 'test',
        status: 'PENDING',
      }),
    ).rejects.toThrow('Failed to create transaction');
  });
});
