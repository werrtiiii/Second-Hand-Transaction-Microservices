/**
 * HTTP 请求封装。
 *
 * 后端统一返回 ApiResponse<T> { success: boolean, data: T, error?: { code, message } }，
 * 本模块会自动解包：成功时返回 data，失败时抛出带 message 的 Error。
 */

const BASE = ''
const pendingOrders = new Map() // 仅保留结果未知的下单键，避免网络重试重复下单

export async function api(path, { method = 'GET', body, auth = true, raw = false, idempotencyKey } = {}) {
  const headers = {}

  // raw body (e.g. FormData) — don't set Content-Type
  if (!raw) {
    headers['Content-Type'] = 'application/json'
  }

  if (auth) {
    const token = localStorage.getItem('token')
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
  }

  const orderFingerprint = method === 'POST' && path === '/api/orders'
    ? `${localStorage.getItem('userId') || ''}:${JSON.stringify(body)}` : null
  if (orderFingerprint) {
    if (!pendingOrders.has(orderFingerprint)) pendingOrders.set(orderFingerprint, idempotencyKey || crypto.randomUUID())
    headers['Idempotency-Key'] = idempotencyKey || pendingOrders.get(orderFingerprint)
  }

  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: raw ? body : (body ? JSON.stringify(body) : undefined),
  })

  if (orderFingerprint && res.status !== 202 && res.status < 500) pendingOrders.delete(orderFingerprint)

  // 解析响应体
  let json
  try {
    json = await res.json()
  } catch {
    if (!res.ok) {
      throw new Error(res.statusText || '请求失败')
    }
    return null
  }

  // 标准 ApiResponse 格式：{ success, data, error }
  if (json.success === undefined) {
    if (!res.ok) {
      throw new Error(json.message || res.statusText || '请求失败')
    }
    return json
  }

  if (!json.success) {
    const msg = json.error?.message || '请求失败'
    throw new Error(msg)
  }

  return json.data
}
