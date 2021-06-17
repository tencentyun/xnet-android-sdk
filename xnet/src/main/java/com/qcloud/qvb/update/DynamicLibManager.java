package com.qcloud.qvb.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Created by passion on 16-9-20.
 */
public class DynamicLibManager {
    public static final String DYNAMIC_LIB_NAME = "libxp2p";
    public static final String[] ANDROID_ABIS = {"armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64"};

    private Context context;
    private String basePath;
    private String libPath;

    //jni接口版本
    private String jniVersion = "v8";
    private boolean supportHttps = false;
    private String archAbi = "";

    public DynamicLibManager(Context context) {
        this.context = context;
        basePath = context.getFilesDir().getAbsolutePath() + File.separator + "vlib";

        StringBuilder tmpLibPath = new StringBuilder();
        tmpLibPath.append(basePath)
                .append(File.separator)
                .append(getAppVersion())
                .append(File.separator)
                .append(jniVersion);

        File tmpDir = new File(tmpLibPath.toString());
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        libPath = tmpLibPath.toString();
        int archAbiNum = 0;
        String tmpArchAbi = "";
        for (File file : tmpDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                return false;
            }
        })) {
            archAbiNum += 1;
            if (Arrays.asList(ANDROID_ABIS).contains(file.getName())) {
                tmpArchAbi = file.getName();
            }
        }

        //如果扫描到一个文件夹获取到arch, 就可以加载了, archCpuAbi不为"" 可以使用locate不然不能用locate
        if (archAbiNum == 1 && !tmpArchAbi.isEmpty()) {
            archAbi = tmpArchAbi;
            tmpLibPath.append(File.separator).append(archAbi);
            libPath = tmpLibPath.toString();

            //检测curentLibDirPath存不存在
            if (!(new File(libPath)).exists()) {
                (new File(libPath)).mkdirs();
            }
        } else {
            deleteDir(tmpDir);
        }
    }

    public void checkUpdateV2(final String version, final String arch) {
        if (!libPath.endsWith(File.separator + arch)) {
            libPath = libPath + File.separator + arch;
            File tmpDir = new File(libPath);
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
        }
        //使用传进来的arch
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String packageName = context.getPackageName();
                    //获取10位unix时间戳
                    String timeStamp = Long.toString(System.currentTimeMillis() / 1000);
                    String token = MD5Util.MD5((timeStamp + "qvb2017tencent" + packageName).getBytes());

                    StringBuffer sb = new StringBuffer();
                    sb.append("https://update.qvb.qcloud.com/checkupdate").append("/v2")
                            .append("?abi=").append(arch)
                            .append("&token=").append(token)
                            .append("&timeStamp=").append(timeStamp)
                            .append("&jniVersion=").append(jniVersion)
                            .append("&packageName=").append(context.getPackageName());

                    if (supportHttps) {
                        sb.append("&supportHttps=true");
                    }
                    sb.append("&fileId=").append(DYNAMIC_LIB_NAME)
                            .append("&fifoVersion=").append(version);

                    URL url = new URL(sb.toString());

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(30_000);
                    conn.setReadTimeout(10_000);
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String jsonStr = "";
                        String line;
                        while ((line = input.readLine()) != null) {
                            jsonStr += line;
                        }
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        if (jsonObj.has("downloadUrl")) {
                            Map<String, JSONObject> soJsonMap = new HashMap<>();
                            JSONObject jsonObjDownload = jsonObj.getJSONObject("downloadUrl");

                            if (jsonObjDownload.has(DYNAMIC_LIB_NAME)) {
                                JSONObject jsonObjTmp = jsonObjDownload.getJSONObject(DYNAMIC_LIB_NAME);
                                if (jsonObjTmp.has("jniVersion")
                                        && !TextUtils.isEmpty(jsonObjTmp.getString("jniVersion"))
                                        && jsonObjTmp.has("version")
                                        && !TextUtils.isEmpty(jsonObjTmp.getString("version"))
                                        && jsonObjTmp.has("url")
                                        && !TextUtils.isEmpty(jsonObjTmp.getString("url"))
                                        && jsonObjTmp.has("md5token")
                                        && !TextUtils.isEmpty(jsonObjTmp.getString("md5token"))) {
                                    soJsonMap.put(DYNAMIC_LIB_NAME, jsonObjTmp);
                                }
                            }

                            for (Map.Entry<String, JSONObject> entry : soJsonMap.entrySet()) {
                                JSONObject jsonObject = entry.getValue();
                                updateDynamicLib(entry.getKey(), jsonObject.getString("url"), jsonObject.getString("version"),
                                        jsonObject.getString("md5token"));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //存在so或者下载完成, 返回true
            private boolean updateDynamicLib(String libName, String downloadUrl, String newVersion, String md5) throws Exception {
                String libFilename = libName + "_" + newVersion + "_" + md5 + ".so";
                String tmpFilename = libName + "_" + newVersion + "_" + md5 + ".tmp";
                String fullLibFilename = libPath + File.separator + libFilename;
                String fullTmpFilename = libPath + File.separator + tmpFilename;

                //上次下载未完成，存在文件，返回true
                if (new File(fullLibFilename).exists()) {
                    return true;
                }
                File tmpDir = new File(libPath);
                // 删除无用的tmp文件
                for (File file : tmpDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".tmp");
                    }
                })) {
                    if (!file.getName().equals(libFilename)) {
                        file.delete();
                    }
                }

                File tmpFile = new File(fullTmpFilename);
                if (!tmpFile.exists()) {
                    tmpFile.createNewFile();
                }
                long finishedSize = tmpFile.length();

                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30_000);
                conn.setReadTimeout(30_000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Range", "bytes=" + finishedSize + "-");
                if (conn.getResponseCode() == 206 || conn.getResponseCode() == 200) {
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw");
                    try {
                        //下载buffer要为4k以上，不然安全团队找
                        byte[] bytes = new byte[10240];
                        int count;
                        while ((count = bis.read(bytes)) != -1) {
                            raf.seek(finishedSize);
                            raf.write(bytes, 0, count);
                            finishedSize += count;
                        }

                        // 对比指纹是否正确
                        String md5sum = MD5Util.MD5(tmpFile);
                        if (md5sum.toLowerCase(Locale.US).equals(md5.toLowerCase(Locale.US))) {
                            tmpFile.renameTo(new File(fullLibFilename));
                            return true;
                        }
                        tmpFile.delete();
                    } finally {
                        raf.close();
                        bis.close();
                    }
                }
                return false;
            }
        }).start();
    }

    public String locate(final String fileid) {
        if (archAbi.isEmpty()) {
            return null;
        }

        File[] dirs = new File(basePath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && !file.getName().equals(getAppVersion());
            }
        });
        // 删除旧版本的残留热更文件
        for (File dir : dirs) {
            deleteDir(dir);
        }

        File destFile = null;
        String maxVersion = "";
        String md5 = "";
        for (File file : (new File(libPath)).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getName().startsWith(fileid) && file.getName().endsWith(".so"));
            }
        })) {
            String[] info = file.getName().split("_");
            if (info.length == 3 && info[1].compareTo(maxVersion) > 0) {
                if (destFile != null) {
                    destFile.delete();
                }
                maxVersion = info[1];
                destFile = file;
                //md5是类似 21a948385c11706669a7740309968ee1.so 的
                md5 = info[2];
            }
        }

        if (destFile != null) {
            // 对比指纹是否正确
            String md5sum = MD5Util.MD5(destFile);
            if ((md5sum + ".so").toLowerCase(Locale.US).equals(md5.toLowerCase(Locale.US))) {
                return (libPath + File.separator + destFile.getName());
            }
        }
        return null;
    }

    private String getAppVersion() {
        PackageManager packageManager = context.getPackageManager();
        String version = "default";
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}