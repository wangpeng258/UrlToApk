# URL to APK Builder

<p align="center">
  <strong>Convert any website URL into an Android APK using GitHub Actions — zero coding required</strong>
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

## 📖 About

UrlToApk is a zero-code Android app generation tool. Simply provide a website URL and GitHub Actions will automatically build a native Android APK for you. Supports custom app name, icon, theme color, splash screen, and more. Includes 30+ JavaScript Bridge APIs for calling native device features from your web pages.

## ✨ Features

| Category | Features |
|----------|----------|
| 🎨 Customization | Website URL, app name, package name, theme color, app icon |
| 🖼️ Splash Screen | Custom splash image or app icon with fade animation |
| 🔐 Signing | Custom keystore certificate info (CN/OU/O/L/ST/C) |
| 🔙 Back Button | Double-press to exit with Toast confirmation |
| 🌉 JS Bridge | 30+ native APIs (device info, screenshot, location, QR code, etc.) |
| 📸 Screenshot | Regular screenshot & full-page long screenshot |
| 💾 Storage | Save images to gallery, local key-value storage |
| 📺 Fullscreen | Toggle fullscreen mode |
| ⚙️ System | System settings shortcut, permission query |
| 🎮 Events | Key event listener, app exit event listener |
| 🔗 Deep Links | Open the app from a browser using a package-derived URI scheme, with an optional temporary `url` parameter |

## 🚀 Getting Started

### 1. Fork this Repository

Click the **Fork** button in the top-right corner to fork this repo to your GitHub account.

### 2. Run GitHub Action

1. Go to the **Actions** tab
2. Select the **"Build Android APK"** workflow
3. Click **"Run workflow"**
4. Fill in the parameters:

#### Basic Parameters
| Parameter | Description | Required | Default |
|-----------|-------------|----------|---------|
| Website URL | The URL to wrap into an APK | ✅ | - |
| App Name | Name displayed on the device | ✅ | My App |
| Package Name | Unique Android app identifier | ✅ | com.webview.app |
| Theme Color | Status bar and theme color | ❌ | #2196F3 |
| App Icon URL | Publicly accessible PNG image URL | ❌ | https://wmimg.com/i/1792/2025/12/692e96b4c100f.png |

#### Splash Screen Parameters
| Parameter | Description | Required | Default |
|-----------|-------------|----------|---------|
| Splash Image URL | Custom splash image URL (if empty, uses app icon) | ❌ | - |

> **Splash Screen Logic:**
> - If a splash image URL is provided, the image always fills the screen like CSS `object-fit: cover`: it scales proportionally and crops overflow from the center with no letterboxing or distortion. Smaller images may be upscaled and look blurry.
> - If no splash image URL is provided, the app icon is centered on a white background with the app name below
> - The splash screen displays for at least 2 seconds, then fades out over 500ms after WebView finishes loading
> - User interaction is disabled while the splash screen is displayed

#### Certificate Parameters
| Parameter | Description | Default |
|-----------|-------------|---------|
| Common Name (CN) | Certificate holder name | wppApk |
| Org Unit (OU) | Organization unit | hangzhoushiqi |
| Organization (O) | Organization name | shiqi |
| Locality (L) | City | zhejiang |
| State (ST) | State/Province | hangzhou |
| Country (C) | Country code | CN |

### 3. Download APK

After the workflow completes:
- Download Artifacts from the Actions run result
- Download the released APK from the Releases page

## 🔗 Open the App from a Browser (Deep Link)

The URI scheme is generated **at build time** from the Android package name: remove the first and last package segments, concatenate the middle segments without dots, and lowercase them.

| Package name | Deep link |
|---|---|
| `com.webview.app` | `webview://open` |
| `com.example.smart.app` | `examplesmart://open` |

For a two-segment package, the final segment is used. Invalid URI-scheme characters (such as underscores) are removed, and `app` is prefixed if the result would not begin with a letter. Identical middle segments can still produce the same scheme.

For an app packaged as `com.webview.app`:

```html
<a href="webview://open?url=https%3A%2F%2Fwww.example.com%2F">
  Open the app at this URL
</a>
<a href="webview://open">Open the app</a>
```

For URLs containing query parameters, encode the *entire* destination address:

```js
const destination = 'https://www.example.com/home?tab=1&from=web';
const deepLink = 'webview://open?url=' + encodeURIComponent(destination);
```

