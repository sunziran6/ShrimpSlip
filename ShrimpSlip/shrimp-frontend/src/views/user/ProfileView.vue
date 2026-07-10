<template>
  <div class="max-w-[720px] mx-auto px-6 py-8">
    <h2 class="text-xl font-bold text-foreground mb-6">个人中心</h2>

    <!-- Profile Card -->
    <div class="bg-secondary rounded-2xl border border-border p-8 mb-6">
      <div class="flex items-center gap-5 mb-6">
        <!-- Avatar: click to upload -->
        <div
          class="relative w-16 h-16 rounded-full bg-primary/15 flex items-center justify-center text-primary font-bold text-2xl cursor-pointer overflow-hidden group shrink-0"
          @click="triggerAvatarUpload"
          title="点击更换头像"
        >
          <img v-if="profile.avatar" :src="profile.avatar" class="w-full h-full object-cover" />
          <span v-else>{{ (profile.nickname || 'U')[0].toUpperCase() }}</span>
          <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
            <el-icon :size="20" color="#fff"><CameraFilled /></el-icon>
          </div>
        </div>
        <input ref="avatarInput" type="file" accept="image/*" class="hidden" @change="handleAvatarChange" />
        <div>
          <h3 class="text-lg font-semibold text-foreground">{{ profile.nickname || '未设置昵称' }}</h3>
          <p class="text-muted-foreground text-sm">{{ profile.phone || '' }}</p>
          <el-tag :type="profile.role === 'ADMIN' ? 'danger' : ''" size="small" class="mt-1">
            {{ profile.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </div>
      </div>

      <div class="flex gap-3">
        <button
          @click="openEditDialog"
          class="px-5 py-2 rounded-xl bg-primary text-primary-foreground font-medium hover:bg-primary/85 transition-all text-sm"
        >
          编辑资料
        </button>
        <button
          v-if="profile.role === 'ADMIN'"
          @click="$router.push('/admin')"
          class="px-5 py-2 rounded-xl bg-primary/15 text-primary font-medium hover:bg-primary hover:text-primary-foreground transition-all text-sm"
        >
          进入管理后台
        </button>
      </div>
    </div>

    <!-- Address Section -->
    <div class="bg-secondary rounded-2xl border border-border p-8">
      <div class="flex items-center justify-between mb-5">
        <h3 class="text-lg font-semibold text-foreground">收货地址</h3>
        <button
          @click="openAddressDialog(null)"
          class="px-4 py-1.5 rounded-lg bg-primary/15 text-primary text-sm font-medium hover:bg-primary hover:text-primary-foreground transition-all"
        >
          新增地址
        </button>
      </div>

      <div v-if="addresses.length === 0" class="text-center text-muted-foreground py-8 text-sm">
        暂无收货地址
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="flex items-start justify-between p-4 rounded-xl border border-border hover:border-primary/30 transition-colors"
        >
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <span class="text-foreground font-medium">{{ addr.receiverName }}</span>
              <span class="text-muted-foreground text-sm">{{ addr.receiverPhone }}</span>
              <el-tag v-if="addr.isDefault === 1" type="success" size="small">默认</el-tag>
            </div>
            <p class="text-muted-foreground text-sm truncate">
              {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
            </p>
          </div>
          <div class="flex items-center gap-2 shrink-0 ml-4">
            <el-button type="primary" link size="small" @click="openAddressDialog(addr)">编辑</el-button>
            <el-button type="danger" link size="small" @click="openDeleteDialog(addr)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Profile Dialog -->
    <el-dialog v-model="editVisible" title="编辑资料" width="440px" :close-on-click-modal="false">
      <el-form :model="editForm" label-position="top" size="large" ref="editFormRef" :rules="editRules">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>

        <el-divider />

        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="editForm.oldPassword" type="password" show-password placeholder="如需修改密码，请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="editForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="editForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProfile" :loading="savingProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- Delete Address Dialog -->
    <el-dialog v-model="deleteVisible" title="删除地址" width="400px" :close-on-click-modal="false">
      <div class="text-center py-4">
        <el-icon :size="48" color="#f56c6c"><WarningFilled /></el-icon>
        <p class="text-foreground mt-4 mb-2">确定删除该地址吗？</p>
        <p v-if="deletingAddr" class="text-muted-foreground text-sm truncate px-4">
          {{ deletingAddr.province }}{{ deletingAddr.city }}{{ deletingAddr.district }} {{ deletingAddr.detail }}
        </p>
      </div>
      <template #footer>
        <el-button @click="deleteVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="deleting">确定删除</el-button>
      </template>
    </el-dialog>

    <!-- Address Edit Dialog -->
    <el-dialog v-model="addrVisible" :title="editingAddr ? '编辑地址' : '新增地址'" width="480px" :close-on-click-modal="false">
      <el-form :model="addrForm" label-position="top" size="large" ref="addrFormRef">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="收货人" required>
              <el-input v-model="addrForm.receiverName" placeholder="姓名" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" required>
              <el-input v-model="addrForm.receiverPhone" placeholder="手机号" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="省">
              <el-input v-model="addrForm.province" placeholder="省" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="市">
              <el-input v-model="addrForm.city" placeholder="市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区">
              <el-input v-model="addrForm.district" placeholder="区" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址" required>
          <el-input v-model="addrForm.detail" type="textarea" :rows="2" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item label="">
          <el-checkbox v-model="addrForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addrVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress" :loading="savingAddr">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CameraFilled, WarningFilled } from '@element-plus/icons-vue'
import { getProfile, updateProfile, changePassword, uploadFile, getAddresses, createAddress, updateAddress, deleteAddress } from '../../api/user'

const profile = reactive({ phone: '', nickname: '', avatar: '', role: '' })

// Avatar
const avatarInput = ref(null)

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadFile(file)
    const url = res.data
    await updateProfile({ nickname: profile.nickname, avatar: url })
    profile.avatar = url
    ElMessage.success('头像已更新')
  } catch {
    // ignore
  } finally {
    if (avatarInput.value) avatarInput.value.value = ''
  }
}

