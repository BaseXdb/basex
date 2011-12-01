package org.basex.test.rest;

import static org.basex.api.HTTPText.*;
import static org.basex.core.Text.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.basex.api.BaseXHTTP;
import org.basex.api.rest.RESTText;
import org.basex.core.BaseXException;
import org.basex.data.DataText;
import org.basex.io.in.ArrayInput;
import org.basex.io.in.BufferInput;
import org.basex.io.out.ArrayOutput;
import org.basex.util.Base64;
import org.basex.util.Util;
import org.basex.util.list.StringList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the embedded REST implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class RESTTest {
  /** Test database. */
  private static final String DB = Util.name(RESTTest.class);
  /** REST identified. */
  private static final String NAME = "rest";
  /** REST URI. */
  private static final String URI = string(RESTText.RESTURI);
  /** Opening result. */
  private static final String WRAP =
      "<" + NAME + ":results xmlns:" + NAME + "=\"" + URI + "\"/>";
  /** Root path. */
  private static final String ROOT =
      "http://" + LOCALHOST + ":9998/" + NAME + "/";
  /** Input file. */
  private static final String FILE = "etc/test/input.xml";
  /** Start servers. */
  private static BaseXHTTP http;

  // INITIALIZERS =============================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(true);
  }

  /**
   * Initializes the test.
   * @param local local flag
   * @throws Exception exception
   */
  protected static void init(final boolean local) throws Exception {
    final StringList sl = new StringList();
    if(local) sl.add("-l");
    sl.add(new String[] {"-p9996", "-e9997", "-h9998", "-s9999", "-z",
        "-U" + ADMIN, "-P" + ADMIN });
    http = new BaseXHTTP(sl.toArray());
  }

  /**
   * Finish test.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    http.stop();
  }

  // TEST METHODS =============================================================

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get() throws Exception {
    assertEquals("1 2 3", get("?query=1+to+3&wrap=no"));
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get2() throws Exception {
    assertEquals(WRAP, get("?query=()&wrap=yes"));
  }

  /**
   * GET Test: returns a resource.
   * @throws IOException I/O exception
   */
  @Test
  public void get3() throws IOException {
    put(DB, new ArrayInput("<a/>"));
    put(DB + "/raw", new ArrayInput("XXX"), APP_OCTET);
    assertEquals("<a/>", get(DB + '/' + DB + ".xml"));
    assertEquals("XXX", get(DB + "/raw"));
    delete(DB);
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void getInput() throws Exception {
    assertEquals("<a/>", get("?query=.&context=<a/>"));
  }

  /**
   * GET Test.
   */
  @Test
  public void getInputError() {
    try {
      assertEquals("<a/>", get("?query=.&context=<"));
      fail("Error expected.");
    } catch(final IOException ex) {
      /** expected. */
    }
  }

  /**
   * GET Test.
   * @throws IOException I/O exception
   */
  @Test
  public void getBind() throws IOException {
    assertEquals("123", get("?"
        + "query=declare+variable+$x+as+xs:integer+external;$x&$x=123"));
  }

  /**
   * GET Test.
   * @throws IOException I/O exception
   */
  @Test
  public void getBind2() throws IOException {
    assertEquals("124", get("?wrap=no&$x=123&"
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
        + "$a=1&$b=2&$c=3"));
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
      get("?query=()&wrap=n");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /** GET Test. */
  @Test
  public void getErr3() {
    try {
      get("?query=()&method=xxx");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /**
   * GET content types.
   * @throws Exception exception
   */
  @Test
  public void getContentType() throws Exception {
    assertEquals(APP_XML, ct("?query=1"));
    assertEquals(TEXT_PLAIN, ct("?command=info"));

    assertEquals(APP_XML, ct("?query=1&method=xml"));
    assertEquals(TEXT_HTML, ct("?query=1&method=xhtml"));
    assertEquals(TEXT_HTML, ct("?query=1&method=html"));
    assertEquals(TEXT_PLAIN, ct("?query=1&method=text"));
    assertEquals(APP_OCTET, ct("?query=1&method=raw"));
    assertEquals(APP_JSON, ct("?query=<json+type='object'/>&method=json"));
    assertEquals(APP_JSON, ct("?query=<json/>&method=jsonml"));

    assertEquals(APP_XML, ct("?query=1&media-type=application/xml"));
    assertEquals(TEXT_HTML, ct("?query=1&media-type=text/html"));
    assertEquals("xxx", ct("?query=1&media-type=xxx"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void post1() throws IOException {
    assertEquals("123",
        post("", "<query xmlns=\"" + URI + "\">" +
          "<text>123</text><parameter name='wrap' value='no'/></query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void post2() throws IOException {
    assertEquals("",
        post("", "<query xmlns=\"" + URI + "\">" +
          "<text>()</text><parameter name='wrap' value='no'/></query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void post3() throws IOException {
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?> 123",
        post("", "<query xmlns=\"" + URI + "\">" +
          "<text>123</text><parameter name='wrap' value='no'/>" +
          "<parameter name='omit-xml-declaration' value='no'/></query>"));
  }

  /**
   * POST Test: execute a query and ignore/overwrite duplicates declarations.
   * @throws IOException I/O exception
   */
  @Test
  public void post4() throws IOException {
    assertEquals("<html></html>",
        post("", "<query xmlns=\"" + URI + "\">" +
        "<text><![CDATA[<html/>]]></text>" +
        "<parameter name='wrap' value='yes'/>" +
        "<parameter name='wrap' value='no'/>" +
        "<parameter name='omit-xml-declaration' value='no'/>" +
        "<parameter name='omit-xml-declaration' value='yes'/>" +
        "<parameter name='method' value='xhtml'/>" + "</query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void post5() throws IOException {
    assertEquals("123", post("",
        "<query xmlns=\"" + URI + "\">" +
        "<text>123</text>" +
        "<parameter name='wrap' value='no'/>" +
        "<parameter name='omit-xml-declaration' value='no'/>" +
        "<parameter name='omit-xml-declaration' value='yes'/>" +
        "</query>"));
  }

  /**
   * POST Test: execute a query with an initial context.
   * @throws IOException I/O exception
   */
  @Test
  public void post6() throws IOException {
    assertEquals("<a/>", post("",
        "<query xmlns=\"" + URI + "\">" +
        "<text>.</text>" +
        "<context><a/></context>" +
        "</query>"));
  }

  /** POST Test: execute buggy query. */
  @Test
  public void postErr() {
    try {
      assertEquals("", post("",
          "<query xmlns=\"" + URI + "\"><text>(</text></query>"));
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XPST0003]");
    }
  }

  /**
   * PUT Test: create empty database.
   * @throws IOException I/O exception
   */
  @Test
  public void put1() throws IOException {
    put(DB, null);
    assertEquals("0", get(DB + "?query=count(/)"));
    delete(DB);
  }

  /**
   * PUT Test: create simple database.
   * @throws IOException I/O exception
   */
  @Test
  public void put2() throws IOException {
    put(DB, new ArrayInput(token("<a>A</a>")));
    assertEquals("A", get(DB + "?query=/*/text()"));
    delete(DB);
  }

  /**
   * PUT Test: create and overwrite database.
   * @throws IOException I/O exception
   */
  @Test
  public void put3() throws IOException {
    put(DB, new FileInputStream(FILE));
    put(DB, new FileInputStream(FILE));
    assertEquals("XML", get(DB + "?query=//title/text()"));
    delete(DB);
  }

  /**
   * PUT Test: create two documents in a database.
   * @throws IOException I/O exception
   */
  @Test
  public void put4() throws IOException {
    put(DB, null);
    put(DB + "/a", new ArrayInput(token("<a>A</a>")));
    put(DB + "/b", new ArrayInput(token("<b>B</b>")));
    assertEquals("2", get(DB + "?query=count(//text())"));
    assertEquals("2", get("?query=count(" + _DB_OPEN.args(DB) + "//text())"));
    assertEquals("1", get("?query=count(" + _DB_OPEN.args(DB, "b") + "/*)"));
    delete(DB);
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete1() throws IOException {
    put(DB, new FileInputStream(FILE));
    // delete database
    assertEquals(delete(DB).trim(), Util.info(DBDROPPED, DB));
    try {
      // no database left
      delete(DB);
      fail("Error expected.");
    } catch(final FileNotFoundException ex) {
    }
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete2() throws IOException {
    put(DB, null);
    put(DB + "/a", new ArrayInput(token("<a/>")));
    put(DB + "/b", new ArrayInput(token("<b/>")));
    // delete 'a' directory
    assertContains(delete(DB + "/a"), "1 document");
    // delete 'b' directory
    assertContains(delete(DB + "/b"), "1 document");
    // no 'b' directory left
    assertContains(delete(DB + "/b"), "0 document");
    // delete database
    assertEquals(delete(DB).trim(), Util.info(DBDROPPED, DB));
    try {
      // no database left
      delete(DB);
      fail("Error expected.");
    } catch(final FileNotFoundException ex) {
    }
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
   * Executes the specified GET request and returns the result.
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
      throw error(conn, ex);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified GET request and returns the content type.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String ct(final String query) throws IOException {
    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      read(conn.getInputStream());
      return conn.getContentType();
    } catch(final IOException ex) {
      throw error(conn, ex);
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
  private String post(final String path, final String query)
      throws IOException {
    final URL url = new URL(ROOT + path);

    // create connection
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty(DataText.CONTENT_TYPE, APP_XML);
    // basic authentication example
    final String encoded = Base64.encode(ADMIN + ':' + ADMIN);
    conn.setRequestProperty(AUTHORIZATION, BASIC + ' ' + encoded);
    // send query
    final OutputStream out = conn.getOutputStream();
    out.write(token(query));
    out.close();

    try {
      return read(conn.getInputStream()).replaceAll("\r?\n *", "");
    } catch(final IOException ex) {
      throw error(conn, ex);
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
    return put(query, is, null);
  }

  /**
   * Executes the specified PUT request.
   * @param query request
   * @param is input stream
   * @param ctype content type (optional)
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String put(final String query, final InputStream is,
      final String ctype) throws IOException {

    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");
    if(ctype != null) conn.setRequestProperty(DataText.CONTENT_TYPE, ctype);
    final OutputStream bos = new BufferedOutputStream(conn.getOutputStream());
    if(is != null) {
      // send input stream if it not empty
      final BufferedInputStream bis = new BufferedInputStream(is);
      for(int i; (i = bis.read()) != -1;) bos.write(i);
      bis.close();
      bos.close();
    }
    try {
      return read(conn.getInputStream());
    } catch(final IOException ex) {
      throw error(conn, ex);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Returns an exception with improved error message.
   * @param conn connection reference
   * @param ex exception
   * @return exception
   * @throws IOException I/O exception
   */
  private IOException error(final HttpURLConnection conn, final IOException ex)
      throws IOException {
    final String msg = read(conn.getErrorStream());
    throw new BaseXException(msg.isEmpty() ? ex.getMessage() : msg);

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
    final ArrayOutput baos = new ArrayOutput();
    final BufferInput bis = new BufferInput(is);
    for(int i; (i = bis.read()) != -1;) baos.write(i);
    bis.close();
    return baos.toString();
  }
}
