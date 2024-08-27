<template>
    <div class="common-layout" >
        <el-container >
            <el-header>
                <el-affix :offset-top="100">
                <div>
                <el-menu class="el-menu-demo" mode="horizontal" :ellipsis="false">
                    <el-menu-item index="0">
                        <img :src="require('@/assets/logo_ups.png')" alt="UPS" style="width: 40px; height: 40px; margin-right: 8px;">
                        <router-link to="/">UPS</router-link>
                    </el-menu-item>
                    <div class="flex-grow" />
                    <span class="my-menu-greeting">Hi~ {{userId}} Welcome to Mini UPS :)</span>
                    <div class="flex-grow" />
                        <el-menu-item index="1">
                            <router-link to="/tracking">Track</router-link>
                        </el-menu-item>
                        <el-menu-item index="2">
                            <router-link to="/myorder">MyOrder</router-link>
                        </el-menu-item>
                        <el-sub-menu index="3" >
                            <template #title>
                                <img :src="require('@/assets/userlogo.png')" alt="user" style="width: 40px; height: 40px; margin-right: 8px;">
                                Account
                            </template>
                            <el-menu-item v-if="userId" index="3-1" >
                                <router-link to="/profile">MyProfile</router-link>
                            </el-menu-item>
                            <el-menu-item v-if="!userId" index="3-2" >
                                <router-link to="/signup">Signup</router-link>
                            </el-menu-item>
                            <el-menu-item v-if="!userId" index="3-3" >
                                <router-link  to="/mylogin">Login</router-link>
                            </el-menu-item>
                            <el-menu-item v-else index="3-4" >
                                <router-link to="/logout">Logout</router-link>
                            </el-menu-item>
                        </el-sub-menu>
                </el-menu>
                </div>
            </el-affix>
            </el-header>
        </el-container>
    </div>
</template>


<script>
import jwt_decode from "jwt-decode";

export default {
    name: "MenuLine",
    data(){
        return{
            userId:''
        }
    },
    created() {
        const authToken = localStorage.getItem('authToken');
        if (authToken) {
            const decoded = jwt_decode(authToken);
            const userIdAuth = decoded.sub;
            this.userId = userIdAuth
        }else{
            this.userId = ''
        }
    },
}
</script>

<style scoped>
.flex-grow {
    flex-grow: 1;
}
.el-menu-demo {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    width: 100%;
    z-index: 999;
}
.my-menu-greeting{
    display: inline-block;
    text-align: center;
    line-height: 60px;
    font-size: 15px;
}

</style>