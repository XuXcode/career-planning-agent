import client from './client'

export const graphApi = {
  getFullGraph(maxDepth = 3) {
    return client.get('/job-graph/full', { params: { maxDepth } })
  },
  getGraphFromJob(jobName, maxDepth = 3) {
    return client.get(`/job-graph/from/${encodeURIComponent(jobName)}`, { params: { maxDepth } })
  }
}
