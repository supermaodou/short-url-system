<template>
  <el-form :model="form" ref="formRef" :rules="rules" @submit.prevent="handleSubmit">
    <el-form-item prop="longUrl">
      <el-input
        v-model="form.longUrl"
        placeholder="请输入长链接"
        clearable
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSubmit">生成短链接</el-button>
    </el-form-item>
    <el-form-item v-if="shortUrl">
      <el-input
        v-model="shortUrl"
        readonly
      >
        <template #append>
          <el-button @click="copyToClipboard">复制</el-button>
        </template>
      </el-input>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const formRef = ref(null)
const form = reactive({
  longUrl: ''
})
const shortUrl = ref('')

const rules = {
  longUrl: [
    { required: true, message: '请输入长链接', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    console.log('Sending URL:', form.longUrl, 'Length:', form.longUrl.length);
    const response = await axios.post('/api/shorten', form.longUrl, {
      headers: { 'Content-Type': 'text/plain; charset=UTF-8' },
      transformRequest: [(data) => data], // 防止 Axios 自动编码
    });
    shortUrl.value = response.data;
    ElMessage.success('短链接生成成功！');
  } catch (error) {
    ElMessage.error(error.response?.data || '生成短链接失败：' + error.message);
  }
};

const copyToClipboard = () => {
  navigator.clipboard.writeText(shortUrl.value)
  ElMessage.success('已复制到剪贴板')
}
</script>