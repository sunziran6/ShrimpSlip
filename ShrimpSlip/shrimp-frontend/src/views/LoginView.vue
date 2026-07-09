<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>虾滑智能</h1>
        <p>AI 智能购物，一句话搞定所有需求</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" size="large" @submit.prevent>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            maxlength="11"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <!-- 验证码登录模式 -->
        <template v-if="loginMode === 'code'">
          <el-form-item prop="code">
            <div class="code-row">
              <el-input
                v-model="form.code"
                placeholder="验证码"
                maxlength="6"
                :prefix-icon="Message"
              />
              <el-button
                class="send-btn"
                :disabled="countdown > 0"
                @click="handleSendCode"
                :loading="sending"
              >
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </template>

        <!-- 密码登录模式 -->
        <template v-if="loginMode === 'password'">
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
        </template>

        <!-- 切换登录方式 -->
        <div class="mode-switch">
          <el-link type="primary" :underline="false" @click="toggleMode">
            {{ loginMode === 'code' ? '使用密码登录' : '使用验证码登录' }}
          </el-link>
        </div>

        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="logging">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 注册入口 -->
      <div class="register-hint">
        没有账号，<el-link type="primary" @click="router.push('/register')">点击注册</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Message, Lock } from '@element-plus/icons-vue'
import { sendCode, login } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const sending = ref(false)
const logging = ref(false)
const countdown = ref(0)
const loginMode = ref('code')

const form = reactive({ phone: '', code: '', password: '' })
const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{6,20}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function toggleMode() {
  loginMode.value = loginMode.value === 'code' ? 'password' : 'code'
  form.code = ''
  form.password = ''
}

let timer = null

async function handleSendCode() {
  try {
    await formRef.value.validateField('phone')
  } catch {
    return
  }
  sending.value = true
  try {
    await sendCode(form.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } finally {
    sending.value = false
  }
}

async function handleLogin() {
  if (loginMode.value === 'code') {
    try { await formRef.value.validate(['phone', 'code']) } catch { return }
  } else {
    try { await formRef.value.validate(['phone', 'password']) } catch { return }
  }

  logging.value = true
  try {
    const payload = { phone: form.phone }
    if (loginMode.value === 'code') {
      payload.code = form.code
    } else {
      payload.password = form.password
    }
    const res = await login(payload)
    localStorage.setItem('accessToken', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    localStorage.setItem('role', res.data.role)
    ElMessage.success(`欢迎回来，${res.data.nickname}`)
    router.push('/home')
  } finally {
    logging.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 48px 40px 36px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-header h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}

.login-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.send-btn {
  flex-shrink: 0;
  width: 120px;
}

.mode-switch {
  text-align: right;
  margin-bottom: 18px;
  margin-top: -8px;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
}

.register-hint {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin-top: -8px;
}
</style>
