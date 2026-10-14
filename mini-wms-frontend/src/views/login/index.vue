<template>
  <div class="login-shell">
    <!-- 左：功能介绍 -->
    <section class="login-intro">
      <div class="brand rise">
        <span class="mark">W</span>
        <div class="brand-text">
          <strong>小仓图管</strong>
          <span>MiniWMS Visual</span>
        </div>
      </div>

      <h1 class="headline rise">货位可视化，<br />库存一眼看清</h1>
      <p class="lede rise">面向小仓库的货位可视化、库存展示与拍照标记管理系统。</p>

      <ul class="feature-list rise">
        <li v-for="f in features" :key="f.title" class="feat">
          <span class="feat-icon"><el-icon :size="20"><component :is="f.icon" /></el-icon></span>
          <div class="feat-body">
            <strong>{{ f.title }}</strong>
            <p>{{ f.desc }}</p>
          </div>
        </li>
      </ul>

      <p class="foot rise">Spring Boot 3 · Vue 3 · SQLite · 单机 Docker 部署</p>
    </section>

    <!-- 右：登录 -->
    <section class="login-panel">
      <div class="login-box">
        <div class="mobile-brand">
          <span class="mark">W</span>
          <strong>小仓图管</strong>
        </div>

        <header class="panel-head">
          <h2>欢迎回来</h2>
          <p>请使用管理员分配的账号登录</p>
        </header>

        <form class="login-form" @submit.prevent="login">
          <label class="field">
            <span>用户名</span>
            <el-input v-model="form.username" placeholder="请输入用户名" clearable size="large" autocomplete="username" />
          </label>
          <label class="field">
            <span>密码</span>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large" autocomplete="current-password" @keyup.enter="login" />
          </label>
          <el-button type="primary" size="large" class="submit" :loading="loading" native-type="submit">
            {{ loading ? '登录中…' : '登 录' }}
          </el-button>
        </form>

        <p class="hint">账密由管理员分配 · 登录后按角色显示权限</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, markRaw } from 'vue'
import { ElMessage } from 'element-plus'
import { Grid, DataBoard, Box, Iphone } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const form = ref({ username: '', password: '' })
const loading = ref(false)
const userStore = useUserStore()

const features = [
  { icon: markRaw(Grid), title: '货位可视化', desc: '仓库 / 库区 / 货架 / 储位逐级维护，货位状态一目了然' },
  { icon: markRaw(DataBoard), title: '库存多视图', desc: '商品视图 / 储位视图 / 库存流水，进出有据可查' },
  { icon: markRaw(Box), title: '出入库与拆分', desc: '入库出库单据流转，出库自动推荐拆分储位' },
  { icon: markRaw(Iphone), title: '手机扫码授权', desc: '扫码免密进入，只读 / 操作双模式按需授权' }
]

const login = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const result = await request.post('/api/auth/login', {
      username: form.value.username,
      password: form.value.password,
      clientType: 'PC',
      deviceInfo: navigator.userAgent
    })
    userStore.setSession(result)
    ElMessage.success('登录成功')
    window.location.href = '/'
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-shell {
  min-height: 100svh;
  display: grid;
  grid-template-columns: minmax(0, 47%) minmax(0, 53%);
  background: var(--color-paper);
}

/* ---------- 左：功能介绍 ---------- */
.login-intro {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-6);
  padding: clamp(32px, 5vw, 72px);
  color: oklch(96% 0.006 250);
  background-color: oklch(26% 0.025 262);
  background-image:
    repeating-linear-gradient(0deg, transparent 0 27px, oklch(100% 0 0 / 0.035) 27px 28px),
    repeating-linear-gradient(90deg, transparent 0 27px, oklch(100% 0 0 / 0.035) 27px 28px),
    radial-gradient(120% 80% at 15% 0%, oklch(40% 0.09 258) 0%, oklch(26% 0.025 262) 55%);
  overflow: hidden;
}

.brand { display: flex; align-items: center; gap: var(--space-3); }
.mark {
  display: inline-grid; place-items: center;
  width: 40px; height: 40px; border-radius: var(--radius-md);
  background: var(--color-accent); color: #fff;
  font-weight: 700; font-size: var(--text-xl);
}
.brand-text { display: flex; flex-direction: column; line-height: 1.25; }
.brand-text strong { font-size: var(--text-lg); color: oklch(98% 0.004 250); }
.brand-text span { font-size: var(--text-xs); letter-spacing: 0.06em; color: oklch(78% 0.02 250); }

.headline {
  margin: 0;
  font-size: clamp(30px, 3.4vw, 44px);
  line-height: 1.14;
  letter-spacing: -0.02em;
  color: oklch(99% 0.003 250);
}
.lede {
  margin: 0;
  max-width: 34ch;
  font-size: var(--text-md);
  color: oklch(82% 0.012 250);
}

.feature-list {
  list-style: none;
  margin: var(--space-2) 0 0;
  padding: 0;
  display: grid;
  gap: var(--space-4);
  max-width: 42ch;
}
.feat { display: flex; gap: var(--space-4); align-items: flex-start; }
.feat-icon {
  flex: 0 0 auto;
  display: grid; place-items: center;
  width: 38px; height: 38px;
  border-radius: var(--radius-md);
  background: oklch(100% 0 0 / 0.09);
  border: 1px solid oklch(100% 0 0 / 0.12);
  color: oklch(80% 0.09 255);
}
.feat-body strong { display: block; font-size: var(--text-md); font-weight: 600; color: oklch(98% 0.004 250); }
.feat-body p { margin: 2px 0 0; font-size: var(--text-sm); line-height: 1.5; color: oklch(80% 0.012 250); }

.foot {
  margin: var(--space-4) 0 0;
  font-size: var(--text-xs);
  letter-spacing: 0.04em;
  color: oklch(70% 0.015 250);
}

/* ---------- 右：登录 ---------- */
.login-panel {
  display: grid; place-items: center;
  padding: clamp(24px, 6vw, 72px);
}
.login-box { width: 100%; max-width: 380px; }

.mobile-brand {
  display: none;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-8);
}
.mobile-brand strong { font-size: var(--text-lg); color: var(--color-ink); }

.panel-head { margin-bottom: var(--space-8); }
.panel-head h2 { margin: 0 0 var(--space-2); }
.panel-head p { margin: 0; font-size: var(--text-sm); color: var(--color-ink-faint); }

.login-form { display: flex; flex-direction: column; gap: var(--space-4); }
.field span {
  display: block; font-size: var(--text-xs); color: var(--color-ink-soft);
  margin-bottom: var(--space-1); letter-spacing: 0.02em;
}
.submit { width: 100%; margin-top: var(--space-2); }
.hint {
  margin-top: var(--space-8); font-size: var(--text-xs);
  color: var(--color-ink-faint); text-align: center;
}

/* ---------- 入场动效（首次加载，尊重减弱动效偏好） ---------- */
@media (prefers-reduced-motion: no-preference) {
  .rise { animation: rise var(--dur-slow) var(--ease-out) both; }
  .headline { animation-delay: 70ms; }
  .lede { animation-delay: 140ms; }
  .feature-list { animation-delay: 210ms; }
  .foot { animation-delay: 280ms; }
}
@keyframes rise {
  from { opacity: 0; transform: translateY(12px); filter: blur(4px); }
  to { opacity: 1; transform: none; filter: none; }
}

@media (max-width: 900px) {
  .login-shell { grid-template-columns: 1fr; }
  .login-intro { display: none; }
  .mobile-brand { display: flex; }
  .login-panel { min-height: 100svh; }
}
</style>
