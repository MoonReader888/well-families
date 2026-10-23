import request from '../utils/request'

export function register(data) {
  return request.post('/auth/register', data)
}

export function login(data) {
  return request.post('/auth/login', data)
}

export function logout(refreshToken) {
  return request.post('/auth/logout', { refreshToken })
}

export function resetPassword(data) {
  return request.post('/auth/password/reset', data)
}
