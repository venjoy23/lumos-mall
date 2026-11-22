<template>
  <CrudPage
    api="/banner"
    title="轮播图"
    :columns="columns"
    :formFields="formFields"
    :paged="false"
    listUrl="/banner/all"
  >
    <template #image="{ row }">
      <el-image v-if="row.image" :src="row.image" fit="cover" style="width: 80px; height: 44px; border-radius: 4px" />
      <span v-else>-</span>
    </template>
    <template #status="{ row }">
      <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
    </template>
  </CrudPage>
</template>

<script setup>
import CrudPage from '../../components/CrudPage.vue'

const columns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'image', label: '图片', width: 110, slot: 'image' },
  { prop: 'name', label: '名称', minWidth: 180 },
  { prop: 'url', label: '跳转链接', minWidth: 200 },
  { prop: 'sort', label: '顺序', width: 80 },
  { prop: 'status', label: '状态', width: 90, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 170 }
]

const formFields = [
  { prop: 'name', label: '名称' },
  { prop: 'image', label: '图片', type: 'image', required: true },
  { prop: 'url', label: '跳转链接', placeholder: '如 /products?categoryId=1' },
  { prop: 'sort', label: '展示顺序', type: 'number', default: 0 },
  { prop: 'status', label: '状态', type: 'select', default: 1, options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }] }
]
</script>
