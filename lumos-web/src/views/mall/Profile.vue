<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <div class="balance-card">
      <div>
        <div class="hello">{{ store.user?.name || store.user?.username }}，你好</div>
        <div class="sub">账户余额（元）</div>
      </div>
      <div class="balance">￥{{ store.user?.balance ?? 0 }}</div>
      <el-button type="primary" plain @click="rechargeVisible = true">余额充值</el-button>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="tab">
        <!-- 基本资料 -->
        <el-tab-pane label="基本资料" name="info">
          <el-form :model="infoForm" label-width="90px" style="max-width: 480px">
            <el-form-item label="用户名">
              <el-input v-model="infoForm.username" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="infoForm.name" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="infoForm.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="infoForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="pwdForm" label-width="90px" style="max-width: 480px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirm" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 收货地址 -->
        <el-tab-pane label="我的地址" name="address">
          <el-button type="primary" size="small" style="margin-bottom: 12px" @click="openAddress()">新增地址</el-button>
          <el-table :data="addresses" border>
            <el-table-column prop="name" label="收货人" width="120" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column prop="detail" label="详细地址" />
            <el-table-column label="默认" width="80">
              <template #default="{ row }"><el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" text type="primary" @click="openAddress(row)">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeAddress(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我的订单 -->
        <el-tab-pane label="我的订单" name="orders">
          <el-radio-group v-model="orderStatus" @change="loadOrders" style="margin-bottom: 14px">
            <el-radio-button :value="undefined">全部</el-radio-button>
            <el-radio-button :value="0">待支付</el-radio-button>
            <el-radio-button :value="1">已支付</el-radio-button>
            <el-radio-button :value="2">已发货</el-radio-button>
            <el-radio-button :value="3">已完成</el-radio-button>
            <el-radio-button :value="4">已取消</el-radio-button>
            <el-radio-button :value="5">已退款</el-radio-button>
          </el-radio-group>
          <div v-for="o in orders" :key="o.order.id" class="order-card">
            <div class="o-head">
              <span>订单号：{{ o.order.orderNo }}</span>
              <span>{{ o.order.createTime }}</span>
              <el-tag :type="statusType(o.order.status)" size="small">{{ statusText(o.order.status) }}</el-tag>
            </div>
            <div v-for="item in o.items" :key="item.id" class="o-item">
              <span class="i-name">{{ item.productName }} × {{ item.quantity }}</span>
              <span class="i-price">￥{{ item.price }}</span>
            </div>
            <div class="o-foot">
              <span>合计：<b class="price">￥{{ o.order.totalPrice }}</b></span>
              <div>
                <el-button v-if="o.order.status === 0" size="small" type="primary" @click="payOrder(o.order)">去支付</el-button>
                <el-button v-if="o.order.status === 0" size="small" @click="cancelOrder(o.order)">取消订单</el-button>
                <el-button v-if="o.order.status === 2" size="small" type="success" @click="confirmOrder(o.order)">确认收货</el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="!orders.length" description="暂无订单" />
        </el-tab-pane>

        <!-- 我的收藏 -->
        <el-tab-pane label="我的收藏" name="favorites">
          <div class="fav-grid">
            <div v-for="f in favorites" :key="f.id" class="fav-card" @click="$router.push(`/product/${f.productId}`)">
              <el-image :src="f.image" fit="cover" class="f-img">
                <template #error><div class="f-fallback"></div></template>
              </el-image>
              <div class="f-name">{{ f.name }}</div>
              <div class="f-bottom">
                <span class="price">￥{{ f.price }}</span>
                <el-button size="small" text type="danger" @click.stop="removeFavorite(f)">取消收藏</el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="!favorites.length" description="暂无收藏" />
        </el-tab-pane>

        <!-- 充值记录 -->
        <el-tab-pane label="充值记录" name="recharges">
          <el-table :data="recharges" border>
            <el-table-column prop="amount" label="充值金额（元）" width="180" />
            <el-table-column prop="createTime" label="充值时间" />
          </el-table>
          <el-empty v-if="!recharges.length" description="暂无充值记录" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 地址编辑 -->
    <el-dialog v-model="addressVisible" :title="addressForm.id ? '编辑地址' : '新增地址'" width="480px" destroy-on-close>
      <el-form :model="addressForm" label-width="90px">
        <el-form-item label="收货人" required><el-input v-model="addressForm.name" /></el-form-item>
        <el-form-item label="电话" required><el-input v-model="addressForm.phone" /></el-form-item>
        <el-form-item label="详细地址" required><el-input v-model="addressForm.detail" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="设为默认"><el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>

    <!-- 充值 -->
    <el-dialog v-model="rechargeVisible" title="余额充值" width="420px" destroy-on-close>
      <el-input-number v-model="rechargeAmount" :min="1" :max="100000" :precision="2" controls-position="right" style="width: 100%" placeholder="充值金额" />
      <p class="tip">演示环境为模拟支付，点击确认即到账。</p>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" @click="doRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'
import { store } from '../../store'

