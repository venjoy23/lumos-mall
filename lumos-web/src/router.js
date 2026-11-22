import { createRouter, createWebHistory } from 'vue-router'
import { store } from './store'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('./layout/MallLayout.vue'),
      children: [
        { path: '', component: () => import('./views/mall/Home.vue') },
        { path: 'products', component: () => import('./views/mall/Products.vue') },
        { path: 'product/:id', component: () => import('./views/mall/ProductDetail.vue') },
        { path: 'news', component: () => import('./views/mall/News.vue') },
        { path: 'news/:id', component: () => import('./views/mall/NewsDetail.vue') },
        { path: 'cart', component: () => import('./views/mall/Cart.vue'), meta: { userAuth: true } },
        { path: 'profile', component: () => import('./views/mall/Profile.vue'), meta: { userAuth: true } }
      ]
    },
    { path: '/login', component: () => import('./views/mall/Login.vue') },
    { path: '/register', component: () => import('./views/mall/Register.vue') },
    { path: '/admin/login', component: () => import('./views/admin/AdminLogin.vue') },
    {
      path: '/admin',
      component: () => import('./layout/AdminLayout.vue'),
      meta: { adminAuth: true },
      children: [
        { path: '', component: () => import('./views/admin/Dashboard.vue') },
        { path: 'users', component: () => import('./views/admin/Users.vue') },
        { path: 'admins', component: () => import('./views/admin/Admins.vue') },
        { path: 'categories', component: () => import('./views/admin/Categories.vue') },
        { path: 'products', component: () => import('./views/admin/Products.vue') },
        { path: 'warehouse', component: () => import('./views/admin/Warehouse.vue') },
        { path: 'orders', component: () => import('./views/admin/Orders.vue') },
        { path: 'comments', component: () => import('./views/admin/Comments.vue') },
        { path: 'news-types', component: () => import('./views/admin/NewsTypes.vue') },
        { path: 'news', component: () => import('./views/admin/News.vue') },
        { path: 'banners', component: () => import('./views/admin/Banners.vue') },
        { path: 'recharges', component: () => import('./views/admin/Recharges.vue') }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach((to) => {
  if (to.meta.adminAuth && !store.isAdmin) return '/admin/login'
  if (to.meta.userAuth && !store.isLogin) return '/login'
  return true
})

export default router
