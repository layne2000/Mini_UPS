<template>
    <div class="page-wrapper">
    <MenuLine/>
    <el-main>
        <div class="container">
            <div class="left-table">
                <h3 >Timeline</h3><br>
                <el-timeline>
                    <el-timeline-item v-if="showLabel1" :timestamp="order.createdTime">Label Created</el-timeline-item>
                    <el-timeline-item v-else ></el-timeline-item>
                    <el-timeline-item v-if="showLabel2" :timestamp="order.deliveringTime">Package started to deliver</el-timeline-item>
                    <el-timeline-item v-else ></el-timeline-item>
                    <el-timeline-item v-if="showLabel3" :timestamp="order.deliveredTime">Package started delivering</el-timeline-item>
                    <el-timeline-item v-else ></el-timeline-item>
                </el-timeline>
            </div>
            <div class="right-table">
                <h3 >Order Detail</h3><br>
                <el-descriptions :column="1" border>
                    <el-descriptions-item>
                        <template #label><div class="cell-item">shipId</div></template>
                        <span>{{ order.shipId }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item>
                        <template #label>userId</template>
                        <span>{{ order.userId }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item>
                        <template #label>truckId</template>
                        <span>{{ order.truckId }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item>
                        <template #label>shipmentStatus</template>
                        <span>{{ order.shipmentStatus }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item>
                        <template #label>commodity id</template>
                        <span>{{ order.id }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item>
                        <template #label>description</template>
                        <span>{{ order.description }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item v-if="edit_mode==='0'">
                        <template #label>Destination.x</template>
                        <el-input  v-model="order.x"  :placeholder="order.x" disabled></el-input>
                    </el-descriptions-item>
                    <el-descriptions-item v-if="edit_mode==='0'">
                        <template #label>Destination.y</template>
                        <el-input  v-model="order.y"  :placeholder="order.y" disabled></el-input>
                    </el-descriptions-item>
                    <el-descriptions-item v-if="edit_mode==='1'">
                        <template #label>Destination.x</template>
                        <el-input  v-model="order.x"  :placeholder="order.x"></el-input>
                    </el-descriptions-item>
                    <el-descriptions-item v-if="edit_mode==='1'">
                        <template #label>Destination.y</template>
                        <el-input  v-model="order.y"  :placeholder="order.y"></el-input>
                    </el-descriptions-item>
                </el-descriptions>
            </div>

        </div>
        <br><br><br>
        <el-form-item class="table-container">
            <el-button v-if="edit_mode==='1'" type="primary" color="rosybrown" @click="SaveHandle(order)">Save</el-button>
            <el-button v-if="edit_mode==='0'" type="primary" color="rosybrown" @click="EditHandle(order)">Edit</el-button>
        </el-form-item>
    </el-main>
    <PageFoot/>
    </div>
</template>

<script>
import MenuLine from "@/components/Menu.vue";
import jwt_decode from "jwt-decode";
import axios from "axios";
import {ElNotification} from "element-plus";
import PageFoot from "@/components/PageFoot.vue"
export default {
    name: "EditOrderDetail",
    components:{MenuLine,PageFoot},
    created() {
        const authToken = localStorage.getItem('authToken');
        if(authToken){
            const decoded = jwt_decode(authToken);
            const userIdAuth = decoded.sub;
            this.userId = userIdAuth;
        }
        this.getOrderDetail();
    },
    computed:{
        shipId(){
            return this.$route.query.shipId;
        },
        edit_mode(){
            return this.$route.query.edit_mode;
        },
        showLabel1(){
            return this.order.createdTime;
        },
        showLabel2(){
            return this.order.deliveringTime;
        },
        showLabel3(){
            return this.order.deliveredTime;
        },
    },
    data() {
        return {
            order: null,
            userId:''
        };
    },
    methods:{
        SaveHandle(order){
            //save data into database and redirect the page
            this.$router.push({name:'OrderDetail',query:{shipId:order.shipId,edit_mode:'0'}}).catch(() => {})
            ElNotification({
                title: 'Success',
                message: 'You have successfully changed the destination',
                type: 'success',
                position: 'top-right',
                duration: 5000,
                iconClass: 'el-icon-success',
                showClose: true,
                customClass: 'my-notification'
            });
            ElNotification({
                title:'Error',
                message: 'Destination change failure, try latter',
                type: 'error',
                position: 'top-right',
                duration: 5000,
                iconClass: 'el-icon-error',
                showClose: true,
                customClass: 'my-notification'
            });
            // window.location.reload()
        },
        EditHandle(order){
            this.$router.push({name:'EditOrderDetail',query:{shipId:order.shipId,edit_mode:'1'}}).catch(() => {})
            // window.location.reload()
        },
        getOrderDetail(){
            axios.post('/editorderdetail', {shipId: this.shipId})
                .then(response => {
                    this.order = response.data;
                    console.log(response.data);
                })
                .catch(error => {
                    console.error(error);
                });
        }
    }
}
</script>

<style scoped>
.table-container {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    height: 100%;
}

.container {
    display: flex;
    justify-content: center;
    align-items: center;
}

.left-table {
    margin-right: 80px;
}

.right-table {
    margin-left: 80px;
}

.margin-top {
    margin-top: 50px;
    width: 25%;
    margin: 0 auto;
}
.cell-item {
    display: flex;
    align-items: center;
}
</style>
