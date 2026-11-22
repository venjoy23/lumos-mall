<template>
  <CrudPage ref="crud" api="/order" title="订单" :columns="columns" :searchFields="searchFields" :actionWidth="200" noAdd>
    <template #status="{ row }">
      <el-tag size="small" :type="['warning','primary','success','success','info','danger'][row.order.status]">
        {{ ['待支付','已支付','已发货','已完成','已取消','已退款'][row.order.status] }}
      </el-tag>
    </template>
    <template #items="{ row }">
      <span v-for="item in row.items" :key="item.id" class="item-line">{{ item.productName }} ×{{ item.quantity }}</span>
    </template>
    <template #actions="{ row }">
      <el-button v-if="row.order.status === 1" size="small" type="primary" @click="ship(row.order)">发货</el-button>
      <el-button v-if="row.order.status === 1 || row.order.status === 2" size="small" type="danger" @click="refund(row.order)">退款</el-button>
      <el-button size="small" @click="detail(row.order)">详情</el-button>
    </template>
  </CrudPage>

  <el-dialog v-model="detailVisible" title="订单详情" width="560px">
    <template v-if="current">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号">{{ current.order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ ['待支付','已支付','已发货','已完成','已取消','已退款'][current.order.status] }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ current.order.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ current.order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ current.order.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ current.order.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="current.items" size="small" style="margin-top: 12px">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
      </el-table>
      <div style="text-align: right; margin-top: 10px; color: #f56c6c; font-weight: bold">合计：￥{{ current.order.totalPrice }}</div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CrudPage from '../../components/CrudPage.vue'
import request from '../../api/request'

const crud = ref()
const detailVisible = ref(false)
const current = ref(null)

const columns = [
  { prop: 'order.orderNo', label: '订单编号', width: 200 },
  { prop: 'order.receiverName', label: '收货人', width: 100 },
  { prop: 'items', label: '商品', minWidth: 240, slot: 'items' },
  { prop: 'order.totalPrice', label: '金额（元）', width: 100 },
  { prop: 'status', label: '状态', width: 90, slot: 'status' },
  { prop: 'order.createTime', label: '下单时间', width: 170 }
]

const searchFields = [
  {
    prop: 'status', label: '订单状态', type: 'select',
    options: [
      { label: '待支付', value: 0 }, { label: '已支付', value: 1 }, { label: '已发货', value: 2 },
      { label: '已完成', value: 3 }, { label: '已取消', value: 4 }, { label: '已退款', value: 5 }
    ]
  },
  { prop: 'orderNo', label: '订单编号' }
]

async function ship(order) {
  await ElMessageBox.confirm(`确认订单 ${order.orderNo} 发货？`, '发货确认', { type: 'warning' })
  await request.put(`/order/ship/${order.id}`)
  ElMessage.success('已发货')
  crud.value.load()
}

async function refund(order) {
  await ElMessageBox.confirm(`确认退款订单 ${order.orderNo}？金额将退回用户余额并恢复库存。`, '退款确认', { type: 'warning' })
  await request.put(`/order/refund/${order.id}`)
  ElMessage.success('已退款')
  crud.value.load()
}

async function detail(order) {
  const res = await request.get(`/order/${order.id}`)
  current.value = res.data
  detailVisible.value = true
}
</script>

<style scoped>
.item-line { display: block; font-size: 12px; color: #666; line-height: 1.7; }
</style>
