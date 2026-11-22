<template>
  <CrudPage api="/news" title="商城资讯" :columns="columns" :searchFields="searchFields" :formFields="formFields">
    <template #image="{ row }">
      <el-image v-if="row.image" :src="row.image" fit="cover" style="width: 60px; height: 40px; border-radius: 4px" />
      <span v-else>-</span>
    </template>
  </CrudPage>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CrudPage from '../../components/CrudPage.vue'
import request from '../../api/request'

const types = ref([])

onMounted(async () => {
  const res = await request.get('/newsType/list')
  types.value = res.data
})

const columns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'image', label: '封面', width: 90, slot: 'image' },
  { prop: 'title', label: '标题', minWidth: 220 },
  { prop: 'typeId', label: '分类', width: 110 },
  { prop: 'intro', label: '简介', minWidth: 200 },
  { prop: 'createTime', label: '发布时间', width: 170 }
]

const searchFields = [
  { prop: 'title', label: '标题' },
  {
    prop: 'typeId', label: '分类', type: 'select',
    options: types
  }
]

const formFields = [
  { prop: 'title', label: '标题', required: true },
  { prop: 'typeId', label: '所属分类', type: 'select', options: types, required: true },
  { prop: 'intro', label: '简介', type: 'textarea' },
  { prop: 'image', label: '封面图', type: 'image' },
  { prop: 'content', label: '正文', type: 'textarea' }
]
</script>
