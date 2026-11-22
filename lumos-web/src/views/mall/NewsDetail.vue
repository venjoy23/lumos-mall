<template>
  <div class="news-detail" v-loading="loading">
    <template v-if="news">
      <h1>{{ news.title }}</h1>
      <div class="meta">{{ news.createTime }}</div>
      <el-image v-if="news.image" :src="news.image" fit="cover" class="cover" />
      <div class="content">{{ news.content }}</div>
      <div class="back"><el-button @click="$router.push('/news')">返回资讯列表</el-button></div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../api/request'

const route = useRoute()
const news = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/news/detail/${route.params.id}`)
    news.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.news-detail { max-width: 800px; margin: 0 auto; padding: 32px 20px 40px; background: #fff; min-height: 60vh; }
h1 { font-size: 24px; margin-bottom: 10px; }
.meta { color: #999; font-size: 13px; margin-bottom: 16px; }
.cover { width: 100%; height: 300px; border-radius: 8px; margin-bottom: 20px; }
.content { white-space: pre-wrap; line-height: 1.9; color: #333; }
.back { margin-top: 30px; text-align: center; }
</style>
