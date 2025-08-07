<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/LoginUser.ts'
import { message } from 'ant-design-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const goToUserManage = () => {
  router.push('/admin/userManage')
}

const goToLogin = () => {
  // 检查用户是否已登录
  if (loginUserStore.loginUser.id) {
    message.info(`您已登录，当前用户：${loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount}`)
  } else {
    router.push('/user/login')
  }
}

const goToRegister = () => {
  // 检查用户是否已登录
  if (loginUserStore.loginUser.id) {
    message.info('您已登录，无需注册新账号')
  } else {
    router.push('/user/register')
  }
}
</script>

<template>
  <div class="home-page">
    <h1>AI 代码生成平台</h1>
    <p>欢迎使用 AI 代码生成平台！</p>
    
    <!-- 用户登录状态显示 -->
    <div v-if="loginUserStore.loginUser.id" class="user-status">
      <h3>当前登录状态：</h3>
      <div class="user-info">
        <a-avatar :src="loginUserStore.loginUser.userAvatar" />
        <span class="user-name">{{ loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount }}</span>
        <a-tag :color="loginUserStore.loginUser.userRole === 'admin' ? 'red' : 'blue'">
          {{ loginUserStore.loginUser.userRole === 'admin' ? '管理员' : '普通用户' }}
        </a-tag>
      </div>
    </div>
    
    <div class="features">
      <h3>功能特性：</h3>
      <ul>
        <li>用户注册和登录</li>
        <li>用户管理（管理员功能）</li>
        <li>AI 代码生成</li>
        <li>模板库</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 800px;
  width: 80%;
  margin: 0 auto;
  padding: 24px;
  text-align: center;
}

.home-page h1 {
  color: #1890ff;
  margin-bottom: 16px;
}

.navigation-links {
  margin: 32px 0;
  padding: 24px;
  background: #f5f5f5;
  border-radius: 8px;
}

.link-group {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 16px;
}

.user-status {
  margin: 32px 0;
  padding: 24px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
  margin-top: 16px;
}

.user-name {
  font-weight: 500;
  color: #1890ff;
}

.features {
  text-align: left;
  max-width: 400px;
  margin: 0 auto;
}

.features ul {
  list-style: none;
  padding: 0;
}

.features li {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.features li:last-child {
  border-bottom: none;
}
</style>
