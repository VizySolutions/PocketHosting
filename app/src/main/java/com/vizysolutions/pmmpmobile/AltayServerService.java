package com.vizysolutions.pmmpmobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/* JADX INFO: loaded from: classes2.dex */
public class AltayServerService extends Service {
    public static final String ACTION_RESTART = "com.vizysolutions.pmmpmobile.RESTART";
    public static final String ACTION_START = "com.vizysolutions.pmmpmobile.START";
    public static final String ACTION_STOP = "com.vizysolutions.pmmpmobile.STOP";
    public static final String ACTION_STOP_NOTIFICATION = "com.vizysolutions.pmmpmobile.STOP_NOTIFICATION";
    public static final String CHANNEL_ID = "altay_server_channel";
    private static final int NOTIFICATION_ID = 1001;
    public static final String PREF_PHP_PATH = "php_path";
    public static final String PREF_XBOX_AUTH = "xbox_auth_enabled";
    private static Process process;
    private static boolean starting;
    private static BufferedWriter stdin;
    private PowerManager.WakeLock wakeLock;
    private static final List<ServerListener> listeners = new CopyOnWriteArrayList();
    private static final StringBuilder logBuffer = new StringBuilder();
    private static long lastPublishMs = 0;
    private static boolean publishScheduled = false;

    public interface ServerListener {
        void onLogChanged(String str);

        void onStateChanged(boolean z);
    }

    public static void addListener(ServerListener listener) {
        if (listener != null) {
            List<ServerListener> list = listeners;
            if (!list.contains(listener)) {
                list.add(listener);
            }
        }
    }

    public static void removeListener(ServerListener listener) {
        listeners.remove(listener);
    }

