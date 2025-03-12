package tap.company.go_sell_sdk_flutter;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * GoSellSdkFlutterPlugin - Updated for Flutter 3.x
 */
public class GoSellSdkFlutterPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin, ActivityAware {

    private MethodChannel channel;
    private GoSellSdKDelegate delegate;
    private FlutterPluginBinding pluginBinding;
    private ActivityPluginBinding activityBinding;
    private Application application;
    private Activity activity;
    private Lifecycle lifecycle;
    private LifeCycleObserver observer;
    private static final String CHANNEL = "tap.company.go_sell_sdk_flutter.GoSellSdkFlutterPlugin";

    public GoSellSdkFlutterPlugin() {
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        pluginBinding = binding;
        channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL);
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        pluginBinding = null;
        if (channel != null) {
            channel.setMethodCallHandler(null);
            channel = null;
        }
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activityBinding = binding;
        activity = binding.getActivity();
        setup(pluginBinding.getBinaryMessenger(), (Application) pluginBinding.getApplicationContext(), activity, binding);
    }

    @Override
    public void onDetachedFromActivity() {
        tearDown();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    private void setup(
            final BinaryMessenger messenger,
            final Application application,
            final Activity activity,
            final ActivityPluginBinding activityBinding) {
        this.activity = activity;
        this.application = application;
        this.delegate = new GoSellSdKDelegate(activity);
        observer = new LifeCycleObserver(activity);

        if (activityBinding != null) {
            activityBinding.addActivityResultListener(delegate);
            activityBinding.addRequestPermissionsResultListener(delegate);
        }
    }

    private void tearDown() {
        if (activityBinding != null) {
            activityBinding.removeActivityResultListener(delegate);
            activityBinding.removeRequestPermissionsResultListener(delegate);
        }
        activityBinding = null;
        if (lifecycle != null)
            lifecycle.removeObserver(observer);
        lifecycle = null;
        delegate = null;
        application = null;
    }

    private static class MethodResultWrapper implements MethodChannel.Result {
        private MethodChannel.Result methodResult;
        private Handler handler;

        MethodResultWrapper(MethodChannel.Result result) {
            methodResult = result;
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void success(final Object result) {
            handler.post(() -> methodResult.success(result));
        }

        @Override
        public void error(final String errorCode, final String errorMessage, final Object errorDetails) {
            handler.post(() -> methodResult.error(errorCode, errorMessage, errorDetails));
        }

        @Override
        public void notImplemented() {
            handler.post(methodResult::notImplemented);
        }
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result rawResult) {
        if (activity == null) {
            rawResult.error("no_activity", "SDK plugin requires a foreground activity.", null);
            return;
        }
        MethodChannel.Result result = new MethodResultWrapper(rawResult);
        delegate.startSDK(call, result, (HashMap<String, Object>) call.arguments());
    }
}
