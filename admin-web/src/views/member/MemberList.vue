<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 150px" @keyup.enter="search" />
      <el-input v-model="query.realName" placeholder="姓名" clearable style="width: 140px" @keyup.enter="search" />
      <el-select v-model="query.status" placeholder="账号状态" clearable style="width: 140px">
        <el-option label="正常" value="ENABLED" />
        <el-option label="禁用" value="DISABLED" />
      </el-select>
      <el-select v-model="query.memberLevel" placeholder="会员等级" clearable style="width: 140px">
        <el-option v-for="lv in levels" :key="lv" :label="lv" :value="lv" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" :disabled="!selectedIds.length" @click="batchToggle('ENABLED')">批量启用</el-button>
      <el-button type="danger" :disabled="!selectedIds.length" @click="batchToggle('DISABLED')">批量禁用</el-button>
    </div>

    <div class="content-card">
      <el-table ref="tableRef" :data="list" v-loading="loading" border stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.memberLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ENABLED' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="warning" @click="openLevel(row)">等级</el-button>
            <el-button link type="success" @click="openPoints(row)">积分</el-button>
            <el-button link type="danger" @click="openResetPassword(row)">重置密码</el-button>
            <el-button :type="row.status === 'ENABLED' ? 'danger' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
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

    <!-- 会员详情 -->
    <el-drawer v-model="detailVisible" title="会员详情" size="480px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="手机号">{{ detail.user?.phone }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detail.user?.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detail.user?.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ detail.user?.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ detail.user?.height || '-' }} cm</el-descriptions-item>
        <el-descriptions-item label="等级">{{ detail.user?.memberLevel }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ detail.user?.points }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ detail.user?.emergencyContact || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>健康记录（最近 10 条）</el-divider>
      <el-table :data="detail?.healthRecords || []" size="small">
        <el-table-column prop="recordedAt" label="时间" width="110" />
        <el-table-column label="血压" width="90">
          <template #default="{ row }">{{ row.systolic }}/{{ row.diastolic }}</template>
        </el-table-column>
        <el-table-column prop="bloodSugar" label="血糖" width="80" />
        <el-table-column prop="heartRate" label="心率" width="80" />
        <el-table-column prop="weight" label="体重" />
      </el-table>

      <el-divider>预约记录</el-divider>
      <el-table :data="detail?.appointments || []" size="small">
        <el-table-column prop="packageName" label="套餐" />
        <el-table-column prop="appointDate" label="日期" width="100" />
        <el-table-column prop="status" label="状态" width="90" />
      </el-table>
    </el-drawer>

    <!-- 调整等级 -->
    <el-dialog v-model="levelVisible" title="调整会员等级" width="360px">
      <el-select v-model="levelForm.memberLevel" style="width: 100%">
        <el-option v-for="lv in levels" :key="lv" :label="lv" :value="lv" />
      </el-select>
      <template #footer>
        <el-button @click="levelVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLevel">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调整积分 -->
    <el-dialog v-model="pointsVisible" title="手动调整积分" width="360px">
      <el-form label-width="80px">
        <el-form-item label="变动值">
          <el-input-number v-model="pointsForm.points" :step="50" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="pointsForm.reason" placeholder="如：活动奖励补发" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPoints">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog v-model="pwdVisible" title="重置会员密码" width="360px">
      <el-input v-model="pwdForm.newPassword" type="password" placeholder="8 位以上，含字母和数字" show-password />
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMembers, getMemberDetail, updateMemberStatus, batchUpdateMemberStatus, updateMemberLevel,
  adjustMemberPoints, resetMemberPassword
} from '../../api/admin'

const levels = ['NORMAL', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND']

const query = reactive({ pageNum: 1, pageSize: 10, phone: '', realName: '', status: '', memberLevel: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const tableRef = ref(null)
const selectedIds = ref([])

const detailVisible = ref(false)
const detail = ref(null)
const levelVisible = ref(false)
const levelForm = reactive({ memberId: null, memberLevel: '' })
const pointsVisible = ref(false)
const pointsForm = reactive({ memberId: null, points: 0, reason: '' })
const pwdVisible = ref(false)
const pwdForm = reactive({ memberId: null, newPassword: '' })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getMembers(query)
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

async function openDetail(row) {
  const res = await getMemberDetail(row.id)
  detail.value = res.data
  detailVisible.value = true
}

async function toggleStatus(row) {
  const next = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  try {
    await ElMessageBox.confirm(`确定${next === 'DISABLED' ? '禁用' : '启用'}该会员吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await updateMemberStatus(row.id, next)
  ElMessage.success('已更新')
  load()
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.id)
}

async function batchToggle(status) {
  const label = status === 'ENABLED' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定批量${label}选中的 ${selectedIds.value.length} 个会员吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  const res = await batchUpdateMemberStatus(selectedIds.value, status)
  ElMessage.success(`已${label} ${res.data} 个会员`)
  load()
  tableRef.value?.clearSelection()
}

function openLevel(row) {
  levelForm.memberId = row.id
  levelForm.memberLevel = row.memberLevel
  levelVisible.value = true
}

async function submitLevel() {
  await updateMemberLevel(levelForm.memberId, levelForm.memberLevel)
  ElMessage.success('已更新')
  levelVisible.value = false
  load()
}

function openPoints(row) {
  pointsForm.memberId = row.id
  pointsForm.points = 0
  pointsForm.reason = ''
  pointsVisible.value = true
}

async function submitPoints() {
  if (!pointsForm.reason) return ElMessage.warning('请填写调整原因')
  await adjustMemberPoints(pointsForm.memberId, pointsForm.points, pointsForm.reason)
  ElMessage.success('已调整')
  pointsVisible.value = false
  load()
}

function openResetPassword(row) {
  pwdForm.memberId = row.id
  pwdForm.newPassword = ''
  pwdVisible.value = true
}

async function submitResetPassword() {
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,}$/.test(pwdForm.newPassword)) {
    return ElMessage.warning('密码需 8 位以上，含字母和数字')
  }
  await resetMemberPassword(pwdForm.memberId, pwdForm.newPassword)
  ElMessage.success('已重置')
  pwdVisible.value = false
}
</script>
