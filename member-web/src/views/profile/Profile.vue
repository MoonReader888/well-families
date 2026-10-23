<template>
  <div class="page">
    <!-- 用户信息 -->
    <div class="user-card">
      <img v-if="avatarUrl" :src="avatarUrl" class="user-avatar" alt="avatar" />
      <van-icon v-else name="contact" size="50" color="#fff" />
      <div class="user-info">
        <div class="user-name">{{ userStore.user?.realName || '亲爱的用户' }}</div>
        <div class="user-sub">{{ userStore.user?.phone }}</div>
        <van-tag plain type="primary">{{ levelTextMap[userStore.memberLevel] || '普通' }}</van-tag>
      </div>
      <div class="points-box">
        <div class="points-num">{{ userStore.points }}</div>
        <div class="points-label">积分</div>
      </div>
    </div>

    <!-- 功能列表 -->
    <van-cell-group inset class="menu">
      <van-cell title="编辑资料" icon="edit" is-link @click="openEdit" />
      <van-cell title="修改密码" icon="lock" is-link @click="router.push('/profile/password')" />
    </van-cell-group>

    <div class="logout-wrap">
      <van-button round block plain type="danger" class="big-btn" @click="onLogout">退出登录</van-button>
    </div>

    <!-- 编辑资料弹窗 -->
    <van-popup v-model:show="showEdit" position="bottom" round safe-area-inset-bottom>
      <van-form @submit="onSave" class="edit-form">
        <van-cell-group inset>
          <div class="avatar-row">
            <div class="avatar-label">头像</div>
            <van-uploader
              v-model="fileList"
              :max-count="1"
              :max-size="5 * 1024 * 1024"
              :after-read="onAfterRead"
              :before-delete="onAvatarDelete"
              accept="image/*"
              :preview-full-image="true"
              round
            />
          </div>
          <van-field v-model="form.realName" label="姓名" placeholder="请输入姓名" />
          <van-field label="性别">
            <template #input>
              <van-radio-group v-model="form.gender" direction="horizontal">
                <van-radio name="男">男</van-radio>
                <van-radio name="女">女</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field label="出生日期">
            <template #input>
              <div class="birth-inputs">
                <input :value="form.birthYear" type="text" inputmode="numeric" maxlength="4" placeholder="1950" class="birth-input" @input="onBirthInput('birthYear', 4, $event)" />
                <span class="birth-suffix">年</span>
                <input :value="form.birthMonth" type="text" inputmode="numeric" maxlength="2" placeholder="8" class="birth-input birth-input-sm" @input="onBirthInput('birthMonth', 2, $event)" />
                <span class="birth-suffix">月</span>
                <input :value="form.birthDay" type="text" inputmode="numeric" maxlength="2" placeholder="20" class="birth-input birth-input-sm" @input="onBirthInput('birthDay', 2, $event)" />
                <span class="birth-suffix">日</span>
              </div>
            </template>
          </van-field>
          <van-field v-model="form.height" label="身高(cm)" type="number" placeholder="用于计算 BMI" />
          <van-field v-model="form.emergencyContact" label="紧急联系人" type="tel" maxlength="11" placeholder="请输入紧急联系人电话" />
        </van-cell-group>
        <div class="actions">
          <van-button round block type="primary" native-type="submit" class="big-btn">保存</van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'
import { getProfile, updateProfile, uploadAvatar } from '../../api/profile'
import { logout } from '../../api/auth'
import { useUserStore } from '../../store/user'
import { levelTextMap } from '../../utils/format'

const router = useRouter()
const userStore = useUserStore()

const showEdit = ref(false)
const avatarUrl = ref('')
const fileList = ref([])
const form = reactive({ realName: '', gender: '', birthYear: '', birthMonth: '', birthDay: '', height: '', emergencyContact: '', avatar: '' })

// 出生日期：三个独立输入框，只保留数字
function onBirthInput(part, max, e) {
  form[part] = e.target.value.replace(/\D/g, '').slice(0, max)
}

function daysInMonth(y, m) {
  return new Date(y, m, 0).getDate()
}

function validateBirth() {
  const y = form.birthYear.trim()
  const m = form.birthMonth.trim()
  const d = form.birthDay.trim()
  if (!y && !m && !d) return { ok: true, value: '' }
  if (!/^\d{4}$/.test(y)) return { ok: false, msg: '出生年份请填 4 位数字' }
  if (+y < 1900 || +y > new Date().getFullYear()) return { ok: false, msg: '出生年份超出合理范围' }
  if (!/^\d{1,2}$/.test(m) || +m < 1 || +m > 12) return { ok: false, msg: '出生月份需为 1-12 的整数' }
  if (!/^\d{1,2}$/.test(d) || +d < 1 || +d > daysInMonth(+y, +m)) return { ok: false, msg: `出生日需为 1-${daysInMonth(+y, +m)} 的整数` }
  return { ok: true, value: `${y}-${String(+m).padStart(2, '0')}-${String(+d).padStart(2, '0')}` }
}

