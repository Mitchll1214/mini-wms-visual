<template>
  <div class="goods-detail-container">
    <el-page-header @back="$router.back()" title="返回列表" :content="goods.name || '商品详情'" />

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card header="商品基本信息">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="商品名称">{{ goods.name }}</el-descriptions-item>
            <el-descriptions-item label="规格型号">{{ goods.spec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="计量单位">{{ goods.unit || '-' }}</el-descriptions-item>
            <el-descriptions-item label="条形码">{{ goods.barcode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="goods.status === 'ENABLED' ? 'success' : 'danger'">
                {{ goods.status === 'ENABLED' ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="备注">{{ goods.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card header="储位分布与库存">
          <el-table :data="inventoryList" border style="width: 100%" empty-text="暂无储位库存">
            <el-table-column label="存放位置" min-width="220">
              <template #default="{ row }">
                <span class="loc-text">{{ locationText(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="储位编码" width="140">
              <template #default="{ row }">
                <el-tooltip :content="fullLocation(row)" placement="top">
                  <span class="bin-code">{{ row.binCode || '-' }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="binName" label="储位名称" width="120" />
            <el-table-column prop="qty" label="库存数量" width="100">
              <template #default="{ row }">
                <el-tag type="info">{{ row.qty }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图片与标记展示 -->
    <el-card header="商品与储位摆放图片" style="margin-top: 20px;">
      <div class="upload-bar">
        <el-select v-model="uploadImageType" size="small" style="width: 110px; margin-right: 8px;">
          <el-option label="商品图" value="goods" />
          <el-option label="储位图" value="bin" />
        </el-select>
        <el-upload
          :show-file-list="false"
          :http-request="handleUploadRequest"
          :before-upload="beforeUpload"
          accept="image/*"
        >
          <el-button type="primary" size="small">上传摆放图片</el-button>
        </el-upload>
      </div>

      <el-empty v-if="images.length === 0" description="暂无商品或储位图片" />
      <el-row :gutter="16" v-else>
        <el-col :xs="12" :sm="8" :md="6" v-for="img in images" :key="img.id">
          <el-card :body-style="{ padding: '0px' }" shadow="hover">
            <el-image
              style="width: 100%; height: 180px; object-fit: cover;"
              :src="`/api/images/${img.id}/thumb`"
              :preview-src-list="[`/api/images/${img.id}/content`]"
              fit="cover"
            />
            <div style="padding: 10px; font-size: 13px;">
              <span style="color: #606266;">{{ img.imageType === 'bin' ? '储位图' : '商品图' }}</span>
              <el-button link type="danger" size="small" style="float: right;" @click="handleDeleteImage(img.id)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const goodsId = route.params.id

const goods = ref({})
const inventoryList = ref([])
const images = ref([])
const uploadImageType = ref('goods')

// 仓库 / 库区 / 货架 位置文本
const locationText = (row) =>
  [row.warehouseName, row.areaName, row.shelfName].filter(Boolean).join(' / ') || '—'
// 悬浮完整位置：仓库 / 库区 / 货架 / 储位
const fullLocation = (row) =>
  [row.warehouseName, row.areaName, row.shelfName, row.binCode ? '储位 ' + row.binCode : null]
    .filter(Boolean).join(' / ') || '—'

const loadDetail = async () => {
  if (!goodsId) return
  try {
    const res = await request.get(`/api/goods/${goodsId}`)
    goods.value = res || {}
  } catch (err) {
    console.error(err)
  }
}

const loadInventory = async () => {
  if (!goodsId) return
  try {
    const res = await request.get(`/api/inventory/goods/${goodsId}`)
    inventoryList.value = res || []
  } catch (err) {
    console.error(err)
  }
}

const loadImages = async () => {
  if (!goodsId) return
  try {
    const [goodsImgs, binImgs] = await Promise.all([
      request.get(`/api/images/${goodsId}/images`, { params: { imageType: 'goods' } }),
      request.get(`/api/images/${goodsId}/images`, { params: { imageType: 'bin' } })
    ])
    images.value = [...(goodsImgs || []), ...(binImgs || [])]
  } catch (err) {
    console.error(err)
  }
}

const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
  }
  return isLt10M
}

const handleUploadRequest = async ({ file }) => {
  try {
    const fd = new FormData()
    fd.append('file', file)
    await request.post(`/api/images/${goodsId}/images?imageType=${uploadImageType.value}`, fd)
    ElMessage.success('上传成功')
    loadImages()
  } catch (err) {
    ElMessage.error(err.message || '上传失败')
  }
}

const handleDeleteImage = async (imageId) => {
  try {
    await request.delete(`/api/images/${imageId}`)
    ElMessage.success('已删除')
    loadImages()
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

onMounted(() => {
  loadDetail()
  loadInventory()
  loadImages()
})
</script>

<style scoped>
.goods-detail-container { padding: 20px; }
.upload-bar { display: flex; align-items: center; margin-bottom: 15px; }
.loc-text { color: var(--color-ink-soft); font-size: 13px; }
.bin-code { border-bottom: 1px dashed var(--color-border-strong); cursor: help; }
</style>
