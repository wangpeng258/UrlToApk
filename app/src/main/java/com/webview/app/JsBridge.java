package com.webview.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class JsBridge {
    private final Activity activity;
    private final WebView webView;
    private final SharedPreferences prefs;
    private ProgressDialog loadingDialog;
    private volatile boolean backButtonEnabled = true;
    private boolean isFullscreen = false;

    public JsBridge(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
        this.prefs = activity.getSharedPreferences("WebViewAppPrefs", Context.MODE_PRIVATE);
    }

    @JavascriptInterface
    public void showToast(String message) {
        activity.runOnUiThread(() -> 
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        );
    }

    @JavascriptInterface
    public void vibrate(int duration) {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(duration);
            }
        }
    }

    @JavascriptInterface
    public String getDeviceInfo() {
        JSONObject info = new JSONObject();
        try {
            info.put("brand", Build.BRAND);
            info.put("model", Build.MODEL);
            info.put("device", Build.DEVICE);
            info.put("sdkVersion", Build.VERSION.SDK_INT);
            info.put("release", Build.VERSION.RELEASE);
            info.put("manufacturer", Build.MANUFACTURER);
            info.put("product", Build.PRODUCT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info.toString();
    }

    @JavascriptInterface
    public String getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return "wifi";
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return "cellular";
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            return "ethernet";
                        }
                    }
                }
            } else {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    int type = networkInfo.getType();
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        return "wifi";
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        return "cellular";
                    }
                }
            }
        }
        return "none";
    }

    @JavascriptInterface
    public void copyToClipboard(String text) {
        activity.runOnUiThread(() -> {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", text);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public String getClipboardContent() {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clipData = clipboard.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                CharSequence text = item.getText();
                if (text != null) {
                    return text.toString();
                }
            }
        }
        return "";
    }

    @JavascriptInterface
    public void openUrl(String url) {
        activity.runOnUiThread(() -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                activity.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(activity, "无法打开链接", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void share(String title, String text, String url) {
        activity.runOnUiThread(() -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            String shareText = text;
            if (url != null && !url.isEmpty()) {
                shareText += "\n" + url;
            }
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            activity.startActivity(Intent.createChooser(shareIntent, "分享到"));
        });
    }

    @JavascriptInterface
    public void setStatusBarColor(String color) {
        activity.runOnUiThread(() -> {
            try {
                int colorInt = Color.parseColor(color);
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(colorInt);
                
                WindowInsetsControllerCompat windowInsetsController = 
                        WindowCompat.getInsetsController(window, window.getDecorView());
                double darkness = 1 - (0.299 * Color.red(colorInt) + 0.587 * Color.green(colorInt) + 0.114 * Color.blue(colorInt)) / 255;
                windowInsetsController.setAppearanceLightStatusBars(darkness < 0.5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @JavascriptInterface
    public String getAppVersion() {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }

    @JavascriptInterface
    public void exitApp() {
        activity.runOnUiThread(() -> {
            new AlertDialog.Builder(activity)
                    .setTitle("退出应用")
                    .setMessage("确定要退出应用吗？")
                    .setPositiveButton("确定", (dialog, which) -> activity.finish())
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    @JavascriptInterface
    public void goBack() {
        activity.runOnUiThread(() -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        });
    }

    @JavascriptInterface
    public void reload() {
        activity.runOnUiThread(() -> webView.reload());
    }

    @JavascriptInterface
    public void clearCache() {
        activity.runOnUiThread(() -> {
            webView.clearCache(true);
            webView.clearHistory();
            Toast.makeText(activity, "缓存已清除", Toast.LENGTH_SHORT).show();
        });
    }

    @JavascriptInterface
    public void setScreenOrientation(String orientation) {
        activity.runOnUiThread(() -> {
            switch (orientation.toLowerCase()) {
                case "portrait":
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case "landscape":
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case "auto":
                default:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    break;
            }
        });
    }

    @JavascriptInterface
    public int getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = activity.registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            return (int) ((level / (float) scale) * 100);
        }
        return -1;
    }

    @JavascriptInterface
    public boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                    return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                }
            } else {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }
        }
        return false;
    }

    @JavascriptInterface
    public void playSound(String soundName) {
        activity.runOnUiThread(() -> {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            switch (soundName.toLowerCase()) {
                case "beep":
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                    break;
                case "success":
                    toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 150);
                    break;
                case "error":
                    toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 150);
                    break;
                default:
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                    break;
            }
        });
    }

    @JavascriptInterface
    public void makeCall(String phone) {
        activity.runOnUiThread(() -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                activity.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(activity, "无法拨打电话", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void sendSMS(String phone, String message) {
        activity.runOnUiThread(() -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
                intent.putExtra("sms_body", message);
                activity.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(activity, "无法发送短信", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void setLocalStorage(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    @JavascriptInterface
    public String getLocalStorage(String key) {
        return prefs.getString(key, null);
    }

    @JavascriptInterface
    public void removeLocalStorage(String key) {
        prefs.edit().remove(key).apply();
    }

    @JavascriptInterface
    public void clearLocalStorage() {
        prefs.edit().clear().apply();
    }

    @JavascriptInterface
    public void showLoading(String message) {
        activity.runOnUiThread(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            loadingDialog = new ProgressDialog(activity);
            loadingDialog.setMessage(message != null && !message.isEmpty() ? message : "加载中...");
            loadingDialog.setCancelable(false);
            loadingDialog.show();
        });
    }

    @JavascriptInterface
    public void hideLoading() {
        activity.runOnUiThread(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    @JavascriptInterface
    public void showAlert(String title, String message) {
        activity.runOnUiThread(() -> {
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("确定", (dialog, which) -> {
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).executeJsCallback("_alertCallback", "true");
                        }
                    })
                    .show();
        });
    }

    @JavascriptInterface
    public void showConfirm(String title, String message) {
        activity.runOnUiThread(() -> {
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("确定", (dialog, which) -> {
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).executeJsCallback("_confirmCallback", "true");
                        }
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).executeJsCallback("_confirmCallback", "false");
                        }
                    })
                    .show();
        });
    }

    @JavascriptInterface
    public void enableBackButton(boolean enabled) {
        this.backButtonEnabled = enabled;
    }

    @JavascriptInterface
    public boolean isBackButtonEnabled() {
        return backButtonEnabled;
    }

    @JavascriptInterface
    public void setTitle(String title) {
        // Note: This app uses NoActionBar theme, so this method
        // is a no-op. Document title can be handled in WebView JS.
        activity.runOnUiThread(() -> {
            if (activity instanceof AppCompatActivity) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
                if (appCompatActivity.getSupportActionBar() != null) {
                    appCompatActivity.getSupportActionBar().setTitle(title);
                }
            }
        });
    }

    @JavascriptInterface
    public void scanQRCode() {
        activity.runOnUiThread(() -> {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).startQRCodeScanner();
            }
        });
    }

    @JavascriptInterface
    public void getCurrentLocation() {
        activity.runOnUiThread(() -> {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).requestLocation();
            }
        });
    }

    @JavascriptInterface
    public void takeScreenshot() {
        activity.runOnUiThread(() -> {
            try {
                // Capture only the visible portion of WebView (respects current scroll position)
                // Using View.getDrawingCache() or creating bitmap from visible area
                int webViewWidth = webView.getWidth();
                int webViewHeight = webView.getHeight();
                
                if (webViewWidth <= 0 || webViewHeight <= 0) {
                    throw new Exception("无法获取页面尺寸");
                }
                
                // Create bitmap for visible area only
                Bitmap bitmap = Bitmap.createBitmap(webViewWidth, webViewHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                
                // Save current scroll position
                int scrollX = webView.getScrollX();
                int scrollY = webView.getScrollY();
                
                // Translate canvas to account for scroll position
                // This ensures we capture what's currently visible, not from the top
                canvas.translate(-scrollX, -scrollY);
                webView.draw(canvas);
                
                String base64 = bitmapToBase64(bitmap);
                bitmap.recycle();
                
                if (activity instanceof MainActivity) {
                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("data", base64);
                    ((MainActivity) activity).executeJsCallback("_screenshotCallback", response.toString());
                }
            } catch (Exception e) {
                try {
                    if (activity instanceof MainActivity) {
                        JSONObject response = new JSONObject();
                        response.put("status", "error");
                        response.put("message", e.getMessage());
                        ((MainActivity) activity).executeJsCallback("_screenshotCallback", response.toString());
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public void takeFullScreenshot() {
        activity.runOnUiThread(() -> {
            try {
                // Get the full content height including scrolled content
                float scale = webView.getScale();
                int contentHeight = (int) (webView.getContentHeight() * scale);
                int webViewWidth = webView.getWidth();
                int webViewHeight = webView.getHeight();
                
                if (contentHeight <= 0 || webViewWidth <= 0) {
                    throw new Exception("无法获取页面尺寸");
                }
                
                // Limit the height to prevent OutOfMemoryError
                int maxHeight = Math.min(contentHeight, 10000);
                
                // Save current scroll position to restore later
                final int originalScrollX = webView.getScrollX();
                final int originalScrollY = webView.getScrollY();
                
                // Create bitmap for full page
                final Bitmap bitmap = Bitmap.createBitmap(webViewWidth, maxHeight, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                
                // Use a recursive approach with post() to ensure rendering is complete
                final int[] capturedHeight = {0};
                final int sectionHeight = webViewHeight;
                
                Runnable captureSection = new Runnable() {
                    @Override
                    public void run() {
                        if (capturedHeight[0] >= maxHeight) {
                            // All sections captured, restore and return result
                            webView.scrollTo(originalScrollX, originalScrollY);
                            
                            String base64 = bitmapToBase64(bitmap);
                            bitmap.recycle();
                            
                            if (activity instanceof MainActivity) {
                                try {
                                    JSONObject response = new JSONObject();
                                    response.put("status", "success");
                                    response.put("data", base64);
                                    ((MainActivity) activity).executeJsCallback("_fullScreenshotCallback", response.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            return;
                        }
                        
                        // Scroll to the current section
                        webView.scrollTo(0, capturedHeight[0]);
                        
                        // Post to ensure scroll and rendering is complete before drawing
                        webView.post(() -> {
                            int remainingHeight = maxHeight - capturedHeight[0];
                            int currentSectionHeight = Math.min(sectionHeight, remainingHeight);
                            
                            // Save canvas state
                            canvas.save();
                            // Translate canvas to the correct position
                            canvas.translate(0, capturedHeight[0]);
                            // Clip to current section
                            canvas.clipRect(0, 0, webViewWidth, currentSectionHeight);
                            // Draw WebView (it will draw from its current scroll position)
                            webView.draw(canvas);
                            // Restore canvas state
                            canvas.restore();
                            
                            capturedHeight[0] += currentSectionHeight;
                            
                            // Continue to next section
                            webView.post(this);
                        });
                    }
                };
                
                // Start capturing
                captureSection.run();
                
            } catch (Exception e) {
                try {
                    if (activity instanceof MainActivity) {
                        JSONObject response = new JSONObject();
                        response.put("status", "error");
                        response.put("message", e.getMessage());
                        ((MainActivity) activity).executeJsCallback("_fullScreenshotCallback", response.toString());
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    @JavascriptInterface
    public String getExtendedDeviceInfo() {
        JSONObject info = new JSONObject();
        try {
            // Device model
            info.put("brand", Build.BRAND);
            info.put("model", Build.MODEL);
            info.put("device", Build.DEVICE);
            info.put("manufacturer", Build.MANUFACTURER);
            info.put("product", Build.PRODUCT);
            
            // OS version
            info.put("sdkVersion", Build.VERSION.SDK_INT);
            info.put("release", Build.VERSION.RELEASE);
            
            // Screen resolution
            try {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                JSONObject screen = new JSONObject();
                screen.put("width", displayMetrics.widthPixels);
                screen.put("height", displayMetrics.heightPixels);
                screen.put("density", displayMetrics.density);
                screen.put("densityDpi", displayMetrics.densityDpi);
                info.put("screen", screen);
            } catch (Exception e) {
                info.put("screen", JSONObject.NULL);
            }
            
            // Battery status
            try {
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = activity.registerReceiver(null, ifilter);
                if (batteryStatus != null) {
                    JSONObject battery = new JSONObject();
                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    battery.put("level", (int) ((level / (float) scale) * 100));
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
                    battery.put("isCharging", isCharging);
                    info.put("battery", battery);
                } else {
                    info.put("battery", JSONObject.NULL);
                }
            } catch (Exception e) {
                info.put("battery", JSONObject.NULL);
            }
            
            // Network status
            try {
                info.put("networkType", getNetworkType());
            } catch (Exception e) {
                info.put("networkType", JSONObject.NULL);
            }
            
            // App version
            try {
                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                info.put("appVersion", pInfo.versionName);
                info.put("appVersionCode", pInfo.versionCode);
            } catch (Exception e) {
                info.put("appVersion", JSONObject.NULL);
                info.put("appVersionCode", JSONObject.NULL);
            }
            
            // Device unique identifier (Android ID)
            try {
                String androidId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
                if (androidId != null && !androidId.isEmpty()) {
                    info.put("deviceId", androidId);
                } else {
                    // Fallback to a combination of device properties (without deprecated Build.SERIAL)
                    String fallbackId = Build.BRAND + "-" + Build.MODEL + "-" + Build.FINGERPRINT.hashCode();
                    info.put("deviceId", fallbackId);
                }
            } catch (Exception e) {
                // Last resort fallback using device properties
                try {
                    String fallbackId = Build.BRAND + "-" + Build.MODEL + "-" + Build.FINGERPRINT.hashCode();
                    info.put("deviceId", fallbackId);
                } catch (Exception ex) {
                    info.put("deviceId", JSONObject.NULL);
                }
            }
            
            // Package name
            try {
                info.put("packageName", activity.getPackageName());
            } catch (Exception e) {
                info.put("packageName", JSONObject.NULL);
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info.toString();
    }

    @JavascriptInterface
    public void openSystemSettings() {
        activity.runOnUiThread(() -> {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                activity.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(activity, "无法打开系统设置", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void openAppSettings() {
        activity.runOnUiThread(() -> {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(activity, "无法打开应用设置", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void enterFullscreen() {
        activity.runOnUiThread(() -> {
            try {
                Window window = activity.getWindow();
                WindowInsetsControllerCompat windowInsetsController = 
                        WindowCompat.getInsetsController(window, window.getDecorView());
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
                windowInsetsController.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                isFullscreen = true;
                // Notify MainActivity about fullscreen mode change
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).onFullscreenModeChanged();
                }
            } catch (Exception e) {
                Toast.makeText(activity, "无法进入全屏模式", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void exitFullscreen() {
        activity.runOnUiThread(() -> {
            try {
                Window window = activity.getWindow();
                WindowInsetsControllerCompat windowInsetsController = 
                        WindowCompat.getInsetsController(window, window.getDecorView());
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
                isFullscreen = false;
                // Notify MainActivity about fullscreen mode change
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).onFullscreenModeChanged();
                }
            } catch (Exception e) {
                Toast.makeText(activity, "无法退出全屏模式", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public boolean isFullscreenMode() {
        return isFullscreen;
    }

    @JavascriptInterface
    public void registerKeyListener() {
        // Key listeners are handled in MainActivity
        // This method just enables the feature
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setKeyListenerEnabled(true);
        }
    }

    @JavascriptInterface
    public void unregisterKeyListener() {
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setKeyListenerEnabled(false);
        }
    }

    @JavascriptInterface
    public void registerExitListener() {
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setExitListenerEnabled(true);
        }
    }

    @JavascriptInterface
    public void unregisterExitListener() {
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setExitListenerEnabled(false);
        }
    }

    @JavascriptInterface
    public void saveToGallery(String base64) {
        activity.runOnUiThread(() -> {
            try {
                // Check permission for Android 9 and below
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Request permission
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).requestStoragePermissionForSave(base64);
                        }
                        return;
                    }
                }
                
                // Permission granted or not needed, proceed with saving
                saveToGalleryInternal(base64);
                
            } catch (Exception e) {
                try {
                    if (activity instanceof MainActivity) {
                        JSONObject response = new JSONObject();
                        response.put("success", false);
                        response.put("message", "保存图片失败");
                        ((MainActivity) activity).executeJsCallback("_saveGalleryCallback", response.toString());
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Internal method to save image to gallery (called after permission is granted)
     */
    public void saveToGalleryInternal(String base64) {
        activity.runOnUiThread(() -> {
            try {
                // Remove data:image/xxx;base64, prefix if present
                String pureBase64 = base64;
                if (base64.contains(",")) {
                    pureBase64 = base64.substring(base64.indexOf(",") + 1);
                }
                
                byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                
                if (bitmap == null) {
                    throw new Exception("无法解码图片");
                }
                
                // Save to gallery
                String fileName = "IMG_" + System.currentTimeMillis() + ".png";
                boolean saved = false;
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10 and above - use MediaStore
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                    
                    Uri uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        try (OutputStream outputStream = activity.getContentResolver().openOutputStream(uri)) {
                            if (outputStream != null) {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                saved = true;
                            }
                        }
                    }
                } else {
                    // Android 9 and below
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                    java.io.File file = new java.io.File(path, fileName);
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        // Notify gallery about new image
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(Uri.fromFile(file));
                        activity.sendBroadcast(mediaScanIntent);
                        saved = true;
                    }
                }
                
                bitmap.recycle();
                
                if (activity instanceof MainActivity) {
                    JSONObject response = new JSONObject();
                    if (saved) {
                        response.put("success", true);
                    } else {
                        response.put("success", false);
                        response.put("message", "保存图片失败");
                    }
                    ((MainActivity) activity).executeJsCallback("_saveGalleryCallback", response.toString());
                }
                
            } catch (Exception e) {
                try {
                    if (activity instanceof MainActivity) {
                        JSONObject response = new JSONObject();
                        response.put("success", false);
                        response.put("message", "保存图片失败");
                        ((MainActivity) activity).executeJsCallback("_saveGalleryCallback", response.toString());
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public void getContacts() {
        Toast.makeText(activity, "获取联系人需要申请权限", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Get list of granted permissions for this app
     * @return JSON string containing arrays of dangerous and normal permissions
     */
    @JavascriptInterface
    public String getGrantedPermissions() {
        JSONObject result = new JSONObject();
        try {
            org.json.JSONArray dangerousPermissions = new org.json.JSONArray();
            org.json.JSONArray normalPermissions = new org.json.JSONArray();
            
            // Dangerous permissions that require user consent
            String[] dangerousPermissionsToCheck = {
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            };
            
            // Add READ_MEDIA_IMAGES for Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= 33) {
                dangerousPermissionsToCheck = new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    "android.permission.READ_MEDIA_IMAGES"
                };
            }
            
            // Normal permissions that are automatically granted
            String[] normalPermissionsToCheck = {
                android.Manifest.permission.VIBRATE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE
            };
            
            for (String permission : dangerousPermissionsToCheck) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(activity, permission) 
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    String simpleName = permission.substring(permission.lastIndexOf('.') + 1);
                    dangerousPermissions.put(simpleName);
                }
            }
            
            for (String permission : normalPermissionsToCheck) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(activity, permission) 
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    String simpleName = permission.substring(permission.lastIndexOf('.') + 1);
                    normalPermissions.put(simpleName);
                }
            }
            
            result.put("status", "success");
            result.put("dangerousPermissions", dangerousPermissions);
            result.put("normalPermissions", normalPermissions);
            // For backwards compatibility, also include combined list
            org.json.JSONArray allPermissions = new org.json.JSONArray();
            for (int i = 0; i < dangerousPermissions.length(); i++) {
                allPermissions.put(dangerousPermissions.get(i));
            }
            for (int i = 0; i < normalPermissions.length(); i++) {
                allPermissions.put(normalPermissions.get(i));
            }
            result.put("permissions", allPermissions);
            
        } catch (Exception e) {
            try {
                result.put("status", "error");
                result.put("message", e.getMessage());
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }
}
