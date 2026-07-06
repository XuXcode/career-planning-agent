<template>
  <div class="section-card">
    <h3>👤 学生能力画像</h3>
    <p class="assessment">{{ profile.overallAssessment || '暂无综合评价' }}</p>
    <div class="skill-lists">
      <div v-for="g in groups" :key="g.label" class="skill-group">
        <h4>{{ g.label }}</h4>
        <div class="chips">
          <span v-for="(s, i) in g.items" :key="i" :class="['chip', g.color]">{{ s }}</span>
          <span v-if="!g.items.length" class="chip gray">暂无</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ profile: Object })

function split(val) {
  if (!val) return []
  if (Array.isArray(val)) return val
  if (typeof val === 'string') return val.split(/[；;,，]/).filter(Boolean)
  return []
}

const groups = computed(() => [
  { label: '专业技能', items: split(props.profile.professionalSkills), color: 'blue' },
  { label: '证书', items: split(props.profile.certificates), color: 'orange' },
  { label: '创新能力', items: split(props.profile.innovationAbilities), color: 'purple' },
  { label: '学习能力', items: split(props.profile.learningAbilities), color: 'green' },
  { label: '抗压能力', items: split(props.profile.stressResistance), color: 'red' },
  { label: '沟通能力', items: split(props.profile.communicationSkills), color: 'teal' },
  { label: '实习能力', items: split(props.profile.internshipAbilities), color: 'indigo' }
])
</script>

<style scoped>
.section-card {
  background: var(--card-solid); border: 1px solid var(--line);
  border-radius: var(--radius-lg); padding: 20px; margin-bottom: 16px;
}
.section-card h3 { font-size: 16px; margin-bottom: 10px; }
.assessment { font-size: 14px; color: var(--muted); line-height: 1.7; margin-bottom: 16px; }
.skill-lists { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; }
.skill-group h4 { font-size: 13px; color: var(--muted); margin-bottom: 6px; }
.chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
  display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.chip.blue { background: #eff6ff; color: #1d4ed8; }
.chip.orange { background: #fff7ed; color: #c2410c; }
.chip.purple { background: #faf5ff; color: #7c3aed; }
.chip.green { background: #f0fdf4; color: #166534; }
.chip.red { background: #fef2f2; color: #dc2626; }
.chip.teal { background: #f0fdfa; color: #0f766e; }
.chip.indigo { background: #eef2ff; color: #4338ca; }
.chip.gray { background: #f1f5f9; color: #94a3b8; }
</style>
