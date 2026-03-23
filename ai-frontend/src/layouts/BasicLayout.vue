<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader v-if="!isChatWorkspace" />
    <!-- 主要内容区域 -->
    <a-layout-content :class="isChatWorkspace ? 'main-content workspace-mode' : 'main-content'">
      <router-view />
    </a-layout-content>
    <!-- 底部版权信息 -->
    <GlobalFooter v-if="!isChatWorkspace" />
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const route = useRoute()

const isChatWorkspace = computed(() => {
  return route.path.startsWith('/app/chat/')
})
</script>

<style scoped>
.basic-layout {
  background: none;
}

.main-content {
  width: 100%;
  padding: 16px;
  background: none;
  margin: 0;
}

.workspace-mode {
  padding: 0;
}
</style>
