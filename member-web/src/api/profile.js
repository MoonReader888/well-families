import request from '../utils/request'

export function getProfile() {
  return request.get('/member/profile')
}

export function updateProfile(data) {
  return request.put('/member/profile', data)
}

export function changePassword(data) {
  return request.put('/member/profile/password', data)
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  // 不手动设置 Content-Type：浏览器会自动带上 multipart boundary，手动设置反而导致后端解析失败
  return request.post('/member/profile/upload/avatar', formData)
}
