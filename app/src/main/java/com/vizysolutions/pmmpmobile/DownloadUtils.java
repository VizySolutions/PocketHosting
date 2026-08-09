package com.vizysolutions.pmmpmobile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
public class DownloadUtils {
    public static final String PHP_ARM64_URL = "https://github.com/pmmp/PHP-Binaries/releases/download/pm5-php-8.2-latest/PHP-8.2-Android-arm64-PM5.tar.gz";
    public static final String ALTAY_LATEST_API = "https://api.github.com/repos/altayofficial/Altay/releases/latest";
    public static final String ALTAY_LATEST_PHAR_FALLBACK = "https://github.com/altayofficial/Altay/releases/latest/download/Altay.phar";

    public interface ProgressCallback {
        void onProgress(String str, int i);
    }

    public static String fetchText(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("User-Agent", "PocketHosting-Altay-Vizy-Mobile");
        conn.setRequestProperty("Accept", "application/vnd.github+json, application/json, text/plain, */*");
        int code = conn.getResponseCode();
        InputStream input = (code < 200 || code >= 400) ? conn.getErrorStream() : conn.getInputStream();
        if (input == null) {
            throw new IOException("HTTP " + code + " from " + url);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(input, out, null, "Text");
        input.close();
        if (code < 200 || code >= 400) {
            throw new IOException("HTTP " + code + ": " + out.toString("UTF-8"));
        }
        return out.toString("UTF-8");
    }

    public static AltayReleaseInfo findLatestAltayRelease() {
        try {
            String json = fetchText(ALTAY_LATEST_API);
            JSONObject release = new JSONObject(json);
            String version = release.optString("tag_name", "").trim();
            JSONArray assets = release.optJSONArray("assets");
            String downloadUrl = "";
            String pharFallback = "";
            if (assets != null) {
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.optJSONObject(i);
                    if (asset == null) {
                        continue;
                    }
                    String name = asset.optString("name", "").trim();
                    String url = asset.optString("browser_download_url", "").trim();
                    if (name.equalsIgnoreCase("Altay.phar") && !url.isEmpty()) {
                        downloadUrl = url;
                        break;
                    }
                    if (pharFallback.isEmpty() && name.toLowerCase(Locale.ROOT).endsWith(".phar") && !url.isEmpty()) {
                        pharFallback = url;
                    }
                }
            }
            if (downloadUrl.isEmpty() && !pharFallback.isEmpty()) {
                downloadUrl = pharFallback;
            }
            if (downloadUrl.isEmpty()) {
                downloadUrl = ALTAY_LATEST_PHAR_FALLBACK;
            }
            return new AltayReleaseInfo(version, downloadUrl);
        } catch (Exception e) {
            return new AltayReleaseInfo("", ALTAY_LATEST_PHAR_FALLBACK);
        }
    }

    public static String findAltayPharUrlFromLatestRelease() {
        return findLatestAltayRelease().downloadUrl;
    }

    public static final class AltayReleaseInfo {
        public final String version;
        public final String downloadUrl;

        AltayReleaseInfo(String version, String downloadUrl) {
            this.version = version == null ? "" : version;
            this.downloadUrl = (downloadUrl == null || downloadUrl.isEmpty()) ? ALTAY_LATEST_PHAR_FALLBACK : downloadUrl;
        }
    }

    public static void downloadToFile(String url, File file, ProgressCallback cb, String label) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        File tmp = new File(file.getAbsolutePath() + ".download");
        if (tmp.exists()) {
            tmp.delete();
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(120000);
        conn.setRequestProperty("User-Agent", "PocketHosting-Altay-Vizy-Mobile");
        int code = conn.getResponseCode();
        if (code < 200 || code >= 400) {
            throw new IOException("HTTP " + code + " downloading " + url);
        }
        int length = conn.getContentLength();
        InputStream input = new BufferedInputStream(conn.getInputStream());
        FileOutputStream output = new FileOutputStream(tmp);
        copy(input, output, cb, label, length);
        input.close();
        output.close();
        if (file.exists()) {
            file.delete();
        }
        if (!tmp.renameTo(file)) {
            throw new IOException("Unable to move downloaded file to " + file.getAbsolutePath());
        }
    }

    private static void copy(InputStream input, ByteArrayOutputStream output, ProgressCallback cb, String label) throws IOException {
        byte[] buffer = new byte[8192];
        while (true) {
            int read = input.read(buffer);
            if (read != -1) {
                output.write(buffer, 0, read);
            } else {
                return;
            }
        }
    }

    private static void copy(InputStream input, FileOutputStream output, ProgressCallback cb, String label, int length) throws IOException {
        byte[] buffer = new byte[8192];
        long total = 0;
        int lastPercent = -1;
        while (true) {
            int read = input.read(buffer);
            if (read == -1) {
                break;
            }
            output.write(buffer, 0, read);
            long total2 = total + ((long) read);
            if (cb == null || length <= 0) {
                buffer = buffer;
                total = total2;
            } else {
                byte[] buffer2 = buffer;
                int percent = (int) Math.min(100L, (total2 * 100) / ((long) length));
                if (percent != lastPercent) {
                    cb.onProgress(label + " " + percent + "%", percent);
                    lastPercent = percent;
                }
                buffer = buffer2;
                total = total2;
            }
        }
        if (cb != null) {
            cb.onProgress(label + " 100%", 100);
        }
    }

