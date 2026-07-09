<template>
  <div class="absolute inset-0 overflow-hidden">
    <canvas ref="canvasRef" class="w-full h-full"></canvas>
    <div class="absolute inset-0 bg-black/30 z-[1] pointer-events-none"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Application } from '@splinetool/runtime'

const canvasRef = ref(null)
let splineApp = null

onMounted(() => {
  if (!canvasRef.value) return
  try {
    splineApp = new Application(canvasRef.value)
    splineApp.load('https://prod.spline.design/Slk6b8kz3LRlKiyk/scene.splinecode')
  } catch {
    // Spline scene failed to load — page still works with dark background
  }
})

onUnmounted(() => {
  if (splineApp && typeof splineApp.dispose === 'function') {
    splineApp.dispose()
  }
  splineApp = null
})
</script>
