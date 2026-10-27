# URL to APK 打包工具

<p align="center">
  <strong>使用 GitHub Action 将任意网站 URL 一键打包成 Android APK 应用</strong>
</p>

<p align="center">
  <a href="https://github.com/wangpeng258/UrlToApk/stargazers"><img src="https://img.shields.io/github/stars/wangpeng258/UrlToApk?style=social" alt="GitHub Stars"></a>
  <a href="https://github.com/wangpeng258/UrlToApk/network/members"><img src="https://img.shields.io/github/forks/wangpeng258/UrlToApk?style=social" alt="GitHub Forks"></a>
  <a href="https://github.com/wangpeng258/UrlToApk/issues"><img src="https://img.shields.io/github/issues/wangpeng258/UrlToApk" alt="GitHub Issues"></a>
  <a href="https://github.com/wangpeng258/UrlToApk/blob/main/LICENSE"><img src="https://img.shields.io/github/license/wangpeng258/UrlToApk" alt="License"></a>
</p>

<p align="center">
  <a href="https://github.com/wangpeng258/UrlToApk/actions"><img src="https://img.shields.io/github/actions/workflow/status/wangpeng258/UrlToApk/build.yml?label=Build&logo=github" alt="Build Status"></a>
  <img src="https://img.shields.io/badge/platform-Android-brightgreen?logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/language-Java-orange?logo=openjdk" alt="Language">
  <a href="https://github.com/wangpeng258/UrlToApk/releases"><img src="https://img.shields.io/github/v/release/wangpeng258/UrlToApk?include_prereleases" alt="Latest Release"></a>
  <img src="https://img.shields.io/github/repo-size/wangpeng258/UrlToApk" alt="Repo Size">
  <img src="https://img.shields.io/github/last-commit/wangpeng258/UrlToApk" alt="Last Commit">
</p>

<p align="center">
  <a href="./README.md">🇨🇳 中文</a> | <a href="./README_EN.md">🇺🇸 English</a>
</p>

---

## 📖 项目简介

UrlToApk 是一个零代码 Android 应用生成工具，只需提供一个网站 URL，即可通过 GitHub Actions 自动构建生成一个原生 Android APK。支持自定义应用名称、图标、主题色、首屏动画等，同时内置丰富的 JavaScript Bridge API，让 Web 页面可以调用原生设备功能。

## ✨ 功能特性

| 分类 | 功能 |
|------|------|
| 🎨 自定义配置 | 网站 URL、应用名称、应用包名、主题色、应用图标 |
| 🖼️ 首屏动画 | 自定义首屏图片或使用应用图标，支持淡入淡出动画 |
| 🔐 签名证书 | 自定义签名证书信息（CN/OU/O/L/ST/C） |
| 🔙 返回键 | 二次确认退出（Toast 提示方式） |
| 🌉 JS Bridge | 40+ 原生 API 调用（设备信息、截图、定位、二维码、全屏、事件监听等） |
| 📸 截图 | 普通截图与全屏长截图 |
| 💾 存储 | 保存图片到相册、本地 KV 存储 |
| 📺 全屏 | 全屏模式切换 |
| ⚙️ 系统 | 系统设置快捷入口、权限查询 |
| 🎮 事件 | 按键事件监听、应用退出事件监听 |

## 🚀 使用方法

### 1. Fork 本仓库

点击右上角的 Fork 按钮，将本仓库 Fork 到您的 GitHub 账号下。

### 2. 运行 GitHub Action

1. 进入 Actions 页面
2. 选择 "Build Android APK" 工作流
3. 点击 "Run workflow"
4. 填写以下参数：

#### 基本参数
| 参数 | 说明 | 必填 | 默认值 |
|------|------|------|--------|
| 网站URL | 要打包的网站地址 | ✅ | - |
| 应用名称 | 显示在手机上的应用名称 | ✅ | My App |
| 应用包名 | Android 应用的唯一标识 | ✅ | com.webview.app |
| 主题色 | 状态栏和主题颜色 | ❌ | #2196F3 |
| 应用图标URL | 公开可访问的 PNG 图片地址 | ❌ | https://wmimg.com/i/1792/2025/12/692e96b4c100f.png |

#### 首屏加载动画参数
| 参数 | 说明 | 必填 | 默认值 |
|------|------|------|--------|
| 首屏图片URL | 自定义首屏图片的 URL 地址（填写则使用自定义图片，不填则使用应用图标） | ❌ | - |

