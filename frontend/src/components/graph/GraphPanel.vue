<template>
  <div>
    <div class="graph-toolbar">
      <button :class="{ active: mode === 'full' }" @click="loadFull">🌐 全量图谱</button>
      <button :class="{ active: mode === 'single' }" @click="showSearch = true" :disabled="!selectedJob">
        🔍 当前岗位路径
      </button>
      <div class="graph-search" v-if="showSearch">
        <input v-model="searchJob" placeholder="输入岗位名称..." @keyup.enter="loadSingle" />
        <button @click="loadSingle">查询</button>
        <button @click="showSearch = false">✕</button>
      </div>
      <span class="graph-info" v-if="graphData">
        {{ graphData.nodes?.length || 0 }} 节点 · {{ graphData.edges?.length || 0 }} 边
      </span>
    </div>

    <div ref="chartEl" class="graph-chart"></div>
    <div v-if="loading" class="graph-loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { graphApi } from '../../api/graph'

const emit = defineEmits(['selectJob'])
const chartEl = ref(null)
const mode = ref('full')
const loading = ref(false)
const showSearch = ref(false)
const searchJob = ref('')
const selectedJob = ref(null)
const graphData = ref(null)
let chart = null

onMounted(() => { loadFull() })

async function loadFull() {
  mode.value = 'full'
  loading.value = true
  try {
    const res = await graphApi.getFullGraph(3)
    graphData.value = res.data?.data || res.data
    await nextTick()
    renderChart()
  } finally { loading.value = false }
}

async function loadSingle() {
  const job = searchJob.value || selectedJob.value?.jobName
  if (!job) return
  mode.value = 'single'
  showSearch.value = false
  loading.value = true
  try {
    const res = await graphApi.getGraphFromJob(job, 3)
    graphData.value = res.data?.data || res.data
    selectedJob.value = { jobName: job, ...graphData.value?.nodes?.[0] }
    await nextTick()
    renderChart(job)
  } finally { loading.value = false }
}

function renderChart(highlightName) {
  if (!chartEl.value) return
  if (!chart) chart = echarts.init(chartEl.value)

  const g = graphData.value
  const nodes = (g.nodes || []).map(n => ({
    name: n.id,
    symbolSize: n.category === 'anchor' ? 50 : n.category === 'intermediate' ? 38 : 28,
    itemStyle: { color: n.id === highlightName ? '#4f8ef7' : 'rgba(79,142,247,.55)' },
    label: { show: true, fontSize: 12 }
  }))
  const edges = (g.edges || []).map(e => ({
    source: e.source,
    target: e.target,
    lineStyle: {
      color: e.type === 'PROMOTION' ? '#16a34a' : '#f59e0b',
      width: Math.max(1, (e.score || 70) / 40)
    }
  }))

  chart.setOption({
    tooltip: { formatter: p => p.dataType === 'node' ? `<b>${p.name}</b>` : `${p.data.source} → ${p.data.target}` },
    series: [{
      type: 'graph', layout: 'force', roam: true, draggable: true,
      force: { repulsion: 300, edgeLength: [120, 300] },
      data: nodes, edges: edges,
      emphasis: { focus: 'adjacency', lineStyle: { width: 4 } }
    }]
  })

  chart.off('click')
  chart.on('click', params => {
    if (params.dataType === 'node') {
      const name = params.name
      emit('selectJob', { jobName: name })
      selectedJob.value = { jobName: name }
    }
  })
}
</script>

<style scoped>
.graph-toolbar {
  display: flex; align-items: center; gap: 10px; margin-bottom: 16px; flex-wrap: wrap;
}
.graph-toolbar button {
  padding: 8px 16px; border-radius: var(--radius-sm); font-size: 13px; font-weight: 600;
  background: var(--card-solid); border: 1px solid var(--line); transition: all .2s;
}
.graph-toolbar button:hover { border-color: var(--primary); }
.graph-toolbar button.active { background: var(--primary); color: #fff; border-color: var(--primary); }
.graph-toolbar button:disabled { opacity: .4; cursor: default; }
.graph-search { display: flex; gap: 6px; }
.graph-search input {
  padding: 6px 12px; border: 1px solid var(--line); border-radius: var(--radius-sm);
  font-size: 13px; outline: none; width: 160px;
}
.graph-search input:focus { border-color: var(--primary); }
.graph-search button {
  padding: 6px 12px !important; font-size: 12px !important;
}
.graph-info { margin-left: auto; font-size: 13px; color: var(--muted); }
.graph-chart { width: 100%; height: 560px; border: 1px solid var(--line); border-radius: var(--radius-lg); background: var(--card-solid); }
.graph-loading { text-align: center; padding: 40px; color: var(--muted); }
</style>
