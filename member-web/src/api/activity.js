import request from '../utils/request'

export function listActivities() {
  return request.get('/member/activity')
}

export function getActivityDetail(activityId) {
  return request.get(`/member/activity/${activityId}`)
}

export function registerActivity(activityId) {
  return request.post(`/member/activity/${activityId}/register`)
}

export function cancelRegisterActivity(activityId) {
  return request.delete(`/member/activity/${activityId}/register`)
}

export function getMyActivities() {
  return request.get('/member/activity/my')
}

export function checkInStatus(activityId) {
  return request.get(`/member/activity/${activityId}/checkin`)
}

export function checkInActivity(activityId) {
  return request.post(`/member/activity/${activityId}/checkin`)
}
