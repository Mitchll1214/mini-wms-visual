import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 菜单路由角色规定：
//   ADMIN    —— 全部（含用户管理 /system/users）
//   OPERATOR —— 除用户管理和删除数据外全部
//   INSPECTOR—— 仅查看（隐藏入库/出库/回收站等操作页，后端仍会硬拦截）
const pcRoutes = [
  { path: '/', name: 'Home', component: () => import('../views/home/index.vue'), meta: { title: '首页' } },
  { path: '/login', name: 'Login', component: () => import('../views/login/index.vue'), meta: { title: '登录', public: true } },
  { path: '/warehouse', name: 'Warehouse', component: () => import('../views/warehouse/index.vue'), meta: { title: '仓储管理' } },
  { path: '/category', name: 'Category', component: () => import('../views/category/index.vue'), meta: { title: '类别管理' } },
  { path: '/goods', name: 'Goods', component: () => import('../views/goods/index.vue'), meta: { title: '商品管理' } },
  { path: '/goods/:id', name: 'GoodsDetail', component: () => import('../views/goods/detail.vue'), meta: { title: '商品详情' }, props: true },
  { path: '/supplier', name: 'Supplier', component: () => import('../views/supplier/index.vue'), meta: { title: '供应商管理' } },
  { path: '/inventory', name: 'Inventory', component: () => import('../views/inventory/index.vue'), meta: { title: '库存总览' } },
  { path: '/inventory/bin', name: 'InventoryBin', component: () => import('../views/inventory/bin.vue'), meta: { title: '储位视图' } },
  { path: '/inventory/logs', name: 'InventoryLogs', component: () => import('../views/inventory/logs.vue'), meta: { title: '库存流水' } },
  { path: '/inventory/adjust', name: 'InventoryAdjust', component: () => import('../views/inventory/adjust.vue'), meta: { title: '库存调整', roles: ['ADMIN'] } },
  { path: '/stock-in', name: 'StockIn', component: () => import('../views/stock/index.vue'), meta: { title: '入库管理', roles: ['ADMIN', 'OPERATOR'] } },
  { path: '/stock-out', name: 'StockOut', component: () => import('../views/stock/out.vue'), meta: { title: '出库管理', roles: ['ADMIN', 'OPERATOR'] } },
  { path: '/recycle', name: 'Recycle', component: () => import('../views/recycle/index.vue'), meta: { title: '回收站', roles: ['ADMIN'] } },
  { path: '/system', name: 'System', component: () => import('../views/system/index.vue'), meta: { title: '系统配置', roles: ['ADMIN'] } },
  { path: '/system/images', name: 'SystemImages', component: () => import('../views/system/images.vue'), meta: { title: '图片管理', roles: ['ADMIN'] } },
  { path: '/system/users', name: 'SystemUsers', component: () => import('../views/system/users.vue'), meta: { title: '用户管理', roles: ['ADMIN'] } },
  { path: '/system/logs', name: 'SystemLogs', component: () => import('../views/system/logs.vue'), meta: { title: '操作日志', roles: ['ADMIN'] } }
]

const router = createRouter({
  history: createWebHistory(),
  routes: pcRoutes
})

// 动态标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '小仓图管'
  const userStore = useUserStore()

  // 免登录页面（登录、扫码入口）直接放行
  if (to.meta.public) {
    return next()
  }

  // 需登录：无会话则去登录页
  if (!userStore.sessionId) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 角色校验：路由声明了 roles 且当前角色不在其中则退回首页
  if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    return next({ path: '/' })
  }

  next()
})

export default router