> **首屏图片逻辑说明**: 
> - 如果填写了首屏图片URL，当像素尺寸足以覆盖设备屏幕时将无圆角铺满显示；图片分辨率不足时等比居中且不放大（周围可能留白），避免放大导致模糊
> - 如果未填写首屏图片URL，将使用应用图标居中显示，白色背景，图标下方显示应用名称，图标圆角半径为10%
> - 首屏图片至少显示2秒钟，WebView加载完成后500ms淡出动画
> - 显示首屏期间禁止用户进行任何操作

#### 证书信息参数
| 参数 | 说明 | 默认值 |
|------|------|--------|
| 证书姓名 (CN) | 证书持有者姓名 | wppApk |
| 证书组织单位 (OU) | 组织单位名称 | hangzhoushiqi |
| 证书组织 (O) | 组织名称 | shiqi |
| 证书城市 (L) | 所在城市 | zhejiang |
| 证书省份 (ST) | 所在省份 | hangzhou |
| 证书国家 (C) | 国家代码 | CN |

### 3. 下载 APK

工作流完成后，您可以：
- 在 Actions 运行结果中下载 Artifacts
- 在 Releases 页面下载发布的 APK

## 🖼️ 首屏加载动画

应用启动时会显示首屏加载动画，提升用户体验：

### 自定义首屏图片模式
- 在 GitHub Action 中提供首屏图片的 URL 地址
- 图片分辨率足够时铺满整个屏幕（无圆角）；分辨率不足时不强制放大，保持原始像素清晰度，周围可能留白
- 支持 PNG 和 JPEG 格式；优先使用高清 PNG 或高质量 JPEG 原图
- **建议**：按目标设备的物理像素尺寸提供图片（例如 1080×2400 或更高，且宽高比接近手机屏幕）。低分辨率素材无法通过 App 解码恢复丢失的细节。
- 首屏图片在最上层显示，遮挡状态栏、导航栏和WebView

### 默认图标模式
- 如未提供首屏图片URL，自动使用应用图标
- 构建时保留应用图标的高清原图作为首屏资源，避免使用最高仅 192×192 的桌面图标资源放大显示
- 若原图分辨率不足，则不再强制放大（可能出现留白）；未提供图标 URL 时仍使用内置桌面图标作为回退
- 白色背景，图标居中显示
- 图标圆角半径为图标宽度的10%
- 图标下方显示应用名称

### 显示时间逻辑
- 首屏图片至少显示2秒钟
- WebView加载完成后，如果已超过2秒，则开始500ms淡出动画
- 如果WebView加载未完成，首屏继续显示直到加载完成
- 显示首屏期间，禁止用户进行任何操作（点击、滚动等）

## 🌉 JavaScript Bridge API

本项目内置了完整的 JavaScript Bridge，允许 Web 页面通过 `NativeBridge` 全局对象调用 Android 原生能力。Bridge 在 WebView 加载完成后自动注入，无需额外配置。

### 初始化

```javascript
// 方式一：监听 NativeBridgeReady 事件
document.addEventListener('NativeBridgeReady', function() {
  // NativeBridge 已就绪，可以安全调用所有 API
});

// 方式二：使用回调函数
window.onNativeBridgeReady = function() {
  // NativeBridge 已就绪
};
```

> **重要**: 所有 `NativeBridge` API 必须在 `NativeBridgeReady` 事件触发后调用，否则可能抛出 `undefined` 错误。

---

### API 总览

