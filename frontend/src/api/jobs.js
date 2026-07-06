import client from './client'

export const jobsApi = {
  getJobList() {
    return client.get('/job-profile')
  },
  getJobDetail(id) {
    return client.get(`/job-profile/${id}`)
  },
  getAllRelations() {
    return client.get('/job-relation')
  },
  getRelationsByJobName(jobName) {
    return client.get(`/job-relation/${encodeURIComponent(jobName)}`)
  }
}
