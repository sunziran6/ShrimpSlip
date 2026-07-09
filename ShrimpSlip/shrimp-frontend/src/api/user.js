import request from './request'

export function sendCode(phone) {
  return request.post('/user/send-code', { phone })
}

export function login(payload) {
  return request.post('/user/login', payload)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function refreshToken(refreshToken) {
  return request.post('/user/refresh', { refreshToken })
}

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}
