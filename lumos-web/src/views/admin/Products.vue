<template>
  <CrudPage
    api="/product"
    title="商品"
    :columns="columns"
    :searchFields="searchFields"
    :formFields="formFields"
    pageUrl="/product/adminPage"
  >
    <template #image="{ row }">
      <el-image v-if="row.image" :src="row.image" fit="cover" style="width: 46px; height: 46px; border-radius: 4px" />
      <span v-else>-</span>
    </template>
    <template #status="{ row }">
      <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架中' : '已下架' }}</el-tag>
    </template>
  </CrudPage>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CrudPage from '../../components/CrudPage.vue'
import request from '../../api/request'

const categories = ref([])

onMounted(async () => {
  const res = await request.get('/category/list')
  categories.value = res.data
})

const columns = [
  { prop: 'id', label: 'ID', width: 60 },
  { prop: 'image', label: '图片', width: 80, slot: 'image' },
  { prop: 'name', label: '商品名称', minWidth: 200 },
  { prop: 'categoryId', label: '分类', width: 100 },
  { prop: 'price', label: '价格（元）', width: 100 },
  { prop: 'stock', label: '库存', width: 80 },
  { prop: 'brand', label: '品牌', width: 110 },
  { prop: 'spec', label: '规格', width: 130 },
  { prop: 'limitNum', label: '单限', width: 70 },
  { prop: 'clickCount', label: '点击', width: 70 },
  { prop: 'status', label: '状态', width: 90, slot: 'status' }
]

const searchFields = [
  { prop: 'name', label: '商品名称' },
  { prop: 'brand', label: '品牌' }
]

const formFields = [
  { prop: 'name', label: '商品名称', required: true },
  { prop: 'categoryId', label: '商品分类', type: 'select', options: categories, required: true },
  { prop: 'price', label: '价格', type: 'money', required: true },
  { prop: 'stock', label: '库存', type: 'number', default: 0 },
  { prop: 'brand', label: '品牌' },
  { prop: 'spec', label: '规格' },
  { prop: 'limitNum', label: '单人限购' },
  { prop: 'image', label: '商品图片', type: 'image' },
  { prop: 'status', label: '状态', type: 'select', default: 1, options: [{ label: '上架', value: 1 }, { label: '下架', value: 0 }] },
  { prop: 'detail', label: '商品详情', type: 'textarea' }
]
</script>
