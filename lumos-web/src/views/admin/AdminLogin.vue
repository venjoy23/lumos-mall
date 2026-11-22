<template>
  <div class="admin-login">
    <el-card class="login-card">
      <h2>Lumos 管理后台</h2>
      <el-form :model="form" size="large" @keyup.enter="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="管理员账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div class="links"><router-link to="/">返回商城首页</router-link></div>
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
const form = reactive({ username: 'admin', password: '' })
const loading = ref(false)

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/auth/login', { ...form, role: 'ADMIN' })
    store.login(res.data.token, res.data.user, res.data.role)
    ElMessage.success('登录成功')
    router.push('/admin')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1f2430 0%, #2c3e50 100%); }
.login-card { width: 400px; padding: 10px 20px 20px; }
h2 { text-align: center; margin-bottom: 20px; }
.links { margin-top: 14px; text-align: center; font-size: 13px; }
.links a { color: #409eff; }
</style>
