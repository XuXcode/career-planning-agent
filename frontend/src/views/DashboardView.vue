<template>
  <div class="dashboard">
    <AppTopbar />
    <div class="dash-tabs">
      <button
        v-for="t in tabs" :key="t.key"
        :class="['dash-tab', { active: activeTab === t.key }]"
        @click="activeTab = t.key"
      >{{ t.label }}</button>
    </div>
    <div class="dash-content">
      <JobList v-if="activeTab === 'jobs'" @select-job="openJobDetail" />
      <AnalysisPanel v-else-if="activeTab === 'analysis'" />
      <GraphPanel v-else-if="activeTab === 'graph'" @select-job="openJobDetail" />
    </div>
    <JobDetailDrawer
      v-if="selectedJob"
      :job="selectedJob"
      @close="selectedJob = null"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppTopbar from '../components/layout/AppTopbar.vue'
import JobList from '../components/jobs/JobList.vue'
import JobDetailDrawer from '../components/jobs/JobDetailDrawer.vue'
import AnalysisPanel from '../components/analysis/AnalysisPanel.vue'
import GraphPanel from '../components/graph/GraphPanel.vue'

const activeTab = ref('jobs')
const selectedJob = ref(null)

const tabs = [
  { key: 'jobs', label: '岗位浏览' },
  { key: 'analysis', label: '智能分析' },
  { key: 'graph', label: '职业图谱' }
]

function openJobDetail(job) {
  selectedJob.value = job
}
</script>

<style scoped>
.dashboard { min-height: 100vh; }
.dash-tabs {
  display: flex; gap: 0;
  background: var(--card-solid); border-bottom: 1px solid var(--line);
  padding: 0 24px; position: sticky; top: 60px; z-index: 10;
}
.dash-tab {
  padding: 14px 24px; font-size: 14px; font-weight: 600; color: var(--muted);
  background: none; border-bottom: 2px solid transparent; transition: all .2s;
}
.dash-tab:hover { color: var(--text); }
.dash-tab.active { color: var(--primary); border-bottom-color: var(--primary); }
.dash-content { padding: 20px 24px 40px; }
</style>
