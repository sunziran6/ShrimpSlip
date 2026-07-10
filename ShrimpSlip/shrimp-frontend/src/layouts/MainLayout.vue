<template>
  <div class="min-h-screen bg-background text-foreground font-sans">
    <!-- Fixed Top Navbar -->
    <header class="fixed top-0 left-0 right-0 z-50 h-16 bg-secondary border-b border-border">
      <div class="h-full max-w-[1400px] mx-auto px-6 flex items-center justify-between">
        <!-- Left: Logo + Nav -->
        <div class="flex items-center gap-1">
          <!-- Logo (not clickable) -->
          <div class="flex items-center gap-2.5 mr-4">
            <div class="w-9 h-9 rounded-xl bg-primary flex items-center justify-center text-primary-foreground font-bold text-lg shadow-lg shadow-primary/20 flex-shrink-0">
              S
            </div>
            <span class="text-foreground font-bold text-lg tracking-tight hidden sm:inline">
              Shrimp<span class="text-primary">Slip</span>
            </span>
          </div>

          <!-- Nav Items -->
          <nav class="flex items-center gap-0.5">
            <button
              v-for="item in navItems"
              :key="item.path"
              @click="$router.push(item.path)"
              class="flex items-center gap-2 px-3.5 py-2 rounded-lg text-sm font-medium transition-all duration-200"
              :class="$route.path === item.path
                ? 'bg-primary/15 text-primary'
                : 'text-muted-foreground hover:text-foreground hover:bg-white/5'"
            >
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
              <span class="hidden md:inline">{{ item.label }}</span>
            </button>
          </nav>
        </div>

        <!-- Right: Logout -->
        <button
          @click="handleLogout"
          class="flex items-center gap-2 px-3.5 py-2 rounded-lg text-sm font-medium text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-all duration-200"
        >
          <el-icon :size="18"><SwitchButton /></el-icon>
          <span class="hidden md:inline">退出登录</span>
        </button>
      </div>
    </header>

    <!-- Content Area -->
    <main class="pt-16 min-h-screen">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ChatDotRound, Shop, ShoppingCart, Tickets, User, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()

const navItems = [
  { path: '/home', label: 'Agent', icon: ChatDotRound },
  { path: '/shop', label: '商城', icon: Shop },
  { path: '/cart', label: '购物车', icon: ShoppingCart },
  { path: '/orders', label: '消费记录', icon: Tickets },
  { path: '/profile', label: '个人中心', icon: User },
]

function handleLogout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('role')
  router.push('/auth')
}
</script>
