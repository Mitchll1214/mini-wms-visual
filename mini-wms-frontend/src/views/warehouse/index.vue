<template>
  <div class="warehouse-container">
    <!-- ============ 视图 A：仓库列表 ============ -->
    <el-card v-if="!currentWarehouse">
      <template #header>
        <div class="card-header">
          <span>仓库管理</span>
          <el-button type="primary" size="small" @click="openWarehouseDialog()">新建仓库</el-button>
        </div>
      </template>

      <el-table :data="warehouses" border style="width: 100%">
        <el-table-column prop="name" label="仓库名称" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column prop="remark" label="备注" min-width="160" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="enterWarehouse(row)">储位管理</el-button>
            <el-button link type="primary" size="small" @click="openWarehouseDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该仓库吗？" @confirm="deleteWarehouse(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="warehouses.length === 0" class="empty-state">暂无仓库，请点击右上角新建</div>
    </el-card>

    <!-- ============ 视图 B：储位管理 ============ -->
    <el-card v-else>
      <template #header>
        <div class="card-header">
          <span>
            <el-button link type="primary" size="small" @click="backToWarehouses">← 返回</el-button>
            <strong>储位管理 · {{ currentWarehouse.name }}</strong>
          </span>
          <div>
            <el-button size="small" @click="openWarehouseDialog(currentWarehouse)">编辑仓库</el-button>
            <el-button size="small" type="primary" @click="openAreaDialog()">新建库区</el-button>
          </div>
        </div>
      </template>

      <div v-if="areas.length === 0" class="empty-state">暂无库区，请先点击「新建库区」</div>

      <div v-for="area in areas" :key="area.id" class="area-block">
        <div class="area-header">
          <el-tag size="small" type="info">库区</el-tag>
          <span class="area-name">{{ area.name }}</span>
          <span class="area-remark">{{ area.remark }}</span>
          <span class="area-actions">
            <el-button link type="primary" size="small" @click="openShelfDialog(area)">新建货架</el-button>
            <el-popconfirm title="确定删除该库区？（其下货架与储位一并删除）" @confirm="deleteArea(area.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </span>
        </div>

        <div v-for="shelf in shelvesByArea[area.id] || []" :key="shelf.id" class="shelf-block">
          <div class="shelf-header">
            <el-tag size="small" type="success">货架</el-tag>
            <span class="shelf-name">{{ shelf.name }}</span>
            <span class="shelf-remark">{{ shelf.remark }}</span>
            <span class="shelf-actions">
              <el-button link type="primary" size="small" @click="openBinDialog(area, shelf)">新建储位</el-button>
              <el-popconfirm title="确定删除该货架？（其下储位一并删除）" @confirm="deleteShelf(shelf.id)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </span>
          </div>

          <div class="bin-list">
            <div v-if="(binsByShelf[shelf.id] || []).length === 0" class="bin-empty">暂无储位</div>
            <div v-for="bin in binsByShelf[shelf.id] || []" :key="bin.id" class="bin-item">
              <el-tag
                size="small"
                :type="bin.status === 'DISABLED' ? 'info' : bin.status === 'OCCUPIED' ? 'warning' : 'success'"
              >
                {{ bin.code }}{{ bin.name ? ' · ' + bin.name : '' }}
              </el-tag>
              <el-popconfirm title="确定删除该储位？" @confirm="deleteBin(shelf.id, bin.id)">
                <template #reference>
                  <el-button link type="danger" size="small" :icon="Delete" />
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 仓库表单 -->
    <el-dialog v-model="warehouseDialogVisible" :title="warehouseForm.id ? '编辑仓库' : '新建仓库'" width="500px">
      <el-form :model="warehouseForm" :rules="warehouseRules" ref="warehouseFormRef" label-width="80px">
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="warehouseForm.name" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="warehouseForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="warehouseForm.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="warehouseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWarehouse">确定</el-button>
      </template>
    </el-dialog>

    <!-- 库区表单 -->
    <el-dialog v-model="areaDialogVisible" title="新建库区" width="480px">
      <el-form :model="areaForm" :rules="areaRules" label-width="80px">
        <el-form-item label="库区名称" prop="name">
          <el-input v-model="areaForm.name" placeholder="例如：A区 / 一楼东区" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="areaForm.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="areaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitArea">确定</el-button>
      </template>
    </el-dialog>

    <!-- 货架表单 -->
    <el-dialog v-model="shelfDialogVisible" :title="`新建货架（${shelfForm.areaName || ''}）`" width="480px">
      <el-form :model="shelfForm" :rules="shelfRules" label-width="80px">
        <el-form-item label="货架名称" prop="name">
          <el-input v-model="shelfForm.name" placeholder="例如：A-01" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="shelfForm.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shelfDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShelf">确定</el-button>
      </template>
    </el-dialog>

    <!-- 储位表单 -->
    <el-dialog v-model="binDialogVisible" :title="`新建储位（${binForm.shelfName || ''}）`" width="480px">
      <el-form :model="binForm" :rules="binRules" label-width="80px">
        <el-form-item label="储位编码" prop="code">
          <el-input v-model="binForm.code" placeholder="例如：A-01-01（全局唯一）" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="binForm.name" placeholder="选填" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="binForm.status" style="width: 100%">
            <el-option label="空闲 FREE" value="FREE" />
            <el-option label="占用 OCCUPIED" value="OCCUPIED" />
            <el-option label="停用 DISABLED" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="binDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBin">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

