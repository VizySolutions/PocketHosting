package com.vizysolutions.pmmpmobile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/* JADX INFO: loaded from: classes2.dex */
public class LocalHttpTunnelProxy {
    private static LocalHttpTunnelProxy instance;
    private final Thread acceptThread;
    private volatile boolean running = true;
    private final ServerSocket serverSocket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));

    private LocalHttpTunnelProxy() throws Exception {
        Thread thread = new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.LocalHttpTunnelProxy$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                LocalHttpTunnelProxy.this.acceptLoop();
            }
        }, "altay-auth-proxy");
        this.acceptThread = thread;
        thread.setDaemon(true);
        thread.start();
    }

    public static synchronized int ensureStarted() {
        try {
            LocalHttpTunnelProxy localHttpTunnelProxy = instance;
            if (localHttpTunnelProxy == null || !localHttpTunnelProxy.running || instance.serverSocket.isClosed()) {
                instance = new LocalHttpTunnelProxy();
            }
        } catch (Exception e) {
            return -1;
        }
        return instance.getPort();
    }

    public static synchronized void stopProxy() {
        LocalHttpTunnelProxy localHttpTunnelProxy = instance;
        if (localHttpTunnelProxy != null) {
            localHttpTunnelProxy.stop();
            instance = null;
        }
    }

    public int getPort() {
        return this.serverSocket.getLocalPort();
    }

    private void stop() {
        this.running = false;
        try {
            this.serverSocket.close();
        } catch (Exception e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void acceptLoop() {
        while (this.running) {
            try {
                final Socket client = this.serverSocket.accept();
                Thread t = new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.LocalHttpTunnelProxy$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        LocalHttpTunnelProxy.this.m1x1dd5a29e(client);
                    }
                }, "altay-auth-proxy-client");
                t.setDaemon(true);
                t.start();
            } catch (Exception e) {
                if (this.running) {
                    try {
                        Thread.sleep(250L);
                    } catch (InterruptedException e2) {
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: handle, reason: merged with bridge method [inline-methods] */
    public void m1x1dd5a29e(Socket client) {
        Socket remote = null;
        try {
            try {
                client.setTcpNoDelay(true);
                InputStream rawIn = client.getInputStream();
                OutputStream clientOut = client.getOutputStream();
                StringBuilder header = new StringBuilder();
                int matched = 0;
                while (true) {
                    int b = rawIn.read();
                    if (b == -1) {
                        break;
                    }
                    header.append((char) b);
                    if ((matched == 0 && b == 13) || ((matched == 1 && b == 10) || ((matched == 2 && b == 13) || (matched == 3 && b == 10)))) {
                        matched++;
                        if (matched == 4) {
                            break;
                        }
                    } else {
                        matched = 0;
                    }
                    if (header.length() > 16384) {
                        throw new Exception("Proxy header too large");
                    }
                }
                if (header.length() == 0) {
                    try {
                        client.close();
                    } catch (Exception e) {
                    }
                    if (0 != 0) {
                        try {
                            remote.close();
                            return;
                        } catch (Exception e2) {
                            return;
                        }
                    }
                    return;
                }
                String headerText = header.toString();
                String firstLine = headerText.split("\\r?\\n", 2)[0];
                String firstUpper = firstLine.toUpperCase(Locale.ROOT);
                if (firstUpper.startsWith("CONNECT ")) {
                    String target = firstLine.split(" ")[1].trim();
                    String[] hp = target.split(":", 2);
                    String host = hp[0];
                    remote = new Socket(host, hp.length > 1 ? Integer.parseInt(hp[1]) : 443);
                    remote.setTcpNoDelay(true);
                    clientOut.write("HTTP/1.1 200 Connection Established\r\nProxy-Agent: AltayServerAuthBridge\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
                    clientOut.flush();
                    bridge(client, remote);
                } else if (firstUpper.startsWith("GET ") || firstUpper.startsWith("POST ") || firstUpper.startsWith("HEAD ")) {
                    String[] parts = firstLine.split(" ", 3);
                    URL url = new URL(parts[1]);
                    String host2 = url.getHost();
                    int port = url.getPort() > 0 ? url.getPort() : url.getDefaultPort();
                    if (port <= 0) {
                        port = 80;
                    }
                    String path = url.getFile().isEmpty() ? "/" : url.getFile();
                    remote = new Socket(host2, port);
                    remote.setTcpNoDelay(true);
                    StringBuilder sb = new StringBuilder();
                    sb.append(parts[0]);
                    sb.append(" ");
                    sb.append(path);
                    sb.append(" ");
                    sb.append(parts.length > 2 ? parts[2] : "HTTP/1.1");
                    String newFirst = sb.toString();
                    String rewritten = headerText.replaceFirst("^.*?\\r?\\n", newFirst + "\r\n");
                    remote.getOutputStream().write(rewritten.getBytes(StandardCharsets.ISO_8859_1));
                    remote.getOutputStream().flush();
                    bridge(client, remote);
                } else {
                    clientOut.write("HTTP/1.1 405 Method Not Allowed\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
                    clientOut.flush();
                }
                try {
                    client.close();
                } catch (Exception e3) {
                }
                if (remote != null) {
                    remote.close();
                }
            } catch (Exception e4) {
                try {
                    client.close();
                } catch (Exception e5) {
                }
                if (0 != 0) {
                    remote.close();
                }
            } catch (Throwable th) {
                try {
                    client.close();
                } catch (Exception e6) {
                }
                if (0 == 0) {
                    throw th;
                }
                try {
                    remote.close();
                    throw th;
                } catch (Exception e7) {
                    throw th;
                }
            }
        } catch (Exception e8) {
        }
    }

    private void bridge(Socket a, Socket b) throws Exception {
        Thread t1 = pipeThread(a.getInputStream(), b.getOutputStream(), b);
        Thread t2 = pipeThread(b.getInputStream(), a.getOutputStream(), a);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    private Thread pipeThread(final InputStream in, final OutputStream out, final Socket closeWhenDone) {
        Thread t = new Thread(new Runnable() { // from class: com.vizysolutions.pmmpmobile.LocalHttpTunnelProxy$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                LocalHttpTunnelProxy.lambda$pipeThread$1(in, out, closeWhenDone);
            }
        }, "altay-auth-proxy-pipe");
        t.setDaemon(true);
        return t;
    }

    static /* synthetic */ void lambda$pipeThread$1(InputStream in, OutputStream out, Socket closeWhenDone) {
        byte[] buf = new byte[8192];
        while (true) {
            try {
                int n = in.read(buf);
                if (n != -1) {
                    out.write(buf, 0, n);
                    out.flush();
                } else {
                    try {
                        closeWhenDone.shutdownOutput();
                        return;
                    } catch (Exception e) {
                        return;
                    }
                }
            } catch (Exception e2) {
                try {
                    closeWhenDone.shutdownOutput();
                    return;
                } catch (Exception e3) {
                    return;
                }
            } catch (Throwable th) {
                try {
                    closeWhenDone.shutdownOutput();
                } catch (Exception e4) {
                }
                throw th;
            }
        }
    }
}