| 分类 | 方法 | 说明 |
|------|------|------|
| **UI 交互** | `showToast(message)` | 显示 Toast 消息 |
| | `showLoading(message)` | 显示加载对话框 |
| | `hideLoading()` | 隐藏加载对话框 |
| | `showAlert(title, message, callback)` | 显示提示对话框 |
| | `showConfirm(title, message, callback)` | 显示确认对话框 |
| | `setStatusBarColor(color)` | 设置状态栏颜色 |
| | `setTitle(title)` | 设置应用标题 |
| **设备信息** | `getDeviceInfo()` | 获取基本设备信息 |
| | `getExtendedDeviceInfo()` | 获取扩展设备信息 |
| | `getNetworkType()` | 获取网络连接类型 |
| | `isWifiConnected()` | 判断 WiFi 是否连接 |
| | `getBatteryLevel()` | 获取电池电量 |
| | `getAppVersion()` | 获取应用版本号 |
| **硬件能力** | `vibrate(duration)` | 触发设备震动 |
| | `playSound(soundName)` | 播放系统提示音 |
| | `getCurrentLocation(callback)` | 获取地理位置 |
| | `scanQRCode(callback)` | 扫描二维码 |
| **截图与存储** | `takeScreenshot(callback)` | 截取当前可见区域 |
| | `takeFullScreenshot(callback)` | 截取完整页面长图 |
| | `saveToGallery(base64, callback)` | 保存图片到相册 |
| **本地存储** | `setLocalStorage(key, value)` | 写入键值对 |
| | `getLocalStorage(key)` | 读取键值对 |
| | `removeLocalStorage(key)` | 删除指定键 |
| | `clearLocalStorage()` | 清空所有数据 |
| **导航控制** | `goBack()` | 返回上一页 |
| | `reload()` | 刷新当前页面 |
| | `clearCache()` | 清除 WebView 缓存 |
| | `exitApp()` | 退出应用 |
| | `setScreenOrientation(orientation)` | 设置屏幕方向 |
| | `enableBackButton(enabled)` | 启用/禁用返回键 |
| | `isBackButtonEnabled()` | 查询返回键状态 |
| **全屏模式** | `enterFullscreen()` | 进入全屏模式 |
| | `exitFullscreen()` | 退出全屏模式 |
| | `isFullscreenMode()` | 查询全屏状态 |
| **分享与通讯** | `share(title, text, url)` | 调用系统分享 |
| | `copyToClipboard(text)` | 复制到剪贴板 |
| | `getClipboardContent()` | 读取剪贴板内容 |
| | `openUrl(url)` | 外部浏览器打开链接 |
| | `makeCall(phone)` | 拨打电话 |
| | `sendSMS(phone, message)` | 发送短信 |
| **系统设置** | `openSystemSettings()` | 打开系统设置 |
| | `openAppSettings()` | 打开应用设置 |
| | `getGrantedPermissions()` | 获取已授权权限列表 |
| **事件监听** | `registerKeyListener(callback)` | 注册按键监听 |
| | `unregisterKeyListener()` | 取消按键监听 |
| | `registerExitListener(callback)` | 注册退出事件监听 |
| | `unregisterExitListener()` | 取消退出事件监听 |
| **联系人** | `getContacts()` | 获取联系人（需权限） |

---

### UI 交互

#### `showToast(message)`

显示原生 Toast 短消息提示。

| 参数 | 类型 | 说明 |
|------|------|------|
| `message` | `String` | 要显示的消息文本 |

```javascript
NativeBridge.showToast('操作成功');
```

#### `showLoading(message)` / `hideLoading()`

显示/隐藏模态加载对话框。加载对话框显示期间，用户无法与页面交互。

| 参数 | 类型 | 说明 |
|------|------|------|
| `message` | `String` | 加载提示文本，为空时显示"加载中..." |

```javascript
NativeBridge.showLoading('正在提交...');

// 异步操作完成后隐藏
setTimeout(function() {
  NativeBridge.hideLoading();
}, 3000);
```

#### `showAlert(title, message, callback)`

显示原生 Alert 对话框，用户点击"确定"后触发回调。

| 参数 | 类型 | 说明 |
|------|------|------|
| `title` | `String` | 对话框标题 |
| `message` | `String` | 对话框内容 |
| `callback` | `Function` | 点击确定后的回调函数 |

```javascript
NativeBridge.showAlert('提示', '操作成功！', function() {
  console.log('用户点击了确定');
});
```

#### `showConfirm(title, message, callback)`

显示原生确认对话框（确定/取消），回调参数为布尔值。

| 参数 | 类型 | 说明 |
|------|------|------|
| `title` | `String` | 对话框标题 |
| `message` | `String` | 对话框内容 |
| `callback` | `Function` | 回调函数，参数: `result` (Boolean) |

```javascript
NativeBridge.showConfirm('确认', '确定要删除吗？', function(result) {
  if (result) {
    console.log('用户点击了确定');
  } else {
    console.log('用户点击了取消');
  }
});
```

#### `setStatusBarColor(color)`

设置 Android 状态栏颜色，自动适配状态栏文字颜色（深色/浅色）。

| 参数 | 类型 | 说明 |
|------|------|------|
| `color` | `String` | 十六进制颜色值，如 `#FF5722` |

```javascript
NativeBridge.setStatusBarColor('#1976D2');
```

#### `setTitle(title)`

设置应用标题栏文本（仅在启用 ActionBar 时生效）。

| 参数 | 类型 | 说明 |
|------|------|------|
| `title` | `String` | 标题文本 |

