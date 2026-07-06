<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <div class="auth-logo">AI</div>
        <h1>职业规划智能体</h1>
        <p>基于大模型的智能职业规划与岗位匹配平台</p>
      </div>
      <form @submit.prevent="handleLogin" class="auth-form">
        <h2>登录</h2>
        <div class="field">
          <label>用户名</label>
          <input v-model="username" type="text" placeholder="请输入用户名" required />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" required />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
        <p class="switch-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.auth-card {
  display: flex;
  background: var(--card-solid);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow);
  overflow: hidden;
  max-width: 800px;
  width: 100%;
}
.auth-brand {
  flex: 1;
  background: linear-gradient(135deg, #0d1322, #1a2744);
  color: #fff;
  padding: 48px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}
.auth-logo {
  width: 64px; height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #4f8ef7, #8b5cf6);
  display: grid; place-items: center;
  font-size: 24px; font-weight: 800;
  margin-bottom: 16px;
}
.auth-brand h1 { font-size: 22px; margin-bottom: 8px; }
.auth-brand p { color: rgba(255,255,255,.6); font-size: 13px; }
.auth-form {
  flex: 1;
  padding: 48px 36px;
  display: flex; flex-direction: column; justify-content: center;
}
.auth-form h2 { font-size: 20px; margin-bottom: 20px; }
.field { margin-bottom: 14px; }
.field label { display: block; font-size: 13px; color: var(--muted); margin-bottom: 6px; }
.field input {
  width: 100%; padding: 10px 14px;
  border: 1px solid var(--line); border-radius: var(--radius-sm);
  font-size: 14px; outline: none; transition: border .2s;
}
.field input:focus { border-color: var(--primary); }
.error-msg { color: var(--danger); font-size: 13px; margin-bottom: 12px; }
.btn-primary {
  width: 100%; padding: 11px;
  background: linear-gradient(135deg, var(--primary), var(--primary-2));
  color: #fff; border-radius: var(--radius-sm);
  font-size: 15px; font-weight: 600; margin-top: 6px; transition: opacity .2s;
}
.btn-primary:disabled { opacity: .6; }
.btn-primary:hover:not(:disabled) { opacity: .9; }
.switch-link { margin-top: 16px; text-align: center; font-size: 13px; color: var(--muted); }
.switch-link a { color: var(--primary); text-decoration: none; }
@media (max-width: 640px) {
  .auth-card { flex-direction: column; }
  .auth-brand { padding: 32px 24px; }
}
</style>
