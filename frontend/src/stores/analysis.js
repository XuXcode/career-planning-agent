import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAnalysisStore = defineStore('analysis', () => {
  const phase = ref('')
  const profile = ref(null)
  const matches = ref(null)
  const path = ref(null)
  const plan = ref(null)
  const isStreaming = ref(false)
  const error = ref(null)
  const report = ref(null)

  const fullReport = computed(() => {
    if (!profile.value) return null
    return {
      studentProfile: profile.value,
      jobMatchAnalysis: matches.value || { matchedJobs: [], overallMatchScore: 0, gapAnalysis: '' },
      careerPathPlan: path.value || {},
      actionPlan: plan.value || {}
    }
  })

  function startStream(token) {
    isStreaming.value = true
    error.value = null
    phase.value = ''
    profile.value = null
    matches.value = null
    path.value = null
    plan.value = null

    const es = new EventSource(`/stu/analysis/stream?token=${encodeURIComponent(token)}`)

    es.addEventListener('phase', e => { phase.value = e.data })
    es.addEventListener('profile', e => {
      try { profile.value = JSON.parse(e.data) } catch {}
    })
    es.addEventListener('matches', e => {
      try { matches.value = JSON.parse(e.data) } catch {}
    })
    es.addEventListener('path', e => {
      try { path.value = JSON.parse(e.data) } catch {}
    })
    es.addEventListener('plan', e => {
      try { plan.value = JSON.parse(e.data) } catch {}
    })
    es.addEventListener('complete', e => {
      try { report.value = JSON.parse(e.data) } catch { report.value = fullReport.value }
      isStreaming.value = false
      es.close()
    })
    es.addEventListener('error', e => {
      if (e.data) {
        try { error.value = JSON.parse(e.data) } catch { error.value = e.data }
      } else {
        error.value = '连接中断，请重试'
      }
      isStreaming.value = false
      es.close()
    })
    es.onerror = () => {
      if (isStreaming.value) {
        error.value = 'SSE 连接异常'
        isStreaming.value = false
      }
      es.close()
    }
    return es
  }

  function setReport(r) {
    report.value = r
    profile.value = r?.studentProfile || null
    matches.value = r?.jobMatchAnalysis || null
    path.value = r?.careerPathPlan || null
    plan.value = r?.actionPlan || null
  }

  function reset() {
    phase.value = ''
    profile.value = null
    matches.value = null
    path.value = null
    plan.value = null
    report.value = null
    isStreaming.value = false
    error.value = null
  }

  return { phase, profile, matches, path, plan, report, fullReport, isStreaming, error, startStream, setReport, reset }
})