```javascript
NativeBridge.setTitle('我的应用');
```

> **注意**: 本应用默认使用 NoActionBar 主题，此 API 在默认配置下无可见效果。

---

### 设备信息

#### `getDeviceInfo()`

获取基本设备信息，返回已解析的 JSON 对象。

| 返回值字段 | 类型 | 说明 |
|------------|------|------|
| `brand` | `String` | 设备品牌 |
| `model` | `String` | 设备型号 |
| `device` | `String` | 设备名称 |
| `sdkVersion` | `Number` | Android SDK 版本号 |
| `release` | `String` | Android 系统版本 |
| `manufacturer` | `String` | 制造商 |
| `product` | `String` | 产品名称 |

```javascript
var info = NativeBridge.getDeviceInfo();
console.log(info.brand + ' ' + info.model); // 例: "Xiaomi Redmi Note 12"
```

#### `getExtendedDeviceInfo()`

获取扩展设备信息，包含屏幕、电池、网络、应用等完整信息。

| 返回值字段 | 类型 | 说明 |
|------------|------|------|
| `brand` | `String` | 设备品牌 |
| `model` | `String` | 设备型号 |
| `device` | `String` | 设备名称 |
| `manufacturer` | `String` | 制造商 |
| `product` | `String` | 产品名称 |
| `sdkVersion` | `Number` | Android SDK 版本号 |
| `release` | `String` | Android 系统版本 |
| `screen` | `Object\|null` | 屏幕信息 `{width, height, density, densityDpi}` |
| `battery` | `Object\|null` | 电池信息 `{level, isCharging}` |
| `networkType` | `String\|null` | 网络类型 |
| `appVersion` | `String\|null` | 应用版本名 |
| `appVersionCode` | `Number\|null` | 应用版本号 |
| `deviceId` | `String\|null` | 设备唯一标识符 (Android ID) |
| `packageName` | `String\|null` | 应用包名 |

```javascript
var info = NativeBridge.getExtendedDeviceInfo();

// 屏幕分辨率
if (info.screen) {
  console.log('屏幕:', info.screen.width + 'x' + info.screen.height);
  console.log('密度:', info.screen.densityDpi + 'dpi');
}

// 电池状态
if (info.battery) {
  console.log('电量:', info.battery.level + '%');
  console.log('充电中:', info.battery.isCharging);
}

// 设备标识
console.log('设备ID:', info.deviceId);
console.log('包名:', info.packageName);
```

> **注意**: 部分字段获取失败时返回 `null`，不影响其他字段。

#### `getNetworkType()`

获取当前网络连接类型。

| 返回值 | 说明 |
|--------|------|
| `"wifi"` | WiFi 连接 |
| `"cellular"` | 移动数据 |
| `"ethernet"` | 以太网 |
| `"none"` | 无网络 |

```javascript
var type = NativeBridge.getNetworkType();
if (type === 'none') {
  NativeBridge.showToast('当前无网络连接');
}
```

#### `isWifiConnected()`

判断当前是否通过 WiFi 连接网络。

| 返回值 | 类型 | 说明 |
|--------|------|------|
| — | `Boolean` | `true` 表示 WiFi 已连接 |

```javascript
if (NativeBridge.isWifiConnected()) {
  // 可以执行大文件下载等操作
}
```

#### `getBatteryLevel()`

获取当前电池电量百分比。

| 返回值 | 类型 | 说明 |
|--------|------|------|
| — | `Number` | 0-100 的电量百分比，获取失败返回 -1 |

```javascript
var level = NativeBridge.getBatteryLevel();
if (level < 20) {
  NativeBridge.showToast('电量不足，请及时充电');
}
```

#### `getAppVersion()`

获取当前应用的版本名称（`versionName`）。

| 返回值 | 类型 | 说明 |
|--------|------|------|
| — | `String` | 版本号字符串，如 `"1.0.0"`，获取失败返回 `"1.0"` |

```javascript
var version = NativeBridge.getAppVersion();
console.log('当前版本:', version);
```

---

### 硬件能力

#### `vibrate(duration)`

触发设备震动。

| 参数 | 类型 | 说明 |
|------|------|------|
| `duration` | `Number` | 震动时长（毫秒） |

```javascript
NativeBridge.vibrate(200); // 震动 200ms
```

#### `playSound(soundName)`

播放系统预置提示音。

| 参数 | 类型 | 可选值 |
|------|------|--------|
| `soundName` | `String` | `"beep"` \| `"success"` \| `"error"` |

