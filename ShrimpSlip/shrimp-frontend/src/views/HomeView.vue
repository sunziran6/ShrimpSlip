<template>
  <div class="home-container">
    <el-container>
      <el-header class="home-header">
        <span class="logo">虾滑智能</span>
        <el-button @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <div class="welcome">
          <h2>AI 智能购物助手</h2>
          <p>用自然语言描述你的购物需求，AI 帮你搞定一切</p>
          <el-input
            class="chat-input"
            placeholder='例如："帮我配一套入门露营装备，预算2000以内"'
            size="large"
          />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function handleLogout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
}

.welcome {
  max-width: 600px;
  margin: 120px auto 0;
  text-align: center;
}

.welcome h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 12px;
}

.welcome p {
  color: #909399;
  margin-bottom: 32px;
}

.chat-input {
  max-width: 500px;
}
</style>
