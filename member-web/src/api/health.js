import request from '../utils/request'

export function createHealthRecord(data) {
  return request.post('/member/health', data)
}

export function getHealthHistory() {
  return request.get('/member/health')
}

export function getHealthDetail(id) {
  return request.get(`/member/health/${id}`)
}

export function getHealthTrend() {
  return request.get('/member/health/trend')
}
