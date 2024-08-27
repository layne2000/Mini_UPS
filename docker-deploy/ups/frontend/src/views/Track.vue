<template>
    <div class="page-wrapper">
  <MenuLine/>
  <el-main>
      <div class="order-search">
          <el-form inline style="display: flex; justify-content: center;">
              <el-form-item label="Tracking Number" class="tracking-label">
                  <el-input v-model="orderNo" placeholder=""></el-input>
              </el-form-item>
              <el-form-item>
                  <el-button type="primary" color="rosybrown" @click="search">Track</el-button>
              </el-form-item>
          </el-form>
          <br>
      </div>
      <div class="order-filter">
          <OrderTable :table-data="tableData"/>
      </div>
  </el-main>
    <PageFoot/>
    </div>
</template>

<script>
import MenuLine from "@/components/Menu.vue"
import OrderTable from "@/components/OrderTable.vue";
import axios from "axios"
import PageFoot from "@/components/PageFoot.vue";
import {ElMessage} from "element-plus";
export default {
    name: "TrackPage",
    components: {MenuLine,OrderTable,PageFoot},
    computed: {
        id() {
            return this.$route.query.shipId;
        },
    },
    methods:{
        search(){
            this.$router.replace(`/tracking?shipId=${this.orderNo}`).then(() => {
                this.getOrders();
            });
        },
        getOrders(){
            //get package info from database
            axios.post('/tracking', {shipId: this.id})
                .then(response => {
                    this.tableData = response.data;
                    console.log(this.id)
                    if (Array.isArray(this.tableData) && this.tableData.length === 0 && this.orderNo!=='') {
                        ElMessage({
                            message: 'Cannot find this order, please input correct tracking number',
                            type: 'warning'
                        })
                    }
                })
                .catch(error => {
                    console.error(error);
                });
        }
    },
    data() {
        return {
            orderNo:'',
            tableData: [],
        }
    },
    created() {
      this.getOrders();
    }

};

</script>

<style>
</style>