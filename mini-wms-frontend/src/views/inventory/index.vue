<template>
  <div class="inventory-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存总览（商品合并视图）</span>
          <div>
            <el-button type="warning" size="small" @click="$router.push('/inventory/adjust')">库存调整</el-button>
          </div>
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
        <el-table-column type="expand">
          <template #default="{ row }">
            <div style="padding: 10px 20px;">
              <h4>储位库存明细</h4>
              <el-table :data="row.binList || []" size="small" border>
                <el-table-column prop="binCode" label="储位编码" width="160" />
                <el-table-column prop="binName" label="储位名称" width="160" />
                <el-table-column prop="qty" label="当前库存" width="120">
                  <template #default="scope">
                    <span :style="{ color: scope.row.qty === 0 ? '#F56C6C' : '#303133', fontWeight: 'bold' }">
                      {{ scope.row.qty }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="140">
                  <template #default="scope">
                    <el-button
                      v-if="scope.row.qty === 0"
                      link
                      type="danger"
                      size="small"
                      @click="handleZeroStockDelete(scope.row.id)"
                    >
                      零库存删除
                    </el-button>
                    <span v-else style="color: #909399; font-size: 12px;">正常在库</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="goodsBarcode" label="条码" width="140" />
        <el-table-column prop="goodsName" label="商品名称" min-width="160" />
        <el-table-column prop="categoryName" label="类别" width="120" />
        <el-table-column prop="totalQty" label="总库存" width="120">
          <template #default="{ row }">
            <el-tag :type="row.totalQty > 0 ? 'success' : 'danger'">{{ row.totalQty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="binSummary" label="分布储位" min-width="200" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const query = ref({ page: 1, size: 10, keyword: '' })
const tableData = ref([])
const total = ref(0)

const loadData = async () => {
  try {
    const res = await request.get('/api/inventory/page', { params: query.value })
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

const handleZeroStockDelete = async (inventoryId) => {
  try {
    await ElMessageBox.confirm('确定将该零库存记录移入回收站吗？30天内可恢复。', '零库存删除', {
      type: 'warning'
    })
    await request.delete(`/api/inventory/${inventoryId}`)
    ElMessage.success('已移入回收站')
    loadData()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err.message || '删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.inventory-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 10px; }
.pagination-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
