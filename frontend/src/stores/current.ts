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
      console.log("Fetching accounts..."); // DEBUG

      const response = await accountsApi.getAll();
      accounts.value = response.data;
      console.log("Fetched accounts:", accounts.value); // DEBUG

      // Set first account as current if none selected
      if (accounts.value.length > 0 && !currentAccount.value) {
        console.log("Auto-selecting first account"); // DEBUG
        await selectAccount(accounts.value[0].id);
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

      console.log("Selecting account with ID:", accountId); // DEBUG

      const [accountRes, transactionsRes, holdingsRes] = await Promise.all([
        accountsApi.getById(accountId),
        transactionsApi.getByAccount(accountId),
        transactionsApi.getHoldings(accountId),
      ]);

      console.log("Account API response:", accountRes.data); // DEBUG
      console.log("Transactions API response:", transactionsRes.data); // DEBUG
      console.log("Holdings API response:", holdingsRes.data); // DEBUG

      currentAccount.value = accountRes.data;
      transactions.value = transactionsRes.data;
      holdings.value = holdingsRes.data;

      console.log("Current account set to:", currentAccount.value); // DEBUG
      console.log("Is currentAccount truthy?", !!currentAccount.value); // DEBUG
    } catch (err) {
      error.value = "Failed to fetch account data";
      console.error("Error fetching account data:", err);
    } finally {
      loading.value = false;
      console.log("Loading set to false, currentAccount is:", currentAccount.value); // DEBUG
    }
  };

  const createAccount = async (name: string, initialBalance?: number) => {
    try {
      loading.value = true;
      error.value = null;

      console.log("Creating account:", { name, initialBalance }); // DEBUG

      const response = await accountsApi.create({ name, initialBalance });
      console.log("Account created:", response.data); // DEBUG

      accounts.value.push(response.data);
      console.log("Accounts after push:", accounts.value); // DEBUG

      // Select the new account
      console.log("Selecting new account with ID:", response.data.id); // DEBUG
      await selectAccount(response.data.id);

      console.log("Current account after selection:", currentAccount.value); // DEBUG
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
