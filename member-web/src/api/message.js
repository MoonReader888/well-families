import request from '../utils/request'

export function getMessages() {
  return request.get('/member/message')
}

export function getAnnouncements() {
  return request.get('/member/message/announcements')
}

export function getMessageDetail(messageId) {
  return request.get(`/member/message/${messageId}`)
}

export function markMessageRead(messageId) {
  return request.put(`/member/message/${messageId}/read`)
}

export function getUnreadCount() {
  return request.get('/member/message/unread-count')
}
