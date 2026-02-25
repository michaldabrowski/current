<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="opacity-0 translate-y-4"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-4"
    >
      <div
        v-if="visible"
        class="fixed bottom-6 left-1/2 -translate-x-1/2 z-50"
      >
        <div
          class="flex items-center gap-3 bg-gray-900 text-white rounded-xl
                 px-4 py-3 shadow-lg text-sm"
        >
          <span>{{ message }}</span>
          <button
            @click="handleUndo"
            class="font-semibold text-primary-300 hover:text-primary-200
                   transition-colors whitespace-nowrap"
          >
            Undo ({{ countdown }}s)
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue'

const props = defineProps<{
  visible: boolean
  message: string
  duration?: number
}>()

const emit = defineEmits<{
  undo: []
  expire: []
}>()

const durationSec = Math.ceil((props.duration ?? 5000) / 1000)
const countdown = ref(durationSec)
let intervalId: ReturnType<typeof setInterval> | null = null
let timeoutId: ReturnType<typeof setTimeout> | null = null

const clearTimers = () => {
  if (intervalId) { clearInterval(intervalId); intervalId = null }
  if (timeoutId) { clearTimeout(timeoutId); timeoutId = null }
}

const handleUndo = () => {
  clearTimers()
  emit('undo')
}

watch(() => props.visible, (val) => {
  clearTimers()
  if (val) {
    countdown.value = durationSec
    intervalId = setInterval(() => {
      if (countdown.value > 0) countdown.value--
    }, 1000)
    timeoutId = setTimeout(() => {
      clearTimers()
      emit('expire')
    }, props.duration ?? 5000)
  }
})

onUnmounted(clearTimers)
</script>
