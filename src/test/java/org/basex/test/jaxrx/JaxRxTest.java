package org.basex.test.jaxrx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.basex.api.jaxrx.JaxRxServer;
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
  final String get(final String query) throws IOException {
    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      return read(conn.getInputStream()).replaceAll("\r?\n *", "");
    } catch(final IOException ex) {
      throw new IOException(read(conn.getErrorStream()));
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified PUT request.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  final String put(final String query) throws IOException {
    final URL url = new URL(ROOT + "/" + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");
    final OutputStream out = new BufferedOutputStream(conn.getOutputStream());
    final InputStream in = new BufferedInputStream(
      new FileInputStream("etc/xml/input.xml"));
    int i;
    while((i = in.read()) != -1) out.write(i);
    in.close();
    out.close();
    try {
      return read(conn.getInputStream()).replaceAll("\r?\n *", "");
    } catch(final IOException ex) {
      throw new IOException(read(conn.getErrorStream()));
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified DELETE request.
   * @param query request
   * @return response code
   * @throws IOException I/O exception
   */
  final String delete(final String query) throws IOException {
    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      conn.setRequestMethod("DELETE");
      return read(conn.getInputStream());
      //return conn.getResponseCode();
    } finally {
      conn.disconnect();
    }
  }
  
  /**
   * Returns a string result from the specified input stream.
   * @param is input stream
   * @return string
   * @throws IOException I/O exception
   */
  final String read(final InputStream is) throws IOException {
    final ByteArrayOutputStream bais = new ByteArrayOutputStream();
    final BufferedInputStream bis = new BufferedInputStream(is);
    int i;
    while((i = bis.read()) != -1) bais.write(i);
    is.close();
    return bais.toString();
  }
}
