import { createRouter, createWebHistory } from 'vue-router'

const MainLayout = () => import('../layouts/MainLayout.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')

const routes = [
  {
    path: '/auth',
    name: 'Auth',
    component: () => import('../views/AuthView.vue'),
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: (to) => { const role = localStorage.getItem('role'); return role === 'ADMIN' ? '/admin' : '/home' } },
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/user/AgentView.vue'),
      },
      {
        path: 'shop',
        name: 'Shop',
        component: () => import('../views/user/ShopView.vue'),
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('../views/user/CartView.vue'),
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/user/OrdersView.vue'),
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/ProfileView.vue'),
      },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminStats',
        component: () => import('../views/admin/StatisticsView.vue'),
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/admin/ProductManageView.vue'),
      },
      {
        path: 'revenue',
        name: 'AdminRevenue',
        component: () => import('../views/admin/RevenueView.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    next('/auth')
    return
  }

  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    next('/home')
    return
  }

  // Admin users visiting user pages → redirect to admin
  if (role === 'ADMIN' && to.path === '/home') {
    next('/admin')
    return
  }

  next()
})

export default router