Chrome's Android intent syntax can explicitly target the package:

```html
<a href="intent://open?url=https%3A%2F%2Fwww.example.com%2F#Intent;scheme=webview;package=com.webview.app;end">Open app</a>
```

Replace both the `webview` scheme and `com.webview.app` package name according to your build configuration. Browser support depends on browser policy and a user gesture.

The optional `url` must be a valid HTTP(S) address and affects **only the current launch**; it is not saved as the default home page. Relaunching the app normally uses the original website URL configured at build time. Rebuild and reinstall the APK to use the new scheme; the old `urltoapk://` scheme is no longer registered.

---

## 🖼️ Splash Screen

The app displays a splash screen on startup to improve user experience:

### Custom Splash Image Mode
- Provide a splash image URL in the GitHub Action parameters
- Images always fill the screen with no rounded corners or letterboxing, using the equivalent of CSS `object-fit: cover` (proportional scaling with centered cropping). Small images are upscaled and may become blurry.
- Supports PNG and JPEG formats
- Splash image is displayed on top of everything (status bar, navigation bar, WebView)
- For a sharp full-screen result, provide an original image at least as large as the target device's physical screen resolution (e.g. 1080×2400 or higher) with a similar aspect ratio. Upscaling cannot restore missing image detail.

### Default Icon Mode
- If no splash image URL is provided, the app icon is used
- The original full-resolution app icon is preserved for the splash screen, instead of enlarging the small launcher mipmap assets (up to 192×192 pixels)
- Undersized images are not forcibly upscaled (letterboxing may appear); without an icon URL, the packaged launcher icon remains the fallback
- White background with centered icon
- Icon corner radius is 10% of icon width
- App name displayed below the icon

### Display Timing
- Splash screen displays for at least 2 seconds
- After WebView finishes loading, if 2 seconds have passed, a 500ms fade-out animation begins
- If WebView hasn't finished loading, the splash continues until loading completes
- User interaction is disabled while the splash is displayed

## 🌉 JavaScript Bridge API

JavaScript in the WebView can call native features via the `NativeBridge` object:

```javascript
// Wait for NativeBridge initialization
document.addEventListener('NativeBridgeReady', function() {
  console.log('NativeBridge is ready!');
});

// Or use a callback
window.onNativeBridgeReady = function() {
  console.log('NativeBridge is ready!');
};
```

### Available APIs

---

### UI

#### showToast(message)
Display a Toast notification.

```javascript
NativeBridge.showToast('Hello World');
```

#### showLoading(message) / hideLoading()
Show/hide a loading dialog.

```javascript
NativeBridge.showLoading('Loading...');
NativeBridge.hideLoading();
```

#### showAlert(title, message, callback)
Show an alert dialog.

```javascript
NativeBridge.showAlert('Info', 'Operation successful!', function() {
  console.log('User clicked OK');
});
```

#### showConfirm(title, message, callback)
Show a confirmation dialog.

```javascript
NativeBridge.showConfirm('Confirm', 'Are you sure you want to delete?', function(result) {
  if (result) {
    console.log('User confirmed');
  } else {
    console.log('User cancelled');
  }
});
```

#### setStatusBarColor(color)
Set the status bar color.

```javascript
NativeBridge.setStatusBarColor('#FF5722');
```

---

### Device Features

#### getDeviceInfo()
Get basic device information.

```javascript
var deviceInfo = NativeBridge.getDeviceInfo();
console.log('Brand:', deviceInfo.brand);
console.log('Model:', deviceInfo.model);
console.log('Device:', deviceInfo.device);
console.log('SDK Version:', deviceInfo.sdkVersion);
console.log('OS Version:', deviceInfo.release);
console.log('Manufacturer:', deviceInfo.manufacturer);
console.log('Product:', deviceInfo.product);
```

#### getExtendedDeviceInfo()
Get extended device information with more details.

```javascript
var info = NativeBridge.getExtendedDeviceInfo();

// Basic device info
console.log('Brand:', info.brand);
console.log('Model:', info.model);

// Screen resolution
if (info.screen) {
  console.log('Width:', info.screen.width);
  console.log('Height:', info.screen.height);
  console.log('Density:', info.screen.density);
  console.log('DPI:', info.screen.densityDpi);
}

// Battery status
if (info.battery) {
  console.log('Battery:', info.battery.level + '%');
  console.log('Charging:', info.battery.isCharging);
}

// Network & App info
console.log('Network:', info.networkType);
console.log('App Version:', info.appVersion);
console.log('Package:', info.packageName);
console.log('Device ID:', info.deviceId);
```

