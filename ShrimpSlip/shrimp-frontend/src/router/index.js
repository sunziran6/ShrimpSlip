import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/auth' },
  {
    path: '/auth',
    name: 'Auth',
    component: () => import('../views/AuthView.vue'),
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  if (to.meta.requiresAuth && !token) {
    next('/auth')
  } else {
    next()
  }
})

export default router
