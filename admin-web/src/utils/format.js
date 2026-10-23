/**
 * 管理端通用格式化
 */
export function parseItems(items) {
  if (!items) return []
  try {
    const arr = typeof items === 'string' ? JSON.parse(items) : items
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

export function parseOptions(options) {
  if (!options) return []
  try {
    const arr = typeof options === 'string' ? JSON.parse(options) : options
    if (!Array.isArray(arr)) return []
    // 兼容旧版纯字符串选项：归一化为 {label, score}
    return arr.map((o) =>
      typeof o === 'string' ? { label: o, score: 0 } : { label: o?.label || '', score: o?.score ?? 0 }
    )
  } catch {
    return []
  }
}

export const memberLevelOptions = ['NORMAL', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND']