// ---------- 视图 A：仓库列表 ----------
const warehouses = ref([])
const currentWarehouse = ref(null)

const loadWarehouses = async () => {
  try {
    const res = await request.get('/api/warehouse/list')
    warehouses.value = res || []
  } catch (err) {
    console.error(err)
  }
}

// ---------- 视图 B：储位管理（分层） ----------
const areas = ref([])
const shelvesByArea = ref({})
const binsByShelf = ref({})

const enterWarehouse = async (row) => {
  currentWarehouse.value = row
  areas.value = []
  shelvesByArea.value = {}
  binsByShelf.value = {}
  await loadAreas()
}

const backToWarehouses = () => {
  currentWarehouse.value = null
  loadWarehouses()
}

const loadAreas = async () => {
  try {
    const res = await request.get('/api/area/list', { params: { warehouseId: currentWarehouse.value.id } })
    areas.value = res || []
    // 并行加载每个库区的货架
    await Promise.all(areas.value.map(loadShelves))
  } catch (err) {
    console.error(err)
  }
}

const loadShelves = async (area) => {
  try {
    const res = await request.get('/api/shelf/list', { params: { areaId: area.id } })
    shelvesByArea.value[area.id] = res || []
    await Promise.all((res || []).map(loadBins))
  } catch (err) {
    console.error(err)
  }
}

const loadBins = async (shelf) => {
  try {
    const res = await request.get('/api/bin/list', { params: { shelfId: shelf.id } })
    binsByShelf.value[shelf.id] = res || []
  } catch (err) {
    console.error(err)
  }
}

// ---------- 仓库表单 ----------
const warehouseDialogVisible = ref(false)
const warehouseFormRef = ref(null)
const warehouseForm = ref({ id: null, name: '', address: '', remark: '' })
const warehouseRules = { name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }] }

const openWarehouseDialog = (row) => {
  warehouseForm.value = row ? { id: row.id, name: row.name, address: row.address, remark: row.remark } : { id: null, name: '', address: '', remark: '' }
  warehouseDialogVisible.value = true
}

const submitWarehouse = async () => {
  try {
    if (warehouseForm.value.id) {
      await request.put('/api/warehouse', warehouseForm.value)
      ElMessage.success('仓库已更新')
    } else {
      await request.post('/api/warehouse', warehouseForm.value)
      ElMessage.success('仓库创建成功')
    }
    warehouseDialogVisible.value = false
    if (currentWarehouse.value?.id === warehouseForm.value.id) {
      currentWarehouse.value = { ...currentWarehouse.value, ...warehouseForm.value }
    }
    loadWarehouses()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  }
}