```javascript
NativeBridge.playSound('success'); // 播放成功提示音
```

#### `getCurrentLocation(callback)`

请求获取当前地理位置坐标。首次调用会请求位置权限。超时时间为 10 秒。

| 参数 | 类型 | 说明 |
|------|------|------|
| `callback` | `Function` | 回调函数，参数为结果对象 |

**回调结果对象**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | `String` | `"success"` \| `"permission_denied"` \| `"error"` |
| `latitude` | `Number` | 纬度（成功时） |
| `longitude` | `Number` | 经度（成功时） |
| `accuracy` | `Number` | 精度（米） |
| `altitude` | `Number` | 海拔（米） |
| `speed` | `Number` | 速度（m/s） |
| `timestamp` | `Number` | 时间戳 |
| `message` | `String` | 错误信息（失败时） |

```javascript
NativeBridge.getCurrentLocation(function(result) {
  if (result.status === 'success') {
    console.log('坐标:', result.latitude, result.longitude);
  } else if (result.status === 'permission_denied') {
    NativeBridge.showToast('请授予位置权限');
  } else {
    console.log('定位失败:', result.message);
  }
});
```

#### `scanQRCode(callback)`

启动相机扫描二维码/条形码。首次调用会请求相机权限。

| 参数 | 类型 | 说明 |
|------|------|------|
| `callback` | `Function` | 回调函数，参数为结果对象 |

**回调结果对象**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | `String` | `"success"` \| `"cancelled"` \| `"permission_denied"` \| `"error"` |
| `result` | `String` | 扫描到的内容（成功时） |

```javascript
NativeBridge.scanQRCode(function(result) {
  if (result.status === 'success') {
    console.log('扫描结果:', result.result);
  } else if (result.status === 'cancelled') {
    console.log('用户取消');
  }
});
```

---

### 截图与存储

#### `takeScreenshot(callback)`

截取 WebView 当前可见区域，返回 PNG 格式的 Base64 编码图片。

| 参数 | 类型 | 说明 |
|------|------|------|
| `callback` | `Function` | 回调函数，参数为结果对象 |

**回调结果对象**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | `String` | `"success"` \| `"error"` |
| `data` | `String` | Base64 编码的 PNG 图片数据（成功时） |
| `message` | `String` | 错误信息（失败时） |

```javascript
NativeBridge.takeScreenshot(function(result) {
  if (result.status === 'success') {
    var img = new Image();
    img.src = 'data:image/png;base64,' + result.data;
    document.body.appendChild(img);
  }
});
```

#### `takeFullScreenshot(callback)`

截取 WebView 完整页面内容（包含滚动区域），最大高度 10000px，返回 PNG 格式 Base64 数据。

| 参数 | 类型 | 说明 |
|------|------|------|
| `callback` | `Function` | 回调函数，参数为结果对象 |

**回调结果对象**: 同 `takeScreenshot`。

```javascript
NativeBridge.takeFullScreenshot(function(result) {
  if (result.status === 'success') {
    // 长图截取成功
    NativeBridge.saveToGallery(result.data, function(saveResult) {
      NativeBridge.showToast(saveResult.success ? '已保存' : saveResult.message);
    });
  }
});
```

#### `saveToGallery(base64, callback)`

将 Base64 编码的图片保存到设备相册。Android 9 及以下会自动请求存储权限。

| 参数 | 类型 | 说明 |
|------|------|------|
| `base64` | `String` | Base64 图片数据，支持 `data:image/xxx;base64,` 前缀格式 |
| `callback` | `Function` | 回调函数，参数为结果对象 |

**回调结果对象**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | `Boolean` | 是否保存成功 |
| `message` | `String` | 错误信息（失败时） |

```javascript
NativeBridge.saveToGallery('data:image/png;base64,iVBORw0KGgo...', function(result) {
  if (result.success) {
    NativeBridge.showToast('图片已保存到相册');
  } else {
    NativeBridge.showToast(result.message); // "用户拒绝存储权限" 或 "保存图片失败"
  }
});
```

---

### 本地存储

基于 Android `SharedPreferences` 的持久化键值存储，数据在应用卸载前一直保留，不受 WebView 缓存清除影响。

#### `setLocalStorage(key, value)`

| 参数 | 类型 | 说明 |
|------|------|------|
| `key` | `String` | 存储键名 |
| `value` | `String` | 存储值（仅支持字符串，复杂对象请 JSON 序列化） |

#### `getLocalStorage(key)`

