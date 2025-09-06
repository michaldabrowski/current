<template>
  <div class="dashboard">
    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="text-lg text-gray-600">Loading your account data...</div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
      <div class="text-red-800">{{ error }}</div>
      <button @click="refresh" class="mt-2 btn-secondary text-sm">Try Again</button>
    </div>

    <!-- No Account State -->
    <div v-else-if="!currentAccount" class="text-center py-12">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Welcome to Current!</h2>
      <p class="text-gray-600 mb-6">Create your first account to start tracking your investments.</p>
      <button @click="showCreateAccount = true" class="btn-primary">Create Account</button>
    </div>

    <!-- Dashboard Content -->
    <div v-else>
      <!-- Header -->
      <header class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 mb-2">Dashboard</h1>
            <p class="text-gray-600">{{ currentAccount.name }}</p>
          </div>
          <div v-if="accounts.length > 1" class="flex items-center space-x-4">
            <select
              :value="currentAccount.id"
              @change="handleAccountChange"
              class="input-field"
            >
              <option v-for="account in accounts" :key="account.id" :value="account.id">
                {{ account.name }}
              </option>
            </select>
          </div>
        </div>
      </header>

      <!-- Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="card">
          <h3 class="text-sm font-medium text-gray-500 mb-2">Total Account Value</h3>
          <p class="text-2xl font-bold text-gray-900">${{ formatCurrency(totalValue) }}</p>
        </div>

        <div class="card">
          <h3 class="text-sm font-medium text-gray-500 mb-2">Stocks</h3>
          <p class="text-2xl font-bold text-gray-900">${{ formatCurrency(stocksValue) }}</p>
          <p class="text-sm text-gray-500 mt-1">{{ stocksCount }} holdings</p>
        </div>

        <div class="card">
          <h3 class="text-sm font-medium text-gray-500 mb-2">Crypto</h3>
          <p class="text-2xl font-bold text-gray-900">${{ formatCurrency(cryptoValue) }}</p>
          <p class="text-sm text-gray-500 mt-1">{{ cryptoCount }} holdings</p>
        </div>

        <div class="card">
          <h3 class="text-sm font-medium text-gray-500 mb-2">Cash</h3>
          <p class="text-2xl font-bold text-gray-900">${{ formatCurrency(currentAccount.cashBalance) }}</p>
          <p class="text-sm text-gray-500 mt-1">Available balance</p>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex gap-4 mb-8">
        <button @click="showAddTransaction = true" class="btn-primary">
          Add Transaction
        </button>
        <button @click="showCreateAccount = true" class="btn-secondary">
          Create Account
        </button>
        <button @click="refresh" class="btn-secondary">
          Refresh
        </button>
      </div>

      <!-- Recent Holdings -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Stocks -->
        <div class="card">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900">Stocks</h3>
          </div>

          <div v-if="stockHoldings.length === 0" class="text-gray-500 text-center py-8">
            No stock investments yet
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="stock in stockHoldings"
              :key="stock.symbol"
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <div class="flex items-center">
                <div class="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center mr-3">
                  <span class="text-primary-600 font-medium text-sm">{{ stock.symbol.slice(0, 2) }}</span>
                </div>
                <div>
                  <p class="font-medium text-gray-900">{{ stock.symbol }}</p>
                  <p class="text-sm text-gray-500">{{ stock.quantity }} shares</p>
                </div>
              </div>
              <div class="text-right">
                <p class="font-medium text-gray-900">${{ formatCurrency(stock.quantity * stock.averagePrice) }}</p>
                <p class="text-sm text-gray-500">Avg: ${{ formatCurrency(stock.averagePrice) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Crypto -->
        <div class="card">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900">Cryptocurrency</h3>
          </div>

          <div v-if="cryptoHoldings.length === 0" class="text-gray-500 text-center py-8">
            No crypto investments yet
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="crypto in cryptoHoldings"
              :key="crypto.symbol"
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <div class="flex items-center">
                <div class="w-10 h-10 bg-yellow-100 rounded-full flex items-center justify-center mr-3">
                  <span class="text-yellow-600 font-medium text-sm">{{ crypto.symbol.slice(0, 2) }}</span>
                </div>
                <div>
                  <p class="font-medium text-gray-900">{{ crypto.symbol }}</p>
                  <p class="text-sm text-gray-500">{{ crypto.quantity }} {{ crypto.symbol }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="font-medium text-gray-900">${{ formatCurrency(crypto.quantity * crypto.averagePrice) }}</p>
                <p class="text-sm text-gray-500">Avg: ${{ formatCurrency(crypto.averagePrice) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Account Modal -->
    <div
      v-if="showCreateAccount"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click="showCreateAccount = false"
    >
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4" @click.stop>
        <h2 class="text-xl font-semibold mb-4">Create Account</h2>
        <form @submit.prevent="handleCreateAccount">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">Account Name</label>
            <input v-model="newAccountName" type="text" required class="input-field" placeholder="My Investment Account">
          </div>
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-1">Initial Cash Balance (optional)</label>
            <input v-model.number="newAccountBalance" type="number" step="0.01" class="input-field" placeholder="0.00">
          </div>
          <div class="flex gap-2">
            <button type="button" @click="showCreateAccount = false" class="btn-secondary flex-1">Cancel</button>
            <button type="submit" class="btn-primary flex-1">Create</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Add Transaction Modal -->
    <div
      v-if="showAddTransaction"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click="showAddTransaction = false"
    >
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4" @click.stop>
        <h2 class="text-xl font-semibold mb-4">Add Transaction</h2>
        <form @submit.prevent="handleCreateTransaction">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">Symbol</label>
            <input v-model="newTransaction.symbol" type="text" required class="input-field" placeholder="AAPL, BTC">
          </div>
          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Type</label>
              <select v-model="newTransaction.type" required class="input-field">
                <option value="BUY">Buy</option>
                <option value="SELL">Sell</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Asset Type</label>
              <select v-model="newTransaction.assetType" required class="input-field">
                <option value="STOCK">Stock</option>
                <option value="CRYPTO">Crypto</option>
              </select>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
              <input v-model.number="newTransaction.quantity" type="number" step="0.00000001" required class="input-field">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Price</label>
              <input v-model.number="newTransaction.price" type="number" step="0.01" required class="input-field">
            </div>
          </div>
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-1">Notes (optional)</label>
            <textarea v-model="newTransaction.notes" class="input-field" rows="2"></textarea>
          </div>
          <div class="flex gap-2">
            <button type="button" @click="showAddTransaction = false" class="btn-secondary flex-1">Cancel</button>
            <button type="submit" class="btn-primary flex-1">Add Transaction</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCurrentStore } from '@/stores/current'
import { storeToRefs } from 'pinia'

const store = useCurrentStore()

// Use storeToRefs to ensure reactivity in the template
const {
  accounts,
  currentAccount,
  holdings,
  loading,
  error,
  totalValue,
  stocksValue,
  cryptoValue,
  stocksCount,
  cryptoCount
} = storeToRefs(store)

// Local state
const showCreateAccount = ref(false)
const showAddTransaction = ref(false)
const newAccountName = ref('')
const newAccountBalance = ref<number | null>(null)
const newTransaction = ref({
  symbol: '',
  type: 'BUY' as 'BUY' | 'SELL',
  assetType: 'STOCK' as 'STOCK' | 'CRYPTO',
  quantity: 0,
  price: 0,
  notes: ''
})

// Computed
const stockHoldings = computed(() => holdings.value.filter(h => h.assetType === 'STOCK'))
const cryptoHoldings = computed(() => holdings.value.filter(h => h.assetType === 'CRYPTO'))

// Methods
const formatCurrency = (value: number): string => {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const refresh = async () => {
  await store.fetchAccounts()
}

const handleAccountChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  const accountId = parseInt(target.value)
  store.selectAccount(accountId)
}

const handleCreateAccount = async () => {
  if (!newAccountName.value.trim()) return

  await store.createAccount(newAccountName.value.trim(), newAccountBalance.value || undefined)

  // Reset form
  newAccountName.value = ''
  newAccountBalance.value = null
  showCreateAccount.value = false
}

const handleCreateTransaction = async () => {
  await store.createTransaction(newTransaction.value)

  // Reset form
  newTransaction.value = {
    symbol: '',
    type: 'BUY',
    assetType: 'STOCK',
    quantity: 0,
    price: 0,
    notes: ''
  }
  showAddTransaction.value = false
}

// Initialize
onMounted(() => {
  store.fetchAccounts()
})
</script>
