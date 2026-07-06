<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <div class="auth-logo">AI</div>
        <h1>职业规划智能体</h1>
        <p>加入我们，开启你的智能职业规划之旅</p>
      </div>
      <form @submit.prevent="handleRegister" class="auth-form">
        <h2>注册</h2>
        <div class="field">
          <label>用户名</label>
          <input v-model="username" type="text" placeholder="至少3个字符" required minlength="3" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="至少6个字符" required minlength="6" />
        </div>
        <div class="field">
          <label>确认密码</label>
          <input v-model="confirmPassword" type="password" placeholder="再次输入密码" required />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <p v-if="success" class="success-msg">{{ success }}</p>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
        <p class="switch-link">已有账号？<router-link to="/login">立即登录</router-link></p>
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
const confirmPassword = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''
  success.value = ''
  if (password.value !== confirmPassword.value) { error.value = '两次输入的密码不一致'; return }
  loading.value = true
  try {
    await auth.register(username.value, password.value, confirmPassword.value)
    success.value = '注册成功！正在跳转登录...'
    setTimeout(() => router.push('/login'), 1000)
  } catch (e) {
    error.value = e.response?.data?.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px;
}
.auth-card {
  display: flex; background: var(--card-solid); border-radius: var(--radius-xl);
  box-shadow: var(--shadow); overflow: hidden; max-width: 800px; width: 100%;
}
.auth-brand {
  flex: 1; background: linear-gradient(135deg, #0d1322, #1a2744); color: #fff;
  padding: 48px 36px; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center;
}
.auth-logo {
  width: 64px; height: 64px; border-radius: 18px;
  background: linear-gradient(135deg, #4f8ef7, #8b5cf6);
  display: grid; place-items: center; font-size: 24px; font-weight: 800; margin-bottom: 16px;
}
.auth-brand h1 { font-size: 22px; margin-bottom: 8px; }
.auth-brand p { color: rgba(255,255,255,.6); font-size: 13px; }
.auth-form {
  flex: 1; padding: 48px 36px; display: flex; flex-direction: column; justify-content: center;
}
.auth-form h2 { font-size: 20px; margin-bottom: 20px; }
.field { margin-bottom: 14px; }
.field label { display: block; font-size: 13px; color: var(--muted); margin-bottom: 6px; }
.field input {
  width: 100%; padding: 10px 14px; border: 1px solid var(--line);
  border-radius: var(--radius-sm); font-size: 14px; outline: none; transition: border .2s;
}
.field input:focus { border-color: var(--primary); }
.error-msg { color: var(--danger); font-size: 13px; margin-bottom: 12px; }
.success-msg { color: var(--success); font-size: 13px; margin-bottom: 12px; }
.btn-primary {
  width: 100%; padding: 11px; background: linear-gradient(135deg, var(--primary), var(--primary-2));
  color: #fff; border-radius: var(--radius-sm); font-size: 15px; font-weight: 600; margin-top: 6px; transition: opacity .2s;
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
