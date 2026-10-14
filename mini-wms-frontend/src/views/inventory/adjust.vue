<template>
  <div class="adjust-container">
    <el-card header="库存调整（仅主程序电脑可用）">
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="库存调整将直接修改库存数量，不产生入库/出库单据；每次调整都会永久记录在库存流水中。"
        style="margin-bottom: 16px;"
      />
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px" style="max-width: 560px;">
        <el-form-item label="商品" prop="goodsId">
          <el-select v-model="form.goodsId" filterable placeholder="选择商品" style="width: 100%;" @change="onGoodsChange">
            <el-option v-for="g in goodsList" :key="g.id" :label="goodsLabel(g)" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="储位" prop="binId">
          <el-select v-model="form.binId" filterable placeholder="选择储位" style="width: 100%;">
            <el-option v-for="b in binList" :key="b.id"
              :label="`${b.code}${b.name ? ' · ' + b.name : ''}（当前 ${b.qty}）`" :value="b.id">
              <span>{{ b.code }}{{ b.name ? ' · ' + b.name : '' }}（当前 {{ b.qty }}）</span>
              <span v-if="binLoc(b)" class="bin-loc">{{ binLoc(b) }}</span>
            </el-option>
          </el-select>
          <div class="tip">仅列出该商品有库存的储位</div>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag type="info" size="large">{{ currentQty }}</el-tag>
        </el-form-item>
        <el-form-item label="调整方式" prop="adjustType">
          <el-radio-group v-model="form.adjustType">
            <el-radio value="SET">调整为指定数量</el-radio>
            <el-radio value="INCREASE">增加数量</el-radio>
            <el-radio value="DECREASE">减少数量</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.adjustType === 'SET' ? '调整后数量' : '调整数量'" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" />
        </el-form-item>
        <el-form-item label="调整后库存">
          <el-tag :type="afterQty < 0 ? 'danger' : 'success'" size="large">{{ afterQty }}</el-tag>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择原因" style="width: 100%;">
            <el-option v-for="r in reasons" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" :loading="submitting" @click="handleSubmit">确认调整</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const formRef = ref(null)
const goodsList = ref([])
const binList = ref([]) // { id, code, name, qty }
const submitting = ref(false)
const reasons = ['盘点差异', '录入错误', '损耗', '丢失', '赠品补录', '其他']
const form = ref({ goodsId: null, binId: null, adjustType: 'SET', quantity: 0, reason: '盘点差异', remark: '' })
const rules = {
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  binId: [{ required: true, message: '请选择储位', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择原因', trigger: 'change' }]
}

const currentQty = computed(() => {
  const b = binList.value.find((x) => x.id === form.value.binId)
  return b ? b.qty : 0
})
const afterQty = computed(() => {
  const q = form.value.quantity || 0
  if (form.value.adjustType === 'INCREASE') return currentQty.value + q
  if (form.value.adjustType === 'DECREASE') return currentQty.value - q
  return q
})

const goodsLabel = (g) => [g.barcode, g.name, g.code].filter(Boolean).join(' · ')
const binLoc = (b) => [b.warehouseName, b.areaName, b.shelfName].filter(Boolean).join(' / ')

const loadGoods = async () => {
  try {
    const g = await request.get('/api/goods/page', { params: { page: 1, size: 999 } })
    goodsList.value = g?.records || g?.rows || []
  } catch (e) { console.error('加载商品失败', e) }
}

const onGoodsChange = async () => {
  form.value.binId = null
  binList.value = []
  if (!form.value.goodsId) return
  try {
    const bins = await request.get('/api/inventory/bins', { params: { goodsId: form.value.goodsId } })
    binList.value = (bins || []).map((b) => ({
      id: b.binId, code: b.binCode, name: b.binName, qty: b.qty,
      warehouseName: b.warehouseName, areaName: b.areaName, shelfName: b.shelfName
    }))
  } catch (e) { console.error('加载储位失败', e) }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch { return }
  const after = afterQty.value
  if (after < 0) return ElMessage.error('调整后库存不能为负')
  const goods = goodsList.value.find((g) => g.id === form.value.goodsId)
  const bin = binList.value.find((b) => b.id === form.value.binId)
  try {
    await ElMessageBox.confirm(
      `商品：${goods ? (goods.name || '') + (goods.barcode ? '（' + goods.barcode + '）' : '') : '-'}\n` +
      `储位：${bin ? bin.code + (binLoc(bin) ? '（' + binLoc(bin) + '）' : '') : '-'}\n` +
      `当前库存：${currentQty.value}\n` +
      `调整后库存：${after}\n` +
      `调整原因：${form.value.reason}\n\n` +
      '此操作将永久记录在日志中，确认执行？',
      '库存调整确认',
      { type: 'warning', confirmButtonText: '确认调整', cancelButtonText: '取消' }
    )
  } catch { return }

  submitting.value = true
  try {
    const goodsId = form.value.goodsId
    await request.post('/api/inventory/adjust', { ...form.value, deviceInfo: 'PC' })
    ElMessage.success('库存调整成功')
    form.value = { goodsId, binId: null, adjustType: 'SET', quantity: 0, reason: '盘点差异', remark: '' }
    await onGoodsChange()
  } catch (err) {
    ElMessage.error(err.message || '调整失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadGoods)
</script>

<style scoped>
.adjust-container { padding: 20px; }
.tip { font-size: 12px; color: var(--el-text-color-secondary); line-height: 1.6; }
.bin-loc { float: right; color: var(--el-text-color-secondary); font-size: 12px; }
</style>