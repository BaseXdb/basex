package org.basex.test.jaxrx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.basex.api.jaxrx.JaxRxServer;
import org.jaxrx.core.JaxRxException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * This class provides a framework for JAX-RX tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class JaxRxTest {
  /** Opening result. */
  protected static final String WRAP1 = "<jax-rx:results xmlns:jax-rx=" +
    "\"http://jax-rx.sourceforge.net\">";
  /** Closing result. */
  protected static final String WRAP2 = "</jax-rx:results>";
  /** Root path. */
  protected static final String ROOT = "http://localhost:8984/basex/jax-rx";
  /** REST server. */
  protected static JaxRxServer jaxrx;

  /** Start server. */
  @BeforeClass
  public static void start() {
    jaxrx = new JaxRxServer("-z");
  }

  /** Stop server. */
  @AfterClass
  public static void stop() {
    jaxrx.stop();
  }

  /**
   * Executes the specified GET request.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  String get(final String query) throws IOException {
    final URL url = new URL(ROOT + "?" + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    final int code = conn.getResponseCode();
    final InputStream is = conn.getInputStream();
    final ByteArrayOutputStream bais = new ByteArrayOutputStream();
    int i;
    while((i = is.read()) != -1) bais.write(i);
    is.close();
    conn.disconnect();
    final String result = bais.toString().replaceAll("\r?\n *", "");
    if(code != HttpURLConnection.HTTP_OK) {
      System.out.println("???????????");
      throw new JaxRxException(code, result);
    }
    return result;
  }
}
