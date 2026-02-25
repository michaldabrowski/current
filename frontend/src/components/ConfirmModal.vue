<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-100"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="show"
        class="fixed inset-0 bg-black/40 backdrop-blur-xs flex items-center
               justify-center z-50"
        @click="emit('cancel')"
      >
        <div
          class="bg-white rounded-2xl p-6 max-w-sm w-full mx-4 shadow-xl"
          @click.stop
        >
          <h2 class="text-lg font-semibold text-gray-900 mb-2">{{ title }}</h2>
          <p class="text-sm text-gray-500 mb-6">{{ message }}</p>
          <div class="flex gap-2">
            <button
              @click="emit('cancel')"
              class="btn-secondary flex-1"
            >
              Cancel
            </button>
            <button
              @click="emit('confirm')"
              class="flex-1 font-medium py-2 px-4 rounded-lg transition-all
                     duration-150 focus:outline-none focus:ring-2
                     focus:ring-offset-2 active:scale-[0.98]"
              :class="destructive
                ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500 text-white'
                : 'bg-primary-600 hover:bg-primary-700 focus:ring-primary-500 text-white'"
            >
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{
  show: boolean
  title: string
  message: string
  confirmText?: string
  destructive?: boolean
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>
