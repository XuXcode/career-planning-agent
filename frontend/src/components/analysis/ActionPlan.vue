<template>
  <div class="section-card">
    <h3>📅 行动计划</h3>
    <div class="stages" v-if="plan.stages?.length">
      <div v-for="(s, i) in plan.stages" :key="i" class="stage-card">
        <strong>{{ s.stageName }}</strong>
        <p>{{ s.description }}</p>
        <div class="chips">
          <span v-for="(a, j) in splitArr(s.actions)" :key="j" class="chip green">{{ a }}</span>
        </div>
      </div>
    </div>
    <div class="plan-meta">
      <span><strong>评估指标：</strong>{{ plan.evaluationMetrics || '—' }}</span>
      <span><strong>评估周期：</strong>{{ plan.evaluationCycle || '—' }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({ plan: Object })
function splitArr(v) { return Array.isArray(v) ? v : [] }
</script>

<style scoped>
.section-card {
  background: var(--card-solid); border: 1px solid var(--line);
  border-radius: var(--radius-lg); padding: 20px; margin-bottom: 16px;
}
.section-card h3 { font-size: 16px; margin-bottom: 12px; }
.stages { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; margin-bottom: 14px; }
.stage-card { background: #f8fafc; padding: 14px; border-radius: var(--radius-sm); }
.stage-card strong { font-size: 15px; display: block; margin-bottom: 6px; }
.stage-card p { font-size: 13px; color: var(--muted); line-height: 1.6; margin-bottom: 8px; }
.chips { display: flex; flex-wrap: wrap; gap: 4px; }
.chip { display: inline-block; padding: 3px 8px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.chip.green { background: #f0fdf4; color: #166534; }
.plan-meta { display: flex; gap: 24px; font-size: 13px; color: var(--muted); flex-wrap: wrap; }
.plan-meta strong { color: var(--text); }
</style>
