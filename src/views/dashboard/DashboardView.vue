<template>
  <PageShell title="经营看板">
    <!-- 筛选区 -->
    <SearchForm :model="filters" @search="refreshAll" @reset="resetFilters">
      <el-form-item label="项目">
        <el-select
          v-model="filters.projectId"
          clearable
          placeholder="全部项目"
          class="search-form__select"
          @change="refreshAll"
        >
          <el-option
            v-for="p in projectOptions"
            :key="p.value"
            :label="p.label"
            :value="p.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="refreshAll"
        />
      </el-form-item>
    </SearchForm>

    <!-- KPI 统计卡片 -->
    <AppState v-if="kpiError" type="error" :action-text="'重试'" @action="refreshAll" />
    <el-row v-else :gutter="16">
      <el-col v-for="card in kpiCards" :key="card.label" :xs="12" :sm="8" :lg="4">
        <div class="metric-card" :class="{ 'metric-card--clickable': Boolean(card.link) }" @click="handleCardClick(card)">
          <span>{{ card.label }}</span>
          <strong :style="{ color: card.color }">
            <template v-if="kpiLoading">
              <el-icon class="is-loading"><Loading /></el-icon>
            </template>
            <template v-else>{{ card.value }}</template>
          </strong>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row v-if="!kpiError" :gutter="16">
      <el-col :xs="24" :lg="12">
        <div class="dashboard-chart" ref="statusChartRef">
          <AppState v-if="chartsLoading" type="loading" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="dashboard-chart" ref="trendChartRef">
          <AppState v-if="chartsLoading" type="loading" />
        </div>
      </el-col>
    </el-row>
    <el-row v-if="!kpiError" :gutter="16">
      <el-col :xs="24" :lg="12">
        <div class="dashboard-chart" ref="projectChartRef">
          <AppState v-if="chartsLoading" type="loading" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="dashboard-chart" ref="materialChartRef">
          <AppState v-if="chartsLoading" type="loading" />
        </div>
      </el-col>
    </el-row>
    <el-row v-if="!kpiError" :gutter="16">
      <el-col :span="24">
        <div class="dashboard-chart" ref="outputChartRef">
          <AppState v-if="chartsLoading" type="loading" />
        </div>
      </el-col>
    </el-row>

    <!-- 待办与预警 -->
    <template v-if="!kpiError">
      <h3>待办与预警</h3>
      <el-row :gutter="16">
        <el-col v-for="alert in alertCards" :key="alert.label" :xs="12" :sm="8" :lg="4">
          <div class="metric-card metric-card--clickable" @click="handleAlertClick(alert)">
            <span>{{ alert.label }}</span>
            <strong :style="{ color: alert.color }">{{ alert.value }}</strong>
            <small v-if="alert.desc">{{ alert.desc }}</small>
          </div>
        </el-col>
      </el-row>
    </template>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Loading } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import {
  fetchMaterialStatistics,
  fetchOutputValue,
  fetchProjectStatistics,
  fetchWorkOrderStatistics
} from '@/api/dashboard';
import PageShell from '@/components/page/PageShell.vue';
import SearchForm from '@/components/form/SearchForm.vue';
import AppState from '@/components/state/AppState.vue';
import type { DashboardOverview, OutputValueItem, ProjectStatItem, MaterialStatItem } from '@/types/dashboard';

const router = useRouter();

/* ========== 筛选 ========== */
const filters = reactive({
  projectId: '' as string | number,
  dateRange: null as [string, string] | null
});
const projectOptions = ref<{ label: string; value: number }[]>([]);

function resetFilters() {
  filters.projectId = '';
  filters.dateRange = null;
  refreshAll();
}

/* ========== KPI 数据 ========== */
const kpiLoading = ref(true);
const kpiError = ref(false);
const overview = ref<DashboardOverview | null>(null);
const completedCount = ref(0);
const totalCount = ref(0);

async function loadKpi() {
  kpiLoading.value = true;
  kpiError.value = false;
  try {
    const data = await fetchWorkOrderStatistics();
    overview.value = data;
    // 从 status breakdown 计算已完成数
    if (data.items?.length) {
      completedCount.value = data.items.reduce((sum, it) => sum + (it.status === 'COMPLETED' ? it.count : 0), 0);
      totalCount.value = data.items.reduce((sum, it) => sum + it.count, 0);
    }
  } catch {
    kpiError.value = true;
  } finally {
    kpiLoading.value = false;
  }
}

