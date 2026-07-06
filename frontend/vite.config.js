import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/auth': 'http://localhost:8080',
      '/stu': 'http://localhost:8080',
      '/job-profile': 'http://localhost:8080',
      '/job-relation': 'http://localhost:8080',
      '/job-graph': 'http://localhost:8080',
    }
  }
})
