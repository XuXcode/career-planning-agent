import client from './client'

export const analysisApi = {
  uploadResume(formData) {
    return client.post('/stu/uploadResume', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  uploadText(text) {
    return client.post('/stu/uploadText', text, {
      headers: { 'Content-Type': 'text/plain' }
    })
  },
  generateReport() {
    return client.post('/stu/analysis')
  }
}