#### getNetworkType()
Get current network type.

```javascript
var networkType = NativeBridge.getNetworkType();
// Returns: 'wifi' | 'cellular' | 'ethernet' | 'none'
```

#### isWifiConnected()
Check if WiFi is connected.

```javascript
var isWifi = NativeBridge.isWifiConnected();
```

#### getBatteryLevel()
Get battery level percentage.

```javascript
var battery = NativeBridge.getBatteryLevel();
console.log('Battery:', battery + '%');
```

#### getAppVersion()
Get app version string.

```javascript
var version = NativeBridge.getAppVersion();
```

#### vibrate(duration)
Trigger device vibration.

```javascript
NativeBridge.vibrate(200); // Vibrate for 200ms
```

#### playSound(soundName)
Play system notification sound.

```javascript
NativeBridge.playSound('beep');    // Default notification
NativeBridge.playSound('success'); // Success sound
NativeBridge.playSound('error');   // Error sound
```

#### getCurrentLocation(callback)
Get current GPS location.

```javascript
NativeBridge.getCurrentLocation(function(result) {
  if (result.status === 'success') {
    console.log('Latitude:', result.latitude);
    console.log('Longitude:', result.longitude);
    console.log('Accuracy:', result.accuracy);
  } else if (result.status === 'permission_denied') {
    console.log('Location permission denied');
  } else {
    console.log('Failed:', result.message);
  }
});
```

#### scanQRCode(callback)
Scan a QR code.

```javascript
NativeBridge.scanQRCode(function(result) {
  if (result.status === 'success') {
    console.log('Result:', result.result);
  } else if (result.status === 'cancelled') {
    console.log('User cancelled');
  } else if (result.status === 'permission_denied') {
    console.log('Camera permission denied');
  }
});
```

---

### Screenshot

#### takeScreenshot(callback)
Capture the current visible screen and return as Base64.

```javascript
NativeBridge.takeScreenshot(function(result) {
  if (result.status === 'success') {
    var img = document.createElement('img');
    img.src = 'data:image/png;base64,' + result.data;
    document.body.appendChild(img);
  }
});
```

#### takeFullScreenshot(callback)
Capture the entire WebView content including scrollable areas.

```javascript
NativeBridge.takeFullScreenshot(function(result) {
  if (result.status === 'success') {
    console.log('Full page screenshot captured');
  }
});
```

#### saveToGallery(base64, callback)
Save a Base64-encoded image to the device gallery.

```javascript
NativeBridge.saveToGallery(base64Image, function(result) {
  if (result.success) {
    console.log('Saved successfully');
  } else {
    console.log('Failed:', result.message);
  }
});
```

---

### System Settings

#### openSystemSettings()
Open the system settings page.

```javascript
NativeBridge.openSystemSettings();
```

#### openAppSettings()
Open the current app's settings page.

```javascript
NativeBridge.openAppSettings();
```

#### getGrantedPermissions()
Get the list of granted permissions.

```javascript
var result = NativeBridge.getGrantedPermissions();
if (result.status === 'success') {
  console.log('Dangerous permissions:', result.dangerousPermissions);
  console.log('Normal permissions:', result.normalPermissions);
}
```

---

### Fullscreen Mode

#### enterFullscreen() / exitFullscreen()
Enter or exit fullscreen mode.

```javascript
NativeBridge.enterFullscreen();
NativeBridge.exitFullscreen();
```

#### isFullscreenMode()
Check if currently in fullscreen mode.

```javascript
var isFullscreen = NativeBridge.isFullscreenMode();
```

---

### Key Event Listener

#### registerKeyListener(callback)
Register a key event listener for Back, Home, Task, and Menu keys.

```javascript
NativeBridge.registerKeyListener(function(event) {
  console.log('Key:', event.key);       // 'back', 'home', 'task', 'menu'
  console.log('KeyCode:', event.keyCode);
});
```

#### unregisterKeyListener()
Unregister the key event listener.

```javascript
NativeBridge.unregisterKeyListener();
```

---

### App Exit Event

#### registerExitListener(callback)
Register a listener that fires when the app is about to exit.

```javascript
NativeBridge.registerExitListener(function(event) {
  console.log('App is exiting at:', event.timestamp);
  // Perform cleanup: save data, send analytics, etc.
});
```

