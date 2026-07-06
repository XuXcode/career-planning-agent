<template>
  <div>
    <!-- File Upload -->
    <div class="upload-area">
      <div class="upload-row">
        <label class="upload-btn">
          📄 上传简历 (PDF/TXT)
          <input type="file" accept=".pdf,.txt" @change="handleFileUpload" hidden />
        </label>
        <button class="upload-btn" @click="showTextInput = !showTextInput">
          ✏️ 输入文本
        </button>
      </div>
      <div v-if="showTextInput" class="text-input-area">
        <textarea v-model="textContent" rows="6" placeholder="请在此输入或粘贴您的简历文本..."></textarea>
        <button class="btn-submit" @click="uploadText" :disabled="!textContent.trim()">提交文本</button>
      </div>
      <p v-if="uploadMsg" :class="['upload-msg', uploadOk ? 'ok' : 'err']">{{ uploadMsg }}</p>
    </div>

    <!-- Generate Buttons -->
    <div class="generate-area">
      <button class="btn-generate" @click="startSSEAnalysis" :disabled="isStreaming">
        {{ isStreaming ? '⏳ 分析中...' : '🚀 生成报告 (SSE 流式)' }}
      </button>
      <button class="btn-generate secondary" @click="startSyncAnalysis" :disabled="syncing" style="margin-left:10px;">
        {{ syncing ? '⏳ 同步中...' : '🔄 同步生成 (兼容)' }}
      </button>
    </div>

    <!-- Stream Progress -->
    <StreamProgress v-if="isStreaming" :phase="analysisStore.phase" />

    <!-- Error -->
    <div v-if="analysisStore.error" class="error-banner">{{ analysisStore.error }}</div>

    <!-- Report -->
    <div v-if="report" class="report-area">
      <div class="kpi-row">
        <KpiCard label="完整度" :value="report.studentProfile?.completenessScore" suffix="%" />
        <KpiCard label="竞争力" :value="report.studentProfile?.competitivenessScore" suffix="%" />
        <KpiCard label="总体匹配度" :value="report.jobMatchAnalysis?.overallMatchScore" suffix="%" />
        <KpiCard label="匹配岗位数" :value="report.jobMatchAnalysis?.matchedJobs?.length || 0" />
      </div>

      <StudentProfile v-if="report.studentProfile" :profile="report.studentProfile" />
      <MatchRadar v-if="report" :report="report" />
      <MatchTable v-if="report.jobMatchAnalysis?.matchedJobs?.length" :jobs="report.jobMatchAnalysis.matchedJobs" />
      <CareerPath v-if="report.careerPathPlan" :path="report.careerPathPlan" />
      <ActionPlan v-if="report.actionPlan" :plan="report.actionPlan" />

      <!-- Export -->
      <div class="export-area">
        <button class="btn-export" @click="exportPDF">📥 导出 PDF</button>
        <button class="btn-export" @click="exportWord">📝 导出 Word</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { useAnalysisStore } from '../../stores/analysis'
import { analysisApi } from '../../api/analysis'
import { exportApi, downloadBlob } from '../../api/export'
import StreamProgress from './StreamProgress.vue'
import KpiCard from '../common/KpiCard.vue'
import StudentProfile from './StudentProfile.vue'
import MatchRadar from './MatchRadar.vue'
import MatchTable from './MatchTable.vue'
import CareerPath from './CareerPath.vue'
import ActionPlan from './ActionPlan.vue'

const auth = useAuthStore()
const analysisStore = useAnalysisStore()
const showTextInput = ref(false)
const textContent = ref('')
const uploadMsg = ref('')
const uploadOk = ref(true)
const syncing = ref(false)

const isStreaming = computed(() => analysisStore.isStreaming)
const report = computed(() => analysisStore.report || analysisStore.fullReport)

async function handleFileUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  const fd = new FormData()
  fd.append('resume', file)
  try {
    await analysisApi.uploadResume(fd)
    uploadMsg.value = '文件上传成功！'
    uploadOk.value = true
  } catch (e) {
    uploadMsg.value = '上传失败: ' + (e.response?.data?.message || e.message)
    uploadOk.value = false
  }
}

async function uploadText() {
  try {
    await analysisApi.uploadText(textContent.value)
    uploadMsg.value = '文本提交成功！'
    uploadOk.value = true
    showTextInput.value = false
  } catch (e) {
    uploadMsg.value = '提交失败: ' + (e.response?.data?.message || e.message)
    uploadOk.value = false
  }
}

function startSSEAnalysis() {
  analysisStore.reset()
  analysisStore.startStream(auth.token)
}

