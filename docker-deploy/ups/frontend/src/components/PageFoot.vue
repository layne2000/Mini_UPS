<template>
    <div>
        <el-footer height="30px" class="footer">
            © 2023 Mini UPS. All rights reserved.
        </el-footer>
    </div>
</template>

<script>
export default {
    name: "PageFoot"
}
</script>

<style scoped>
.footer {
    position: fixed;
    bottom: 0;
    width: 100%;
    font-size: 12px;
    text-align: center;
    line-height: 10px;
    background-color: rosybrown;
    padding: 10px;
    z-index: 9999;
}
</style>