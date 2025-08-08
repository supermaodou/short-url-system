<template>
  <div class="container">
    <h1>短链接生成器</h1>
    <ShortUrlForm />
  </div>
</template>

<script setup>
import ShortUrlForm from './components/ShortUrlForm.vue'
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
</style>