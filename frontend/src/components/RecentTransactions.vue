<template>
  <div class="bg-white rounded-xl border border-gray-100 p-5">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-sm font-semibold text-gray-900 uppercase tracking-wider">
        Recent Transactions
      </h3>
      <RouterLink
        v-if="transactions.length > 0"
        to="/transactions"
        class="text-xs font-medium text-primary-600 hover:text-primary-700 transition-colors"
      >
        View all
      </RouterLink>
    </div>

    <div v-if="transactions.length === 0" class="text-center py-8">
      <p class="text-sm text-gray-400">No transactions yet</p>
    </div>

    <div v-else class="space-y-1">
      <div
        v-for="tx in transactions"
        :key="tx.id"
        class="group flex items-center justify-between py-2.5 border-b border-gray-50
               last:border-0"
      >
        <div class="flex items-center gap-3">
          <div
            class="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold"
            :class="tx.type === 'BUY'
              ? 'bg-emerald-50 text-emerald-600'
              : 'bg-red-50 text-red-600'"
          >
            {{ tx.type === 'BUY' ? '+' : '-' }}
          </div>
          <div>
            <p class="text-sm font-medium text-gray-900">{{ tx.symbol }}</p>
            <p class="text-xs text-gray-400">
              {{ tx.quantity }} @ ${{ formatPrice(tx.price) }}
            </p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <div class="text-right">
            <p
              class="text-sm font-medium"
              :class="tx.type === 'BUY' ? 'text-gray-900' : 'text-red-600'"
            >
              {{ tx.type === 'BUY' ? '-' : '+' }}${{ formatPrice(tx.totalAmount) }}
            </p>
            <p class="text-xs text-gray-400">{{ formatDate(tx.transactionDate) }}</p>
          </div>
          <button
            @click="emit('delete', tx.id)"
            class="opacity-0 group-hover:opacity-100 p-1 text-gray-300
                   hover:text-red-500 transition-all"
            title="Delete transaction"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5
                   4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Transaction } from '@/services/api'

defineProps<{
  transactions: Transaction[]
}>()

const emit = defineEmits<{
  delete: [transactionId: number]
}>()

const formatPrice = (value: number): string => {
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}
</script>
