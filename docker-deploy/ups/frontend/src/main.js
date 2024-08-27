// init
import { createApp } from 'vue'
import App from './App.vue'
import axios from 'axios'
import router from '@/router/index'
// import element_plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// axios.defaults.baseURL = 'http://localhost:8080/'
const app = createApp(App)

app.config.globalProperties.$axios = axios

app.use(ElementPlus)
app.use(router)

app.mount('#app')