<template>
  <div class="section-card">
    <h3>🛤️ 职业路径规划</h3>
    <div class="path-info">
      <div class="path-item"><strong>职业目标</strong><span>{{ path.careerGoal || '—' }}</span></div>
      <div class="path-item"><strong>发展路径</strong><span>{{ path.careerPath || '—' }}</span></div>
      <div class="path-item"><strong>行业趋势</strong><span>{{ path.industryTrend || '—' }}</span></div>
    </div>
    <div class="path-chips" v-if="path.keyMilestones?.length">
      <h4>关键里程碑</h4>
      <span v-for="(m, i) in splitArr(path.keyMilestones)" :key="i" class="chip blue">{{ m }}</span>
    </div>
    <div class="path-chips" v-if="path.requiredSkills?.length">
      <h4>需要培养的能力</h4>
      <span v-for="(s, i) in splitArr(path.requiredSkills)" :key="i" class="chip purple">{{ s }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({ path: Object })
function splitArr(v) { return Array.isArray(v) ? v : [] }
</script>

<style scoped>
.section-card {
  background: var(--card-solid); border: 1px solid var(--line);
  border-radius: var(--radius-lg); padding: 20px; margin-bottom: 16px;
}
.section-card h3 { font-size: 16px; margin-bottom: 12px; }
.path-info { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 10px; margin-bottom: 14px; }
.path-item { background: #f8fafc; padding: 10px; border-radius: var(--radius-sm); }
.path-item strong { display: block; font-size: 11px; color: var(--muted); margin-bottom: 4px; }
.path-item span { font-size: 14px; line-height: 1.5; }
.path-chips { margin-top: 12px; }
.path-chips h4 { font-size: 13px; color: var(--muted); margin-bottom: 6px; }
.chip { display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; margin: 2px 4px 2px 0; }
.chip.blue { background: #eff6ff; color: #1d4ed8; }
.chip.purple { background: #faf5ff; color: #7c3aed; }
</style>
