import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import 'dayjs/locale/zh-cn'
import { createPinia } from 'pinia'

const app = createApp(App)

// 全局配置
app.config.globalProperties.$api = '/api'
app.config.globalProperties.$baseUrl = ''

app.use(store)
app.use(router)
app.use(ElementPlus)

app.mount('#app')