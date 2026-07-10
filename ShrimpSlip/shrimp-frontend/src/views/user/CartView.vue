<template>
  <div class="max-w-[900px] mx-auto px-6 py-8">
    <h2 class="text-xl font-bold text-foreground mb-6">购物车</h2>
    <div class="flex flex-col items-center justify-center py-24 text-center">
      <div class="w-16 h-16 rounded-2xl bg-primary/10 flex items-center justify-center mb-6">
        <el-icon :size="32" class="text-primary"><ShoppingCart /></el-icon>
      </div>
      <h3 class="text-lg font-semibold text-foreground mb-2">购物车是空的</h3>
      <p class="text-muted-foreground text-sm max-w-xs">快去商城逛逛，把心仪的商品加入购物车吧</p>
      <button
        @click="$router.push('/shop')"
        class="mt-6 px-6 py-2.5 rounded-xl bg-primary text-primary-foreground font-medium hover:bg-primary/85 transition-all"
      >
        去逛逛
      </button>
    </div>
  </div>
</template>

<script setup>
import { ShoppingCart } from '@element-plus/icons-vue'
</script>
