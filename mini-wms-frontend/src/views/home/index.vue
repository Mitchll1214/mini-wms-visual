<template>
  <div class="dash">
    <!-- 统计带 -->
    <section class="stat-band">
      <div v-for="s in stats" :key="s.label" class="stat-cell">
        <span class="stat-num">{{ s.value }}</span>
        <span class="stat-label">{{ s.label }}</span>
      </div>
    </section>

    <!-- 功能入口（按角色过滤） -->
    <section class="panel">
      <p class="panel-title">快捷入口</p>
      <div class="quick-grid">
        <button v-for="item in visibleFunctions" :key="item.path" class="quick-card" @click="goTo(item.path)">
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
          <span>{{ item.name }}</span>
        </button>
      </div>
    </section>

    <!-- 最近操作日志 -->
    <section class="two-col">
      <div class="panel">
        <div class="panel-head">
          <p class="panel-title">最近操作日志</p>
          <button class="link-btn" @click="$router.push('/system/logs')">查看全部 →</button>
        </div>
        <el-table :data="recentLogs" style="width: 100%" empty-text="暂无操作日志">
          <el-table-column prop="createdAt" label="时间" width="170" />
          <el-table-column prop="operator" label="操作人" width="110" />
          <el-table-column prop="detail" label="详情" min-width="160" show-overflow-tooltip />
          <el-table-column prop="module" label="模块" width="100">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.module || '系统' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Box, Goods, User, TrendCharts, ArrowDown, ArrowUp, Document, Delete, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const stats = ref([
  { key: 'totalGoods', label: '商品总数', value: 0 },
  { key: 'totalWarehouses', label: '仓库数量', value: 0 },
  { key: 'totalInventory', label: '总库存数量', value: 0 },
  { key: 'pendingStock', label: '待处理单据', value: 0 }
])

const allFunctions = [
  { name: '仓储管理', path: '/warehouse', icon: Box, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
  { name: '商品管理', path: '/goods', icon: Goods, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
  { name: '供应商', path: '/supplier', icon: User, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
  { name: '库存总览', path: '/inventory', icon: TrendCharts, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
  { name: '入库管理', path: '/stock-in', icon: ArrowDown, roles: ['ADMIN', 'OPERATOR'] },
  { name: '出库管理', path: '/stock-out', icon: ArrowUp, roles: ['ADMIN', 'OPERATOR'] },
  { name: '回收站', path: '/recycle', icon: Delete, roles: ['ADMIN'] },
  { name: '系统设置', path: '/system', icon: Setting, roles: ['ADMIN'] }
]

const visibleFunctions = computed(() => {
  const role = userStore.role || 'OPERATOR'
  return allFunctions.filter((f) => f.roles.includes(role))
})

const recentLogs = ref([])

const loadStats = async () => {
  try {
    const res = await request.get('/api/system/stats')
    if (res) {
      stats.value.forEach((s) => { s.value = res[s.key] ?? 0 })
    }
  } catch (e) { /* 无统计接口时保持 0 */ }
}

const loadRecentLogs = async () => {
  try {
    const res = await request.get('/api/system/logs?page=1&size=5')
    recentLogs.value = res?.records || res?.rows || res?.data || []
  } catch (e) { /* 忽略 */ }
}

const goTo = (path) => router.push(path)

onMounted(() => { loadStats(); loadRecentLogs() })
</script>

<style scoped>
.dash { display: flex; flex-direction: column; gap: var(--space-6); }

/* 统计带 */
.stat-band {
  display: grid; grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
}
.stat-cell {
  display: flex; flex-direction: column; gap: var(--space-1);
  padding: var(--space-5);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}
.stat-num { font-size: var(--text-2xl); font-weight: 700; letter-spacing: -0.01em; }
.stat-label { font-size: var(--text-xs); color: var(--color-ink-faint); letter-spacing: 0.04em; }

/* 面板 */
.panel {
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-5);
}
.panel-title { margin: 0 0 var(--space-4); font-weight: 600; color: var(--color-ink-soft); font-size: var(--text-sm); letter-spacing: 0.02em; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4); }
.panel-head .panel-title { margin: 0; }
.link-btn {
  border: none; background: none; color: var(--color-accent); cursor: pointer;
  font-size: var(--text-sm); padding: 0;
}

/* 快捷入口 */
.quick-grid {
  display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: var(--space-3);
}
.quick-card {
  display: flex; flex-direction: column; align-items: center; gap: var(--space-2);
  padding: var(--space-5) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-paper);
  color: var(--color-ink-soft);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: border-color var(--dur-fast) var(--ease-out),
    transform var(--dur-fast) var(--ease-out), box-shadow var(--dur-fast) var(--ease-out);
}
.quick-card:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
  transform: translateY(-1px);
  box-shadow: var(--shadow-2);
}
.quick-card:active { transform: translateY(0); }
.quick-card .el-icon { color: var(--color-accent); }

.two-col { display: grid; grid-template-columns: minmax(0, 2fr) minmax(0, 1fr); gap: var(--space-4); }
.phone-tips { line-height: 1.8; color: var(--color-ink-soft); font-size: var(--text-md); }
.lan-ip { color: var(--color-ink-faint); font-size: var(--text-sm); font-family: var(--font-mono); }

.qr-code-container { text-align: center; margin-top: var(--space-4); }
.qr-code-img { width: 200px; height: 200px; border: 1px solid var(--color-border); padding: var(--space-2); border-radius: var(--radius-md); }
.qr-text { margin-top: var(--space-3); color: var(--color-ink-soft); font-size: var(--text-sm); }
.qr-url { font-size: var(--text-xs); word-break: break-all; color: var(--color-accent); }

@media (max-width: 900px) {
  .stat-band { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .quick-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .two-col { grid-template-columns: 1fr; }
}
</style>