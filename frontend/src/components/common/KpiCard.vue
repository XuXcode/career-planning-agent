<template>
  <div class="kpi-card">
    <strong>{{ fmtValue }}</strong>
    <span>{{ label }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: String,
  value: [Number, String],
  suffix: { type: String, default: '' }
})

const fmtValue = computed(() => {
  const v = props.value
  if (v === null || v === undefined || v === '') return '—'
  if (typeof v === 'number' && props.suffix) {
    return Math.round(v) + props.suffix
  }
  return String(v) + props.suffix
})
</script>

<style scoped>
.kpi-card {
  flex: 1; min-width: 120px; background: var(--card-solid);
  border: 1px solid var(--line); border-radius: var(--radius-lg); padding: 16px;
  text-align: center;
}
.kpi-card strong { display: block; font-size: 26px; color: var(--primary); margin-bottom: 4px; }
.kpi-card span { font-size: 12px; color: var(--muted); }
</style>
