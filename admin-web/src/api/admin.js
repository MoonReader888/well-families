import request from '../utils/request'
import { useUserStore } from '../store/user'

// 仪表盘
export function getDashboard() {
  return request.get('/admin/dashboard')
}

// 会员管理
export function getMembers(params) {
  return request.get('/admin/members', { params })
}
export function getMemberDetail(id) {
  return request.get(`/admin/members/${id}`)
}
export function updateMemberStatus(id, status) {
  return request.put(`/admin/members/${id}/status`, { status })
}
export function batchUpdateMemberStatus(ids, status) {
  return request.put('/admin/members/status/batch', { ids, status })
}
export function updateMemberLevel(id, memberLevel) {
  return request.put(`/admin/members/${id}/level`, { memberLevel })
}
export function adjustMemberPoints(id, points, reason) {
  return request.put(`/admin/members/${id}/points`, { points, reason })
}
export function resetMemberPassword(id, newPassword) {
  return request.put(`/admin/members/${id}/password/reset`, { newPassword })
}

// 体检套餐
export function getPackages(params) {
  return request.get('/admin/appointment/packages', { params })
}
export function createPackage(data) {
  return request.post('/admin/appointment/packages', data)
}
export function updatePackage(id, data) {
  return request.put(`/admin/appointment/packages/${id}`, data)
}
export function deletePackage(id) {
  return request.delete(`/admin/appointment/packages/${id}`)
}

// 时段批量生成
export function batchGenerateSlots(packageId, data) {
  return request.post(`/admin/appointment/packages/${packageId}/slots/batch`, data)
}

// 时段管理
export function getPackageSlots(packageId) {
  return request.get(`/admin/appointment/packages/${packageId}/slots`)
}
export function updateSlot(slotId, data) {
  return request.put(`/admin/appointment/packages/slots/${slotId}`, data)
}
export function deleteSlot(slotId) {
  return request.delete(`/admin/appointment/packages/slots/${slotId}`)
}

// 预约管理
export function getAppointments(params) {
  return request.get('/admin/appointment/appointments', { params })
}
export function updateAppointmentStatus(id, status) {
  return request.put(`/admin/appointment/appointments/${id}/status`, { status })
}
export function batchUpdateAppointmentStatus(ids, status) {
  return request.put('/admin/appointment/appointments/status/batch', { ids, status })
}
export function uploadReport(id, file) {
  const form = new FormData()
  form.append('file', file)
  return request.post(`/admin/appointment/appointments/${id}/report`, form)
}

// 报告下载：axios 拦截器按 JSON 解包，blob 需走原生 fetch 并手动带 token
export async function downloadReport(id) {
  const store = useUserStore()
  const resp = await fetch(`/api/admin/appointment/appointments/${id}/report`, {
    headers: { Authorization: `Bearer ${store.accessToken}` }
  })
  if (!resp.ok) {
    let message = '下载失败'
    try {
      const err = await resp.json()
      message = err.message || message
    } catch {
      /* 非 JSON 响应，忽略 */
    }
    throw new Error(message)
  }
  return resp.blob()
}

// 评测管理
export function getQuestionnaires() {
  return request.get('/admin/assessment/questionnaires')
}
export function createQuestionnaire(data) {
  return request.post('/admin/assessment/questionnaires', data)
}
export function updateQuestionnaire(id, data) {
  return request.put(`/admin/assessment/questionnaires/${id}`, data)
}
export function updateQuestionnaireStatus(id, status) {
  return request.put(`/admin/assessment/questionnaires/${id}/status`, null, { params: { status } })
}
export function deleteQuestionnaire(id) {
  return request.delete(`/admin/assessment/questionnaires/${id}`)
}
export function getQuestions(questionnaireId) {
  return request.get(`/admin/assessment/questionnaires/${questionnaireId}/questions`)
}
export function createQuestion(questionnaireId, data) {
  return request.post(`/admin/assessment/questionnaires/${questionnaireId}/questions`, data)
}
export function updateQuestion(id, data) {
  return request.put(`/admin/assessment/questions/${id}`, data)
}
export function deleteQuestion(id) {
  return request.delete(`/admin/assessment/questions/${id}`)
}

// 活动管理
export function getActivities(params) {
  return request.get('/admin/activity', { params })
}
export function createActivity(data) {
  return request.post('/admin/activity', data)
}
export function updateActivity(id, data) {
  return request.put(`/admin/activity/${id}`, data)
}
export function deleteActivity(id) {
  return request.delete(`/admin/activity/${id}`)
}
export function getActivityRegistrations(id, params) {
  return request.get(`/admin/activity/${id}/registrations`, { params })
}
export function uploadActivityCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/activity/upload/cover', formData)
}

// 消息管理
export function getMessages(params) {
  return request.get('/admin/message', { params })
}
export function deleteMessage(id) {
  return request.delete(`/admin/message/${id}`)
}
export function pushMessage(data) {
  return request.post('/admin/message/push', data)
}
export function pushBatchMessage(data) {
  return request.post('/admin/message/push/batch', data)
}
export function getPushCount(params) {
  return request.get('/admin/message/push/count', { params })
}

// 系统配置
export function getConfigs() {
  return request.get('/admin/config')
}
export function updateConfig(data) {
  return request.put('/admin/config', data)
}
