import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as userApi from '../api/user'

export const useUserStore = defineStore('user', () => {
  const userId = ref(null)
  const nickname = ref('')
  const avatar = ref('')

  async function doLogin(phone, code) {
    const res = await userApi.login(phone, code)
    localStorage.setItem('accessToken', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    userId.value = res.data.userId
    nickname.value = res.data.nickname
    avatar.value = res.data.avatar
  }

  function logout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    userId.value = null
    nickname.value = ''
    avatar.value = ''
  }

  return { userId, nickname, avatar, doLogin, logout }
})