| 参数 | 类型 | 说明 |
|------|------|------|
| `key` | `String` | 存储键名 |

| 返回值 | 类型 | 说明 |
|--------|------|------|
| — | `String\|null` | 对应的值，不存在时返回 `null` |

#### `removeLocalStorage(key)`

删除指定键的存储数据。

#### `clearLocalStorage()`

清空所有本地存储数据。

```javascript
// 写入
NativeBridge.setLocalStorage('user', JSON.stringify({ name: 'John', age: 30 }));

// 读取
var user = JSON.parse(NativeBridge.getLocalStorage('user') || '{}');
console.log(user.name); // "John"

// 删除
NativeBridge.removeLocalStorage('user');

// 清空全部
NativeBridge.clearLocalStorage();
```

---

### 导航控制

#### `goBack()`

WebView 历史后退。如果没有可后退的历史记录，则不执行任何操作。

```javascript
NativeBridge.goBack();
```

#### `reload()`

刷新当前 WebView 页面。

```javascript
NativeBridge.reload();
```

#### `clearCache()`

清除 WebView 缓存和浏览历史。

```javascript
NativeBridge.clearCache();
```

#### `exitApp()`

退出应用，会弹出原生确认对话框。

```javascript
NativeBridge.exitApp();
```

#### `setScreenOrientation(orientation)`

设置屏幕方向锁定。

| 参数 | 类型 | 可选值 |
|------|------|--------|
| `orientation` | `String` | `"portrait"` \| `"landscape"` \| `"auto"` |

```javascript
NativeBridge.setScreenOrientation('landscape'); // 强制横屏
NativeBridge.setScreenOrientation('auto');      // 跟随传感器
```

#### `enableBackButton(enabled)` / `isBackButtonEnabled()`

控制返回键是否由 WebView 处理。

| 参数 | 类型 | 说明 |
|------|------|------|
| `enabled` | `Boolean` | `true` 启用, `false` 禁用 |

```javascript
// 禁用返回键（例如在支付流程中）
NativeBridge.enableBackButton(false);

// 查询当前状态
var enabled = NativeBridge.isBackButtonEnabled();
console.log('返回键状态:', enabled);

// 恢复
NativeBridge.enableBackButton(true);
```

---

### 全屏模式

#### `enterFullscreen()`

进入沉浸式全屏模式，隐藏系统状态栏和导航栏。支持手势滑动临时呼出系统栏。

```javascript
NativeBridge.enterFullscreen();
```

#### `exitFullscreen()`

退出全屏模式，恢复显示系统状态栏和导航栏。

```javascript
NativeBridge.exitFullscreen();
```

#### `isFullscreenMode()`

| 返回值 | 类型 | 说明 |
|--------|------|------|
| — | `Boolean` | `true` 表示当前处于全屏模式 |

```javascript
// 切换全屏状态
function toggleFullscreen() {
  if (NativeBridge.isFullscreenMode()) {
    NativeBridge.exitFullscreen();
  } else {
    NativeBridge.enterFullscreen();
  }
}
```

---

### 分享与通讯

#### `share(title, text, url)`

调用系统分享面板。

| 参数 | 类型 | 说明 |
|------|------|------|
| `title` | `String` | 分享标题 |
| `text` | `String` | 分享文本内容 |
| `url` | `String` | 分享链接（可选，会附加在文本末尾） |

```javascript
NativeBridge.share('推荐应用', '这个应用很好用！', 'https://example.com');
```

#### `copyToClipboard(text)`

复制文本到系统剪贴板，成功后显示 Toast 提示。

| 参数 | 类型 | 说明 |
|------|------|------|
| `text` | `String` | 要复制的文本 |

```javascript
NativeBridge.copyToClipboard('邀请码: ABC123');
```

#### `getClipboardContent()`

读取系统剪贴板中的文本内容。

| 返回值 | 类型 | 说明 |
|--------|------|------|
| — | `String` | 剪贴板文本内容，为空时返回空字符串 `""` |

```javascript
var text = NativeBridge.getClipboardContent();
if (text) {
  console.log('剪贴板:', text);
}
```

> **注意**: 仅支持读取纯文本内容。

#### `openUrl(url)`

在外部浏览器中打开指定 URL。

| 参数 | 类型 | 说明 |
|------|------|------|
| `url` | `String` | 完整的 URL 地址 |

```javascript
NativeBridge.openUrl('https://www.google.com');
```

#### `makeCall(phone)`

调起系统拨号界面（不会直接拨出）。

