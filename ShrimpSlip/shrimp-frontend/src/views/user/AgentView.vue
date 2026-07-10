<template>
  <div class="flex flex-col h-[calc(100vh-64px)] max-w-4xl mx-auto px-4">
    <!-- Messages Area -->
    <div class="flex-1 overflow-y-auto py-6 space-y-4">
      <!-- Empty State -->
      <div v-if="messages.length === 0" class="flex flex-col items-center justify-center h-full text-center">
        <div class="w-16 h-16 rounded-2xl bg-primary/10 flex items-center justify-center mb-6">
          <el-icon :size="32" class="text-primary"><ChatDotRound /></el-icon>
        </div>
        <h3 class="text-xl font-semibold text-foreground mb-2">AI 智能购物助手</h3>
        <p class="text-muted-foreground max-w-sm">
          用自然语言描述你的购物需求，AI 帮你智能分析、精准推荐
        </p>
        <div class="flex flex-wrap gap-2 mt-6 justify-center">
          <button
            v-for="tip in tips"
            :key="tip"
            @click="inputText = tip"
            class="px-4 py-2 rounded-full bg-secondary border border-border text-sm text-muted-foreground hover:text-foreground hover:border-primary/30 transition-all"
          >
            {{ tip }}
          </button>
        </div>
      </div>

      <!-- Messages -->
      <div v-for="(msg, i) in messages" :key="i" class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
        <div
          class="max-w-[75%] rounded-2xl px-5 py-3.5"
          :class="msg.role === 'user'
            ? 'bg-primary text-primary-foreground rounded-br-md'
            : 'bg-secondary text-foreground rounded-bl-md border border-border'"
        >
          {{ msg.content }}
        </div>
      </div>
    </div>

    <!-- Input Area -->
    <div class="py-4 border-t border-border">
      <div class="flex items-center gap-3 bg-secondary rounded-xl px-4 py-2 border border-border focus-within:border-primary/40 transition-colors">
        <input
          v-model="inputText"
          @keydown.enter="sendMessage"
          placeholder='例如："帮我配一套入门露营装备，预算2000以内"'
          class="flex-1 bg-transparent text-foreground outline-none placeholder:text-muted-foreground text-sm py-1"
        />
        <button
          @click="sendMessage"
          :disabled="!inputText.trim()"
          class="w-9 h-9 rounded-lg bg-primary text-primary-foreground flex items-center justify-center hover:bg-primary/85 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
        >
          <el-icon :size="16"><Promotion /></el-icon>
        </button>
      </div>
      <p class="text-xs text-muted-foreground text-center mt-2">AI 购物助手正在开发中，当前为演示界面</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ChatDotRound, Promotion } from '@element-plus/icons-vue'

const inputText = ref('')
const messages = ref([])

const tips = [
  '帮我配一套入门露营装备，预算2000以内',
  '推荐一款适合学生党的笔记本电脑',
  '想买一套秋冬护肤套装',
]

function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  setTimeout(() => {
    messages.value.push({ role: 'assistant', content: '感谢你的咨询！AI 智能购物功能正在开发中，敬请期待。届时我会根据你的需求智能分析并推荐最合适的商品。' })
  }, 600)
}
</script>
