package org.basex.http.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.http.*;
import java.net.http.HttpResponse;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API and the GET method.
 *
 * @author BaseX Team 2005-22, BSD License
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
    send(NAME + "/binary", HttpMethod.PUT.name(), new ArrayInput("XXX"),
        MediaType.APPLICATION_OCTET_STREAM, 201);
    assertEquals("<a/>", get(NAME + '/' + NAME + ".xml"));
    assertEquals("XXX", get(NAME + "/binary"));
    delete(NAME, 200);
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test public void input() throws Exception {
    assertEquals("<a/>", get("?query=.&context=<a/>"));
    get("?query=.&context=<", 500);
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

  /**
   * Errors.
   * @throws Exception exception
   */
  @Test public void errors() throws Exception {
    get("?query=(", 500);
    get("?query=()&method=xxx", 400);
  }

  /**
   * Content type.
   * @throws Exception exception
   */
  @Test public void contentType() throws Exception {
    check("?query=1", MediaType.APPLICATION_XML);
    check("?command=info", MediaType.TEXT_PLAIN);

    check("?query=1&method=xml", MediaType.APPLICATION_XML);
    check("?query=1&method=xhtml", MediaType.TEXT_HTML);
    check("?query=1&method=html", MediaType.TEXT_HTML);
    check("?query=1&method=text", MediaType.TEXT_PLAIN);
    check("?query=1&method=json", MediaType.APPLICATION_JSON);

    check("?query=1&media-type=application/octet-stream", MediaType.APPLICATION_OCTET_STREAM);
    check("?query=1&media-type=application/xml", MediaType.APPLICATION_XML);
    check("?query=1&media-type=text/html", MediaType.TEXT_HTML);
    check("?query=1&media-type=xxx", new MediaType("xxx"));
  }

  /**
   * Executes the specified GET request and checks the media type of the response.
   * @param query request
   * @param type expected media type
   * @throws IOException I/O exception
   */
  private static void check(final String query, final MediaType type)
      throws IOException {
    final HttpHeaders headers = new IOUrl(REST_ROOT + query).response(
        HttpResponse.BodyHandlers.discarding()).headers();
    final MediaType mt = new MediaType(headers.firstValue(HttpText.CONTENT_TYPE).get());
    if(!mt.is(type)) fail("Wrong media type: " + mt + " returned, " + type + " expected.");
  }

  /**
   * Specify options.
   * @throws IOException I/O exception
   */
  @Test public void queryOption() throws IOException {
    assertEquals("2", get("?query=2,delete+node+<a/>&" + MainOptions.MIXUPDATES.name() + "=true"));
    get("?query=1,delete+node+<a/>&" + MainOptions.MIXUPDATES.name() + "=false", 500);
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

    get("?run=unknown.abc", 404);

    new IOFile(path, "x.bxs").write("<set option='unknown'>123</set>");
    get("?run=x.bxs", 500);
  }
}