| 参数 | 类型 | 说明 |
|------|------|------|
| `phone` | `String` | 电话号码 |

```javascript
NativeBridge.makeCall('10086');
```

#### `sendSMS(phone, message)`

调起系统短信界面，预填收件人和内容。

| 参数 | 类型 | 说明 |
|------|------|------|
| `phone` | `String` | 收件人号码 |
| `message` | `String` | 短信内容 |

```javascript
NativeBridge.sendSMS('10086', '查询余额');
```

---

### 系统设置

#### `openSystemSettings()`

跳转到 Android 系统设置主页面。

```javascript
NativeBridge.openSystemSettings();
```

#### `openAppSettings()`

跳转到当前应用的系统设置详情页（可用于引导用户手动开启权限）。

```javascript
NativeBridge.openAppSettings();
```

#### `getGrantedPermissions()`

获取当前应用已授权的权限列表，区分危险权限和普通权限。

| 返回值字段 | 类型 | 说明 |
|------------|------|------|
| `status` | `String` | `"success"` \| `"error"` |
| `dangerousPermissions` | `Array<String>` | 已授权的危险权限列表 |
| `normalPermissions` | `Array<String>` | 已授权的普通权限列表 |
| `permissions` | `Array<String>` | 全部已授权权限（向后兼容） |
| `message` | `String` | 错误信息（失败时） |

**可能的危险权限**: `CAMERA`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `WRITE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES` (Android 13+)

**可能的普通权限**: `VIBRATE`, `INTERNET`, `ACCESS_NETWORK_STATE`

```javascript
var result = NativeBridge.getGrantedPermissions();
if (result.status === 'success') {
  if (result.dangerousPermissions.includes('CAMERA')) {
    console.log('相机权限已授权');
  }
}
```

---

### 事件监听

#### `registerKeyListener(callback)` / `unregisterKeyListener()`

注册/取消物理按键事件监听。

**回调事件对象**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `eventType` | `String` | 固定为 `"keydown"` |
| `key` | `String` | 按键名: `"back"` \| `"home"` \| `"task"` \| `"menu"` \| `"key_xxx"` |
| `keyCode` | `Number` | Android 键码值 |

```javascript
NativeBridge.registerKeyListener(function(event) {
  switch (event.key) {
    case 'back':
      console.log('返回键');
      break;
    case 'menu':
      console.log('菜单键');
      break;
  }
});

// 不再需要时取消监听
NativeBridge.unregisterKeyListener();
```

> **注意**: Home 键和任务键的监听受 Android 系统限制，部分设备可能无法捕获。

#### `registerExitListener(callback)` / `unregisterExitListener()`

注册/取消应用退出事件监听，在应用即将销毁时触发。

**回调事件对象**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `event` | `String` | 固定为 `"exit"` |
| `timestamp` | `Number` | 退出时的时间戳 |

```javascript
NativeBridge.registerExitListener(function(event) {
  // 执行清理操作
  NativeBridge.setLocalStorage('lastExit', String(event.timestamp));
});

// 取消监听
NativeBridge.unregisterExitListener();
```

---

### 联系人

#### `getContacts()`

获取设备联系人列表（需要联系人权限）。

```javascript
NativeBridge.getContacts();
```

> **注意**: 此 API 当前为预留接口，调用后会提示需要申请权限。

---

## 💡 完整示例

