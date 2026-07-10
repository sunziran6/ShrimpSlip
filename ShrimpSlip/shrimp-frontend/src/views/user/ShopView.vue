<template>
  <div class="max-w-[1200px] mx-auto px-6 py-8">
    <!-- Search & Category -->
    <div class="flex flex-col sm:flex-row items-start sm:items-center gap-4 mb-8">
      <el-input
        v-model="keyword"
        placeholder="搜索商品..."
        :prefix-icon="Search"
        class="max-w-[360px]"
        @input="searchProducts"
        size="large"
      />
      <div class="flex gap-2 flex-wrap">
        <button
          v-for="cat in categories"
          :key="cat"
          @click="activeCategory = cat; searchProducts()"
          class="px-4 py-1.5 rounded-full text-sm border transition-all"
          :class="activeCategory === cat
            ? 'bg-primary/15 text-primary border-primary/30'
            : 'bg-secondary text-muted-foreground border-border hover:text-foreground'"
        >
          {{ cat }}
        </button>
      </div>
    </div>

    <!-- Product Grid -->
    <div v-if="products.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
      <div
        v-for="p in products"
        :key="p.id"
        class="bg-secondary rounded-xl border border-border overflow-hidden hover:border-primary/30 transition-all group cursor-pointer"
      >
        <div class="aspect-[4/3] bg-muted flex items-center justify-center text-muted-foreground text-4xl group-hover:bg-primary/5 transition-colors overflow-hidden">
          <img v-if="p.image" :src="p.image" class="w-full h-full object-cover" />
          <el-icon v-else :size="48"><Goods /></el-icon>
        </div>
        <div class="p-4">
          <h4 class="text-foreground font-semibold text-sm truncate">{{ p.name }}</h4>
          <p class="text-muted-foreground text-xs mt-1 line-clamp-2 min-h-[32px]">{{ p.description || '暂无描述' }}</p>
          <div class="flex items-center justify-between mt-3">
            <span class="text-primary font-bold text-lg">¥{{ p.price }}</span>
            <button class="px-3 py-1.5 rounded-lg bg-primary/15 text-primary text-xs font-medium hover:bg-primary hover:text-primary-foreground transition-all">
              加入购物车
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="flex flex-col items-center justify-center py-24 text-center">
      <el-icon :size="48" class="text-muted-foreground mb-4"><Goods /></el-icon>
      <p class="text-muted-foreground">暂无商品</p>
    </div>

    <!-- Pagination -->
    <div v-if="total > 0" class="flex justify-center mt-8">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="searchProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Goods } from '@element-plus/icons-vue'
import request from '../../api/request'

const keyword = ref('')
const activeCategory = ref('全部')
const categories = ['全部', '电子产品', '服装鞋帽', '食品饮料', '家居用品']
const products = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)

onMounted(() => searchProducts())

async function searchProducts() {
  try {
    const params = { page: page.value, size: size.value, keyword: keyword.value }
    if (activeCategory.value !== '全部') params.category = activeCategory.value
    const res = await request.get('/products', { params })
    products.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // ignore
  }
}
</script>
