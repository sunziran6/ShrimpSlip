<template>
  <div class="max-w-[1200px] mx-auto px-6 py-8">
    <h2 class="text-xl font-bold text-foreground mb-6">收益统计</h2>

    <!-- Revenue Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div
        v-for="card in revenueCards"
        :key="card.label"
        class="bg-secondary rounded-xl border border-border p-5"
      >
        <p class="text-muted-foreground text-sm">{{ card.label }}</p>
        <p class="text-foreground text-2xl font-bold mt-2">{{ card.value }}</p>
        <p class="text-muted-foreground text-xs mt-1">{{ card.sub }}</p>
      </div>
    </div>

    <!-- Trend Chart Placeholder -->
    <div class="bg-secondary rounded-xl border border-border p-6">
      <h4 class="text-foreground font-semibold mb-4">近 7 天收益趋势</h4>
      <div class="flex items-end gap-3 h-48 px-2">
        <div
          v-for="d in stats.trend"
          :key="d.date"
          class="flex-1 flex flex-col items-center gap-2"
        >
          <div class="w-full bg-primary/20 rounded-t-lg transition-all hover:bg-primary/40" :style="{ height: (d.amount / maxAmount * 100) + '%', minHeight: '8px' }" />
          <span class="text-muted-foreground text-xs">{{ d.date }}</span>
          <span class="text-foreground text-xs font-medium">¥{{ d.amount.toLocaleString() }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../api/request'

const stats = ref({
  todayRevenue: 0,
  weekRevenue: 0,
  monthRevenue: 0,
  totalRevenue: 0,
  trend: [],
})

const revenueCards = computed(() => [
  { label: '今日收益', value: '¥' + stats.value.todayRevenue.toLocaleString(), sub: '实时更新' },
  { label: '本周收益', value: '¥' + stats.value.weekRevenue.toLocaleString(), sub: '周一至今' },
  { label: '本月收益', value: '¥' + stats.value.monthRevenue.toLocaleString(), sub: '1号至今' },
  { label: '累计收益', value: '¥' + stats.value.totalRevenue.toLocaleString(), sub: '全部' },
])

const maxAmount = computed(() => Math.max(...(stats.value.trend?.map(d => d.amount) || [1]), 1))

onMounted(async () => {
  try {
    const res = await request.get('/admin/statistics/revenue')
    stats.value = res.data
  } catch (e) {
    // ignore
  }
})
</script>