const defaultKpiColor = '#1f2937';
interface KpiCard {
  label: string;
  value: string;
  color?: string;
  link?: string;
}

const kpiCards = computed<KpiCard[]>(() => {
  const ov = overview.value;
  return [
    {
      label: '进行中工单',
      value: kpiLoading.value ? '-' : String(ov?.inProgressWorkOrderCount ?? 0),
      color: '#2563eb',
      link: '/work-orders?status=IN_PROGRESS'
    },
    {
      label: '待验收工单',
      value: kpiLoading.value ? '-' : String(ov?.pendingAcceptanceWorkOrderCount ?? 0),
      color: ov?.pendingAcceptanceWorkOrderCount ? '#d97706' : defaultKpiColor,
      link: '/work-orders?status=PENDING_ACCEPTANCE'
    },
    {
      label: '已完成工单',
      value: kpiLoading.value ? '-' : String(completedCount.value),
      color: '#16a34a',
      link: '/work-orders?status=COMPLETED'
    },
    {
      label: '今日出勤',
      value: kpiLoading.value ? '-' : `${ov?.todayAttendanceCount ?? 0} 人`,
      color: '#7c3aed'
    },
    {
      label: '本周产值',
      value: kpiLoading.value ? '-' : formatMoney(ov?.weeklyCompletedOutputValue ?? 0),
      color: '#059669'
    },
    {
      label: '资质预警',
      value: kpiLoading.value ? '-' : String(ov?.abnormalWorkOrderCount ?? 0),
      color: ov?.abnormalWorkOrderCount ? '#dc2626' : defaultKpiColor,
      link: '/qualifications'
    }
  ];
});

function handleCardClick(card: KpiCard) {
  if (card.link) {
    router.push(card.link);
  }
}

/* ========== 图表数据 ========== */
const chartsLoading = ref(true);
const statusChartRef = ref<HTMLDivElement>();
const trendChartRef = ref<HTMLDivElement>();
const projectChartRef = ref<HTMLDivElement>();
const materialChartRef = ref<HTMLDivElement>();
const outputChartRef = ref<HTMLDivElement>();

let statusChart: echarts.ECharts | null = null;
let trendChart: echarts.ECharts | null = null;
let projectChart: echarts.ECharts | null = null;
let materialChart: echarts.ECharts | null = null;
let outputChart: echarts.ECharts | null = null;

async function loadCharts() {
  chartsLoading.value = true;
  try {
    const [projectStats, materialStats, outputValues, workOrderStats] = await Promise.all([
      fetchProjectStatistics(),
      fetchMaterialStatistics(),
      fetchOutputValue(),
      // 如果 KPI 还没加载，再独立拉取一次工单统计给图表用
      overview.value ? Promise.resolve(null) : fetchWorkOrderStatistics()
    ]);

    // 补充 KPI 数据
    if (workOrderStats && !overview.value) {
      overview.value = workOrderStats;
      if (workOrderStats.items?.length) {
        completedCount.value = workOrderStats.items.reduce((sum, it) => sum + (it.status === 'COMPLETED' ? it.count : 0), 0);
        totalCount.value = workOrderStats.items.reduce((sum, it) => sum + it.count, 0);
      }
    }

    const statusItems = overview.value?.items ?? [];
    renderStatusChart(statusItems);
    renderTrendChart(outputValues);
    renderProjectChart(projectStats);
    renderMaterialChart(materialStats);
    renderOutputChart(outputValues);

    // 填充项目筛选选项
    projectOptions.value = projectStats.map((p) => ({ label: p.projectName ?? `项目${p.projectId}`, value: p.projectId }));
  } finally {
    chartsLoading.value = false;
  }
}

/* ========== 图表渲染 ========== */