/** 拉取数据库中的当前资料并回显到表单（含头像）；编辑弹窗打开时也刷新，保证回显最新数据 */
async function loadProfile() {
  const res = await getProfile()
  const u = res.data
  userStore.setUser(u)
  avatarUrl.value = resolveAvatarUrl(u.avatar)
  Object.assign(form, {
    realName: u.realName || '',
    gender: u.gender || '',
    height: u.height || '',
    emergencyContact: u.emergencyContact || '',
    avatar: u.avatar || ''
  })
  const [y, m, d] = (u.birthDate || '').split('-')
  form.birthYear = y || ''
  form.birthMonth = m ? String(+m) : ''
  form.birthDay = d ? String(+d) : ''
  fileList.value = form.avatar ? [{ url: resolveAvatarUrl(form.avatar), isImage: true }] : []
}

async function openEdit() {
  showEdit.value = true
  try {
    await loadProfile()
  } catch {
    /* 忽略 */
  }
}

onMounted(async () => {
  try {
    await loadProfile()
  } catch {
    /* 忽略 */
  }
})

async function onSave() {
  const birth = validateBirth()
  if (!birth.ok) return showFailToast(birth.msg)

  const payload = {}
  const fields = ['realName', 'gender', 'height', 'emergencyContact', 'avatar']
  fields.forEach((k) => {
    if (form[k] !== '' && form[k] != null) payload[k] = form[k]
  })
  if (birth.value) payload.birthDate = birth.value
  // 头像未上传时不提交 avatar 字段，避免覆盖已有头像
  if (!form.avatar) delete payload.avatar
  await updateProfile(payload)
  const res = await getProfile()
  userStore.setUser(res.data)
  avatarUrl.value = resolveAvatarUrl(res.data.avatar)
  showEdit.value = false
  showSuccessToast('已保存')
}

async function onAfterRead(item) {
  const file = item.file
  if (!file) return
  try {
    const res = await uploadAvatar(file)
    form.avatar = res.data.path
    fileList.value = [{ url: res.data.url, isImage: true }]
    showSuccessToast('头像上传成功')
  } catch {
    fileList.value = []
    form.avatar = ''
  }
}

function onAvatarDelete() {
  form.avatar = ''
  fileList.value = []
  return true
}

function resolveAvatarUrl(path) {
  if (!path) return ''
  return path.startsWith('http') ? path : '/uploads/' + path
}

async function onLogout() {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定退出当前账号吗？' })
  } catch {
    return
  }
  try {
    await logout(userStore.refreshToken)
  } catch {
    /* 忽略登出失败 */
  }
  userStore.clearLogin()
  router.replace('/login')
}
</script>

<style scoped>
.user-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  border-radius: 12px;
  padding: 24px 16px;
  color: #fff;
  margin-bottom: 12px;
}
.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}
.user-info {
  flex: 1;
}
.user-name {
  font-size: 1.3rem;
  font-weight: 600;
}
.user-sub {
  color: rgba(255, 255, 255, 0.85);
  margin: 4px 0 6px;
}
.points-box {
  text-align: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  padding: 8px 16px;
}
.points-num {
  font-size: 1.5rem;
  font-weight: 700;
}
.points-label {
  font-size: 0.8rem;
}
.menu {
  margin-top: 12px;
}
.menu :deep(.van-cell) {
  min-height: 60px;
  font-size: 1.1rem;
  padding: 14px 16px;
}
.menu :deep(.van-cell__left-icon) {
  font-size: 24px;
}
.menu :deep(.van-cell__right-icon) {
  font-size: 18px;
}
.logout-wrap {
  padding: 24px 16px;
}
.edit-form {
  padding-bottom: 20px;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
}
.avatar-label {
  width: 6.2em;
  color: #323233;
  font-size: 14px;
}
.birth-inputs {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}
.birth-input {
  width: 56px;
  border: 1px solid #ebedf0;
  border-radius: 4px;
  background: #f7f8fa;
  padding: 6px 8px;
  font-size: 16px;
  text-align: center;
  color: #323233;
}
.birth-input-sm {
  width: 44px;
}
.birth-suffix {
  color: #969799;
  font-size: 14px;
}
</style>
