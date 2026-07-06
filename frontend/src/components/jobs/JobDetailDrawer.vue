<template>
  <Teleport to="body">
    <div class="drawer-overlay" @click.self="$emit('close')">
      <div class="drawer">
        <button class="drawer-close" @click="$emit('close')">✕</button>

        <div class="drawer-hero">
          <h2>{{ job.jobName }}</h2>
          <p>{{ job.description || '暂无岗位描述' }}</p>
        </div>

        <div class="drawer-metrics">
          <div class="metric"><strong>岗位评分</strong><span :class="['tag', scoreColor]">{{ job.score || '—' }}</span></div>
          <div class="metric"><strong>岗位 ID</strong><span>{{ job.id }}</span></div>
          <div class="metric"><strong>创建时间</strong><span>{{ job.createTime || '—' }}</span></div>
          <div class="metric"><strong>更新时间</strong><span>{{ job.updateTime || '—' }}</span></div>
        </div>

        <!-- v2.0: 四维权重展示 -->
        <div class="drawer-section">
          <h4>四维匹配权重</h4>
          <div class="weight-grid">
            <div class="weight-item">
              <div class="weight-bar" :style="{ width: wBasePct + '%' }"></div>
              <span>基础要求</span><strong>{{ wBasePct }}%</strong>
            </div>
            <div class="weight-item">
              <div class="weight-bar" :style="{ width: wSkillPct + '%' }"></div>
              <span>职业技能</span><strong>{{ wSkillPct }}%</strong>
            </div>
            <div class="weight-item">
              <div class="weight-bar" :style="{ width: wQualityPct + '%' }"></div>
              <span>职业素养</span><strong>{{ wQualityPct }}%</strong>
            </div>
            <div class="weight-item">
              <div class="weight-bar" :style="{ width: wPotentialPct + '%' }"></div>
              <span>发展潜力</span><strong>{{ wPotentialPct }}%</strong>
            </div>
          </div>
        </div>

        <div class="drawer-section">
          <h4>能力维度</h4>
          <div class="dim-list">
            <div v-for="d in dimensions" :key="d.label" class="dim-item">
              <div class="dim-row"><span>{{ d.label }}</span><small>{{ d.pct }}%</small></div>
              <div class="dim-bar"><div :style="{ width: d.pct + '%' }"></div></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  job: { type: Object, required: true }
})
defineEmits(['close'])

const wBasePct = computed(() => Math.round((props.job.wBase || 0.25) * 100))
const wSkillPct = computed(() => Math.round((props.job.wSkill || 0.25) * 100))
const wQualityPct = computed(() => Math.round((props.job.wQuality || 0.25) * 100))
const wPotentialPct = computed(() => Math.round((props.job.wPotential || 0.25) * 100))

const scoreColor = computed(() => {
  const s = Number(props.job.score)
  return s >= 400 ? 'green' : s >= 200 ? 'orange' : 'red'
})

const dimensions = computed(() => {
  const fields = [
    ['专业技能', 'skills'],
    ['证书要求', 'cert'],
    ['创新能力', 'innovation'],
    ['学习能力', 'learning'],
    ['抗压能力', 'pressure'],
    ['沟通能力', 'communication'],
    ['实习能力', 'practical']
  ]
  return fields.map(([label, key]) => ({
    label,
    pct: tokenPct(props.job[key])
  }))
})

function tokenPct(val) {
  if (!val || typeof val !== 'string') return 0
  const tokens = val.split(/[；;]/).filter(Boolean)
  return Math.min(95, Math.max(15, tokens.length * 18))
}
</script>

<style scoped>
.drawer-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.3); z-index: 50;
  display: flex; justify-content: flex-end;
}
.drawer {
  width: 520px; max-width: 100vw; background: var(--card-solid);
  overflow-y: auto; padding: 28px 24px 40px; position: relative;
}
.drawer-close {
  position: absolute; top: 16px; right: 16px;
  width: 36px; height: 36px; border-radius: 50%; background: #f1f5f9;
  font-size: 16px; color: var(--text); display: grid; place-items: center; transition: .2s;
}
.drawer-close:hover { background: #e2e8f0; }
.drawer-hero { margin-bottom: 20px; }
.drawer-hero h2 { font-size: 22px; margin-bottom: 8px; }
.drawer-hero p { font-size: 14px; color: var(--muted); line-height: 1.7; }
.drawer-metrics {
  display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 20px;
}
.metric { background: #f8fafc; padding: 12px; border-radius: var(--radius-sm); }
.metric strong { display: block; font-size: 12px; color: var(--muted); margin-bottom: 4px; }
.metric span { font-size: 14px; }
.drawer-section { margin-bottom: 20px; }
.drawer-section h4 { font-size: 15px; margin-bottom: 12px; }
.weight-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.weight-item { background: #f8fafc; padding: 10px; border-radius: var(--radius-sm); }
.weight-bar { height: 4px; background: var(--primary); border-radius: 2px; margin-bottom: 6px; min-width: 4px; }
.weight-item span { font-size: 12px; color: var(--muted); display: block; }
.weight-item strong { font-size: 14px; }
.dim-list { display: flex; flex-direction: column; gap: 10px; }
.dim-item { }
.dim-row { display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 13px; }
.dim-row small { color: var(--muted); }
.dim-bar { height: 6px; background: #f1f5f9; border-radius: 3px; overflow: hidden; }
.dim-bar div { height: 100%; background: linear-gradient(90deg, var(--primary), var(--accent)); border-radius: 3px; transition: width .4s; }
.tag { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.tag.green { background: #dcfce7; color: #166534; }
.tag.orange { background: #fff7ed; color: #c2410c; }
.tag.red { background: #fef2f2; color: #dc2626; }
</style>
