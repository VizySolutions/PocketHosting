package com.vizysolutions.pmmpmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
public class UpdateChecker {
    private static final String CHANNEL_ID = "pockethosting_app_updates";
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/VizySolutions/PocketHosting/releases/latest";
    private static final int NOTIFICATION_ID = 2201;
    private static final String RELEASES_URL = "https://github.com/VizySolutions/PocketHosting/releases";

    public static void check(final Activity activity, final boolean force, final Lang lang) {
        if (activity == null || lang == null) {
            return;
        }
        Context appContext = activity.getApplicationContext();
        final SharedPreferences sp = appContext.getSharedPreferences(Lang.PREF, 0);
        if (force) {
            Toast.makeText(activity, lang.t("checking_updates"), 0).show();
        }
        new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.UpdateChecker$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                UpdateChecker.lambda$check$2(activity, sp, force, lang);
            }
        }, "pockethosting-update-check").start();
    }

    static /* synthetic */ void lambda$check$2(final Activity activity, final SharedPreferences sp, final boolean force, final Lang lang) {
        try {
            final UpdateInfo info = fetchLatestRelease();
            final String currentVersion = BuildConfig.VERSION_NAME.trim();
            final boolean newer = isNewerVersion(info.version, currentVersion);
            activity.runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.UpdateChecker$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    UpdateChecker.lambda$check$0(activity, newer, sp, force, info, lang, currentVersion);
                }
            });
        } catch (Exception e) {
            activity.runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.UpdateChecker$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    UpdateChecker.lambda$check$1(force, activity, lang, e);
                }
            });
        }
    }

    static /* synthetic */ void lambda$check$0(Activity activity, boolean newer, SharedPreferences sp, boolean force, UpdateInfo info, Lang lang, String currentVersion) {
        if (activity.isFinishing()) {
            return;
        }
        if (newer) {
            showUpdateDialog(activity, lang, info, currentVersion);
            postUpdateNotification(activity, lang, info);
            return;
        }
        if (force) {
            Toast.makeText(activity, lang.t("update_no_update"), 1).show();
        }
    }

    static /* synthetic */ void lambda$check$1(boolean force, Activity activity, Lang lang, Exception e) {
        if (force && !activity.isFinishing()) {
            Toast.makeText(activity, lang.t("update_check_failed") + ": " + e.getMessage(), 1).show();
        }
    }

    private static UpdateInfo fetchLatestRelease() throws Exception {
        String json = DownloadUtils.fetchText(LATEST_RELEASE_API);
        JSONObject object = new JSONObject(json);
        String tag = object.optString("tag_name", "").trim();
        String name = object.optString("name", "").trim();
        String htmlUrl = object.optString("html_url", RELEASES_URL).trim();
        String version = tag.length() > 0 ? tag : name;
        if (version.length() == 0) {
            version = "latest";
        }
        if (htmlUrl.length() == 0) {
            htmlUrl = RELEASES_URL;
        }
        String apkUrl = "";
        JSONArray assets = object.optJSONArray("assets");
        if (assets != null) {
            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.optJSONObject(i);
                if (asset != null) {
                    String assetName = asset.optString("name", "").toLowerCase(Locale.ROOT);
                    String download = asset.optString("browser_download_url", "");
                    if (assetName.endsWith(".apk") && download.length() > 0) {
                        apkUrl = download;
                        break;
                    }
                }
            }
        }
        return new UpdateInfo(version, name, htmlUrl, apkUrl);
    }

    private static void showUpdateDialog(final Activity activity, Lang lang, final UpdateInfo info, String currentVersion) {
        String latestLabel = info.version;
        if (info.name != null && info.name.length() > 0 && !info.name.equals(info.version)) {
            latestLabel = info.version + " - " + info.name;
        }
        String message = lang.t("update_available_message").replace("%1$s", currentVersion).replace("%2$s", latestLabel);
        new AlertDialog.Builder(activity).setTitle(lang.t("update_available_title")).setMessage(message).setPositiveButton(lang.t("update_open_release"), new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.UpdateChecker$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                UpdateChecker.openRelease(activity, info);
            }
        }).setNegativeButton(lang.t("update_later"), (DialogInterface.OnClickListener) null).show();
    }

    private static void postUpdateNotification(Context context, Lang lang, UpdateInfo info) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
            if (nm == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "PocketHosting updates", 3);
                channel.setDescription("PocketHosting app update alerts");
                nm.createNotificationChannel(channel);
            }
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse((info.htmlUrl == null || info.htmlUrl.length() == 0) ? RELEASES_URL : info.htmlUrl));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 35, intent, pendingFlags());
            Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, CHANNEL_ID) : new Notification.Builder(context);
            builder.setContentTitle(lang.t("update_notification_title")).setContentText(lang.t("update_notification_text") + " " + info.version).setSmallIcon(android.R.drawable.stat_sys_download_done).setContentIntent(pendingIntent).setAutoCancel(true);
            if (Build.VERSION.SDK_INT >= 21) {
                builder.setColor(-9920712);
            }
            nm.notify(NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void openRelease(Context context, UpdateInfo info) {
        String url = (info == null || info.htmlUrl == null || info.htmlUrl.length() == 0) ? RELEASES_URL : info.htmlUrl;
        try {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            if (!(context instanceof Activity)) {
                intent.addFlags(268435456);
            }
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    private static int pendingFlags() {
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = 134217728 | 67108864;
            return flags;
        }
        return 134217728;
    }

    private static boolean isNewerVersion(String latest, String current) {
        if (latest == null || latest.trim().isEmpty()) {
            return false;
        }
        if (current == null) {
            current = "";
        }
        String latestClean = cleanVersion(latest);
        String currentClean = cleanVersion(current);
        if (latestClean.equalsIgnoreCase(currentClean)) {
            return false;
        }
        List<Integer> latestParts = numberParts(latestClean);
        List<Integer> currentParts = numberParts(currentClean);
        if (!latestParts.isEmpty() && !currentParts.isEmpty()) {
            int max = Math.max(latestParts.size(), currentParts.size());
            int i = 0;
            while (i < max) {
                int a = i < latestParts.size() ? latestParts.get(i).intValue() : 0;
                int b = i < currentParts.size() ? currentParts.get(i).intValue() : 0;
                if (a > b) {
                    return true;
                }
                if (a < b) {
                    return false;
                }
                i++;
            }
            return false;
        }
        return !latestClean.equalsIgnoreCase(currentClean);
    }

    private static String cleanVersion(String value) {
        if (value == null) {
            return "";
        }
        String value2 = value.trim();
        if (value2.startsWith("v") || value2.startsWith("V")) {
            value2 = value2.substring(1);
        }
        return value2.trim();
    }

    private static List<Integer> numberParts(String value) {
        ArrayList<Integer> out = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+").matcher(value == null ? "" : value);
        while (matcher.find()) {
            try {
                out.add(Integer.valueOf(Integer.parseInt(matcher.group())));
            } catch (Exception e) {
            }
        }
        return out;
    }

    /* JADX INFO: Access modifiers changed from: private */
    static class UpdateInfo {
        final String apkUrl;
        final String htmlUrl;
        final String name;
        final String version;

        UpdateInfo(String version, String name, String htmlUrl, String apkUrl) {
            this.version = version == null ? "latest" : version;
            this.name = name == null ? "" : name;
            this.htmlUrl = htmlUrl == null ? UpdateChecker.RELEASES_URL : htmlUrl;
            this.apkUrl = apkUrl != null ? apkUrl : "";
        }
    }
}
