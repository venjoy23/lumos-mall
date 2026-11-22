<template>
  <CrudPage
    api="/recharge"
    title="充值记录"
    :columns="columns"
    :searchFields="searchFields"
    noAdd
    hideActions
  />
</template>

<script setup>
import CrudPage from '../../components/CrudPage.vue'

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'username', label: '用户账号', width: 180 },
  { prop: 'amount', label: '充值金额（元）', width: 160 },
  { prop: 'createTime', label: '充值时间', width: 200 }
]

const searchFields = [{ prop: 'username', label: '用户账号' }]
</script>