const route = useRoute()
const router = useRouter()
const tab = ref(route.query.tab || 'info')
const infoForm = reactive({ username: '', name: '', phone: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const addresses = ref([])
const addressVisible = ref(false)
const addressForm = reactive({ id: null, name: '', phone: '', detail: '', isDefault: 0 })
const orders = ref([])
const orderStatus = ref(undefined)
const favorites = ref([])
const recharges = ref([])
const rechargeVisible = ref(false)
const rechargeAmount = ref(100)

onMounted(async () => {
  await refreshProfile()
  loadAddresses()
  loadOrders()
  loadFavorites()
  loadRecharges()
})

async function refreshProfile() {
  const res = await request.get('/user/profile')
  store.refreshUser(res.data)
  Object.assign(infoForm, res.data)
}

async function saveInfo() {
  await request.put('/user/profile', infoForm)
  await refreshProfile()
  ElMessage.success('保存成功')
}

async function savePassword() {
  if (pwdForm.newPassword !== pwdForm.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  await request.put('/user/password', pwdForm)
  ElMessage.success('密码修改成功，请重新登录')
  store.logout()
  router.push('/login')
}

async function loadAddresses() {
  const res = await request.get('/address/list')
  addresses.value = res.data
}

function openAddress(row) {
  Object.assign(addressForm, row ? { id: row.id, name: row.name, phone: row.phone, detail: row.detail, isDefault: row.isDefault } : { id: null, name: '', phone: '', detail: '', isDefault: 0 })
  addressVisible.value = true
}

async function saveAddress() {
  if (addressForm.id) await request.put('/address', addressForm)
  else await request.post('/address', addressForm)
  ElMessage.success('保存成功')
  addressVisible.value = false
  loadAddresses()
}

async function removeAddress(row) {
  await ElMessageBox.confirm('确认删除该地址？', '提示', { type: 'warning' })
  await request.delete(`/address/${row.id}`)
  loadAddresses()
}

async function loadOrders() {
  const res = await request.get('/order/my', { params: { status: orderStatus.value } })
  orders.value = res.data
}

async function payOrder(order) {
  await ElMessageBox.confirm(`确认使用余额支付 ￥${order.totalPrice}？`, '支付确认', { type: 'warning' })
  await request.post(`/order/pay/${order.id}`)
  ElMessage.success('支付成功')
  await refreshProfile()
  loadOrders()
}

async function cancelOrder(order) {
  await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
  await request.put(`/order/cancel/${order.id}`)
  ElMessage.success('已取消')
  loadOrders()
}

async function confirmOrder(order) {
  await request.put(`/order/confirm/${order.id}`)
  ElMessage.success('已确认收货')
  loadOrders()
}

function statusText(s) {
  return ['待支付', '已支付', '已发货', '已完成', '已取消', '已退款'][s] || '未知'
}

function statusType(s) {
  return ['warning', 'primary', 'success', 'success', 'info', 'danger'][s] || 'info'
}

async function loadFavorites() {
  const res = await request.get('/favorite/list')
  favorites.value = res.data
}

async function removeFavorite(f) {
  await request.delete(`/favorite/${f.id}`)
  loadFavorites()
}

async function loadRecharges() {
  const res = await request.get('/recharge/my')
  recharges.value = res.data
}

async function doRecharge() {
  await request.post('/recharge', { amount: rechargeAmount.value })
  ElMessage.success('充值成功')
  rechargeVisible.value = false
  await refreshProfile()
  loadRecharges()
}
</script>

<style scoped>
.profile-page { max-width: 1000px; margin: 0 auto; padding: 24px 20px 40px; }
h2 { margin-bottom: 16px; }
.balance-card { background: linear-gradient(120deg, #1f2430, #34415c); color: #fff; border-radius: 10px; padding: 22px 28px; display: flex; align-items: center; gap: 30px; margin-bottom: 16px; }
.hello { font-size: 16px; margin-bottom: 6px; }
.sub { color: #aab; font-size: 13px; }
.balance { font-size: 30px; font-weight: bold; flex: 1; }
.order-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 14px; padding: 12px 16px; }
.o-head { display: flex; gap: 20px; color: #666; font-size: 13px; border-bottom: 1px dashed #eee; padding-bottom: 8px; margin-bottom: 8px; }
.o-item { display: flex; justify-content: space-between; padding: 4px 0; color: #444; }
.i-price { color: #f56c6c; }
.o-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; padding-top: 8px; border-top: 1px dashed #eee; }
.price { color: #f56c6c; font-size: 16px; }
.fav-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.fav-card { border: 1px solid #ebeef5; border-radius: 8px; padding: 10px; cursor: pointer; }
.fav-card:hover { box-shadow: 0 2px 10px rgba(0,0,0,.08); }
.f-img { width: 100%; height: 120px; border-radius: 6px; }
.f-fallback { width: 100%; height: 120px; background: #eef1f6; }
.f-name { font-size: 13px; height: 36px; overflow: hidden; margin-top: 6px; }
.f-bottom { display: flex; justify-content: space-between; align-items: center; }
.tip { color: #999; font-size: 12px; margin-top: 10px; }
@media (max-width: 900px) { .fav-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