async function startSyncAnalysis() {
  syncing.value = true
  analysisStore.reset()
  try {
    const res = await analysisApi.generateReport()
    const data = res.data?.data || res.data
    analysisStore.setReport(data)
  } catch (e) {
    analysisStore.error = e.response?.data?.message || '同步分析失败'
  } finally {
    syncing.value = false
  }
}

async function exportPDF() {
  try {
    const res = await exportApi.exportPDF()
    downloadBlob(res.data, '职业规划报告.pdf')
  } catch (e) {
    if (e.response?.status === 501) {
      toastMsg('服务端导出开发中，使用浏览器截图导出')
      await clientSideExport('pdf')
    } else {
      toastMsg('PDF 导出失败')
    }
  }
}

async function exportWord() {
  try {
    const res = await exportApi.exportWord()
    downloadBlob(res.data, '职业规划报告.docx')
  } catch (e) {
    if (e.response?.status === 501) {
      toastMsg('服务端导出开发中，暂无 Word 导出')
    } else {
      toastMsg('Word 导出失败')
    }
  }
}

// Simple client-side fallback using html2canvas + jsPDF (loaded via CDN in index.html)
async function clientSideExport() {
  try {
    const { default: html2canvas } = await import('html2canvas')
    const { default: jsPDF } = await import('jspdf')
    const el = document.querySelector('.report-area')
    if (!el) return
    const canvas = await html2canvas(el, { scale: 2 })
    const pdf = new jsPDF('p', 'mm', 'a4')
    const w = pdf.internal.pageSize.getWidth()
    const h = (canvas.height * w) / canvas.width
    pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, w, h)
    pdf.save('职业规划报告.pdf')
  } catch { toastMsg('客户端导出失败，请重试') }
}

function toastMsg(msg) {
  uploadMsg.value = msg
  uploadOk.value = false
  setTimeout(() => { uploadMsg.value = '' }, 4000)
}
</script>

<style scoped>
.upload-area { margin-bottom: 20px; }
.upload-row { display: flex; gap: 10px; flex-wrap: wrap; }
.upload-btn {
  padding: 10px 20px; border-radius: var(--radius-sm); font-size: 14px; font-weight: 600;
  background: var(--card-solid); border: 1px solid var(--line); cursor: pointer; transition: all .2s;
}
.upload-btn:hover { border-color: var(--primary); color: var(--primary); }
.text-input-area { margin-top: 12px; }
.text-input-area textarea {
  width: 100%; padding: 12px; border: 1px solid var(--line); border-radius: var(--radius-sm);
  font-size: 13px; resize: vertical; outline: none;
}
.text-input-area textarea:focus { border-color: var(--primary); }
.btn-submit {
  margin-top: 8px; padding: 8px 20px; border-radius: var(--radius-sm);
  background: var(--primary); color: #fff; font-size: 13px; font-weight: 600;
}
.btn-submit:disabled { opacity: .5; }
.upload-msg { margin-top: 8px; font-size: 13px; }
.upload-msg.ok { color: var(--success); }
.upload-msg.err { color: var(--danger); }

.generate-area { margin-bottom: 24px; }
.btn-generate {
  padding: 12px 28px; border-radius: var(--radius-sm); font-size: 15px; font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--primary-2));
  color: #fff; box-shadow: 0 4px 16px rgba(79,142,247,.3); transition: all .2s;
}
.btn-generate:disabled { opacity: .5; cursor: wait; }
.btn-generate:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(79,142,247,.4); }
.btn-generate.secondary {
  background: var(--card-solid); color: var(--primary); border: 1px solid var(--primary); box-shadow: none;
}
.btn-generate.secondary:hover:not(:disabled) { background: var(--primary-soft); }

.error-banner {
  padding: 12px 16px; background: #fef2f2; border: 1px solid #fecaca;
  color: var(--danger); border-radius: var(--radius-sm); margin-bottom: 16px; font-size: 14px;
}

.report-area { margin-top: 20px; }
.kpi-row { display: flex; gap: 14px; margin-bottom: 20px; flex-wrap: wrap; }

.export-area { display: flex; gap: 12px; margin-top: 28px; padding-top: 20px; border-top: 1px solid var(--line); }
.btn-export {
  padding: 10px 24px; border-radius: var(--radius-sm); font-size: 14px; font-weight: 600;
  background: var(--card-solid); border: 1px solid var(--line); transition: all .2s;
}
.btn-export:hover { border-color: var(--primary); color: var(--primary); background: var(--primary-soft); }
</style>
