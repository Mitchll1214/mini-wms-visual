<template>
  <div class="shell" :class="{ 'is-login': isLogin }">
    <aside v-if="!isLogin" class="rail">
      <div class="brand">
        <span class="brand-mark">W</span>
        <span class="brand-name">小仓图管</span>
      </div>
      <nav class="nav-scroll">
        <template v-for="group in menuGroups" :key="group.title">
          <p v-if="group.items.length" class="nav-group">{{ group.title }}</p>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ 'is-active': isActive(item) }"
          >
            <el-icon :size="16"><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </router-link>
        </template>
      </nav>
      <div class="rail-foot">
        <span class="foot-dot" :class="'dot-' + deployMode"></span>
        {{ modeLabel }}
      </div>
    </aside>

    <div class="stage">
      <header v-if="!isLogin" class="topbar">
        <button class="menu-btn" aria-label="菜单" @click="drawer = true">
          <el-icon :size="20"><Menu /></el-icon>
        </button>
        <div class="crumb">{{ route.meta.title || '小仓图管' }}</div>
        <div class="userbox">
          <span class="role-chip" :class="'role-' + roleClass">{{ roleLabel }}</span>
          <span class="uname">{{ username }}</span>
          <button class="logout" @click="handleLogout">退出</button>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>

      <footer v-if="!isLogin" class="app-footer">
        <span>© {{ year }} 小仓图管 MiniWMS Visual · 保留所有权利</span>
        <a class="footer-link" href="https://github.com/Mitchll1214/mini-wms-visual" target="_blank" rel="noopener noreferrer">GitHub 开源地址</a>
      </footer>
    </div>

    <el-drawer v-if="!isLogin" v-model="drawer" direction="ltr" size="264px" :with-header="false">
      <nav class="nav-scroll drawer-nav">
        <router-link :to="'/' " class="drawer-brand" @click="drawer = false">小仓图管</router-link>
        <template v-for="group in menuGroups" :key="group.title">
          <p v-if="group.items.length" class="nav-group">{{ group.title }}</p>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ 'is-active': isActive(item) }"
            @click="drawer = false"
          >
            <el-icon :size="16"><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </router-link>
        </template>
      </nav>
    </el-drawer>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Menu, HomeFilled, Box, Goods, User, TrendCharts, ArrowDown, ArrowUp, Delete, Setting, Document, Picture, Operation } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

/**
 * 按当前部署环境自动判定运行模式：
 *   本机 localhost / 私有网段（10.x / 192.168.x / 172.16-31.x）访问 → 局域网模式（本地单机 / 内网）
 *   其余（公网 IP / 域名）访问 → 云服务器模式
 */
const detectDeployMode = () => {
  const host = (window.location.hostname || '').toLowerCase()
  const isLocalhost = host === 'localhost' || host === '127.0.0.1' || host === '::1' || host === '[::1]'
  const isPrivate = /^10\./.test(host)
    || /^192\.168\./.test(host)
    || /^172\.(1[6-9]|2\d|3[01])\./.test(host)
  return (isLocalhost || isPrivate) ? 'lan' : 'cloud'
}

