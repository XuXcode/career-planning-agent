<template>
  <div class="stream-progress">
    <div class="phases">
      <div
        v-for="(p, i) in phases" :key="i"
        :class="['phase-dot', { done: i < currentIndex, active: i === currentIndex }]"
      >
        <span class="dot"></span>
        <span class="phase-label">{{ p }}</span>
      </div>
    </div>
    <p class="phase-current">{{ phase }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ phase: { type: String, default: '' } })
const phases = ['提取能力画像', '岗位匹配分析', '规划职业路径', '生成行动计划']

const currentIndex = computed(() => {
  const p = props.phase || ''
  if (p.includes('能力画像')) return 0
  if (p.includes('岗位匹配') || p.includes('岗位')) return 1
  if (p.includes('职业路径') || p.includes('路径')) return 2
  if (p.includes('行动计划') || p.includes('行动')) return 3
  return 0
})
</script>

<style scoped>
.stream-progress {
  background: var(--card-solid); border: 1px solid var(--line);
  border-radius: var(--radius-lg); padding: 20px 24px; margin-bottom: 20px;
}
.phases { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.phase-dot { display: flex; flex-direction: column; align-items: center; gap: 6px; flex: 1; }
.dot {
  width: 14px; height: 14px; border-radius: 50%; background: #e2e8f0; transition: all .3s;
}
.phase-dot.done .dot { background: var(--success); }
.phase-dot.active .dot {
  background: var(--primary);
  animation: pulse 1s ease-in-out infinite;
}
.phase-label { font-size: 11px; color: var(--muted); text-align: center; white-space: nowrap; }
.phase-dot.done .phase-label { color: var(--success); }
.phase-dot.active .phase-label { color: var(--primary); font-weight: 600; }
.phase-current { text-align: center; margin-top: 12px; font-size: 14px; color: var(--primary); font-weight: 600; }
@keyframes pulse {
  0%,100% { box-shadow: 0 0 0 0 rgba(79,142,247,.4); }
  50% { box-shadow: 0 0 0 10px rgba(79,142,247,0); }
}
</style>
