<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">Lumos 管理后台</div>
      <el-menu :default-active="$route.path" router background-color="#1f2430" text-color="#cfd3dc" active-text-color="#409eff">
        <el-menu-item index="/admin"><el-icon><DataAnalysis /></el-icon>数据概览</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-menu-item index="/admin/admins"><el-icon><Avatar /></el-icon>管理员管理</el-menu-item>
        <el-menu-item index="/admin/categories"><el-icon><Menu /></el-icon>商品分类</el-menu-item>
        <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon>商品信息</el-menu-item>
        <el-menu-item index="/admin/warehouse"><el-icon><Box /></el-icon>商品入库</el-menu-item>
        <el-menu-item index="/admin/orders"><el-icon><Tickets /></el-icon>订单管理</el-menu-item>
        <el-menu-item index="/admin/comments"><el-icon><ChatDotSquare /></el-icon>评论管理</el-menu-item>
        <el-menu-item index="/admin/banners"><el-icon><Picture /></el-icon>轮播图管理</el-menu-item>
        <el-menu-item index="/admin/news-types"><el-icon><Folder /></el-icon>资讯分类</el-menu-item>
        <el-menu-item index="/admin/news"><el-icon><Document /></el-icon>商城资讯</el-menu-item>
        <el-menu-item index="/admin/recharges"><el-icon><Money /></el-icon>充值记录</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ $route.meta.title || '管理后台' }}</span>
        <div class="right">
          <router-link to="/" class="mr">返回商城</router-link>
          <el-dropdown @command="onCommand">
            <span class="admin-name">{{ store.user?.name || store.user?.username }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { store } from '../store'

const router = useRouter()

function onCommand(cmd) {
  if (cmd === 'logout') {
    store.logout()
    router.push('/admin/login')
  }
}
</script>

<style scoped>
.admin-layout { min-height: 100vh; }
.aside { background: #1f2430; }
.logo { color: #fff; font-size: 17px; font-weight: bold; text-align: center; padding: 18px 0; border-bottom: 1px solid #2b3140; }
.header { background: #fff; border-bottom: 1px solid #e5e7eb; display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 16px; font-weight: 600; }
.right { display: flex; align-items: center; gap: 16px; font-size: 14px; }
.mr { color: #409eff; }
.admin-name { cursor: pointer; color: #333; }
.main { background: #f5f6f8; }
</style>
