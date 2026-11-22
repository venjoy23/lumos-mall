<template>
  <CrudPage
    api="/category"
    title="商品分类"
    :columns="columns"
    :formFields="formFields"
    :paged="false"
    listUrl="/category/list"
  />
</template>

<script setup>
import CrudPage from '../../components/CrudPage.vue'

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '分类名称', width: 200 },
  { prop: 'remark', label: '备注' },
  { prop: 'sort', label: '排序', width: 100 },
  { prop: 'createTime', label: '创建时间', width: 180 }
]

const formFields = [
  { prop: 'name', label: '分类名称', required: true },
  { prop: 'remark', label: '备注', type: 'textarea' },
  { prop: 'sort', label: '排序', type: 'number', default: 0 }
]
</script>
