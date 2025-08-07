<template>
  <a-layout-header class="global-header">
    <div class="header-content">
      <!-- 左侧 Logo 和标题 -->
      <div class="header-left">
        <div class="logo-container">
          <img src="/暹罗猫.png" alt="Logo" class="logo" />
          <h1 class="site-title">AI代码生成平台</h1>
        </div>
      </div>

      <!-- 中间菜单 -->
      <div class="header-menu">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          class="menu"
          @click="handleMenuClick"
        />
        <!-- 管理员菜单 -->
        <a-menu
          v-if="loginUserStore.loginUser.userRole === 'admin'"
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="adminMenuItems"
          class="admin-menu"
          @click="handleMenuClick"
        />
      </div>

      <!-- 右侧用户信息 -->
      <div class="header-right">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '默认用户' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>

          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/LoginUser.ts'
import { LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'
import { message } from 'ant-design-vue'

const router = useRouter()
const selectedKeys = ref<string[]>(['home'])

const loginUserStore = useLoginUserStore()

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

// 菜单配置
const menuItems = [
  {
    key: 'home',
    label: '首页',
    icon: '🏠',
  },
  {
    key: 'generator',
    label: '代码生成',
    icon: '⚡',
  },
  {
    key: 'templates',
    label: '模板库',
    icon: '📚',
  },
  {
    key: 'docs',
    label: '文档',
    icon: '📖',
  },
  {
    key: 'about',
    label: '关于',
    icon: 'ℹ️',
  },
]

// 管理员菜单项
const adminMenuItems = [
  {
    key: 'userManage',
    label: '用户管理',
    icon: '👥',
  },
]

// 菜单点击处理
const handleMenuClick = ({ key }: { key: string }) => {
  selectedKeys.value = [key]
  // 路由跳转
  switch (key) {
    case 'home':
      router.push('/')
      break
    case 'userManage':
      router.push('/admin/userManage')
      break
    default:
      router.push({ name: key })
  }
}

// 登录处理
const handleLogin = () => {
  console.log('点击登录')
  // 这里可以添加登录逻辑
}
</script>

<style scoped>
.global-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 64px;
  line-height: 64px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 80%;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.header-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.menu {
  border: none;
  background: transparent;
}

.admin-menu {
  border: none;
  background: transparent;
  margin-left: 16px;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }

  .site-title {
    font-size: 16px;
  }

  .header-menu {
    display: none; /* 在移动端隐藏菜单，可以改为汉堡菜单 */
  }
}

@media (max-width: 480px) {
  .site-title {
    display: none; /* 在很小屏幕上隐藏标题 */
  }
}
</style>
