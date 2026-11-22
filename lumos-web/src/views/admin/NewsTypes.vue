<template>
  <CrudPage
    api="/newsType"
    title="资讯分类"
    :columns="columns"
    :formFields="formFields"
    :paged="false"
    listUrl="/newsType/list"
  />
</template>

<script setup>
import CrudPage from '../../components/CrudPage.vue'

const columns = [
  { prop: 'id', label: 'ID', width: 90 },
  { prop: 'name', label: '分类名称', width: 220 },
  { prop: 'remark', label: '备注' }
]

const formFields = [
  { prop: 'name', label: '分类名称', required: true },
  { prop: 'remark', label: '备注', type: 'textarea' }
]
</script>
