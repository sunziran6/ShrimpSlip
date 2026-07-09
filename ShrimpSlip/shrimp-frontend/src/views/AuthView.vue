<template>
  <div class="relative min-h-screen bg-hero-bg text-foreground overflow-hidden font-sora antialiased">
    <!-- Spline 3D Background -->
    <SplineBackground />

    <!-- Content layer (right-aligned form) -->
    <div class="relative z-10 flex items-center justify-end min-h-screen px-6 pointer-events-none">
      <div class="w-full max-w-[420px] mr-0 lg:mr-[8%] pointer-events-auto opacity-0 animate-fade-up" style="animation-delay: 0.3s">
        <AuthForm />
      </div>
    </div>

    <!-- ShrimpSlip branding (bottom-left) -->
    <div class="absolute bottom-0 left-0 z-10 pointer-events-none pb-12 md:pb-16 pl-8 md:pl-16 max-w-[90%] sm:max-w-md lg:max-w-xl">
      <!-- Logo + Name -->
      <div class="flex items-center gap-4 md:gap-5 mb-5 md:mb-8 opacity-0 animate-fade-up" style="animation-delay: 0.2s">
        <div class="w-14 h-14 md:w-18 md:h-18 rounded-2xl bg-primary flex items-center justify-center text-primary-foreground font-bold text-2xl md:text-3xl shadow-2xl shadow-primary/30 flex-shrink-0"
             style="width: clamp(3.5rem, 8vw, 5rem); height: clamp(3.5rem, 8vw, 5rem); font-size: clamp(1.5rem, 4vw, 2.5rem);">
          S
        </div>
        <span class="text-foreground font-bold tracking-tight leading-none"
              style="font-size: clamp(2rem, 5vw, 3.5rem);">
          Shrimp<span class="text-primary">Slip</span>
        </span>
      </div>

      <!-- Tagline -->
      <p class="text-muted-foreground font-light mb-5 md:mb-8 opacity-0 animate-fade-up"
         style="animation-delay: 0.4s; font-size: clamp(1rem, 2vw, 1.35rem); max-width: 480px;">
        AI 智能购物，一句话搞定所有需求
      </p>

      <!-- Feature tags -->
      <div class="flex flex-wrap gap-2 md:gap-3 opacity-0 animate-fade-up" style="animation-delay: 0.55s;">
        <span class="px-4 py-1.5 rounded-full border border-white/10 bg-white/5 text-muted-foreground text-xs md:text-sm backdrop-blur-sm">AI 智能导购</span>
        <span class="px-4 py-1.5 rounded-full border border-white/10 bg-white/5 text-muted-foreground text-xs md:text-sm backdrop-blur-sm">场景化推荐</span>
        <span class="px-4 py-1.5 rounded-full border border-white/10 bg-white/5 text-muted-foreground text-xs md:text-sm backdrop-blur-sm">一键下单</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import SplineBackground from '../components/auth/SplineBackground.vue'
import AuthForm from '../components/auth/AuthForm.vue'
</script>
