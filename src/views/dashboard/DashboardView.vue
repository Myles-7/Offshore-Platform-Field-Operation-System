<template>
  <PageShell title="经营看板">
    <el-row :gutter="16">
      <el-col v-for="item in cards" :key="item.label" :xs="24" :sm="12" :lg="6">
        <div class="metric-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </el-col>
    </el-row>
    <div ref="chartRef" class="dashboard-chart" />
  </PageShell>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import * as echarts from 'echarts';
import PageShell from '@/components/page/PageShell.vue';

const cards = [
  { label: '待派工工单', value: 0 },
  { label: '施工中工单', value: 0 },
  { label: '待验收工单', value: 0 },
  { label: '待处理冲突', value: 0 }
];

const chartRef = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

onMounted(() => {
  if (!chartRef.value) {
    return;
  }
  chart = echarts.init(chartRef.value);
  chart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: ['项目', '工单', '物料', '资质', 'AI'] },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: [0, 0, 0, 0, 0], itemStyle: { color: '#2563eb' } }]
  });
});

onBeforeUnmount(() => {
  chart?.dispose();
});
</script>
