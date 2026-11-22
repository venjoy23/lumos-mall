<template>
  <CrudPage api="/user" title="用户" :columns="columns" :searchFields="searchFields" :formFields="formFields" />
</template>

<script setup>
import CrudPage from '../../components/CrudPage.vue'

const columns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'username', label: '用户账号', width: 140 },
  { prop: 'name', label: '姓名', width: 120 },
  { prop: 'phone', label: '联系电话', width: 140 },
  { prop: 'email', label: '邮箱' },
  { prop: 'balance', label: '余额（元）', width: 110 },
  { prop: 'createTime', label: '注册时间', width: 170 }
]

const searchFields = [{ prop: 'username', label: '用户账号' }]

const formFields = [
  { prop: 'username', label: '用户账号', required: true },
  { prop: 'name', label: '姓名' },
  { prop: 'phone', label: '联系电话' },
  { prop: 'email', label: '邮箱' },
  { prop: 'password', label: '密码', placeholder: '编辑时留空则不修改', inputType: 'password' }
]
</script>
