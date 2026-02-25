<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-black/40 backdrop-blur-xs flex items-center justify-center z-50"
    @click="emit('close')"
  >
    <div
      class="bg-white rounded-2xl p-6 max-w-sm w-full mx-4 shadow-xl"
      @click.stop
    >
      <h2 class="text-lg font-semibold text-gray-900 mb-1">Manage Cash</h2>
      <p class="text-sm text-gray-400 mb-4">
        Current balance: ${{ formatCurrency(currentBalance) }}
      </p>

      <form @submit.prevent="handleSubmit">
        <!-- Type toggle -->
        <div class="flex gap-2 mb-4">
          <button
            type="button"
            @click="type = 'deposit'"
            class="flex-1 py-2 px-3 rounded-lg text-sm font-medium transition-colors"
            :class="type === 'deposit'
              ? 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200'
              : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
          >
            Deposit
          </button>
          <button
            type="button"
            @click="type = 'withdraw'"
            class="flex-1 py-2 px-3 rounded-lg text-sm font-medium transition-colors"
            :class="type === 'withdraw'
              ? 'bg-red-50 text-red-700 ring-1 ring-red-200'
              : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
          >
            Withdraw
          </button>
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-1">Amount</label>
          <input
            v-model.number="amount"
            type="number"
            step="0.01"
            min="0.01"
            required
            class="input-field"
            placeholder="0.00"
          />
        </div>

        <div class="flex gap-2">
          <button type="button" @click="emit('close')" class="btn-secondary flex-1">
            Cancel
          </button>
          <button type="submit" class="btn-primary flex-1">
            {{ type === 'deposit' ? 'Deposit' : 'Withdraw' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  show: boolean
  currentBalance: number
}>()

const emit = defineEmits<{
  close: []
  submit: [amount: number]
}>()

const type = ref<'deposit' | 'withdraw'>('deposit')
const amount = ref<number | null>(null)

const formatCurrency = (value: number): string => {
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

const handleSubmit = () => {
  if (!amount.value || amount.value <= 0) return
  const adjustedAmount = type.value === 'deposit' ? amount.value : -amount.value
  emit('submit', adjustedAmount)
  amount.value = null
  type.value = 'deposit'
}

watch(() => props.show, (newVal) => {
  if (!newVal) {
    amount.value = null
    type.value = 'deposit'
  }
})
</script>
