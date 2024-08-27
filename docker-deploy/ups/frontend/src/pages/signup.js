//init
import { createApp } from 'vue';
import App from '../App.vue';
import router from '@/router/index'
import axios from 'axios'
// import element_plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

createApp(App).mount('#app')

const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(ElementPlus)
app.use(router)
app.mount('#app')
