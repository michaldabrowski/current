<script setup lang="ts">
import {RouterLink, RouterView} from "vue-router";
</script>

<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <!-- Navigation -->
    <nav class="bg-white shadow-xs border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo/Brand -->
          <div class="flex items-center">
            <img src="@/assets/logo.svg" alt="Current" class="w-8 h-8 mr-3"/>
            <h1 class="text-xl font-bold text-gray-900">Current</h1>
          </div>

          <!-- Navigation Links -->
          <div class="flex space-x-8">
            <RouterLink
              to="/"
              class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 router-link-inactive"
              active-class="router-link-active"
            >
              Dashboard
            </RouterLink>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <RouterView/>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200 mt-12">
      <div class="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
        <p class="text-center text-sm text-gray-500">© 2025 Current</p>
      </div>
    </footer>
  </div>
</template>
