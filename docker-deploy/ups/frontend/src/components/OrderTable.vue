<template>
    <div class="order-search">
        <el-table :data="tableData" style="width: 100%" >
            <el-table-column fixed label="Ship ID" prop="shipId" width="150"></el-table-column>
            <el-table-column label="User ID" prop="userId" width="150"></el-table-column>
            <el-table-column label="Truck ID" prop="truckId" width="150"></el-table-column>
            <el-table-column label="Status" prop="shipmentStatus" width="150"></el-table-column>
            <el-table-column label="Commodity ID" prop="id" width="150"></el-table-column>
            <el-table-column label="Description" prop="description" width="150"></el-table-column>
            <el-table-column label="X" prop="x" width="150"></el-table-column>
            <el-table-column label="Y" prop="y" width="150"></el-table-column>
            <el-table-column label="Warehouse ID" prop="whId" width="150"></el-table-column>
            <el-table-column label="Created Time" prop="createdTime" width="150" :formatter="formatCreatedTime"></el-table-column>
            <el-table-column label="Delivering Time" prop="deliveringTime" width="150" :formatter="formatCreatedTime"></el-table-column>
            <el-table-column label="Delivered Time" prop="deliveredTime" width="150" :formatter="formatCreatedTime"></el-table-column>

            <el-table-column fixed="right" label=""  width="100">
                <template #default="{row}">
                    <el-button type="primary" color="rosybrown" @click="Detail(row)">Detail</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script>
import {ElTable, ElTableColumn} from "element-plus";
import jwt_decode from "jwt-decode";
import dayjs from "dayjs";

export default {
    name: "OrderTable",
    components:{ ElTable, ElTableColumn },
    props: {
        tableData: {
            type: Array,
            required: true,
        },
    },
    data(){
        return{
            userId:'',
        }
    },
    created() {
        // DONE: load information from local storage
        const authToken = localStorage.getItem('authToken');
        if (authToken) {
            const decoded = jwt_decode(authToken);
            const userIdAuth = decoded.sub;
            this.userId = userIdAuth
        }else{
            this.userId = ''
        }
        this.formatCreatedTime = (row, column, cellValue) => {
            if (!dayjs(cellValue).isValid()) {
                return null;
            }
            return dayjs(cellValue).format('YYYY-MM-DD HH:mm:ss');
        }

    },
    methods:{
        Detail(row){
            this.$router.push({path:'/orderdetail',query:{shipId:row.shipId,edit_mode:0}});
        },
        // Edit(row){
        //     this.$router.push({path:'/EditOrderDetail',query:{shipId:row.shipId,edit_mode:1}});
        // }
    }
};
</script>

<style scoped>
</style>