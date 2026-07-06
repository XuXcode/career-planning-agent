import client from './client'

export const exportApi = {
  async exportPDF() {
    return client.get('/stu/export/report/pdf', { responseType: 'blob' })
  },
  async exportWord() {
    return client.get('/stu/export/report/word', { responseType: 'blob' })
  }
}

export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
