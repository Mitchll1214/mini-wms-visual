<template>
  <div class="system-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置与数据备份</span>
        </div>
      </template>

      <el-form label-width="120px" style="max-width: 600px;">
        <el-form-item label="局域网访问IP">
          <el-input :value="lanIp" readonly />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            局域网其他设备请访问：http://{{ lanIp }}:8080
          </div>
        </el-form-item>

        <el-divider />

        <el-form-item label="一键数据备份">
          <el-button type="primary" :loading="backingUp" @click="handleBackup">
            立即生成完整数据备份
          </el-button>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            备份包含 SQLite 数据库及全部商品原图、缩略图，备份文件存储于 data/backup/ 目录。
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const lanIp = ref('127.0.0.1')
const backingUp = ref(false)

const loadLanIp = async () => {
  try {
    const res = await request.get('/api/system/lan-ip')
    lanIp.value = res || window.location.hostname
  } catch (err) {
    lanIp.value = window.location.hostname
  }
}

const handleBackup = async () => {
  backingUp.value = true
  try {
    const res = await request.post('/api/system/backup')
    ElMessage.success('备份成功！已归档至 data/backup 目录')
  } catch (err) {
    ElMessage.error(err.message || '备份失败')
  } finally {
    backingUp.value = false
  }
}

onMounted(loadLanIp)
</script>

<style scoped>
.system-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
