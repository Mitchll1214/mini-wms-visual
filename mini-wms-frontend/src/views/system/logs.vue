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
          <el-input v-model="query.module" placeholder="操作模块" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column label="模块" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :type="moduleTagType(row.module)">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="动作" width="110">
          <template #default="{ row }">
            <span class="action-text">{{ row.action }}</span>
          </template>
        </el-table-column>
        <el-table-column label="详细说明" min-width="240">
          <template #default="{ row }">
            <div v-if="row.detail" class="detail-chips">
              <span v-for="(part, i) in detailParts(row.detail)" :key="i" class="chip">{{ part }}</span>
            </div>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="deviceInfo" label="操作 IP" width="140" fixed="right" />
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

// 模块 → 标签色，便于快速识别操作领域
const MODULE_TAG = {
  入库管理: 'success', 出库管理: 'danger', 库存调整: 'warning', 库存管理: 'warning',
  商品管理: 'primary', 商品类别: 'info', 供应商管理: 'info', 供应商图片: 'info',
  仓库管理: 'primary', 库区管理: 'info', 货架管理: 'info', 储位管理: 'primary',
  回收站: 'danger', 商品图片: 'info', 系统管理: 'info', 认证: 'success',
  用户管理: 'danger', 其他: 'info'
}
const moduleTagType = (m) => MODULE_TAG[m] || 'info'
// 详细说明按「 · 」拆成可读小标签
const detailParts = (detail) => (detail ? detail.split(' · ').filter(Boolean) : [])

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
.action-text { font-weight: 500; color: var(--color-ink); }
.detail-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
  display: inline-block; padding: 1px 8px; border-radius: 999px;
  background: var(--color-paper-2); border: 1px solid var(--color-border);
  font-size: 12px; color: var(--color-ink-soft); white-space: nowrap;
}
.muted { color: var(--color-ink-faint); }
</style>
