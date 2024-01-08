package org.basex.http.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API and the POST method.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RESTPostTest extends RESTTest {
  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test public void post1() throws IOException {
    assertEquals("123", post("<query><text>123</text></query>", MediaType.APPLICATION_XML, ""));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test public void post2() throws IOException {
    assertEquals("", post("<query xmlns=\"" + URI + "\"><text>()</text></query>",
        MediaType.APPLICATION_XML, ""));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test public void post3() throws IOException {
    assertEquals("1", post("<query xmlns=\"" + URI + "\">"
        + "<text><![CDATA[<a>1</a>]]></text><parameter name='method' value='text'/></query>",
        MediaType.APPLICATION_XML, ""));
  }

  /**
   * POST Test: execute a query and ignore/overwrite duplicates declarations.
   * @throws IOException I/O exception
   */
  @Test public void post4() throws IOException {
    assertEquals("<html></html>", post("<query xmlns=\"" + URI + "\">"
        + "<text><![CDATA[<html/>]]></text>"
        + "<parameter name='omit-xml-declaration' value='no'/>"
        + "<parameter name='omit-xml-declaration' value='yes'/>"
        + "<parameter name='method' value='xhtml'/>" + "</query>",
        MediaType.APPLICATION_XML, ""));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test public void post5() throws IOException {
    assertEquals("123", post("<query xmlns=\"" + URI + "\">"
        + "<text>123</text>"
        + "<parameter name='omit-xml-declaration' value='no'/>"
        + "<parameter name='omit-xml-declaration' value='yes'/>"
        + "</query>",
        MediaType.APPLICATION_XML, ""));
  }

  /**
   * POST Test: execute a query with an initial context.
   * @throws IOException I/O exception
   */
  @Test public void postContext() throws IOException {
    assertEquals("<a/>", post("<query xmlns=\"" + URI + "\">"
        + "<text>.</text>"
        + "<context><a/></context>"
        + "</query>",
        MediaType.APPLICATION_XML, ""));
  }

  /**
   * POST Test: execute a query with variables.
   * @throws IOException I/O exception
   */
  @Test public void postVariable() throws IOException {
    assertEquals("123", post("<query xmlns=\"" + URI + "\">"
        + "<text>declare variable $a as xs:integer external; $a</text>"
        + "<variable name='a' value='123'/>"
        + "</query>",
        MediaType.APPLICATION_XML, ""));

    assertEquals("1000", post("<query xmlns=\"" + URI + "\">"
        + "<text>declare variable $a as xs:integer* external; sum($a)</text>"
        + "<variable name='a' value='123'/>"
        + "<variable name='a' value='877'/>"
        + "</query>",
        MediaType.APPLICATION_XML, ""));
  }

  /**
   * POST Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test public void postOption() throws IOException {
    assertEquals("2", post("<query>"
        + "<text>2, delete node &lt;a/&gt;</text>"
        + "<option name='" + MainOptions.MIXUPDATES.name() + "' value='true'/></query>",
        MediaType.APPLICATION_XML, ""));

    post(500, "<query xmlns=\"" + URI + "\">"
        + "<text>1, delete node &lt;a/&gt;</text>"
        + "<option name='" + MainOptions.MIXUPDATES.name() + "' value='false'/></query>",
      MediaType.APPLICATION_XML, "");
  }

  /**
   * POST Test: execute a script.
   * @throws IOException I/O exception
   */
  @Test public void postScript() throws IOException {
    assertEquals("1", post("<commands><xquery>1</xquery></commands>",
        MediaType.APPLICATION_XML, ""));
    assertEquals("12", post("<commands><xquery>1</xquery><xquery>2</xquery></commands>",
        MediaType.APPLICATION_XML, ""));
  }

  /**
   * POST Test: execute buggy query.
   * @throws IOException I/O exception
   */
  @Test public void postErr() throws IOException {
    post(500, "<query xmlns=\"" + URI + "\"><text>(</text></query>",
        MediaType.APPLICATION_XML, "");
  }
}
