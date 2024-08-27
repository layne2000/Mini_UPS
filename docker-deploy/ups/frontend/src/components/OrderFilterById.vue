<template>
    <div class="order-search">
        <el-table :data="filteredData" style="width: 100%">
            <el-table-column prop="orderNo" label="TrackID"></el-table-column>
            <el-table-column prop="account" label="User"></el-table-column>
            <el-table-column prop="productName" label="Description"></el-table-column>
            <el-table-column prop="status" label="Status"></el-table-column>
            <el-table-column prop="TruckId" label="TruckId"></el-table-column>
            <el-table-column prop="posx" label="PositionX"></el-table-column>
            <el-table-column prop="posy" label="PositionY"></el-table-column>
            <el-table-column prop="whid" label="WarehouseId"></el-table-column>
        </el-table>
    </div>
</template>

<script>
import {ElTable, ElTableColumn} from "element-plus";

export default {
    name: "OrderFilterById",
    components:{ ElTable, ElTableColumn },
    props: {
        tableData: {
            type: Array,
            required: true,
        },
        orderNo: {
            type: String,
            required: true,
        },
    },
    computed: {
        filteredData() {
            return this.tableData;
        },
    },
}
</script>

<style scoped>

</style>