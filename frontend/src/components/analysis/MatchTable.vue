<template>
  <div class="section-card">
    <h3>📋 四维人岗匹配评分</h3>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>岗位名称</th>
            <th>基础要求</th>
            <th>职业技能</th>
            <th>职业素养</th>
            <th>发展潜力</th>
            <th class="col-total">加权总分</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(job, i) in jobs" :key="i">
            <td><strong>{{ job.jobName }}</strong></td>
            <td><span :class="scoreClass(job.dimensions?.baseScore)">{{ fmt(job.dimensions?.baseScore) }}</span></td>
            <td><span :class="scoreClass(job.dimensions?.skillScore)">{{ fmt(job.dimensions?.skillScore) }}</span></td>
            <td><span :class="scoreClass(job.dimensions?.qualityScore)">{{ fmt(job.dimensions?.qualityScore) }}</span></td>
            <td><span :class="scoreClass(job.dimensions?.potentialScore)">{{ fmt(job.dimensions?.potentialScore) }}</span></td>
            <td class="col-total"><strong :class="scoreClass(job.weightedTotalScore || job.matchScore)">
              {{ fmt(job.weightedTotalScore || job.matchScore) }}
            </strong></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
defineProps({ jobs: Array })

function fmt(v) { return v != null ? Math.round(v) : '—' }

function scoreClass(v) {
  if (v == null) return 'score-na'
  const n = Number(v)
  return n >= 80 ? 'score-high' : n >= 60 ? 'score-mid' : 'score-low'
}
</script>

<style scoped>
.section-card {
  background: var(--card-solid); border: 1px solid var(--line);
  border-radius: var(--radius-lg); padding: 20px; margin-bottom: 16px;
}
.section-card h3 { font-size: 16px; margin-bottom: 12px; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 14px; }
th, td { padding: 10px 12px; text-align: center; border-bottom: 1px solid var(--line); }
th { font-size: 12px; color: var(--muted); background: #f8fafc; white-space: nowrap; }
.col-total { background: #f8fafc; }
.score-high { color: var(--success); font-weight: 700; }
.score-mid { color: var(--warning); font-weight: 600; }
.score-low { color: var(--danger); }
.score-na { color: var(--muted); }
</style>
