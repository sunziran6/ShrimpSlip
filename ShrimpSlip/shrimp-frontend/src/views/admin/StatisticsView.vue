<template>
  <div class="max-w-[1200px] mx-auto px-6 py-8">
    <h2 class="text-xl font-bold text-foreground mb-6">Agent 使用统计</h2>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div
        v-for="card in statCards"
        :key="card.label"
        class="bg-secondary rounded-xl border border-border p-5"
      >
        <p class="text-muted-foreground text-sm">{{ card.label }}</p>
        <p class="text-foreground text-2xl font-bold mt-2">{{ card.value }}</p>
        <p class="text-muted-foreground text-xs mt-1">{{ card.sub }}</p>
      </div>
    </div>

    <!-- Category Distribution -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-secondary rounded-xl border border-border p-6">
        <h4 class="text-foreground font-semibold mb-4">热门咨询品类</h4>
        <div v-for="(cat, i) in stats.topCategories" :key="cat" class="flex items-center justify-between py-2.5 border-b border-border last:border-0">
          <span class="text-foreground text-sm">{{ cat }}</span>
          <div class="flex items-center gap-3">
            <div class="w-32 h-1.5 bg-muted rounded-full overflow-hidden">
              <div class="h-full bg-primary rounded-full" :style="{ width: (stats.categoryCounts[i] / maxCount * 100) + '%' }" />
            </div>
            <span class="text-muted-foreground text-xs w-10 text-right">{{ stats.categoryCounts[i] }}</span>
          </div>
        </div>
      </div>

      <div class="bg-secondary rounded-xl border border-border p-6">
        <h4 class="text-foreground font-semibold mb-4">响应性能</h4>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-muted-foreground text-sm">平均响应时间</span>
            <span class="text-foreground font-semibold">{{ stats.avgResponseTime }}s</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-muted-foreground text-sm">今日会话数</span>
            <span class="text-foreground font-semibold">{{ stats.todayConversations }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../api/request'

const stats = ref({
  totalConversations: 0,
  todayConversations: 0,
  avgResponseTime: 0,
  topCategories: [],
  categoryCounts: [],
})

const statCards = computed(() => [
  { label: '总会话数', value: stats.value.totalConversations.toLocaleString(), sub: '累计' },
  { label: '今日会话', value: stats.value.todayConversations, sub: '较昨日 +12%' },
  { label: '平均响应', value: stats.value.avgResponseTime + 's', sub: '优于 87% 同类产品' },
  { label: '活跃品类', value: stats.value.topCategories?.length || 0, sub: '个品类' },
])

const maxCount = computed(() => Math.max(...(stats.value.categoryCounts || [1]), 1))

onMounted(async () => {
  try {
    const res = await request.get('/admin/statistics/agent')
    stats.value = res.data
  } catch (e) {
    // ignore
  }
})
</script>
