<template>
  <div>
    <div class="job-header">
      <input v-model="search" type="text" placeholder="搜索岗位名称..." class="search-input" />
      <span class="job-count">共 {{ filteredJobs.length }} 个岗位</span>
    </div>
    <div class="job-grid" v-if="filteredJobs.length">
      <JobCard
        v-for="job in pagedJobs" :key="job.id"
        :job="job" :active="activeId === job.id"
        @click="$emit('selectJob', job)"
      />
    </div>
    <div v-else class="empty">未找到匹配的岗位</div>
    <div class="pagination" v-if="totalPages > 1">
      <button :disabled="page <= 1" @click="page--">‹</button>
      <span>{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="page++">›</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { jobsApi } from '../../api/jobs'
import JobCard from './JobCard.vue'

const emit = defineEmits(['selectJob'])
const jobs = ref([])
const search = ref('')
const page = ref(1)
const pageSize = 12
const activeId = ref(null)

const filteredJobs = computed(() => {
  const s = search.value.toLowerCase()
  return s ? jobs.value.filter(j => (j.jobName || '').toLowerCase().includes(s)) : jobs.value
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredJobs.value.length / pageSize)))

const pagedJobs = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredJobs.value.slice(start, start + pageSize)
})

onMounted(async () => {
  try {
    const res = await jobsApi.getJobList()
    jobs.value = res.data?.data || res.data || []
  } catch (e) {
    console.error('加载岗位列表失败', e)
  }
})
</script>

<style scoped>
.job-header {
  display: flex; align-items: center; gap: 16px; margin-bottom: 20px;
}
.search-input {
  flex: 1; max-width: 360px; padding: 10px 16px;
  border: 1px solid var(--line); border-radius: var(--radius-sm);
  font-size: 14px; outline: none; transition: border .2s;
}
.search-input:focus { border-color: var(--primary); }
.job-count { color: var(--muted); font-size: 13px; }
.job-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 14px;
}
.empty { text-align: center; padding: 60px 20px; color: var(--muted); }
.pagination {
  display: flex; align-items: center; justify-content: center; gap: 12px; margin-top: 20px;
}
.pagination button {
  width: 36px; height: 36px; border-radius: var(--radius-sm); background: var(--card-solid);
  border: 1px solid var(--line); font-size: 16px; color: var(--text); transition: all .2s;
}
.pagination button:disabled { opacity: .3; cursor: default; }
.pagination button:hover:not(:disabled) { border-color: var(--primary); }
.pagination span { font-size: 14px; color: var(--muted); }
</style>
