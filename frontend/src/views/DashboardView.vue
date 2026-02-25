<template>
  <div>
    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="text-sm text-gray-400">Loading...</div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-xl p-4 mb-6">
      <div class="text-red-800 text-sm">{{ error }}</div>
      <button @click="refresh" class="mt-2 btn-secondary text-sm">Try Again</button>
    </div>

    <!-- No Account State -->
    <div v-else-if="!currentAccount" class="text-center py-16">
      <h2 class="text-xl font-semibold text-gray-900 mb-2">Welcome to Current</h2>
      <p class="text-gray-400 mb-6 text-sm">
        Create your first account to start tracking your investments.
      </p>
      <div class="flex items-center justify-center gap-3">
        <button @click="showCreateAccount = true" class="btn-primary">
          Create Account
        </button>
        <button @click="handleSeedDemo" class="btn-secondary text-sm">
          Load Demo Data
        </button>
      </div>

      <!-- Inline create account modal for no-account state -->
      <div
        v-if="showCreateAccount"
        class="fixed inset-0 bg-black/40 backdrop-blur-xs flex items-center justify-center z-50"
        @click="showCreateAccount = false"
      >
        <div class="bg-white rounded-2xl p-6 max-w-md w-full mx-4 shadow-xl" @click.stop>
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Create Account</h2>
          <form @submit.prevent="handleCreateAccount">
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Account Name
              </label>
              <input
                v-model="newAccountName"
                type="text"
                required
                class="input-field"
                placeholder="My Investment Account"
              />
            </div>
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Initial Cash Balance (optional)
              </label>
              <input
                v-model.number="newAccountBalance"
                type="number"
                step="0.01"
                class="input-field"
                placeholder="0.00"
              />
            </div>
            <div class="flex gap-2">
              <button
                type="button"
                @click="showCreateAccount = false"
                class="btn-secondary flex-1"
              >
                Cancel
              </button>
              <button type="submit" class="btn-primary flex-1">Create</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Dashboard Content -->
    <div v-else>
      <!-- Header -->
      <header class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-lg font-semibold text-gray-900">Dashboard</h1>
        </div>
        <div class="flex items-center gap-2">
          <AccountSelector
            :accounts="accounts"
            :current-account="currentAccount"
            @select="handleAccountChange"
            @create="handleCreateAccountFromSelector"
            @delete="requestDeleteAccount"
          />
          <button
            @click="showCashModal = true"
            class="btn-secondary flex items-center gap-1.5 text-sm py-1.5 px-3"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343
                   2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0
                   0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
            </svg>
            Cash
          </button>
          <button
            @click="showAddTransaction = true"
            class="btn-primary flex items-center gap-1.5 text-sm py-1.5 px-3"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 4v16m8-8H4" />
            </svg>
            Add Transaction
          </button>
        </div>
      </header>

      <!-- Balance Hero -->
      <BalanceHero
        :total-value="totalValue"
        :stocks-value="stocksValue"
        :crypto-value="cryptoValue"
        :cash-balance="currentAccount.cashBalance"
        :stocks-count="stocksCount"
        :crypto-count="cryptoCount"
      />

      <!-- Balance Chart -->
      <BalanceChart :snapshots="snapshots" />

      <!-- Recent Transactions + Holdings -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Recent Transactions (takes 1 column) -->
        <RecentTransactions
          :transactions="visibleRecentTransactions"
          @delete="requestDeleteTransaction"
        />

        <!-- Holdings (takes 2 columns) -->
        <div class="lg:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-6">
          <HoldingsList
            :holdings="stockHoldings"
            title="Stocks"
            empty-message="No stock investments yet"
            accent="blue"
          />
          <HoldingsList
            :holdings="cryptoHoldings"
            title="Crypto"
            empty-message="No crypto investments yet"
            accent="amber"
          />
        </div>
      </div>
    </div>

    <!-- Add Transaction Modal -->
    <AddTransactionModal
      :show="showAddTransaction"
      @close="showAddTransaction = false"
      @submit="handleCreateTransaction"
    />

    <!-- Cash Modal -->
    <CashModal
      :show="showCashModal"
      :current-balance="currentAccount?.cashBalance ?? 0"
      @close="showCashModal = false"
      @submit="handleAdjustCash"
    />

    <!-- Delete Account Confirm -->
    <ConfirmModal
      :show="deleteAccountConfirm.show"
      :title="deleteAccountConfirm.title"
      :message="deleteAccountConfirm.message"
      :confirm-text="deleteAccountConfirm.confirmText"
      :destructive="true"
      @confirm="confirmDeleteAccount"
      @cancel="deleteAccountConfirm.show = false"
    />

    <!-- Delete All Transactions Confirm -->
    <ConfirmModal
      :show="deleteAllTxConfirm.show"
      title="Delete All Transactions"
      :message="deleteAllTxConfirm.message"
      confirm-text="Delete All"
      :destructive="true"
      @confirm="confirmDeleteAllTransactions"
      @cancel="deleteAllTxConfirm.show = false"
    />

    <!-- Undo Toast for transaction delete -->
    <UndoToast
      :visible="undoState.show"
      :message="undoState.message"
      @undo="handleUndoDelete"
      @expire="handleUndoExpire"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCurrentStore } from '@/stores/current'
