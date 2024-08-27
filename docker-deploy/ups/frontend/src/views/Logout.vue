<template>
    <div class="page-wrapper">
    <MenuLine/>
    <el-main>
        <br>
        <div class="accountContainer">
            <el-form >
                <h3 class="accountTitle" >Logout</h3>
                <p class="text-p">Hi {{username}},</p>
                <p class="text-p">Are you sure to logout?</p>
            </el-form>
            <div class="btn-middle">
            <el-button type="primary" color="rosybrown" @click="handleLogout" >Logout</el-button>
            </div>
        </div>
    </el-main>
    <PageFoot/>
    </div>
</template>

<script>
import { defineComponent } from 'vue'
import MenuLine from "@/components/Menu.vue";
import {ElMessage} from "element-plus";
import axios from "axios";
import jwt_decode from 'jwt-decode';
import PageFoot from "@/components/PageFoot.vue";

export default defineComponent({
    name: 'LogoutPage',
    components: {MenuLine,PageFoot},
    data(){
        return {
            username:''
        }
    },
    created(){
        const authToken = localStorage.getItem('authToken');
        if (authToken) {
            const decoded = jwt_decode(authToken);
            const userIdAuth = decoded.sub;
            this.username = userIdAuth
        }else{
            this.username = "  "
        }
    },
    methods: {
        async handleLogout() {
            try {
                //DONE:
                const response = await axios.post('/logout');
                console.log('response.data:', response.data)
                if (response.status===200) {
                    // 1. remove the authentic from the local authentic
                    localStorage.removeItem('authToken')
                    localStorage.removeItem('userInfo')
                    // 2. remove the saved username and other information
                    this.username = ''
                    // 3. show the main index page
                    ElMessage({
                        message: 'Good, logout successfully.',
                        type: 'success'
                    })
                    this.$router.push('/mylogin')
                }else{
                    ElMessage.error('Oops, logout failure! try again.')
                }

            } catch (error) {
                ElMessage.error('Oops, logout failure!! try again.')
            }
        }
    }
})
</script>

<style scoped>
.btn-middle{
    text-align: center;
}
.text-p{
    font-size: 18px;
}
</style>