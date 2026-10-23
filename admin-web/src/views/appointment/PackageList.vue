<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.name" placeholder="套餐名称" clearable style="width: 200px" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="primary" @click="openEdit(null)">新增套餐</el-button>
      <el-button type="success" @click="openSlots">生成时段</el-button>
    </div>

    <div class="content-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="套餐名称" width="180" />
        <el-table-column prop="price" label="价格(积分)" width="100" />
        <el-table-column prop="suitablePeople" label="适合人群" width="140" />
        <el-table-column label="包含项目" min-width="220">
          <template #default="{ row }">
            <el-tag v-for="item in parseItems(row.items)" :key="item" size="small" style="margin-right: 4px">
              {{ item }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
              {{ row.status === 'ENABLED' ? '启用' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSlotsManage(row)">时段</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="visible" :title="form.id ? '编辑套餐' : '新增套餐'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="套餐名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格(积分)"><el-input-number v-model="form.price" :min="0" /></el-form-item>
        <el-form-item label="适合人群"><el-input v-model="form.suitablePeople" /></el-form-item>
        <el-form-item label="套餐描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="包含项目">
          <div style="width: 100%">
            <div v-for="(it, i) in form.items" :key="i" style="display: flex; gap: 6px; margin-bottom: 6px">
              <el-input v-model="form.items[i]" placeholder="体检项目" />
              <el-button @click="form.items.splice(i, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="form.items.push('')">+ 添加项目</el-button>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="ENABLED">启用</el-radio>
            <el-radio value="DISABLED">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量生成预约时段 -->
    <el-dialog v-model="slotVisible" title="批量生成预约时段" width="520px">
      <el-form :model="slotForm" label-width="120px">
        <el-form-item label="体检套餐" required>
          <el-select v-model="slotForm.packageId" placeholder="请选择套餐" style="width: 100%">
            <el-option v-for="p in list" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" required>
          <el-date-picker
            v-model="slotForm.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间段" required>
          <div style="width: 100%">
            <div v-for="(t, i) in slotForm.timeRanges" :key="i" style="display: flex; gap: 6px; margin-bottom: 6px">
              <el-input v-model="slotForm.timeRanges[i]" placeholder="如 09:00-10:00" />
              <el-button @click="slotForm.timeRanges.splice(i, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="slotForm.timeRanges.push('')">+ 添加时间段</el-button>
          </div>
        </el-form-item>
        <el-form-item label="每时段人数">
          <el-input-number v-model="slotForm.maxCount" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item v-if="slotCreated !== null">
          <el-tag type="success">已生成 {{ slotCreated }} 个时段</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="slotVisible = false">取消</el-button>
        <el-button type="primary" :loading="slotLoading" @click="onGenerateSlots">批量生成</el-button>
      </template>
    </el-dialog>

    <!-- 时段管理 -->
    <el-dialog v-model="slotsVisible" :title="`时段管理：${currentPkg?.name || ''}`" width="720px">
      <el-table :data="slots" v-loading="slotsLoading" border stripe size="small">
        <el-table-column prop="appointDate" label="日期" width="110" />
        <el-table-column prop="timeRange" label="时间段" width="130" />
        <el-table-column prop="maxCount" label="上限" width="70" />
        <el-table-column prop="currentCount" label="已约" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="slotStatusType(row.status)" size="small">{{ slotStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSlotEdit(row)">编辑</el-button>
            <el-button link type="danger" :disabled="row.currentCount > 0" @click="onSlotDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="slotsVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑时段 -->
    <el-dialog v-model="slotEditVisible" title="编辑时段" width="420px">
      <el-form :model="slotEditForm" label-width="100px">
        <el-form-item label="日期">
          <el-date-picker v-model="slotEditForm.appointDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时间段">
          <el-input v-model="slotEditForm.timeRange" placeholder="如 09:00-10:00" />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="slotEditForm.maxCount" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="slotEditForm.status" style="width: 100%">
            <el-option label="可预约" value="AVAILABLE" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="slotEditVisible = false">取消</el-button>
        <el-button type="primary" @click="onSlotSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPackages, createPackage, updatePackage, deletePackage, batchGenerateSlots, getPackageSlots, updateSlot, deleteSlot } from '../../api/admin'
import { parseItems } from '../../utils/format'

const query = reactive({ pageNum: 1, pageSize: 10, name: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const form = reactive({ id: null, name: '', price: 0, suitablePeople: '', description: '', items: [], status: 'ENABLED' })

const slotVisible = ref(false)
const slotLoading = ref(false)
const slotCreated = ref(null)
const slotForm = reactive({
  packageId: null,
  dateRange: [],
  timeRanges: ['09:00-10:00', '10:00-11:00', '14:00-15:00'],
  maxCount: 10
})

const slotsVisible = ref(false)
const slotsLoading = ref(false)
const slots = ref([])
const currentPkg = ref(null)
const slotEditVisible = ref(false)
const slotEditForm = reactive({ id: null, appointDate: '', timeRange: '', maxCount: 10, status: 'AVAILABLE' })

const slotStatusLabel = (s) => ({ AVAILABLE: '可预约', FULL: '已满', CLOSED: '已关闭' })[s] || s
const slotStatusType = (s) => ({ AVAILABLE: 'success', FULL: 'warning', CLOSED: 'info' })[s] || 'info'

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getPackages(query)
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

function openEdit(row) {
  if (row) {
    Object.assign(form, row, { items: parseItems(row.items) })
  } else {
    Object.assign(form, { id: null, name: '', price: 0, suitablePeople: '', description: '', items: [], status: 'ENABLED' })
  }
  visible.value = true
}

async function onSave() {
  const { coverUrl, ...rest } = form
  const payload = { ...rest, items: form.items.filter((i) => i.trim()) }
  if (form.id) {
    await updatePackage(form.id, payload)
  } else {
    await createPackage(payload)
  }
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除套餐「${row.name}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await deletePackage(row.id)
  ElMessage.success('已删除')
  load()
}

function openSlots() {
  slotForm.packageId = null
  slotForm.dateRange = []
  slotForm.timeRanges = ['09:00-10:00', '10:00-11:00', '14:00-15:00']
  slotForm.maxCount = 10
  slotCreated.value = null
  slotVisible.value = true
}

async function onGenerateSlots() {
  if (!slotForm.packageId) return ElMessage.warning('请选择套餐')
  if (!slotForm.dateRange || slotForm.dateRange.length !== 2) return ElMessage.warning('请选择日期范围')
  const times = slotForm.timeRanges.filter((t) => t.trim())
  if (!times.length) return ElMessage.warning('请至少添加一个时间段')

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (new Date(slotForm.dateRange[0]) < today) {
    return ElMessage.warning('日期不能早于今天，请重新选择日期')
  }

  const dates = []
  const start = new Date(slotForm.dateRange[0])
  const end = new Date(slotForm.dateRange[1])
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    dates.push(`${y}-${m}-${day}`)
  }

  slotLoading.value = true
  try {
    const res = await batchGenerateSlots(slotForm.packageId, { dates, timeRanges: times, maxCount: slotForm.maxCount })
    slotCreated.value = res.data
    ElMessage.success(`成功生成 ${res.data} 个时段`)
  } catch (e) {
    slotCreated.value = null
  } finally {
    slotLoading.value = false
  }
}

async function openSlotsManage(row) {
  currentPkg.value = row
  slotsVisible.value = true
  await loadSlots()
}

async function loadSlots() {
  slotsLoading.value = true
  try {
    const res = await getPackageSlots(currentPkg.value.id)
    slots.value = res.data || []
  } finally {
    slotsLoading.value = false
  }
}

function openSlotEdit(row) {
  Object.assign(slotEditForm, {
    id: row.id,
    appointDate: row.appointDate,
    timeRange: row.timeRange,
    maxCount: row.maxCount,
    status: row.status === 'CLOSED' ? 'CLOSED' : 'AVAILABLE'
  })
  slotEditVisible.value = true
}

async function onSlotSave() {
  if (!slotEditForm.appointDate) return ElMessage.warning('请选择日期')
  if (!slotEditForm.timeRange || !slotEditForm.timeRange.trim()) return ElMessage.warning('请填写时间段')
  await updateSlot(slotEditForm.id, {
    appointDate: slotEditForm.appointDate,
    timeRange: slotEditForm.timeRange.trim(),
    maxCount: slotEditForm.maxCount,
    status: slotEditForm.status
  })
  ElMessage.success('已保存')
  slotEditVisible.value = false
  await loadSlots()
}

async function onSlotDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.appointDate} ${row.timeRange} 时段吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteSlot(row.id)
  ElMessage.success('已删除')
  await loadSlots()
}
</script>
