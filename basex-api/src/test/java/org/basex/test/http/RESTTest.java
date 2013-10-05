package org.basex.test.http;

import static org.basex.io.MimeTypes.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.basex.core.*;
import org.basex.http.rest.*;
import org.basex.io.in.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the embedded REST API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTTest extends HTTPTest {
  /** REST identifier. */
  private static final String REST = "rest";
  /** Root path. */
  protected static final String ROOT = "http://" + Text.LOCALHOST + ":9998/" + REST + '/';

  /** REST URI. */
  private static final String URI = RESTText.RESTURI;
  /** Opening result. */
  private static final String WRAP =
      '<' + REST + ":results xmlns:" + REST + "=\"" + URI + "\"/>";
  /** Input file. */
  private static final String FILE = "src/test/resources/input.xml";

  // INITIALIZERS =============================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(ROOT, true);
  }

  // TEST METHODS =============================================================

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get0() throws Exception {
    assertEquals("1", get("?query=1"));
  }

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
    put(ROOT + NAME, new ArrayInput("<a/>"));
    put(ROOT + NAME + "/raw", new ArrayInput("XXX"), APP_OCTET);
    assertEquals("<a/>", get(NAME + '/' + NAME + ".xml"));
    assertEquals("XXX", get(NAME + "/raw"));
    delete(NAME);
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
    assertEquals("123", get('?'
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
    assertStartsWith(contentType("?query=1"), APP_XML);
    assertStartsWith(contentType("?command=info"), TEXT_PLAIN);

    assertStartsWith(contentType("?query=1&method=xml"), APP_XML);
    assertStartsWith(contentType("?query=1&method=xhtml"), TEXT_HTML);
    assertStartsWith(contentType("?query=1&method=html"), TEXT_HTML);
    assertStartsWith(contentType("?query=1&method=text"), TEXT_PLAIN);
    assertStartsWith(contentType("?query=1&method=raw"), APP_OCTET);
    assertStartsWith(contentType("?query=<json+type='object'/>&method=json"), APP_JSON);

    assertStartsWith(contentType("?query=1&media-type=application/xml"), APP_XML);
    assertStartsWith(contentType("?query=1&media-type=text/html"), TEXT_HTML);
    assertStartsWith(contentType("?query=1&media-type=xxx"), "xxx");
  }

  /**
   * Compares two byte arrays for equality.
   * @param string full string
   * @param prefix prefix
   */
  private static void assertStartsWith(final String string, final String prefix) {
    assertTrue("'" + string + "' does not start with '" + prefix + "'",
        string.startsWith(prefix));
  }

  /**
   * GET Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test
  public void getOption() throws IOException {
    assertEquals("2",
        get("?query=switch(1)+case+1+return+2+default+return+3&" +
        Prop.XQUERY3[0] + "=true")
    );
    try {
      get("?query=switch(1)+case+1+return+2+default+return+3&" +
          Prop.XQUERY3[0] + "=false");
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XPST0003]");
    }
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void post1() throws IOException {
    assertEquals("123",
        post("", "<query xmlns=\"" + URI + "\">" +
          "<text>123</text><parameter name='wrap' value='no'/></query>", APP_XML));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void post2() throws IOException {
    assertEquals("",
        post("", "<query xmlns=\"" + URI + "\">" +
          "<text>()</text><parameter name='wrap' value='no'/></query>", APP_XML));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void post3() throws IOException {
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>123",
        post("", "<query xmlns=\"" + URI + "\">" +
          "<text>123</text><parameter name='wrap' value='no'/>" +
          "<parameter name='omit-xml-declaration' value='no'/></query>", APP_XML));
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
        "<parameter name='method' value='xhtml'/>" + "</query>", APP_XML));
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
        "</query>", APP_XML));
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
        "</query>", APP_XML));
  }

  /**
   * POST Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test
  public void postOption() throws IOException {
    assertEquals("2", post("", "<query xmlns=\"" + URI + "\">" +
        "<text>switch(1) case 1 return 2 default return 3</text>" +
        "<option name='" + Prop.XQUERY3[0] + "' value='true'/></query>", APP_XML));

    try {
      post("", "<query xmlns=\"" + URI + "\">" +
        "<text>switch(1) case 1 return 2 default return 3</text>" +
        "<option name='" + Prop.XQUERY3[0] + "' value='false'/></query>", APP_XML);
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XPST0003]");
    }
  }

  /** POST Test: execute buggy query. */
  @Test
  public void postErr() {
    try {
      assertEquals("", post("", "<query xmlns=\"" + URI + "\"><text>(</text></query>",
          APP_XML));
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
    put(ROOT + NAME, null);
    assertEquals("0", get(NAME + "?query=count(/)"));
    delete(NAME);
  }

  /**
   * PUT Test: create simple database.
   * @throws IOException I/O exception
   */
  @Test
  public void put2() throws IOException {
    put(ROOT + NAME, new ArrayInput(token("<a>A</a>")));
    assertEquals("A", get(NAME + "?query=/*/text()"));
    delete(NAME);
  }

  /**
   * PUT Test: create and overwrite database.
   * @throws IOException I/O exception
   */
  @Test
  public void put3() throws IOException {
    put(ROOT + NAME, new FileInputStream(FILE));
    put(ROOT + NAME, new FileInputStream(FILE));
    assertEquals("XML", get(NAME + "?query=//title/text()"));
    delete(NAME);
  }

  /**
   * PUT Test: create two documents in a database.
   * @throws IOException I/O exception
   */
  @Test
  public void put4() throws IOException {
    put(ROOT + NAME, null);
    put(ROOT + NAME + "/a", new ArrayInput(token("<a>A</a>")));
    put(ROOT + NAME + "/b", new ArrayInput(token("<b>B</b>")));
    assertEquals("2", get(NAME + "?query=count(//text())"));
    assertEquals("2", get("?query=count(" + Function._DB_OPEN.args(NAME) + "//text())"));
    assertEquals("1", get("?query=count(" + Function._DB_OPEN.args(NAME, "b") + "/*)"));
    delete(NAME);
  }

  /**
   * PUT Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test
  public void putOption() throws IOException {
    put(ROOT + NAME + "?" + Prop.CHOP[0] + "=true", new FileInputStream(FILE));
    assertEquals("5", get(NAME + "?query=count(//text())"));
    put(ROOT + NAME + "?" + Prop.CHOP[0] + "=false", new FileInputStream(FILE));
    assertEquals("22", get(NAME + "?query=count(//text())"));

    try {
      put(ROOT + NAME + "?xxx=yyy", new FileInputStream(FILE));
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete1() throws IOException {
    put(ROOT + NAME, new FileInputStream(FILE));
    // delete database
    assertEquals(delete(NAME).trim(), Util.info(Text.DB_DROPPED_X, NAME));
    try {
      // no database left
      delete(NAME);
      fail("Error expected.");
    } catch(final BaseXException ex) {
    }
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete2() throws IOException {
    put(ROOT + NAME, null);
    put(ROOT + NAME + "/a", new ArrayInput(token("<a/>")));
    put(ROOT + NAME + "/b", new ArrayInput(token("<b/>")));
    // delete 'a' directory
    assertStartsWith(delete(NAME + "/a"), "1 ");
    // delete 'b' directory
    assertStartsWith(delete(NAME + "/b"), "1 ");
    // no 'b' directory left
    assertStartsWith(delete(NAME + "/b"), "0 ");
    // delete database
    assertEquals(delete(NAME).trim(), Util.info(Text.DB_DROPPED_X, NAME));
    try {
      // no database left
      delete(NAME);
      fail("Error expected.");
    } catch(final BaseXException ex) {
    }
  }

  /**
   * DELETE Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test
  public void deleteOption() throws IOException {
    put(ROOT + NAME, null);
    delete(NAME + "/a?" + Prop.CHOP[0] + "=true");
    try {
      delete(NAME + "/a?xxx=true");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Checks if a string is contained in another string.
   * @param str string
   * @param sub sub string
   */
  private static void assertContains(final String str, final String sub) {
    if(!str.contains(sub)) fail('\'' + sub + "' not contained in '" + str + "'.");
  }

  /**
   * Executes the specified GET request and returns the content type.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private static String contentType(final String query) throws IOException {
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
}
