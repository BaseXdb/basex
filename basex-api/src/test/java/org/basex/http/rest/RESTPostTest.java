package org.basex.http.rest;

import static org.basex.io.MimeTypes.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.junit.*;

/**
 * This class tests the embedded REST API and the POST method.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RESTPostTest extends RESTTest {
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
        "<text>2, delete node &lt;a/&gt;</text>" +
        "<option name='" + MainOptions.MIXUPDATES.name() + "' value='true'/></query>", APP_XML));

    try {
      post("", "<query xmlns=\"" + URI + "\">" +
          "<text>1, delete node &lt;a/&gt;</text>" +
        "<option name='" + MainOptions.MIXUPDATES.name() + "' value='false'/></query>", APP_XML);
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XUST0001]");
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
}
