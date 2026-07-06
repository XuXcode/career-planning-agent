import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '')
  const userId = computed(() => user.value?.userId || null)

  async function login(username, password) {
    const res = await authApi.login(username, password)
    const data = res.data?.data || res.data
    token.value = data.token
    user.value = { username: data.username, userId: data.userId, role: data.role }
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  async function register(username, password, confirmPassword) {
    await authApi.register(username, password, confirmPassword)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    authApi.logout().catch(() => {})
  }

  return { token, user, isLoggedIn, username, userId, login, register, logout }
})
