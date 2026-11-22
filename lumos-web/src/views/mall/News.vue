<template>
  <div class="news-page">
    <h2>商城资讯</h2>
    <div class="type-bar">
      <span class="type" :class="{ active: !query.typeId }" @click="changeType()">全部</span>
      <span v-for="t in types" :key="t.id" class="type" :class="{ active: query.typeId === t.id }" @click="changeType(t.id)">{{ t.name }}</span>
    </div>
    <div class="news-grid" v-loading="loading">
      <div v-for="n in rows" :key="n.id" class="news-card" @click="$router.push(`/news/${n.id}`)">
        <el-image :src="n.image" fit="cover" class="n-img">
          <template #error><div class="n-fallback"></div></template>
        </el-image>
        <div class="n-body">
          <div class="n-title">{{ n.title }}</div>
          <div class="n-intro">{{ n.intro }}</div>
          <div class="n-date">{{ n.createTime }}</div>
        </div>
      </div>
    </div>
    <el-empty v-if="!loading && !rows.length" description="暂无资讯" />
    <el-pagination class="pager" layout="total, prev, pager, next" :total="total" v-model:current-page="pageNum" :page-size="pageSize" @current-change="load()" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../../api/request'

const types = ref([])
const rows = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 9
const loading = ref(false)
const query = reactive({ typeId: undefined })

onMounted(async () => {
  const res = await request.get('/newsType/list')
  types.value = res.data
  load(1)
})

function changeType(id) {
  query.typeId = id
  load(1)
}

async function load(page) {
  if (page) pageNum.value = page
  loading.value = true
  try {
    const res = await request.get('/news/page', { params: { ...query, pageNum: pageNum.value, pageSize } })
    rows.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.news-page { max-width: 1100px; margin: 0 auto; padding: 24px 20px 40px; }
h2 { margin-bottom: 14px; }
.type-bar { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.type { padding: 6px 16px; border-radius: 18px; background: #fff; cursor: pointer; font-size: 14px; }
.type.active, .type:hover { background: #409eff; color: #fff; }
.news-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.news-card { background: #fff; border-radius: 10px; overflow: hidden; cursor: pointer; transition: box-shadow .2s; }
.news-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.12); }
.n-img { width: 100%; height: 150px; }
.n-fallback { width: 100%; height: 150px; background: #eef1f6; }
.n-body { padding: 12px 14px; }
.n-title { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.n-intro { color: #888; font-size: 13px; height: 38px; overflow: hidden; }
.n-date { color: #bbb; font-size: 12px; margin-top: 8px; }
.pager { margin-top: 20px; justify-content: center; }
@media (max-width: 900px) { .news-grid { grid-template-columns: 1fr; } }
</style>
