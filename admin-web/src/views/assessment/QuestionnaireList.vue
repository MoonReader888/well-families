<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="openEdit(null)">新增问卷</el-button>
    </div>

    <div class="content-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="问卷标题" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="计分" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRated === 1 ? 'success' : 'info'" size="small">
              {{ row.isRated === 1 ? '计分' : '不计分' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openQuestions(row)">题目</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="toggleStatus(row)">
              {{ row.status === 'PUBLISHED' ? '下架' : '发布' }}
            </el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 问卷编辑 -->
    <el-dialog v-model="visible" :title="form.id ? '编辑问卷' : '新增问卷'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="是否计分">
          <el-switch v-model="form.isRated" :active-value="1" :inactive-value="0" active-text="计分" inactive-text="不计分" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 题目管理 -->
    <el-drawer v-model="questionVisible" :title="`题目管理：${currentQ?.title || ''}`" size="560px">
      <el-button type="primary" size="small" style="margin-bottom: 12px" @click="openQuestionEdit(null)">新增题目</el-button>
      <el-table :data="questions" size="small" border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="content" label="题目" min-width="180" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            {{ typeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openQuestionEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDeleteQuestion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="qVisible" :title="qForm.id ? '编辑题目' : '新增题目'" width="480px" append-to-body>
        <el-form :model="qForm" label-width="80px">
          <el-form-item label="题目内容"><el-input v-model="qForm.content" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="类型">
            <el-select v-model="qForm.type" style="width: 100%">
              <el-option label="单选" value="SINGLE" />
              <el-option label="多选" value="MULTIPLE" />
              <el-option label="文本" value="TEXT" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="qForm.type !== 'TEXT'" label="选项">
            <div style="width: 100%">
              <div v-for="(opt, i) in qForm.options" :key="i" style="display: flex; gap: 6px; margin-bottom: 6px; align-items: center">
                <el-input v-model="opt.label" placeholder="选项文本" />
                <el-input-number v-model="opt.score" :min="0" :max="100" :controls="false" placeholder="分值" style="width: 90px" />
                <el-button @click="qForm.options.splice(i, 1)">删除</el-button>
              </div>
              <el-button size="small" @click="qForm.options.push({ label: '', score: 0 })">+ 添加选项</el-button>
            </div>
          </el-form-item>
          <el-form-item label="排序号"><el-input-number v-model="qForm.sortOrder" :min="0" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="qVisible = false">取消</el-button>
          <el-button type="primary" @click="onSaveQuestion">保存</el-button>
        </template>
      </el-dialog>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getQuestionnaires, createQuestionnaire, updateQuestionnaire, updateQuestionnaireStatus, deleteQuestionnaire,
  getQuestions, createQuestion, updateQuestion, deleteQuestion
} from '../../api/admin'
import { parseOptions } from '../../utils/format'

const typeLabel = (t) => ({ SINGLE: '单选', MULTIPLE: '多选', TEXT: '文本' })[t] || t

const list = ref([])
const loading = ref(false)
const visible = ref(false)
const form = reactive({ id: null, title: '', description: '', status: 'DRAFT', isRated: 1 })

const questionVisible = ref(false)
const currentQ = ref(null)
const questions = ref([])
const qVisible = ref(false)
const qForm = reactive({ id: null, content: '', type: 'SINGLE', options: [], sortOrder: 0 })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getQuestionnaires()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, title: '', description: '', status: 'DRAFT', isRated: 1 })
  }
  visible.value = true
}

async function onSave() {
  if (form.id) {
    await updateQuestionnaire(form.id, form)
  } else {
    await createQuestionnaire(form)
  }
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function toggleStatus(row) {
  const next = row.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED'
  await updateQuestionnaireStatus(row.id, next)
  ElMessage.success('已更新')
  load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除问卷「${row.title}」及其题目吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteQuestionnaire(row.id)
  ElMessage.success('已删除')
  load()
}

async function openQuestions(row) {
  currentQ.value = row
  questionVisible.value = true
  const res = await getQuestions(row.id)
  questions.value = res.data || []
}

function openQuestionEdit(row) {
  if (row) {
    Object.assign(qForm, row, { options: parseOptions(row.options) })
  } else {
    Object.assign(qForm, { id: null, content: '', type: 'SINGLE', options: [{ label: '', score: 0 }, { label: '', score: 0 }], sortOrder: questions.value.length })
  }
  qVisible.value = true
}

async function onSaveQuestion() {
  if (qForm.type !== 'TEXT' && !qForm.options.some((o) => o.label.trim())) {
    return ElMessage.warning('单选/多选必须提供选项')
  }
  if (qForm.type !== 'TEXT' && qForm.options.some((o) => (o.score ?? 0) < 0 || (o.score ?? 0) > 100)) {
    return ElMessage.warning('选项分值需在 0-100 之间')
  }
  const payload = { content: qForm.content, type: qForm.type, sortOrder: qForm.sortOrder, options: qForm.options }
  if (qForm.id) {
    await updateQuestion(qForm.id, payload)
  } else {
    await createQuestion(currentQ.value.id, payload)
  }
  ElMessage.success('已保存')
  qVisible.value = false
  const res = await getQuestions(currentQ.value.id)
  questions.value = res.data || []
}

async function onDeleteQuestion(row) {
  try {
    await ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteQuestion(row.id)
  ElMessage.success('已删除')
  const res = await getQuestions(currentQ.value.id)
  questions.value = res.data || []
}
</script>
