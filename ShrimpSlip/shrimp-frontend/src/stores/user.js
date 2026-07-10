import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as userApi from '../api/user'

export const useUserStore = defineStore('user', () => {
  const userId = ref(null)
  const nickname = ref('')
  const avatar = ref('')
  const role = ref(localStorage.getItem('role') || '')

  async function doLogin(phone, code) {
    const res = await userApi.login(phone, code)
    localStorage.setItem('accessToken', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    localStorage.setItem('role', res.data.role)
    userId.value = res.data.userId
    nickname.value = res.data.nickname
    avatar.value = res.data.avatar
    role.value = res.data.role
  }

  function logout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('role')
    userId.value = null
    nickname.value = ''
    avatar.value = ''
    role.value = ''
  }

  return { userId, nickname, avatar, role, doLogin, logout }
})
