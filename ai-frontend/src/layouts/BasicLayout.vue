<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />
    
    <!-- 中间内容区域 -->
    <a-layout-content class="layout-content">
      <router-view />
    </a-layout-content>
    
    <!-- 底部版权信息 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  width: 100%;
  margin: 0 auto;
}

.layout-content {
  padding: 24px;
  padding-top: 88px; /* 为固定定位的头部留出空间 (64px头部 + 24pxpadding) */
  background: #f0f2f5;
  min-height: calc(100vh - 64px - 70px); /* 减去头部和底部的高度 */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-content {
    padding: 16px;
  }
}
</style>
