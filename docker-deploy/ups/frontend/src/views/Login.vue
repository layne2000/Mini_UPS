<template>
    <div class="page-wrapper">
    <MenuLine/>
    <el-main>
        <div class="accountContainer">
            <h3 class="accountTitle">Login</h3>
            <el-form ref="form" :model="form" :rules="rules" label-width="80px" class="form">
                <el-form-item label="UserId" prop="username">
                    <el-input v-model="form.username" placeholder=""></el-input>
                </el-form-item>
                <el-form-item label="Password" prop="password">
                    <el-input type="password" v-model="form.password" placeholder=""></el-input>
                </el-form-item>
            </el-form>
            <div class="btn-middle">
                <el-button color="rosybrown" type="primary" @click="handleLogin">Login</el-button>
            </div>
            <div class="transfer-link">
                <p style="flex: 1; color:rosybrown">If you have no account, please
                    <el-link href="/signup" @click.prevent="redirectToSignup" class="my-link">signup</el-link>.</p>
            </div>
        </div>
    </el-main>
    <PageFoot/>
    </div>
</template>

<script>
import MenuLine from "@/components/Menu.vue"
import { defineComponent } from 'vue'
import { ElMessage } from 'element-plus'
import axios from "axios";
import CryptoJS from "crypto-js";
import PageFoot from "@/components/PageFoot.vue";

export default defineComponent({
    name: "LoginPage",
    components:{MenuLine,PageFoot},
    data() {
        return{
            form:{
                username:'',
                password:''
            },
            rules: {
                username: [
                    {required: true, message: 'Username is required', trigger: 'blur'},
                ],
                password: [
                    {required: true, message: 'Password is required', trigger: 'blur'},
                ],
            }
        }
    },
    methods:{
        async handleLogin(){
            await this.$refs.form.validate(async (valid) => {
                if (valid){
                    const formData = {
                        userId: this.form.username,
                        password: CryptoJS.SHA256(this.form.password).toString()
                    }
                    try{
                        //Done: submit the form to database
                        const response = await axios.post('/mylogin', formData)
                        console.log('response.data:', response.data)
                        if (response.status === 200) {
                            ElMessage({
                                message: 'Good, login successfully.',
                                type: 'success'
                            });
                            const token = response.data.token
                            localStorage.setItem('authToken', token)
                            const userInfo = JSON.stringify(response.data.userInfo);
                            localStorage.setItem('userInfo',userInfo)
                            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
                            await this.$router.push('/')
                        }
                    }catch (error){
                        ElMessage.error('Oops!! login failure, try again.')
                    }
                }else{
                    ElMessage.error('Please input correct format of userId and password.')
                }
            })
        },
        redirectToSignup(){
            window.location.href ='/signup';
        }
    }
})
</script>

<style scoped>
.transfer-link{
    font-size: 14px;
    text-align: center;
    display: flex;
}
.btn-middle{
    text-align: center;
}
.my-link{
    font-weight: bold;
    color:rosybrown;
}
.my-link:hover {
    color:brown;
}
</style>