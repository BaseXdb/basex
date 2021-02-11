package org.basex.http.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API and the GET method.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RESTGetTest extends RESTTest {
  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test public void basic() throws Exception {
    assertEquals("1", get("?query=1"));
    assertEquals("a,b", get("?query=string-join(('a','b'),',')"));

    put(NAME, new ArrayInput("<a/>"));
    put(NAME + "/raw", new ArrayInput("XXX"), MediaType.APPLICATION_OCTET_STREAM);
    assertEquals("<a/>", get(NAME + '/' + NAME + ".xml"));
    assertEquals("XXX", get(NAME + "/raw"));
    delete(NAME);
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test public void input() throws Exception {
    assertEquals("<a/>", get("?query=.&context=<a/>"));

    try {
      assertEquals("<a/>", get("?query=.&context=<"));
      fail("Error expected.");
    } catch(final IOException ex) {
      // expected
    }
  }

  /**
   * Binding.
   * @throws IOException I/O exception
   */
  @Test public void bind() throws IOException {
    assertEquals("123", get('?'
        + "query=declare+variable+$x+as+xs:integer+external;$x&$x=123"));
    assertEquals("124", get("?$x=123&"
        + "query=declare+variable+$x+as+xs:integer+external;$x%2b1"));
    assertEquals("6", get('?'
        + "query=declare+variable+$a++as+xs:integer+external;"
        + "declare+variable+$b+as+xs:integer+external;"
        + "declare+variable+$c+as+xs:integer+external;$a*$b*$c&$a=1&$b=2&$c=3"));
  }

  /** Error. */
  @Test public void error1() {
    try {
      get("?query=(");
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XPST0003]");
    }
  }

  /** Error. */
  @Test public void error2() {
    try {
      get("?query=()&method=xxx");
      fail("Error expected.");
    } catch(final IOException ignored) {
    }
  }

  /**
   * Content type.
   * @throws Exception exception
   */
  @Test public void contentType() throws Exception {
    assertMediaType(mediaType("?query=1"), MediaType.APPLICATION_XML);
    assertMediaType(mediaType("?command=info"), MediaType.TEXT_PLAIN);

    assertMediaType(mediaType("?query=1&method=xml"), MediaType.APPLICATION_XML);
    assertMediaType(mediaType("?query=1&method=xhtml"), MediaType.TEXT_HTML);
    assertMediaType(mediaType("?query=1&method=html"), MediaType.TEXT_HTML);
    assertMediaType(mediaType("?query=1&method=text"), MediaType.TEXT_PLAIN);
    assertMediaType(mediaType("?query=<json+type='object'/>&method=json"),
        MediaType.APPLICATION_JSON);

    assertMediaType(mediaType("?query=1&media-type=application/octet-stream"),
        MediaType.APPLICATION_OCTET_STREAM);
    assertMediaType(mediaType("?query=1&media-type=application/xml"), MediaType.APPLICATION_XML);
    assertMediaType(mediaType("?query=1&media-type=text/html"), MediaType.TEXT_HTML);
    assertMediaType(mediaType("?query=1&media-type=xxx"), new MediaType("xxx"));
  }

  /**
   * Specify options.
   * @throws IOException I/O exception
   */
  @Test public void queryOption() throws IOException {
    assertEquals("2", get("?query=2,delete+node+<a/>&" + MainOptions.MIXUPDATES.name() + "=true"));
    try {
      get("?query=1,delete+node+<a/>&" + MainOptions.MIXUPDATES.name() + "=false");
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XUST0001]");
    }
  }

  /**
   * Specify a server file.
   * @throws IOException I/O exception
   */
  @Test public void runOption() throws IOException {
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    new IOFile(path, "x.xq").write("1");
    assertEquals("1", get("?run=x.xq"));

    new IOFile(path, "x.bxs").write("xquery 2");
    assertEquals("2", get("?run=x.bxs"));

    new IOFile(path, "x.bxs").write("xquery 3\nxquery 4");
    assertEquals("34", get("?run=x.bxs"));

    new IOFile(path, "x.bxs").write("<commands><xquery>5</xquery></commands>");
    assertEquals("5", get("?run=x.bxs"));

    new IOFile(path, "x.bxs").write("<set option='maxlen'>123</set>");
    assertEquals("", get("?run=x.bxs"));

    try {
      get("?run=unknown.abc");
      fail("Error expected.");
    } catch(final IOException ignored) {
    }

    try {
      new IOFile(path, "x.bxs").write("<set option='unknown'>123</set>");
      assertEquals("", get("?run=x.bxs"));
      fail("Error expected.");
    } catch(final IOException ignored) {
    }
  }
}
