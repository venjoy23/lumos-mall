<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>用户登录</h2>
      <el-form :model="form" size="large" @keyup.enter="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div class="links">
        <router-link to="/register">没有账号？立即注册</router-link>
        <router-link to="/admin/login">管理员入口</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
import { store } from '../../store'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/auth/login', { ...form, role: 'USER' })
    store.login(res.data.token, res.data.user, res.data.role)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { min-height: calc(100vh - 60px); display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1f2430 0%, #2c3e50 100%); }
.auth-card { width: 400px; padding: 10px 20px 20px; }
h2 { text-align: center; margin-bottom: 20px; }
.links { display: flex; justify-content: space-between; margin-top: 14px; font-size: 13px; }
.links a { color: #409eff; }
</style>
