<template>
  <div class="cart-page">
    <h2>我的购物车</h2>
    <template v-if="rows.length">
      <el-table :data="rows" @selection-change="onSelect" class="cart-table">
        <el-table-column type="selection" width="50" />
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="goods">
              <el-image :src="row.image" fit="cover" class="g-img">
                <template #error><div class="g-fallback"></div></template>
              </el-image>
              <span class="g-name" @click="$router.push(`/product/${row.productId}`)">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }"><span class="price">￥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="row.stock" size="small" controls-position="right" @change="(v) => updateQty(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }"><span class="price">￥{{ (row.price * row.quantity).toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" text @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="settle">
        <span>已选 <b>{{ selected.length }}</b> 种商品，合计：<span class="total">￥{{ total }}</span></span>
        <el-button type="primary" size="large" :disabled="!selected.length" @click="openCheckout(false)">去结算</el-button>
      </div>
    </template>
    <el-empty v-else description="购物车还是空的，快去逛逛吧">
      <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
    </el-empty>

    <!-- 结算弹窗 -->
    <el-dialog v-model="checkoutVisible" title="确认订单" width="600px" destroy-on-close>
      <h4 class="addr-title">选择收货地址</h4>
      <div v-if="addresses.length" class="addr-list">
        <div v-for="a in addresses" :key="a.id" class="addr-item" :class="{ active: checkout.addressId === a.id }" @click="checkout.addressId = a.id">
          <div><b>{{ a.name }}</b> {{ a.phone }}</div>
          <div class="addr-detail">{{ a.detail }}</div>
          <el-tag v-if="a.isDefault" size="small" type="success">默认</el-tag>
        </div>
      </div>
      <el-empty v-else description="暂无收货地址，请先到个人中心添加" :image-size="60" />
      <el-button size="small" text type="primary" @click="$router.push('/profile?tab=address')">管理地址</el-button>
      <h4 class="addr-title">订单备注</h4>
      <el-input v-model="checkout.remark" type="textarea" :rows="2" placeholder="选填" maxlength="200" />
      <div class="order-total">应付金额：<span>￥{{ total }}</span>（当前余额：￥{{ store.user?.balance ?? 0 }}）</div>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!checkout.addressId" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'
import { store } from '../../store'

const route = useRoute()
const router = useRouter()
const rows = ref([])
const selected = ref([])
const addresses = ref([])
const checkoutVisible = ref(false)
const submitting = ref(false)
const checkout = reactive({ addressId: null, remark: '', fromCart: true, items: [] })

const total = computed(() =>
  checkout.items.reduce((sum, i) => {
    const g = rows.value.find((r) => r.productId === i.productId)
    return g ? sum + g.price * i.quantity : sum
  }, 0).toFixed(2)
)

onMounted(async () => {
  await load()
  // 立即购买入口
  if (route.query.buyNow) {
    checkout.items = [{ productId: Number(route.query.buyNow), quantity: Number(route.query.quantity || 1) }]
    checkout.fromCart = false
    const res = await request.get('/address/list')
    addresses.value = res.data
    if (addresses.value.length) checkout.addressId = (addresses.value.find((a) => a.isDefault) || addresses.value[0]).id
    checkoutVisible.value = true
  }
})

async function load() {
  const res = await request.get('/cart/list')
  rows.value = res.data
}

function onSelect(list) {
  selected.value = list
}

async function updateQty(row, value) {
  await request.put(`/cart/${row.id}`, { quantity: value })
}

async function remove(row) {
  await ElMessageBox.confirm('确认从购物车删除该商品？', '提示', { type: 'warning' })
  await request.delete(`/cart/${row.id}`)
  ElMessage.success('已删除')
  load()
}

function openCheckout(fromCart) {
  checkout.fromCart = fromCart
  checkout.items = selected.value.map((r) => ({ productId: r.productId, quantity: r.quantity }))
  checkout.remark = ''
  request.get('/address/list').then((res) => {
    addresses.value = res.data
    if (addresses.value.length) checkout.addressId = (addresses.value.find((a) => a.isDefault) || addresses.value[0]).id
    checkoutVisible.value = true
  })
}

async function submitOrder() {
  submitting.value = true
  try {
    const res = await request.post('/order/create', { ...checkout })
    const orderId = res.data.orderId
    await ElMessageBox.confirm(`订单已创建（${res.data.orderNo}），应付 ￥${res.data.totalPrice}，是否立即使用余额支付？`, '下单成功', {
      type: 'success', confirmButtonText: '立即支付', cancelButtonText: '稍后支付'
    }).then(async () => {
      await request.post(`/order/pay/${orderId}`)
      ElMessage.success('支付成功')
      await refreshProfile()
    }).catch(() => {})
    checkoutVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function refreshProfile() {
  const res = await request.get('/user/profile')
  store.refreshUser(res.data)
}
</script>

<style scoped>
.cart-page { max-width: 1100px; margin: 0 auto; padding: 24px 20px 40px; }
h2 { margin-bottom: 16px; }
.cart-table { background: #fff; border-radius: 10px; padding: 8px; }
.goods { display: flex; align-items: center; gap: 12px; }
.g-img { width: 56px; height: 56px; border-radius: 6px; }
.g-fallback { width: 56px; height: 56px; background: #eef1f6; }
.g-name { cursor: pointer; }
.g-name:hover { color: #409eff; }
.price { color: #f56c6c; }
.settle { background: #fff; margin-top: 12px; border-radius: 10px; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; }
.total { color: #f56c6c; font-size: 22px; font-weight: bold; }
.addr-title { margin: 10px 0; }
.addr-list { display: flex; flex-direction: column; gap: 8px; }
.addr-item { border: 1px solid #dcdfe6; border-radius: 6px; padding: 10px 14px; cursor: pointer; }
.addr-item.active { border-color: #409eff; background: #f0f7ff; }
.addr-detail { color: #666; font-size: 13px; margin: 4px 0; }
.order-total { margin-top: 16px; text-align: right; color: #666; }
.order-total span { color: #f56c6c; font-size: 20px; font-weight: bold; }
</style>
