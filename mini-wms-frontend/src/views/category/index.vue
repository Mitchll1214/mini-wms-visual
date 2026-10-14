<template>
  <div class="category-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品类别管理</span>
          <el-button type="primary" size="small" @click="handleOpenDialog()">新建顶级类别</el-button>
        </div>
      </template>

      <el-table
        :data="categoryTree"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="类别名称" min-width="200" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenDialog(row.id)">添加子类</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该类别吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑类别' : '新建类别'" width="450px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="类别名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入类别名称" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const categoryTree = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = ref({ id: null, parentId: 0, name: '', sortOrder: 0 })
const rules = { name: [{ required: true, message: '请输入类别名称', trigger: 'blur' }] }

const loadTree = async () => {
  try {
    const res = await request.get('/api/category/tree')
    categoryTree.value = res || []
  } catch (err) {
    console.error(err)
  }
}

const handleOpenDialog = (parentId = 0) => {
  form.value = { id: null, parentId: parentId || 0, name: '', sortOrder: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { id: row.id, parentId: row.parentId, name: row.name, sortOrder: row.sortOrder }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.value.id) {
      await request.put(`/api/category/${form.value.id}`, form.value)
    } else {
      await request.post('/api/category', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadTree()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete(`/api/category/${id}`)
    ElMessage.success('删除成功')
    loadTree()
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

onMounted(loadTree)
</script>

<style scoped>
.category-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
