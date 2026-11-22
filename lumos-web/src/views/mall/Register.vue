<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>用户注册</h2>
      <el-form :model="form" size="large">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名（3-20 位）" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.name" placeholder="姓名（选填）" prefix-icon="Postcard" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.phone" placeholder="手机号（选填）" prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（至少 6 位）" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">注 册</el-button>
      </el-form>
      <div class="links">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const router = useRouter()
const form = reactive({ username: '', name: '', phone: '', password: '', confirmPassword: '' })
const loading = ref(false)

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await request.post('/auth/register', form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { min-height: calc(100vh - 60px); display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1f2430 0%, #2c3e50 100%); }
.auth-card { width: 400px; padding: 10px 20px 20px; }
h2 { text-align: center; margin-bottom: 20px; }
.links { margin-top: 14px; font-size: 13px; text-align: center; }
.links a { color: #409eff; }
</style>
