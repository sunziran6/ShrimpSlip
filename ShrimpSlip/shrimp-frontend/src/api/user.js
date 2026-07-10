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

export function changePassword(data) {
  return request.post('/user/change-password', data)
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// Address
export function getAddresses() {
  return request.get('/user/addresses')
}

export function createAddress(data) {
  return request.post('/user/addresses', data)
}

export function updateAddress(id, data) {
  return request.put(`/user/addresses/${id}`, data)
}

export function deleteAddress(id) {
  return request.delete(`/user/addresses/${id}`)
}
