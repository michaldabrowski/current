<template>
  <div>
    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="text-sm text-gray-400">Loading transactions...</div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-xl p-4 mb-6">
      <div class="text-red-800 text-sm">{{ error }}</div>
    </div>

    <!-- No Account State -->
    <div v-else-if="!currentAccount" class="text-center py-16">
      <p class="text-gray-400 text-sm">
        Create an account first from the
        <RouterLink to="/" class="text-primary-600 hover:text-primary-700">Dashboard</RouterLink>.
      </p>
    </div>

    <!-- Transactions Content -->
    <div v-else>
      <header class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-lg font-semibold text-gray-900">Transactions</h1>
          <p class="text-sm text-gray-400">{{ currentAccount.name }}</p>
        </div>
        <RouterLink
          to="/"
          class="text-sm text-gray-500 hover:text-gray-700 transition-colors"
        >
          Back to Dashboard
        </RouterLink>
      </header>

      <div v-if="visibleTransactions.length === 0" class="text-center py-16">
        <p class="text-sm text-gray-400">No transactions recorded yet.</p>
      </div>

      <div v-else class="bg-white rounded-xl border border-gray-100 overflow-hidden">
        <!-- Table header -->
        <div
          class="grid grid-cols-13 gap-4 px-5 py-3 bg-gray-50 border-b border-gray-100
                 text-xs font-medium text-gray-400 uppercase tracking-wider"
        >
          <div class="col-span-2">Date</div>
          <div class="col-span-2">Symbol</div>
          <div class="col-span-1">Type</div>
          <div class="col-span-1">Asset</div>
          <div class="col-span-2 text-right">Quantity</div>
          <div class="col-span-2 text-right">Price</div>
          <div class="col-span-2 text-right">Total</div>
          <div class="col-span-1"></div>
        </div>

        <!-- Table rows -->
        <div
          v-for="tx in visibleTransactions"
          :key="tx.id"
          class="group grid grid-cols-13 gap-4 px-5 py-3 border-b border-gray-50
                 last:border-0 hover:bg-gray-50/50 transition-colors items-center"
        >
          <div class="col-span-2 text-sm text-gray-500">
            {{ formatDate(tx.transactionDate) }}
          </div>
          <div class="col-span-2">
            <span class="text-sm font-medium text-gray-900">{{ tx.symbol }}</span>
          </div>
          <div class="col-span-1">
            <span
              class="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium"
              :class="tx.type === 'BUY'
                ? 'bg-emerald-50 text-emerald-700'
                : 'bg-red-50 text-red-700'"
            >
              {{ tx.type }}
            </span>
          </div>
          <div class="col-span-1">
            <span class="text-xs text-gray-400">{{ tx.assetType }}</span>
          </div>
          <div class="col-span-2 text-right text-sm text-gray-700">
            {{ formatQuantity(tx.quantity) }}
          </div>
          <div class="col-span-2 text-right text-sm text-gray-700">
            ${{ formatCurrency(tx.price) }}
          </div>
          <div class="col-span-2 text-right text-sm font-medium text-gray-900">
            ${{ formatCurrency(tx.totalAmount) }}
          </div>
          <div class="col-span-1 flex justify-end">
            <button
              @click="requestDeleteTransaction(tx.id)"
              class="opacity-0 group-hover:opacity-100 p-1.5 text-gray-300
                     hover:text-red-500 transition-all rounded-lg hover:bg-red-50"
              title="Delete transaction"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0
                     01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0
                     00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Undo Toast -->
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
import { RouterLink } from 'vue-router'
import { useCurrentStore } from '@/stores/current'
import { storeToRefs } from 'pinia'
import UndoToast from '@/components/UndoToast.vue'

const store = useCurrentStore()
const { currentAccount, transactions, loading, error } = storeToRefs(store)

// Undo-delete state
const undoState = ref({
  show: false,
  message: '',
  transactionId: 0,
})
const hiddenTransactionIds = ref<Set<number>>(new Set())

const visibleTransactions = computed(() =>
  transactions.value.filter(tx => !hiddenTransactionIds.value.has(tx.id)),
)

const formatCurrency = (value: number): string => {
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

const formatQuantity = (value: number): string => {
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 8,
  })
}

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

const requestDeleteTransaction = (transactionId: number) => {
  hiddenTransactionIds.value.add(transactionId)

  // If there's a pending delete, finalize it immediately
  if (undoState.value.show) {
    finalizeTransactionDelete(undoState.value.transactionId)
  }

  const tx = transactions.value.find(t => t.id === transactionId)
  const label = tx ? `${tx.symbol} ${tx.type}` : 'Transaction'

  undoState.value = {
    show: true,
    message: `${label} deleted`,
    transactionId,
  }
}

const handleUndoDelete = () => {
  hiddenTransactionIds.value.delete(undoState.value.transactionId)
  undoState.value.show = false
}

const handleUndoExpire = () => {
  finalizeTransactionDelete(undoState.value.transactionId)
  undoState.value.show = false
}

const finalizeTransactionDelete = async (transactionId: number) => {
  hiddenTransactionIds.value.delete(transactionId)
  await store.deleteTransaction(transactionId)
}

onMounted(() => {
  if (!currentAccount.value) {
    store.fetchAccounts()
  }
})
</script>
