<template>
  <div class="page-wrapper">
  <MenuLine/>
    <el-main>
        <div class="accountContainer">
            <h3 class="accountTitle">Signup</h3>
            <el-form ref="form" :model="form" :rules="rules" label-width="80px" class="form">
                <el-form-item label="UserId" prop="username">
                    <el-input v-model="form.username" placeholder=""></el-input>
                </el-form-item>
                <el-form-item label="Email" prop="email">
                    <el-input type="email" v-model="form.email" placeholder=""></el-input>
                </el-form-item>
                <el-form-item label="Password" prop="password">
                    <el-input type="password" v-model="form.password" placeholder=""></el-input>
                </el-form-item>
            </el-form>
            <div class="btn-middle">
                <el-button color="rosybrown" type="primary" @click="handleSubmit">Signup</el-button>
            </div>
            <div class="transfer-link">
                <p style="flex: 1; color:rosybrown">If you already have account, please
                    <el-link href="/mylogin" @click.prevent="redirectToLogin" class="my-link">login</el-link>.</p>
            </div>
        </div>
  </el-main>
  <PageFoot/>
  </div>
</template>

<script>
import MenuLine from '../components/Menu.vue'
import { defineComponent } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import CryptoJS from 'crypto-js'
import PageFoot from "@/components/PageFoot.vue";

export default defineComponent({
    name: "SignupPage",
    components:{MenuLine,PageFoot},
    data() {
        return {
            form: {
                email: '',
                password: '',
                username: ''
            },
            rules: {
                username: [
                    {required: true, message: 'Username is required', trigger: 'blur'},
                    {min: 3, max: 20, message: 'Username length should be between 3 and 20', trigger: 'blur'}
                ],
                password: [
                    {required: true, message: 'Password is required', trigger: 'blur'},
                    {min: 6, max: 20, message: 'Password length should be between 6 and 20', trigger: 'blur'}
                ],
                email: [
                    {required: true, message: 'Email is required', trigger: 'blur'},
                    {type: 'email', message: 'Invalid email format', trigger: ['blur', 'change']}
                ]
            }
        }
    },
    methods: {
         async handleSubmit(){
            await this.$refs.form.validate(async (valid) => {
                if (valid){
                    const formData = {
                        userId: this.form.username,
                        email:this.form.email,
                        password: CryptoJS.SHA256(this.form.password).toString()
                    }
                    try {
                        //Done: submit the form to backend, encode the password;
                        const response = await axios.post('/signup', formData)
                        console.log('response.data:', response.data)
                        ElMessage({
                            message: 'Good, account created successfully.',
                            type: 'success'
                        })
                        // decode and get the userId instead
                        const token = response.data.token;
                        const userInfo = JSON.stringify(response.data.userInfo);
                        localStorage.setItem('authToken', token)
                        localStorage.setItem('userInfo',userInfo)
                        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
                        // await this.$router.push('/logout')
                        window.location.href ='/';
                    } catch (error) {
                        console.error(error)
                        ElMessage.error('Oops, something wrong. Try another userId.')
                    }

                } else {
                    ElMessage.error('Oops, please create account with correct format!')
                }
            })
        },
        redirectToLogin(){
            window.location.href ='/mylogin';
        }
    }
})
</script>

<style>
.accountContainer{
    border-radius: 15px;
    background-clip: padding-box;
    margin: 180px auto;
    width:350px;
    padding: 15px 25px 15px 35px;
    background: #fff;
    border: 1px solid #eaeaea;
    box-shadow: 0 0 25px #cac6c6;
}
.accountTitle{
    margin: 0 auto 40px auto;
    text-align: center;
}
</style>

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