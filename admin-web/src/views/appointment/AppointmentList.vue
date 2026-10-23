<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="search" />
      <el-select v-model="query.status" placeholder="预约状态" clearable style="width: 140px">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="primary" :disabled="!selectedIds.length" @click="batchSetStatus('CONFIRMED')">批量确认</el-button>
      <el-button type="warning" :disabled="!selectedIds.length" @click="batchSetStatus('COMPLETED')">批量完成</el-button>
      <el-button type="danger" :disabled="!selectedIds.length" @click="batchSetStatus('CANCELED')">批量取消</el-button>
    </div>

    <div class="content-card">
      <el-table ref="tableRef" :data="list" v-loading="loading" border stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="packageName" label="套餐" min-width="140" />
        <el-table-column prop="appointDate" label="日期" width="110" />
        <el-table-column prop="timeRange" label="时段" width="120" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预约时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="['PENDING', 'CONFIRMED'].includes(row.status)" link type="warning" @click="setStatus(row, 'COMPLETED')">
              完成
            </el-button>
            <el-button v-if="row.status === 'PENDING'" link type="primary" @click="setStatus(row, 'CONFIRMED')">
              确认
            </el-button>
            <el-button v-if="row.reportUrl" link type="primary" @click="onDownload(row)">查看报告</el-button>
            <el-button link type="success" @click="openUpload(row)">上传报告</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          @current-change="onPage"
        />
      </div>
    </div>

    <el-dialog v-model="uploadVisible" title="上传体检报告（PDF ≤ 20MB）" width="400px">
      <el-upload drag :auto-upload="false" :limit="1" accept="application/pdf" :on-change="onFileChange">
        <el-icon style="font-size: 40px"><UploadFilled /></el-icon>
        <div>将 PDF 报告拖到此处，或点击上传</div>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!file" :loading="uploading" @click="onUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getAppointments, updateAppointmentStatus, batchUpdateAppointmentStatus, uploadReport, downloadReport } from '../../api/admin'

const statusOptions = [
  { label: '待确认', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELED' }
]

const statusMap = {
  PENDING: { label: '待确认', tag: 'warning' },
  CONFIRMED: { label: '已确认', tag: 'primary' },
  COMPLETED: { label: '已完成', tag: 'success' },
  CANCELED: { label: '已取消', tag: 'info' }
}
const statusLabel = (s) => statusMap[s]?.label || s
const statusTag = (s) => statusMap[s]?.tag || 'info'

const query = reactive({ pageNum: 1, pageSize: 10, phone: '', status: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const tableRef = ref(null)
const selectedIds = ref([])

const uploadVisible = ref(false)
const file = ref(null)
const uploadRow = ref(null)
const uploading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getAppointments(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function onPage(page) {
  query.pageNum = page
  load()
}

async function setStatus(row, status) {
  await updateAppointmentStatus(row.id, status)
  ElMessage.success('已更新')
  load()
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.id)
}

async function batchSetStatus(status) {
  const label = statusMap[status].label
  try {
    await ElMessageBox.confirm(`确定将选中的 ${selectedIds.value.length} 条预约批量置为「${label}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  const res = await batchUpdateAppointmentStatus(selectedIds.value, status)
  ElMessage.success(`已更新 ${res.data} 条预约`)
  load()
  tableRef.value?.clearSelection()
}

function openUpload(row) {
  uploadRow.value = row
  file.value = null
  uploadVisible.value = true
}

function onFileChange(uploadFile) {
  file.value = uploadFile.raw
}

async function onUpload() {
  uploading.value = true
  try {
    await uploadReport(uploadRow.value.id, file.value)
    ElMessage.success('报告已上传')
    uploadVisible.value = false
    load()
  } finally {
    uploading.value = false
  }
}

async function onDownload(row) {
  try {
    const blob = await downloadReport(row.id)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `体检报告-${row.packageName || row.id}.pdf`
    link.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error(e.message || '下载失败')
  }
}
</script>
