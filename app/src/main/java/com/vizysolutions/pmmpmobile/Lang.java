package com.vizysolutions.pmmpmobile;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
public class Lang {
    public static final String EN = "en";
    public static final String ES = "es";
    public static final String KEY_LANGUAGE = "language";
    public static final String PREF = "pmmp_mobile_prefs";
    public static final String PT_BR = "pt_br";
    private String code;
    private final Context context;
    private final Map<String, String> strings = new HashMap();

    public Lang(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.context = applicationContext;
        SharedPreferences sp = applicationContext.getSharedPreferences(PREF, 0);
        String string = sp.getString(KEY_LANGUAGE, EN);
        this.code = string;
        load(string);
    }

    public String getCode() {
        return this.code;
    }

    public void setLanguage(String code) {
        this.code = code == null ? EN : code;
        this.context.getSharedPreferences(PREF, 0).edit().putString(KEY_LANGUAGE, this.code).apply();
        load(this.code);
    }

    public String t(String key) {
        String value = this.strings.get(key);
        return value == null ? key : value;
    }

    private void load(String code) {
        this.strings.clear();
        try {
            readFile("lang/en.json");
            if (!EN.equals(code)) {
                readFile("lang/" + code + ".json");
            }
        } catch (Exception e) {
        }
    }

    private void readFile(String assetPath) throws Exception {
        InputStream input = this.context.getAssets().open(assetPath);
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
        JSONObject object = new JSONObject(output.toString(StandardCharsets.UTF_8.name()));
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            this.strings.put(key, object.getString(key));
        }
    }
}