// Edit profile dialog
const editVisible = ref(false)
const savingProfile = ref(false)
const editFormRef = ref(null)
const editForm = reactive({ nickname: '', oldPassword: '', newPassword: '', confirmPassword: '' })

function validateConfirm(rule, value, callback) {
  if (editForm.oldPassword && !editForm.newPassword) {
    callback(new Error('请输入新密码'))
  } else if (editForm.newPassword !== editForm.confirmPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const editRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  newPassword: [{ min: 6, message: '新密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }],
}

function openEditDialog() {
  editForm.nickname = profile.nickname
  editForm.oldPassword = ''
  editForm.newPassword = ''
  editForm.confirmPassword = ''
  editVisible.value = true
}

async function handleSaveProfile() {
  try {
    await editFormRef.value?.validate()
  } catch {
    return
  }

  savingProfile.value = true
  try {
    // Update nickname
    await updateProfile({ nickname: editForm.nickname })
    profile.nickname = editForm.nickname

    // Change password if old password is provided
    if (editForm.oldPassword) {
      await changePassword({
        oldPassword: editForm.oldPassword,
        newPassword: editForm.newPassword,
        confirmPassword: editForm.confirmPassword,
      })
      ElMessage.success('资料和密码已更新')
    } else {
      ElMessage.success('资料已更新')
    }
    editVisible.value = false
  } catch (e) {
    // error handled by interceptor
  } finally {
    savingProfile.value = false
  }
}

// Addresses
const addresses = ref([])
const addrVisible = ref(false)
const editingAddr = ref(null)
const savingAddr = ref(false)
const addrFormRef = ref(null)
const addrForm = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: false })

// Delete address
const deleteVisible = ref(false)
const deletingAddr = ref(null)
const deleting = ref(false)

onMounted(async () => {
  try {
    const res = await getProfile()
    Object.assign(profile, res.data)
  } catch {
    // ignore
  }
  loadAddresses()
})

async function loadAddresses() {
  try {
    const res = await getAddresses()
    addresses.value = res.data || []
  } catch {
    // ignore
  }
}

function openAddressDialog(addr) {
  editingAddr.value = addr
  if (addr) {
    Object.assign(addrForm, {
      receiverName: addr.receiverName || '',
      receiverPhone: addr.receiverPhone || '',
      province: addr.province || '',
      city: addr.city || '',
      district: addr.district || '',
      detail: addr.detail || '',
      isDefault: addr.isDefault === 1,
    })
  } else {
    Object.assign(addrForm, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: false })
  }
  addrVisible.value = true
}

async function handleSaveAddress() {
  if (!addrForm.receiverName.trim() || !addrForm.receiverPhone.trim() || !addrForm.detail.trim()) {
    ElMessage.warning('请填写必填项')
    return
  }
  savingAddr.value = true
  try {
    const data = {
      receiverName: addrForm.receiverName,
      receiverPhone: addrForm.receiverPhone,
      province: addrForm.province,
      city: addrForm.city,
      district: addrForm.district,
      detail: addrForm.detail,
      isDefault: addrForm.isDefault ? 1 : 0,
    }
    if (editingAddr.value) {
      await updateAddress(editingAddr.value.id, data)
      ElMessage.success('地址已更新')
    } else {
      await createAddress(data)
      ElMessage.success('地址已添加')
    }
    addrVisible.value = false
    loadAddresses()
  } catch {
    // ignore
  } finally {
    savingAddr.value = false
  }
}

function openDeleteDialog(addr) {
  deletingAddr.value = addr
  deleteVisible.value = true
}

async function confirmDelete() {
  if (!deletingAddr.value) return
  deleting.value = true
  try {
    await deleteAddress(deletingAddr.value.id)
    ElMessage.success('地址已删除')
    deleteVisible.value = false
    deletingAddr.value = null
    loadAddresses()
  } catch {
    // ignore
  } finally {
    deleting.value = false
  }
}
</script>