#### unregisterExitListener()
Unregister the exit event listener.

```javascript
NativeBridge.unregisterExitListener();
```

---

### Share & Communication

#### share(title, text, url)
Invoke the system share dialog.

```javascript
NativeBridge.share('Title', 'Description', 'https://example.com');
```

#### copyToClipboard(text)
Copy text to the clipboard.

```javascript
NativeBridge.copyToClipboard('Text to copy');
```

#### getClipboardContent()
Read text from the clipboard.

```javascript
var text = NativeBridge.getClipboardContent();
```

#### openUrl(url)
Open a URL in the external browser.

```javascript
NativeBridge.openUrl('https://www.google.com');
```

#### makeCall(phone)
Make a phone call.

```javascript
NativeBridge.makeCall('10086');
```

#### sendSMS(phone, message)
Send an SMS message.

```javascript
NativeBridge.sendSMS('10086', 'Message content');
```

---

### Local Storage

#### setLocalStorage(key, value) / getLocalStorage(key)
Store and retrieve data locally.

```javascript
NativeBridge.setLocalStorage('username', 'john_doe');
var username = NativeBridge.getLocalStorage('username');
```

#### removeLocalStorage(key) / clearLocalStorage()
Remove or clear local storage data.

```javascript
NativeBridge.removeLocalStorage('username');
NativeBridge.clearLocalStorage();
```

---

### Navigation

#### goBack() / reload()
Navigate back or reload the page.

```javascript
NativeBridge.goBack();
NativeBridge.reload();
```

#### clearCache()
Clear WebView cache.

```javascript
NativeBridge.clearCache();
```

#### exitApp()
Exit the app (shows a confirmation dialog).

```javascript
NativeBridge.exitApp();
```

#### setScreenOrientation(orientation)
Set screen orientation.

```javascript
NativeBridge.setScreenOrientation('portrait');  // Portrait
NativeBridge.setScreenOrientation('landscape'); // Landscape
NativeBridge.setScreenOrientation('auto');      // Auto-rotate
```

#### enableBackButton(enabled)
Enable/disable back button handling.

```javascript
NativeBridge.enableBackButton(true);
NativeBridge.enableBackButton(false);
```

---

## 💡 Full Example

```html
<!DOCTYPE html>
<html>
<head>
  <title>NativeBridge Demo</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
  <h1>NativeBridge Demo</h1>
  
  <button onclick="showDeviceInfo()">Device Info</button>
  <button onclick="takeScreenshotDemo()">Screenshot</button>
  <button onclick="toggleFullscreen()">Toggle Fullscreen</button>
  
  <div id="output"></div>
  
  <script>
    document.addEventListener('NativeBridgeReady', function() {
      NativeBridge.showToast('NativeBridge is ready!');
    });
    
    function showDeviceInfo() {
      var info = NativeBridge.getExtendedDeviceInfo();
      document.getElementById('output').innerHTML = 
        '<pre>' + JSON.stringify(info, null, 2) + '</pre>';
    }
    
    function takeScreenshotDemo() {
      NativeBridge.takeScreenshot(function(result) {
        if (result.status === 'success') {
          var img = document.createElement('img');
          img.src = 'data:image/png;base64,' + result.data;
          img.style.maxWidth = '100%';
          document.getElementById('output').appendChild(img);
        }
      });
    }
    
    function toggleFullscreen() {
      if (NativeBridge.isFullscreenMode()) {
        NativeBridge.exitFullscreen();
      } else {
        NativeBridge.enterFullscreen();
      }
    }
  </script>
</body>
</html>
```

## ⚠️ Notes

1. First run requires downloading Gradle and dependencies, which may take a few minutes
2. App icon must be PNG format, recommended size 512x512 pixels
3. Splash image supports PNG and JPEG formats
4. Package name must be a valid Java package format (e.g., com.company.app)
5. Some APIs (location, camera) require user permission grants
6. Home and Task key listeners may be restricted by the Android system

## 📲 Installation

The generated APK is already signed and can be installed directly:

1. Transfer the APK file to your Android device
2. Open the APK file to install
3. If prompted about "Unknown sources", enable installation from unknown sources in Settings

## 🤝 Contributing

Issues and Pull Requests are welcome! If you find this project helpful, please give it a ⭐ Star!

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  If this project helps you, please give it a ⭐ Star!
</p>
