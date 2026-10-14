<template>
  <div class="recycle-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>回收站（保留 30 个自然日）</span>
        </div>
      </template>

      <el-alert
        title="回收站内记录保留 30 个自然日，系统启动时将自动物理删除到期记录。30 天内可随时恢复。"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      />

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="bizType" label="业务类型" width="130">
          <template #default="{ row }">
            <el-tag :type="row.bizType === 'inventory' ? 'warning' : 'info'">
              {{ row.bizType === 'inventory' ? '零库存' : row.bizType === 'image' ? '图片' : '标记' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizId" label="关联ID" width="100" />
        <el-table-column prop="deletedAt" label="删除时间" width="180" />
        <el-table-column prop="expireAt" label="到期清理时间" width="180" />
        <el-table-column prop="originalData" label="原始数据摘要" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleRestore(row.id)">恢复</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const query = ref({ page: 1, size: 10 })
const tableData = ref([])
const total = ref(0)

const loadData = async () => {
  try {
    const res = await request.get('/api/recycle/page', { params: query.value })
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

const handleRestore = async (id) => {
  try {
    await request.post(`/api/recycle/${id}/restore`)
    ElMessage.success('恢复成功')
    loadData()
  } catch (err) {
    ElMessage.error(err.message || '恢复失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.recycle-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
