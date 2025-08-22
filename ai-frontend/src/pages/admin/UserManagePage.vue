<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="120" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" @click="doEdit(record)">编辑</a-button>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 编辑用户模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑用户信息"
      width="600px"
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
      :confirm-loading="updating"
    >
      <a-form
        ref="editFormRef"
        :model="editForm"
        layout="vertical"
        :rules="editRules"
      >
        <a-form-item label="用户名" name="userName">
          <a-input
            v-model:value="editForm.userName"
            placeholder="请输入用户名"
            size="large"
          />
        </a-form-item>
        
        <a-form-item label="个人简介" name="userProfile">
          <a-textarea
            v-model:value="editForm.userProfile"
            placeholder="介绍一下用户..."
            :rows="3"
            :maxlength="200"
            show-count
          />
        </a-form-item>
        
        <a-form-item label="头像" name="userAvatar">
          <div class="avatar-edit">
            <a-avatar
              :src="editForm.userAvatar"
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
              <div v-if="!editForm.userAvatar">
                <PlusOutlined />
                <div style="margin-top: 8px">上传</div>
              </div>
            </a-upload>
          </div>
          <div class="avatar-tips muted">
            支持 JPG、PNG 格式，文件大小不超过 2MB
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage, updateUser } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import type { FormInstance, UploadChangeParam } from 'ant-design-vue'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 编辑相关
const editModalVisible = ref(false)
const updating = ref(false)
const editFormRef = ref<FormInstance>()
const fileList = ref([])

const editForm = reactive({
  id: 0,
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const editRules = {
  userName: [{ required: true, message: '请输入用户名' }],
}

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 编辑数据
const doEdit = (record: API.UserVO) => {
  editForm.id = record.id || 0
  editForm.userName = record.userName || ''
  editForm.userAvatar = record.userAvatar || ''
  editForm.userProfile = record.userProfile || ''
  editModalVisible.value = true
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
    editForm.userAvatar = 'https://via.placeholder.com/80x80'
    message.success('头像上传成功')
  }
}

// 提交编辑
const handleEditSubmit = async () => {
  try {
    await editFormRef.value?.validate()
    updating.value = true
    
    const res = await updateUser({
      id: editForm.id,
      userName: editForm.userName,
      userAvatar: editForm.userAvatar,
      userProfile: editForm.userProfile,
    })
    
    if (res.data.code === 0) {
      message.success('更新成功')
      editModalVisible.value = false
      fetchData() // 刷新数据
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

// 取消编辑
const handleEditCancel = () => {
  editModalVisible.value = false
  editFormRef.value?.resetFields()
}

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}

.avatar-edit {
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
  border-color: #3b82f6;
}

.avatar-tips {
  font-size: 12px;
  line-height: 1.4;
  color: #6b7280;
}
</style>
