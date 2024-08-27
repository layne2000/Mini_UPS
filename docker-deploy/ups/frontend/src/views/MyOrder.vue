<template>
    <div class="page-wrapper">
    <MenuLine/><br>
    <el-main>
        <OrderTable :table-data="tableData" />
    </el-main>
  <PageFoot/>
    </div>
</template>

<script>
import MenuLine from "@/components/Menu.vue"
import OrderTable from "@/components/OrderTable.vue"
import axios from 'axios'
import { ElMessage } from 'element-plus'
import jwt_decode from 'jwt-decode'
import PageFoot from "@/components/PageFoot.vue";
export default {
    name: "MyOrderPage",
    components:{MenuLine,OrderTable,PageFoot},
    data() {
        return {
            userId:'',
            tableData: [],
        }
    },
    created() {
        //DONE: get current user info and package info from local storage

        // load authentication from local storage
        const authToken = localStorage.getItem('authToken')
        // if no authentication, then turn to log in page
        if (!authToken) {
            ElMessage.error('Oops, please login.')
            this.$router.push('/mylogin')
        } else {
            try {
                const decoded = jwt_decode(authToken);
                const userIdAuth = decoded.sub;
                this.userId = userIdAuth;
                axios.get(`/myorder`).then(response => {
                    this.tableData = response.data;
                    console.log("data is ",this.tableData);
                }).catch(error => {
                    console.log(error);
                });
            } catch (error) {
                window.location.href ='/mylogin';
                ElMessage.error('Oops, something wrong, please login.');
            }
        }
    }
}

</script>

<style scoped>

</style>
