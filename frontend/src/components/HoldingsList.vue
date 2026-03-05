<template>
  <div class="bg-white rounded-xl border border-gray-100 p-5">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-sm font-semibold text-gray-900 uppercase tracking-wider">
        {{ title }}
      </h3>
    </div>

    <div v-if="holdings.length === 0" class="text-center py-8">
      <p class="text-sm text-gray-400">{{ emptyMessage }}</p>
    </div>

    <div v-else class="space-y-1">
      <div
        v-for="holding in holdings"
        :key="holding.symbol"
        class="flex items-center justify-between py-2.5 border-b border-gray-50 last:border-0"
      >
        <div class="flex items-center gap-3">
          <div
            class="w-9 h-9 rounded-lg flex items-center justify-center"
            :class="accentClasses"
          >
            <span class="text-xs font-bold">
              {{ holding.symbol.slice(0, 3) }}
            </span>
          </div>
          <div>
            <p class="text-sm font-medium text-gray-900">{{ holding.symbol }}</p>
            <p class="text-xs text-gray-400">{{ formatQuantity(holding) }}</p>
          </div>
        </div>
        <div class="text-right">
          <p class="text-sm font-semibold text-gray-900">
            ${{ formatCurrency(holding.quantity * holding.averagePrice) }}
          </p>
          <p class="text-xs text-gray-400">
            Avg ${{ formatCurrency(holding.averagePrice) }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Holding } from '@/services/api'

const props = defineProps<{
  holdings: Holding[]
  title: string
  emptyMessage: string
  accent: 'blue' | 'amber'
}>()

const accentClasses = props.accent === 'blue'
  ? 'bg-primary-50 text-primary-600'
  : 'bg-amber-50 text-amber-600'

const formatCurrency = (value: number): string => {
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

const formatQuantity = (holding: Holding): string => {
  if (holding.assetType === 'STOCK') {
    return `${holding.quantity} shares`
  }
  return `${holding.quantity} ${holding.symbol}`
}
</script>
