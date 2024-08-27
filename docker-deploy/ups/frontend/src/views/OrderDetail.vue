<template>
    <div class="page-wrapper">
    <MenuLine/>
    <el-main>
        <div class="container">
            <div class="left-table">
                <h3 >Timeline</h3><br>
                <el-timeline>
                    <el-timeline-item v-if="showLabel1" color='#0bbd87' :timestamp="dayjs(order.createdTime).format('YYYY-MM-DD HH:mm:ss')">Label Created</el-timeline-item>
                    <el-timeline-item v-else ></el-timeline-item>
                    <el-timeline-item v-if="showLabel2" color='#0bbd87' :timestamp="dayjs(order.deliveringTime).format('YYYY-MM-DD HH:mm:ss')">Package started to deliver</el-timeline-item>
                    <el-timeline-item v-else ></el-timeline-item>
                    <el-timeline-item v-if="showLabel3" color='#0bbd87' :timestamp="dayjs(order.deliveredTime).format('YYYY-MM-DD HH:mm:ss')">Package delivered</el-timeline-item>
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
                    <el-descriptions-item v-if="ifTruck">
                        <template #label>Truck</template>
                        <el-button @click="viewTruckDetail()">View Truck Detail</el-button>
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
                    <el-descriptions-item>
                        <template #label>Destination.x</template>
                        <el-input  v-model="newX"  :placeholder="order.x" :disabled="!isDisabled"></el-input>
                    </el-descriptions-item>
                    <el-descriptions-item>
                        <template #label>Destination.y</template>
                        <el-input  v-model="newY"  :placeholder="order.y" :disabled="!isDisabled"></el-input>
                    </el-descriptions-item>
                </el-descriptions>
            </div>
        </div>
        <div>
        <br><br><br>
        <el-form-item class="table-container">
            <el-button v-if="edit_mode==='1'" color="rosybrown" type="primary" @click="SaveHandle(order)">Save Order</el-button>
            <el-button v-if="edit_mode==='0'" color="rosybrown" :disabled="isDisabled||!ifOwner" type="primary" @click="EditHandle(order)">Edit</el-button>
            <el-button v-if="edit_mode==='0'" type="danger" :disabled="!ifOwner" @click="CancelHandle()">Cancel Order</el-button>
        </el-form-item>
        </div>
        <div>
            <el-dialog v-model="dialogTableVisible" title="Truck Address" :width="240" :height="300" :center="true">
                <el-table :data="gridData" style="text-align:center">
                    <el-table-column property="x" label="truckX" width="80" />
                    <el-table-column property="y" label="truckY" width="80" />
                </el-table>
            </el-dialog>
        </div>
    </el-main>
    <PageFoot/>
    </div>
</template>

<script>
import MenuLine from "@/components/Menu.vue"
import jwt_decode from "jwt-decode";
import axios from "axios";
import {ElNotification} from 'element-plus';
import PageFoot from "@/components/PageFoot.vue";
import dayjs from 'dayjs';
export default {
    name: "OrderDetail",
    components:{MenuLine,PageFoot},
    created() {
        const authToken = localStorage.getItem('authToken');
        if(authToken){
            const decoded = jwt_decode(authToken);
            const userIdAuth = decoded.sub;
            this.userId = userIdAuth;
            this.auth = 1;
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
            if (this.order.createdTime===null){
                return false;
            }else{
                return true;
            }
        },
        showLabel2(){
            return this.order.deliveringTime;
        },
        showLabel3(){
            return this.order.deliveredTime;
        },
        isDisabled(){
            if (this.$route.query.edit_mode==="1"){
                return true;
            }else{
                return false;
            }
        },
        ifOwner(){
            if (this.userId===''){
                return false;
            }
            if (this.order.userId===this.userId){
                return true;
            }else{
                return false;
            }
        },
        ifTruck(){
            return this.order.truckId !== null;
        }

    },
    data() {
        return {
            order:null,
            userId:'',
            auth:0,
            newX:'',
            newY:'',
            truckX:'',
            truckY:'',
            dialogTableVisible:false,
            gridData: []
        };
    },
    methods:{
        dayjs,
        SaveHandle(order){
            this.$router.replace(`/orderdetail?shipId=${order.shipId}&edit_mode=0`).then(() => {
                this.updateOrderDetail();
            });

        },
        EditHandle(order){
            this.$router.replace(`/orderdetail?shipId=${order.shipId}&edit_mode=1`).then(() => {
                this.getOrderDetail();
            });

        },
        viewTruckDetail(){
            console.log("view truck detail: "+this.order.truckId)
            axios.post('/orderdetail', {shipId: this.shipId,edit_mode:'3'})
                .then(response => {
                    this.truckX = response.data.x;
                    this.truckY = response.data.y;
                    this.gridData = [{
                        x: response.data.x,
                        y: response.data.y
                    }]
                    console.log("view result x",this.gridData.x," y",this.gridData.y);
                })
                .catch(error => {
                    console.error(error);
                });
            this.dialogTableVisible = true;
        },
        getOrderDetail(){
            axios.post('/orderdetail', {shipId: this.shipId,edit_mode:'0'})
                .then(response => {
                    this.order = response.data;
                    this.newX = this.order.x;
                    this.newY = this.order.y;
                    console.log(response.data);
                })
                .catch(error => {
                    console.error(error);
                });
        },
        updateOrderDetail(){
            console.log("newX:",this.newX);
            console.log("newY:",this.newY);
            axios.post('/orderdetail', {shipId: this.shipId,edit_mode:'1',newX:this.newX,newY:this.newY})
                .then(response => {
                    this.order = response.data;
                    if (this.order.shipId<0){
                        // failure -> change shipId back
                        ElNotification({
                            title: 'Edit Failure',
                            message: 'You cannot change the destination due to the shipment status',
                            type: 'error',
                            position: 'top-right',
                            duration: 5000,
                            iconClass: 'el-icon-error',
                            showClose: true,
                            customClass: 'my-notification'
                        });
                        this.order.shipId=this.shipId;
                    }else{
                        //success -> update new data
                        ElNotification({
                            title: 'Change Success',
                            message: 'Order change success, Good',
                            type: 'success',
                            position: 'top-right',
                            duration: 5000,
                            iconClass: 'el-icon-success',
                            showClose: true,
                            customClass: 'my-notification'
                        });
                    }
                    this.newX = this.order.x;
                    this.newY = this.order.y;
                    console.log(response.data);
                })
                .catch(error => {
                    console.error(error);
                });
        },
        CancelHandle(){
            //not really handle it
            axios.post('/orderdetail', {shipId: this.shipId,edit_mode:'2'})
                .then(response => {
                    const temp = response.data;
                    if (temp.shipId<0){
                        // failure
                        ElNotification({
                            title: 'Cancel Failure',
                            message: 'Feature not yet developed',
                            type: 'error',
                            position: 'top-right',
                            duration: 5000,
                            iconClass: 'el-icon-error',
                            showClose: true,
                            customClass: 'my-notification'
                        });
                    }else{
                        //success
                        ElNotification({
                            title: 'Cancel Success',
                            message: 'Order cancel success, Good',
                            type: 'success',
                            position: 'top-right',
                            duration: 5000,
                            iconClass: 'el-icon-success',
                            showClose: true,
                            customClass: 'my-notification'
                        });
                        this.$router.push({path:'/'});
                    }
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
.el-alert {
    margin: 20px 0 0;
}
.el-alert:first-child {
    margin: 0;
}
</style>
