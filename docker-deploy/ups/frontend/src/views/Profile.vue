<template>
    <div class="page-wrapper">
<MenuLine/>
  <el-main>
      <div class="accountContainer">
          <h3 class="accountTitle">My Profile</h3>
          <el-form :model="this" :rules="rules" label-width="80px" class="form" ref="form" >
              <el-form-item label="UserId" prop="username">
                  <el-input v-model="userId" :disabled="true" />
              </el-form-item>
              <el-form-item label="Email" prop="newEmail">
                  <el-input v-model="newEmail" :placeholder="email" :disabled="emailDisable" />
              </el-form-item>
          </el-form>
          <div class="btn-middle">
              <el-button type="primary" v-if="emailDisable" color="rosybrown" @click="changePassword">Change Password</el-button>
              <el-button color="rosybrown" v-if="emailDisable" @click="changeEmail">Change Email</el-button>
              <el-button color="rosybrown" v-if="!emailDisable" @click="saveEmail">Save Email</el-button>
              <el-button color="rosybrown" v-if="!emailDisable" @click="goBack">Back</el-button>
          </div>
      </div>
  </el-main>
<PageFoot/>
    </div>
</template>

<script>
import MenuLine from "@/components/Menu.vue";
import { defineComponent } from 'vue'
import {ElMessage, ElNotification} from "element-plus";
import axios from 'axios'
import jwt_decode from 'jwt-decode'
import PageFoot from "@/components/PageFoot.vue";
export default defineComponent({
    name: "ProfilePage",
    components:{MenuLine,PageFoot},
    data() {
        return {
            userId: '',
            email: '',
            emailDisable:true,
            newEmail:'',
            rules:{
                newEmail:[
                    {required: true, message: 'Email is required', trigger: 'blur'},
                    {type: 'email', message: 'Invalid email format', trigger: 'blur'}
                ]
            }
        };
    },

    methods: {
        async changePassword() {
            await this.$router.push({path:'/changepwd'});
        },
        async changeEmail(){
            this.emailDisable=!this.emailDisable;
        },
        async saveEmail(){
            await this.$refs.form.validate(async (valid) => {
                console.log(valid);
                console.log("new email:"+this.newEmail);
                console.log("old email:"+this.email);
                if (valid && this.email!==this.newEmail){
                    this.emailDisable=!this.emailDisable;
                    // submit to database and reload information
                    axios.post('/profile', {newEmail: this.newEmail})
                        .then(response => {
                            this.newEmail = response.data;
                            this.email = this.newEmail
                            //success -> update new data
                            ElNotification({
                                title: 'Change Success',
                                message: 'Email change success, Good',
                                type: 'success',
                                position: 'top-right',
                                duration: 5000,
                                iconClass: 'el-icon-success',
                                showClose: true,
                                customClass: 'my-notification'
                            });
                        })
                        .catch(error => {
                            // failure -> change email back
                            ElNotification({
                                title: 'Edit Failure',
                                message: 'Email change failure, try latter',
                                type: 'error',
                                position: 'top-right',
                                duration: 5000,
                                iconClass: 'el-icon-error',
                                showClose: true,
                                customClass: 'my-notification'
                            });
                            console.error(error);
                        });
                    }else {
                            ElMessage.error('Oops, use a new and correct format email.')
                    }
            })
        },
        async goBack(){
            this.emailDisable=!this.emailDisable;
            this.newEmail=this.email;
        }
    },
    async created(){
        // load authentication from local storage
        const authToken = localStorage.getItem('authToken')
        // if no authentication, then turn to log in page
        if (!authToken) {
            ElMessage.error('Oops, please login.')
            await this.$router.push('/mylogin')
        }else{
            try{
                const decoded = jwt_decode(authToken);
                const userIdAuth = decoded.sub;
                this.userId = userIdAuth
                // const response = await axios.get('/profile/${userIdAuth}', { headers: { Authorization: `Bearer ${authToken}` } })
                axios.get(`/profile`)
                    .then(response => {
                        this.email = response.data
                        this.newEmail=this.email
                        console.log(response.data);
                    })
                    .catch(error => {
                        console.error(error);
                    });

            }catch (error){
                ElMessage.error('Oops, something wrong, please login.')
                await this.$router.push('/mylogin')
            }
        }
    }
})
</script>

<style scoped>
.btn-middle{
    text-align: center;
}
</style>