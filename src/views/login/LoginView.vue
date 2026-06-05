<template>
  <main class="login-view">
    <el-form ref="formRef" class="login-panel" :model="form" :rules="rules" @keyup.enter="handleLogin">
      <h1>海上平台现场作业管理系统</h1>
      <el-form-item prop="loginName">
        <el-input v-model="form.loginName" size="large" placeholder="账号" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" />
      </el-form-item>
      <el-button type="primary" size="large" class="login-panel__button" :loading="loading" @click="handleLogin">
        登录
      </el-button>
      <div class="login-panel__tips">
        <strong>测试账号</strong>
        <span>管理员：admin / 123456</span>
        <span>项目经理：pm_zhang / 123456</span>
        <span>物资：material_wang / 123456</span>
        <span>资质：qualification_chen / 123456</span>
        <span>验收：acceptor_zhao / 123456</span>
        <span>经营：business_sun / 123456</span>
      </div>
    </el-form>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { getDefaultHome } from '@/constants/access';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const loading = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  loginName: '',
  password: ''
});

const rules: FormRules = {
  loginName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

async function handleLogin() {
  await formRef.value?.validate();
  loading.value = true;
  try {
    const user = await authStore.login(form);
    const redirect = String(route.query.redirect || '');
    router.replace(redirect && redirect !== '/403' && redirect !== '/404' ? redirect : getDefaultHome(user));
  } finally {
    loading.value = false;
  }
}
</script>
