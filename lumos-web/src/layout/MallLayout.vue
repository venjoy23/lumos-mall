<template>
  <div class="mall-layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">Lumos<span>商城</span></router-link>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link to="/products">全部商品</router-link>
          <router-link to="/news">商城资讯</router-link>
        </nav>
        <div class="user-area">
          <template v-if="store.isLogin && !store.isAdmin">
            <router-link to="/cart" class="link"><el-icon><ShoppingCart /></el-icon> 购物车</router-link>
            <el-dropdown @command="onCommand">
              <span class="link user-name">{{ store.user?.name || store.user?.username }} <el-icon><ArrowDown /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else-if="store.isAdmin">
            <router-link to="/admin" class="link">进入管理后台</router-link>
          </template>
          <template v-else>
            <router-link to="/login" class="link">登录</router-link>
            <router-link to="/register" class="link">注册</router-link>
          </template>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <p>Lumos 网络商品贸易平台 · 基于 SpringBoot + Vue.js + MySQL</p>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { store } from '../store'

const router = useRouter()

function onCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  if (cmd === 'logout') {
    store.logout()
    router.push('/')
  }
}
</script>

<style scoped>
.header { background: #1f2430; color: #fff; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1200px; margin: 0 auto; height: 60px; display: flex; align-items: center; padding: 0 20px; gap: 40px; }
.logo { font-size: 22px; font-weight: bold; color: #fff; }
.logo span { color: #409eff; }
.nav { display: flex; gap: 24px; flex: 1; }
.nav a { color: #cfd3dc; font-size: 15px; }
.nav a.router-link-active, .nav a:hover { color: #409eff; }
.user-area { display: flex; align-items: center; gap: 18px; }
.link { color: #cfd3dc; font-size: 14px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; }
.link:hover { color: #409eff; }
.user-name { color: #fff; }
.main { min-height: calc(100vh - 160px); }
.footer { text-align: center; color: #999; font-size: 13px; padding: 24px 0; border-top: 1px solid #e5e7eb; background: #fff; }
</style>
