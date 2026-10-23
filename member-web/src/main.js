import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import './styles/global.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Vant)

// 长屏适配：以 667px 屏高为基准，长屏按高度等比放大 UI，上限 1.2 倍；短屏不缩小。
// 通过 --s-scale 交给全局 CSS 使用（CSS calc 不能算「长度÷长度」的比例，故用 JS 计算）
function applyScreenScale() {
  const grow = Math.min(0.2, Math.max(0, window.innerHeight - 667) / 1450)
  document.documentElement.style.setProperty('--s-scale', (1 + grow).toFixed(4))
}
applyScreenScale()
window.addEventListener('resize', applyScreenScale)

app.mount('#app')
