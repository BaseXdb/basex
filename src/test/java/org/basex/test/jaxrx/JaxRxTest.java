package org.basex.test.jaxrx;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.core.HttpHeaders;

import org.basex.api.jaxrx.JaxRxServer;
import org.basex.util.Token;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sun.misc.BASE64Encoder;

/**
 * This class provides a framework for JAX-RX tests.
 * 
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class JaxRxTest {
  /** Opening result. */
  private static final String WRAP1 = "<jax-rx:results xmlns:jax-rx="
      + "\"http://jax-rx.sourceforge.net\">";
  /** Closing result. */
  private static final String WRAP2 = "</jax-rx:results>";
  /** Root path. */
  private static final String ROOT = "http://localhost:8984/basex/jax-rx";
  /** JAX-RX server. */
  private static JaxRxServer jaxrx;

  // INITIALIZERS =============================================================

  /** Start server. */
  @BeforeClass
  public static void start() {
    jaxrx = new JaxRxServer("-Uadmin -Padmin -z");
  }

  /** Stop server. */
  @AfterClass
  public static void stop() {
    jaxrx.stop();
  }

  // TEST METHODS =============================================================

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get() throws Exception {
    assertEquals("123", get("?query=1+to+3&wrap=no"));
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get2() throws Exception {
    assertEquals(WRAP1 + WRAP2, get("?query=()"));
  }

  /**
   * GET Test.
   * @throws IOException I/O exception
   */
  @Test
  public void getBind() throws IOException {
    assertEquals("123", get("?wrap=no&"
        + "query=declare+variable+$x+as+xs:integer+external;$x&var=$x=123"));
  }

  /**
   * GET Test.
   * @throws IOException I/O exception
   */
  @Test
  public void getBind2() throws IOException {
    assertEquals("124", get("?wrap=no&var=x=123&"
        + "query=declare+variable+$x+as+xs:integer+external;$x%2b1"));
  }

  /**
   * GET Test.
   * @throws IOException I/O exception
   */
  @Test
  public void getBind3() throws IOException {
    assertEquals("6", get("?wrap=no&"
        + "query=declare+variable+$a++as+xs:integer+external;"
        + "declare+variable+$b+as+xs:integer+external;"
        + "declare+variable+$c+as+xs:integer+external;" + "$a*$b*$c&"
        + "var=a=1&var=b=2&var=c=3"));
  }

  /** GET Test. */
  @Test
  public void getErr1() {
    try {
      get("?query=(");
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XPST0003]");
    }
  }

  /** GET Test. */
  @Test
  public void getErr2() {
    try {
      get("?query=()&output=wrp=no");
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[SEPM0016]");
    }
  }

  /** GET Test. */
  @Test
  public void getErr3() {
    try {
      get("?query=()&wrap=n");
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[SEPM0016]");
    }
  }

  /** GET Test. */
  @Test
  public void getErr4() {
    try {
      get("?query=()&output=wrap=no");
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[SEPM0016]");
    }
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery1() throws IOException {
    assertEquals(
        "123",
        postQuery("",
            "<query><text>123</text><parameter name='wrap' value='no'/></query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery2() throws IOException {
    assertEquals(
        "",
        postQuery("",
            "<query><text>()</text><parameter name='wrap' value='no'/></query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery3() throws IOException {
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>123",
        postQuery(
            "",
            "<query><text>123</text><parameter name='wrap' value='no'/>"
                + "<parameter name='output' value='omit-xml-declaration=no'/></query>"));
  }

  /**
   * POST Test: execute a query and ignore/overwrite duplicates declarations.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery4() throws IOException {
    assertEquals("<html></html>", postQuery("", "<query>"
        + "<text><![CDATA[<html/>]]></text>"
        + "<parameter name='wrap' value='no'/>"
        + "<parameter name='wrap' value='yes'/>"
        + "<parameter name='output' value='omit-xml-declaration=no'/>"
        + "<parameter name='output' value='omit-xml-declaration=yes'/>"
        + "<output name='omit-xml-declaration' value='no'/>"
        + "<output name='omit-xml-declaration' value='yes'/>"
        + "<output name='method' value='xhtml'/>" + "</query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery5() throws IOException {
    assertEquals("123", postQuery("", "<query>" + "<text>123</text>"
        + "<parameter name='wrap' value='no'/>"
        + "<parameter name='output' value='omit-xml-declaration=no'/>"
        + "<parameter name='output' value='omit-xml-declaration=yes'/>"
        + "</query>"));
  }

  /** POST Test: execute buggy query. */
  @Test
  public void postQueryErr() {
    try {
      assertEquals("", postQuery("", "<query><text>(</text></query>"));
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XPST0003]");
    }
  }

  /**
   * POST Test: create and add database.
   * @throws IOException I/O exception
   */
  @Test
  public void post1() throws IOException {
    put("/jax-rx", null);
    post("/jax-rx", stream("<a>A</a>"));
    assertEquals("1", get("/jax-rx?query=count(/)&wrap=no"));
    delete("/jax-rx");
  }

  /**
   * PUT Test: create empty database.
   * @throws IOException I/O exception
   */
  @Test
  public void put0() throws IOException {
    put("/jax-rx", null);
    assertEquals("0", get("/jax-rx?query=count(/)&wrap=no"));
    delete("/jax-rx");
  }

  /**
   * PUT Test: create simple database.
   * @throws IOException I/O exception
   */
  @Test
  public void put1() throws IOException {
    put("/jax-rx", stream("<a>A</a>"));
    assertEquals("A", get("/jax-rx?query=/*/text()&wrap=no"));
    delete("/jax-rx");
  }

  /**
   * PUT Test: create and overwrite database.
   * @throws IOException I/O exception
   */
  @Test
  public void put2() throws IOException {
    put("/jax-rx", new FileInputStream("etc/xml/input.xml"));
    put("/jax-rx", new FileInputStream("etc/xml/input.xml"));
    assertEquals("XML", get("/jax-rx?query=//title/text()&wrap=no"));
    delete("/jax-rx");
  }

  /**
   * PUT Test: create two documents in a database.
   * @throws IOException I/O exception
   */
  @Test
  public void put3() throws IOException {
    put("/jax-rx", null);
    put("/jax-rx/a", stream("<a>A</a>"));
    put("/jax-rx/b", stream("<b>B</b>"));
    assertEquals("2", get("/jax-rx?query=count(//text())&wrap=no"));
    assertEquals("2", get("?query=count(db:open('jax-rx')//text())&wrap=no"));
    assertEquals("1", get("?query=count(db:open('jax-rx/b')/*)&wrap=no"));
    delete("/jax-rx");
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete1() throws IOException {
    put("/jax-rx", new FileInputStream("etc/xml/input.xml"));
    // delete database
    assertContains(delete("/jax-rx"), "Database '");
    // no database left
    assertContains(delete("/jax-rx"), "No database");
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete2() throws IOException {
    put("/jax-rx", null);
    post("/jax-rx/a", stream("<a/>"));
    post("/jax-rx/a", stream("<a/>"));
    post("/jax-rx/b", stream("<b/>"));
    // delete 'a' directory
    assertContains(delete("/jax-rx/a"), "2 document");
    // delete 'b' directory
    assertContains(delete("/jax-rx/b"), "1 document");
    // no 'b' directory left
    assertContains(delete("/jax-rx/b"), "0 document");
    // delete database
    assertContains(delete("/jax-rx"), "Database '");
    // no database left
    assertContains(delete("/jax-rx"), "No database");
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Checks if a string is contained in another string.
   * @param str string
   * @param sub sub string
   */
  private void assertContains(final String str, final String sub) {
    if(!str.contains(sub)) {
      fail("'" + sub + "' not contained in '" + str + "'.");
    }
  }

  /**
   * Executes the specified GET request.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String get(final String query) throws IOException {
    final URL url = new URL(ROOT + query);

    // create connection
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
   * @param path path
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String postQuery(final String path, final String query)
      throws IOException {
    final URL url = new URL(ROOT + path);

    // create connection
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/query+xml");
    // basic authentication example
    BASE64Encoder enc = new BASE64Encoder();
    String user = "admin";
    String pw ="admin";
    String userpw = user+":"+pw;
    String encoded = enc.encode(userpw.getBytes());
    conn.setRequestProperty("Authorization", "Basic "+encoded);
    // send query
    final OutputStream out = conn.getOutputStream();
    out.write(Token.token(query));
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
   * Executes the specified PUT request.
   * @param query request
   * @param is input stream
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String put(final String query, final InputStream is)
      throws IOException {

    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");
    final OutputStream bos = new BufferedOutputStream(conn.getOutputStream());
    if(is != null) {
      // send input stream if it not empty
      final BufferedInputStream bis = new BufferedInputStream(is);
      int i;
      while((i = bis.read()) != -1)
        bos.write(i);
      bis.close();
      bos.close();
    }
    try {
      return read(conn.getInputStream());
    } catch(final IOException ex) {
      throw new IOException(read(conn.getErrorStream()));
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified PUT request.
   * @param query request
   * @param is input stream
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String post(final String query, final InputStream is)
      throws IOException {

    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/xml");
    final OutputStream bos = new BufferedOutputStream(conn.getOutputStream());
    final BufferedInputStream bis = new BufferedInputStream(is);
    int i;
    while((i = bis.read()) != -1)
      bos.write(i);
    bis.close();
    bos.close();
    try {
      return read(conn.getInputStream());
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
  private String delete(final String query) throws IOException {
    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      conn.setRequestMethod("DELETE");
      return read(conn.getInputStream());
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
  private String read(final InputStream is) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final BufferedInputStream bis = new BufferedInputStream(is);
    int i;
    while((i = bis.read()) != -1)
      baos.write(i);
    bis.close();
    return baos.toString();
  }

  /**
   * Creates a byte input stream for the specified string.
   * @param str string
   * @return stream
   */
  private InputStream stream(final String str) {
    return new ByteArrayInputStream(Token.token(str));
  }
}
