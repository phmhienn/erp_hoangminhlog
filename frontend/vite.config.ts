import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Ứng dụng web demo (mục 1.3): frontend gọi backend qua proxy /api -> Spring Boot :8080
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_BACKEND_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
      // Ảnh minh chứng/sự cố do backend phục vụ; thiếu dòng này dev server trả về index.html
      // nên ảnh không hiển thị và bấm vào ảnh lại rơi vào route không tồn tại của SPA.
      '/uploads': {
        target: process.env.VITE_BACKEND_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
})
