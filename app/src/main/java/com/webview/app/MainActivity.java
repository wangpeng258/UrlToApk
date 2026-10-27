package com.webview.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final int STORAGE_PERMISSION_REQUEST = 1002;
    private static final String DEEP_LINK_SCHEME = "urltoapk";
    private static final String DEEP_LINK_HOST = "open";
    private static final int MAX_HOME_URL_LENGTH = 4096;
    private static final long BACK_PRESS_EXIT_INTERVAL = 1000; // 1 second for double tap exit

    // Location constants
    private static final long LAST_LOCATION_MAX_AGE_MS = 2 * 60 * 1000; // 2 minutes
    private static final long LOCATION_UPDATE_INTERVAL_MS = 1000; // 1 second
    private static final float LOCATION_MIN_DISTANCE_METERS = 0f; // No minimum distance
    private static final long LOCATION_TIMEOUT_MS = 10000; // 10 seconds

    // Splash screen constants
    private static final long SPLASH_FADE_OUT_DURATION = 500; // 500ms fade out
    private static final long SPLASH_MIN_DURATION = 2000; // Minimum 2 seconds display time
    private static final long SPLASH_MAX_DURATION = 30000; // Maximum 30 seconds (fallback)
    private static final long THEME_COLOR_REAPPLY_DELAY = 50; // 50ms delay for theme color reapplication

    private WebView webView;
    private FrameLayout webViewContainer;
    private FrameLayout progressContainer;
    private View progressIndicator;
    private ValueCallback<Uri[]> filePathCallback;
    private JsBridge jsBridge;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler locationTimeoutHandler;
    private long lastBackPressTime = 0;
    private Toast exitToast;
    private boolean keyListenerEnabled = false;
    private boolean exitListenerEnabled = false;
    private boolean pageLoadComplete = false;
    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private String pendingSaveBase64 = null;

    // Progress bar animation constants
    private static final long PROGRESS_ANIMATION_DURATION = 150; // 150ms for progress updates
    private static final long PROGRESS_FADE_OUT_DURATION = 200; // 200ms fade out
    private static final long PROGRESS_HIDE_DELAY = 100; // 100ms delay before hiding
    private boolean progressVisible = false;
    private int statusBarHeight = 0;
    private android.view.ViewPropertyAnimator currentProgressAnimation = null;

    // Splash screen fields
    private ConstraintLayout splashContainer;
    private ImageView splashImage;
    private ImageView splashAppIcon;
    private LinearLayout defaultSplashContainer;
    private Handler splashHandler;
    private long splashStartTime;
    private boolean splashMinTimeElapsed = false;
    private boolean splashWebViewLoaded = false;
    private boolean splashFinishing = false;
    private boolean splashVisible = true;

    private final ActivityResultLauncher<Intent> fileChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (filePathCallback != null) {
                    // parseResult handles both single and multiple file selections
                    // (Intent data and ClipData), and returns null if cancelled.
                    Uri[] results = WebChromeClient.FileChooserParams.parseResult(
                            result.getResultCode(), result.getData());
                    filePathCallback.onReceiveValue(results);
                    filePathCallback = null;
                }
            }
    );

    private final ActivityResultLauncher<Intent> qrCodeScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                String scanResult = null;
                String status = "cancelled";

                if (result.getData() != null) {
                    scanResult = result.getData().getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT);
                }

                if (result.getResultCode() == ScannerActivity.RESULT_SUCCESS && scanResult != null) {
                    status = "success";
                } else if (result.getResultCode() == ScannerActivity.RESULT_ERROR) {
                    status = "error";
                    if ("PERMISSION_DENIED".equals(scanResult)) {
                        status = "permission_denied";
                    }
                }

                final String finalStatus = status;
                final String finalResult = scanResult;

                runOnUiThread(() -> {
                    try {
                        JSONObject response = new JSONObject();
                        response.put("status", finalStatus);
                        response.put("result", finalResult != null ? finalResult : "");
                        executeJsCallback("_qrCallback", response.toString());
                    } catch (Exception e) {
                        executeJsCallback("_qrCallback", "{\"status\":\"error\",\"result\":\"\"}");
                    }
                });
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize splash handler
        splashHandler = new Handler(Looper.getMainLooper());
        splashStartTime = System.currentTimeMillis();

        // Show splash screen in fullscreen mode (hide system bars)
        showSplashFullscreen();

        setContentView(R.layout.activity_main);

        initViews();
        initSplashScreen();
        initWebView();

        // Remove the legacy persistent override from earlier app versions.
        // Deep link URLs must only affect the current launch.
        getSharedPreferences("UrlToApkHomePrefs", MODE_PRIVATE)
                .edit().remove("home_url").apply();

        // Restore state if savedInstanceState is not null
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
            // If restoring state, hide splash immediately
            hideSplashImmediately();
        } else {
            // Use a deep link URL for this launch only. A normal launch always
            // starts at the original URL configured when building the APK.
            String launchUrl = getDeepLinkUrl(getIntent());
            webView.loadUrl(launchUrl != null ? launchUrl : BuildConfig.APP_URL);

            // Set minimum display time callback
            splashHandler.postDelayed(() -> {
                splashMinTimeElapsed = true;
                checkAndHideSplash();
            }, SPLASH_MIN_DURATION);

            // Fallback timer - hide splash after max duration
            splashHandler.postDelayed(this::forceHideSplash, SPLASH_MAX_DURATION);
        }
    }

    /**
     * Receive links when the existing activity is at the top of its task.
     * If Android creates a new activity instead, onCreate handles the link.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String launchUrl = getDeepLinkUrl(intent);
        if (launchUrl != null && webView != null) {
            // Only navigate the current session; never update the default URL.
            webView.loadUrl(launchUrl);
        }
    }

    private boolean isHomeDeepLink(Intent intent) {
        if (intent == null || !Intent.ACTION_VIEW.equals(intent.getAction())) {
            return false;
        }
        Uri data = intent.getData();
        return data != null
                && DEEP_LINK_SCHEME.equalsIgnoreCase(data.getScheme())
                && DEEP_LINK_HOST.equalsIgnoreCase(data.getHost());
    }

    /**
     * Read a temporary URL from an external launch intent without saving it.
     * A link without a URL leaves the current page unchanged.
     */
    private String getDeepLinkUrl(Intent intent) {
        if (!isHomeDeepLink(intent)) {
            return null;
        }
        String requestedUrl;
        try {
            requestedUrl = intent.getData().getQueryParameter("url");
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "首页地址参数无效", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (requestedUrl == null || requestedUrl.isEmpty()) {
            return null;
        }
        String homeUrl = validatedHomeUrl(requestedUrl);
        if (homeUrl == null) {
            Toast.makeText(this, "首页地址必须是有效的 HTTP 或 HTTPS URL", Toast.LENGTH_SHORT).show();
            return null;
        }
        return homeUrl;
    }

    /**
     * Reject malformed, unsupported and excessively long URLs before using
     * an external parameter in WebView.loadUrl().
     */
    private String validatedHomeUrl(String url) {
        if (url == null) {
            return null;
        }
        String candidate = url.trim();
        if (candidate.isEmpty() || candidate.length() > MAX_HOME_URL_LENGTH
                || candidate.indexOf((char) 10) >= 0 || candidate.indexOf((char) 13) >= 0) {
            return null;
        }
        try {
            Uri parsed = Uri.parse(candidate);
            String scheme = parsed.getScheme();
            String host = parsed.getHost();
            if (!parsed.isHierarchical() || host == null || host.isEmpty()
                    || scheme == null
                    || (!"https".equalsIgnoreCase(scheme)
                        && !"http".equalsIgnoreCase(scheme))) {
                return null;
            }
            return candidate;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    private void setThemeColor() {
        try {
            String colorStr = BuildConfig.THEME_COLOR;
            int color = Color.parseColor(colorStr);
            applyStatusBarColor(color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isColorLight(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }

    /**
     * Show splash screen in fullscreen mode, hiding status bar and navigation bar
     */
    private void showSplashFullscreen() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            // Hide both status bar and navigation bar
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        // Make status bar and navigation bar transparent
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    /**
     * Restore normal window mode after splash screen is hidden
     * Uses standard mode (setDecorFitsSystemWindows=true) for stable status bar color
     */
    private void restoreNormalWindow() {
        restoreNormalWindow(true);
    }

    /**
     * Restore normal window mode after splash screen is hidden
     * @param setColor Whether to set the theme color immediately
     */
    private void restoreNormalWindow(boolean setColor) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // Use standard mode (true) for stable status bar color handling
        // When true, system automatically handles status bar background and content won't draw behind it
        WindowCompat.setDecorFitsSystemWindows(window, true);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            // Show system bars
            controller.show(WindowInsetsCompat.Type.systemBars());
        }

        // Apply theme color to status bar only if requested
        if (setColor) {
            setThemeColor();
            // Post a delayed task to ensure the color is applied after window state changes
            if (splashHandler != null) {
                splashHandler.postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        setThemeColor();
                    }
                }, THEME_COLOR_REAPPLY_DELAY);
            }
        }

        // Update webViewContainer padding and progress bar position
        updateWebViewContainerPadding();
        updateProgressBarPosition();
    }

    /**
     * Initialize splash screen views and load splash image
     */
    private void initSplashScreen() {
        splashContainer = findViewById(R.id.splashContainer);
        splashImage = findViewById(R.id.splashImage);
        splashAppIcon = findViewById(R.id.appIcon);
        defaultSplashContainer = findViewById(R.id.defaultSplashContainer);
        TextView splashAppName = findViewById(R.id.splashAppName);

        // Set app name dynamically
        if (splashAppName != null) {
            splashAppName.setText(BuildConfig.APP_NAME);
        }

        // Make splash visible
        splashContainer.setVisibility(View.VISIBLE);
        splashContainer.setAlpha(1f);

        // Load splash image
        loadSplashImage();
    }

    /**
     * Load splash image from assets or use default app icon
     */
    private void loadSplashImage() {
        try {
            // Read the original pixel dimensions without decoding the bitmap.
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream stream = getAssets().open("splash_image.png")) {
                BitmapFactory.decodeStream(stream, null, bounds);
            }

            if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                // Use physical screen pixels: dp sizes would unnecessarily upscale images
                // on high-density phones and make the splash look blurry.
                Point screenSize = new Point();
                getWindowManager().getDefaultDisplay().getRealSize(screenSize);
                int screenWidth = Math.max(1, screenSize.x);
                int screenHeight = Math.max(1, screenSize.y);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inScaled = false; // Preserve source pixels; do not density-scale assets.

                // Decode close to the display resolution, without going below the
                // pixels needed by CENTER_CROP. This also reduces peak memory use
                // when a large splash asset is supplied.
                int sampleSize = 1;
                while (bounds.outWidth / (sampleSize * 2) >= screenWidth
                        && bounds.outHeight / (sampleSize * 2) >= screenHeight) {
                    sampleSize *= 2;
                }
                options.inSampleSize = sampleSize;

                try (InputStream stream = getAssets().open("splash_image.png")) {
                    Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
                    if (bitmap != null) {
                        // Keep one source pixel equal to one device pixel.
                        bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
                        if (bitmap.getWidth() >= screenWidth && bitmap.getHeight() >= screenHeight) {
                            // Full-bleed, sharp splash when the image has enough pixels.
                            splashImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            // Never enlarge an undersized asset: this avoids the
                            // strong blur caused by CENTER_CROP upscaling.
                            // The screen background may be visible around the image.
                            splashImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        }
                        splashImage.setImageBitmap(bitmap);
                        splashImage.setVisibility(View.VISIBLE);
                        defaultSplashContainer.setVisibility(View.GONE);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {
            // No custom splash asset, or an unreadable image: show the app icon.
        }

        splashImage.setVisibility(View.GONE);
        defaultSplashContainer.setVisibility(View.VISIBLE);
        loadSplashAppIcon();
    }

    /**
     * Load the original launcher image for the centered splash icon.
     * The mipmap launcher resources are only 48-192px and look blurry when
     * expanded into the 120dp splash icon view on high-density devices.
     */
    private void loadSplashAppIcon() {
        if (splashAppIcon == null) return;

        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream stream = getAssets().open("splash_icon.png")) {
                BitmapFactory.decodeStream(stream, null, bounds);
            }

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return;

            int targetSizePx = Math.max(1,
                    Math.round(120f * getResources().getDisplayMetrics().density));

            // Decode near the displayed size to keep memory use reasonable,
            // while never reducing the bitmap below the desired physical size.
            int sampleSize = 1;
            while (bounds.outWidth / (sampleSize * 2) >= targetSizePx
                    && bounds.outHeight / (sampleSize * 2) >= targetSizePx) {
                sampleSize *= 2;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inScaled = false;
            options.inSampleSize = sampleSize;

            try (InputStream stream = getAssets().open("splash_icon.png")) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
                if (bitmap == null) return;

                // BitmapDrawable otherwise treats the decoded bitmap as 160dpi
                // and may apply an additional, unwanted density scale.
                bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
                splashAppIcon.setScaleType(
                        bitmap.getWidth() >= targetSizePx && bitmap.getHeight() >= targetSizePx
                                ? ImageView.ScaleType.CENTER_CROP
                                : ImageView.ScaleType.CENTER_INSIDE);
                splashAppIcon.setImageBitmap(bitmap);
            }
        } catch (Exception ignored) {
            // The original icon asset may be absent for builds without an icon URL.
            // In that case keep the existing mipmap fallback from the layout.
        }
    }

    /**
     * Called when WebView has finished loading
     */
    private void onWebViewLoadComplete() {
        splashWebViewLoaded = true;
        checkAndHideSplash();
    }

    /**
     * Check if both conditions are met (min time elapsed AND webview loaded)
     * If so, trigger the fade out animation
     */
    private void checkAndHideSplash() {
        if (splashMinTimeElapsed && splashWebViewLoaded && !splashFinishing) {
            hideSplashWithAnimation();
        }
    }

    /**
     * Force hide splash (used for fallback timer)
     */
    private void forceHideSplash() {
        if (!splashFinishing) {
            hideSplashWithAnimation();
        }
    }

    /**
     * Hide splash immediately without animation (used when restoring state)
     */
    private void hideSplashImmediately() {
        splashFinishing = true;
        splashVisible = false;
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }
        if (splashContainer != null) {
            splashContainer.setVisibility(View.GONE);
        }
        // Release both splash images after they are no longer displayed.
        if (splashImage != null) {
            splashImage.setImageDrawable(null);
        }
        if (splashAppIcon != null) {
            splashAppIcon.setImageDrawable(null);
        }
        restoreNormalWindow();
    }

    /**
     * Hide splash screen with fade out animation
     */
    private void hideSplashWithAnimation() {
        if (splashFinishing || splashContainer == null) return;
        splashFinishing = true;

        // Remove any pending callbacks
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }

        // Set splashVisible to false and restore normal window BEFORE fade-out animation
        // This ensures content shifts to final position before splash disappears,
        // preventing visible content jump when splash ends
        // Apply theme color immediately so status bar has correct color during fade-out
        splashVisible = false;
        restoreNormalWindow(true);

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(SPLASH_FADE_OUT_DURATION);
        fadeOut.setFillAfter(true);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                splashContainer.setVisibility(View.GONE);
                // Release both images after the fade-out finishes.
                if (splashImage != null) {
                    splashImage.setImageDrawable(null);
                }
                if (splashAppIcon != null) {
                    splashAppIcon.setImageDrawable(null);
                }
                // Force apply status bar color after splash animation completes
                forceSetStatusBarColor();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        splashContainer.startAnimation(fadeOut);
    }

    /**
     * Force set status bar color to the configured theme color after splash screen ends.
     * This method is called after the splash animation completes to ensure the status bar
     * displays the correct color that was set during app packaging.
     */
    private void forceSetStatusBarColor() {
        if (isFinishing() || isDestroyed()) return;

        try {
            // Get the theme color from build configuration
            String colorStr = BuildConfig.THEME_COLOR;
            int color = Color.parseColor(colorStr);

            // Apply the status bar color immediately
            applyStatusBarColor(color);

            // Post additional delayed calls to ensure the color sticks
            // This handles cases where system may reset the color during window transitions
            postDelayedStatusBarColorUpdate(color, 100);
            postDelayedStatusBarColorUpdate(color, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Post a delayed update of status bar color
     */
    private void postDelayedStatusBarColorUpdate(int color, long delayMs) {
        if (splashHandler != null) {
            splashHandler.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    applyStatusBarColor(color);
                }
            }, delayMs);
        }
    }

    /**
     * Apply status bar color with proper window flags and appearance settings
     */
    private void applyStatusBarColor(int color) {
        try {
            Window window = getWindow();
            // Must clear translucent status flag, otherwise color won't be applied on some devices
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);

            WindowInsetsControllerCompat windowInsetsController =
                    WindowCompat.getInsetsController(window, window.getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.setAppearanceLightStatusBars(isColorLight(color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        webViewContainer = findViewById(R.id.webViewContainer);
        progressContainer = findViewById(R.id.progressContainer);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Get status bar height using WindowInsets API and apply padding to webViewContainer
        ViewCompat.setOnApplyWindowInsetsListener(webViewContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            statusBarHeight = systemBars.top;
            // Apply padding to webViewContainer based on fullscreen mode
            updateWebViewContainerPadding();
            // Update progress bar position
            updateProgressBarPosition();
            return insets;
        });

        // Initialize progress bar as hidden (alpha=0) and set hardware layer for GPU optimization
        progressContainer.setAlpha(0f);
        progressContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        progressIndicator.setScaleX(0f);
        progressIndicator.setPivotX(0f);

        // Request insets to be applied
        webViewContainer.requestApplyInsets();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings webSettings = webView.getSettings();

        // Enable JavaScript
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // DOM storage
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // Cache settings
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Media settings
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // Mixed content
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Viewport settings
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // User agent - append custom user agent to default
        String userAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString(userAgent + " " + BuildConfig.USER_AGENT);

        // Geolocation
        webSettings.setGeolocationEnabled(true);

        // Cookie settings
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // Initialize JS Bridge
        jsBridge = new JsBridge(this, webView);
        webView.addJavascriptInterface(jsBridge, "AndroidBridge");

        // WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                // Handle external links
                if (url.startsWith("tel:") || url.startsWith("mailto:") ||
                    url.startsWith("sms:") || url.startsWith("geo:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "无法打开链接", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                // Handle market links
                if (url.startsWith("market:") || url.startsWith("intent:")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }

                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                pageLoadComplete = false;
                showProgressBar();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pageLoadComplete = true;
                hideProgressBar();

                // Notify that page load is complete for splash screen
                onWebViewLoadComplete();

                // Inject JS Bridge helper
                injectJsBridgeHelper();
            }
        });

        // WebChromeClient
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                updateProgress(newProgress);

                // Ensure progress bar is visible while loading
                if (newProgress < 100 && !pageLoadComplete) {
                    showProgressBar();
                }

                // Hide progress bar when complete
                if (newProgress >= 100) {
                    hideProgressBar();
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // If there's already a pending callback, cancel it first to prevent WebView deadlock
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    fileChooserLauncher.launch(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "无法打开文件选择器", Toast.LENGTH_SHORT).show();
                    // Critical: must call callback on exception, otherwise WebView input element will be locked
                    if (MainActivity.this.filePathCallback != null) {
                        MainActivity.this.filePathCallback.onReceiveValue(null);
                        MainActivity.this.filePathCallback = null;
                    }
                    return false;
                }
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });
    }

    private void injectJsBridgeHelper() {
        String jsCode = "javascript:(function() {" +
                "if (window.NativeBridge) return;" +
                "window.NativeBridge = {" +
                "  showToast: function(message) { AndroidBridge.showToast(message); }," +
                "  vibrate: function(duration) { AndroidBridge.vibrate(duration); }," +
                "  getDeviceInfo: function() { return JSON.parse(AndroidBridge.getDeviceInfo()); }," +
                "  getExtendedDeviceInfo: function() { return JSON.parse(AndroidBridge.getExtendedDeviceInfo()); }," +
                "  getNetworkType: function() { return AndroidBridge.getNetworkType(); }," +
                "  copyToClipboard: function(text) { AndroidBridge.copyToClipboard(text); }," +
                "  getClipboardContent: function() { return AndroidBridge.getClipboardContent(); }," +
                "  openUrl: function(url) { AndroidBridge.openUrl(url); }," +
                "  share: function(title, text, url) { AndroidBridge.share(title, text, url); }," +
                "  scanQRCode: function(callback) { window._qrCallback = callback; AndroidBridge.scanQRCode(); }," +
                "  getCurrentLocation: function(callback) { window._locationCallback = callback; AndroidBridge.getCurrentLocation(); }," +
                "  setStatusBarColor: function(color) { AndroidBridge.setStatusBarColor(color); }," +
                "  getAppVersion: function() { return AndroidBridge.getAppVersion(); }," +
                "  exitApp: function() { AndroidBridge.exitApp(); }," +
                "  goBack: function() { AndroidBridge.goBack(); }," +
                "  reload: function() { AndroidBridge.reload(); }," +
                "  clearCache: function() { AndroidBridge.clearCache(); }," +
                "  setScreenOrientation: function(orientation) { AndroidBridge.setScreenOrientation(orientation); }," +
                "  getBatteryLevel: function() { return AndroidBridge.getBatteryLevel(); }," +
                "  isWifiConnected: function() { return AndroidBridge.isWifiConnected(); }," +
                "  playSound: function(soundName) { AndroidBridge.playSound(soundName); }," +
                "  takeScreenshot: function(callback) { window._screenshotCallback = callback; AndroidBridge.takeScreenshot(); }," +
                "  takeFullScreenshot: function(callback) { window._fullScreenshotCallback = callback; AndroidBridge.takeFullScreenshot(); }," +
                "  saveToGallery: function(base64, callback) { window._saveGalleryCallback = callback; AndroidBridge.saveToGallery(base64); }," +
                "  getContacts: function(callback) { window._contactsCallback = callback; AndroidBridge.getContacts(); }," +
                "  makeCall: function(phone) { AndroidBridge.makeCall(phone); }," +
                "  sendSMS: function(phone, message) { AndroidBridge.sendSMS(phone, message); }," +
                "  setLocalStorage: function(key, value) { AndroidBridge.setLocalStorage(key, value); }," +
                "  getLocalStorage: function(key) { return AndroidBridge.getLocalStorage(key); }," +
                "  removeLocalStorage: function(key) { AndroidBridge.removeLocalStorage(key); }," +
                "  clearLocalStorage: function() { AndroidBridge.clearLocalStorage(); }," +
                "  showLoading: function(message) { AndroidBridge.showLoading(message); }," +
                "  hideLoading: function() { AndroidBridge.hideLoading(); }," +
                "  showAlert: function(title, message, callback) { window._alertCallback = callback; AndroidBridge.showAlert(title, message); }," +
                "  showConfirm: function(title, message, callback) { window._confirmCallback = callback; AndroidBridge.showConfirm(title, message); }," +
                "  enableBackButton: function(enabled) { AndroidBridge.enableBackButton(enabled); }," +
                "  setTitle: function(title) { AndroidBridge.setTitle(title); }," +
                "  openSystemSettings: function() { AndroidBridge.openSystemSettings(); }," +
                "  openAppSettings: function() { AndroidBridge.openAppSettings(); }," +
                "  enterFullscreen: function() { AndroidBridge.enterFullscreen(); }," +
                "  exitFullscreen: function() { AndroidBridge.exitFullscreen(); }," +
                "  isFullscreenMode: function() { return AndroidBridge.isFullscreenMode(); }," +
                "  registerKeyListener: function(callback) { window._keyEventCallback = callback; AndroidBridge.registerKeyListener(); }," +
                "  unregisterKeyListener: function() { window._keyEventCallback = null; AndroidBridge.unregisterKeyListener(); }," +
                "  registerExitListener: function(callback) { window._exitEventCallback = callback; AndroidBridge.registerExitListener(); }," +
                "  unregisterExitListener: function() { window._exitEventCallback = null; AndroidBridge.unregisterExitListener(); }," +
                "  getGrantedPermissions: function() { return JSON.parse(AndroidBridge.getGrantedPermissions()); }" +
                "};" +
                "console.log('NativeBridge initialized');" +
                "if (window.onNativeBridgeReady) { window.onNativeBridgeReady(); }" +
                "document.dispatchEvent(new Event('NativeBridgeReady'));" +
                "})()";
        webView.evaluateJavascript(jsCode, null);
    }

    /**
     * Start QR code scanner activity
     */
    public void startQRCodeScanner() {
        Intent intent = new Intent(this, ScannerActivity.class);
        qrCodeScannerLauncher.launch(intent);
    }

    /**
     * Request current location with permission handling
     */
    public void requestLocation() {
        if (hasLocationPermission()) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 1. Get all available providers
        List<String> providers = locationManager.getProviders(true);

        if (providers.isEmpty()) {
            sendLocationErrorToJs("error", "没有可用的定位服务，请打开GPS");
            return;
        }

        // 2. Try to get last known location for fast response
        // This provides instant location (though possibly outdated), better than making users wait
        Location bestLastLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) continue;
            // Select best location based on recency and accuracy
            if (bestLastLocation == null) {
                bestLastLocation = l;
            } else {
                // Prefer newer location, but also consider accuracy
                long timeDelta = l.getTime() - bestLastLocation.getTime();
                float accuracyDelta = l.getAccuracy() - bestLastLocation.getAccuracy();
                // Use new location if it's significantly newer or notably more accurate
                if (timeDelta > 30000 || (timeDelta > -60000 && accuracyDelta < -10)) {
                    bestLastLocation = l;
                }
            }
        }

        // If we have a recent location (within 2 minutes), return it immediately
        if (bestLastLocation != null && System.currentTimeMillis() - bestLastLocation.getTime() < LAST_LOCATION_MAX_AGE_MS) {
            sendLocationToJs(bestLastLocation, "success");
            return;
        }

        // 3. Define location listener with synchronized cleanup to prevent race conditions
        final AtomicBoolean locationReceived = new AtomicBoolean(false);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Use atomic flag to ensure only one location is processed
                if (locationReceived.getAndSet(true)) {
                    return;
                }

                // Got location, send to JS
                sendLocationToJs(location, "success");

                // Remove listener and cancel timeout
                removeLocationUpdates();
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        // 4. Request real-time updates from both GPS and Network providers
        // Uses whichever provider responds first for fastest result
        try {
            // Network location: Fast indoors using WiFi/cell towers, but requires network connectivity
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL_MS,
                    LOCATION_MIN_DISTANCE_METERS,
                    locationListener
                );
            }

            // GPS location: Accurate outdoors using satellites, but doesn't work indoors
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL_MS,
                    LOCATION_MIN_DISTANCE_METERS,
                    locationListener
                );
            }

            // 5. Set timeout mechanism to report error if no location received
            locationTimeoutHandler = new Handler(Looper.getMainLooper());
            locationTimeoutHandler.postDelayed(() -> {
                // Use atomic flag to ensure timeout is only processed if no location was received
                if (!locationReceived.getAndSet(true)) {
                    sendLocationErrorToJs("timeout", "定位超时");
                    removeLocationUpdates();
                }
            }, LOCATION_TIMEOUT_MS);

        } catch (Exception e) {
            sendLocationErrorToJs("error", "定位请求失败: " + e.getMessage());
            // Clean up listener on exception
            removeLocationUpdates();
        }
    }

    /**
     * Helper method to remove location updates and cancel timeout
     */
    private void removeLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
        if (locationTimeoutHandler != null) {
            locationTimeoutHandler.removeCallbacksAndMessages(null);
            locationTimeoutHandler = null;
        }
    }

    private void sendLocationToJs(Location location, String status) {
        runOnUiThread(() -> {
            try {
                JSONObject response = new JSONObject();
                response.put("status", status);
                response.put("latitude", location.getLatitude());
                response.put("longitude", location.getLongitude());
                response.put("accuracy", location.getAccuracy());
                response.put("altitude", location.getAltitude());
                response.put("speed", location.getSpeed());
                response.put("timestamp", location.getTime());
                executeJsCallback("_locationCallback", response.toString());
            } catch (Exception e) {
                sendLocationErrorToJs("error", e.getMessage());
            }
        });
    }

    private void sendLocationErrorToJs(String status, String message) {
        runOnUiThread(() -> {
            try {
                JSONObject response = new JSONObject();
                response.put("status", status);
                response.put("message", message != null ? message : "Unknown error");
                executeJsCallback("_locationCallback", response.toString());
            } catch (Exception e) {
                executeJsCallback("_locationCallback", "{\"status\":\"error\",\"message\":\"Unknown error\"}");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            // Check if either location permission was granted
            boolean permissionGranted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    break;
                }
            }

            if (permissionGranted) {
                getLocation();
            } else {
                sendLocationErrorToJs("permission_denied", "位置权限被拒绝");
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            // Android 10+ (API 29) shouldn't reach here as we handle it directly
            // This is for Android 9 and below
            boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionGranted && pendingSaveBase64 != null) {
                // Permission granted, proceed with saving
                jsBridge.saveToGalleryInternal(pendingSaveBase64);
            } else {
                // Permission denied
                sendStoragePermissionDenied();
            }
            pendingSaveBase64 = null;
        }
    }

    /**
     * Request storage permission for saving images
     * Android 10+ (API 29) uses MediaStore and doesn't need WRITE_EXTERNAL_STORAGE permission
     */
    public void requestStoragePermissionForSave(String base64) {
        pendingSaveBase64 = base64;
        // Android 10+ (API 29) uses MediaStore mechanism and doesn't need WRITE permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Directly save using MediaStore
            jsBridge.saveToGalleryInternal(base64);
            pendingSaveBase64 = null;
        } else {
            // Android 9 and below need WRITE_EXTERNAL_STORAGE permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST);
        }
    }

    /**
     * Check if storage permission is granted
     */
    public boolean hasStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10+ doesn't need storage permission for MediaStore
            return true;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void sendStoragePermissionDenied() {
        runOnUiThread(() -> {
            try {
                org.json.JSONObject response = new org.json.JSONObject();
                response.put("success", false);
                response.put("message", "用户拒绝存储权限");
                executeJsCallback("_saveGalleryCallback", response.toString());
            } catch (Exception e) {
                executeJsCallback("_saveGalleryCallback", "{\"success\":false,\"message\":\"用户拒绝存储权限\"}");
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Block back button during splash screen
        if (splashVisible && keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        // Respect NativeBridge.enableBackButton(false) for the physical Back key.
        // Consume the event without navigating back or exiting the app.
        if (keyCode == KeyEvent.KEYCODE_BACK && jsBridge != null && !jsBridge.isBackButtonEnabled()) {
            return true;
        }

        // Notify JavaScript about key events if listener is enabled
        if (keyListenerEnabled) {
            notifyKeyEvent(keyCode, "keydown");
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // In fullscreen mode, directly exit the app instead of exiting fullscreen first
            if (jsBridge != null && jsBridge.isFullscreenMode()) {
                handleBackExit();
                return true;
            }

            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                handleBackExit();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void notifyKeyEvent(int keyCode, String eventType) {
        runOnUiThread(() -> {
            try {
                JSONObject response = new JSONObject();
                response.put("eventType", eventType);

                String keyName;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        keyName = "back";
                        break;
                    case KeyEvent.KEYCODE_HOME:
                        keyName = "home";
                        break;
                    case KeyEvent.KEYCODE_APP_SWITCH:
                        keyName = "task";
                        break;
                    case KeyEvent.KEYCODE_MENU:
                        keyName = "menu";
                        break;
                    default:
                        keyName = "key_" + keyCode;
                        break;
                }
                response.put("key", keyName);
                response.put("keyCode", keyCode);

                executeJsCallback("_keyEventCallback", response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleBackExit() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackPressTime <= BACK_PRESS_EXIT_INTERVAL) {
            // Double tap within 0.5 seconds, exit the app
            if (exitToast != null) {
                exitToast.cancel();
            }
            // Notify exit listener before exiting
            if (exitListenerEnabled) {
                notifyExitEvent();
            }
            finish();
        } else {
            // First tap, show toast message at bottom
            lastBackPressTime = currentTime;
            if (exitToast != null) {
                exitToast.cancel();
            }
            exitToast = Toast.makeText(this, R.string.exit_toast_message, Toast.LENGTH_SHORT);
            exitToast.setGravity(Gravity.BOTTOM, 0, 100);
            exitToast.show();
        }
    }

    private void notifyExitEvent() {
        runOnUiThread(() -> {
            try {
                JSONObject response = new JSONObject();
                response.put("event", "exit");
                response.put("timestamp", System.currentTimeMillis());
                executeJsCallback("_exitEventCallback", response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void setKeyListenerEnabled(boolean enabled) {
        this.keyListenerEnabled = enabled;
    }

    public void setExitListenerEnabled(boolean enabled) {
        this.exitListenerEnabled = enabled;
    }

    public void executeJsCallback(String callbackName, String data) {
        runOnUiThread(() -> {
            String js = "javascript:if(window." + callbackName + "){window." + callbackName + "(" + data + ");}";
            webView.evaluateJavascript(js, null);
        });
    }

    private void showProgressBar() {
        // Don't show progress bar during splash screen
        if (splashVisible) return;

        progressHandler.removeCallbacksAndMessages(null);
        if (!progressVisible) {
            progressVisible = true;
            // Update position for current fullscreen state
            updateProgressBarPosition();
            // Fade in using alpha animation with hardware layer for GPU optimization
            progressContainer.animate()
                    .alpha(1f)
                    .setDuration(PROGRESS_ANIMATION_DURATION)
                    .start();
        }
    }

    private void hideProgressBar() {
        // Delay hiding progress bar slightly to ensure smooth transition
        progressHandler.removeCallbacksAndMessages(null);
        progressHandler.postDelayed(() -> {
            progressVisible = false;
            // First animate progress to 100%, then fade out
            progressIndicator.animate()
                    .scaleX(1f)
                    .setDuration(PROGRESS_ANIMATION_DURATION)
                    .withEndAction(() -> {
                        // Fade out the container
                        progressContainer.animate()
                                .alpha(0f)
                                .setDuration(PROGRESS_FADE_OUT_DURATION)
                                .withEndAction(() -> {
                                    // Reset scaleX to 0 for next load
                                    progressIndicator.setScaleX(0f);
                                })
                                .start();
                    })
                    .start();
        }, PROGRESS_HIDE_DELAY);
    }

    /**
     * Update progress bar scaleX with smooth animation
     * Cancels any previous animation to prevent performance issues with rapid updates
     */
    private void updateProgress(int progress) {
        // Cancel previous animation if running
        if (currentProgressAnimation != null) {
            currentProgressAnimation.cancel();
        }

        float scale = progress / 100f;
        currentProgressAnimation = progressIndicator.animate()
                .scaleX(scale)
                .setDuration(PROGRESS_ANIMATION_DURATION);
        currentProgressAnimation.start();
    }

    /**
     * Update progress bar position based on fullscreen mode
     * In fullscreen mode, position at top (translationY=0)
     * In normal mode, position below status bar
     */
    private void updateProgressBarPosition() {
        if (progressContainer == null) return;

        // Check fullscreen state, default to non-fullscreen if jsBridge not yet initialized
        boolean isFullscreen = jsBridge != null && jsBridge.isFullscreenMode();
        // During splash, position at top; after splash and not fullscreen, position below status bar
        float translationY = (splashVisible || isFullscreen) ? 0f : statusBarHeight;
        progressContainer.setTranslationY(translationY);
    }

    /**
     * Update webViewContainer padding based on fullscreen mode and splash visibility
     * In fullscreen mode or during splash, no padding (webview fills entire screen)
     * In normal mode after splash, add top padding equal to status bar height
     */
    private void updateWebViewContainerPadding() {
        if (webViewContainer == null) return;

        // Check fullscreen state, default to non-fullscreen if jsBridge not yet initialized
        boolean isFullscreen = jsBridge != null && jsBridge.isFullscreenMode();
        // During splash or in fullscreen mode, no padding; otherwise, add status bar height as padding
        int topPadding = (splashVisible || isFullscreen) ? 0 : statusBarHeight;
        webViewContainer.setPadding(0, topPadding, 0, 0);
    }

    /**
     * Called when fullscreen mode changes to update layout positions
     */
    public void onFullscreenModeChanged() {
        runOnUiThread(() -> {
            updateProgressBarPosition();
            updateWebViewContainerPadding();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (progressHandler != null) {
            progressHandler.removeCallbacksAndMessages(null);
        }
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }
        // Clean up location listener
        removeLocationUpdates();
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
