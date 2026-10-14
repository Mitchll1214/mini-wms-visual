<template>
  <div class="users-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              placeholder="搜索用户名"
              clearable
              style="width: 200px; margin-right: 10px;"
              @keyup.enter="loadData(1)"
              @clear="loadData(1)"
            />
            <el-button type="primary" size="small" @click="openCreate">新建用户</el-button>
            <el-button size="small" @click="loadData(currentPage)">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)">
              {{ roleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该用户？此操作不可恢复" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small" :disabled="row.username === currentUsername">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新建 / 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建用户' : '编辑用户'" width="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="登录用户名" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item :label="dialogMode === 'create' ? '密码' : '重置密码'" required>
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="dialogMode === 'edit' ? '留空则不修改密码' : '至少 4 位'"
          />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" style="width: 100%;">
            <el-option label="管理员（全部权限）" value="ADMIN" />
            <el-option label="操作员（除用户管理、删除数据外）" value="OPERATOR" />
            <el-option label="盘点员（仅查看）" value="INSPECTOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const currentUsername = userStore.username

const tableData = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref('create') // create / edit
const saving = ref(false)
const form = ref({
  id: null,
  username: '',
  password: '',
  role: 'OPERATOR',
  enabled: 1
})

const roleLabel = (role) => ({ ADMIN: '管理员', OPERATOR: '操作员', INSPECTOR: '盘点员' }[role] || role)
const roleTagType = (role) => ({ ADMIN: 'danger', OPERATOR: 'primary', INSPECTOR: 'info' }[role] || 'info')

const loadData = async (page = 1) => {
  currentPage.value = page
  try {
    const res = await request.get('/api/auth/users/page', {
      params: { page: currentPage.value, size: pageSize.value, keyword: keyword.value || undefined }
    })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (err) {
    console.error(err)
  }
}

const openCreate = () => {
  dialogMode.value = 'create'
  form.value = { id: null, username: '', password: '', role: 'OPERATOR', enabled: 1 }
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogMode.value = 'edit'
  form.value = {
    id: row.id,
    username: row.username,
    password: '',
    role: row.role || 'OPERATOR',
    enabled: row.enabled
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (dialogMode.value === 'create' && (!form.value.password || form.value.password.length < 4)) {
    ElMessage.warning('密码至少 4 位')
    return
  }
  if (dialogMode.value === 'edit' && form.value.password && form.value.password.length < 4) {
    ElMessage.warning('密码至少 4 位')
    return
  }
  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await request.post('/api/auth/users', {
        username: form.value.username,
        passwordHash: form.value.password,
        role: form.value.role,
        enabled: form.value.enabled
      })
      ElMessage.success('用户创建成功')
    } else {
      await request.put(`/api/auth/users/${form.value.id}`, {
        passwordHash: form.value.password || null,
        role: form.value.role,
        enabled: form.value.enabled
      })
      ElMessage.success('用户更新成功')
    }
    dialogVisible.value = false
    loadData(currentPage.value)
  } catch (err) {
    // 错误已在拦截器提示
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await request.delete(`/api/auth/users/${row.id}`)
    ElMessage.success('用户已删除')
    loadData(currentPage.value)
  } catch (err) {
    // 错误已提示
  }
}

onMounted(() => loadData(1))
</script>

<style scoped>
.users-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
.pagination-wrap { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>