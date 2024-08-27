<template>
    <div class="page-wrapper">
    <MenuLine/>
    <el-main>
        <div class="accountContainer">
            <h3 class="accountTitle">Change Password</h3>
            <el-form :model="form" :rules="rules" ref="form" label-position="top" >
                <el-form-item label="Current Password" prop="currentPassword">
                    <el-input type="password" v-model.trim="form.currentPassword" autocomplete="off" show-password></el-input>
                </el-form-item>
                <el-form-item label="New Password" prop="newPassword">
                    <el-input type="password" v-model.trim="form.newPassword" autocomplete="off" show-password></el-input>
                </el-form-item>
            </el-form>
            <div class="btn-midlle">
                <el-button type="primary" color="rosybrown" @click="submitPwd">Submit</el-button>
                <el-button @click="resetPwd">Reset</el-button>
                <el-button @click="goBack">Back</el-button>
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
import axios from "axios"
import CryptoJS from "crypto-js";
import PageFoot from "@/components/PageFoot.vue"
export default defineComponent({
    name:"ChangePwd",
    components:{MenuLine,PageFoot},
    data(){
        return {
            form: {
                currentPassword: '',
                newPassword: '',
            },
            rules:{
                currentPassword:[
                    {required: true, message: 'Username is required', trigger: 'blur'},
                ],
                newPassword:[
                    {required: true, message: 'Username is required', trigger: 'blur'},
                    {min: 6, max: 20, message: 'Password length should be between 6 and 20', trigger: 'blur'}
                ],
            }
        }
    },
    methods:{
        async submitPwd(){
            this.$refs.form.validate(async (valid) => {
                if (valid) {
                    const formData = {
                        oldPassword: CryptoJS.SHA256(this.form.currentPassword).toString(),
                        newPassword:CryptoJS.SHA256(this.form.newPassword).toString()
                    }
                    try{
                        //DONE: connect to backend and change the password
                        const response = await axios.post('/changepwd',formData)
                        console.log('response.data:', response.data)
                        if (response.status === 200) {
                            ElMessage({
                                message: 'Good, password changed successfully.',
                                type: 'success'
                            })
                            this.$router.push('/profile')
                        } else {
                            ElMessage.error(response.data)
                        }
                    }catch (error) {
                        ElMessage.error('Oops! password change failure, please try again.')
                    }
                } else {
                    ElMessage.error('Oops!! password change failure, please try again.')
                }
            })
        },
        resetPwd() {
            this.$refs.form.resetFields();
        },
        async goBack(){
            await this.$router.push({path:'/profile'});
        }
    }
})
</script>

<style scoped>
btn-midlle{
    text-align: center;
}
</style>