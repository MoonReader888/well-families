import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 会员端开发配置：本地 5173，/api 代理到后端 8080
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
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
