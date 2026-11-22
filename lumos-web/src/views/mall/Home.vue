<template>
  <div class="home">
    <!-- 轮播图 -->
    <el-carousel v-if="banners.length" height="380px" class="banner">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <a :href="b.url || 'javascript:void(0)'" target="_blank">
          <el-image :src="b.image" fit="cover" class="banner-img">
            <template #error><div class="banner-fallback">{{ b.name }}</div></template>
          </el-image>
        </a>
      </el-carousel-item>
    </el-carousel>
    <div v-else class="banner-empty">欢迎来到 Lumos 商城</div>

    <!-- 分类导航 -->
    <div class="section category-bar">
      <router-link v-for="c in categories" :key="c.id" :to="`/products?categoryId=${c.id}`" class="cat-item">{{ c.name }}</router-link>
    </div>

    <!-- 热门商品 -->
    <div class="section">
      <h2 class="section-title">热门商品</h2>
      <div class="product-grid" v-loading="loading">
        <div v-for="p in hotProducts" :key="p.id" class="product-card" @click="$router.push(`/product/${p.id}`)">
          <el-image :src="p.image" fit="cover" class="p-img">
            <template #error><div class="img-fallback">{{ p.name }}</div></template>
          </el-image>
          <div class="p-name">{{ p.name }}</div>
          <div class="p-bottom">
            <span class="p-price">￥{{ p.price }}</span>
            <span class="p-click">热度 {{ p.clickCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新资讯 -->
    <div class="section" v-if="newsList.length">
      <h2 class="section-title">商城资讯 <router-link to="/news" class="more">更多 →</router-link></h2>
      <div class="news-list">
        <div v-for="n in newsList" :key="n.id" class="news-item" @click="$router.push(`/news/${n.id}`)">
          <span class="news-title">{{ n.title }}</span>
          <span class="news-date">{{ (n.createTime || '').slice(0, 10) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const banners = ref([])
const categories = ref([])
const hotProducts = ref([])
const newsList = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [b, c, p, n] = await Promise.all([
      request.get('/banner/list').catch(() => ({ data: [] })),
      request.get('/category/list'),
      request.get('/product/hot', { params: { limit: 8 } }),
      request.get('/news/page', { params: { pageNum: 1, pageSize: 5 } })
    ])
    banners.value = b.data
    categories.value = c.data
    hotProducts.value = p.data
    newsList.value = n.data.records
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home { max-width: 1200px; margin: 0 auto; padding: 16px 20px; }
.banner { border-radius: 10px; overflow: hidden; }
.banner-img { width: 100%; height: 100%; }
.banner-fallback { width: 100%; height: 380px; display: flex; align-items: center; justify-content: center; background: linear-gradient(120deg, #1f2430, #409eff); color: #fff; font-size: 28px; }
.banner-empty { height: 160px; display: flex; align-items: center; justify-content: center; color: #888; }
.section { margin-top: 28px; }
.category-bar { display: flex; gap: 12px; flex-wrap: wrap; background: #fff; padding: 14px 16px; border-radius: 10px; }
.cat-item { padding: 8px 18px; background: #f2f6fc; border-radius: 20px; color: #333; font-size: 14px; }
.cat-item:hover { background: #409eff; color: #fff; }
.section-title { font-size: 20px; margin-bottom: 14px; display: flex; justify-content: space-between; align-items: center; }
.more { font-size: 13px; color: #409eff; font-weight: normal; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border-radius: 10px; overflow: hidden; cursor: pointer; transition: box-shadow .2s; }
.product-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.12); }
.p-img { width: 100%; height: 200px; }
.img-fallback { width: 100%; height: 200px; display: flex; align-items: center; justify-content: center; background: #eef1f6; color: #999; font-size: 13px; padding: 0 10px; text-align: center; }
.p-name { padding: 10px 12px 4px; font-size: 14px; height: 42px; overflow: hidden; }
.p-bottom { display: flex; justify-content: space-between; padding: 0 12px 12px; }
.p-price { color: #f56c6c; font-weight: bold; font-size: 17px; }
.p-click { color: #999; font-size: 12px; align-self: flex-end; }
.news-list { background: #fff; border-radius: 10px; padding: 8px 20px; }
.news-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.news-item:last-child { border-bottom: none; }
.news-title:hover { color: #409eff; }
.news-date { color: #999; font-size: 13px; }
@media (max-width: 900px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
