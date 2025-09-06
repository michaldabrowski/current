import { defineStore } from "pinia";
import { ref, computed } from "vue";
import {
  accountsApi,
  transactionsApi,
  type Account,
  type Transaction,
  type Holding,
} from "@/services/api";

export const useCurrentStore = defineStore("current", () => {
  // State
  const accounts = ref<Account[]>([]);
  const currentAccount = ref<Account | null>(null);
  const transactions = ref<Transaction[]>([]);
  const holdings = ref<Holding[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Computed
  const totalValue = computed(() => {
    if (!currentAccount.value) return 0;

    const holdingsValue = holdings.value.reduce((sum, holding) => {
      // For demo purposes, using average price as current price
      // In real app, you'd fetch current market prices
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

  // Actions
  const fetchAccounts = async () => {
    try {
      loading.value = true;
      error.value = null;

      const response = await accountsApi.getAll();
      accounts.value = response.data;

      // Set first account as current if none selected
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
      // Select the new account
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

      // Refresh account data
      await selectAccount(currentAccount.value.id);
    } catch (err) {
      error.value = "Failed to create transaction";
      console.error("Error creating transaction:", err);
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
    loading,
    error,

    // Computed
    totalValue,
    stocksValue,
    cryptoValue,
    stocksCount,
    cryptoCount,

    // Actions
    fetchAccounts,
    selectAccount,
    createAccount,
    createTransaction,
  };
});
