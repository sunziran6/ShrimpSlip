<template>
  <div class="max-w-[1200px] mx-auto px-6 py-8">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-foreground">商品管理</h2>
      <button
        @click="openDialog(null)"
        class="px-5 py-2.5 rounded-xl bg-primary text-primary-foreground font-medium hover:bg-primary/85 transition-all flex items-center gap-2"
      >
        <el-icon :size="16"><Plus /></el-icon>
        新增商品
      </button>
    </div>

    <!-- Table -->
    <div class="bg-secondary rounded-xl border border-border overflow-hidden">
      <el-table :data="products" style="width: 100%" size="large" row-key="id" v-loading="loading">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <img v-if="row.image" :src="row.image" class="w-12 h-12 rounded-lg object-cover" />
            <div v-else class="w-12 h-12 rounded-lg bg-muted flex items-center justify-center text-muted-foreground text-xs">无图</div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该商品？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div class="flex justify-center mt-6" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadProducts"
      />
    </div>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑商品' : '新增商品'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-position="top" ref="formRef" size="large">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入商品描述" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="价格" required>
              <el-input-number v-model="form.price" :min="0" :precision="2" class="!w-full" placeholder="0.00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" required>
              <el-input-number v-model="form.stock" :min="0" class="!w-full" placeholder="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" class="!w-full" placeholder="请选择">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" class="!w-full">
                <el-option :value="1" label="上架" />
                <el-option :value="0" label="下架" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品图片">
          <div class="flex items-center gap-3">
            <img v-if="form.image" :src="form.image" class="w-20 h-20 rounded-lg object-cover" />
            <div v-else class="w-20 h-20 rounded-lg bg-muted flex items-center justify-center text-muted-foreground text-xs">无图片</div>
            <input ref="imageInput" type="file" accept="image/*" class="hidden" @change="handleImageChange" />
            <el-button size="small" @click="imageInput?.click()" :loading="uploadingImage">
              {{ form.image ? '更换图片' : '上传图片' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../api/request'
import { uploadFile } from '../../api/user'

const products = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const form = reactive({ name: '', description: '', price: 0, stock: 0, category: '', status: 1, image: '' })
const categories = ['电子产品', '服装鞋帽', '食品饮料', '家居用品']

const imageInput = ref(null)
const uploadingImage = ref(false)

onMounted(() => loadProducts())

async function loadProducts() {
  loading.value = true
  try {
    const res = await request.get('/products', { params: { page: page.value, size: size.value } })
    products.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editing.value = row
  if (row) {
    Object.assign(form, { name: row.name, description: row.description || '', price: row.price, stock: row.stock, category: row.category || '', status: row.status, image: row.image || '' })
  } else {
    Object.assign(form, { name: '', description: '', price: 0, stock: 0, category: '', status: 1, image: '' })
  }
  dialogVisible.value = true
}

async function handleImageChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  uploadingImage.value = true
  try {
    const res = await uploadFile(file)
    form.image = res.data
  } catch {
    ElMessage.error('图片上传失败')
  } finally {
    uploadingImage.value = false
    if (imageInput.value) imageInput.value.value = ''
  }
}

async function handleSave() {
  if (!form.name.trim()) { ElMessage.warning('请输入商品名称'); return }
  saving.value = true
  try {
    const data = { ...form, version: editing.value?.version }
    if (editing.value) {
      await request.put(`/admin/products/${editing.value.id}`, data)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/products', data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadProducts()
  } catch (e) {
    // ignore
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await request.delete(`/admin/products/${id}`)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (e) {
    // ignore
  }
}
</script>
