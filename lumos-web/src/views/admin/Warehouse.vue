<template>
  <CrudPage
    ref="crud"
    api="/warehouse"
    title="入库记录"
    :columns="columns"
    :searchFields="searchFields"
    :formFields="formFields"
    :actionWidth="80"
    noAdd
  >
    <template #productId="{ row }">{{ productName(row.productId) }}</template>
    <template #toolbar>
      <el-button type="warning" @click="inVisible = true">商品入库</el-button>
    </template>
    <template #actions="{ row }">
      <el-button size="small" type="danger" @click="crud.remove(row)">删除</el-button>
    </template>
  </CrudPage>

  <!-- 入库弹窗 -->
  <el-dialog v-model="inVisible" title="商品入库" width="480px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="商品" required>
        <el-select v-model="form.productId" filterable placeholder="选择商品" style="width: 100%">
          <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="入库数量" required>
        <el-input-number v-model="form.quantity" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" placeholder="选填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="inVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitIn">确认入库</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import CrudPage from '../../components/CrudPage.vue'
import request from '../../api/request'

const crud = ref()
const products = ref([])
const inVisible = ref(false)
const saving = ref(false)
const form = reactive({ productId: null, quantity: 1, remark: '' })

onMounted(async () => {
  const res = await request.get('/product/adminPage', { params: { pageNum: 1, pageSize: 500 } })
  products.value = res.data.records
})

const columns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'productId', label: '商品', minWidth: 220, slot: 'productId' },
  { prop: 'quantity', label: '入库数量', width: 110 },
  { prop: 'remark', label: '备注', minWidth: 140 },
  { prop: 'createTime', label: '入库时间', width: 180 }
]

const searchFields = [
  { prop: 'name', label: '商品名称' },
  { prop: 'brand', label: '品牌' }
]

const formFields = []

function productName(id) {
  const p = products.value.find((x) => x.id === id)
  return p ? p.name : id
}

async function submitIn() {
  if (!form.productId) {
    ElMessage.warning('请选择商品')
    return
  }
  saving.value = true
  try {
    await request.post('/warehouse', { ...form })
    ElMessage.success('入库成功')
    inVisible.value = false
    form.productId = null
    form.quantity = 1
    form.remark = ''
    crud.value.load(1)
  } finally {
    saving.value = false
  }
}
</script>