export default {
  name: 'Layout',
  components: { Menu },
  setup() {
    const userStore = useUserStore()
    const route = useRoute()
    const username = ref(userStore.username || '')
    const drawer = ref(false)
    const role = computed(() => userStore.role || 'OPERATOR')

    const deployMode = ref(detectDeployMode())
    const modeLabel = computed(() => (deployMode.value === 'cloud' ? '云服务器模式' : '局域网模式'))
    const year = new Date().getFullYear()

    const menus = [
      { path: '/', title: '首页', icon: HomeFilled, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
      { path: '/warehouse', title: '仓储管理', icon: Box, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
      { path: '/goods', title: '商品管理', icon: Goods, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
      { path: '/supplier', title: '供应商', icon: User, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
      { path: '/inventory', title: '库存总览', icon: TrendCharts, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
      { path: '/inventory/logs', title: '库存流水', icon: Document, roles: ['ADMIN', 'OPERATOR', 'INSPECTOR'] },
      { path: '/inventory/adjust', title: '库存调整', icon: Operation, roles: ['ADMIN'] },
      { path: '/stock-in', title: '入库管理', icon: ArrowDown, roles: ['ADMIN', 'OPERATOR'] },
      { path: '/stock-out', title: '出库管理', icon: ArrowUp, roles: ['ADMIN', 'OPERATOR'] },
      { path: '/recycle', title: '回收站', icon: Delete, roles: ['ADMIN'] },
      { path: '/system', title: '系统设置', icon: Setting, roles: ['ADMIN'] },
      { path: '/system/images', title: '图片管理', icon: Picture, roles: ['ADMIN'] },
      { path: '/system/users', title: '用户管理', icon: User, roles: ['ADMIN'] },
      { path: '/system/logs', title: '操作日志', icon: Document, roles: ['ADMIN'] }
    ]

    const groups = [
      { title: '工作台', keys: ['/'] },
      { title: '业务', keys: ['/warehouse', '/goods', '/supplier', '/inventory', '/inventory/logs', '/inventory/adjust', '/stock-in', '/stock-out'] },
      { title: '系统', keys: ['/recycle', '/system', '/system/images', '/system/users', '/system/logs'] }
    ]

    const menuGroups = computed(() =>
      groups.map((g) => ({
        title: g.title,
        items: menus.filter((m) => g.keys.includes(m.path) && m.roles.includes(role.value))
      }))
    )

    const isActive = (item) => {
      if (item.path === '/') return route.path === '/'
      if (route.path === item.path) return true
      if (route.path.startsWith(item.path + '/')) {
        // 若存在更具体的菜单项也匹配当前路径（如 /inventory 与 /inventory/logs、/system 与 /system/images），
        // 则让更具体的项高亮，避免父项与子项同时点亮
        const deeper = menus.some(
          (m) => m.path !== item.path && m.path.length > item.path.length &&
            (route.path === m.path || route.path.startsWith(m.path + '/'))
        )
        return !deeper
      }
      return false
    }

    const isLogin = computed(() => route.path === '/login')

    const roleLabel = computed(() => userStore.roleLabel)
    const roleClass = computed(() => (userStore.role || 'OPERATOR').toLowerCase())

    watch(() => userStore.username, (v) => { username.value = v || '' })

    const handleLogout = async () => {
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '系统提示', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
        await request.post('/api/auth/logout')
        userStore.logout()
        window.location.href = '/login'
      } catch (e) { /* 取消 */ }
    }

    return { menuGroups, isActive, isLogin, roleLabel, roleClass, username, drawer, route, handleLogout, userStore, deployMode, modeLabel, year }
  }
}
</script>

<style scoped>
/* Hallmark · macrostructure: Side-Rail Workbench · tone: minimal-premium · anchor hue: 255 · theme: Cobalt */
.shell { display: flex; min-height: 100svh; background: var(--color-paper); }
.shell.is-login { display: block; }
.shell.is-login .stage { min-height: 100svh; }

/* ---- N3 side-rail ---- */
.rail {
  width: 236px;
  flex: 0 0 236px;
  display: flex;
  flex-direction: column;
  background: var(--color-paper);
  border-right: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  height: 100svh;
}
.brand {
  display: flex; align-items: center; gap: var(--space-3);
  padding: var(--space-6) var(--space-5) var(--space-5);
}
.brand-mark {
  width: 30px; height: 30px; border-radius: 8px;
  background: var(--color-accent); color: #fff;
  display: grid; place-items: center;
  font-weight: 700; font-size: var(--text-sm);
  letter-spacing: -0.01em;
  box-shadow: var(--shadow-1);
}
.brand-name { font-weight: 600; font-size: var(--text-lg); letter-spacing: -0.01em; color: var(--color-ink); }

.nav-scroll { flex: 1; overflow-y: auto; padding: var(--space-2) var(--space-3) var(--space-6); }
.nav-group {
  margin: var(--space-5) var(--space-3) var(--space-2);
  font-size: 11px; color: var(--color-ink-faint);
  letter-spacing: 0.12em;
}
.nav-group:first-child { margin-top: var(--space-2); }
.nav-item {
  display: flex; align-items: center; gap: var(--space-3);
  padding: 9px var(--space-3);
  margin-bottom: 2px;
  border-radius: 8px;
  color: var(--color-ink-soft);
  font-size: var(--text-md);
  transition: background-color var(--dur-fast) var(--ease-out), color var(--dur-fast) var(--ease-out);
}
.nav-item:hover { background: var(--color-paper-2); color: var(--color-ink); }
.nav-item.is-active {
  background: var(--color-accent-soft);
  color: var(--color-accent);
  font-weight: 600;
}
.rail-foot {
  padding: var(--space-4) var(--space-5);
  font-size: var(--text-xs); color: var(--color-ink-faint);
  border-top: 1px solid var(--color-border);
  display: flex; align-items: center; gap: var(--space-2);
  letter-spacing: 0.02em;
}
.foot-dot { width: 7px; height: 7px; border-radius: 50%; }
.dot-lan { background: var(--color-success); }
.dot-cloud { background: var(--color-accent); }

/* ---- stage ---- */
.stage { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.topbar {
  height: 56px; flex: 0 0 56px;
  display: flex; align-items: center; gap: var(--space-4);
  padding: 0 var(--space-6);
  border-bottom: 1px solid var(--color-border);
  background: color-mix(in oklab, var(--color-paper) 88%, transparent);
  backdrop-filter: saturate(1.2) blur(8px);
  position: sticky; top: 0; z-index: 5;
}
.crumb { font-weight: 600; font-size: var(--text-lg); letter-spacing: -0.01em; color: var(--color-ink); }
.menu-btn {
  display: none; border: none; background: none; cursor: pointer;
  padding: var(--space-2); border-radius: 8px; color: var(--color-ink);
}
.menu-btn:hover { background: var(--color-paper-2); }
.userbox { margin-left: auto; display: flex; align-items: center; gap: var(--space-3); }
.role-chip {
  font-size: var(--text-xs); padding: 3px 9px; border-radius: 999px;
  background: var(--color-paper-2); color: var(--color-ink-soft);
  border: 1px solid var(--color-border);
}
.role-admin { background: oklch(96% 0.025 25); color: oklch(48% 0.14 25); border-color: oklch(89% 0.045 25); }
.role-operator { background: var(--color-accent-soft); color: var(--color-accent); border-color: oklch(89% 0.05 255); }
.role-inspector { background: var(--color-paper-2); color: var(--color-ink-faint); }
.uname { font-size: var(--text-sm); color: var(--color-ink-soft); }
.logout {
  border: 1px solid var(--color-border-strong); background: none;
  border-radius: 8px; padding: 5px var(--space-3);
  font-size: var(--text-sm); color: var(--color-ink-soft); cursor: pointer;
  transition: border-color var(--dur-fast) var(--ease-out), color var(--dur-fast) var(--ease-out), background-color var(--dur-fast) var(--ease-out);
}
.logout:hover { border-color: var(--color-danger); color: var(--color-danger); background: oklch(97% 0.02 25); }
.content { flex: 1 1 auto; padding: var(--space-6); }

.app-footer {
  display: flex; align-items: center; justify-content: space-between;
  flex-wrap: wrap; gap: var(--space-2) var(--space-4);
  padding: var(--space-3) var(--space-6);
  border-top: 1px solid var(--color-border);
  font-size: var(--text-xs); color: var(--color-ink-faint);
}
.footer-link { color: var(--color-ink-soft); }
.footer-link:hover { color: var(--color-accent); }

.drawer-nav { height: 100%; }
.drawer-brand {
  display: block; font-weight: 600; padding: var(--space-4) var(--space-3);
  color: var(--color-ink); font-size: var(--text-lg);
}

@media (max-width: 768px) {
  .rail { display: none; }
  .menu-btn { display: inline-flex; }
  .content { padding: var(--space-4); }
  .uname { display: none; }
}
</style>