/** 工单状态分布 — 饼图 */
function renderStatusChart(items: { status: string; count: number }[]) {
  if (!statusChartRef.value) return;
  if (!statusChart) statusChart = echarts.init(statusChartRef.value);
  const statusLabelMap: Record<string, string> = {
    DRAFT: '草稿', PENDING: '待派工', ASSIGNED: '已派工', ACCEPTED: '已接收',
    IN_PROGRESS: '施工中', PENDING_ACCEPTANCE: '待验收', COMPLETED: '已完成',
    REJECTED: '已驳回', CLOSED: '已关闭'
  };
  const data = items.length
    ? items.map((it) => ({ name: statusLabelMap[it.status] ?? it.status, value: it.count }))
    : [{ name: '暂无数据', value: 1 }];

  statusChart.setOption({
    title: { text: '工单状态分布', left: 'center', top: 10, textStyle: { fontSize: 15 } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 5, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['50%', '52%'],
        data,
        label: { show: false },
        emphasis: { label: { show: true } }
      }
    ],
    color: ['#909399', '#e6a23c', '#409eff', '#409eff', '#e6a23c', '#e6a23c', '#67c23a', '#f56c6c', '#909399']
  }, { notMerge: true });
}

/** 近30天工单趋势 — 从产值数据推导 (折线图) */
function renderTrendChart(outputValues: OutputValueItem[]) {
  if (!trendChartRef.value) return;
  if (!trendChart) trendChart = echarts.init(trendChartRef.value);

  const dateMap = new Map<string, number>();
  outputValues.forEach((item) => {
    const key = item.summaryDate ?? '';
    dateMap.set(key, (dateMap.get(key) ?? 0) + 1);
  });
  const entries = [...dateMap.entries()].sort().slice(-30);
  const xData = entries.map(([k]) => k);
  const yData = entries.map(([, v]) => v);

  trendChart.setOption({
    title: { text: '近30天工单活跃趋势', left: 'center', top: 10, textStyle: { fontSize: 15 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 25, bottom: 35, top: 55 },
    xAxis: { type: 'category', data: xData.length ? xData : ['暂无数据'], axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', data: yData.length ? yData : [0], smooth: true, itemStyle: { color: '#2563eb' }, areaStyle: { color: 'rgba(37,99,235,0.1)' } }]
  }, { notMerge: true });
}

/** 项目工单分布 — 柱状图 */
function renderProjectChart(projectStats: ProjectStatItem[]) {
  if (!projectChartRef.value) return;
  if (!projectChart) projectChart = echarts.init(projectChartRef.value);

  const names = projectStats.map((p) => p.projectName ?? `项目${p.projectId}`);
  const totals = projectStats.map((p) => p.total);
  const completeds = projectStats.map((p) => p.completed);

  projectChart.setOption({
    title: { text: '项目工单分布', left: 'center', top: 10, textStyle: { fontSize: 15 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['工单总数', '已完成'], bottom: 5 },
    grid: { left: 55, right: 25, bottom: 40, top: 55 },
    xAxis: { type: 'category', data: names.length ? names : ['暂无数据'], axisLabel: { rotate: 20, interval: 0 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '工单总数', type: 'bar', data: totals.length ? totals : [0], itemStyle: { color: '#409eff' } },
      { name: '已完成', type: 'bar', data: completeds.length ? completeds : [0], itemStyle: { color: '#67c23a' } }
    ]
  }, { notMerge: true });
}

/** 物料消耗排行 — 横向柱状图 Top10 */
function renderMaterialChart(materialStats: MaterialStatItem[]) {
  if (!materialChartRef.value) return;
  if (!materialChart) materialChart = echarts.init(materialChartRef.value);

  const top10 = materialStats.slice(0, 10);
  const names = top10.map((m) => m.materialName);
  const values = top10.map((m) => m.usedQty);

  materialChart.setOption({
    title: { text: '物料消耗排行', left: 'center', top: 10, textStyle: { fontSize: 15 } },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 120, right: 30, bottom: 30, top: 55 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names.length ? names.reverse() : ['暂无数据'], inverse: true },
    series: [{ type: 'bar', data: values.length ? values.reverse() : [0], itemStyle: { color: '#8b5cf6' } }]
  }, { notMerge: true });
}

/** 完工产值趋势 — 折线图 */
function renderOutputChart(outputValues: OutputValueItem[]) {
  if (!outputChartRef.value) return;
  if (!outputChart) outputChart = echarts.init(outputChartRef.value);

  const sorted = [...outputValues].sort((a, b) => (a.summaryDate ?? '').localeCompare(b.summaryDate ?? ''));
  const xData = sorted.map((o) => o.summaryDate ?? '');
  const yData = sorted.map((o) => o.outputValue ?? 0);

  outputChart.setOption({
    title: { text: '完工产值趋势', left: 'center', top: 10, textStyle: { fontSize: 15 } },
    tooltip: { trigger: 'axis', valueFormatter: (val: number) => `¥${val?.toLocaleString()}` },
    grid: { left: 75, right: 25, bottom: 35, top: 55 },
    xAxis: { type: 'category', data: xData.length ? xData : ['暂无数据'], axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', axisLabel: { formatter: (val: number) => `¥${val}` } },
    series: [{ type: 'line', data: yData.length ? yData : [0], smooth: true, itemStyle: { color: '#059669' }, areaStyle: { color: 'rgba(5,150,105,0.1)' } }]
  }, { notMerge: true });
}

/* ========== 待办与预警 ========== */
interface AlertCard {
  label: string;
  value: string;
  desc?: string;
  color?: string;
  link?: string;
}

const alertCards = computed<AlertCard[]>(() => {
  const ov = overview.value;
  return [
    {
      label: '待验收',
      value: String(ov?.pendingAcceptanceWorkOrderCount ?? 0),
      color: ov?.pendingAcceptanceWorkOrderCount ? '#d97706' : defaultKpiColor,
      desc: '施工完成待验收工单',
      link: '/acceptance'
    },
    {
      label: '资质临期/过期',
      value: String(ov?.certificateExpiringCount ?? 0),
      color: ov?.certificateExpiringCount ? '#dc2626' : defaultKpiColor,
      desc: '资质证书即将到期',
      link: '/qualifications'
    },
    {
      label: '库存预警',
      value: String(ov?.inventoryWarningCount ?? 0),
      color: ov?.inventoryWarningCount ? '#dc2626' : defaultKpiColor,
      desc: '可用库存低于安全库存',
      link: '/materials'
    },
    {
      label: '同步冲突',
      value: String(ov?.pendingConflictCount ?? 0),
      color: ov?.pendingConflictCount ? '#dc2626' : defaultKpiColor,
      desc: '待处理同步冲突',
      link: '/sync/conflicts'
    },
    {
      label: 'AI 待复核',
      value: String(ov?.pendingAiReviewCount ?? 0),
      color: ov?.pendingAiReviewCount ? '#d97706' : defaultKpiColor,
      desc: 'AI 识别待人工复核',
      link: '/ai/review'
    },
    {
      label: '完成率',
      value: ov?.completionRate != null ? `${ov.completionRate}%` : '-',
      color: '#16a34a',
      desc: totalCount.value ? `已完成 ${completedCount.value}/${totalCount.value}` : ''
    }
  ];
});

function handleAlertClick(card: AlertCard) {
  if (card.link) {
    router.push(card.link);
  }
}

/* ========== 工具函数 ========== */
function formatMoney(value: number): string {
  if (value >= 10000) {
    return `¥${(value / 10000).toFixed(1)}万`;
  }
  return `¥${value.toLocaleString()}`;
}

/* ========== 生命周期 ========== */
async function refreshAll() {
  // 并行加载 KPI 和图表
  await Promise.all([loadKpi(), loadCharts()]);
}

function handleResize() {
  statusChart?.resize();
  trendChart?.resize();
  projectChart?.resize();
  materialChart?.resize();
  outputChart?.resize();
}

onMounted(() => {
  refreshAll();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  statusChart?.dispose();
  trendChart?.dispose();
  projectChart?.dispose();
  materialChart?.dispose();
  outputChart?.dispose();
});
</script>

<style scoped>
.metric-card--clickable {
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.metric-card--clickable:hover {
  box-shadow: 0 2px 12px rgb(0 0 0 / 10%);
}
.metric-card small {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}
.dashboard-chart {
  position: relative;
  height: 340px;
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}
h3 {
  margin: 8px 0 12px;
  font-size: 16px;
  color: #334155;
}
</style>
