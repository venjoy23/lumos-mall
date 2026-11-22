<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="product">
      <div class="top">
        <div class="gallery">
          <el-image :src="product.image" fit="cover" class="main-img">
            <template #error><div class="img-fallback">{{ product.name }}</div></template>
          </el-image>
        </div>
        <div class="info">
          <h1 class="title">{{ product.name }}</h1>
          <div class="price-row">
            <span class="label">价格</span>
            <span class="price">￥{{ product.price }}</span>
          </div>
          <div class="meta"><span>品牌：{{ product.brand || '-' }}</span><span>规格：{{ product.spec || '-' }}</span></div>
          <div class="meta"><span>分类：{{ categoryName }}</span><span>点击次数：{{ product.clickCount }}</span></div>
          <div class="meta"><span>库存：{{ product.stock }} 件</span><span v-if="product.limitNum">单人限购 {{ product.limitNum }} 件</span></div>
          <div class="buy-row">
            <span class="label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="product.limitNum || product.stock" controls-position="right" />
          </div>
          <div class="actions">
            <el-button type="primary" size="large" @click="buyNow">立即购买</el-button>
            <el-button size="large" @click="addCart">加入购物车</el-button>
            <el-button size="large" :type="favored ? 'danger' : 'default'" @click="toggleFavorite">{{ favored ? '已收藏' : '收藏' }}</el-button>
          </div>
        </div>
      </div>

      <el-tabs class="tabs">
        <el-tab-pane label="商品详情">
          <div class="detail-content">{{ product.detail || '暂无详情' }}</div>
        </el-tab-pane>
        <el-tab-pane :label="`用户评价 (${comments.total})`">
          <div v-for="c in comments.records" :key="c.id" class="comment">
            <div class="c-head"><span class="c-user">{{ c.username }}</span><span class="c-time">{{ c.createTime }}</span></div>
            <div class="c-content">{{ c.content }}</div>
            <div v-if="c.reply" class="c-reply">商家回复：{{ c.reply }}</div>
          </div>
          <el-empty v-if="!comments.records.length" description="暂无评论" />
          <el-pagination v-if="comments.total > 10" layout="prev, pager, next" :total="comments.total" :page-size="10" v-model:current-page="commentPage" @current-change="loadComments" style="margin-top: 12px; justify-content: center" />
          <div class="comment-form">
            <el-input v-model="myComment" type="textarea" :rows="3" placeholder="写下你的评价…" maxlength="500" show-word-limit />
            <el-button type="primary" style="margin-top: 10px" @click="submitComment">发表评论</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
import { store } from '../../store'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const categories = ref([])
const quantity = ref(1)
const loading = ref(false)
const favored = ref(false)
const comments = ref({ records: [], total: 0 })
const commentPage = ref(1)
const myComment = ref('')

const categoryName = computed(() => {
  const c = categories.value.find((x) => x.id === product.value?.categoryId)
  return c ? c.name : '-'
})

onMounted(async () => {
  const res = await request.get('/category/list')
  categories.value = res.data
  await loadProduct()
})

watch(() => route.params.id, () => loadProduct())

async function loadProduct() {
  loading.value = true
  try {
    const res = await request.get(`/product/detail/${route.params.id}`)
    product.value = res.data
    quantity.value = 1
    loadComments(1)
    if (store.isLogin && !store.isAdmin) {
      const f = await request.get(`/favorite/check/${route.params.id}`).catch(() => ({ data: false }))
      favored.value = f.data
    }
  } finally {
    loading.value = false
  }
}

async function loadComments(page) {
  commentPage.value = page || commentPage.value
  const res = await request.get('/comment/page', { params: { productId: route.params.id, pageNum: commentPage.value, pageSize: 10 } })
  comments.value = res.data
}

function requireLogin() {
  if (!store.isLogin || store.isAdmin) {
    ElMessage.warning('请先登录用户账号')
    router.push('/login')
    return false
  }
  return true
}

async function addCart() {
  if (!requireLogin()) return
  await request.post('/cart', { productId: product.value.id, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}

async function buyNow() {
  if (!requireLogin()) return
  router.push({ path: '/cart', query: { buyNow: product.value.id, quantity: quantity.value } })
}

async function toggleFavorite() {
  if (!requireLogin()) return
  const res = await request.post(`/favorite/toggle/${product.value.id}`)
  favored.value = res.data
  ElMessage.success(favored.value ? '收藏成功' : '已取消收藏')
}

async function submitComment() {
  if (!requireLogin()) return
  if (!myComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  await request.post('/comment', { productId: product.value.id, content: myComment.value.trim() })
  myComment.value = ''
  ElMessage.success('评论成功')
  loadComments(1)
}
</script>

<style scoped>
.detail-page { max-width: 1200px; margin: 0 auto; padding: 24px 20px 40px; }
.top { display: flex; gap: 32px; background: #fff; border-radius: 10px; padding: 24px; }
.gallery { flex: 0 0 420px; }
.main-img { width: 420px; height: 420px; border-radius: 8px; }
.img-fallback { width: 420px; height: 420px; display: flex; align-items: center; justify-content: center; background: #eef1f6; color: #999; }
.info { flex: 1; }
.title { font-size: 22px; margin-bottom: 18px; }
.price-row { background: #fff5f5; padding: 12px 16px; border-radius: 6px; margin-bottom: 14px; }
.label { color: #999; margin-right: 12px; }
.price { color: #f56c6c; font-size: 26px; font-weight: bold; }
.meta { display: flex; gap: 40px; color: #666; font-size: 14px; margin: 10px 0; }
.buy-row { margin: 18px 0; display: flex; align-items: center; }
.actions { display: flex; gap: 12px; margin-top: 22px; }
.tabs { background: #fff; border-radius: 10px; margin-top: 20px; padding: 12px 24px; }
.detail-content { white-space: pre-wrap; color: #444; line-height: 1.8; padding: 10px 0 20px; }
.comment { border-bottom: 1px solid #f0f0f0; padding: 14px 0; }
.c-head { display: flex; justify-content: space-between; margin-bottom: 6px; }
.c-user { font-weight: 600; font-size: 14px; }
.c-time { color: #999; font-size: 12px; }
.c-content { color: #444; line-height: 1.7; }
.c-reply { background: #f7f8fa; border-radius: 6px; padding: 8px 12px; margin-top: 8px; color: #666; font-size: 13px; }
.comment-form { margin-top: 16px; }
@media (max-width: 900px) { .top { flex-direction: column; } .gallery { flex: none; } .main-img, .img-fallback { width: 100%; height: 300px; } }
</style>
