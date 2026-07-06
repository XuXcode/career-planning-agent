import client from './client'

export const authApi = {
  login(username, password) {
    return client.post('/auth/login', { username, password })
  },
  register(username, password, confirmPassword) {
    return client.post('/auth/register', { username, password, confirmPassword })
  },
  checkUsername(username) {
    return client.get('/auth/check-username', { params: { username } })
  },
  getUserInfo() {
    return client.get('/auth/info')
  },
  logout() {
    return client.post('/auth/logout')
  }
}
