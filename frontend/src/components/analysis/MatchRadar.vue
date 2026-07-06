<template>
  <div class="radar-row">
    <div class="section-card" style="flex:1;min-width:350px;">
      <h3>📊 学生能力雷达图</h3>
      <div ref="studentRadar" style="height:340px;"></div>
    </div>
    <div class="section-card" style="flex:1;min-width:350px;" v-if="hasDimensions">
      <h3>🎯 四维匹配雷达图</h3>
      <div ref="matchRadar" style="height:340px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ report: Object })
const studentRadar = ref(null)
const matchRadar = ref(null)
let chart1 = null, chart2 = null

const hasDimensions = computed(() => {
  return props.report?.jobMatchAnalysis?.matchedJobs?.[0]?.dimensions != null
})

function metricScore(val) {
  if (!val) return 20
  const arr = Array.isArray(val) ? val : String(val).split(/[；;,，]/).filter(Boolean)
  return Math.min(100, Math.max(20, arr.length * 18))
}

function buildStudentChart() {
  if (!studentRadar.value) return
  if (!chart1) chart1 = echarts.init(studentRadar.value)
  const p = props.report?.studentProfile || {}
  const indicators = [
    { name: '专业技能', max: 100 }, { name: '证书', max: 100 },
    { name: '创新能力', max: 100 }, { name: '学习能力', max: 100 },
    { name: '抗压能力', max: 100 }, { name: '沟通能力', max: 100 },
    { name: '实习能力', max: 100 }
  ]
  const values = [
    metricScore(p.professionalSkills), metricScore(p.certificates),
    metricScore(p.innovationAbilities), metricScore(p.learningAbilities),
    metricScore(p.stressResistance), metricScore(p.communicationSkills),
    metricScore(p.internshipAbilities)
  ]
  chart1.setOption({
    radar: { indicator: indicators, center: ['50%', '52%'], radius: '65%' },
    series: [{
      type: 'radar',
      data: [{ value: values, name: '学生画像', areaStyle: { color: 'rgba(79,142,247,0.15)' } }],
      itemStyle: { color: '#4f8ef7' }
    }]
  })
}

function buildMatchChart() {
  if (!matchRadar.value || !hasDimensions.value) return
  if (!chart2) chart2 = echarts.init(matchRadar.value)
  const dim = props.report.jobMatchAnalysis.matchedJobs[0].dimensions
  const indicators = [
    { name: '基础要求', max: 100 }, { name: '职业技能', max: 100 },
    { name: '职业素养', max: 100 }, { name: '发展潜力', max: 100 }
  ]
  const values = [dim.baseScore || 0, dim.skillScore || 0, dim.qualityScore || 0, dim.potentialScore || 0]
  chart2.setOption({
    radar: { indicator: indicators, center: ['50%', '52%'], radius: '65%' },
    series: [{
      type: 'radar',
      data: [{ value: values, name: '匹配度', areaStyle: { color: 'rgba(139,92,246,0.15)' } }],
      itemStyle: { color: '#8b5cf6' }
    }]
  })
}

watch(() => props.report, () => { buildStudentChart(); buildMatchChart() }, { deep: true })
onMounted(() => { buildStudentChart(); buildMatchChart() })
</script>

<style scoped>
.radar-row { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 16px; }
.section-card {
  background: var(--card-solid); border: 1px solid var(--line);
  border-radius: var(--radius-lg); padding: 20px;
}
.section-card h3 { font-size: 16px; margin-bottom: 8px; }
</style>
