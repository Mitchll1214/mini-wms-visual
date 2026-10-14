<template>
  <div class="logs-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存流水记录（永久保留）</span>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="变动类型">
          <el-select v-model="query.changeType" placeholder="全部类型" clearable style="width: 160px">
            <el-option label="入库" value="in" />
            <el-option label="出库" value="out" />
            <el-option label="库存调整" value="adjust" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询流水</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="createdAt" label="变动时间" width="170" />
        <el-table-column prop="goodsName" label="商品名称" min-width="160" />
        <el-table-column prop="barcode" label="条码" width="150" />
        <el-table-column prop="binCode" label="储位编码" width="120" />
        <el-table-column prop="changeType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.changeType === 'in' ? 'success' : row.changeType === 'out' ? 'danger' : 'warning'">
              {{ row.changeType === 'in' ? '入库' : row.changeType === 'out' ? '出库' : '调整' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeQty" label="变动数量" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.changeQty > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
              {{ row.changeQty > 0 ? '+' + row.changeQty : row.changeQty }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQty" label="变动前" width="80" />
        <el-table-column prop="afterQty" label="变动后" width="80" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="adjustReason" label="原因/备注" min-width="150" />
        <el-table-column prop="deviceIp" label="操作 IP" width="140" fixed="right" />
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

const query = ref({ page: 1, size: 10, changeType: '' })
const tableData = ref([])
const total = ref(0)

const loadData = async () => {
  try {
    const res = await request.get('/api/inventory/logs', { params: query.value })
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
.logs-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 10px; }
.pagination-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
