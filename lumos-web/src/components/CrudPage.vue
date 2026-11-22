<template>
  <div class="crud-page">
    <!-- 搜索区 -->
    <el-form v-if="searchFields.length" :inline="true" :model="query" class="search">
      <el-form-item v-for="f in searchFields" :key="f.prop" :label="f.label">
        <el-input v-if="!f.type || f.type === 'input'" v-model="query[f.prop]" :placeholder="f.placeholder || '请输入'" clearable @keyup.enter="load(1)" />
        <el-input-number v-else-if="f.type === 'number'" v-model="query[f.prop]" :min="0" controls-position="right" style="width: 140px" />
        <el-select v-else-if="f.type === 'select'" v-model="query[f.prop]" placeholder="请选择" clearable style="width: 160px">
          <el-option v-for="opt in f.options" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button v-if="!noAdd" type="primary" @click="openForm()">新增</el-button>
      <slot name="toolbar" />
    </div>

    <!-- 数据表 -->
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column v-for="c in columns" :key="c.prop" :prop="c.prop" :label="c.label" :width="c.width" :show-overflow-tooltip="!c.slot">
        <template #default="{ row }" v-if="c.slot">
          <slot :name="c.slot" :row="row">{{ row[c.prop] }}</slot>
        </template>
      </el-table-column>
      <el-table-column v-if="!hideActions" label="操作" :width="actionWidth" fixed="right">
        <template #default="{ row }">
          <slot name="actions" :row="row">
            <el-button size="small" @click="openForm(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </slot>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="paged"
      class="pager"
      layout="total, prev, pager, next, sizes"
      :total="total"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      @current-change="load()"
      @size-change="load(1)"
    />

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="(form.id ? '编辑' : '新增') + title" width="560px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item v-for="f in formFields" :key="f.prop" :label="f.label" :required="f.required">
          <el-input v-if="!f.type || f.type === 'input'" v-model="form[f.prop]" :placeholder="f.placeholder" :type="f.inputType || 'text'" />
          <el-input v-else-if="f.type === 'textarea'" v-model="form[f.prop]" type="textarea" :rows="4" />
          <el-input-number v-else-if="f.type === 'number'" v-model="form[f.prop]" :min="f.min ?? 0" controls-position="right" style="width: 200px" />
          <el-input-number v-else-if="f.type === 'money'" v-model="form[f.prop]" :min="0" :precision="2" controls-position="right" style="width: 200px" />
          <el-select v-else-if="f.type === 'select'" v-model="form[f.prop]" placeholder="请选择" style="width: 100%">
            <el-option v-for="opt in f.options" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <div v-else-if="f.type === 'image'" class="image-field">
            <el-input v-model="form[f.prop]" placeholder="图片地址，可上传" />
            <el-upload :show-file-list="false" accept="image/*" :http-request="(req) => uploadImage(req, f.prop)">
              <el-button size="small">上传图片</el-button>
            </el-upload>
            <el-image v-if="form[f.prop]" :src="form[f.prop]" fit="cover" class="preview" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const props = defineProps({
  // 请求基础路径，如 /user
  api: { type: String, required: true },
  title: { type: String, default: '' },
  columns: { type: Array, required: true },
  searchFields: { type: Array, default: () => [] },
  formFields: { type: Array, default: () => [] },
  // 分页模式：page 接口返回 Page 结构；list 接口返回数组
  paged: { type: Boolean, default: true },
  noAdd: { type: Boolean, default: false },
  actionWidth: { type: Number, default: 160 },
  pageUrl: { type: String, default: '' },
  listUrl: { type: String, default: '' },
  hideActions: { type: Boolean, default: false }
})

const emit = defineEmits(['loaded'])

const rows = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const query = reactive({})
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({})

onMounted(() => load(1))

async function load(page) {
  if (page) pageNum.value = page
  loading.value = true
  try {
    if (props.paged) {
      const res = await request.get(props.pageUrl || props.api + '/page', { params: { ...query, pageNum: pageNum.value, pageSize: pageSize.value } })
      rows.value = res.data.records
      total.value = res.data.total
    } else {
      const res = await request.get(props.listUrl || props.api + '/all')
      rows.value = res.data
    }
    emit('loaded', rows.value)
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.keys(query).forEach((k) => (query[k] = undefined))
  load(1)
}

function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) {
    props.formFields.forEach((f) => (form[f.prop] = row[f.prop]))
    form.id = row.id
  } else {
    props.formFields.forEach((f) => (form[f.prop] = f.default !== undefined ? f.default : undefined))
  }
  dialogVisible.value = true
}

async function save() {
  for (const f of props.formFields) {
    if (f.required && (form[f.prop] === undefined || form[f.prop] === '' || form[f.prop] === null)) {
      ElMessage.warning('请填写' + f.label)
      return
    }
  }
  saving.value = true
  try {
    if (form.id) {
      await request.put(props.api, form)
    } else {
      await request.post(props.api, form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该记录吗？', '提示', { type: 'warning' })
  await request.delete(`${props.api}/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

async function uploadImage(req, prop) {
  const fd = new FormData()
  fd.append('file', req.file)
  const res = await request.post('/file/upload', fd)
  form[prop] = res.data.url
  ElMessage.success('上传成功')
}

defineExpose({ load, query })
</script>

<style scoped>
.search { background: #fff; padding: 16px 16px 0; border-radius: 6px; margin-bottom: 12px; }
.toolbar { margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
.image-field { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.preview { width: 60px; height: 60px; border-radius: 4px; }
</style>