    public static void extractTarGz(File archive, File targetDir, ProgressCallback cb) throws IOException {
        FileInputStream fis;
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Unable to create " + targetDir.getAbsolutePath());
        }
        FileInputStream fis2 = new FileInputStream(archive);
        GZIPInputStream gis = new GZIPInputStream(new BufferedInputStream(fis2));
        int i = 512;
        byte[] header = new byte[512];
        while (true) {
            int readHeader = readFully(gis, header);
            if (readHeader <= 0 || isZeroBlock(header)) {
                break;
            }
            if (readHeader < i) {
                throw new IOException("Invalid tar header");
            }
            String name = parseName(header, 0, 100);
            String prefix = parseName(header, 345, 155);
            if (!prefix.isEmpty()) {
                name = prefix + "/" + name;
            }
            long size = parseOctal(header, 124, 12);
            byte type = header[156];
            if (name.contains("..")) {
                skipFully(gis, size);
                skipPadding(gis, size);
            } else {
                File out = new File(targetDir, name);
                String root = targetDir.getCanonicalPath();
                String outPath = out.getCanonicalPath();
                if (outPath.equals(root)) {
                    fis = fis2;
                } else {
                    fis = fis2;
                    if (!outPath.startsWith(root + File.separator)) {
                        skipFully(gis, size);
                        skipPadding(gis, size);
                        i = 512;
                        fis2 = fis;
                    }
                }
                if (type == 53 || name.endsWith("/")) {
                    out.mkdirs();
                } else if (type == 48 || type == 0) {
                    File parent = out.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(out);
                    copyLimited(gis, fos, size);
                    fos.close();
                    if (out.getName().equals("php") || out.getName().startsWith("php")) {
                        out.setExecutable(true, false);
                    }
                } else {
                    skipFully(gis, size);
                }
                skipPadding(gis, size);
                i = 512;
                fis2 = fis;
            }
        }
        gis.close();
        if (cb != null) {
            cb.onProgress("PHP extracted", 100);
        }
    }

    public static File findPhpExecutable(File dir) {
        if (dir == null || !dir.exists()) {
            return null;
        }
        File preferred = findPhpExecutableInternal(dir, true);
        return preferred != null ? preferred : findPhpExecutableInternal(dir, false);
    }

    private static File findPhpExecutableInternal(File dir, boolean preferBinPath) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                File found = findPhpExecutableInternal(f, preferBinPath);
                if (found != null) {
                    return found;
                }
            } else if (isPhpExecutableCandidate(f)) {
                String normalized = f.getAbsolutePath().replace('\\', '/').toLowerCase(Locale.ROOT);
                boolean inBinDir = normalized.contains("/bin/php") || normalized.endsWith("/php");
                if (!preferBinPath || inBinDir) {
                    f.setExecutable(true, false);
                    return f;
                }
            } else {
                continue;
            }
        }
        return null;
    }

    public static boolean isPhpExecutableCandidate(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (!name.equals("php")) {
            return false;
        }
        return looksLikeElf(file);
    }

    private static boolean looksLikeElf(File file) {
        FileInputStream input = null;
        boolean z = false;
        try {
            input = new FileInputStream(file);
            byte[] magic = new byte[4];
            if (input.read(magic) != 4) {
                try {
                    input.close();
                } catch (Exception e) {
                }
                return false;
            }
            if (magic[0] == 127 && magic[1] == 69 && magic[2] == 76 && magic[3] == 70) {
                z = true;
            }
            try {
                input.close();
            } catch (Exception e2) {
            }
            return z;
        } catch (Exception e3) {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e4) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e5) {
                }
            }
            throw th;
        }
    }

    private static int readFully(InputStream input, byte[] target) throws IOException {
        int read;
        int offset = 0;
        while (offset < target.length && (read = input.read(target, offset, target.length - offset)) != -1) {
            offset += read;
        }
        return offset;
    }

    private static boolean isZeroBlock(byte[] block) {
        for (byte b : block) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    private static String parseName(byte[] data, int offset, int length) {
        int end = offset;
        int max = offset + length;
        while (end < max && data[end] != 0) {
            end++;
        }
        return new String(data, offset, end - offset, StandardCharsets.UTF_8).trim();
    }

    private static long parseOctal(byte[] data, int offset, int length) {
        String raw = new String(data, offset, length, StandardCharsets.US_ASCII).trim();
        if (raw.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.replace("\u0000", "").trim(), 8);
        } catch (Exception e) {
            return 0L;
        }
    }

    private static void copyLimited(InputStream input, FileOutputStream output, long size) throws IOException {
        byte[] buffer = new byte[8192];
        long remaining = size;
        while (remaining > 0) {
            int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read == -1) {
                throw new IOException("Unexpected EOF");
            }
            output.write(buffer, 0, read);
            remaining -= (long) read;
        }
    }

    private static void skipFully(InputStream input, long size) throws IOException {
        long remaining = size;
        while (remaining > 0) {
            long skipped = input.skip(remaining);
            if (skipped <= 0) {
                if (input.read() != -1) {
                    skipped = 1;
                } else {
                    return;
                }
            }
            remaining -= skipped;
        }
    }

    private static void skipPadding(InputStream input, long size) throws IOException {
        long padding = (512 - (size % 512)) % 512;
        skipFully(input, padding);
    }
}
