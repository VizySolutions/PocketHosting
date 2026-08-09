package com.vizysolutions.pmmpmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/* JADX INFO: loaded from: classes2.dex */
public class MainActivity extends Activity {
    private static final String DISCORD_URL = "https://discord.gg/kDxM9Ks5U2";
    private static final String KEY_BATTERY_ASKED = "battery_asked";
    private static final String KEY_DARK = "dark_theme";
    private static final String KEY_DNS_START_PENDING = "dns_start_pending";
    private static final String KEY_INTRO = "intro_seen_v5";
    private static final String KEY_ALTAY_VERSION = "altay_version";
    private static final int LIGHT_CARD = -1;
    private static final String PRIVACY_URL = "https://vizysolutions.cloud/politica-de-privacidade-vizy-solutions.pdf";
    private static final int REQ_DNS_STORAGE = 7302;
    private static final int REQ_UPLOAD_FILES = 7301;
    private static final String TERMS_URL = "https://vizysolutions.cloud/termos-de-servico-vizy-solutions.pdf";
    private EditText commandInput;
    private ScrollView consoleScroll;
    private TextView consoleText;
    private FrameLayout contentFrame;
    private StatsGraphView cpuGraph;
    private TextView cpuValueText;
    private File currentDir;
    private boolean darkMode;
    private Lang lang;
    private LinearLayout navBar;
    private TextView phpStatusText;
    private TextView playersOnlineText;
    private TextView altayStatusText;
    private TextView altayVersionInfoText;
    private ProgressBar progressBar;
    private TextView progressText;
    private StatsGraphView ramGraph;
    private TextView ramValueText;
    private LinearLayout root;
    private File serverDir;
    private TextView serverStatusText;
    private Button startActionButton;
    private TextView toolbarSubTitle;
    private TextView toolbarTitle;
    private static final int PM_GREEN = Color.rgb(104, 159, 56);
    private static final int PM_GREEN_DARK = Color.rgb(56, 112, 2);
    private static final int PM_GREEN_LIGHT = Color.rgb(153, 208, 102);
    private static final int INTRO_ONE = Color.rgb(75, 131, 13);
    private static final int INTRO_TWO = Color.rgb(0, 92, 178);
    private static final int INTRO_THREE = Color.rgb(245, 124, 0);
    private static final int DARK_SURFACE = Color.rgb(27, 32, 35);
    private static final int DARK_CARD = Color.rgb(35, 43, 47);
    private static final int LIGHT_BG = Color.rgb(245, 245, 246);
    private String currentTab = "server";
    private String lastConsoleLog = "";
    private boolean consoleRenderScheduled = false;
    private boolean updateCheckScheduled = false;
    private long lastConsoleRenderMs = 0;
    private int introIndex = 0;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final CpuSampler cpuSampler = new CpuSampler();
    private final DecimalFormat oneDecimal = new DecimalFormat("0.0");
    private final Runnable statsRunnable = new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity.1
        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.updateStatsGraphs();
            MainActivity.this.uiHandler.postDelayed(this, 1400L);
        }
    };
    private final AltayServerService.ServerListener serverListener = new AnonymousClass2();

    /* JADX INFO: renamed from: com.vizysolutions.pmmpmobile.MainActivity$2, reason: invalid class name */
    class AnonymousClass2 implements AltayServerService.ServerListener {
        AnonymousClass2() {
        }

        @Override // com.vizysolutions.pmmpmobile.AltayServerService.ServerListener
        public void onLogChanged(String log) {
            MainActivity.this.lastConsoleLog = log == null ? "" : log;
            if ("console".equals(MainActivity.this.currentTab)) {
                MainActivity.this.runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$2$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        AnonymousClass2.this.m59xc33d38c4();
                    }
                });
            }
        }

        /* JADX INFO: renamed from: lambda$onLogChanged$0$com-vizysolutions-pmmpmobile-MainActivity$2, reason: not valid java name */
        public /* synthetic */ void m59xc33d38c4() {
            MainActivity.this.scheduleConsoleRender();
        }

        /* JADX INFO: renamed from: lambda$onStateChanged$1$com-vizysolutions-pmmpmobile-MainActivity$2, reason: not valid java name */
        public /* synthetic */ void m60xae1e8118() {
            MainActivity.this.updateStatusViews();
        }

        @Override // com.vizysolutions.pmmpmobile.AltayServerService.ServerListener
        public void onStateChanged(boolean running) {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$2$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    AnonymousClass2.this.m60xae1e8118();
                }
            });
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.lang = new Lang(this);
        this.darkMode = getSharedPreferences(Lang.PREF, 0).getBoolean(KEY_DARK, false);
        File file = new File(getSafeExternalFilesDir(), "Altay");
        this.serverDir = file;
        this.currentDir = file;
        ensureServerFolders();
        AltayServerService.addListener(this.serverListener);
        this.lastConsoleLog = AltayServerService.getLogText();
        if (!getSharedPreferences(Lang.PREF, 0).getBoolean(KEY_INTRO, false)) {
            showIntroSlides();
        } else {
            openMainInterface();
        }
        this.uiHandler.post(this.statsRunnable);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.uiHandler.removeCallbacks(this.statsRunnable);
        AltayServerService.removeListener(this.serverListener);
        super.onDestroy();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(Lang.PREF, 0);
        if (sp.getBoolean(KEY_DNS_START_PENDING, false) && tryWriteDnsFixFromActivity(true)) {
            sp.edit().putBoolean(KEY_DNS_START_PENDING, false).apply();
            Toast.makeText(this, this.lang.t("dns_fix_applied"), 1).show();
            startServer();
        }
    }

    private void openMainInterface() {
        buildShell();
        showServerTab();
        checkForAppUpdates(false);
    }

    private File getSafeExternalFilesDir() {
        File base = getExternalFilesDir(null);
        return base == null ? getFilesDir() : base;
    }

    private void ensureServerFolders() {
        this.serverDir.mkdirs();
        new File(this.serverDir, "plugins").mkdirs();
        new File(this.serverDir, "worlds").mkdirs();
        new File(this.serverDir, "tmp").mkdirs();
        AltayServerService.createDefaultPhpIni(new File(this.serverDir, "php.ini"));
        File props = new File(this.serverDir, "server.properties");
        AltayServerService.createDefaultServerProperties(props);
    }

    private void showIntroSlides() {
        LinearLayout linearLayout = new LinearLayout(this);
        this.root = linearLayout;
        linearLayout.setOrientation(1);
        setContentView(this.root);
        renderIntroSlide(false);
    }

    private void renderIntroSlide(boolean animated) {
        Lang lang;
        String str;
        this.root.removeAllViews();
        IntroPage page = introPage(this.introIndex);
        this.root.setBackgroundColor(page.color);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(1);
        content.setGravity(17);
        content.setPadding(dp(26), dp(28), dp(26), dp(24));
        this.root.addView(content, new LinearLayout.LayoutParams(LIGHT_CARD, 0, 1.0f));
        ImageView icon = new ImageView(this);
        icon.setImageResource(page.iconRes);
        icon.setColorFilter(LIGHT_CARD);
        icon.setAlpha(0.95f);
        content.addView(icon, new LinearLayout.LayoutParams(dp(92), dp(92)));
        TextView title = new TextView(this);
        title.setText(page.title);
        title.setTextColor(LIGHT_CARD);
        title.setTextSize(30.0f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(17);
        title.setPadding(0, dp(24), 0, dp(10));
        content.addView(title, new LinearLayout.LayoutParams(LIGHT_CARD, -2));
        TextView desc = new TextView(this);
        desc.setText(page.description);
        desc.setTextColor(Color.argb(232, 255, 255, 255));
        desc.setTextSize(16.0f);
        desc.setGravity(17);
        desc.setLineSpacing(3.0f, 1.08f);
        content.addView(desc, new LinearLayout.LayoutParams(LIGHT_CARD, -2));
        LinearLayout dots = new LinearLayout(this);
        dots.setGravity(17);
        dots.setPadding(0, 0, 0, dp(14));
        int i = 0;
        while (true) {
            if (i >= 3) {
                break;
            }
            TextView dot = new TextView(this);
            dot.setText(i == this.introIndex ? "●" : "○");
            dot.setTextSize(18.0f);
            dot.setTextColor(LIGHT_CARD);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-2, -2);
            dlp.setMargins(dp(4), 0, dp(4), 0);
            dots.addView(dot, dlp);
            i++;
        }
        this.root.addView(dots, new LinearLayout.LayoutParams(LIGHT_CARD, -2));
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(0);
        controls.setPadding(dp(18), 0, dp(18), dp(20));
        this.root.addView(controls, new LinearLayout.LayoutParams(LIGHT_CARD, -2));
        Button skip = introButton(this.introIndex == 2 ? "" : this.lang.t("skip"), true);
        skip.setVisibility(this.introIndex != 2 ? 0 : 4);
        skip.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m38x4861c402(view);
            }
        });
        controls.addView(skip, new LinearLayout.LayoutParams(0, dp(52), 1.0f));
        if (this.introIndex == 2) {
            lang = this.lang;
            str = "start_now";
        } else {
            lang = this.lang;
            str = "next";
        }
        Button next = introButton(lang.t(str), false);
        next.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m39x2da332c3(view);
            }
        });
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, dp(52), 1.0f);
        nlp.setMargins(dp(10), 0, 0, 0);
        controls.addView(next, nlp);
        if (animated) {
            content.setAlpha(0.0f);
            content.setTranslationX(dp(24));
            content.animate().alpha(1.0f).translationX(0.0f).setDuration(260L).start();
        }
    }

    /* JADX INFO: renamed from: lambda$renderIntroSlide$0$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m38x4861c402(View v) {
        finishIntro();
    }

    /* JADX INFO: renamed from: lambda$renderIntroSlide$1$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m39x2da332c3(View v) {
        int i = this.introIndex;
        if (i < 2) {
            this.introIndex = i + 1;
            renderIntroSlide(true);
        } else {
            finishIntro();
        }
    }

    private Button introButton(String text, boolean transparent) {
        Button b = new Button(this);
        b.setText(text);
        int iArgb = 0;
        b.setAllCaps(false);
        b.setTextSize(15.0f);
        b.setTextColor(LIGHT_CARD);
        if (!transparent) {
            iArgb = Color.argb(48, 255, 255, 255);
        }
        b.setBackground(makeRound(iArgb, dp(3), Color.argb(150, 255, 255, 255)));
        return b;
    }

    private IntroPage introPage(int index) {
        if (index == 0) {
            return new IntroPage(R.drawable.ci_server_w, this.lang.t("intro_1_title"), this.lang.t("intro_1_desc"), INTRO_ONE);
        }
        return index == 1 ? new IntroPage(R.drawable.ci_download, this.lang.t("intro_2_title"), this.lang.t("intro_2_desc"), INTRO_TWO) : new IntroPage(R.drawable.ci_code_w, this.lang.t("intro_3_title"), this.lang.t("intro_3_desc"), INTRO_THREE);
    }

    private void finishIntro() {
        getSharedPreferences(Lang.PREF, 0).edit().putBoolean(KEY_INTRO, true).apply();
        openMainInterface();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void buildShell() {
        LinearLayout linearLayout = new LinearLayout(this);
        this.root = linearLayout;
        linearLayout.setOrientation(1);
        this.root.setBackgroundColor(bgColor());
        setContentView(this.root);
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(1);
        toolbar.setGravity(16);
        toolbar.setPadding(dp(18), dp(12), dp(18), dp(12));
        GradientDrawable toolbarBg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{primaryDark(), primary()});
        toolbar.setBackground(toolbarBg);
        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(dp(4));
        }
        this.root.addView(toolbar, new LinearLayout.LayoutParams(LIGHT_CARD, -2));
        TextView textView = new TextView(this);
        this.toolbarTitle = textView;
        textView.setText(this.lang.t("app_title"));
        this.toolbarTitle.setTextColor(LIGHT_CARD);
        this.toolbarTitle.setTextSize(20.0f);
        this.toolbarTitle.setTypeface(Typeface.DEFAULT_BOLD);
        toolbar.addView(this.toolbarTitle);
        TextView textView2 = new TextView(this);
        this.toolbarSubTitle = textView2;
        textView2.setText(this.lang.t("app_subtitle"));
        this.toolbarSubTitle.setTextColor(Color.argb(225, 255, 255, 255));
        this.toolbarSubTitle.setTextSize(12.0f);
        toolbar.addView(this.toolbarSubTitle);
        FrameLayout frameLayout = new FrameLayout(this);
        this.contentFrame = frameLayout;
        this.root.addView(frameLayout, new LinearLayout.LayoutParams(LIGHT_CARD, 0, 1.0f));
        LinearLayout linearLayout2 = new LinearLayout(this);
        this.navBar = linearLayout2;
        linearLayout2.setOrientation(0);
        this.navBar.setPadding(dp(2), dp(4), dp(2), dp(4));
        this.navBar.setBackgroundColor(navColor());
        if (Build.VERSION.SDK_INT >= 21) {
            this.navBar.setElevation(dp(8));
        }
        this.root.addView(this.navBar, new LinearLayout.LayoutParams(LIGHT_CARD, dp(64)));
        rebuildNavBar();
    }

    private void rebuildNavBar() {
        LinearLayout linearLayout = this.navBar;
        if (linearLayout == null) {
            return;
        }
        linearLayout.removeAllViews();
        addNavButton("server", R.drawable.ci_server, this.lang.t("tab_server"));
        addNavButton("console", R.drawable.ci_console, this.lang.t("tab_console"));
        addNavButton("files", R.drawable.ci_files, this.lang.t("tab_files"));
        addNavButton("settings", R.drawable.ci_settings, this.lang.t("tab_settings"));
    }

    private void addNavButton(final String id, int iconRes, String text) {
        boolean selected = id.equals(this.currentTab);
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(1);
        item.setGravity(17);
        item.setClickable(true);
        item.setPadding(dp(2), dp(3), dp(2), dp(3));
        item.setBackground(makeRound(selected ? selectedNavColor() : 0, dp(20), 0));
        if (selected && Build.VERSION.SDK_INT >= 21) {
            item.setElevation(dp(2));
        }
        ImageView iconView = new ImageView(this);
        iconView.setImageResource(iconRes);
        iconView.setColorFilter(selected ? LIGHT_CARD : subTextColor());
        item.addView(iconView, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(11.0f);
        label.setGravity(17);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setTextColor(selected ? LIGHT_CARD : textColor());
        item.addView(label);
        item.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda18
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m11lambda$addNavButton$3$comvizysolutionspmmpmobileMainActivity(id, view);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LIGHT_CARD, 1.0f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        this.navBar.addView(item, lp);
    }

    /* JADX INFO: renamed from: lambda$addNavButton$3$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m11lambda$addNavButton$3$comvizysolutionspmmpmobileMainActivity(String id, final View v) {
        v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70L).withEndAction(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda27
            @Override // java.lang.Runnable
            public final void run() {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(90L).start();
            }
        }).start();
        if (!"server".equals(id)) {
            if (!"console".equals(id)) {
                if (!"files".equals(id)) {
                    if ("settings".equals(id)) {
                        showSettingsTab();
                        return;
                    }
                    return;
                }
                showFilesTab();
                return;
            }
            showConsoleTab();
            return;
        }
        showServerTab();
    }

    private LinearLayout newPage() {
        this.contentFrame.removeAllViews();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(1);
        page.setPadding(dp(14), dp(14), dp(14), dp(18));
        scroll.addView(page, new FrameLayout.LayoutParams(LIGHT_CARD, -2));
        this.contentFrame.addView(scroll, new FrameLayout.LayoutParams(LIGHT_CARD, LIGHT_CARD));
        page.setAlpha(0.0f);
        page.setTranslationY(dp(12));
        page.animate().alpha(1.0f).translationY(0.0f).setDuration(180L).start();
        return page;
    }

    private void showServerTab() {
        this.currentTab = "server";
        rebuildNavBar();
        LinearLayout linearLayoutNewPage = newPage();
        LinearLayout actionsCard = card();
        actionsCard.addView(sectionTitle(this.lang.t("server_section")));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(0);
        this.startActionButton = outlineButton(this.lang.t("start"));
        Button stop = outlineButton(this.lang.t("stop"));
        row.addView(this.startActionButton, rowButtonLp());
        row.addView(stop, rowButtonLp());
        actionsCard.addView(row);
        this.startActionButton.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m48lambda$showServerTab$4$comvizysolutionspmmpmobileMainActivity(view);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m49lambda$showServerTab$5$comvizysolutionspmmpmobileMainActivity(view);
            }
        });
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(0);
        Button restart = darkButton(this.lang.t("restart"));
        Button install = primaryButton(this.lang.t("install_update"));
        row2.addView(restart, rowButtonLp());
        row2.addView(install, rowButtonLp());
        actionsCard.addView(row2);
        restart.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m50lambda$showServerTab$6$comvizysolutionspmmpmobileMainActivity(view);
            }
        });
        install.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m51lambda$showServerTab$7$comvizysolutionspmmpmobileMainActivity(view);
            }
        });
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        this.progressBar = progressBar;
        progressBar.setMax(100);
        this.progressBar.setVisibility(8);
        actionsCard.addView(this.progressBar, matchWrap());
        TextView textViewSmallText = smallText("");
        this.progressText = textViewSmallText;
        actionsCard.addView(textViewSmallText);
        linearLayoutNewPage.addView(actionsCard);
        LinearLayout statusCard = card();
        statusCard.addView(sectionTitle(this.lang.t("information")));
        this.serverStatusText = labelValue(statusCard, this.lang.t("status"), "-");
        labelValue(statusCard, this.lang.t("ip_address"), getLocalIpAddress());
        labelValue(statusCard, this.lang.t("port"), readServerPort());
        this.playersOnlineText = labelValue(statusCard, this.lang.t("players_online"), parsePlayersOnline());
        this.altayVersionInfoText = labelValue(statusCard, this.lang.t(KEY_ALTAY_VERSION), getInstalledAltayVersion());
        linearLayoutNewPage.addView(statusCard);
        LinearLayout statsCard = card();
        statsCard.addView(sectionTitle(this.lang.t("usage_stats")));
        TextView textViewSmallText2 = smallText(this.lang.t("cpu") + ": --");
        this.cpuValueText = textViewSmallText2;
        statsCard.addView(textViewSmallText2);
        StatsGraphView statsGraphView = new StatsGraphView(this, primary(), this.darkMode);
        this.cpuGraph = statsGraphView;
        statsCard.addView(statsGraphView, new LinearLayout.LayoutParams(LIGHT_CARD, dp(120)));
        TextView textViewSmallText3 = smallText(this.lang.t("ram") + ": --");
        this.ramValueText = textViewSmallText3;
        textViewSmallText3.setPadding(0, dp(12), 0, dp(2));
        statsCard.addView(this.ramValueText);
        StatsGraphView statsGraphView2 = new StatsGraphView(this, INTRO_TWO, this.darkMode);
        this.ramGraph = statsGraphView2;
        statsCard.addView(statsGraphView2, new LinearLayout.LayoutParams(LIGHT_CARD, dp(120)));
        linearLayoutNewPage.addView(statsCard);
        updateStatusViews();
        updateStatsGraphs();
    }

    /* JADX INFO: renamed from: lambda$showServerTab$4$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m48lambda$showServerTab$4$comvizysolutionspmmpmobileMainActivity(View v) {
        startServer();
    }

    /* JADX INFO: renamed from: lambda$showServerTab$5$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m49lambda$showServerTab$5$comvizysolutionspmmpmobileMainActivity(View v) {
        stopServer();
    }

    /* JADX INFO: renamed from: lambda$showServerTab$6$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m50lambda$showServerTab$6$comvizysolutionspmmpmobileMainActivity(View v) {
        restartServer();
    }

    /* JADX INFO: renamed from: lambda$showServerTab$7$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m51lambda$showServerTab$7$comvizysolutionspmmpmobileMainActivity(View v) {
        installOrUpdateRuntime();
    }

    private void showConsoleTab() {
        this.currentTab = "console";
        rebuildNavBar();
        this.contentFrame.removeAllViews();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(10), dp(10), dp(10), dp(10));
        linearLayout.setBackgroundColor(bgColor());
        this.contentFrame.addView(linearLayout, new FrameLayout.LayoutParams(LIGHT_CARD, LIGHT_CARD));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(dp(12), dp(10), dp(12), dp(10));
        linearLayout2.setBackground(makeRound(LIGHT_CARD, dp(3), borderColor()));
        if (Build.VERSION.SDK_INT >= 21) {
            linearLayout2.setElevation(dp(2));
        }
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(LIGHT_CARD, 0, 1.0f));
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(0);
        titleRow.setGravity(16);
        ImageView termIcon = new ImageView(this);
        termIcon.setImageResource(R.drawable.ci_console);
        termIcon.setColorFilter(primaryDark());
        titleRow.addView(termIcon, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView title = sectionTitle(this.lang.t("tab_console"));
        title.setPadding(dp(8), 0, 0, dp(8));
        titleRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout2.addView(titleRow);
        ScrollView scrollView = new ScrollView(this);
        this.consoleScroll = scrollView;
        scrollView.setFillViewport(true);
        this.consoleScroll.setBackgroundColor(LIGHT_CARD);
        this.consoleScroll.setPadding(0, 0, 0, 0);
        this.consoleScroll.setOnTouchListener(new View.OnTouchListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda20
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return MainActivity.lambda$showConsoleTab$8(view, motionEvent);
            }
        });
        TextView textView = new TextView(this);
        this.consoleText = textView;
        textView.setText(colorizeLog(this.lastConsoleLog));
        this.consoleText.setTextSize(12.0f);
        this.consoleText.setTextIsSelectable(false);
        this.consoleText.setTypeface(Typeface.MONOSPACE);
        this.consoleText.setPadding(dp(6), dp(8), dp(6), dp(8));
        this.consoleText.setMinLines(18);
        this.consoleText.setGravity(8388659);
        this.consoleText.setTextColor(Color.rgb(45, 55, 60));
        this.consoleScroll.addView(this.consoleText, new FrameLayout.LayoutParams(LIGHT_CARD, -2));
        linearLayout2.addView(this.consoleScroll, new LinearLayout.LayoutParams(LIGHT_CARD, 0, 1.0f));
        LinearLayout inputBar = new LinearLayout(this);
        inputBar.setOrientation(0);
        inputBar.setGravity(16);
        inputBar.setPadding(0, dp(8), 0, 0);
        linearLayout.addView(inputBar, new LinearLayout.LayoutParams(LIGHT_CARD, dp(68)));
        EditText editText = new EditText(this);
        this.commandInput = editText;
        editText.setSingleLine(true);
        this.commandInput.setTextColor(textColor());
        this.commandInput.setHintTextColor(subTextColor());
        this.commandInput.setHint(this.lang.t("command_hint"));
        this.commandInput.setImeOptions(4);
        this.commandInput.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda23
            @Override // android.widget.TextView.OnEditorActionListener
            public final boolean onEditorAction(TextView textView2, int i, KeyEvent keyEvent) {
                return MainActivity.this.m46xca8f5ccb(textView2, i, keyEvent);
            }
        });
        inputBar.addView(this.commandInput, new LinearLayout.LayoutParams(0, dp(56), 1.0f));
        Button send = primaryButton(this.lang.t("send"));
        send.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m44x3749eec9(view);
            }
        });
        LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(dp(94), dp(56));
        sendLp.setMargins(dp(8), 0, 0, 0);
        inputBar.addView(send, sendLp);
        this.uiHandler.postDelayed(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda36
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m45x1c8b5d8a();
            }
        }, 100L);
    }

    static /* synthetic */ boolean lambda$showConsoleTab$8(View v, MotionEvent event) {
        if (v.getParent() != null) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
        }
        if ((event.getAction() == 1 || event.getAction() == 3) && v.getParent() != null) {
            v.getParent().requestDisallowInterceptTouchEvent(false);
        }
        return false;
    }

    /* JADX INFO: renamed from: lambda$showConsoleTab$9$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ boolean m46xca8f5ccb(TextView v, int actionId, KeyEvent event) {
        if (actionId == 4) {
            sendCommandFromInput();
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: lambda$showConsoleTab$10$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m44x3749eec9(View v) {
        sendCommandFromInput();
    }

    /* JADX INFO: renamed from: lambda$showConsoleTab$11$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m45x1c8b5d8a() {
        ScrollView scrollView = this.consoleScroll;
        if (scrollView != null) {
            scrollView.fullScroll(130);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleConsoleRender() {
        if (this.consoleText == null || !"console".equals(this.currentTab)) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - this.lastConsoleRenderMs > 300) {
            this.lastConsoleRenderMs = now;
            updateConsoleView(this.lastConsoleLog);
        } else if (!this.consoleRenderScheduled) {
            this.consoleRenderScheduled = true;
            this.uiHandler.postDelayed(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda35
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m43x515d2aa();
                }
            }, 320L);
        }
    }

    /* JADX INFO: renamed from: lambda$scheduleConsoleRender$12$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m43x515d2aa() {
        this.consoleRenderScheduled = false;
        this.lastConsoleRenderMs = SystemClock.elapsedRealtime();
        updateConsoleView(this.lastConsoleLog);
    }

    private void updateConsoleView(String log) {
        ScrollView scrollView;
        if (this.consoleText == null) {
            return;
        }
        boolean stickToBottom = true;
        ScrollView scrollView2 = this.consoleScroll;
        if (scrollView2 != null && scrollView2.getChildCount() > 0) {
            View child = this.consoleScroll.getChildAt(0);
            stickToBottom = child.getBottom() <= (this.consoleScroll.getScrollY() + this.consoleScroll.getHeight()) + dp(100);
        }
        this.consoleText.setText(colorizeLog(log));
        if (stickToBottom && (scrollView = this.consoleScroll) != null) {
            scrollView.post(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda37
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m58xda41eb6e();
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$updateConsoleView$13$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m58xda41eb6e() {
        this.consoleScroll.fullScroll(130);
    }

    private void sendCommandFromInput() {
        EditText editText = this.commandInput;
        if (editText == null) {
            return;
        }
        String cmd = editText.getText().toString().trim();
        if (!cmd.isEmpty()) {
            AltayServerService.sendCommandStatic(cmd);
            this.commandInput.setText("");
        }
    }

    private void showFilesTab() {
        this.currentTab = "files";
        rebuildNavBar();
        File file = this.currentDir;
        if (file == null || !isInsideServerDir(file)) {
            this.currentDir = this.serverDir;
        }
        LinearLayout page = newPage();
        renderFilesPage(page);
    }

    private void renderFilesPage(LinearLayout linearLayout) {
        linearLayout.removeAllViews();
        LinearLayout card = card();
        card.addView(sectionTitle(this.lang.t("file_manager")));
        card.addView(smallText(shortPath(this.currentDir)));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(0);
        Button up = darkButton(this.lang.t("back"));
        up.setEnabled(!sameFile(this.currentDir, this.serverDir));
        up.setAlpha(up.isEnabled() ? 1.0f : 0.45f);
        up.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda49
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m34xae2243f2(view);
            }
        });
        Button newFile = darkButton(this.lang.t("new_file"));
        newFile.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda50
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m35x9363b2b3(view);
            }
        });
        Button newFolder = darkButton(this.lang.t("new_folder"));
        newFolder.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m36x78a52174(view);
            }
        });
        actions.addView(up, rowButtonLp());
        actions.addView(newFile, rowButtonLp());
        actions.addView(newFolder, rowButtonLp());
        card.addView(actions);
        Button upload = primaryButton(this.lang.t("upload_files"));
        upload.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m37x5de69035(view);
            }
        });
        card.addView(upload, matchWrap());
        File[] files = this.currentDir.listFiles();
        List<File> list = new ArrayList<>();
        if (files != null) {
            Collections.addAll(list, files);
        }
        Collections.sort(list, new Comparator() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda45
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return MainActivity.lambda$renderFilesPage$18((File) obj, (File) obj2);
            }
        });
        if (list.isEmpty()) {
            card.addView(paragraph(this.lang.t("empty_folder")));
        } else {
            for (File f : list) {
                card.addView(fileRow(f), matchWrap());
            }
        }
        linearLayout.addView(card);
    }

    /* JADX INFO: renamed from: lambda$renderFilesPage$14$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m34xae2243f2(View v) {
        File parentFile = this.currentDir.getParentFile();
        this.currentDir = parentFile;
        if (!isInsideServerDir(parentFile)) {
            this.currentDir = this.serverDir;
        }
        showFilesTab();
    }

    /* JADX INFO: renamed from: lambda$renderFilesPage$15$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m35x9363b2b3(View v) {
        createEntry(false);
    }

    /* JADX INFO: renamed from: lambda$renderFilesPage$16$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m36x78a52174(View v) {
        createEntry(true);
    }

    /* JADX INFO: renamed from: lambda$renderFilesPage$17$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m37x5de69035(View v) {
        openFilePicker();
    }

    static /* synthetic */ int lambda$renderFilesPage$18(File a, File b) {
        if (a.isDirectory() && !b.isDirectory()) {
            return LIGHT_CARD;
        }
        if (a.isDirectory() || !b.isDirectory()) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
        return 1;
    }

    private View fileRow(final File f) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.setPadding(dp(10), dp(8), dp(8), dp(8));
        linearLayout.setClickable(true);
        linearLayout.setBackground(makeRound(this.darkMode ? Color.rgb(45, 55, 60) : Color.rgb(250, 250, 250), dp(6), this.darkMode ? Color.rgb(60, 70, 75) : Color.rgb(224, 224, 224)));
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconForFile(f));
        icon.setColorFilter(subTextColor());
        linearLayout.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(1);
        TextView name = new TextView(this);
        name.setText(f.getName());
        name.setTextColor(textColor());
        name.setTextSize(14.0f);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        texts.addView(name);
        TextView meta = smallText(fileMeta(f));
        texts.addView(meta);
        linearLayout.addView(texts, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView more = new TextView(this);
        more.setText("⋮");
        more.setGravity(17);
        more.setTextColor(subTextColor());
        more.setTextSize(22.0f);
        linearLayout.addView(more, new LinearLayout.LayoutParams(dp(34), LIGHT_CARD));
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda16
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m17lambda$fileRow$19$comvizysolutionspmmpmobileMainActivity(f, view);
            }
        });
        linearLayout.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda19
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return MainActivity.this.m18lambda$fileRow$20$comvizysolutionspmmpmobileMainActivity(f, view);
            }
        });
        more.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda17
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m19lambda$fileRow$21$comvizysolutionspmmpmobileMainActivity(f, view);
            }
        });
        return linearLayout;
    }

    /* JADX INFO: renamed from: lambda$fileRow$19$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m17lambda$fileRow$19$comvizysolutionspmmpmobileMainActivity(File f, View v) {
        if (f.isDirectory()) {
            this.currentDir = f;
            showFilesTab();
        } else {
            openTextFile(f);
        }
    }

    /* JADX INFO: renamed from: lambda$fileRow$20$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ boolean m18lambda$fileRow$20$comvizysolutionspmmpmobileMainActivity(File f, View v) {
        showFileOptions(f);
        return true;
    }

    /* JADX INFO: renamed from: lambda$fileRow$21$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m19lambda$fileRow$21$comvizysolutionspmmpmobileMainActivity(File f, View v) {
        showFileOptions(f);
    }

    private void showFileOptions(final File f) {
        Lang lang;
        String str;
        final List<String> options = new ArrayList<>();
        if (f.isDirectory()) {
            lang = this.lang;
            str = "open";
        } else {
            lang = this.lang;
            str = "edit";
        }
        options.add(lang.t(str));
        if (!f.isDirectory() && isArchive(f)) {
            options.add(this.lang.t("extract"));
        }
        options.add(this.lang.t("rename"));
        options.add(this.lang.t("delete"));
        new AlertDialog.Builder(this).setTitle(f.getName()).setItems((CharSequence[]) options.toArray(new String[0]), new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda48
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.m47x66b9e05a(options, f, dialogInterface, i);
            }
        }).show();
    }

    /* JADX INFO: renamed from: lambda$showFileOptions$22$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m47x66b9e05a(List options, File f, DialogInterface dialog, int which) {
        String selected = (String) options.get(which);
        if (selected.equals(this.lang.t("open")) || selected.equals(this.lang.t("edit"))) {
            if (f.isDirectory()) {
                this.currentDir = f;
                showFilesTab();
                return;
            } else {
                openTextFile(f);
                return;
            }
        }
        if (selected.equals(this.lang.t("extract"))) {
            extractArchive(f);
        } else if (selected.equals(this.lang.t("rename"))) {
            renameEntry(f);
        } else if (selected.equals(this.lang.t("delete"))) {
            confirmDelete(f);
        }
    }

    private void renameEntry(final File file) {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(file.getName());
        input.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this).setTitle(this.lang.t("rename")).setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda33
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.m33lambda$renameEntry$23$comvizysolutionspmmpmobileMainActivity(input, file, dialogInterface, i);
            }
        }).setNegativeButton(this.lang.t("cancel"), (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$renameEntry$23$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m33lambda$renameEntry$23$comvizysolutionspmmpmobileMainActivity(EditText input, File file, DialogInterface dialog, int which) {
        String name = input.getText().toString().trim();
        if (name.isEmpty() || name.contains("/") || name.contains("..")) {
            return;
        }
        File dest = new File(file.getParentFile(), name);
        if (file.renameTo(dest)) {
            showFilesTab();
        } else {
            Toast.makeText(this, this.lang.t("rename_failed"), 1).show();
        }
    }

    private void confirmDelete(final File file) {
        new AlertDialog.Builder(this).setTitle(this.lang.t("delete")).setMessage(this.lang.t("delete_confirm") + " " + file.getName() + "?").setPositiveButton(this.lang.t("delete"), new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda46
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.m12x1695fc96(file, dialogInterface, i);
            }
        }).setNegativeButton(this.lang.t("cancel"), (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$confirmDelete$24$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m12x1695fc96(File file, DialogInterface d, int w) {
        deleteRecursive(file);
        showFilesTab();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showSettingsTab() {
        this.currentTab = "settings";
        rebuildNavBar();
        LinearLayout linearLayoutNewPage = newPage();
        LinearLayout linearLayoutCard = card();
        linearLayoutCard.addView(sectionTitle(this.lang.t("tab_settings")));
        linearLayoutCard.addView(label(this.lang.t(Lang.KEY_LANGUAGE)));
        Spinner spinner = new Spinner(this);
        String[] labels = {this.lang.t("english"), this.lang.t("portuguese"), this.lang.t("spanish")};
        final String[] codes = {Lang.EN, Lang.PT_BR, Lang.ES};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        spinner.setAdapter((SpinnerAdapter) adapter);
        int selectedLanguage = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(this.lang.getCode())) {
                selectedLanguage = i;
                break;
            }
        }
        spinner.setSelection(selectedLanguage);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity.3
            private boolean first = true;

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (this.first) {
                    this.first = false;
                    return;
                }
                MainActivity.this.lang.setLanguage(codes[position]);
                MainActivity.this.buildShell();
                MainActivity.this.showSettingsTab();
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        linearLayoutCard.addView(spinner, matchWrap());
        LinearLayout themeRow = new LinearLayout(this);
        themeRow.setOrientation(0);
        themeRow.setGravity(16);
        themeRow.setPadding(0, dp(12), 0, dp(6));
        ImageView themeIcon = new ImageView(this);
        themeIcon.setImageResource(this.darkMode ? R.drawable.ci_moon : R.drawable.ci_sun);
        themeIcon.setColorFilter(subTextColor());
        themeRow.addView(themeIcon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView themeLabel = new TextView(this);
        themeLabel.setText(this.lang.t("dark_mode"));
        themeLabel.setTextSize(15.0f);
        themeLabel.setTextColor(textColor());
        themeLabel.setTypeface(Typeface.DEFAULT_BOLD);
        themeLabel.setPadding(dp(8), 0, 0, 0);
        themeRow.addView(themeLabel, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Switch themeSwitch = new Switch(this);
        themeSwitch.setChecked(this.darkMode);
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda21
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.this.m52xe6403ad(compoundButton, z);
            }
        });
        themeRow.addView(themeSwitch);
        linearLayoutCard.addView(themeRow);
        linearLayoutCard.addView(paragraph(this.lang.t("dns_fix_help")));
        Button dnsFix = outlineButton(this.lang.t("apply_dns_fix"));
        dnsFix.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m53xf3a5726e(view);
            }
        });
        linearLayoutCard.addView(dnsFix, matchWrap());
        Button checkUpdates = outlineButton(this.lang.t("check_updates"));
        checkUpdates.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda12
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m54xd8e6e12f(view);
            }
        });
        linearLayoutCard.addView(checkUpdates, matchWrap());
        linearLayoutCard.addView(paragraph(this.lang.t("storage_path") + ":\n" + this.serverDir.getAbsolutePath()));
        Button terms = primaryButton(this.lang.t("terms_of_service"));
        terms.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda13
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m55xbe284ff0(view);
            }
        });
        linearLayoutCard.addView(terms, matchWrap());
        Button privacy = outlineButton(this.lang.t("privacy_terms"));
        privacy.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda14
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m56xa369beb1(view);
            }
        });
        linearLayoutCard.addView(privacy, matchWrap());
        Button discord = darkButton(this.lang.t("discord_community"));
        discord.setOnClickListener(new View.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda15
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m57x57094347(view);
            }
        });
        linearLayoutCard.addView(discord, matchWrap());
        linearLayoutNewPage.addView(linearLayoutCard);
    }

    /* JADX INFO: renamed from: lambda$showSettingsTab$25$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m52xe6403ad(CompoundButton buttonView, boolean isChecked) {
        this.darkMode = isChecked;
        getSharedPreferences(Lang.PREF, 0).edit().putBoolean(KEY_DARK, this.darkMode).apply();
        buildShell();
        showSettingsTab();
    }

    /* JADX INFO: renamed from: lambda$showSettingsTab$26$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m53xf3a5726e(View v) {
        if (tryWriteDnsFixFromActivity(true)) {
            Toast.makeText(this, this.lang.t("dns_fix_applied"), 1).show();
        } else {
            requestDnsStoragePermissionIfNeeded(true);
        }
    }

    /* JADX INFO: renamed from: lambda$showSettingsTab$27$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m54xd8e6e12f(View v) {
        checkForAppUpdates(true);
    }

    /* JADX INFO: renamed from: lambda$showSettingsTab$28$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m55xbe284ff0(View v) {
        openUrl(TERMS_URL);
    }

    /* JADX INFO: renamed from: lambda$showSettingsTab$29$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m56xa369beb1(View v) {
        openUrl(PRIVACY_URL);
    }

    /* JADX INFO: renamed from: lambda$showSettingsTab$30$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m57x57094347(View v) {
        openUrl(DISCORD_URL);
    }

    private void checkForAppUpdates(boolean force) {
        if (force || !this.updateCheckScheduled) {
            if (!force) {
                this.updateCheckScheduled = true;
            }
            UpdateChecker.check(this, force, this.lang);
        }
    }

    private void installOrUpdateRuntime() {
        if (AltayServerService.isRunningStatic()) {
            Toast.makeText(this, this.lang.t("stop_before_update"), 1).show();
            return;
        }
        setProgress(true, this.lang.t("preparing"), 0);
        AltayServerService.clearLog();
        new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda34
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m31x6c4bbc32();
            }
        }, "altay-installer").start();
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$42$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m31x6c4bbc32() {
        try {
            appendUiLog(this.lang.t("log_install_start") + "\n");
            ensureServerFolders();
            DownloadUtils.AltayReleaseInfo altayRelease = DownloadUtils.findLatestAltayRelease();
            String altayUrl = altayRelease.downloadUrl;
            String detectedVersion = altayRelease.version == null || altayRelease.version.trim().isEmpty()
                    ? extractVersionFromAltayUrl(altayUrl)
                    : altayRelease.version.trim();
            if (!"-".equals(detectedVersion) && !detectedVersion.isEmpty()) {
                getSharedPreferences(Lang.PREF, 0).edit().putString(KEY_ALTAY_VERSION, detectedVersion).apply();
            }
            File phar = new File(this.serverDir, "Altay.phar");
            runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda29
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m20xc41de412();
                }
            });
            DownloadUtils.downloadToFile(altayUrl, phar, new DownloadUtils.ProgressCallback() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda24
                @Override // com.vizysolutions.pmmpmobile.DownloadUtils.ProgressCallback
                public final void onProgress(String str, int i) {
                    MainActivity.this.m22x8ea0c194(str, i);
                }
            }, "Altay");
            appendUiLog(this.lang.t("log_altay_downloaded") + ": " + altayUrl + "\n");
            File phpArchive = new File(getCacheDir(), "php-arm64.tar.gz");
            runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda30
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m23x73e23055();
                }
            });
            DownloadUtils.downloadToFile(DownloadUtils.PHP_ARM64_URL, phpArchive, new DownloadUtils.ProgressCallback() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda25
                @Override // com.vizysolutions.pmmpmobile.DownloadUtils.ProgressCallback
                public final void onProgress(String str, int i) {
                    MainActivity.this.m25x3e650dd7(str, i);
                }
            }, "PHP");
            File phpDir = new File(getFilesDir(), "php-runtime");
            deleteRecursive(phpDir);
            phpDir.mkdirs();
            runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda31
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m26x23a67c98();
                }
            });
            DownloadUtils.extractTarGz(phpArchive, phpDir, new DownloadUtils.ProgressCallback() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda26
                @Override // com.vizysolutions.pmmpmobile.DownloadUtils.ProgressCallback
                public final void onProgress(String str, int i) {
                    MainActivity.this.m28xee295a1a(str, i);
                }
            });
            File php = DownloadUtils.findPhpExecutable(phpDir);
            if (!DownloadUtils.isPhpExecutableCandidate(php)) {
                throw new Exception(this.lang.t("php_not_found_after_extract"));
            }
            php.setExecutable(true, false);
            getSharedPreferences(Lang.PREF, 0).edit().putString(AltayServerService.PREF_PHP_PATH, php.getAbsolutePath()).apply();
            appendUiLog(this.lang.t("log_php_installed") + ": " + php.getAbsolutePath() + "\n");
            runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda32
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m29xa1c8deb0();
                }
            });
        } catch (Exception e) {
            appendUiLog(this.lang.t("log_install_error") + ": " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
            runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda40
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m30x870a4d71(e);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$31$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m20xc41de412() {
        setProgress(true, this.lang.t("downloading_altay"), 0);
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$32$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m21xa95f52d3(String msg, int percent) {
        setProgress(true, msg, percent);
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$33$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m22x8ea0c194(final String msg, final int percent) {
        runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda41
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m21xa95f52d3(msg, percent);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$34$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m23x73e23055() {
        setProgress(true, this.lang.t("downloading_php"), 0);
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$35$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m24x59239f16(String msg, int percent) {
        setProgress(true, msg, percent);
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$36$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m25x3e650dd7(final String msg, final int percent) {
        runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda42
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m24x59239f16(msg, percent);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$37$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m26x23a67c98() {
        setProgress(true, this.lang.t("extracting_php"), 0);
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$38$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m27x8e7eb59(String msg, int percent) {
        setProgress(true, msg, percent);
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$39$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m28xee295a1a(final String msg, final int percent) {
        runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda43
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m27x8e7eb59(msg, percent);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$40$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m29xa1c8deb0() {
        setProgress(false, this.lang.t("download_done"), 100);
        updateStatusViews();
        Toast.makeText(this, this.lang.t("download_done"), 1).show();
    }

    /* JADX INFO: renamed from: lambda$installOrUpdateRuntime$41$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m30x870a4d71(Exception e) {
        setProgress(false, this.lang.t("download_failed"), 0);
        updateStatusViews();
        Toast.makeText(this, this.lang.t("download_failed") + ": " + e.getMessage(), 1).show();
    }

    private void startServer() {
        if (AltayServerService.isRunningStatic()) {
            Toast.makeText(this, this.lang.t("server_already_running"), 0).show();
            updateStatusViews();
            return;
        }
        requestBatteryOptimizationPermissionIfNeeded();
        if (!tryWriteDnsFixFromActivity(true)) {
            getSharedPreferences(Lang.PREF, 0).edit().putBoolean(KEY_DNS_START_PENDING, true).apply();
            requestDnsStoragePermissionIfNeeded(false);
            return;
        }
        getSharedPreferences(Lang.PREF, 0).edit().putBoolean(KEY_DNS_START_PENDING, false).apply();
        Intent i = new Intent(this, (Class<?>) AltayServerService.class);
        i.setAction(AltayServerService.ACTION_START);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(i);
        } else {
            startService(i);
        }
        Toast.makeText(this, this.lang.t("starting"), 0).show();
    }

    private void stopServer() {
        Intent i = new Intent(this, (Class<?>) AltayServerService.class);
        i.setAction(AltayServerService.ACTION_STOP);
        startService(i);
        Toast.makeText(this, this.lang.t("stopping"), 0).show();
    }

    private void restartServer() {
        Intent i = new Intent(this, (Class<?>) AltayServerService.class);
        i.setAction(AltayServerService.ACTION_RESTART);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateStatusViews() {
        boolean running = AltayServerService.isRunningStatic();
        TextView textView = this.serverStatusText;
        if (textView != null) {
            textView.setText(this.lang.t(running ? "running" : "offline"));
        }
        Button button = this.startActionButton;
        if (button != null) {
            button.setEnabled(!running);
            this.startActionButton.setAlpha(running ? 0.48f : 1.0f);
        }
        TextView textView2 = this.playersOnlineText;
        if (textView2 != null) {
            textView2.setText(parsePlayersOnline());
        }
        TextView textView3 = this.altayVersionInfoText;
        if (textView3 != null) {
            textView3.setText(getInstalledAltayVersion());
        }
        File php = getStoredPhpFile();
        TextView textView4 = this.phpStatusText;
        if (textView4 != null) {
            textView4.setText((php == null || !php.exists()) ? this.lang.t("not_installed") : this.lang.t("installed"));
        }
        TextView textView5 = this.altayStatusText;
        if (textView5 != null) {
            textView5.setText(new File(this.serverDir, "Altay.phar").exists() ? this.lang.t("installed") : this.lang.t("not_installed"));
        }
    }

    private String parsePlayersOnline() {
        String log = AltayServerService.getLogText();
        if (log == null) {
            return "0/20";
        }
        Pattern pattern = Pattern.compile("Online\\s+(\\d+)\\/(\\d+)");
        Matcher matcher = pattern.matcher(log);
        String last = null;
        while (matcher.find()) {
            last = matcher.group(1) + "/" + matcher.group(2);
        }
        return last == null ? "0/20" : last;
    }

    private String getInstalledAltayVersion() {
        Lang lang;
        String str;
        String logVersion = parseAltayVersionFromLog(AltayServerService.getLogText());
        if (!"-".equals(logVersion)) {
            return logVersion;
        }
        String saved = getSharedPreferences(Lang.PREF, 0).getString(KEY_ALTAY_VERSION, null);
        if (saved != null && saved.trim().length() > 0) {
            return saved;
        }
        if (new File(this.serverDir, "Altay.phar").exists()) {
            lang = this.lang;
            str = "installed";
        } else {
            lang = this.lang;
            str = "not_installed";
        }
        return lang.t(str);
    }

    private String parseAltayVersionFromLog(String log) {
        if (log == null) {
            return "-";
        }
        Pattern[] patterns = {Pattern.compile("Altay\\s+(?:version\\s+)?([0-9]+(?:\\.[0-9]+)+(?:[-A-Za-z0-9.]*)?)", 2), Pattern.compile("Altay[^\\n]*?([0-9]+(?:\\.[0-9]+)+(?:[-A-Za-z0-9.]*)?)", 2)};
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(log);
            String last = null;
            while (matcher.find()) {
                last = matcher.group(1);
            }
            if (last != null) {
                return last;
            }
        }
        return "-";
    }

    private String extractVersionFromAltayUrl(String url) {
        if (url == null) {
            return "-";
        }
        Matcher matcher = Pattern.compile("/download/([^/]+)/Altay\\.phar").matcher(url);
        return matcher.find() ? matcher.group(1) : "-";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateStatsGraphs() {
        float cpu = this.cpuSampler.sample();
        float ram = getAppRamPercent();
        StatsGraphView statsGraphView = this.cpuGraph;
        if (statsGraphView != null && cpu >= 0.0f) {
            statsGraphView.addValue(cpu);
        }
        StatsGraphView statsGraphView2 = this.ramGraph;
        if (statsGraphView2 != null && ram >= 0.0f) {
            statsGraphView2.addValue(ram);
        }
        TextView textView = this.cpuValueText;
        if (textView != null) {
            textView.setText(this.lang.t("cpu") + ": " + this.oneDecimal.format(Math.max(0.0f, cpu)) + "%");
        }
        TextView textView2 = this.ramValueText;
        if (textView2 != null) {
            textView2.setText(this.lang.t("app_ram") + ": " + this.oneDecimal.format(Math.max(0.0f, ram)) + "%");
        }
    }

    private float getAppRamPercent() {
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        long max = rt.maxMemory();
        if (max <= 0) {
            return 0.0f;
        }
        return Math.min(100.0f, (used * 100.0f) / max);
    }

    private File getStoredPhpFile() {
        String path = getSharedPreferences(Lang.PREF, 0).getString(AltayServerService.PREF_PHP_PATH, null);
        File stored = path == null ? null : new File(path);
        if (DownloadUtils.isPhpExecutableCandidate(stored)) {
            return stored;
        }
        File found = DownloadUtils.findPhpExecutable(new File(getFilesDir(), "php-runtime"));
        if (DownloadUtils.isPhpExecutableCandidate(found)) {
            getSharedPreferences(Lang.PREF, 0).edit().putString(AltayServerService.PREF_PHP_PATH, found.getAbsolutePath()).apply();
            return found;
        }
        if (stored != null && stored.exists()) {
            getSharedPreferences(Lang.PREF, 0).edit().remove(AltayServerService.PREF_PHP_PATH).apply();
        }
        return null;
    }

    private void setProgress(boolean visible, String message, int percent) {
        ProgressBar progressBar = this.progressBar;
        if (progressBar != null) {
            progressBar.setVisibility(visible ? 0 : 8);
            this.progressBar.setProgress(Math.max(0, Math.min(100, percent)));
        }
        TextView textView = this.progressText;
        if (textView != null) {
            textView.setText(message == null ? "" : message);
        }
    }

    private void appendUiLog(String text) {
        AltayServerService.log(text);
    }

    private String readServerPort() {
        try {
            String txt = readFile(new File(this.serverDir, "server.properties"));
            for (String str : txt.split("\\r?\\n")) {
                String line = str.trim();
                if (line.startsWith("server-port=")) {
                    return line.substring(line.indexOf(61) + 1).trim();
                }
            }
            return "19132";
        } catch (Exception e) {
            return "19132";
        }
    }

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!addr.isLoopbackAddress() && (addr instanceof Inet4Address)) {
                        return addr.getHostAddress();
                    }
                }
            }
            return "127.0.0.1";
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private void openTextFile(final File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            if (file.length() > 524288) {
                Toast.makeText(this, this.lang.t("file_too_large"), 1).show();
                return;
            }
            final EditText edit = new EditText(this);
            edit.setInputType(655361);
            edit.setGravity(8388659);
            edit.setMinLines(14);
            edit.setText(readFile(file));
            edit.setTypeface(Typeface.MONOSPACE);
            edit.setTextColor(textColor());
            edit.setHintTextColor(subTextColor());
            new AlertDialog.Builder(this).setTitle(file.getName()).setView(edit).setPositiveButton(this.lang.t("save"), new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda47
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.m32lambda$openTextFile$43$comvizysolutionspmmpmobileMainActivity(file, edit, dialogInterface, i);
                }
            }).setNegativeButton(this.lang.t("cancel"), (DialogInterface.OnClickListener) null).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), 1).show();
        }
    }

    /* JADX INFO: renamed from: lambda$openTextFile$43$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m32lambda$openTextFile$43$comvizysolutionspmmpmobileMainActivity(File file, EditText edit, DialogInterface dialog, int which) {
        try {
            writeFile(file, edit.getText().toString());
            if (file.getName().equals("server.properties")) {
                showServerTab();
            } else {
                showFilesTab();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), 1).show();
        }
    }

    private void createEntry(final boolean folder) {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(this.lang.t(folder ? "folder_name_hint" : "file_name_hint"));
        new AlertDialog.Builder(this).setTitle(this.lang.t(folder ? "new_folder" : "new_file")).setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda44
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.m13lambda$createEntry$44$comvizysolutionspmmpmobileMainActivity(input, folder, dialogInterface, i);
            }
        }).setNegativeButton(this.lang.t("cancel"), (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$createEntry$44$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m13lambda$createEntry$44$comvizysolutionspmmpmobileMainActivity(EditText input, boolean folder, DialogInterface dialog, int which) {
        try {
            String name = input.getText().toString().trim();
            if (!name.isEmpty() && !name.contains("/") && !name.contains("..")) {
                File target = new File(this.currentDir, name);
                if (folder) {
                    target.mkdirs();
                } else {
                    target.createNewFile();
                }
                showFilesTab();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), 1).show();
        }
    }

    private boolean isInsideServerDir(File file) {
        if (file != null) {
            try {
                return file.getCanonicalPath().startsWith(this.serverDir.getCanonicalPath());
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private boolean sameFile(File a, File b) {
        try {
            return a.getCanonicalPath().equals(b.getCanonicalPath());
        } catch (Exception e) {
            return a.equals(b);
        }
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(1);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackground(makeRound(cardColor(), dp(3), borderColor()));
        if (Build.VERSION.SDK_INT >= 21) {
            c.setElevation(dp(2));
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LIGHT_CARD, -2);
        lp.setMargins(0, 0, 0, dp(12));
        c.setLayoutParams(lp);
        return c;
    }

    private TextView sectionTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(textColor());
        tv.setTextSize(18.0f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(0, 0, 0, dp(8));
        return tv;
    }

    private TextView label(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(textColor());
        tv.setTextSize(14.0f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(0, dp(8), 0, dp(2));
        return tv;
    }

    private TextView labelValue(LinearLayout linearLayout, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(0);
        row.setPadding(0, dp(4), 0, dp(4));
        TextView l = new TextView(this);
        l.setText(label + ": ");
        l.setTypeface(Typeface.DEFAULT_BOLD);
        l.setTextColor(textColor());
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(textColor());
        row.addView(l);
        row.addView(v, new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (linearLayout != null) {
            linearLayout.addView(row);
        }
        return v;
    }

    private TextView paragraph(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(textColor());
        tv.setTextSize(14.0f);
        tv.setLineSpacing(2.0f, 1.05f);
        tv.setPadding(0, dp(4), 0, dp(4));
        return tv;
    }

    private TextView smallText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(subTextColor());
        tv.setTextSize(12.0f);
        tv.setPadding(0, dp(4), 0, dp(4));
        return tv;
    }

    private Button primaryButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(LIGHT_CARD);
        b.setTextSize(14.0f);
        b.setBackground(makeRound(primary(), dp(4), primaryDark()));
        return b;
    }

    private Button darkButton(String text) {
        int i;
        int i2;
        int i3;
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(LIGHT_CARD);
        b.setTextSize(14.0f);
        if (this.darkMode) {
            i = 70;
            i2 = 82;
            i3 = 88;
        } else {
            i = 55;
            i2 = 71;
            i3 = 79;
        }
        b.setBackground(makeRound(Color.rgb(i, i2, i3), dp(4), 0));
        return b;
    }

    private Button outlineButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(primaryDark());
        b.setTextSize(14.0f);
        b.setBackground(makeRound(0, dp(4), primary()));
        return b;
    }

    private GradientDrawable makeRound(int color, int radius, int stroke) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radius);
        if (stroke != 0) {
            gd.setStroke(Math.max(1, dp(1)), stroke);
        }
        return gd;
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LIGHT_CARD, -2);
        lp.setMargins(0, dp(5), 0, dp(5));
        return lp;
    }

    private LinearLayout.LayoutParams rowButtonLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(56), 1.0f);
        lp.setMargins(dp(4), dp(5), dp(4), dp(5));
        return lp;
    }

    private int dp(int value) {
        return (int) ((value * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, url, 1).show();
        }
    }

    private static String dnsFixContent() {
        return "nameserver 1.1.1.1\nnameserver 8.8.8.8\nnameserver 9.9.9.9\noptions timeout:2 attempts:3\n";
    }

    private boolean tryWriteDnsFixFromActivity(boolean showLog) {
        String dns = dnsFixContent();
        ArrayList<File> targets = new ArrayList<>();
        targets.add(new File("/sdcard/resolv.conf"));
        try {
            File external = Environment.getExternalStorageDirectory();
            if (external != null) {
                targets.add(new File(external, "resolv.conf"));
            }
        } catch (Exception e) {
        }
        ArrayList<String> attempted = new ArrayList<>();
        String lastError = null;
        for (File target : targets) {
            if (target != null) {
                String path = target.getAbsolutePath();
                if (attempted.contains(path)) {
                    continue;
                } else {
                    attempted.add(path);
                    try {
                        File parent = target.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }
                        FileOutputStream fos = new FileOutputStream(target, false);
                        fos.write(dns.getBytes(StandardCharsets.UTF_8));
                        fos.flush();
                        fos.close();
                        if (target.exists() && target.length() > 0) {
                            if (showLog) {
                                appendUiLog(this.lang.t("dns_fix_applied") + ": " + path + "\n");
                                return true;
                            }
                            return true;
                        }
                    } catch (Exception e2) {
                        lastError = e2.getClass().getSimpleName() + ": " + e2.getMessage();
                    }
                }
            }
        }
        if (showLog) {
            appendUiLog(this.lang.t("dns_fix_failed") + "\n");
            if (lastError != null) {
                appendUiLog(lastError + "\n");
            }
        }
        return false;
    }

    private void requestDnsStoragePermissionIfNeeded(boolean onlyOpenSettings) {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                if (!Environment.isExternalStorageManager()) {
                    new AlertDialog.Builder(this).setTitle(this.lang.t("dns_fix_title")).setMessage(this.lang.t("dns_fix_message_required")).setPositiveButton(this.lang.t("allow"), new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda11
                        @Override // android.content.DialogInterface.OnClickListener
                        public final void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.m41xe4bc433e(dialogInterface, i);
                        }
                    }).setNegativeButton(this.lang.t("later"), new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda22
                        @Override // android.content.DialogInterface.OnClickListener
                        public final void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.m42xc9fdb1ff(dialogInterface, i);
                        }
                    }).show();
                }
            } else if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, REQ_DNS_STORAGE);
                Toast.makeText(this, this.lang.t("dns_permission_needed"), 1).show();
            } else if (!onlyOpenSettings) {
                Toast.makeText(this, this.lang.t("dns_permission_needed"), 1).show();
            }
        } catch (Exception e) {
        }
    }

    /* JADX INFO: renamed from: lambda$requestDnsStoragePermissionIfNeeded$45$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m41xe4bc433e(DialogInterface d, int w) {
        try {
            Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"));
            } catch (Exception e2) {
            }
        }
    }

    /* JADX INFO: renamed from: lambda$requestDnsStoragePermissionIfNeeded$46$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m42xc9fdb1ff(DialogInterface d, int w) {
        getSharedPreferences(Lang.PREF, 0).edit().putBoolean(KEY_DNS_START_PENDING, false).apply();
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_DNS_STORAGE) {
            SharedPreferences sp = getSharedPreferences(Lang.PREF, 0);
            if (sp.getBoolean(KEY_DNS_START_PENDING, false)) {
                if (tryWriteDnsFixFromActivity(true)) {
                    sp.edit().putBoolean(KEY_DNS_START_PENDING, false).apply();
                    startServer();
                } else {
                    requestDnsStoragePermissionIfNeeded(true);
                }
            }
        }
    }

    private void requestBatteryOptimizationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        SharedPreferences sp = getSharedPreferences(Lang.PREF, 0);
        PowerManager pm = (PowerManager) getSystemService("power");
        if (pm == null || pm.isIgnoringBatteryOptimizations(getPackageName()) || sp.getBoolean(KEY_BATTERY_ASKED, false)) {
            return;
        }
        sp.edit().putBoolean(KEY_BATTERY_ASKED, true).apply();
        new AlertDialog.Builder(this).setTitle(this.lang.t("battery_title")).setMessage(this.lang.t("battery_message")).setPositiveButton(this.lang.t("allow"), new DialogInterface.OnClickListener() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.m40xf829259c(dialogInterface, i);
            }
        }).setNegativeButton(this.lang.t("later"), (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$requestBatteryOptimizationPermissionIfNeeded$47$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m40xf829259c(DialogInterface d, int w) {
        try {
            Intent intent = new Intent("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"));
            } catch (Exception e2) {
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("*/*");
        intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        startActivityForResult(Intent.createChooser(intent, this.lang.t("upload_files")), REQ_UPLOAD_FILES);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_UPLOAD_FILES && resultCode == LIGHT_CARD && data != null) {
            int copied = 0;
            try {
                ClipData clip = data.getClipData();
                if (clip != null) {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        if (copyUriToCurrentDir(clip.getItemAt(i).getUri())) {
                            copied++;
                        }
                    }
                } else if (data.getData() != null && copyUriToCurrentDir(data.getData())) {
                    copied = 0 + 1;
                }
                Toast.makeText(this, this.lang.t("files_uploaded") + ": " + copied, 1).show();
            } catch (Exception e) {
                Toast.makeText(this, this.lang.t("upload_failed") + ": " + e.getMessage(), 1).show();
            }
            showFilesTab();
        }
    }

    private boolean copyUriToCurrentDir(Uri uri) throws Exception {
        if (uri == null || this.currentDir == null) {
            return false;
        }
        String name = getDisplayName(uri);
        if (name == null || name.trim().isEmpty()) {
            name = this.lang.t("file_default_prefix") + System.currentTimeMillis();
        }
        File out = new File(this.currentDir, name.replace("/", "_").replace("\\", "_"));
        InputStream input = getContentResolver().openInputStream(uri);
        if (input == null) {
            return false;
        }
        FileOutputStream output = new FileOutputStream(out);
        byte[] buffer = new byte[8192];
        while (true) {
            int read = input.read(buffer);
            if (read != LIGHT_CARD) {
                output.write(buffer, 0, read);
            } else {
                input.close();
                output.close();
                return true;
            }
        }
    }

    private String getDisplayName(Uri uri) {
        if (uri == null) {
            return null;
        }

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex("_display_name");
                if (idx >= 0) {
                    String displayName = cursor.getString(idx);
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        return displayName;
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall back to the final path segment below.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private boolean isArchive(File f) {
        if (f == null || f.isDirectory()) {
            return false;
        }
        String n = f.getName().toLowerCase(Locale.ROOT);
        return n.endsWith(".zip") || n.endsWith(".tar.gz") || n.endsWith(".tgz") || n.endsWith(".gz") || n.endsWith(".rar");
    }

    private void extractArchive(final File archive) {
        new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda38
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m16xaeacd69f(archive);
            }
        }, "archive-extract").start();
    }

    /* JADX INFO: renamed from: lambda$extractArchive$50$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m16xaeacd69f(File archive) {
        try {
            String n = archive.getName().toLowerCase(Locale.ROOT);
            File dest = new File(archive.getParentFile(), stripArchiveExtension(archive.getName()));
            if (!dest.exists()) {
                dest.mkdirs();
            }
            if (n.endsWith(".zip")) {
                extractZip(archive, dest);
            } else if (n.endsWith(".tar.gz") || n.endsWith(".tgz")) {
                DownloadUtils.extractTarGz(archive, dest, null);
            } else if (n.endsWith(".gz")) {
                extractGzipFile(archive, new File(archive.getParentFile(), stripGz(archive.getName())));
            } else if (n.endsWith(".rar")) {
                throw new Exception(this.lang.t("rar_not_supported"));
            }
            runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda28
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m14x15cbe348();
                }
            });
        } catch (Exception e) {
            runOnUiThread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.MainActivity$$ExternalSyntheticLambda39
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m15xfb0d5209(e);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$extractArchive$48$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m14x15cbe348() {
        Toast.makeText(this, this.lang.t("extract_done"), 1).show();
        showFilesTab();
    }

    /* JADX INFO: renamed from: lambda$extractArchive$49$com-vizysolutions-pmmpmobile-MainActivity, reason: not valid java name */
    public /* synthetic */ void m15xfb0d5209(Exception e) {
        Toast.makeText(this, this.lang.t("extract_failed") + ": " + e.getMessage(), 1).show();
    }

    private String stripArchiveExtension(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".tar.gz")) {
            return name.substring(0, name.length() - 7);
        }
        if (lower.endsWith(".tgz")) {
            return name.substring(0, name.length() - 4);
        }
        if (lower.endsWith(".zip") || lower.endsWith(".rar")) {
            return name.substring(0, name.length() - 4);
        }
        if (lower.endsWith(".gz")) {
            return name.substring(0, name.length() - 3);
        }
        return name + "-extraido";
    }

    private String stripGz(String name) {
        if (name.toLowerCase(Locale.ROOT).endsWith(".gz")) {
            return name.substring(0, name.length() - 3);
        }
        return name + ".out";
    }

    private void extractZip(File zip, File targetDir) throws Exception {
        String root = targetDir.getCanonicalPath();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
        byte[] buffer = new byte[8192];
        while (true) {
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                File out = new File(targetDir, entry.getName());
                String outPath = out.getCanonicalPath();
                if (!outPath.equals(root)) {
                    if (!outPath.startsWith(root + File.separator)) {
                    }
                }
                if (entry.isDirectory()) {
                    out.mkdirs();
                } else {
                    File parent = out.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(out);
                    while (true) {
                        int read = zis.read(buffer);
                        if (read == LIGHT_CARD) {
                            break;
                        } else {
                            fos.write(buffer, 0, read);
                        }
                    }
                    fos.close();
                }
                zis.closeEntry();
            } else {
                zis.close();
                return;
            }
        }
    }

    private void extractGzipFile(File gz, File out) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(gz));
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buffer = new byte[8192];
        while (true) {
            int read = gis.read(buffer);
            if (read == LIGHT_CARD) {
                gis.close();
                fos.close();
                return;
            }
            fos.write(buffer, 0, read);
        }
    }

    private String readFile(File file) throws Exception {
        FileInputStream input = new FileInputStream(file);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        while (true) {
            int read = input.read(buf);
            if (read == LIGHT_CARD) {
                input.close();
                return output.toString(StandardCharsets.UTF_8.name());
            }
            output.write(buf, 0, read);
        }
    }

    private void writeFile(File file, String text) throws Exception {
        FileOutputStream output = new FileOutputStream(file);
        output.write(text.getBytes(StandardCharsets.UTF_8));
        output.close();
    }

    private void deleteRecursive(File file) {
        File[] children;
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory() && (children = file.listFiles()) != null) {
            for (File child : children) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }

    private int iconForFile(File f) {
        if (f.isDirectory()) {
            return R.drawable.ci_folder;
        }
        String n = f.getName().toLowerCase(Locale.ROOT);
        if (isArchive(f)) {
            return R.drawable.ci_archive;
        }
        if (n.endsWith(".phar") || n.endsWith(".php") || n.endsWith(".yml") || n.endsWith(".yaml") || n.endsWith(".properties") || n.endsWith(".json")) {
            return R.drawable.ci_code;
        }
        return R.drawable.ci_file;
    }

    private String fileMeta(File f) {
        String date = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date(f.lastModified()));
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            int count = children == null ? 0 : children.length;
            return count + " " + this.lang.t("items") + " • " + date;
        }
        return humanSize(f.length()) + " • " + date;
    }

    private String humanSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1048576) {
            return this.oneDecimal.format(size / 1024.0d) + " KB";
        }
        return this.oneDecimal.format((size / 1024.0d) / 1024.0d) + " MB";
    }

    private String shortPath(File dir) {
        try {
            String rootPath = this.serverDir.getCanonicalPath();
            String path = dir.getCanonicalPath();
            if (path.equals(rootPath)) {
                return "Altay/";
            }
            if (path.startsWith(rootPath)) {
                return "Altay" + path.substring(rootPath.length()) + "/";
            }
        } catch (Exception e) {
        }
        return dir.getAbsolutePath();
    }

    private SpannableStringBuilder colorizeLog(String raw) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        if (raw == null || raw.isEmpty()) {
            appendColored(out, this.lang.t("waiting_logs") + "\n", Color.rgb(90, 100, 105));
            return out;
        }
        String[] lines = raw.split("\\n", LIGHT_CARD);
        for (String line : lines) {
            if (line.indexOf(27) >= 0 || line.indexOf(167) >= 0) {
                appendMarkupColored(out, line + "\n");
            } else {
                appendColored(out, line + "\n", colorForLogLine(line));
            }
        }
        return out;
    }

    private void appendMarkupColored(SpannableStringBuilder out, String line) {
        int end;
        int color = Color.rgb(45, 55, 60);
        StringBuilder segment = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == 27 && i + 1 < line.length() && line.charAt(i + 1) == '[' && (end = line.indexOf(109, i + 2)) != LIGHT_CARD) {
                flushColoredSegment(out, segment, color);
                color = ansiColor(line.substring(i + 2, end), color);
                i = end;
            } else if (c == 167 && i + 1 < line.length()) {
                flushColoredSegment(out, segment, color);
                color = minecraftColor(line.charAt(i + 1), color);
                i++;
            } else {
                segment.append(c);
            }
            i++;
        }
        flushColoredSegment(out, segment, color);
    }

    private void flushColoredSegment(SpannableStringBuilder out, StringBuilder segment, int color) {
        if (segment.length() == 0) {
            return;
        }
        int start = out.length();
        out.append((CharSequence) segment.toString());
        out.setSpan(new ForegroundColorSpan(color), start, out.length(), 33);
        segment.setLength(0);
    }

    private void appendColored(SpannableStringBuilder out, String text, int color) {
        int start = out.length();
        out.append((CharSequence) text);
        out.setSpan(new ForegroundColorSpan(color), start, out.length(), 33);
    }

    private int colorForLogLine(String line) {
        String l = line == null ? "" : line.toLowerCase(Locale.ROOT);
        if (l.startsWith(">")) {
            return Color.rgb(0, 131, 143);
        }
        if (l.contains("error") || l.contains("critical") || l.contains("exception") || l.contains("failed")) {
            return Color.rgb(211, 47, 47);
        }
        if (l.contains("warn")) {
            return Color.rgb(245, 124, 0);
        }
        if (l.contains("notice") || l.contains("info") || l.contains("starting") || l.contains("done") || l.contains("server started")) {
            return Color.rgb(46, 125, 50);
        }
        return l.contains(BuildConfig.BUILD_TYPE) ? Color.rgb(21, 101, 192) : Color.rgb(45, 55, 60);
    }

    private int ansiColor(String codes, int current) {
        String[] parts = codes.split(";");
        int color = current;
        for (String part : parts) {
            try {
                int c = Integer.parseInt(part.trim());
                if (c == 0 || c == 39) {
                    color = Color.rgb(45, 55, 60);
                } else if (c == 30) {
                    color = Color.rgb(0, 0, 0);
                } else if (c == 31) {
                    color = Color.rgb(211, 47, 47);
                } else if (c == 32) {
                    color = Color.rgb(46, 125, 50);
                } else if (c == 33) {
                    color = Color.rgb(245, 124, 0);
                } else if (c == 34) {
                    color = Color.rgb(21, 101, 192);
                } else if (c == 35) {
                    color = Color.rgb(123, 31, 162);
                } else if (c == 36) {
                    color = Color.rgb(0, 131, 143);
                } else if (c == 37) {
                    color = Color.rgb(45, 55, 60);
                } else if (c == 90) {
                    color = Color.rgb(120, 130, 135);
                } else if (c == 91) {
                    color = Color.rgb(198, 40, 40);
                } else if (c == 92) {
                    color = Color.rgb(27, 94, 32);
                } else if (c == 93) {
                    color = Color.rgb(230, 81, 0);
                } else if (c == 94) {
                    color = Color.rgb(13, 71, 161);
                } else if (c == 95) {
                    color = Color.rgb(136, 14, 79);
                } else if (c == 96) {
                    color = Color.rgb(0, 96, 100);
                } else if (c == 97) {
                    color = Color.rgb(45, 55, 60);
                }
            } catch (Exception e) {
            }
        }
        return color;
    }

    private int minecraftColor(char code, int current) {
        switch (Character.toLowerCase(code)) {
            case '0':
                return Color.rgb(0, 0, 0);
            case '1':
                return Color.rgb(0, 0, 170);
            case '2':
                return Color.rgb(0, 170, 0);
            case '3':
                return Color.rgb(0, 170, 170);
            case '4':
                return Color.rgb(170, 0, 0);
            case '5':
                return Color.rgb(170, 0, 170);
            case '6':
                return Color.rgb(255, 170, 0);
            case '7':
                return Color.rgb(170, 170, 170);
            case '8':
                return Color.rgb(85, 85, 85);
            case '9':
                return Color.rgb(85, 85, 255);
            case 'a':
                return Color.rgb(85, 255, 85);
            case 'b':
                return Color.rgb(85, 255, 255);
            case 'c':
                return Color.rgb(255, 85, 85);
            case 'd':
                return Color.rgb(255, 85, 255);
            case 'e':
                return Color.rgb(255, 255, 85);
            case 'f':
                return LIGHT_CARD;
            case 'r':
                return Color.rgb(45, 55, 60);
            default:
                return current;
        }
    }

    private int bgColor() {
        return this.darkMode ? Color.rgb(18, 22, 24) : LIGHT_BG;
    }

    private int cardColor() {
        return this.darkMode ? DARK_CARD : LIGHT_CARD;
    }

    private int navColor() {
        return this.darkMode ? DARK_SURFACE : LIGHT_CARD;
    }

    private int textColor() {
        return this.darkMode ? Color.rgb(235, 239, 241) : Color.rgb(33, 33, 33);
    }

    private int subTextColor() {
        int i;
        int i2;
        int i3;
        if (this.darkMode) {
            i = 176;
            i2 = 190;
            i3 = 197;
        } else {
            i = 84;
            i2 = 110;
            i3 = 122;
        }
        return Color.rgb(i, i2, i3);
    }

    private int borderColor() {
        return this.darkMode ? Color.rgb(60, 70, 75) : Color.rgb(224, 224, 224);
    }

    private int primary() {
        return PM_GREEN;
    }

    private int primaryDark() {
        return PM_GREEN_DARK;
    }

    private int selectedNavColor() {
        return this.darkMode ? PM_GREEN_DARK : PM_GREEN;
    }

    private static class IntroPage {
        final int color;
        final String description;
        final int iconRes;
        final String title;

        IntroPage(int iconRes, String title, String description, int color) {
            this.iconRes = iconRes;
            this.title = title;
            this.description = description;
            this.color = color;
        }
    }

    private static class CpuSampler {
        private long lastCpuMs;
        private long lastWallMs;

        private CpuSampler() {
            this.lastCpuMs = -1L;
            this.lastWallMs = -1L;
        }

        float sample() {
            try {
                long cpuMs = Process.getElapsedCpuTime();
                long wallMs = SystemClock.elapsedRealtime();
                long j = this.lastCpuMs;
                if (j < 0) {
                    this.lastCpuMs = cpuMs;
                    this.lastWallMs = wallMs;
                    return 0.0f;
                }
                long cpuDiff = cpuMs - j;
                long wallDiff = wallMs - this.lastWallMs;
                this.lastCpuMs = cpuMs;
                this.lastWallMs = wallMs;
                if (wallDiff <= 0) {
                    return 0.0f;
                }
                int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
                return Math.max(0.0f, Math.min(100.0f, (cpuDiff * 100.0f) / (((long) cores) * wallDiff)));
            } catch (Exception e) {
                return 0.0f;
            }
        }
    }

    public static class StatsGraphView extends View {
        private final int accent;
        private final boolean dark;
        private final Paint fillPaint;
        private final Paint gridPaint;
        private final Paint linePaint;
        private final Paint textPaint;
        private final List<Float> values;

        public StatsGraphView(Context context, int accent, boolean dark) {
            super(context);
            int i;
            int i2;
            int i3;
            this.values = new ArrayList();
            Paint paint = new Paint(1);
            this.gridPaint = paint;
            Paint paint2 = new Paint(1);
            this.linePaint = paint2;
            Paint paint3 = new Paint(1);
            this.fillPaint = paint3;
            Paint paint4 = new Paint(1);
            this.textPaint = paint4;
            this.accent = accent;
            this.dark = dark;
            paint.setColor(dark ? Color.rgb(67, 77, 82) : Color.rgb(224, 224, 224));
            paint.setStrokeWidth(1.0f);
            paint2.setColor(accent);
            paint2.setStrokeWidth(4.0f);
            paint2.setStyle(Paint.Style.STROKE);
            paint3.setColor(Color.argb(45, Color.red(accent), Color.green(accent), Color.blue(accent)));
            paint3.setStyle(Paint.Style.FILL);
            if (dark) {
                i = 176;
                i2 = 190;
                i3 = 197;
            } else {
                i = 96;
                i2 = 125;
                i3 = 139;
            }
            paint4.setColor(Color.rgb(i, i2, i3));
            paint4.setTextSize(24.0f);
            setWillNotDraw(false);
        }

        public void addValue(float v) {
            if (v < 0.0f) {
                return;
            }
            this.values.add(Float.valueOf(Math.max(0.0f, Math.min(100.0f, v))));
            while (this.values.size() > 40) {
                this.values.remove(0);
            }
            invalidate();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            float f = 1.0f;
            RectF r = new RectF(1.0f, 1.0f, w + MainActivity.LIGHT_CARD, h + MainActivity.LIGHT_CARD);
            Paint bg = new Paint(1);
            bg.setColor(this.dark ? Color.rgb(26, 31, 34) : Color.rgb(250, 250, 250));
            canvas.drawRoundRect(r, 8.0f, 8.0f, bg);
            for (int i = 1; i < 4; i++) {
                float y = 12.0f + (((h - (2.0f * 12.0f)) * i) / 4.0f);
                canvas.drawLine(12.0f, y, w - 12.0f, y, this.gridPaint);
            }
            canvas.drawText("100%", 12.0f + 2.0f, 22.0f + 12.0f, this.textPaint);
            canvas.drawText("0%", 12.0f + 2.0f, (h - 12.0f) - 4.0f, this.textPaint);
            if (this.values.size() < 2) {
                return;
            }
            Path line = new Path();
            Path fill = new Path();
            float graphW = w - (12.0f * 2.0f);
            float graphH = h - (2.0f * 12.0f);
            int i2 = 0;
            while (i2 < this.values.size()) {
                float x = ((i2 * graphW) / Math.max(1, this.values.size() - 1)) + 12.0f;
                float y2 = ((f - (this.values.get(i2).floatValue() / 100.0f)) * graphH) + 12.0f;
                if (i2 == 0) {
                    line.moveTo(x, y2);
                    fill.moveTo(x, h - 12.0f);
                    fill.lineTo(x, y2);
                } else {
                    line.lineTo(x, y2);
                    fill.lineTo(x, y2);
                }
                if (i2 == this.values.size() - 1) {
                    fill.lineTo(x, h - 12.0f);
                    fill.close();
                }
                i2++;
                f = 1.0f;
            }
            canvas.drawPath(fill, this.fillPaint);
            canvas.drawPath(line, this.linePaint);
        }
    }
}
