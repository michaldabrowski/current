<template>
  <div class="relative" ref="dropdownRef">
    <button
      @click="open = !open"
      class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium
             text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-colors"
    >
      <span>{{ currentAccount?.name ?? 'Select Account' }}</span>
      <svg
        class="w-4 h-4 transition-transform"
        :class="{ 'rotate-180': open }"
        fill="none" stroke="currentColor" viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <Transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="opacity-0 scale-95"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
      <div
        v-if="open"
        class="absolute right-0 mt-1 w-64 bg-white rounded-xl shadow-lg border border-gray-100
               py-1 z-50 origin-top-right"
      >
        <div
          v-for="account in accounts"
          :key="account.id"
          class="flex items-center justify-between px-4 py-2 hover:bg-gray-50 transition-colors"
          :class="account.id === currentAccount?.id
            ? 'bg-primary-50/50'
            : ''"
        >
          <button
            @click="selectAccount(account.id)"
            class="flex-1 text-left text-sm"
            :class="account.id === currentAccount?.id
              ? 'text-primary-600 font-medium'
              : 'text-gray-700'"
          >
            {{ account.name }}
          </button>
          <button
            @click.stop="requestDelete(account.id, account.name)"
            class="p-1 text-gray-300 hover:text-red-500 transition-colors"
            title="Delete account"
          >
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="border-t border-gray-100 mt-1 pt-1">
          <button
            @click="showCreateModal = true; open = false"
            class="w-full text-left px-4 py-2 text-sm text-gray-500
                   hover:bg-gray-50 hover:text-gray-700 transition-colors
                   flex items-center gap-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 4v16m8-8H4" />
            </svg>
            New Account
          </button>
        </div>
      </div>
    </Transition>

    <!-- Create Account Modal -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/40 backdrop-blur-xs flex items-center justify-center z-50"
      @click="showCreateModal = false"
    >
      <div
        class="bg-white rounded-2xl p-6 max-w-md w-full mx-4 shadow-xl"
        @click.stop
      >
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Create Account</h2>
        <form @submit.prevent="handleCreate">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Account Name
            </label>
            <input
              v-model="newName"
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
              v-model.number="newBalance"
              type="number"
              step="0.01"
              class="input-field"
              placeholder="0.00"
            />
          </div>
          <div class="flex gap-2">
            <button
              type="button"
              @click="showCreateModal = false"
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
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { Account } from '@/services/api'

defineProps<{
  accounts: Account[]
  currentAccount: Account | null
}>()

const emit = defineEmits<{
  select: [accountId: number]
  create: [name: string, initialBalance?: number]
  delete: [accountId: number, accountName: string]
}>()

const open = ref(false)
const showCreateModal = ref(false)
const newName = ref('')
const newBalance = ref<number | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)

const selectAccount = (accountId: number) => {
  emit('select', accountId)
  open.value = false
}

const handleCreate = () => {
  if (!newName.value.trim()) return
  emit('create', newName.value.trim(), newBalance.value || undefined)
  newName.value = ''
  newBalance.value = null
  showCreateModal.value = false
}

const requestDelete = (accountId: number, accountName: string) => {
  emit('delete', accountId, accountName)
  open.value = false
}

const handleClickOutside = (event: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    open.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>
