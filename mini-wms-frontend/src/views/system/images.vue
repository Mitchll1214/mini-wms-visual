<template>
  <div class="image-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>图片管理</span>
          <div class="header-right">
            <el-select v-model="query.imageType" placeholder="全部类型" clearable style="width: 160px" @change="onQueryChange">
              <el-option label="商品主图" value="main" />
              <el-option label="商品相册" value="goods" />
              <el-option label="储位图" value="bin" />
              <el-option label="供应商资质" value="supplier" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column label="预览" width="90" align="center">
          <template #default="{ row }">
            <el-image
              :src="thumbUrl(row)"
              :preview-src-list="[contentUrl(row)]"
              preview-teleported
              fit="cover"
              style="width: 52px; height: 52px; border-radius: 6px"
            />
          </template>
        </el-table-column>
        <el-table-column label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.source === 'goods' ? 'primary' : 'warning'" size="small">
              {{ row.source === 'goods' ? '商品图' : '供应商' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            {{ typeLabel(row.image_type) }}
          </template>
        </el-table-column>
        <el-table-column prop="biz_name" label="所属业务" min-width="160" />
        <el-table-column prop="mime_type" label="格式" width="80" />
        <el-table-column label="大小" width="90">
          <template #default="{ row }">
            {{ formatSize(row.file_size) }}
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="上传时间" width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该图片吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="tableData.length === 0 && !loading" class="empty-state">暂无图片</div>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const query = ref({ page: 1, size: 20, imageType: '' })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/images/admin/page', { params: query.value })
    tableData.value = res?.records || res?.rows || []
    total.value = res?.total || 0
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const onQueryChange = () => {
  query.value.page = 1
  loadData()
}

const onPageChange = (p) => {
  query.value.page = p
  loadData()
}

const thumbUrl = (row) =>
  row.source === 'goods' ? `/api/images/${row.id}/thumb` : `/api/supplier-images/${row.id}/thumb`

const contentUrl = (row) =>
  row.source === 'goods' ? `/api/images/${row.id}/content` : `/api/supplier-images/${row.id}/content`

const typeLabel = (type) => {
  const map = { main: '商品主图', goods: '商品相册', bin: '储位图', supplier: '供应商资质', qualification: '供应商资质' }
  return map[type] || type || '-'
}

const formatSize = (bytes) => {
  if (!bytes && bytes !== 0) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

const handleDelete = async (row) => {
  try {
    if (row.source === 'goods') {
      await request.delete(`/api/images/${row.id}`)
    } else {
      await request.delete(`/api/supplier-images/${row.id}`)
    }
    ElMessage.success('已删除')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.image-manage-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-right { display: flex; gap: 10px; }
.empty-state { text-align: center; padding: 40px 20px; color: #909399; }
.pagination-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>