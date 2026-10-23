/**
 * 通用格式化工具
 */
export const appointmentStatusMap = {
  PENDING: { text: '待确认', color: '#ff976a' },
  CONFIRMED: { text: '已确认', color: 'var(--primary-color)' },
  CANCELED: { text: '已取消', color: '#c8c9cc' },
  COMPLETED: { text: '已完成', color: '#07c160' }
}

export const activityStatusMap = {
  DRAFT: '草稿',
  NOT_STARTED: '未开始',
  REGISTRATING: '报名中',
  IN_PROGRESS: '进行中',
  ENDED: '已结束'
}

export const messageTypeMap = {
  APPOINTMENT: '预约',
  ACTIVITY: '活动',
  SYSTEM: '系统',
  HEALTH_REMINDER: '健康提醒'
}

export const levelTextMap = {
  NORMAL: '普通',
  SILVER: '银卡',
  GOLD: '金卡',
  PLATINUM: '铂金',
  DIAMOND: '钻石'
}

export const checkInStatusMap = {
  NOT_CHECKED_IN: '未签到',
  CHECKED_IN: '已签到'
}

export function fmtTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

export function fmtDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10)
}

/** 把后端返回的图片路径解析为可访问 URL（兼容完整 URL/相对路径/已带 /uploads 前缀） */
export function resolveImageUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//i.test(path)) return path
  if (path.startsWith('/uploads/')) return path
  if (path.startsWith('/')) return path
  return '/uploads/' + path
}

/** BMI 分类文案 */
export function bmiText(bmi) {
  if (bmi == null) return '-'
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '偏胖'
  return '肥胖'
}

/** 体检项目数组（后端存 JSON 字符串） */
export function parseItems(items) {
  if (!items) return []
  try {
    const arr = typeof items === 'string' ? JSON.parse(items) : items
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}
