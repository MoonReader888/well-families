import request from '../utils/request'

export function createSession(sessionName) {
  return request.post('/member/chat/session', null, { params: { sessionName } })
}

export function listSessions() {
  return request.get('/member/chat/sessions')
}

export function getMessages(sessionId) {
  return request.get(`/member/chat/session/${sessionId}/messages`)
}

export function deleteSession(sessionId) {
  return request.delete(`/member/chat/session/${sessionId}`)
}

/** 非流式发送（兜底） */
export function sendMessage(sessionId, message) {
  return request.post(`/member/chat/${sessionId}/message`, { message })
}
