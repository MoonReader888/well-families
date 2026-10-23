import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 管理端开发配置：本地 5174，/api 与 /uploads 代理到后端 8080
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
