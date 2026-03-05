<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-black/40 backdrop-blur-xs flex items-center justify-center z-50"
    @click="emit('close')"
  >
    <div
      class="bg-white rounded-2xl p-6 max-w-md w-full mx-4 shadow-xl"
      @click.stop
    >
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Add Transaction</h2>
      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Symbol</label>
          <input
            v-model="form.symbol"
            type="text"
            required
            class="input-field"
            placeholder="AAPL, BTC"
          />
        </div>
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Type</label>
            <select v-model="form.type" required class="input-field">
              <option value="BUY">Buy</option>
              <option value="SELL">Sell</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Asset Type</label>
            <select v-model="form.assetType" required class="input-field">
              <option value="STOCK">Stock</option>
              <option value="CRYPTO">Crypto</option>
            </select>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
            <input
              v-model.number="form.quantity"
              type="number"
              step="0.00000001"
              required
              class="input-field"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Price</label>
            <input
              v-model.number="form.price"
              type="number"
              step="0.01"
              required
              class="input-field"
            />
          </div>
        </div>
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-1">
            Notes (optional)
          </label>
          <textarea v-model="form.notes" class="input-field" rows="2"></textarea>
        </div>
        <div class="flex gap-2">
          <button type="button" @click="emit('close')" class="btn-secondary flex-1">
            Cancel
          </button>
          <button type="submit" class="btn-primary flex-1">Add Transaction</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  close: []
  submit: [data: {
    symbol: string
    type: 'BUY' | 'SELL'
    assetType: 'STOCK' | 'CRYPTO'
    quantity: number
    price: number
    notes?: string
  }]
}>()

const defaultForm = () => ({
  symbol: '',
  type: 'BUY' as 'BUY' | 'SELL',
  assetType: 'STOCK' as 'STOCK' | 'CRYPTO',
  quantity: 0,
  price: 0,
  notes: '',
})

const form = ref(defaultForm())

const handleSubmit = () => {
  emit('submit', { ...form.value })
  form.value = defaultForm()
}

// Reset form when modal is closed
watch(() => form, () => {}, { deep: true })
</script>
