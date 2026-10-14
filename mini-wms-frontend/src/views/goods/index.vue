<template>
  <div class="goods-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-button type="primary" size="small" @click="handleOpenDialog()">新建商品</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="商品名称/条码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column label="主图" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.mainImageUrl"
              :src="row.mainImageUrl"
              :preview-src-list="[row.mainImageUrl]"
              preview-teleported
              fit="cover"
              style="width: 44px; height: 44px; border-radius: 6px"
            />
            <el-icon v-else color="#c0c4cc" style="font-size: 28px"><Picture /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="商品编码" width="140" />
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column prop="spec" label="规格型号" width="130" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="barcode" label="条码" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="$router.push(`/goods/${row.id}`)">详情/图片</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 'ENABLED' ? 'warning' : 'success'" size="small" @click="handleToggleStatus(row)">
              {{ row.status === 'ENABLED' ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="query.page"
          :page-size="query.size"
          :total="total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新建商品'" width="580px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" placeholder="例如：红色 / L码" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="个 / 箱 / 件" />
        </el-form-item>
        <el-form-item label="条形码" prop="barcode">
          <el-input v-model="form.barcode" placeholder="请输入条形码" />
        </el-form-item>
        <el-form-item label="商品主图" prop="mainImage">
          <el-upload
            ref="mainImageUploadRef"
            :auto-upload="false"
            list-type="picture-card"
            accept="image/*"
            multiple
            :file-list="mainImageFileList"
            :on-change="onMainImageChange"
            :on-remove="onMainImageChange"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">选填，可添加多张主图，保存商品后自动上传，支持 jpg/png/webp</div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const query = ref({ page: 1, size: 10, keyword: '' })
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({ id: null, code: '', name: '', spec: '', unit: '', barcode: '', remark: '' })
const formRef = ref(null)
const mainImageFileList = ref([])
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  barcode: [{ required: true, message: '请输入条形码', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await request.get('/api/goods/page', { params: query.value })
    tableData.value = res?.records || res?.rows || []
    total.value = res?.total || 0
  } catch (err) {
    console.error(err)
  }
}

const onPageChange = (p) => {
  query.value.page = p
  loadData()
}

const handleOpenDialog = () => {
  form.value = { id: null, code: '', name: '', spec: '', unit: '', barcode: '', remark: '' }
  mainImageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  form.value = { ...row }
  mainImageFileList.value = []
  dialogVisible.value = true
  // 加载已有主图用于展示
  try {
    const imgs = (await request.get(`/api/images/${row.id}/images`, { params: { imageType: 'main' } })) || []
    mainImageFileList.value = imgs.map((img) => ({
      name: `main-${img.id}`,
      url: `/api/images/${img.id}/thumb`,
      uid: `existing-${img.id}`,
      isExisting: true
    }))
  } catch (e) {
    // 忽略加载失败
  }
}

const mainImageUploadRef = ref(null)
const onMainImageChange = (uploadFile, uploadFiles) => {
  mainImageFileList.value = uploadFiles
}

// 上传所有新选择的主图（跳过已存在的）
const uploadMainImages = async (goodsId) => {
  const pending = mainImageFileList.value.filter((f) => f.raw)
  for (const f of pending) {
    const fd = new FormData()
    fd.append('file', f.raw)
    await request.post(`/api/images/${goodsId}/images?imageType=main`, fd)
  }
}

const handleSubmit = async () => {
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    let id = form.value.id
    if (id) {
      await request.put(`/api/goods/${id}`, form.value)
    } else {
      const res = await request.post('/api/goods', form.value)
      id = res?.id
    }
    // 商品主图选填上传（imageType=main → main/{goodsId}/ 分区，可多张）
    if (id && mainImageFileList.value.some((f) => f.raw)) {
      await uploadMainImages(id)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    await request.post(`/api/goods/${row.id}/status?status=${newStatus}`)
    ElMessage.success('状态已更新')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '更新失败')
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete(`/api/goods/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.goods-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 10px; }
.pagination-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }
.form-tip { font-size: 12px; color: var(--el-text-color-secondary); line-height: 1.8; }
</style>
