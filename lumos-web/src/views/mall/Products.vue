<template>
  <div class="products-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="query.name" placeholder="商品名称" clearable style="width: 180px" @keyup.enter="load(1)" />
      <el-input v-model="query.brand" placeholder="品牌" clearable style="width: 140px" @keyup.enter="load(1)" />
      <el-input-number v-model="query.minPrice" placeholder="最低价" :min="0" :precision="2" controls-position="right" style="width: 130px" />
      <span class="dash">—</span>
      <el-input-number v-model="query.maxPrice" placeholder="最高价" :min="0" :precision="2" controls-position="right" style="width: 130px" />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="load(1)">搜索</el-button>
      <el-radio-group v-model="sort" @change="load(1)">
        <el-radio-button value="default">综合</el-radio-button>
        <el-radio-button value="price-asc">价格 ↑</el-radio-button>
        <el-radio-button value="price-desc">价格 ↓</el-radio-button>
        <el-radio-button value="hot">人气</el-radio-button>
      </el-radio-group>
    </div>

    <div class="product-grid" v-loading="loading">
      <div v-for="p in rows" :key="p.id" class="product-card" @click="$router.push(`/product/${p.id}`)">
        <el-image :src="p.image" fit="cover" class="p-img">
          <template #error><div class="img-fallback">{{ p.name }}</div></template>
        </el-image>
        <div class="p-body">
          <div class="p-name">{{ p.name }}</div>
          <div class="p-meta">
            <span class="cat">{{ catName(p.categoryId) }}</span>
            <span>{{ p.brand }}</span>
          </div>
          <div class="p-bottom">
            <span class="p-price">￥{{ p.price }}</span>
            <span class="p-stock">库存 {{ p.stock }}</span>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-if="!loading && !rows.length" description="没有找到相关商品" />
    <el-pagination class="pager" layout="total, prev, pager, next" :total="total" v-model:current-page="pageNum" :page-size="pageSize" @current-change="load()" />

    <!-- 购物车入口 -->
    <div class="cart-fab" @click="$router.push('/cart')"><el-icon :size="26"><ShoppingCart /></el-icon></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../api/request'

const route = useRoute()
const categories = ref([])
const rows = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12
const loading = ref(false)
const query = reactive({ name: '', brand: '', minPrice: undefined, maxPrice: undefined, categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined })
const sort = ref('default')

onMounted(async () => {
  const res = await request.get('/category/list')
  categories.value = res.data
  load(1)
})

watch(() => route.query.categoryId, (v) => {
  query.categoryId = v ? Number(v) : undefined
  load(1)
})

async function load(page) {
  if (page) pageNum.value = page
  loading.value = true
  try {
    const params = { ...query, pageNum: pageNum.value, pageSize }
    if (sort.value === 'price-asc') { params.sortField = 'price'; params.sortOrder = 'asc' }
    else if (sort.value === 'price-desc') { params.sortField = 'price'; params.sortOrder = 'desc' }
    else if (sort.value === 'hot') { params.sortField = 'click'; params.sortOrder = 'desc' }
    const res = await request.get('/product/page', { params })
    rows.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function catName(id) {
  const c = categories.value.find((x) => x.id === id)
  return c ? c.name : ''
}
</script>

<style scoped>
.products-page { max-width: 1200px; margin: 0 auto; padding: 16px 20px 40px; }
.search-bar { background: #fff; padding: 14px 16px; border-radius: 10px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; margin-bottom: 16px; }
.dash { color: #999; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border-radius: 10px; overflow: hidden; cursor: pointer; transition: box-shadow .2s; }
.product-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.12); }
.p-img { width: 100%; height: 200px; }
.img-fallback { width: 100%; height: 200px; display: flex; align-items: center; justify-content: center; background: #eef1f6; color: #999; font-size: 13px; padding: 0 10px; text-align: center; }
.p-body { padding: 10px 12px 12px; }
.p-name { font-size: 14px; height: 40px; overflow: hidden; }
.p-meta { display: flex; justify-content: space-between; color: #999; font-size: 12px; margin: 4px 0; }
.cat { color: #409eff; }
.p-bottom { display: flex; justify-content: space-between; align-items: center; }
.p-price { color: #f56c6c; font-weight: bold; font-size: 17px; }
.p-stock { color: #999; font-size: 12px; }
.pager { margin-top: 20px; justify-content: center; }
.cart-fab { position: fixed; right: 32px; bottom: 40px; width: 52px; height: 52px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; box-shadow: 0 4px 12px rgba(64,158,255,.4); }
@media (max-width: 900px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
