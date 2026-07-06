<template>
  <header class="topbar">
    <div class="topbar-brand">
      <span class="topbar-logo">AI</span>
      <div>
        <strong>职业规划智能体</strong>
        <small>基于大模型的智能职业规划平台</small>
      </div>
    </div>
    <div class="topbar-actions">
      <span class="topbar-user">{{ auth.username }}</span>
      <button class="btn-outline" @click="handleLogout">退出</button>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.topbar {
  height: 60px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; background: rgba(255,255,255,.74); backdrop-filter: blur(18px);
  border-bottom: 1px solid var(--line); position: sticky; top: 0; z-index: 20;
}
.topbar-brand {
  display: flex; align-items: center; gap: 12px;
}
.topbar-logo {
  width: 38px; height: 38px; border-radius: 12px;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  display: grid; place-items: center; color: #fff; font-weight: 800; font-size: 15px;
}
.topbar-brand strong { font-size: 16px; display: block; line-height: 1.2; }
.topbar-brand small { font-size: 11px; color: var(--muted); }
.topbar-actions { display: flex; align-items: center; gap: 12px; }
.topbar-user { font-size: 14px; color: var(--muted); }
.btn-outline {
  padding: 6px 16px; border-radius: var(--radius-sm); font-size: 13px;
  border: 1px solid var(--line); background: var(--card-solid); color: var(--muted);
  transition: all .2s;
}
.btn-outline:hover { border-color: var(--danger); color: var(--danger); }
</style>
