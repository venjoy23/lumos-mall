<template>
  <div class="dashboard" v-loading="loading">
    <div class="stat-grid">
      <div class="stat-card" v-for="s in stats" :key="s.label">
        <div class="s-label">{{ s.label }}</div>
        <div class="s-value">{{ s.value }}</div>
      </div>
    </div>
    <el-card shadow="never" class="order-card">
      <template #header><b>最新订单</b></template>
      <el-table :data="latestOrders" size="small">
        <el-table-column prop="orderNo" label="订单号" width="220" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }"><span style="color: #f56c6c">￥{{ row.totalPrice }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="['warning','primary','success','success','info','danger'][row.status]">{{ ['待支付','已支付','已发货','已完成','已取消','已退款'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const stats = ref([
  { label: '用户总数', value: '-' },
  { label: '商品总数', value: '-' },
  { label: '订单总数', value: '-' },
  { label: '今日入库', value: '-' }
])
const latestOrders = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [u, p, o, w] = await Promise.all([
      request.get('/user/page', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/product/adminPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/order/page', { params: { pageNum: 1, pageSize: 5 } }),
      request.get('/warehouse/page', { params: { pageNum: 1, pageSize: 1 } })
    ])
    stats.value[0].value = u.data.total
    stats.value[1].value = p.data.total
    stats.value[2].value = o.data.total
    stats.value[3].value = w.data.total
    latestOrders.value = o.data.records.map((r) => r.order)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { background: #fff; border-radius: 10px; padding: 22px; }
.s-label { color: #999; font-size: 14px; margin-bottom: 8px; }
.s-value { font-size: 30px; font-weight: bold; color: #1f2430; }
.order-card { border-radius: 10px; }
@media (max-width: 900px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
