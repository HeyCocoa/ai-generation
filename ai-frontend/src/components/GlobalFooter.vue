<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      <p class="copyright">
        AI码生成平台 by 程序员kokoaaa
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的逻辑
</script>

<style scoped>
.global-footer {
  background: #001529;
  color: #fff;
  text-align: center;
  padding: 16px 24px;
  position: relative;
  bottom: 0;
  width: 100%;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-content {
  width: 80%;
  margin: 0 auto;
}

.copyright {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.65);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-footer {
    padding: 12px 16px;
    height: 60px;
  }
  
  .copyright {
    font-size: 12px;
  }
}
</style>
