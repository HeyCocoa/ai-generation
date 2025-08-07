<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { addUser, deleteUser, listUserVoByPage, updateUser } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/LoginUser.ts'
import { useRouter } from 'vue-router'

// 表格数据
const tableData = ref<API.UserVO[]>([])
const loading = ref(false)
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  userName: '',
  userAccount: '',
  userRole: '',
})

// 添加/编辑表单
const formState = reactive<API.UserAddRequest>({
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
})

// 模态框控制
const modalVisible = ref(false)
const modalTitle = ref('添加用户')
const isEdit = ref(false)
const editingUserId = ref<number>()
const formRef = ref()

const loginUserStore = useLoginUserStore()
const router = useRouter()

/**
 *
 * 加载用户列表
 */
const loadUserList = async () => {
  // 检查用户权限
  if (!loginUserStore.loginUser.id) {
    message.error('请先登录')
    router.push('/user/login')
    return
  }

  if (loginUserStore.loginUser.userRole !== 'admin') {
    message.error('您没有权限访问此页面')
    router.push('/')
    return
  }

  loading.value = true
  try {
    // 构建查询参数，只包含有值的字段
    const queryParams: any = {
      pageNum: current.value,
      pageSize: pageSize.value,
    }

    // 只有当搜索条件不为空时才添加到查询参数中
    if (searchForm.userName && searchForm.userName.trim()) {
      queryParams.userName = searchForm.userName.trim()
    }
    if (searchForm.userAccount && searchForm.userAccount.trim()) {
      queryParams.userAccount = searchForm.userAccount.trim()
    }
    if (searchForm.userRole && searchForm.userRole.trim()) {
      queryParams.userRole = searchForm.userRole.trim()
    }

    console.log('查询参数:', queryParams)
    const res = await listUserVoByPage(queryParams)
    if (res.data.code === 0 && res.data.data) {
      tableData.value = res.data.data.records || []
      total.value = res.data.data.totalRow || 0
      console.log('用户列表数据:', res.data.data) // 调试信息
    } else {
      message.error('获取用户列表失败：' + res.data.message)
    }
  } catch (error) {
    console.error('加载用户列表错误:', error) // 调试信息
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索用户
 */
const handleSearch = () => {
  current.value = 1
  loadUserList()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.userName = ''
  searchForm.userAccount = ''
  searchForm.userRole = ''
  current.value = 1
  console.log('重置搜索条件，显示所有用户')
  loadUserList()
}

/**
 * 分页变化
 */
const handleTableChange = (pagination: any) => {
  current.value = pagination.current
  pageSize.value = pagination.pageSize
  loadUserList()
}

/**
 * 打开添加用户模态框
 */
const showAddModal = () => {
  // 检查用户权限
  if (loginUserStore.loginUser.userRole !== 'admin') {
    message.error('您没有权限执行此操作')
    return
  }

  modalTitle.value = '添加用户'
  isEdit.value = false
  editingUserId.value = undefined
  // 重置表单
  Object.assign(formState, {
    userName: '',
    userAccount: '',
    userAvatar: '',
    userProfile: '',
    userRole: '',
  })
  modalVisible.value = true
}

/**
 * 打开编辑用户模态框
 */
const showEditModal = (record: API.UserVO) => {
  // 检查用户权限
  if (loginUserStore.loginUser.userRole !== 'admin') {
    message.error('您没有权限执行此操作')
    return
  }

  console.log('编辑用户:', record)
  modalTitle.value = '编辑用户'
  isEdit.value = true
  editingUserId.value = record.id
  // 填充表单数据
  Object.assign(formState, {
    userName: record.userName || '',
    userAccount: record.userAccount || '',
    userAvatar: record.userAvatar || '',
    userProfile: record.userProfile || '',
    userRole: record.userRole || '',
  })
  console.log('表单数据已填充:', formState)
  modalVisible.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 检查用户权限
  if (loginUserStore.loginUser.userRole !== 'admin') {
    message.error('您没有权限执行此操作')
    return
  }

  try {
    // 表单验证
    await formRef.value?.validate()

    if (isEdit.value && editingUserId.value) {
      // 编辑用户
      const updateData = {
        id: editingUserId.value,
        userName: formState.userName,
        userAvatar: formState.userAvatar,
        userProfile: formState.userProfile,
        userRole: formState.userRole,
      }
      console.log('提交编辑数据:', updateData)
      const res = await updateUser(updateData)
      console.log('编辑响应:', res)
      if (res.data.code === 0) {
        message.success('更新用户成功')
        modalVisible.value = false
        loadUserList()
      } else {
        message.error('更新用户失败：' + res.data.message)
      }
    } else {
      // 添加用户
      const res = await addUser(formState)
      if (res.data.code === 0) {
        message.success('添加用户成功')
        modalVisible.value = false
        loadUserList()
      } else {
        message.error('添加用户失败：' + res.data.message)
      }
    }
  } catch (error) {
    console.error('表单提交错误:', error)
    message.error('操作失败')
  }
}

/**
 * 取消操作
 */
const handleCancel = () => {
  // 检查用户权限
  if (loginUserStore.loginUser.userRole !== 'admin') {
    message.error('您没有权限执行此操作')
    return
  }

  modalVisible.value = false
  formRef.value?.resetFields()
}

/**
 * 删除用户
 */
const handleDelete = (record: API.UserVO) => {
  // 检查用户权限
  if (loginUserStore.loginUser.userRole !== 'admin') {
    message.error('您没有权限执行此操作')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户 "${record.userName || record.userAccount}" 吗？`,
    onOk: async () => {
      try {
        const res = await deleteUser({ id: record.id! })
        if (res.data.code === 0) {
          message.success('删除用户成功')
          loadUserList()
        } else {
          message.error('删除用户失败：' + res.data.message)
        }
      } catch (error) {
        message.error('删除用户失败')
      }
    },
  })
}

// 页面加载时获取数据
onMounted(() => {
  console.log('用户管理页面加载，开始获取用户列表')
  loadUserList()
})
</script>

<template>
  <div id="userManagePage">
    <h2 class="title">AI 代码生成 - 用户管理</h2>
    <div class="desc">管理系统用户信息</div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <div class="search-form-container">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="用户名">
            <a-input
              v-model:value="searchForm.userName"
              placeholder="请输入用户名"
              style="width: 200px"
            />
          </a-form-item>
          <a-form-item label="账号">
            <a-input
              v-model:value="searchForm.userAccount"
              placeholder="请输入账号"
              style="width: 200px"
            />
          </a-form-item>
          <a-form-item label="角色">
            <a-select
              v-model:value="searchForm.userRole"
              placeholder="请选择角色"
              style="width: 150px"
              allowClear
            >
              <a-select-option value="user">普通用户</a-select-option>
              <a-select-option value="admin">管理员</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button style="margin-left: 10px" @click="handleReset">重置</a-button>
          </a-form-item>
        </a-form>
        <div class="add-user-button">
          <a-button type="primary" @click="showAddModal">添加用户</a-button>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="table-area">
      <a-table
        :columns="[
          {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 80,
          },
          {
            title: '用户名',
            dataIndex: 'userName',
            key: 'userName',
          },
          {
            title: '账号',
            dataIndex: 'userAccount',
            key: 'userAccount',
          },
          {
            title: '头像',
            dataIndex: 'userAvatar',
            key: 'userAvatar',
            slots: { customRender: 'avatar' },
          },
          {
            title: '角色',
            dataIndex: 'userRole',
            key: 'userRole',
          },
          {
            title: '简介',
            dataIndex: 'userProfile',
            key: 'userProfile',
            ellipsis: true,
          },
          {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
          },
          {
            title: '操作',
            key: 'action',
            width: 200,
            slots: { customRender: 'action' },
          },
        ]"
        :data-source="tableData"
        :loading="loading"
        :pagination="{
          current: current,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条记录`,
        }"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <template #empty>
          <div style="text-align: center; padding: 40px">
            <a-empty description="暂无用户数据" />
          </div>
        </template>

        <template #avatar="{ text }">
          <a-avatar v-if="text" :src="text" />
          <a-avatar v-else>U</a-avatar>
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="showEditModal(record)">编辑</a-button>
            <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table>
    </div>

    <!-- 添加/编辑用户模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleSubmit"
      @cancel="handleCancel"
      width="600px"
      cancel-text="取消"
      ok-text="确认"
    >
      <a-form ref="formRef" :model="formState" layout="vertical">
        <a-form-item
          label="用户名"
          name="userName"
          :rules="[{ required: true, message: '请输入用户名' }]"
        >
          <a-input v-model:value="formState.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item
          label="账号"
          name="userAccount"
          :rules="[{ required: true, message: '请输入账号' }]"
        >
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item label="头像" name="userAvatar">
          <a-input v-model:value="formState.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item
          label="角色"
          name="userRole"
          :rules="[{ required: true, message: '请选择角色' }]"
        >
          <a-select v-model:value="formState.userRole" placeholder="请选择角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="简介" name="userProfile">
          <a-textarea
            v-model:value="formState.userProfile"
            placeholder="请输入用户简介"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
#userManagePage {
  padding: 24px;
  width: 80%;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 24px;
}

.search-area {
  background: #fafafa;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.search-form-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.add-user-button {
  margin-left: 16px;
}

.table-area {
  background: white;
  border-radius: 8px;
  padding: 16px;
}
</style>
