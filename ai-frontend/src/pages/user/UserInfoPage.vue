<template>
  <div id="userInfoPage" class="glass-card">
    <h2 class="title gradient-text">编辑个人信息</h2>
    <div class="desc muted">更新您的个人资料信息</div>
    
    <a-form 
      :model="formState" 
      name="userInfo" 
      autocomplete="off" 
      @finish="handleSubmit"
      layout="vertical"
      class="user-form"
    >
      <a-form-item 
        label="用户名" 
        name="userName" 
        :rules="[{ required: true, message: '请输入用户名' }]"
      >
        <a-input 
          v-model:value="formState.userName" 
          placeholder="请输入用户名" 
          size="large"
        />
      </a-form-item>
      
      <a-form-item 
        label="个人简介" 
        name="userProfile" 
      >
        <a-textarea 
          v-model:value="formState.userProfile" 
          placeholder="介绍一下自己吧..." 
          :rows="4"
          :maxlength="200"
          show-count
        />
      </a-form-item>
      
      <a-form-item 
        label="头像" 
        name="userAvatar" 
      >
        <div class="avatar-upload">
          <a-avatar 
            :src="formState.userAvatar" 
            :size="80"
            class="current-avatar"
          />
          <a-upload
            v-model:file-list="fileList"
            name="avatar"
            list-type="picture-card"
            class="avatar-uploader"
            :show-upload-list="false"
            :before-upload="beforeUpload"
            @change="handleChange"
          >
            <div v-if="!formState.userAvatar">
              <PlusOutlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </div>
        <div class="avatar-tips muted">
          支持 JPG、PNG 格式，文件大小不超过 2MB
        </div>
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button 
            type="primary" 
            html-type="submit" 
            size="large"
            class="pill-button-primary"
            :loading="updating"
          >
            保存修改
          </a-button>
          <a-button 
            @click="goBack" 
            size="large"
            class="pill-button-ghost"
          >
            返回
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { getUserVoById, updateUser } from '@/api/userController'
import type { UploadChangeParam } from 'ant-design-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const formState = reactive({
  id: 0,
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const updating = ref(false)
const fileList = ref([])

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const userId = loginUserStore.loginUser.id
    if (!userId) {
      message.error('用户未登录')
      router.push('/user/login')
      return
    }
    
    const res = await getUserVoById({ id: userId })
    if (res.data.code === 0 && res.data.data) {
      const user = res.data.data
      formState.id = user.id || 0
      formState.userName = user.userName || ''
      formState.userAvatar = user.userAvatar || ''
      formState.userProfile = user.userProfile || ''
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    message.error('获取用户信息失败')
  }
}

// 上传前检查
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 格式的图片!')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
  }
  return isJpgOrPng && isLt2M
}

// 处理上传变化
const handleChange = (info: UploadChangeParam) => {
  if (info.file.status === 'done') {
    // 这里应该处理文件上传成功后的逻辑
    // 暂时使用一个示例头像URL
    formState.userAvatar = 'https://via.placeholder.com/80x80'
    message.success('头像上传成功')
  }
}

// 提交表单
const handleSubmit = async (values: any) => {
  updating.value = true
  try {
    const res = await updateUser({
      id: formState.id,
      userName: values.userName,
      userAvatar: formState.userAvatar,
      userProfile: values.userProfile,
    })
    
    if (res.data.code === 0) {
      message.success('更新成功')
      // 更新本地存储的用户信息
      loginUserStore.setLoginUser({
        ...loginUserStore.loginUser,
        userName: values.userName,
        userAvatar: formState.userAvatar,
      })
      goBack()
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error) {
    console.error('更新用户信息失败：', error)
    message.error('更新失败，请重试')
  } finally {
    updating.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
#userInfoPage {
  max-width: 600px;
  padding: 32px 28px;
  margin: 40px auto;
}

.title {
  text-align: center;
  margin-bottom: 12px;
}

.desc {
  text-align: center;
  margin-bottom: 20px;
}

.user-form {
  margin-top: 24px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.current-avatar {
  border: 2px solid rgba(59, 130, 246, 0.2);
}

.avatar-uploader .ant-upload {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px dashed rgba(59, 130, 246, 0.3);
}

.avatar-uploader .ant-upload:hover {
  border-color: var(--primary-500);
}

.avatar-uploader .ant-upload-text {
  margin-top: 8px;
  color: var(--text-subtle);
}

.avatar-tips {
  font-size: 12px;
  line-height: 1.4;
}
</style>
