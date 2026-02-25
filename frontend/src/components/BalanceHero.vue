<template>
  <div class="mb-8">
    <!-- Main balance -->
    <div class="flex items-baseline gap-3 mb-6">
      <h2 class="text-4xl font-bold tracking-tight text-gray-900">
        ${{ formatCurrency(totalValue) }}
      </h2>
      <span class="text-sm font-medium text-gray-400">Total Value</span>
    </div>

    <!-- Breakdown cards -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl border border-gray-100 p-4">
        <div class="flex items-center gap-2 mb-1">
          <div class="w-2 h-2 rounded-full bg-primary-500"></div>
          <span class="text-xs font-medium text-gray-400 uppercase tracking-wider">
            Stocks
          </span>
        </div>
        <p class="text-lg font-semibold text-gray-900">
          ${{ formatCurrency(stocksValue) }}
        </p>
        <p class="text-xs text-gray-400 mt-0.5">
          {{ stocksCount }} holding{{ stocksCount !== 1 ? 's' : '' }}
        </p>
      </div>

      <div class="bg-white rounded-xl border border-gray-100 p-4">
        <div class="flex items-center gap-2 mb-1">
          <div class="w-2 h-2 rounded-full bg-amber-500"></div>
          <span class="text-xs font-medium text-gray-400 uppercase tracking-wider">
            Crypto
          </span>
        </div>
        <p class="text-lg font-semibold text-gray-900">
          ${{ formatCurrency(cryptoValue) }}
        </p>
        <p class="text-xs text-gray-400 mt-0.5">
          {{ cryptoCount }} holding{{ cryptoCount !== 1 ? 's' : '' }}
        </p>
      </div>

      <div class="bg-white rounded-xl border border-gray-100 p-4">
        <div class="flex items-center gap-2 mb-1">
          <div class="w-2 h-2 rounded-full bg-emerald-500"></div>
          <span class="text-xs font-medium text-gray-400 uppercase tracking-wider">
            Cash
          </span>
        </div>
        <p class="text-lg font-semibold text-gray-900">
          ${{ formatCurrency(cashBalance) }}
        </p>
        <p class="text-xs text-gray-400 mt-0.5">Available</p>
      </div>

      <div class="bg-white rounded-xl border border-gray-100 p-4">
        <div class="flex items-center gap-2 mb-1">
          <div class="w-2 h-2 rounded-full bg-gray-400"></div>
          <span class="text-xs font-medium text-gray-400 uppercase tracking-wider">
            Assets
          </span>
        </div>
        <p class="text-lg font-semibold text-gray-900">
          {{ stocksCount + cryptoCount }}
        </p>
        <p class="text-xs text-gray-400 mt-0.5">Total holdings</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  totalValue: number
  stocksValue: number
  cryptoValue: number
  cashBalance: number
  stocksCount: number
  cryptoCount: number
}>()

const formatCurrency = (value: number): string => {
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}
</script>
