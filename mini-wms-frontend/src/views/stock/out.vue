<template>
  <div class="stock-out-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>出库管理</span>
          <el-button type="primary" size="small" @click="handleOpenDialog">新建出库单</el-button>
        </div>
      </template>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="orderNo" label="出库单号" width="180" />
        <el-table-column prop="outType" label="出库类型" width="130">
          <template #default="{ row }"><el-tag type="danger">{{ row.outType }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'AUDITED' ? 'success' : 'warning'">
              {{ row.status === 'AUDITED' ? '已审核' : '新建' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="明细" width="90">
          <template #default="{ row }">{{ (row.items || []).length }} 条</template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleShowDetail(row)">详情</el-button>
            <el-button v-if="row.status !== 'AUDITED'" link type="success" size="small" @click="handleAudit(row)">审核</el-button>
            <el-popconfirm v-if="row.status !== 'AUDITED'" title="确定撤销该出库单吗？" @confirm="handleCancel(row.id)">
              <template #reference><el-button link type="danger" size="small">撤销</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination background layout="total, prev, pager, next" :current-page="query.page"
          :page-size="query.size" :total="total" @current-change="onPageChange" />
      </div>
    </el-card>

    <!-- 新建 -->
    <el-dialog v-model="dialogVisible" title="新建出库单" width="680px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="出库类型">
          <el-select v-model="form.outType" style="width: 100%;">
            <el-option label="销售出库" value="销售出库" /><el-option label="领用出库" value="领用出库" />
            <el-option label="调拨出库" value="调拨出库" /><el-option label="报损出库" value="报损出库" />
            <el-option label="盘亏出库" value="盘亏出库" /><el-option label="其他出库" value="其他出库" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库明细">
          <el-table :data="form.items" size="small" border>
            <el-table-column label="商品" min-width="190">
              <template #default="scope">
                <el-select v-model="scope.row.goodsId" filterable placeholder="选择商品" style="width: 100%;" @change="onGoodsChange(scope.row)">
                  <el-option v-for="g in goodsList" :key="g.id" :label="goodsLabel(g)" :value="g.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="储位" min-width="180">
              <template #default="scope">
                <el-select v-model="scope.row.binId" filterable placeholder="选择储位" style="width: 100%;">
                  <el-option v-for="b in binList" :key="b.id" :label="binLabel(b)" :value="b.id">
                    <span>{{ binLabel(b) }}</span>
                    <span v-if="binLoc(b)" class="bin-loc">{{ binLoc(b) }}</span>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.qty" :min="1" size="small" style="width: 100%;" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="scope">
                <el-button link type="danger" size="small" @click="form.items.splice(scope.$index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 5px;">
            <el-button size="small" @click="form.items.push({ goodsId: '', binId: '', qty: 1 })">+ 添加明细</el-button>
            <el-button size="small" type="warning" plain :disabled="!form.items.length || !form.items[0].goodsId" @click="handleGetSuggest">获取拆分方案</el-button>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交（新建状态，待审核）</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="出库单详情" width="640px">
      <el-descriptions :column="2" border size="small" v-if="detail">
        <el-descriptions-item label="单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.outType }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 'AUDITED' ? 'success' : 'warning'">{{ detail.status === 'AUDITED' ? '已审核' : '新建' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.auditBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detail?.items || []" size="small" border style="margin-top: 12px;">
        <el-table-column label="商品" min-width="180">
          <template #default="scope">{{ goodsMap[scope.row.goodsId] || `#${scope.row.goodsId}` }}</template>
        </el-table-column>
        <el-table-column label="储位" width="140">
          <template #default="scope">{{ binIdMap[scope.row.binId] || `#${scope.row.binId}` }}</template>
        </el-table-column>
        <el-table-column prop="qty" label="数量" width="100" />
      </el-table>
      <template #footer>
        <el-button v-if="detail && detail.status !== 'AUDITED'" type="success" @click="handleAudit(detail)">审核（确认出库）</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const query = ref({ page: 1, size: 10 })
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const form = ref({ outType: '销售出库', remark: '', items: [{ goodsId: '', binId: '', qty: 1 }] })
const goodsList = ref([])
const binList = ref([])          // { id, code, label, qty }
const goodsMap = ref({})
const binIdMap = ref({})

const loadData = async () => {
  try {
    const res = await request.get('/api/stock-out/page', { params: query.value })
    tableData.value = res?.records || res?.rows || []
    total.value = res?.total || 0
  } catch (err) { console.error(err) }
}

const loadOptions = async () => {
  // 各自独立容错，避免任一接口失败导致整个储位下拉为空
  try {
    const g = await request.get('/api/goods/page', { params: { page: 1, size: 999 } })
    goodsList.value = g?.records || g?.rows || []
    goodsMap.value = Object.fromEntries(goodsList.value.map(x => [x.id, `${x.name || ''}${x.barcode ? '（' + x.barcode + '）' : ''}`]))
  } catch (err) { console.error('加载商品选项失败', err) }
  try {
    const b = await request.get('/api/bin/tree')
    binList.value = b || []
    binIdMap.value = Object.fromEntries(binList.value.map(x => [x.id, x.code]))
  } catch (err) { console.error('加载储位选项失败', err) }
}

const goodsLabel = (g) => `${g.name || ''}${g.barcode ? '（' + g.barcode + '）' : ''}`
const binLabel = (b) => `${b.code || ''}${b.name ? ' · ' + b.name : ''}`
const binLoc = (b) => [b.warehouseName, b.areaName, b.shelfName].filter(Boolean).join(' / ')

const onGoodsChange = async (row) => {
  // 商品变化时带出该商品有库存的储位（含库存）
  try {
    const bins = await request.get('/api/stock-out/bins', { params: { goodsId: row.goodsId } })
    binList.value = (bins || []).map(b => ({ id: b.binId, code: b.binCode, name: `库存 ${b.qty}`, qty: b.qty }))
    binIdMap.value = Object.fromEntries(binList.value.map(x => [x.id, x.code]))
  } catch (e) { console.error(e) }
}

const handleGetSuggest = async () => {
  const row = form.value.items[0]
  if (!row.goodsId) return
  try {
    const totalQty = form.value.items.reduce((s, i) => s + (i.qty || 0), 0)
    const res = await request.post('/api/stock-out/suggest', { goodsId: row.goodsId, totalQty })
    if (res?.items?.length) {
      form.value.items = res.items.map(i => ({ goodsId: row.goodsId, binId: i.binId, qty: i.suggestQty }))
      ElMessage.success('已生成推荐拆分方案')
    } else ElMessage.warning('库存不足或无法拆分')
  } catch (err) { ElMessage.error(err.message || '获取拆分失败') }
}

const onPageChange = (p) => { query.value.page = p; loadData() }
const handleOpenDialog = async () => {
  form.value = { outType: '销售出库', remark: '', items: [{ goodsId: '', binId: '', qty: 1 }] }
  dialogVisible.value = true
  // 每次打开都刷新基础选项，确保刚在仓库管理新建的储位能立即带出
  await loadOptions()
}
const handleSubmit = async () => {
  if (!form.value.items.length || form.value.items.some(i => !i.goodsId || !i.binId)) {
    return ElMessage.warning('请完整填写明细的商品、储位和数量')
  }
  try {
    await request.post('/api/stock-out', form.value)
    ElMessage.success('出库单已创建（新建状态），请审核后出库')
    dialogVisible.value = false
    loadData()
  } catch (err) { ElMessage.error(err.message || '提交失败') }
}
const handleShowDetail = async (row) => {
  try {
    const res = await request.get(`/api/stock-out/${row.id}`)
    detail.value = res
    detailVisible.value = true
  } catch (err) { ElMessage.error(err.message || '加载失败') }
}
const handleAudit = async (row) => {
  try {
    await ElMessageBox.confirm('审核后立即扣减对应库存并写入流水，确定执行吗？', '审核确认', { type: 'warning' })
    await request.post(`/api/stock-out/${row.id}/audit`)
    ElMessage.success('已审核，库存已扣减')
    detailVisible.value = false
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '审核失败') }
}
const handleCancel = async (id) => {
  try {
    await request.post(`/api/stock-out/${id}/cancel`)
    ElMessage.success('已撤销')
    loadData()
  } catch (err) { ElMessage.error(err.message || '撤销失败') }
}

onMounted(() => { loadData(); loadOptions() })
</script>

<style scoped>
.stock-out-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }
.bin-loc { float: right; color: #909399; font-size: 12px; }
</style>