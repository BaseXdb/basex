package org.basex.http.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.http.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.func.*;
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
   * Run queries.
   * @throws Exception exception
   */
  @Test public void queries() throws Exception {
    get("1", "", "query", 1);
    get("a,b", "", "query", "string-join(('a','b'),',')");
  }

  /**
   * List databases.
   * @throws Exception exception
   */
  @Test public void databases() throws Exception {
    final String result = get(200, "");
    query(result + " ! name()", "databases");
    query(result + " ! namespace-uri()", "http://basex.org/rest");
    query(result + "/node()", "");

    put(null, NAME);
    query(get(200, "") + "/*/text()", NAME);
    delete(200, NAME);
  }

  /**
   * List resources.
   * @throws Exception exception
   */
  @Test public void resources() throws Exception {
    final String xml = NAME + ".xml", bin = "binary.data", value = "data.xquery";
    put(null, NAME);
    put(new ArrayInput("<a/>"), NAME + '/' + xml);
    send(201, Method.PUT.name(), new ArrayInput("XXX"),
        MediaType.APPLICATION_OCTET_STREAM, NAME + '/' + bin);
    get(200, "", "query", Function._DB_PUT.args(NAME, value, "DATA"));

    query(get(200, NAME) + "/*/text() => sort()", xml + "\n" + bin + "\n" + value);
    get("<a/>", NAME + '/' + xml);
    get("XXX", NAME + '/' + bin);
    get("DATA", NAME + '/' + value);

    delete(200, NAME);
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test public void input() throws Exception {
    get("<a/>", "", "query", ".", "context", "<a/>");
    get(500, "", "query", ".", "context", "<");
  }

  /**
   * Binding.
   * @throws IOException I/O exception
   */
  @Test public void bind() throws IOException {
    get("123", "", "x", 123, "query",
        "declare variable $x as xs:integer external; $x");
    get("124", "", "x", 123, "query",
        "declare variable $x as xs:integer external; $x + 1");
    get("6", "", "a", 1, "b", 2, "c", 3, "query",
        "declare variable $a as xs:integer external;"
        + "declare variable $b as xs:integer external;"
        + "declare variable $c as xs:integer external; $a * $b * $c");
  }

  /**
   * Errors.
   * @throws Exception exception
   */
  @Test public void errors() throws Exception {
    get(500, "", "query", "(");
    get(400, "", "query", "()", "method", "xxx");
  }

  /**
   * Content type.
   * @throws Exception exception
   */
  @Test public void contentType() throws Exception {
    check(MediaType.APPLICATION_XML, "?query=1");
    check(MediaType.TEXT_PLAIN, "?command=info");

    check(MediaType.APPLICATION_XML, "?query=1&method=xml");
    check(MediaType.TEXT_HTML, "?query=1&method=xhtml");
    check(MediaType.TEXT_HTML, "?query=1&method=html");
    check(MediaType.TEXT_PLAIN, "?query=1&method=text");
    check(MediaType.APPLICATION_JSON, "?query=1&method=json");

    check(MediaType.APPLICATION_OCTET_STREAM, "?query=1&media-type=application/octet-stream");
    check(MediaType.APPLICATION_XML, "?query=1&media-type=application/xml");
    check(MediaType.TEXT_HTML, "?query=1&media-type=text/html");
    check(new MediaType("xxx"), "?query=1&media-type=xxx");
  }

  /**
   * Executes the specified GET request and checks the media type of the response.
   * @param type expected media type
   * @param query request
   * @throws IOException I/O exception
   */
  private static void check(final MediaType type, final String query) throws IOException {
    final HttpHeaders headers = new IOUrl(REST_ROOT + query).response().headers();
    final MediaType mt = new MediaType(headers.firstValue(HTTPText.CONTENT_TYPE).get());
    if(!mt.is(type)) fail("Wrong media type: " + mt + " returned, " + type + " expected.");
  }

  /**
   * Specify options.
   * @throws IOException I/O exception
   */
  @Test public void queryOption() throws IOException {
    get(200, "", "query", "2, delete node <a/>", MainOptions.MIXUPDATES.name(), true);
    get(500, "", "query", "2, delete node <a/>", MainOptions.MIXUPDATES.name(), false);
  }

  /**
   * Specify a server file.
   * @throws IOException I/O exception
   */
  @Test public void runOption() throws IOException {
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    new IOFile(path, "x.xq").write("1");
    get("1", "", "run", "x.xq");

    new IOFile(path, "x.bxs").write("xquery 2");
    get("2", "", "run", "x.bxs");

    new IOFile(path, "x.bxs").write("xquery 3\nxquery 4");
    get("34", "", "run", "x.bxs");

    new IOFile(path, "x.bxs").write("<commands><xquery>5</xquery></commands>");
    get("5", "", "run", "x.bxs");

    new IOFile(path, "x.bxs").write("<set option='maxlen'>123</set>");
    get(200, "", "run", "x.bxs");

    get(404, "", "run", "unknown.abc");

    new IOFile(path, "x.bxs").write("<set option='unknown'>123</set>");
    get(500, "", "run", "x.bxs");
  }
}
