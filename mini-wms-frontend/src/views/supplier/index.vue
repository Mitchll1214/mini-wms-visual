<template>
  <div class="supplier-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>供应商管理</span>
          <el-button type="primary" size="small" @click="handleOpenDialog()">新建供应商</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="名称/联系人">
          <el-input v-model="query.keyword" placeholder="供应商名称/联系人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="name" label="供应商名称" min-width="160" />
        <el-table-column prop="contact" label="联系人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
              {{ row.status === 'ENABLED' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleOpenImages(row)">其他资料</el-button>
            <el-popconfirm title="确定删除该供应商吗？" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑供应商' : '新建供应商'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="联系人姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="公司或发货地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        <el-form-item label="资质图片">
          <el-upload
            :auto-upload="false"
            :show-file-list="true"
            accept="image/*"
            multiple
            :on-change="handleQualificationChange"
            :on-remove="handleQualificationChange"
          >
            <el-button size="small" type="primary" plain>选择图片</el-button>
            <template #tip>
              <div class="form-tip">营业执照、许可证等资质（选填，jpg/png/webp，单张 ≤ 10MB）</div>
            </template>
          </el-upload>
          <div v-if="existingQualificationImages.length" class="existing-qualifications">
            <div v-for="img in existingQualificationImages" :key="img.id" class="qualification-item">
              <el-image :src="`/api/supplier-images/${img.id}/thumb`" :preview-src-list="[`/api/supplier-images/${img.id}/content`]"
                preview-teleported fit="cover" class="qualification-thumb" />
              <el-button link type="danger" size="small" @click="handleDeleteSupplierImage(img.id)">删除</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="imagesDialogVisible" :title="`其他资料 - ${currentSupplier?.name || ''}`" width="640px">
      <div class="images-toolbar">
        <el-upload
          :show-file-list="false"
          :before-upload="beforeSupplierImageUpload"
          :http-request="handleSupplierImageUpload"
          accept="image/*"
        >
          <el-button type="primary" :loading="uploading">上传资质图片</el-button>
        </el-upload>
        <span class="images-tip">证件、资质等图片（jpg/png/webp，单张 ≤ 10MB）</span>
      </div>

      <div v-if="supplierImages.length === 0" class="images-empty">暂无图片</div>
      <div v-else class="images-grid">
        <div v-for="img in supplierImages" :key="img.id" class="image-item">
          <el-image
            :src="`/api/supplier-images/${img.id}/thumb`"
            :preview-src-list="[`/api/supplier-images/${img.id}/content`]"
            preview-teleported
            fit="cover"
            class="image-thumb"
          />
          <div class="image-actions">
            <el-popconfirm title="确定删除该图片吗？" @confirm="handleDeleteSupplierImage(img.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const query = ref({ page: 1, size: 10, keyword: '' })
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({ id: null, name: '', contact: '', phone: '', address: '', remark: '' })
const rules = { name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }] }

// 其他资料（资质图片）
const imagesDialogVisible = ref(false)
const currentSupplier = ref(null)
const supplierImages = ref([])
const uploading = ref(false)

// 表单内资质图片
const newQualificationFiles = ref([])
const existingQualificationImages = ref([])

const handleQualificationChange = (uploadFile, uploadFiles) => {
  form.value.newFiles = uploadFiles.map(f => f.raw)
}

const loadExistingQualifications = async (supplierId) => {
  if (!supplierId) return
  try {
    const res = await request.get(`/api/supplier-images/${supplierId}/images`)
    existingQualificationImages.value = res || []
  } catch (err) {
    console.error(err)
  }
}

const handleOpenImages = (row) => {
  currentSupplier.value = row
  supplierImages.value = []
  imagesDialogVisible.value = true
  loadSupplierImages(row.id)
}

const loadSupplierImages = async (supplierId) => {
  try {
    const res = await request.get(`/api/supplier-images/${supplierId}/images`)
    supplierImages.value = res || []
  } catch (err) {
    console.error(err)
  }
}

const beforeSupplierImageUpload = (file) => {
  const ok = file.size / 1024 / 1024 < 10
  if (!ok) ElMessage.error('图片大小不能超过 10MB')
  return ok
}

const handleSupplierImageUpload = async ({ file }) => {
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    await request.post(`/api/supplier-images/${currentSupplier.value.id}/images?imageType=qualification`, fd)
    ElMessage.success('上传成功')
    loadSupplierImages(currentSupplier.value.id)
  } catch (err) {
    ElMessage.error(err.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

const handleDeleteSupplierImage = async (imageId) => {
  try {
    await request.delete(`/api/supplier-images/${imageId}`)
    ElMessage.success('已删除')
    // 编辑表单内删除：刷新表单内资质列表
    if (form.value.id) {
      loadExistingQualifications(form.value.id)
    }
    // 其他资料弹窗内删除：刷新弹窗列表
    if (currentSupplier.value?.id) {
      loadSupplierImages(currentSupplier.value.id)
    }
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

const loadData = async () => {
  try {
    const res = await request.get('/api/supplier/page', { params: query.value })
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
  form.value = { id: null, name: '', contact: '', phone: '', address: '', remark: '', newFiles: [] }
  existingQualificationImages.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row, newFiles: [] }
  existingQualificationImages.value = []
  dialogVisible.value = true
  loadExistingQualifications(row.id)
}

const uploadNewQualifications = async (supplierId) => {
  const files = form.value.newFiles || []
  for (const file of files) {
    const fd = new FormData()
    fd.append('file', file)
    await request.post(`/api/supplier-images/${supplierId}/images?imageType=qualification`, fd)
  }
}

const handleSubmit = async () => {
  try {
    let id = form.value.id
    if (id) {
      await request.put(`/api/supplier/${form.value.id}`, form.value)
    } else {
      const res = await request.post('/api/supplier', form.value)
      id = res?.id || res
    }
    if ((form.value.newFiles || []).length) {
      await uploadNewQualifications(id)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete(`/api/supplier/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.supplier-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 10px; }
.pagination-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }
.images-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.images-tip { font-size: 12px; color: var(--el-text-color-secondary); }
.images-empty { text-align: center; color: var(--el-text-color-secondary); padding: 32px 0; }
.images-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 12px; }
.image-item { position: relative; }
.image-thumb { width: 100%; height: 120px; border-radius: 6px; }
.image-actions { position: absolute; bottom: 4px; right: 4px; background: rgba(255,255,255,0.9); border-radius: 4px; padding: 0 4px; }
</style>
