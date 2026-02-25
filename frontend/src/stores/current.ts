import { defineStore } from "pinia";
import { ref, computed } from "vue";
import {
  accountsApi,
  transactionsApi,
  snapshotsApi,
  demoApi,
  type Account,
  type Transaction,
  type Holding,
  type BalanceSnapshot,
} from "@/services/api";

export const useCurrentStore = defineStore("current", () => {
  // State
  const accounts = ref<Account[]>([]);
  const currentAccount = ref<Account | null>(null);
  const transactions = ref<Transaction[]>([]);
  const holdings = ref<Holding[]>([]);
  const snapshots = ref<BalanceSnapshot[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Computed
  const totalValue = computed(() => {
    if (!currentAccount.value) return 0;

    const holdingsValue = holdings.value.reduce((sum, holding) => {
      return sum + holding.quantity * holding.averagePrice;
    }, 0);

    return currentAccount.value.cashBalance + holdingsValue;
  });

  const stocksValue = computed(() => {
    return holdings.value
      .filter((h) => h.assetType === "STOCK")
      .reduce((sum, h) => sum + h.quantity * h.averagePrice, 0);
  });

  const cryptoValue = computed(() => {
    return holdings.value
      .filter((h) => h.assetType === "CRYPTO")
      .reduce((sum, h) => sum + h.quantity * h.averagePrice, 0);
  });

  const stocksCount = computed(() => {
    return holdings.value.filter((h) => h.assetType === "STOCK").length;
  });

  const cryptoCount = computed(() => {
    return holdings.value.filter((h) => h.assetType === "CRYPTO").length;
  });

  const recentTransactions = computed(() => {
    return transactions.value.slice(0, 8);
  });

  // Actions
  const fetchAccounts = async () => {
    try {
      loading.value = true;
      error.value = null;

      const response = await accountsApi.getAll();
      accounts.value = response.data;

      if (accounts.value.length > 0 && !currentAccount.value) {
        const firstAccount = accounts.value[0];
        if (firstAccount) {
          await selectAccount(firstAccount.id);
        }
      }
    } catch (err) {
      error.value = "Failed to fetch accounts";
      console.error("Error fetching accounts:", err);
    } finally {
      loading.value = false;
    }
  };

  const selectAccount = async (accountId: number) => {
    try {
      loading.value = true;
      error.value = null;

      const [accountRes, transactionsRes, holdingsRes] = await Promise.all([
        accountsApi.getById(accountId),
        transactionsApi.getByAccount(accountId),
        transactionsApi.getHoldings(accountId),
      ]);

      currentAccount.value = accountRes.data;
      transactions.value = transactionsRes.data;
      holdings.value = holdingsRes.data;

      // Fire-and-forget: record snapshot and fetch history
      recordSnapshot(accountId);
      fetchSnapshots(accountId);
    } catch (err) {
      error.value = "Failed to fetch account data";
      console.error("Error fetching account data:", err);
    } finally {
      loading.value = false;
    }
  };

  const createAccount = async (name: string, initialBalance?: number) => {
    try {
      loading.value = true;
      error.value = null;

      const response = await accountsApi.create({ name, initialBalance });

      accounts.value.push(response.data);
      await selectAccount(response.data.id);
    } catch (err) {
      error.value = "Failed to create account";
      console.error("Error creating account:", err);
    } finally {
      loading.value = false;
    }
  };

  const createTransaction = async (transactionData: {
    symbol: string;
    type: "BUY" | "SELL";
    assetType: "STOCK" | "CRYPTO";
    quantity: number;
    price: number;
    notes?: string;
  }) => {
    if (!currentAccount.value) {
      error.value = "No account selected";
      return;
    }

    try {
      loading.value = true;
      error.value = null;

      await transactionsApi.create({
        accountId: currentAccount.value.id,
        ...transactionData,
      });

      await selectAccount(currentAccount.value.id);
    } catch (err) {
      error.value = "Failed to create transaction";
      console.error("Error creating transaction:", err);
    } finally {
      loading.value = false;
    }
  };

  const recordSnapshot = async (accountId: number) => {
    try {
      await snapshotsApi.record(accountId);
    } catch (err) {
      console.error("Error recording snapshot:", err);
    }
  };

  const fetchSnapshots = async (accountId: number) => {
    try {
      const response = await snapshotsApi.getHistory(accountId);
      snapshots.value = response.data;
    } catch (err) {
      console.error("Error fetching snapshots:", err);
    }
  };

  const deleteTransaction = async (transactionId: number) => {
    try {
      loading.value = true;
      error.value = null;

      await transactionsApi.delete(transactionId);

      if (currentAccount.value) {
        await selectAccount(currentAccount.value.id);
      }
    } catch (err) {
      error.value = "Failed to delete transaction";
      console.error("Error deleting transaction:", err);
    } finally {
      loading.value = false;
    }
  };

  const deleteAccount = async (accountId: number): Promise<'deleted' | 'has_transactions' | 'error'> => {
    try {
      loading.value = true;
      error.value = null;

      await accountsApi.delete(accountId);

      accounts.value = accounts.value.filter((a) => a.id !== accountId);

      if (currentAccount.value?.id === accountId) {
        currentAccount.value = null;
        transactions.value = [];
        holdings.value = [];
        snapshots.value = [];

        if (accounts.value.length > 0) {
          const first = accounts.value[0];
          if (first) {
            await selectAccount(first.id);
          }
        }
      }
      return 'deleted';
    } catch (err: unknown) {
      if (
        typeof err === 'object' && err !== null && 'response' in err &&
        typeof (err as Record<string, unknown>).response === 'object' &&
        (err as { response: { status: number } }).response?.status === 409
      ) {
        error.value = null;
        return 'has_transactions';
      }
      error.value = "Failed to delete account";
      console.error("Error deleting account:", err);
      return 'error';
    } finally {
      loading.value = false;
    }
  };

  const deleteAllTransactions = async (accountId: number): Promise<number> => {
    try {
      loading.value = true;
      error.value = null;

      const response = await transactionsApi.deleteAllByAccount(accountId);
      const count = response.data.deleted;

      if (currentAccount.value?.id === accountId) {
        await selectAccount(accountId);
      }
      return count;
    } catch (err) {
      error.value = "Failed to delete transactions";
      console.error("Error deleting transactions:", err);
      return 0;
    } finally {
      loading.value = false;
    }
  };

  const adjustCash = async (amount: number) => {
    if (!currentAccount.value) {
      error.value = "No account selected";
      return;
    }

    try {
      loading.value = true;
      error.value = null;

      const response = await accountsApi.adjustCash(
        currentAccount.value.id,
        amount,
      );
      currentAccount.value = response.data;
    } catch (err) {
      error.value = "Failed to adjust cash balance";
      console.error("Error adjusting cash:", err);
    } finally {
      loading.value = false;
    }
  };

  const seedDemoData = async () => {
    try {
      loading.value = true;
      error.value = null;

      const response = await demoApi.seed();
      await fetchAccounts();
      await selectAccount(response.data.id);
    } catch (err) {
      error.value = "Failed to seed demo data";
      console.error("Error seeding demo data:", err);
    } finally {
      loading.value = false;
    }
  };

  return {
    // State
    accounts,
    currentAccount,
    transactions,
    holdings,
    snapshots,
    loading,
    error,

    // Computed
    totalValue,
    stocksValue,
    cryptoValue,
    stocksCount,
    cryptoCount,
    recentTransactions,

    // Actions
    fetchAccounts,
    selectAccount,
    createAccount,
    createTransaction,
    deleteTransaction,
    deleteAccount,
    deleteAllTransactions,
    adjustCash,
    seedDemoData,
    recordSnapshot,
    fetchSnapshots,
  };
});