```html
<!DOCTYPE html>
<html>
<head>
  <title>NativeBridge Demo</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
  <h1>NativeBridge 功能演示</h1>
  
  <button onclick="showDeviceInfo()">获取设备信息</button>
  <button onclick="takeScreenshotDemo()">截图</button>
  <button onclick="toggleFullscreenDemo()">切换全屏</button>
  <button onclick="getLocationDemo()">获取位置</button>
  <button onclick="clipboardDemo()">剪贴板操作</button>
  
  <div id="output"></div>
  
  <script>
    // 等待 NativeBridge 初始化
    document.addEventListener('NativeBridgeReady', function() {
      NativeBridge.showToast('NativeBridge 已就绪！');
      
      // 注册按键监听
      NativeBridge.registerKeyListener(function(event) {
        console.log('按键:', event.key);
      });
      
      // 注册退出监听
      NativeBridge.registerExitListener(function(event) {
        console.log('应用即将退出');
      });
    });
    
    function showDeviceInfo() {
      var info = NativeBridge.getExtendedDeviceInfo();
      document.getElementById('output').innerHTML = '<pre>' + JSON.stringify(info, null, 2) + '</pre>';
    }
    
    function takeScreenshotDemo() {
      NativeBridge.takeScreenshot(function(result) {
        if (result.status === 'success') {
          var img = document.createElement('img');
          img.src = 'data:image/png;base64,' + result.data;
          img.style.maxWidth = '100%';
          document.getElementById('output').appendChild(img);
        } else {
          NativeBridge.showToast('截图失败: ' + result.message);
        }
      });
    }
    
    function toggleFullscreenDemo() {
      if (NativeBridge.isFullscreenMode()) {
        NativeBridge.exitFullscreen();
        NativeBridge.showToast('已退出全屏');
      } else {
        NativeBridge.enterFullscreen();
        NativeBridge.showToast('已进入全屏');
      }
    }
    
    function getLocationDemo() {
      NativeBridge.showLoading('正在获取位置...');
      NativeBridge.getCurrentLocation(function(result) {
        NativeBridge.hideLoading();
        if (result.status === 'success') {
          document.getElementById('output').innerHTML = 
            '纬度: ' + result.latitude + '<br>' +
            '经度: ' + result.longitude + '<br>' +
            '精度: ' + result.accuracy + '米';
        } else {
          NativeBridge.showToast('获取位置失败: ' + result.message);
        }
      });
    }
    
    function clipboardDemo() {
      // 先复制一些内容
      NativeBridge.copyToClipboard('这是测试文本');
      
      // 然后读取剪贴板
      setTimeout(function() {
        var content = NativeBridge.getClipboardContent();
        document.getElementById('output').innerHTML = '剪贴板内容: ' + content;
      }, 500);
    }
  </script>
</body>
</html>
```

## 📋 更新日志

### 最新版本修复和改进

1. **首屏加载动画优化**
   - 修复首屏不显示的问题，确保首屏在应用启动时正常显示
   - 添加淡入淡出效果，提供更流畅的视觉体验
   - 首屏至少显示2秒钟，WebView加载完成后500ms淡出动画
   - 显示首屏期间禁止用户操作
   - 首屏在最上层显示，遮挡状态栏、导航栏、webview

2. **进度条优化**
   - 修复进度条显示不正常的问题，确保正确反映网页加载进度
   - 进度条现在悬浮在网页内容之上，不占用任何页面空间
   - 网页加载完毕后进度条自动隐藏

3. **应用状态恢复**
   - 退出应用后再次进入时，能够恢复到上次退出时的状态
   - 不再重新加载页面，保持用户的浏览进度

4. **全屏模式优化**
   - 在全屏状态下按返回键直接退出应用，而不是先退出全屏

5. **返回键二次确认**
   - 改用 Toast 提示方式，显示"再按一次退出应用"
   - 在 1 秒内再次点击返回键即可退出应用
   - Toast 在底部显示，持续时间1秒

6. **截图功能改进**
   - 普通截图现在正确截取当前屏幕可见内容（不再从WebView顶部开始）
   - 全屏截图能够正确截取包括不可见滚动区域的完整内容
   - 状态栏和导航栏不会被截取

7. **位置获取优化**
   - 增加重试机制，10秒钟无法获取位置则提示超时
   - 超时提示消息更加简洁

8. **保存图片到相册优化**
   - 自动请求存储权限
   - 用户拒绝授权时返回明确的错误信息"用户拒绝存储权限"
   - 保存失败时返回"保存图片失败"

9. **新增 API**
   - `getGrantedPermissions()`: 获取当前应用已授权的权限列表
   - `getClipboardContent()`: 读取剪贴板中的文本内容

## ⚠️ 注意事项

1. 首次运行需要下载 Gradle 和依赖，可能需要几分钟时间
2. 应用图标需要是 PNG 格式，建议尺寸 512x512 像素
3. 首屏图片支持 PNG 和 JPEG 格式，建议尺寸匹配目标设备屏幕
4. 包名必须是有效的 Java 包名格式（例如：com.company.app）
5. 某些 API（如位置、相机）需要用户授权权限
6. Home 键和任务键的监听可能受 Android 系统限制

## 📲 安装说明

生成的 APK 已经过签名，可以直接安装到 Android 设备上：

1. 将 APK 文件传输到 Android 设备
2. 在设备上打开 APK 文件进行安装
3. 如果提示"未知来源"，请在设置中允许安装未知来源应用

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！如果觉得这个项目有帮助，请给一个 ⭐ Star 支持一下。

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

<p align="center">
  如果这个项目对您有帮助，请点个 ⭐ Star 支持一下！
</p>
