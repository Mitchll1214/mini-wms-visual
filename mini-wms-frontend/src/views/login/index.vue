<template>
  <div class="login-shell">
    <!-- 左：功能介绍 -->
    <section class="intro">
      <div class="intro-head">
        <div class="brand">
          <span class="mark" aria-hidden="true">W</span>
          <span class="brand-name">小仓图管</span>
        </div>
        <h1 class="headline">把仓库的每一个货位，<br />搬到屏幕上</h1>
        <p class="lede">面向小仓库的货位可视化、库存与流水管理，扫码即用，按角色分配权限。</p>
      </div>

      <ul class="features">
        <li v-for="f in features" :key="f.title" class="feature">
          <span class="dot" aria-hidden="true"></span>
          <span class="feature-text">
            <strong class="feature-title">{{ f.title }}</strong>
            <span class="feature-desc">{{ f.desc }}</span>
          </span>
        </li>
      </ul>

      <p class="intro-foot">Spring Boot 3 · Vue 3 · SQLite · Docker Compose</p>
    </section>

    <!-- 右：登录 -->
    <section class="panel">
      <div class="login-card">
        <h2 class="card-title">登录</h2>
        <p class="card-sub">请使用管理员分配的账号</p>

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

        <p class="hint">登录后按角色显示权限</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const form = ref({ username: '', password: '' })
const loading = ref(false)
const userStore = useUserStore()

const features = [
  { title: '货位可视化', desc: '仓库 / 库区 / 货架 / 储位逐级维护，状态一目了然' },
  { title: '库存多视图', desc: '商品 / 储位 / 流水，进出有据可查' },
  { title: '出入库与拆分', desc: '单据流转，出库自动推荐拆分储位' },
  { title: '手机扫码授权', desc: '扫码免密进入，只读 / 操作双模式' }
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
/* Hallmark · macrostructure: Split Intro · tone: technical · anchor hue: 255 (cobalt) · theme: Cobalt
 * pre-emit critique: P4 H4 E4 S4 R5 V4
 * genre: modern-minimal · paper: light · display: system grotesk · accent: cool-blue
 */
.login-shell {
  min-height: 100svh;
  display: grid;
  grid-template-columns: minmax(0, 1.06fr) minmax(0, 0.94fr);
  background: var(--color-paper);
}

/* ---------- 左：功能介绍 ---------- */
.intro {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-10);
  padding: clamp(32px, 4.5vw, 80px) clamp(28px, 4.5vw, 72px);
}
.intro-head { display: flex; flex-direction: column; gap: var(--space-5); }

.brand { display: flex; align-items: center; gap: var(--space-3); }
.mark {
  display: inline-grid; place-items: center;
  width: 38px; height: 38px; border-radius: var(--radius-md);
  background: var(--color-accent); color: #fff;
  font-weight: 700; font-size: var(--text-lg);
}
.brand-name { font-weight: 600; letter-spacing: 0.02em; }

.headline {
  margin: 0;
  font-size: clamp(28px, 3vw, 42px);
  line-height: 1.14;
  letter-spacing: -0.02em;
  color: var(--color-ink);
}
.lede {
  margin: 0; max-width: 42ch;
  font-size: var(--text-md); color: var(--color-ink-soft);
}

.features { list-style: none; margin: 0; padding: 0; max-width: 46ch; }
.feature {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: var(--space-4);
  padding: var(--space-4) 0;
  border-top: 1px solid var(--color-border);
}
.feature:last-child { border-bottom: 1px solid var(--color-border); }
.dot {
  width: 7px; height: 7px; margin-top: 7px;
  border-radius: 50%; background: var(--color-accent);
}
.feature-text { display: flex; flex-direction: column; gap: 3px; }
.feature-title { font-size: var(--text-md); font-weight: 600; color: var(--color-ink); }
.feature-desc { font-size: var(--text-sm); color: var(--color-ink-soft); }

.intro-foot {
  font-size: var(--text-xs);
  letter-spacing: 0.04em;
  color: var(--color-ink-faint);
}

/* ---------- 右：登录 ---------- */
.panel {
  display: grid; place-items: center;
  padding: clamp(24px, 4vw, 64px);
  background: var(--color-paper-2);
  border-left: 1px solid var(--color-border);
}
.login-card {
  width: 100%; max-width: 380px;
  padding: clamp(24px, 3vw, 36px);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-2);
}
.card-title { margin: 0 0 var(--space-2); font-size: var(--text-xl); }
.card-sub { margin: 0 0 var(--space-8); font-size: var(--text-sm); color: var(--color-ink-faint); }

.login-form { display: flex; flex-direction: column; gap: var(--space-4); }
.field span {
  display: block; font-size: var(--text-xs); color: var(--color-ink-soft);
  margin-bottom: var(--space-1); letter-spacing: 0.02em;
}
.submit { width: 100%; margin-top: var(--space-2); }
.hint {
  margin: var(--space-8) 0 0; font-size: var(--text-xs);
  color: var(--color-ink-faint); text-align: center;
}

/* ---------- 入场动效（首次加载，尊重减弱动效偏好） ---------- */
@media (prefers-reduced-motion: no-preference) {
  .intro-head, .features, .intro-foot, .login-card {
    animation: rise var(--dur-slow) var(--ease-out) both;
  }
  .features { animation-delay: 80ms; }
  .login-card { animation-delay: 120ms; }
}
@keyframes rise {
  from { opacity: 0; transform: translateY(12px); filter: blur(4px); }
  to { opacity: 1; transform: none; filter: none; }
}

@media (max-width: 900px) {
  .login-shell { grid-template-columns: 1fr; }
  .intro { gap: var(--space-6); padding: clamp(28px, 7vw, 44px) clamp(20px, 6vw, 40px); }
  .features, .intro-foot { display: none; }
  .panel {
    border-left: none;
    border-top: 1px solid var(--color-border);
    background: var(--color-paper);
    padding: 0 clamp(20px, 6vw, 40px) clamp(40px, 8vw, 64px);
    place-items: start center;
  }
  .login-card { max-width: 420px; box-shadow: none; border: none; padding: 0; }
}
</style>
