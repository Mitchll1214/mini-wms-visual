<template>
  <div class="bin-inventory-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>储位视图（按储位查看库存）</span>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="选择储位ID">
          <el-input-number v-model="query.binId" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询储位货品</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%" empty-text="当前储位暂无商品库存">
        <el-table-column prop="goodsCode" label="商品编码" width="160" />
        <el-table-column prop="goodsName" label="商品名称" min-width="180" />
        <el-table-column prop="spec" label="规格" width="130" />
        <el-table-column prop="qty" label="在库数量" width="120">
          <template #default="{ row }">
            <el-tag type="success">{{ row.qty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="最后变动时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const query = ref({ binId: 1 })
const tableData = ref([])

const loadData = async () => {
  if (!query.value.binId) return
  try {
    const res = await request.get(`/api/inventory/bin/${query.value.binId}`)
    tableData.value = res || []
  } catch (err) {
    console.error(err)
  }
}

onMounted(loadData)
</script>

<style scoped>
.bin-inventory-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 10px; }
</style>