    public static boolean isRunningStatic() {
        Process process2 = process;
        if (process2 == null) {
            return false;
        }
        try {
            process2.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public static String getLogText() {
        String string;
        StringBuilder sb = logBuffer;
        synchronized (sb) {
            string = sb.toString();
        }
        return string;
    }

    public static void clearLog() {
        StringBuilder sb = logBuffer;
        synchronized (sb) {
            sb.setLength(0);
        }
        publishLog();
    }

    public static void sendCommandStatic(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }
        try {
            if (isRunningStatic() && stdin != null) {
                appendLog("> " + command + "\n");
                stdin.write(command + "\n");
                stdin.flush();
                return;
            }
            appendLog("> Server is not running\n");
        } catch (Exception e) {
            appendLog("Command error: " + e.getMessage() + "\n");
        }
    }

    public static void log(String text) {
        appendLog(text);
    }

    private static void appendLog(String text) {
        StringBuilder sb = logBuffer;
        synchronized (sb) {
            sb.append(cleanLog(text));
            if (sb.length() > 120000) {
                sb.delete(0, sb.length() - 120000);
            }
        }
        requestPublishLog();
    }

    private static void requestPublishLog() {
        long now = System.currentTimeMillis();
        if (now - lastPublishMs > 280) {
            lastPublishMs = now;
            publishLog();
        } else if (!publishScheduled) {
            publishScheduled = true;
            new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.AltayServerService$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    AltayServerService.lambda$requestPublishLog$0();
                }
            }, "altay-log-publish").start();
        }
    }

    static /* synthetic */ void lambda$requestPublishLog$0() {
        try {
            Thread.sleep(320L);
        } catch (InterruptedException e) {
        }
        publishScheduled = false;
        lastPublishMs = System.currentTimeMillis();
        publishLog();
    }

    private static void publishLog() {
        String text = getLogText();
        for (ServerListener listener : listeners) {
            listener.onLogChanged(text);
        }
    }

    private static void publishState(boolean running) {
        for (ServerListener listener : listeners) {
            listener.onStateChanged(running);
        }
    }

    private static String cleanLog(String value) {
        return value == null ? "" : value.replace("\u0007", "");
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification("Altay Server", "Ready"));
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_STOP.equals(action) || ACTION_STOP_NOTIFICATION.equals(action)) {
            stopServer(false);
            if (ACTION_STOP_NOTIFICATION.equals(action)) {
                stopSelf();
                return 1;
            }
            return 1;
        }
        if (ACTION_RESTART.equals(action)) {
            new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.AltayServerService$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    AltayServerService.this.m61x4555c4aa();
                }
            }, "altay-restart").start();
            return 1;
        }
        if (ACTION_START.equals(action)) {
            startServer();
            return 1;
        }
        return 1;
    }

    /* JADX INFO: renamed from: lambda$onStartCommand$1$com-vizysolutions-pmmpmobile-AltayServerService, reason: not valid java name */
    public /* synthetic */ void m61x4555c4aa() {
        stopServer(false);
        try {
            Thread.sleep(1200L);
        } catch (InterruptedException e) {
        }
        startServer();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        stopServer(true);
        super.onDestroy();
    }

    private synchronized void startServer() {
        if (!isRunningStatic() && !starting) {
            starting = true;
            new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.AltayServerService$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    AltayServerService.this.m62xa5775686();
                }
            }, "altay-server-thread").start();
            return;
        }
        appendLog("> Server already running\n");
    }

    /* JADX INFO: renamed from: lambda$startServer$2$com-vizysolutions-pmmpmobile-AltayServerService, reason: not valid java name */
    public /* synthetic */ void m62xa5775686() {
        try {
            try {
                File serverDir = getServerDir();
                File phar = new File(serverDir, "Altay.phar");
                File phpIni = new File(serverDir, "php.ini");
                File tmp = new File(serverDir, "tmp");
                File plugins = new File(serverDir, "plugins");
                File worlds = new File(serverDir, "worlds");
                tmp.mkdirs();
                plugins.mkdirs();
                worlds.mkdirs();
                createDefaultPhpIni(phpIni);
                File serverProperties = new File(serverDir, "server.properties");
                createDefaultServerProperties(serverProperties);
                ensureAndroidMuslDnsFix();
                SharedPreferences sp = getSharedPreferences(Lang.PREF, 0);
                boolean xboxAuthEnabled = readBooleanProperty(serverProperties, "xbox-auth", true);
                if (xboxAuthEnabled) {
                    appendLog("Authentication mode: Xbox Live online (xbox-auth=true)\n");
                } else {
                    appendLog("Authentication mode: offline/local (xbox-auth=false)\n");
                }
                String phpPath = sp.getString(PREF_PHP_PATH, null);
                File plugins2 = phpPath == null ? null : new File(phpPath);
                if (!DownloadUtils.isPhpExecutableCandidate(plugins2)) {
                    if (plugins2 != null && plugins2.exists()) {
                        appendLog("Stored PHP path is invalid, searching runtime again...\n");
                    }
                    File php = getFilesDir();
                    plugins2 = DownloadUtils.findPhpExecutable(new File(php, "php-runtime"));
                    if (plugins2 != null) {
                        getSharedPreferences(Lang.PREF, 0).edit().putString(PREF_PHP_PATH, plugins2.getAbsolutePath()).apply();
                    }
                }
                if (!DownloadUtils.isPhpExecutableCandidate(plugins2)) {
                    appendLog("PHP runtime not installed or invalid. Tap Install / Update first.\n");
                    publishState(false);
                    starting = false;
                    process = null;
                    stdin = null;
                    releaseWakeLock();
                    LocalHttpTunnelProxy.stopProxy();
                    publishState(false);
                    updateNotification("Altay Server", "Stopped");
                    return;
                }
                plugins2.setExecutable(true, false);
                appendLog("Using PHP executable: " + plugins2.getAbsolutePath() + "\n");
                if (!phar.exists()) {
                    appendLog("Altay.phar not installed. Tap Install / Update first.\n");
                    publishState(false);
                    starting = false;
                    process = null;
                    stdin = null;
                    releaseWakeLock();
                    LocalHttpTunnelProxy.stopProxy();
                    publishState(false);
                    updateNotification("Altay Server", "Stopped");
                    return;
                }
                appendLog("> Starting Altay...\n");
                acquireWakeLock();
                ProcessBuilder builder = new ProcessBuilder(plugins2.getAbsolutePath(), "-c", phpIni.getAbsolutePath(), phar.getAbsolutePath(), "--no-wizard", "--enable-ansi", "--console.title-tick=1");
                builder.directory(serverDir);
                builder.redirectErrorStream(true);
                builder.environment().put("TMPDIR", tmp.getAbsolutePath());
                builder.environment().put("HOME", serverDir.getAbsolutePath());
                if (xboxAuthEnabled) {
                    int proxyPort = LocalHttpTunnelProxy.ensureStarted();
                    if (proxyPort > 0) {
                        String proxy = "http://127.0.0.1:" + proxyPort;
                        builder.environment().put("https_proxy", proxy);
                        builder.environment().put("HTTPS_PROXY", proxy);
                        builder.environment().put("http_proxy", proxy);
                        builder.environment().put("ALL_PROXY", proxy);
                        appendLog("Auth network bridge enabled on " + proxy + "\n");
                    } else {
                        appendLog("Warning: Auth network bridge could not start; Xbox auth may fail on Android.\n");
                    }
                }
                process = builder.start();
                stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
                publishState(true);
                updateNotification("Altay Server", "Running on port " + getServerPort(serverDir));
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    appendLog(line + "\n");
                }
                int exit = process.waitFor();
                appendLog("> Server stopped. Exit code: " + exit + "\n");
                starting = false;
                process = null;
                stdin = null;
                releaseWakeLock();
                LocalHttpTunnelProxy.stopProxy();
                publishState(false);
                updateNotification("Altay Server", "Stopped");
            } catch (Exception e) {
                appendLog("Server error: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
                appendLog("Tip: if the error says Permission denied, Android blocked the PHP executable. Keep targetSdk 28 for this development APK or package PHP as native library.\n");
            }
        } catch (Throwable th) {
            starting = false;
            process = null;
            stdin = null;
            releaseWakeLock();
            LocalHttpTunnelProxy.stopProxy();
            publishState(false);
            updateNotification("Altay Server", "Stopped");
            throw th;
        }
    }

    private synchronized void stopServer(boolean force) {
        Process process2;
        if (!isRunningStatic()) {
            publishState(false);
            return;
        }
        try {
            appendLog("> Stopping server...\n");
            if (!force) {
                sendCommandStatic("stop");
            }
            Thread waiter = new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.AltayServerService$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    AltayServerService.lambda$stopServer$3();
                }
            });
            waiter.start();
            waiter.join(force ? 500L : 6000L);
            if (isRunningStatic() && (process2 = process) != null) {
                process2.destroy();
                if (Build.VERSION.SDK_INT >= 26) {
                    process.destroyForcibly();
                }
            }
        } catch (Exception e) {
            appendLog("Stop error: " + e.getMessage() + "\n");
        }
    }

    static /* synthetic */ void lambda$stopServer$3() {
        try {
            Process process2 = process;
            if (process2 != null) {
                process2.waitFor();
            }
        } catch (InterruptedException e) {
        }
    }

    private void ensureAndroidMuslDnsFix() {
        ArrayList<File> targets = new ArrayList<>();
        targets.add(new File("/sdcard/resolv.conf"));
        try {
            File external = Environment.getExternalStorageDirectory();
            if (external != null) {
                targets.add(new File(external, "resolv.conf"));
            }
        } catch (Exception e) {
        }
        HashSet<String> attempted = new HashSet<>();
        String lastError = null;
        for (File target : targets) {
            if (target != null) {
                String path = target.getAbsolutePath();
                if (attempted.add(path)) {
                    try {
                        File parent = target.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }
                        FileOutputStream fos = new FileOutputStream(target, false);
                        fos.write("nameserver 1.1.1.1\nnameserver 8.8.8.8\nnameserver 9.9.9.9\noptions timeout:2 attempts:3\n".getBytes(StandardCharsets.UTF_8));
                        fos.flush();
                        fos.close();
                        if (target.exists() && target.length() > 0) {
                            appendLog("Android DNS fix applied for PHP/musl: " + path + "\n");
                            appendLog("DNS servers: 1.1.1.1, 8.8.8.8, 9.9.9.9\n");
                            return;
                        }
                    } catch (Exception e2) {
                        lastError = e2.getClass().getSimpleName() + ": " + e2.getMessage();
                    }
                } else {
                    continue;
                }
            }
        }
        try {
            String safe = "nameserver 1.1.1.1\nnameserver 8.8.8.8\nnameserver 9.9.9.9\noptions timeout:2 attempts:3\n".replace("'", "'\\''");
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "printf '%s' '" + safe + "' > /sdcard/resolv.conf"});
            int exit = p.waitFor();
            File f = new File("/sdcard/resolv.conf");
            if (exit == 0 && f.exists() && f.length() > 0) {
                appendLog("Android DNS fix applied for PHP/musl using shell: /sdcard/resolv.conf\n");
                return;
            }
        } catch (Exception e3) {
            lastError = e3.getClass().getSimpleName() + ": " + e3.getMessage();
        }
        appendLog("Warning: could not create /sdcard/resolv.conf automatically.\n");
        if (lastError != null) {
            appendLog("DNS fix error: " + lastError + "\n");
        }
        appendLog("If Xbox authentication still fails, grant file access permission or create /sdcard/resolv.conf manually with nameserver 1.1.1.1 and 8.8.8.8.\n");
    }

    private File getServerDir() {
        File base = getExternalFilesDir(null);
        if (base == null) {
            base = getFilesDir();
        }
        File dir = new File(base, "Altay");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static void createDefaultPhpIni(File file) {
        try {
            if (file.exists()) {
                return;
            }
            FileOutputStream output = new FileOutputStream(file);
            output.write("date.timezone=UTC\nshort_open_tag=0\nphar.readonly=0\nphar.require_hash=1\nigbinary.compact_strings=0\nzend.assertions=-1\nerror_reporting=-1\ndisplay_errors=1\ndisplay_startup_errors=1\n".getBytes(StandardCharsets.UTF_8));
            output.close();
        } catch (Exception e) {
        }
    }

    public static void createDefaultServerProperties(File file) {
        try {
            if (file.exists()) {
                return;
            }
            FileOutputStream output = new FileOutputStream(file);
            output.write("# Altay Server - Vizy Solutions\nserver-name=Altay Server\nserver-ip=0.0.0.0\nserver-port=19132\ngamemode=survival\nmax-players=20\nmotd=Altay Server on Android\nxbox-auth=true\nonline-mode=true\nenable-ipv6=false\nview-distance=8\nwhite-list=off\n".getBytes(StandardCharsets.UTF_8));
            output.close();
        } catch (Exception e) {
        }
    }

    public static void patchServerProperties(File file) {
        patchServerProperties(file, false);
    }

    public static void patchServerProperties(File file, boolean xboxAuthEnabled) {
        if (file == null) {
            return;
        }
        try {
            if (!file.exists()) {
                return;
            }
            FileInputStream input = new FileInputStream(file);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (true) {
                int read = input.read(buffer);
                if (read == -1) {
                    break;
                } else {
                    output.write(buffer, 0, read);
                }
            }
            input.close();
            String text = output.toString("UTF-8");
            String str = "true";
            String text2 = setProperty(text, "xbox-auth", xboxAuthEnabled ? "true" : "false");
            if (!xboxAuthEnabled) {
                str = "false";
            }
            String text3 = setProperty(setProperty(setProperty(text2, "online-mode", str), "enable-ipv6", "false"), "server-ip", "0.0.0.0");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(text3.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (Exception e) {
        }
    }

    private static String setProperty(String text, String key, String value) {
        String[] lines = text.split("\\r?\\n", -1);
        StringBuilder out = new StringBuilder();
        boolean found = false;
        for (String line : lines) {
            if (line.trim().startsWith(key + "=")) {
                out.append(key);
                out.append("=");
                out.append(value);
                out.append("\n");
                found = true;
            } else if (!line.isEmpty()) {
                out.append(line);
                out.append("\n");
            }
        }
        if (!found) {
            out.append(key);
            out.append("=");
            out.append(value);
            out.append("\n");
        }
        return out.toString();
    }

    private static boolean readBooleanProperty(File file, String key, boolean defaultValue) {
        if (file != null) {
            try {
                if (file.exists()) {
                    FileInputStream input = new FileInputStream(file);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    while (true) {
                        int read = input.read(buffer);
                        if (read == -1) {
                            break;
                        }
                        output.write(buffer, 0, read);
                    }
                    input.close();
                    String text = output.toString("UTF-8");
                    for (String line : text.split("\\r?\\n")) {
                        String trimmed = line.trim();
                        if (!trimmed.startsWith("#")) {
                            if (trimmed.startsWith(key + "=")) {
                                String value = trimmed.substring(trimmed.indexOf(61) + 1).trim().toLowerCase();
                                return value.equals("true") || value.equals("on") || value.equals("1") || value.equals("yes");
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
            return defaultValue;
        }
        return defaultValue;
    }

    private String getServerPort(File serverDir) {
        int i;
        try {
            File f = new File(serverDir, "server.properties");
            FileInputStream input = new FileInputStream(f);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (true) {
                int read = input.read(buffer);
                if (read == -1) {
                    break;
                }
                output.write(buffer, 0, read);
            }
            input.close();
            String txt = output.toString("UTF-8");
            for (String line : txt.split("\\r?\\n")) {
                if (line.trim().startsWith("server-port=")) {
                    return line.substring(line.indexOf(61) + 1).trim();
                }
            }
            return "19132";
        } catch (Exception e) {
            return "19132";
        }
    }

    private void acquireWakeLock() {
        try {
            PowerManager.WakeLock wakeLock = this.wakeLock;
            if (wakeLock == null || !wakeLock.isHeld()) {
                PowerManager pm = (PowerManager) getSystemService("power");
                PowerManager.WakeLock wakeLockNewWakeLock = pm.newWakeLock(1, "AltayServer:ServerWakeLock");
                this.wakeLock = wakeLockNewWakeLock;
                wakeLockNewWakeLock.acquire(86400000L);
            }
        } catch (Exception e) {
        }
    }

    private void releaseWakeLock() {
        try {
            PowerManager.WakeLock wakeLock = this.wakeLock;
            if (wakeLock == null || !wakeLock.isHeld()) {
                return;
            }
            this.wakeLock.release();
        } catch (Exception e) {
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Altay Server", 2);
            channel.setDescription("Altay server runtime");
            NotificationManager nm = (NotificationManager) getSystemService("notification");
            nm.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String title, String text) {
        Intent open = new Intent(this, (Class<?>) MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(this, 1, open, pendingFlags());
        Intent stop = new Intent(this, (Class<?>) AltayServerService.class);
        stop.setAction(ACTION_STOP_NOTIFICATION);
        PendingIntent stopPi = PendingIntent.getService(this, 2, stop, pendingFlags());
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        builder.setContentTitle(title).setContentText(text).setSmallIcon(android.R.drawable.stat_sys_upload).setOngoing(isRunningStatic()).setContentIntent(openPi).addAction(android.R.drawable.ic_media_pause, "Stop", stopPi);
        if (Build.VERSION.SDK_INT >= 21) {
            builder.setColor(-15770000);
        }
        return builder.build();
    }

    private void updateNotification(String title, String text) {
        NotificationManager nm = (NotificationManager) getSystemService("notification");
        nm.notify(NOTIFICATION_ID, buildNotification(title, text));
    }

    private int pendingFlags() {
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = 134217728 | 67108864;
            return flags;
        }
        return 134217728;
    }
}
