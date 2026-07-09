<template>
  <div class="auth-form-wrapper w-full max-w-md">
    <div class="bg-secondary/40 backdrop-blur-xl rounded-2xl p-8 border border-white/10 shadow-2xl">
      <!-- Title -->
      <div class="text-center mb-8">
        <h2 class="text-2xl font-bold text-foreground">
          {{ mode === 'login' ? 'ShrimpSlip' : '创建账号' }}
        </h2>
        <p class="text-muted-foreground text-sm mt-2">
          {{ mode === 'login' ? '登录虾滑智能，开启 AI 购物' : '加入虾滑智能，体验 AI 购物新方式' }}
        </p>
      </div>

      <el-form :model="form" :rules="computedRules" ref="formRef" size="large" @submit.prevent>
        <!-- Phone -->
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            maxlength="11"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <!-- Code row (login + code mode) -->
        <template v-if="mode === 'login' && loginMethod === 'code'">
          <el-form-item prop="code">
            <div class="flex gap-3 w-full">
              <el-input
                v-model="form.code"
                placeholder="验证码"
                maxlength="6"
                :prefix-icon="Message"
                class="flex-1"
              />
              <el-button
                class="!w-[130px] shrink-0"
                :disabled="countdown > 0"
                @click="handleSendCode"
                :loading="sending"
              >
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </template>

        <!-- Password (login + password mode OR register mode) -->
        <template v-if="(mode === 'login' && loginMethod === 'password') || mode === 'register'">
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

        <!-- Confirm Password (register only) -->
        <template v-if="mode === 'register'">
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
        </template>

        <!-- Nickname (register only) -->
        <template v-if="mode === 'register'">
          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称"
              :prefix-icon="User"
            />
          </el-form-item>
        </template>

        <!-- Login method toggle -->
        <div v-if="mode === 'login'" class="text-right mb-4 -mt-2">
          <el-link type="primary" :underline="false" @click="toggleLoginMethod">
            {{ loginMethod === 'code' ? '使用密码登录' : '使用验证码登录' }}
          </el-link>
        </div>

        <!-- Submit -->
        <el-form-item>
          <el-button type="primary" class="w-full !h-[44px] !text-base" @click="handleSubmit" :loading="logging || loading">
            {{ mode === 'login' ? '登 录' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Mode switch -->
      <div class="text-center text-sm text-muted-foreground">
        {{ mode === 'login' ? '没有账号？' : '已有账号？' }}
        <el-link type="primary" :underline="false" @click="toggleMode">
          {{ mode === 'login' ? '点击注册' : '返回登录' }}
        </el-link>
      </div>
    </div>

    <!-- Registration success dialog -->
    <el-dialog v-model="successVisible" :show-close="false" width="360px" center>
      <div class="text-center py-5">
        <el-icon :size="56" color="#67c23a"><SuccessFilled /></el-icon>
        <p class="text-xl font-semibold text-foreground mt-4 mb-2">注册成功！</p>
        <p class="text-sm text-muted-foreground">{{ successCountdown }}s 后自动跳转...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Message, Lock, User, SuccessFilled } from '@element-plus/icons-vue'
import { sendCode, login, register } from '../../api/user'

const router = useRouter()
const formRef = ref(null)
const mode = ref('login') // 'login' | 'register'
const loginMethod = ref('code') // 'code' | 'password'
const sending = ref(false)
const logging = ref(false)
const loading = ref(false)
const countdown = ref(0)
const successVisible = ref(false)
const successCountdown = ref(2)

const form = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

function validateConfirm(rule, value, callback) {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const computedRules = computed(() => {
  const rules = {
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^\d{6,20}$/, message: '手机号格式不正确', trigger: 'blur' },
    ],
  }

  if (mode.value === 'login') {
    if (loginMethod.value === 'code') {
      rules.code = [{ required: true, message: '请输入验证码', trigger: 'blur' }]
    } else {
      rules.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }
  } else {
    rules.password = [
      { required: true, message: '请设置密码', trigger: 'blur' },
      { min: 6, message: '密码至少6位', trigger: 'blur' },
    ]
    rules.confirmPassword = [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      { validator: validateConfirm, trigger: 'blur' },
    ]
    rules.nickname = [{ required: true, message: '请输入昵称', trigger: 'blur' }]
  }

  return rules
})

function resetForm() {
  form.phone = ''
  form.code = ''
  form.password = ''
  form.confirmPassword = ''
  form.nickname = ''
  formRef.value?.resetFields()
}

function toggleMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  loginMethod.value = 'code'
  resetForm()
}

function toggleLoginMethod() {
  loginMethod.value = loginMethod.value === 'code' ? 'password' : 'code'
  form.code = ''
  form.password = ''
  formRef.value?.resetFields()
}

let sendTimer = null
let successTimer = null

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
    if (sendTimer) clearInterval(sendTimer)
    sendTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(sendTimer)
        sendTimer = null
      }
    }, 1000)
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  if (mode.value === 'login') {
    await handleLogin()
  } else {
    await handleRegister()
  }
}

async function handleLogin() {
  if (loginMethod.value === 'code') {
    try { await formRef.value.validate(['phone', 'code']) } catch { return }
  } else {
    try { await formRef.value.validate(['phone', 'password']) } catch { return }
  }

  logging.value = true
  try {
    const payload = { phone: form.phone }
    if (loginMethod.value === 'code') {
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

async function handleRegister() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await register({
      phone: form.phone,
      password: form.password,
      nickname: form.nickname,
    })
    localStorage.setItem('accessToken', res.data.accessToken)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    localStorage.setItem('role', res.data.role)
    successVisible.value = true
    successCountdown.value = 2
    if (successTimer) clearInterval(successTimer)
    successTimer = setInterval(() => {
      successCountdown.value--
      if (successCountdown.value <= 0) {
        clearInterval(successTimer)
        successTimer = null
        router.push('/home')
      }
    }, 1000)
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (sendTimer) clearInterval(sendTimer)
  if (successTimer) clearInterval(successTimer)
})
</script>
