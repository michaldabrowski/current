<script setup lang="ts">
import { RouterLink, RouterView } from "vue-router";
import { useCurrentStore } from "@/stores/current";

const store = useCurrentStore();

const handleSeedDemo = async () => {
  await store.seedDemoData();
};
</script>

<template>
  <div id="app" class="min-h-screen bg-gray-50 flex flex-col">
    <!-- Navigation -->
    <nav class="bg-white border-b border-gray-100">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-14">
          <!-- Logo/Brand -->
          <RouterLink to="/" class="flex items-center gap-2.5">
            <img src="@/assets/logo.svg" alt="Current" class="w-7 h-7" />
            <span class="text-base font-bold text-gray-900">Current</span>
          </RouterLink>

          <!-- Navigation Links -->
          <div class="flex items-center gap-1">
            <RouterLink
              to="/"
              class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors"
              active-class="router-link-active"
              :class="$route.name === 'dashboard'
                ? ''
                : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50'"
              :exact="true"
            >
              Dashboard
            </RouterLink>
            <RouterLink
              to="/transactions"
              class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors"
              active-class="router-link-active"
              :class="$route.name === 'transactions'
                ? ''
                : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50'"
            >
              Transactions
            </RouterLink>
            <button
              @click="handleSeedDemo"
              class="ml-2 px-3 py-1.5 rounded-lg text-sm font-medium text-gray-400
                     hover:text-gray-600 hover:bg-gray-50 transition-colors"
              title="Load demo portfolio data"
            >
              Demo
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-6xl mx-auto w-full py-6 px-4 sm:px-6 lg:px-8 flex-1">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer class="border-t border-gray-100 mt-auto">
      <div class="max-w-6xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
        <p class="text-center text-xs text-gray-300">Current</p>
      </div>
    </footer>
  </div>
</template>