import { storeToRefs } from 'pinia'
import BalanceHero from '@/components/BalanceHero.vue'
import BalanceChart from '@/components/BalanceChart.vue'
import RecentTransactions from '@/components/RecentTransactions.vue'
import HoldingsList from '@/components/HoldingsList.vue'
import AccountSelector from '@/components/AccountSelector.vue'
import AddTransactionModal from '@/components/AddTransactionModal.vue'
import CashModal from '@/components/CashModal.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import UndoToast from '@/components/UndoToast.vue'

const store = useCurrentStore()

const {
  accounts,
  currentAccount,
  holdings,
  snapshots,
  loading,
  error,
  totalValue,
  stocksValue,
  cryptoValue,
  stocksCount,
  cryptoCount,
  recentTransactions,
} = storeToRefs(store)

// Local state
const showAddTransaction = ref(false)
const showCashModal = ref(false)
const showCreateAccount = ref(false)
const newAccountName = ref('')
const newAccountBalance = ref<number | null>(null)

// Delete account confirmation state
const deleteAccountConfirm = ref({
  show: false,
  accountId: 0,
  accountName: '',
  title: '',
  message: '',
  confirmText: 'Delete',
})

// Delete all transactions confirmation state
const deleteAllTxConfirm = ref({
  show: false,
  accountId: 0,
  accountName: '',
  message: '',
})

// Undo-delete state for transactions
const undoState = ref({
  show: false,
  message: '',
  transactionId: 0,
})
const hiddenTransactionIds = ref<Set<number>>(new Set())

// Computed
const stockHoldings = computed(() =>
  holdings.value.filter(h => h.assetType === 'STOCK'),
)
const cryptoHoldings = computed(() =>
  holdings.value.filter(h => h.assetType === 'CRYPTO'),
)
const visibleRecentTransactions = computed(() =>
  recentTransactions.value.filter(tx => !hiddenTransactionIds.value.has(tx.id)),
)

// Methods
const refresh = async () => {
  await store.fetchAccounts()
}

const handleAccountChange = (accountId: number) => {
  store.selectAccount(accountId)
}

const handleCreateAccount = async () => {
  if (!newAccountName.value.trim()) return
  await store.createAccount(
    newAccountName.value.trim(),
    newAccountBalance.value || undefined,
  )
  newAccountName.value = ''
  newAccountBalance.value = null
  showCreateAccount.value = false
}

const handleCreateAccountFromSelector = async (
  name: string,
  initialBalance?: number,
) => {
  await store.createAccount(name, initialBalance)
}

// Account delete flow — uses ConfirmModal
const requestDeleteAccount = (accountId: number, accountName: string) => {
  deleteAccountConfirm.value = {
    show: true,
    accountId,
    accountName,
    title: `Delete "${accountName}"?`,
    message: 'This will permanently delete the account and all its balance history. '
      + 'This cannot be undone.',
    confirmText: 'Delete Account',
  }
}

const confirmDeleteAccount = async () => {
  const { accountId, accountName } = deleteAccountConfirm.value
  deleteAccountConfirm.value.show = false

  const result = await store.deleteAccount(accountId)

  if (result === 'has_transactions') {
    // Account has transactions — offer to delete all transactions first
    deleteAllTxConfirm.value = {
      show: true,
      accountId,
      accountName,
      message: `"${accountName}" has transactions and cannot be deleted directly. `
        + 'Would you like to delete all its transactions first? '
        + 'This cannot be undone.',
    }
  }
}

const confirmDeleteAllTransactions = async () => {
  const { accountId } = deleteAllTxConfirm.value
  deleteAllTxConfirm.value.show = false
  await store.deleteAllTransactions(accountId)
}

// Transaction delete flow — undo pattern
const requestDeleteTransaction = (transactionId: number) => {
  // Hide immediately from UI
  hiddenTransactionIds.value.add(transactionId)

  // Cancel any existing pending delete
  if (undoState.value.show) {
    // Fire the previous pending delete immediately
    finalizeTransactionDelete(undoState.value.transactionId)
  }

  const tx = recentTransactions.value.find(t => t.id === transactionId)
  const label = tx ? `${tx.symbol} ${tx.type}` : 'Transaction'

  undoState.value = {
    show: true,
    message: `${label} deleted`,
    transactionId,
  }
}

const handleUndoDelete = () => {
  // Restore the hidden transaction
  hiddenTransactionIds.value.delete(undoState.value.transactionId)
  undoState.value.show = false
}

const handleUndoExpire = () => {
  // Timer expired — actually delete
  finalizeTransactionDelete(undoState.value.transactionId)
  undoState.value.show = false
}

const finalizeTransactionDelete = async (transactionId: number) => {
  hiddenTransactionIds.value.delete(transactionId)
  await store.deleteTransaction(transactionId)
}

const handleCreateTransaction = async (data: {
  symbol: string
  type: 'BUY' | 'SELL'
  assetType: 'STOCK' | 'CRYPTO'
  quantity: number
  price: number
  notes?: string
}) => {
  await store.createTransaction(data)
  showAddTransaction.value = false
}

const handleAdjustCash = async (amount: number) => {
  await store.adjustCash(amount)
  showCashModal.value = false
}

const handleSeedDemo = async () => {
  await store.seedDemoData()
}

// Initialize
onMounted(() => {
  store.fetchAccounts()
})
</script>
