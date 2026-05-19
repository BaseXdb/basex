package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.*;
import java.time.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Base class for WebSocket tests.
 * Naming note: the JDK client type {@link java.net.http.WebSocket} collides with
 * BaseX's {@link org.basex.http.ws.WebSocket}; we therefore use the fully-qualified
 * name {@code java.net.http.WebSocket} throughout this file.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class WsTest extends HTTPTest {
  /** Module namespace. */
  protected static final String NS = "http://basex.org/modules/ws/test";
  /** Module header. */
  protected static final String HEADER =
    "module  namespace m = '" + NS + "';" + Prop.NL +
    "declare namespace ws = 'http://basex.org/modules/ws';" + Prop.NL;
  /** WebSocket root URL. */
  protected static final String WS_ROOT = HTTP_ROOT.replaceFirst("^http://", "ws://") + "ws";

  /** Module counter. */
  private static int count;

  /**
   * Starts the HTTP server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(HTTP_ROOT, true);
  }

  /**
   * Installs a new module and removes all others.
   * @param functions function declarations
   * @throws Exception exception
   */
  protected static void register(final String functions) throws Exception {
    final String webpath = context.soptions.get(StaticOptions.WEBPATH);
    for(final IOFile f : new IOFile(webpath).children()) assertTrue(f.delete());
    final IOFile module = new IOFile(webpath, NAME + count++ + IO.XQMSUFFIX);
    module.write(HEADER + functions);
    WebModules.get(context).init(false);
  }

  /**
   * Opens a WebSocket connection to the test server.
   * @param path WebSocket path (must start with "/")
   * @param listener listener
   * @return JDK WebSocket client
   * @throws Exception exception
   */
  protected static java.net.http.WebSocket connect(final String path, final Listener listener)
      throws Exception {
    final HttpClient client = HttpClient.newHttpClient();
    return client.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(10)).
        buildAsync(URI.create(WS_ROOT + path), listener).get(10, TimeUnit.SECONDS);
  }

  /**
   * Closes a WebSocket and waits for the close handshake to complete.
   * @param ws JDK WebSocket client
   * @throws Exception exception
   */
  protected static void close(final java.net.http.WebSocket ws) throws Exception {
    ws.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE, "bye").get(5, TimeUnit.SECONDS);
  }

  /**
   * Polls the supplier until it returns a non-null value or a 5 s timeout expires.
   * @param <T> value type
   * @param supplier supplier
   * @return value
   */
  protected static <T> T await(final java.util.function.Supplier<T> supplier) {
    final long end = System.currentTimeMillis() + 5000;
    while(System.currentTimeMillis() < end) {
      final T value = supplier.get();
      if(value != null) return value;
      try {
        Thread.sleep(20);
      } catch(final InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(ex);
      }
    }
    throw new AssertionError("Timeout waiting for value.");
  }

  /**
   * Listener that collects text and binary messages received over a WebSocket.
   */
  protected static final class Listener implements java.net.http.WebSocket.Listener {
    /** Received text messages. */
    public final BlockingQueue<String> texts = new LinkedBlockingQueue<>();
    /** Received binary messages. */
    public final BlockingQueue<byte[]> binaries = new LinkedBlockingQueue<>();
    /** Close status code, or {@code -1} if not closed. */
    public volatile int closeStatus = -1;
    /** Close reason, or {@code null} if not closed. */
    public volatile String closeReason;
    /** Error, or {@code null} if no error occurred. */
    public volatile Throwable error;

    /** Accumulator for text frame parts. */
    private final StringBuilder textBuf = new StringBuilder();
    /** Accumulator for binary frame parts. */
    private ByteArrayOutputStream binBuf = new ByteArrayOutputStream();

    @Override
    public CompletionStage<?> onText(final java.net.http.WebSocket ws,
        final CharSequence data, final boolean last) {
      textBuf.append(data);
      if(last) {
        texts.add(textBuf.toString());
        textBuf.setLength(0);
      }
      ws.request(1);
      return null;
    }

    @Override
    public CompletionStage<?> onBinary(final java.net.http.WebSocket ws,
        final ByteBuffer data, final boolean last) {
      final byte[] chunk = new byte[data.remaining()];
      data.get(chunk);
      binBuf.write(chunk, 0, chunk.length);
      if(last) {
        binaries.add(binBuf.toByteArray());
        binBuf = new ByteArrayOutputStream();
      }
      ws.request(1);
      return null;
    }

    @Override
    public CompletionStage<?> onClose(final java.net.http.WebSocket ws,
        final int code, final String reason) {
      closeStatus = code;
      closeReason = reason;
      return null;
    }

    @Override
    public void onError(final java.net.http.WebSocket ws, final Throwable err) {
      error = err;
    }

    /**
     * Polls for the next text message, failing the test on timeout.
     * @return text message
     */
    public String pollText() {
      try {
        final String msg = texts.poll(5, TimeUnit.SECONDS);
        assertNotNull(msg, "No text message received within timeout.");
        return msg;
      } catch(final InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(ex);
      }
    }

    /**
     * Polls for the next binary message, failing the test on timeout.
     * @return binary message
     */
    public byte[] pollBinary() {
      try {
        final byte[] msg = binaries.poll(5, TimeUnit.SECONDS);
        assertNotNull(msg, "No binary message received within timeout.");
        return msg;
      } catch(final InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(ex);
      }
    }
  }
}
