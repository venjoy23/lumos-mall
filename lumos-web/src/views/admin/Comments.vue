<template>
  <CrudPage ref="crud" api="/comment" title="评论" :columns="columns" :searchFields="searchFields" :actionWidth="160" noAdd pageUrl="/comment/adminPage">
    <template #actions="{ row }">
      <el-button size="small" type="primary" @click="openReply(row)">回复</el-button>
      <el-button size="small" type="danger" @click="crud.remove(row)">删除</el-button>
    </template>
  </CrudPage>

  <el-dialog v-model="replyVisible" title="回复评论" width="480px" destroy-on-close>
    <p class="origin">{{ current?.content }}</p>
    <el-input v-model="replyText" type="textarea" :rows="3" placeholder="请输入回复内容" />
    <template #footer>
      <el-button @click="replyVisible = false">取消</el-button>
      <el-button type="primary" @click="submitReply">提交回复</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import CrudPage from '../../components/CrudPage.vue'
import request from '../../api/request'

const crud = ref()
const replyVisible = ref(false)
const current = ref(null)
const replyText = ref('')

const columns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'username', label: '评论用户', width: 120 },
  { prop: 'productId', label: '商品ID', width: 90 },
  { prop: 'content', label: '评论内容', minWidth: 240 },
  { prop: 'reply', label: '商家回复', minWidth: 200 },
  { prop: 'createTime', label: '评论时间', width: 170 }
]

const searchFields = [{ prop: 'productId', label: '商品ID', type: 'number' }]

function openReply(row) {
  current.value = row
  replyText.value = row.reply || ''
  replyVisible.value = true
}

async function submitReply() {
  await request.put('/comment/reply', { id: current.value.id, reply: replyText.value })
  ElMessage.success('回复成功')
  replyVisible.value = false
  crud.value.load()
}
</script>

<style scoped>
.origin { background: #f5f6f8; border-radius: 6px; padding: 10px 14px; margin-bottom: 12px; color: #666; }
</style>
