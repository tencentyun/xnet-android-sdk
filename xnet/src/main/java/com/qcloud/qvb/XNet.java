package com.qcloud.qvb;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * XP2P-SDK全局配置
 */
public final class XNet {
    static final String TAG = "[TencentXP2P]" + "[" + XNet.class.getSimpleName() + "]";

    // 持久保存SDK版本号
    private static String SDK_VERSION;
    private static String archCpuAbi = "";
    private static WeakReference<Context> appCtx = null;
    private static boolean sIsSoLoaded = false;
    private static String cacheDir = null;
    private static String filesDir = null;

    /**
     * 设置conf目录，由于保存配置相关数据，默认为Context.getFilesDir
     *
     * @param dir conf目录的绝对路径
     */
    public static void setFilesDir(String dir) {
        filesDir = dir;
    }

    /**
     * 获取本地代理监听地址
     *
     * @return 本地代理监听地址, 默认为http://127.0.0.1:16080
     */
    public static String getHost() {
        return XNet._host();
    }
    /**
     * 获取本地代理监听端口
     *
     * @return 本地代理监听端口
     */
    public static String port() {
        return XNet._port();
    }

    /**
     * 设置本地监听域名别名
     *
     * @param host sdk 监听域名
     * @param name sdk alias 别名
     */
    public static void alias(String host, String name) {
        XNet._alias(host, name);
    }
    /**
     * 设置主服务名
     * @param name sdk server 别名
     */
    public static void setMaster(String name) {
        XNet._setMaster(name);
    }

    /**
     * 新启动一个p2p模块，注意四个参数绝对不能为null,在程序启动时调用
     *
     * @param context      上下文
     * @param appId        应用唯一标识
     * @param appKey       应用密钥
     * @param appSecretKey 应用加密混淆字段，可选
     * @return P2PModule的唯一实例
     * @throws Exception 当参数为null或者p2p模块加载不成功时抛出异常
     */
    public static int create(Context context, String appId, String appKey, String appSecretKey) throws Exception {
        Log.i(TAG, "init XNet.");
        if (context == null || appId == null || appKey == null || appSecretKey == null) {
            throw new NullPointerException("context or appId or appKey or appSecretKey can't be null when init p2p live stream!");
        }
        appCtx = new WeakReference<>(context);
        loadLibrary(context);
        int ret = (int) _construct(appId, appKey, appSecretKey, ((ContextWrapper) context).getBaseContext());
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences("BuglySdkInfos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("e30245116c", getVersion());
        editor.commit();
        return ret;
    }

    /**
     * 返回根据域名类型的代理域名, 直播domain默认为live.p2p.com
     *
     * @param domain 代理域名, 直播domain默认为live.p2p.com
     * @return 本地代理监听host/path, 默认为http://127.0.0.1:16080/domain/
     */
    public static String proxyOf(String domain) {
        return getHost() + "/" + domain + "/";
    }

    @CalledByNative
    private static String getCacheDir() {
        if (cacheDir != null) return cacheDir;
        Context ctx = appCtx.get();
        return ctx != null ? ctx.getCacheDir().getAbsolutePath() : "/";
    }

    /**
     * 获取native应用的版本号
     *
     * @return SDK的版本号
     */
    public static String getVersion() {
        if (sIsSoLoaded && SDK_VERSION == null) {
            SDK_VERSION = XNet._version();
        }
        return SDK_VERSION;
    }

    /**
     * 启用调试模式，该模式会输出一些调试信息，对测试版本尤其有效
     */
    public static void enableDebug() {
        XNet._enableDebug();
    }

    /**
     * 关闭调试模式。默认调试功能是关闭的，发布应用时也应该关闭调试模式。
     */
    public static void disableDebug() {
        XNet._disableDebug();
    }

    /**
     * 设置一个回调接口，用于接收XP2P-SDK输出的日志
     *
     * @param logger logger
     */
    public static void setLoggerCallback(LoggerCallback logger) {
        LoggerCallback.setLoggerCallback(logger);
        if (sIsSoLoaded) {
            XNet._setLogger();
        }
    }

    public static boolean resume() {
        if (sIsSoLoaded) {
            XNet._resume();
            return true;
        }
        return false;
    }

    /**
     * 获取P2P模块的版本号
     *
     * @return P2P模块的版本号
     */
    private static native String _version();

    /**
     * 获取当前运行的ABI名称
     *
     * @return 当前运行的ABI名称
     */
    private static native String _targetArchABI();

    /**
     * 打开调试模式，默认是关闭调试模式的
     */
    private static native void _enableDebug();

    /**
     * 关闭调试模式，应用上线时应关闭调试模式
     */
    private static native void _disableDebug();

    /**
     * 设置自定义logger打印回调函数
     */
    private static native void _setLogger();

    /**
     * native应用初始化
     *
     * @return 成功返回native代码里面对应对象的指针，失败返回0
     */
    private native static long _construct(String appId, String appKey, String appSecretKey, Context context);

    /**
     * 获取P2P代理服务器监听地址
     *
     * @return P2P代理服务器监听地址
     */
    private static native String _host();
    /**
     * 获取P2P代理服务器监听端口
     *
     * @return P2P代理服务器监听端口
     */
    private static native String _port();

    /**
     * app从后台回到前后时调用
     */
    private static native void _resume();

    /**
     * 设置本地监听域名别名
     *
     * @param host sdk 监听域名
     * @param name sdk alias 别名
     */
    private static native void _alias(String host, String name);
    /**
     * 设置主服务名
     * @param name sdk server 名
     */
    private static native void _setMaster(String name);

    private static void loadLibrary(Context context) throws Exception {
        String exceptionMessage = "load library failed.";
        try {
            System.loadLibrary("xp2p");
            sIsSoLoaded = true;
        } catch (Exception e) {
            Log.e(TAG, exceptionMessage, e);
            exceptionMessage = TextUtils.isEmpty(e.getMessage()) ? exceptionMessage : e.getMessage();
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, exceptionMessage, e);
            exceptionMessage = TextUtils.isEmpty(e.getMessage()) ? exceptionMessage : e.getMessage();
        }
        if (!sIsSoLoaded) {
            throw new Exception(exceptionMessage);
        }
    }

    /**
     * 设置cache目录，用于保存缓存相关数据，默认为Context.getCacheDir
     *
     * @param dir cache目录的绝对路径
     */
    public static void setCacheDir(String dir) {
        cacheDir = dir;
    }

    @CalledByNative
    private static String getDiskDir() {
        if (filesDir != null) return filesDir;
        Context ctx = appCtx.get();
        return ctx != null ? ctx.getFilesDir().getAbsolutePath() : "/";
    }

    @CalledByNative
    protected void onEvent(int code, String msg) {
        switch (code) {
            default:
                break;
        }
    }
}
