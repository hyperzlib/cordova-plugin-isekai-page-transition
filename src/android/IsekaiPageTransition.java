package cn.isekai.bbs;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;

public class IsekaiPageTransition extends CordovaPlugin {
    private static final String TAG = "IsekaiPageTransition";

    private ImageView oldPageImageView;

    /** @TODO: 2021/9/5 增加侧拉返回 */
    private ImageView placeholderImageView;

    private FrameLayout frameLayout;

    private boolean isFreeze = false;

    private float retinaFactor;

    private FastOutSlowInInterpolator interpolator = new FastOutSlowInInterpolator();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        oldPageImageView = new ImageView(cordova.getActivity().getBaseContext());
        //placeholderImageView = new ImageView(cordova.getActivity().getBaseContext());

        enableHardwareAcceleration();

        frameLayout = new FrameLayout(cordova.getActivity());
        View webViewView = webView.getView();
        frameLayout.setLayoutParams(webViewView.getLayoutParams());
        ViewGroup viewGroup = (ViewGroup) webViewView.getParent();

        if (viewGroup != null) {
            viewGroup.addView(frameLayout, webViewView.getLayoutParams());
            viewGroup.removeView(webViewView);
        }

        frameLayout.addView(webViewView);
        frameLayout.addView(oldPageImageView);
        //frameLayout.addView(placeholderImageView);

        DisplayMetrics metrics = new DisplayMetrics();
        cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        retinaFactor = metrics.density;
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        if ("freeze".equals(action)) { // 冻结画面，截图当前页面
            cordova.getActivity().runOnUiThread(() -> setFreeze(true, callbackContext));
            return true;
        } else if ("unfreeze".equals(action)) { // 解冻画面
            cordova.getActivity().runOnUiThread(() -> setFreeze(false, callbackContext));
            return true;
        } else if ("animateForward".equals(action)) {
            cordova.getActivity().runOnUiThread(() -> animateForward(callbackContext));
            return true;
        } else if ("animateBackward".equals(action)) {
            cordova.getActivity().runOnUiThread(() -> animateBackward(callbackContext));
            return true;
        } else if ("animateFade".equals(action)) {
            cordova.getActivity().runOnUiThread(() -> animateFade(callbackContext));
        }

        return false;
    }

    private void enableHardwareAcceleration() {
        Window window = cordova.getActivity().getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        oldPageImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void setFreeze(boolean isFreeze) {
        setFreeze(isFreeze, null);
    }

    public void setFreeze(boolean isFreeze, CallbackContext callback) {
        webView.getView().clearAnimation();
        oldPageImageView.clearAnimation();

        if (isFreeze) {
            if (!this.isFreeze) {
                try {
                    oldPageImageView.setImageBitmap(getWebviewBitmap());
                } catch(Exception e) {
                    Log.e(TAG, "Cannot get webview bitmap", e);
                }
            }
            bringToFront(oldPageImageView);
        } else {
            if (this.isFreeze)
                oldPageImageView.setImageBitmap(null);
            bringToFront(webView.getView());
        }
        this.isFreeze = isFreeze;
        if (callback != null)
            callback.success();
    }

    public void bringToFront(View view) {
        view.bringToFront();
        // view.setVisibility(View.VISIBLE);
    }

    private Bitmap getWebviewBitmap() {
        Bitmap bitmap;

        View view = webView.getView();
        view.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(view.getDrawingCache());
        bitmap.setHasAlpha(false);

        view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    public void animateForward() {
        animateForward(null);
    }

    public void animateForward(CallbackContext callback) {
        setFreeze(true);

        Context context = cordova.getActivity().getBaseContext();
        View webViewView = webView.getView();

        Animation enterAnim = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
        Animation leaveAnim = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);

        enterAnim.setFillAfter(true);
        leaveAnim.setFillAfter(false);
        enterAnim.setInterpolator(interpolator);
        leaveAnim.setInterpolator(interpolator);

        enterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                webViewView.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setFreeze(false);
                if (callback != null)
                    callback.success();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        // 把webview放到屏幕右侧，向左滑动
        webViewView.setAlpha(0);
        bringToFront(webViewView);
        webViewView.startAnimation(enterAnim);
        oldPageImageView.startAnimation(leaveAnim);
    }

    public void animateBackward() {
        animateBackward(null);
    }

    public void animateBackward(CallbackContext callback) {
        if (!isFreeze)
            setFreeze(true);

        Context context = cordova.getActivity().getBaseContext();
        View webViewView = webView.getView();

        Animation enterAnim = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
        Animation leaveAnim = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);

        enterAnim.setFillAfter(true);
        leaveAnim.setFillAfter(false);
        enterAnim.setInterpolator(interpolator);
        leaveAnim.setInterpolator(interpolator);

        enterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                setFreeze(false);
                if (callback != null)
                    callback.success();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        oldPageImageView.startAnimation(leaveAnim);
        webViewView.startAnimation(enterAnim);
    }

    public void animateFade() {
        animateFade(null);
    }

    public void animateFade(CallbackContext callback) {
        if (!isFreeze)
            setFreeze(true);

        Context context = cordova.getActivity().getBaseContext();
        View webViewView = webView.getView();

        Animation enterAnim = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        enterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                webViewView.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setFreeze(false);
                if (callback != null)
                    callback.success();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        webViewView.setAlpha(0);
        bringToFront(webViewView);
        webViewView.startAnimation(enterAnim);
    }
}