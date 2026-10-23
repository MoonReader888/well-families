import request from '../utils/request'

export function listPackages() {
  return request.get('/member/appointment/packages')
}

export function getPackageDetail(packageId) {
  return request.get(`/member/appointment/packages/${packageId}`)
}

export function listSlots(packageId, date) {
  return request.get(`/member/appointment/packages/${packageId}/slots`, { params: { date } })
}

export function bookAppointment(data) {
  return request.post('/member/appointment', data)
}

export function cancelAppointment(appointmentId) {
  return request.post(`/member/appointment/${appointmentId}/cancel`)
}

export function getMyAppointments() {
  return request.get('/member/appointment/my')
}

export function getAppointmentDetail(appointmentId) {
  return request.get(`/member/appointment/${appointmentId}`)
}
