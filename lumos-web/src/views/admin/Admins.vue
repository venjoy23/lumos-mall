<template>
  <CrudPage api="/admin" title="管理员" :columns="columns" :searchFields="searchFields" :formFields="formFields" />
</template>

<script setup>
import CrudPage from '../../components/CrudPage.vue'

const columns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'username', label: '账号', width: 150 },
  { prop: 'name', label: '姓名', width: 140 },
  { prop: 'phone', label: '联系电话', width: 150 },
  { prop: 'createTime', label: '创建时间', width: 180 }
]

const searchFields = [{ prop: 'username', label: '账号' }]

const formFields = [
  { prop: 'username', label: '账号', required: true },
  { prop: 'name', label: '姓名' },
  { prop: 'phone', label: '联系电话' },
  { prop: 'password', label: '密码', placeholder: '编辑时留空则不修改', inputType: 'password' }
]
</script>
