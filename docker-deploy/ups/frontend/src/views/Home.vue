<template>
    <div class="page-wrapper">
    <MenuLine/>
    <el-main>
        <div >
            <el-form inline class="el-form-home">
                <el-form-item label="Tracking Number" class="tracking-label">
                    <el-input v-model="shipId"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="search" color="rosybrown">Track</el-button>
                </el-form-item>
            </el-form>
            <br>
        </div>
        <div>
            <img src="@/assets/index.png" style="width: 100%">
        </div>
    </el-main>
    <PageFoot/>
    </div>
</template>

<script>
import MenuLine from "@/components/Menu.vue"
import jwt_decode from "jwt-decode";
import PageFoot from "@/components/PageFoot.vue";
// import axios from "axios";
    export default {
        name: "HomePage",
        components:{MenuLine,PageFoot},
        data() {
            return {
                shipId: '',
                userId: ''
            };
        },
        created(){
            const authToken = localStorage.getItem('authToken');
            if (authToken) {
                const decoded = jwt_decode(authToken);
                const userIdAuth = decoded.sub;
                this.userId = userIdAuth
            }else{
                this.userId = ""
            }
        },
        methods:{
          search(){
              // axios.post('/tracking', {orderNo:this.orderNo})
              this.$router.push({path:'/tracking',query:{shipId:this.shipId}});

          }
        }
    }
</script>

<style scoped>
.el-form-home{
    display: flex;
    justify-content: center;
}

</style>
<style>
.page-wrapper {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
}
.tracking-label {
    font-weight: bold;
    color: rosybrown;
}
</style>