import { createRouter, createWebHistory } from 'vue-router'
//  import component
import LoginPage from '@/views/Login.vue'
import LogoutPage  from '@/views/Logout.vue'
import TrackPage  from '@/views/Track.vue'
import SignupPage  from '@/views/Signup.vue'
import HomePage from '@/views/Home.vue'
import ProfilePage from '@/views/Profile.vue'
import MyOrderPage from '@/views/MyOrder.vue'
import ChangePwd from '@/views/ChangePwd.vue'
import OrderDetail from "@/views/OrderDetail.vue"
import EditOrderDetail from "@/views/EditOrderDetail.vue"
import axios from "axios";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'HomePage',
            component: HomePage,
            meta: {
                requiresAuth: false
            }
        },
        {
            path: '/mylogin',
            name: 'LoginPage',
            component: LoginPage,
            meta: {
                requiresAuth: false
            }
        },
        {
            path: '/logout',
            name: 'Logout',
            component: LogoutPage,
            meta: {
                requiresAuth: false
            }
        },
        {
            path: '/changepwd',
            name: 'ChangePwd',
            component: ChangePwd,
            meta: {
                requireAuth: true
            }
        },
        {
            path: '/tracking',
            name: 'Track',
            component: TrackPage,
            meta: {
                requiresAuth: false
            }
        },
        {
            path: '/signup',
            name: 'Signup',
            component: SignupPage,
            meta: {
                requiresAuth: false
            }
        },
        {
            path: '/profile',
            name: 'Profile',
            component: ProfilePage,
            meta: {
                requireAuth: true
            }

        },
        {
            path: '/myorder',
            name: 'myOrder',
            component: MyOrderPage,
            meta: {
                requireAuth: true
            }
        },
        {
            path: '/orderdetail',
            name: 'OrderDetail',
            component: OrderDetail,
            meta: {
                requiresAuth: false
            }
        },
        {
            path: '/editorderdetail',
            name: 'EditOrderDetail',
            component: EditOrderDetail,
            meta: {
                requiresAuth: false
            }
        }
    ]
})
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('authToken');
    if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
        delete axios.defaults.headers.common['Authorization'];
    }
    next();
});


export default router;