const deleteWarehouse = async (id) => {
  try {
    await request.delete(`/api/warehouse/${id}`)
    ElMessage.success('已删除')
    loadWarehouses()
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

// ---------- 库区表单 ----------
const areaDialogVisible = ref(false)
const areaForm = ref({ name: '', remark: '' })
const areaRules = { name: [{ required: true, message: '请输入库区名称', trigger: 'blur' }] }

const openAreaDialog = () => {
  areaForm.value = { name: '', remark: '' }
  areaDialogVisible.value = true
}

const submitArea = async () => {
  try {
    await request.post('/api/area', { ...areaForm.value, warehouseId: currentWarehouse.value.id })
    ElMessage.success('库区创建成功')
    areaDialogVisible.value = false
    loadAreas()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  }
}

const deleteArea = async (id) => {
  try {
    await request.delete(`/api/area/${id}`)
    ElMessage.success('已删除')
    loadAreas()
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

// ---------- 货架表单 ----------
const shelfDialogVisible = ref(false)
const shelfForm = ref({ areaId: null, areaName: '', name: '', remark: '' })
const shelfRules = { name: [{ required: true, message: '请输入货架名称', trigger: 'blur' }] }

const openShelfDialog = (area) => {
  shelfForm.value = { areaId: area.id, areaName: area.name, name: '', remark: '' }
  shelfDialogVisible.value = true
}

const submitShelf = async () => {
  try {
    await request.post('/api/shelf', { name: shelfForm.value.name, remark: shelfForm.value.remark, areaId: shelfForm.value.areaId })
    ElMessage.success('货架创建成功')
    shelfDialogVisible.value = false
    const area = areas.value.find((a) => a.id === shelfForm.value.areaId)
    if (area) loadShelves(area)
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  }
}

const deleteShelf = async (id) => {
  try {
    await request.delete(`/api/shelf/${id}`)
    ElMessage.success('已删除')
    loadAreas()
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

// ---------- 储位表单 ----------
const binDialogVisible = ref(false)
const binForm = ref({ shelfId: null, shelfName: '', code: '', name: '', status: 'FREE' })
const binRules = { code: [{ required: true, message: '请输入储位编码', trigger: 'blur' }] }

const openBinDialog = (area, shelf) => {
  binForm.value = { shelfId: shelf.id, shelfName: `${area.name}/${shelf.name}`, code: '', name: '', status: 'FREE' }
  binDialogVisible.value = true
}

const submitBin = async () => {
  try {
    await request.post('/api/bin', { code: binForm.value.code, name: binForm.value.name, status: binForm.value.status, shelfId: binForm.value.shelfId })
    ElMessage.success('储位创建成功')
    binDialogVisible.value = false
    const shelf = (Object.values(shelvesByArea.value).flat() || []).find((s) => s.id === binForm.value.shelfId)
    if (shelf) loadBins(shelf)
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  }
}

const deleteBin = async (shelfId, binId) => {
  try {
    await request.delete(`/api/bin/${binId}`)
    ElMessage.success('已删除')
    const shelf = (Object.values(shelvesByArea.value).flat() || []).find((s) => s.id === shelfId)
    if (shelf) loadBins(shelf)
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

onMounted(loadWarehouses)
</script>

<style scoped>
.warehouse-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.empty-state { text-align: center; padding: 40px 20px; color: #909399; }

.area-block { border: 1px solid var(--el-border-color-light); border-radius: 8px; padding: 12px; margin-bottom: 16px; }
.area-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.area-name { font-weight: 600; }
.area-remark { color: #909399; font-size: 12px; flex: 1; }
.area-actions { flex-shrink: 0; }

.shelf-block { border-left: 3px solid var(--el-color-success-light-5); background: var(--el-fill-color-lighter); border-radius: 6px; padding: 10px 12px; margin: 8px 0 8px 8px; }
.shelf-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.shelf-name { font-weight: 600; }
.shelf-remark { color: #909399; font-size: 12px; flex: 1; }
.shelf-actions { flex-shrink: 0; }

.bin-list { display: flex; flex-wrap: wrap; gap: 8px; margin-left: 12px; }
.bin-item { display: inline-flex; align-items: center; gap: 2px; }
.bin-empty { color: #909399; font-size: 12px; }
</style>