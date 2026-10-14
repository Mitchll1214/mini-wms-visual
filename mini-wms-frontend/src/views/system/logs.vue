<template>
  <div class="system-logs-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统操作日志</span>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="模块">
          <el-input v-model="query.module" placeholder="操作模块" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="动作" width="120" />
        <el-table-column prop="deviceInfo" label="设备" width="100" />
        <el-table-column prop="detail" label="详细说明" min-width="200" />
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
import request from '@/utils/request'

const query = ref({ page: 1, size: 10, module: '' })
const tableData = ref([])
const total = ref(0)

const loadData = async () => {
  try {
    const res = await request.get('/api/system/logs', { params: query.value })
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

onMounted(loadData)
</script>

<style scoped>
.system-logs-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 10px; }
.pagination-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
