import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/pages/Home.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/generator',
      name: 'generator',
      component: () => import('@/pages/Generator.vue')
    },
    {
      path: '/templates',
      name: 'templates',
      component: () => import('@/pages/Templates.vue')
    },
    {
      path: '/docs',
      name: 'docs',
      component: () => import('@/pages/Docs.vue')
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/pages/About.vue')
    }
  ],
})

export default router
