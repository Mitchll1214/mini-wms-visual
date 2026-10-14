import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: '',
    role: 'OPERATOR', // ADMIN / OPERATOR / INSPECTOR
    clientType: 'PC', // PC / MOBILE
    permissionMode: 'OPERATE', // READONLY / OPERATE
    sessionId: '',
    lanIp: '',
    deviceId: ''
  }),
  getters: {
    isMobile: (state) => state.clientType === 'MOBILE',
    isReadonly: (state) => state.permissionMode === 'READONLY',
    canOperate: (state) => state.permissionMode === 'OPERATE',
    // 角色判断
    isAdmin: (state) => state.role === 'ADMIN',
    isOperator: (state) => state.role === 'OPERATOR',
    isInspector: (state) => state.role === 'INSPECTOR',
    // 能否进行写操作（管理员、操作员可操作；盘点员仅查看）
    canWrite: (state) => state.role === 'ADMIN' || state.role === 'OPERATOR',
    // 能否删除数据 / 用户管理（仅管理员）
    isSuperAdmin: (state) => state.role === 'ADMIN',
    roleLabel: (state) => {
      const map = { ADMIN: '管理员', OPERATOR: '操作员', INSPECTOR: '盘点员' }
      return map[state.role] || state.role
    }
  },
  actions: {
    setSession(data) {
      this.username = data.username || ''
      this.role = data.role || 'OPERATOR'
      this.clientType = data.clientType || 'PC'
      this.permissionMode = data.permissionMode || 'OPERATE'
      this.sessionId = data.sessionId || ''
    },
    setLanIp(ip) {
      this.lanIp = ip
    },
    logout() {
      this.username = ''
      this.role = 'OPERATOR'
      this.clientType = 'PC'
      this.permissionMode = 'OPERATE'
      this.sessionId = ''
    }
  },
  persist: {
    key: 'mini-wms-user',
    storage: localStorage,
    paths: ['username', 'role', 'clientType', 'permissionMode', 'sessionId', 'lanIp']
  }
})