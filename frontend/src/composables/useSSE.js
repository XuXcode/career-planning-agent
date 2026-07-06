import { ref } from 'vue'

export function useSSE() {
  const phase = ref('')
  const profile = ref(null)
  const matches = ref(null)
  const pathData = ref(null)
  const plan = ref(null)
  const report = ref(null)
  const isStreaming = ref(false)
  const error = ref(null)
  let eventSource = null

  function start(token) {
    isStreaming.value = true
    error.value = null
    phase.value = ''
    profile.value = null
    matches.value = null
    pathData.value = null
    plan.value = null
    report.value = null

    eventSource = new EventSource(`/stu/analysis/stream?token=${encodeURIComponent(token)}`)

    const on = (name, setter) => {
      eventSource.addEventListener(name, e => {
        try { setter(JSON.parse(e.data)) } catch { setter(e.data) }
      })
    }

    on('phase', v => { phase.value = v })
    on('profile', v => { profile.value = v })
    on('matches', v => { matches.value = v })
    on('path', v => { pathData.value = v })
    on('plan', v => { plan.value = v })

    eventSource.addEventListener('complete', e => {
      try { report.value = JSON.parse(e.data) } catch { report.value = { studentProfile: profile.value, jobMatchAnalysis: matches.value, careerPathPlan: pathData.value, actionPlan: plan.value } }
      isStreaming.value = false
      eventSource.close()
    })

    eventSource.addEventListener('error', e => {
      error.value = typeof e.data === 'string' ? e.data : '连接中断'
      isStreaming.value = false
      eventSource.close()
    })

    eventSource.onerror = () => {
      if (isStreaming.value) { error.value = 'SSE 连接异常'; isStreaming.value = false }
      eventSource.close()
    }

    return eventSource
  }

  function stop() {
    if (eventSource) { eventSource.close(); eventSource = null }
    isStreaming.value = false
  }

  return { phase, profile, matches, pathData, plan, report, isStreaming, error, start, stop }
}
