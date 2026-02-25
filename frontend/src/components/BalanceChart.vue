<template>
  <div class="bg-white rounded-xl border border-gray-100 p-5 mb-8">
    <div v-if="snapshots.length < 2" class="flex items-center justify-center h-48 text-gray-400">
      <div class="text-center">
        <svg class="w-8 h-8 mx-auto mb-2 text-gray-300" fill="none" stroke="currentColor"
          viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" />
        </svg>
        <p class="text-sm">Balance history will appear as you use the app</p>
      </div>
    </div>
    <Line v-else :data="chartData" :options="chartOptions" class="h-48" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
} from 'chart.js'
import type { BalanceSnapshot } from '@/services/api'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip)

const props = defineProps<{
  snapshots: BalanceSnapshot[]
}>()

const chartData = computed(() => {
  const labels = props.snapshots.map((s) => {
    const date = new Date(s.snapshotDate)
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  })

  const data = props.snapshots.map((s) => s.totalValue)

  return {
    labels,
    datasets: [
      {
        data,
        borderColor: '#2563eb',
        backgroundColor: 'rgba(37, 99, 235, 0.08)',
        borderWidth: 2,
        fill: true,
        tension: 0.35,
        pointRadius: 0,
        pointHoverRadius: 5,
        pointHoverBackgroundColor: '#2563eb',
        pointHoverBorderColor: '#fff',
        pointHoverBorderWidth: 2,
      },
    ],
  }
})

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    intersect: false,
    mode: 'index' as const,
  },
  plugins: {
    tooltip: {
      backgroundColor: '#1f2937',
      titleFont: { size: 12, weight: 'normal' as const },
      bodyFont: { size: 13, weight: 'bold' as const },
      padding: 10,
      cornerRadius: 8,
      displayColors: false,
      callbacks: {
        label: (context: { parsed: { y: number | null } }) => {
          const val = context.parsed.y ?? 0
          return `$${val.toLocaleString('en-US', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
          })}`
        },
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: {
        color: '#9ca3af',
        font: { size: 11 },
        maxTicksLimit: 8,
      },
    },
    y: {
      grid: { color: 'rgba(0, 0, 0, 0.04)' },
      border: { display: false },
      ticks: {
        color: '#9ca3af',
        font: { size: 11 },
        callback: (value: string | number) => {
          const num = typeof value === 'string' ? parseFloat(value) : value
          return `$${num.toLocaleString('en-US', { maximumFractionDigits: 0 })}`
        },
      },
    },
  },
}))
</script>
