<template>
  <article :class="['job-card', { active }]" @click="$emit('click')">
    <div class="job-card-top">
      <h3>{{ job.jobName }}</h3>
      <span class="job-id">#{{ job.id }}</span>
    </div>
    <p class="job-snippet">{{ active ? '当前已选中' : '点击查看完整岗位画像与关联图谱' }}</p>
    <div class="job-card-footer">
      <span class="tag gray">岗位画像</span>
      <span :class="['tag', active ? 'green' : 'gray']">{{ active ? '已选择' : '点击展开' }}</span>
    </div>
  </article>
</template>

<script setup>
defineProps({
  job: { type: Object, required: true },
  active: { type: Boolean, default: false }
})
defineEmits(['click'])
</script>

<style scoped>
.job-card {
  background: var(--card-solid); border: 1px solid var(--line);
  border-radius: var(--radius-lg); padding: 16px; cursor: pointer; transition: all .2s;
}
.job-card:hover { box-shadow: var(--shadow-soft); transform: translateY(-1px); }
.job-card.active { border-color: var(--primary); background: var(--primary-soft); }
.job-card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.job-card-top h3 { font-size: 15px; }
.job-id { font-size: 12px; color: var(--muted); }
.job-snippet { font-size: 13px; color: var(--muted); margin-bottom: 12px; }
.job-card-footer { display: flex; gap: 8px; }
.tag {
  display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.tag.gray { background: #f1f5f9; color: #475569; }
.tag.green { background: #dcfce7; color: #166534; }
</style>
