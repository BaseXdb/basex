package org.basex.http;

import static org.basex.core.Text.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.QueryError.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.http.HttpRequest.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the server-based HTTP Client.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public class FnHttpTest extends HTTPTest {
  /** Example url. */
  static final String RESTURL = REST_ROOT + NAME;

  /** Books document. */
  private static final String BOOKS = "<books>" + "<book id='1'>"
      + "<name>Sherlock Holmes</name>" + "<author>Doyle</author>" + "</book>"
      + "<book id='2'>" + "<name>Winnetou</name>" + "<author>May</author>"
      + "</book>" + "<book id='3'>" + "<name>Tom Sawyer</name>"
      + "<author>Twain</author>" + "</book>" + "</books>";
  /** Carriage return/line feed. */
  private static final String CRLF = "\r\n";

  /** Local database context. */
  static Context ctx;

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(RESTURL, true);
    ctx = new Context();
  }

  /**
   * Test sending of HTTP PUT requests.
   * @throws Exception exception
   */
  @Test public void put() throws Exception {
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='put' status-only='true'>"
        + "<http:body media-type='text/xml'>" + BOOKS + "</http:body>"
        + "</http:request>", RESTURL), ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_CREATED);
    }
  }

  /**
   * Test sending of HTTP POST requests.
   * @throws Exception exception
   */
  @Test public void putPost() throws Exception {
    // PUT - query
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='put' status-only='true'>"
        + "<http:body media-type='text/xml'>" + BOOKS + "</http:body>"
        + "</http:request>", RESTURL), ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_CREATED);
    }

    // POST - query
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='post'>"
        + "<http:body media-type='application/xml'>"
        + "<query xmlns='" + QueryText.BASEX_URL + "/rest'>"
        + "<text><![CDATA[<x>1</x>]]></text>"
        + "</query>"
        + "</http:body>"
        + "</http:request>", RESTURL), ctx)) {
        checkResponse(qp.value(), 2, HttpURLConnection.HTTP_OK);
    }

    // Execute the same query but with content set from $bodies
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
       " <http:request method='post'>"
        + "<http:body media-type='application/xml'/>"
        + "</http:request>",
        RESTURL,
        " <query xmlns='" + QueryText.BASEX_URL + "/rest'>"
        + "<text><![CDATA[<x>1</x>]]></text>"
        + "</query>"), ctx)) {
      checkResponse(qp.value(), 2, HttpURLConnection.HTTP_OK);
    }
  }

  /**
   * Test sending of HTTP GET requests.
   * @throws Exception exception
   */
  @Test public void get() throws Exception {
    // GET1 - just send a GET request
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='get' href='" + REST_ROOT + "'/>"), ctx)) {
      final Value value = qp.value();
      checkResponse(value, 2, HttpURLConnection.HTTP_OK);

      assertEquals(NodeType.DOCUMENT_NODE, value.itemAt(1).type);
    }

    // GET2 - with override-media-type='text/plain'
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='get' override-media-type='text/plain'/>", REST_ROOT), ctx)) {
      final Value value = qp.value();
      checkResponse(value, 2, HttpURLConnection.HTTP_OK);

      assertEquals(AtomType.STRING, value.itemAt(1).type);
    }

    // Get3 - with status-only='true'
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='get' status-only='true'/>", REST_ROOT), ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_OK);
    }
  }

  /**
   * Test sending of HTTP DELETE requests.
   * @throws Exception exception
   */
  @Test public void putDelete() throws Exception {
    // add document to be deleted
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='put'>"
        + "<http:body media-type='text/xml'><ToBeDeleted/></http:body>"
        + "</http:request>", RESTURL), ctx)) {
      qp.value();
    }

    // DELETE
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='delete' status-only='true'/>", RESTURL), ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_OK);
    }

    // DELETE (same resource, empty sequence as body, 404 expected)
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='delete'/>", RESTURL, " ()") + "[1]/@status/data()", ctx)) {
      assertEquals("404", qp.value().serialize().toString());
    }

    // DELETE (same resource, illegal body)
    try {
      new XQuery(_HTTP_SEND_REQUEST.args(" <http:request method='delete'/>", RESTURL, 123)).
        execute(ctx);
      fail("Error expected");
    } catch(final BaseXException ex) {
      assertTrue(ex.getMessage().contains(ErrType.HC.toString()));
    }
  }

  /**
   * Test sending of HTTP request without any attributes - error shall be thrown
   * that mandatory attributes are missing.
   */
  @Test public void emptyReq() {
    try {
      new XQuery(_HTTP_SEND_REQUEST.args(" <http:request/>")).execute(ctx);
      fail("Error expected");
    } catch(final BaseXException ex) {
      assertTrue(ex.getMessage().contains(ErrType.HC.toString()));
    }
  }

  /**
   * Tests http:send-request((),()).
   */
  @Test public void noParams() {
    final Command cmd = new XQuery(_HTTP_SEND_REQUEST.args(" ()"));
    try {
      cmd.execute(ctx);
      fail("Error expected");
    } catch(final BaseXException ex) {
      assertTrue(ex.getMessage().contains(ErrType.HC.toString()));
    }
  }

  /**
   * Tests an erroneous query.
   * @throws Exception exception
   */
  @Test public void unknown() throws Exception {
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request method='get'/>", RESTURL + "unknown") + "[1]/@status/data()", ctx)) {
      assertEquals("404", qp.value().serialize().toString());
    }
  }

  /**
   * Parse normal request.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test public void parseRequest() throws IOException, QueryException {
    // Simple HTTP request with no errors
    final String request = "<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='POST' href='" + REST_ROOT + "'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:body media-type='text/xml'>" + "Test body content"
        + "</http:body>" + "</http:request>";
    final DBNode dbNode = new DBNode(new IOContent(request));
    final HttpRequestParser rp = new HttpRequestParser(null);
    final HttpRequest r = rp.parse(dbNode.childIter().next(), Empty.VALUE);

    assertEquals(2, r.attributes.size());
    assertEquals(2, r.headers.size());
    assertFalse(r.payload.isEmpty());
    assertEquals(1, r.payloadAtts.size());
  }

  /**
   * Parse multipart request.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test public void parseMultipartReq() throws IOException, QueryException {
    final String multiReq = "<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='POST' href='" + REST_ROOT + "'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:multipart media-type='multipart/mixed' boundary='xxxx'>"
        + "<http:header name='p1hdr1' value='p1hdr1val'/>"
        + "<http:header name='p1hdr2' value='p1hdr2val'/>"
        + "<http:body media-type='text/plain'>" + "Part1" + "</http:body>"
        + "<http:header name='p2hdr1' value='p2hdr1val'/>"
        + "<http:body media-type='text/plain'>" + "Part2" + "</http:body>"
        + "<http:body media-type='text/plain'>"
        + "Part3" + "</http:body>" + "</http:multipart>"
        + "</http:request>";

    final DBNode dbNode1 = new DBNode(new IOContent(multiReq));
    final HttpRequestParser rp = new HttpRequestParser(null);
    final HttpRequest r = rp.parse(dbNode1.childIter().next(), Empty.VALUE);

    assertEquals(2, r.attributes.size());
    assertEquals(2, r.headers.size());
    assertTrue(r.isMultipart);
    assertEquals(3, r.parts.size());

    // check parts
    final Iterator<Part> i = r.parts.iterator();
    Part part = i.next();
    assertEquals(2, part.headers.size());
    assertEquals(1, part.bodyContents.size());
    assertEquals(1, part.bodyAtts.size());

    part = i.next();
    assertEquals(1, part.headers.size());
    assertEquals(1, part.bodyContents.size());
    assertEquals(1, part.bodyAtts.size());

    part = i.next();
    assertEquals(0, part.headers.size());
    assertEquals(1, part.bodyContents.size());
    assertEquals(1, part.bodyAtts.size());
  }

  /**
   * Parse multipart request when the contents for each part are set from the $bodies parameter.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test public void parseMultipartReqBodies() throws IOException, QueryException {
    final String multiReq = "<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='POST' href='" + REST_ROOT + "'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:multipart media-type='multipart/mixed' boundary='xxxx'>"
        + "<http:header name='p1hdr1' value='p1hdr1val'/>"
        + "<http:header name='p1hdr2' value='p1hdr2val'/>"
        + "<http:body media-type='text/plain'/>"
        + "<http:header name='p2hdr1' value='p2hdr1val'/>"
        + "<http:body media-type='text/plain'/>"
        + "<http:body media-type='text/plain'/>"
        + "</http:multipart>" + "</http:request>";

    final DBNode dbNode1 = new DBNode(new IOContent(multiReq));
    final TokenList bodies = new TokenList();
    bodies.add("Part1");
    bodies.add("Part2");
    bodies.add("Part3");

    final HttpRequestParser rp = new HttpRequestParser(null);
    final HttpRequest r = rp.parse(dbNode1.childIter().next(), StrSeq.get(bodies));

    assertEquals(2, r.attributes.size());
    assertEquals(2, r.headers.size());
    assertTrue(r.isMultipart);
    assertEquals(3, r.parts.size());

    // check parts
    final Iterator<Part> i = r.parts.iterator();
    Part part = i.next();
    assertEquals(2, part.headers.size());
    assertEquals(1, part.bodyContents.size());
    assertEquals(1, part.bodyAtts.size());

    part = i.next();
    assertEquals(1, part.headers.size());
    assertEquals(1, part.bodyContents.size());
    assertEquals(1, part.bodyAtts.size());

    part = i.next();
    assertEquals(0, part.headers.size());
    assertEquals(1, part.bodyContents.size());
    assertEquals(1, part.bodyAtts.size());
  }

  /**
   * Tests if errors are thrown when some mandatory attributes are missing in a
   * <http:request/>, <http:body/> or <http:multipart/>.
   * @throws IOException I/O Exception
   */
  @Test public void errors() throws IOException {
    // Incorrect requests
    final HashMap<String, String> queries = new HashMap<>();

    queries.put("Request without method", "<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "href='" + REST_ROOT + "'/>");

    queries.put("Request with send-authorization and only username",
        "<http:request xmlns:http='http://expath.org/ns/http-client' "
        + "method='GET' href='" + REST_ROOT + "' username='test'/>");

    queries.put("Request with body that has no media-type",
        "<http:request xmlns:http='http://expath.org/ns/http-client' "
        + "method='POST' href='" + REST_ROOT + "'>" + "<http:body>"
        + "</http:body>" + "</http:request>");

    queries.put("Request with multipart that has no media-type",
        "<http:request xmlns:http='http://expath.org/ns/http-client' "
        + " method='POST' href='" + REST_ROOT + "'>" + "<http:multipart boundary='xxx'>"
        + "</http:multipart>" + "</http:request>");

    queries.put("Request with multipart with part that has a body without media-type",
        "<http:request xmlns:http='http://expath.org/ns/http-client' "
        + " method='POST' href='" + REST_ROOT + "'>" + "<http:multipart boundary='xxx'>"
        + "<http:header name='hdr1' value-='val1'/>"
        + "<http:body media-type='text/plain'>" + "Part1" + "</http:body>"
        + "<http:header name='hdr1' value-='val1'/>"
        + "<http:body>" + "Part1" + "</http:body>"
        + "</http:multipart>" + "</http:request>");

    queries.put("Request with schema different from http",
        "<http:request xmlns:http='http://expath.org/ns/http-client' "
        + "href='ftp://basex.org'/>");

    queries.put("Request with content and method which must be empty",
        "<http:request xmlns:http='http://expath.org/ns/http-client' "
        + "method='DELETE' href='" + REST_ROOT + "'>"
        + "<http:body media-type='text/plain'>" + "</http:body>"
        + "</http:request>");

    final StringBuilder error = new StringBuilder();
    for(final Entry<String, String> entry : queries.entrySet()) {
      final String name = entry.getKey(), query = entry.getValue();
      final DBNode dbNode = new DBNode(new IOContent(query));
      try {
        final HttpRequestParser rp = new HttpRequestParser(null);
        rp.parse(dbNode.childIter().next(), Empty.VALUE);
        error.append(name).append(": Request did not fail.");
      } catch (final QueryException ex) {
        if(!ex.getMessage().contains(ErrType.HC.toString())) {
          error.append(name).append(": Wrong error code (").append(ex.getMessage()).append(")");
        }
      }
    }
    if(error.length() != 0) fail(error.toString());
  }

  /**
   * Tests method setRequestContent of HttpClient.
   * @throws IOException I/O Exception
   */
  @Test public void writeMultipartMessage() throws IOException {
    final HttpRequest request = new HttpRequest();
    request.isMultipart = true;
    request.payloadAtts.put("media-type", "multipart/alternative");
    request.payloadAtts.put("boundary", "boundary42");
    final Part p1 = new Part();
    p1.headers.put("Content-Type", "text/plain; charset=us-ascii");
    p1.bodyAtts.put("media-type", "text/plain");
    final String plain = "PLAIN\r\n";
    p1.bodyContents.add(Str.get(plain));

    final Part p2 = new Part();
    p2.headers.put("Content-Type", "text/richtext");
    p2.bodyAtts.put("media-type", "text/richtext");
    final String rich = "RICH\n";
    p2.bodyContents.add(Str.get(rich));

    final Part p3 = new Part();
    p3.headers.put("Content-Type", "text/x-whatever");
    p3.bodyAtts.put("media-type", "text/x-whatever");
    final String fancy = "FANCY";
    p3.bodyContents.add(Str.get(fancy));

    request.parts.add(p1);
    request.parts.add(p2);
    request.parts.add(p3);

    final OutputStream out = fakeOutput();
    HttpClient.writePayload(out, request);
    final String expResult = "--boundary42" + CRLF
        + "Content-Type: text/plain; charset=us-ascii" + CRLF + CRLF + plain + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/richtext" + CRLF + CRLF + rich + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/x-whatever" + CRLF + CRLF + fancy + CRLF
        + "--boundary42--" + CRLF;

    // Compare results
    assertEquals(expResult, out.toString());
  }

  /**
   * Tests method setRequestContent of HttpClient.
   * @throws IOException I/O Exception
   */
  @Test public void writeMultipartBinary() throws IOException {
    final HttpRequest request = new HttpRequest();
    request.isMultipart = true;
    request.payloadAtts.put("media-type", "multipart/mixed");
    request.payloadAtts.put("boundary", "boundary");
    final Part p1 = new Part();
    p1.headers.put("Content-Type", "application/octet-stream");
    p1.bodyAtts.put("media-type", "application/octet-stream");
    p1.bodyContents.add(B64.get((byte) -1));
    request.parts.add(p1);

    final ByteArrayOutputStream out = fakeOutput();
    HttpClient.writePayload(out, request);

    final ByteList bl = new ByteList();
    bl.add(token("--boundary" + CRLF + "Content-Type: application/octet-stream" + CRLF + CRLF));
    bl.add(-1).add(token(CRLF + "--boundary--" + CRLF));

    // Compare results
    assertArrayEquals(bl.finish(), out.toByteArray());
  }

  /**
   * Tests writing of request content with different combinations of the body
   * attributes media-type and method.
   * @throws IOException IO exception
   */
  @Test public void writeMessage() throws IOException {
    // Case 1: No method, media-type='text/xml'
    final HttpRequest req1 = new HttpRequest();
    final OutputStream out1 = fakeOutput();
    req1.payloadAtts.put(SerializerOptions.MEDIA_TYPE.name(), "text/xml");
    // Node child
    final FElem elem1 = new FElem("a").add("a");
    req1.payload.add(elem1);
    // String item child
    req1.payload.add(Str.get("<b>b</b>"));
    HttpClient.writePayload(out1, req1);
    assertEquals("<a>a</a>&lt;b&gt;b&lt;/b&gt;", out1.toString());

    // Case 2: No method, media-type='text/plain'
    final HttpRequest req2 = new HttpRequest();
    final OutputStream out2 = fakeOutput();
    req2.payloadAtts.put(SerializerOptions.MEDIA_TYPE.name(), "text/plain");
    // Node child
    final FElem elem2 = new FElem("a").add("a");
    req2.payload.add(elem2);
    // String item child
    req2.payload.add(Str.get("<b>b</b>"));
    HttpClient.writePayload(out2, req2);
    assertEquals("a<b>b</b>", out2.toString());

    // Case 3: method='text', media-type='text/xml'
    final HttpRequest req3 = new HttpRequest();
    final OutputStream out3 = fakeOutput();
    req3.payloadAtts.put(SerializerOptions.MEDIA_TYPE.name(), "text/xml");
    req3.payloadAtts.put("method", "text");
    // Node child
    final FElem e3 = new FElem("a").add("a");
    req3.payload.add(e3);
    // String item child
    req3.payload.add(Str.get("<b>b</b>"));
    HttpClient.writePayload(out3, req3);
    assertEquals("a<b>b</b>", out3.toString());
  }

  /**
   * Tests writing of body content when @method is raw and output is xs:base64Binary.
   * @throws IOException I/O Exception
   */
  @Test public void writeBase64() throws IOException {
    // Case 1: content is xs:base64Binary
    HttpRequest request = new HttpRequest();
    request.payloadAtts.put("method", SerialMethod.BASEX.toString());
    request.payload.add(B64.get(token("test")));
    OutputStream out = fakeOutput();
    HttpClient.writePayload(out, request);
    assertEquals("test", out.toString());

    // Case 2: content is a node
    request = new HttpRequest();
    request.payloadAtts.put("method", SerialMethod.BASEX.toString());
    request.payload.add(new FElem("a").add("test"));
    out = fakeOutput();
    HttpClient.writePayload(out, request);
    assertEquals("<a>test</a>", out.toString());
  }

  /**
   * Tests writing text nodes (children of http:send-request bodies).
   * @throws IOException I/O Exception
   */
  @Test public void writeText() throws IOException {
    HttpRequest req1 = new HttpRequest();
    req1.payloadAtts.put(SerializerOptions.MEDIA_TYPE.name(), "application/octet-stream");
    req1.payload.add(new FTxt("&"));
    OutputStream out1 = fakeOutput();
    HttpClient.writePayload(out1, req1);
    assertEquals("&", out1.toString());

    req1 = new HttpRequest();
    req1.payloadAtts.put(SerializerOptions.MEDIA_TYPE.name(), "application/x-www-form-urlencoded");
    req1.payload.add(new FTxt("&"));
    out1 = fakeOutput();
    HttpClient.writePayload(out1, req1);
    assertEquals("&", out1.toString());
  }

  /**
   * Tests writing of body content when @method is raw and output is xs:hexBinary.
   * @throws IOException I/O Exception
   */
  @Test public void writeHex() throws IOException {
    // Case 1: content is xs:hexBinary
    final HttpRequest req1 = new HttpRequest();
    req1.payloadAtts.put("method", SerialMethod.BASEX.toString());
    req1.payload.add(new Hex(token("test")));
    final OutputStream out1 = fakeOutput();
    HttpClient.writePayload(out1, req1);
    assertEquals("test", out1.toString());

    // Case 2: content is a node
    final HttpRequest req2 = new HttpRequest();
    req2.payloadAtts.put("method", SerialMethod.BASEX.toString());
    final FElem e3 = new FElem("a").add("test");
    req2.payload.add(e3);
    final OutputStream out2 = fakeOutput();
    HttpClient.writePayload(out2, req2);
    assertEquals("<a>test</a>", out2.toString());
  }

  /**
   * Tests writing of request content when @src is set.
   * @throws IOException I/O Exception
   */
  @Test public void writeFromResource() throws IOException {
    // Create a file form which will be read
    final IOFile file = new IOFile(Prop.TEMPDIR, Util.className(FnHttpTest.class));
    file.write("test");

    // Request
    final HttpRequest request = new HttpRequest();
    request.payloadAtts.put("src", file.url());
    request.payloadAtts.put("method", "binary");
    // HTTP connection
    final OutputStream out = fakeOutput();
    HttpClient.writePayload(out, request);

    // Delete file
    file.delete();

    assertEquals("test", out.toString());
  }

  /**
   * Tests response handling with specified charset in the header 'Content-Type'.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test public void responseWithCharset() throws IOException, QueryException {
    // Create fake HTTP connection
    final FakeHttpConnection conn = new FakeHttpConnection();
    // Set content type
    conn.contentType = "text/plain; charset=CP1251";
    // set content encoded in CP1251
    final String test = "\u0442\u0435\u0441\u0442";
    conn.content = Charset.forName("CP1251").encode(test).array();
    final Value response = new HttpResponse(null, ctx.options).getResponse(conn, true, null);
    // compare results
    assertEquals(test, string(response.itemAt(1).string(null)));
  }

  /**
   * Tests content-type parsing.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test public void parseContentType() throws IOException, QueryException {
    final FakeHttpConnection conn = new FakeHttpConnection();
    // upper case attribute, quoted string
    conn.contentType = "text/plain; CHARSET=\"CP1252\"";
    conn.content = new byte[] { };
    new HttpResponse(null, ctx.options).getResponse(conn, true, null);

    conn.contentType = "text/plain; ChArSeT=\"\\C\\P\\1\\2\\5\\2\"";
    new HttpResponse(null, ctx.options).getResponse(conn, true, null);

    try {
      conn.contentType = "text/plain; CHARSET=\\C\\P\\1\\2\\5\\2";
      new HttpResponse(null, ctx.options).getResponse(conn, true, null);
      fail("Encoding exception expected");
    } catch(final DecodingException expected) { }
  }

  /**
   * Tests ResponseHandler.getResponse() with multipart response.
   * @throws IOException I/O Exception
   * @throws Exception exception
   */
  @Test public void multipartResponse() throws Exception {
    // Create fake HTTP connection
    final FakeHttpConnection conn = new FakeHttpConnection();
    final Map<String, List<String>> hdrs = new HashMap<>();
    final List<String> fromVal = new ArrayList<>();
    fromVal.add("Nathaniel Borenstein <nsb@bellcore.com>");
    // From: Nathaniel Borenstein <nsb@bellcore.com>
    hdrs.put("From", fromVal);
    final List<String> mimeVal = new ArrayList<>();
    mimeVal.add("1.0");
    // MIME-Version: 1.0
    hdrs.put("MIME-version", mimeVal);
    final List<String> subjVal = new ArrayList<>();
    subjVal.add("Formatted text mail");
    // Subject: Formatted text mail
    hdrs.put("Subject", subjVal);
    final List<String> contTypeVal = new ArrayList<>();
    contTypeVal.add("multipart/alternative;boundary=\"boundary42\"");
    // Content-Type: multipart/alternative; boundary=boundary42
    hdrs.put("Content-Type", contTypeVal);

    conn.headers = hdrs;
    conn.contentType = "multipart/alternative; boundary=\"boundary42\"";
    conn.content = token("--boundary42" + CRLF
        + "Content-Type: text/plain; charset=us-ascii" + CRLF + CRLF
        + "...plain text...." + CRLF + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/richtext" + CRLF + CRLF
        + ".... richtext..." + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/x-whatever" + CRLF + CRLF
        + ".... fanciest formatted version  " + CRLF + "..."  + CRLF + "--boundary42--");
    final Value returned = new HttpResponse(null, ctx.options).getResponse(conn, true, null);

    // Construct expected result
    final ItemList expected = new ItemList();
    final String response = "<http:response "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "status='200' message='OK'>"
        + "<http:header name='Subject' value='Formatted text mail'/>"
        + "<http:header name='Content-Type' "
        + "value='multipart/alternative;boundary=&quot;boundary42&quot;'/>"
        + "<http:header name='MIME-version' value='1.0'/>"
        + "<http:header name='From' value='Nathaniel Borenstein "
        + "&lt;nsb@bellcore.com&gt;'/>"
        + "<http:multipart media-type='multipart/alternative' "
        + "boundary='boundary42'>"
        + "<http:header name='Content-Type' "
        + "value='text/plain; charset=us-ascii'/>"
        + "<http:body media-type='text/plain; charset=us-ascii'/>"
        + "<http:header name='Content-Type' value='text/richtext'/>"
        + "<http:body media-type='text/richtext'/>"
        + "<http:header name='Content-Type' value='text/x-whatever'/>"
        + "<http:body media-type='text/x-whatever'/>"
        + "</http:multipart>" + "</http:response> ";
    expected.add(new DBNode(new IOContent(response)).childIter().next());
    expected.add(Str.get("...plain text....\n"));
    expected.add(Str.get(".... richtext..."));
    expected.add(Str.get(".... fanciest formatted version  \n..."));
    compare(expected.value(), returned);
  }

  /**
   * Tests ResponseHandler.getResponse() with multipart response having preamble and epilogue.
   * @throws IOException I/O Exception
   * @throws Exception exception
   */
  @Test public void multipartRespPreamble() throws Exception {
    // Create fake HTTP connection
    final FakeHttpConnection conn = new FakeHttpConnection();
    final Map<String, List<String>> hdrs = new HashMap<>();
    final List<String> fromVal = new ArrayList<>();
    fromVal.add("Nathaniel Borenstein <nsb@bellcore.com>");
    // From: Nathaniel Borenstein <nsb@bellcore.com>
    hdrs.put("From", fromVal);
    final List<String> mimeVal = new ArrayList<>();
    mimeVal.add("1.0");
    final List<String> toVal = new ArrayList<>();
    toVal.add("Ned Freed <ned@innosoft.com>");
    // To: Ned Freed <ned@innosoft.com>
    hdrs.put("To", toVal);
    // MIME-Version: 1.0
    hdrs.put("MIME-version", mimeVal);
    final List<String> subjVal = new ArrayList<>();
    subjVal.add("Formatted text mail");
    // Subject: Formatted text mail
    hdrs.put("Subject", subjVal);
    final List<String> contTypeVal = new ArrayList<>();
    contTypeVal.add("multipart/mixed;boundary=\"simple boundary\"");
    // Content-Type: multipart/alternative; boundary=boundary42
    hdrs.put("Content-Type", contTypeVal);
    conn.headers = hdrs;
    conn.contentType = "multipart/mixed; boundary=\"simple boundary\"";
    // Response to be read
    conn.content = token("This is the preamble.  "
        + "It is to be ignored, though it" + NL
        + "is a handy place for mail composers to include an" + CRLF
        + "explanatory note to non-MIME compliant readers." + CRLF
        + "--simple boundary" + CRLF + CRLF
        + "This is implicitly typed plain ASCII text." + CRLF
        + "It does NOT end with a linebreak."
        +  CRLF + "--simple boundary" + CRLF
        + "Content-type: text/plain; charset=us-ascii" + CRLF + CRLF
        + "This is explicitly typed plain ASCII text." + CRLF
        + "It DOES end with a linebreak." + CRLF
        +  CRLF + "--simple boundary--" + CRLF
        + "This is the epilogue.  It is also to be ignored.");
    // Get response as sequence of XQuery items
    final Value returned = new HttpResponse(null, ctx.options).getResponse(conn, true, null);

    // Construct expected result
    final ItemList expected = new ItemList();
    final String response = "<http:response "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "status='200' message='OK'>"
        + "<http:header name='Subject' value='Formatted text mail'/>"
        + "<http:header name='To' value='Ned "
        + "Freed &lt;ned@innosoft.com&gt;'/>"
        + "<http:header name='Content-Type' value='multipart/mixed;"
        + "boundary=&quot;simple boundary&quot;'/>"
        + "<http:header name='MIME-version' value='1.0'/>"
        + "<http:header name='From' value='Nathaniel Borenstein "
        + "&lt;nsb@bellcore.com&gt;'/>"
        + "<http:multipart media-type='multipart/mixed' "
        + "boundary='simple boundary'>"
        + "<http:body media-type='text/plain'/>"
        + "<http:header name='Content-type' value='text/plain; "
        + "charset=us-ascii'/>"
        + "<http:body media-type='text/plain; charset=us-ascii'/>"
        + "</http:multipart>" + "</http:response>";
    expected.add(new DBNode(new IOContent(response)).childIter().next());
    expected.add(Str.get("This is implicitly typed plain ASCII text.\n"
        + "It does NOT end with a linebreak."));
    expected.add(Str.get("This is explicitly typed plain ASCII text.\n"
        + "It DOES end with a linebreak.\n"));

    compare(expected.value(), returned);
  }

  /**
   * Compares results.
   * @param expected expected result
   * @param returned returned result
   * @throws Exception exception
   */
  private static void compare(final Value expected, final Value returned) throws Exception {
    // Compare response with expected result
    assertEquals(expected.size(), returned.size(), "Different number of results");

    final long es = expected.size();
    for(int e = 0; e < es; e++) {
      Item exp = expected.itemAt(e), ret = returned.itemAt(e);
      // reorder response headers
      if(exp.type == NodeType.ELEMENT) exp = reorderHeaders(exp);
      if(ret.type == NodeType.ELEMENT) ret = reorderHeaders(ret);
      // compare items
      if(!new DeepEqual().equal(exp, ret)) {
        fail(Strings.concat("Result ", e, " differs:\nReturned: ",
            ret.serialize().finish(), "\nExpected: ", exp.serialize()));
      }
    }
  }

  /**
   * Sorts HTTP headers.
   * @param xml original element
   * @return element with reordered headers
   * @throws QueryException query exception
   */
  private static Item reorderHeaders(final Item xml) throws QueryException {
    final String query = ". update {"
      + " delete nodes http:header,"
      + " for $h in http:header"
      + " order by $h/@name"
      + " return insert node $h as first into ."
      + '}';
    try(QueryProcessor qp = new QueryProcessor(query, ctx).context(xml)) {
      return qp.iter().next();
    }
  }

  /**
   * Tests nested multipart responses.
   * @throws Exception exception
   */
  @Test public void nestedMultipart() throws Exception {
    // Create fake HTTP connection
    final String boundary = "batchresponse_4c4c5223-efa7-4aba-9865-fb4cb102cfd2";

    final FakeHttpConnection conn = new FakeHttpConnection();
    final Map<String, List<String>> hdrs = new HashMap<>();
    final List<String> contTypeVal = new ArrayList<>();
    contTypeVal.add("multipart/mixed;boundary=\"" + boundary + '"');
    hdrs.put("Content-Type", contTypeVal);

    conn.headers = hdrs;
    conn.contentType = "multipart/alternative; boundary=\"" + boundary + '"';
    conn.content = new IOFile("src/test/resources/response.txt").read();

    new HttpResponse(null, ctx.options).getResponse(conn, true, null);
  }

  /**
   * Checks the response to an HTTP request.
   * @param value query result
   * @param itemsCount expected number of items
   * @param expStatus expected status
   */
  private static void checkResponse(final Value value, final int itemsCount, final int expStatus) {
    assertEquals(itemsCount, value.size());
    assertTrue(value.itemAt(0) instanceof FElem);
    final FElem response = (FElem) value.itemAt(0);
    assertNotNull(response.attributeIter());
    if(!eq(response.attribute(STATUS), token(expStatus))) {
      fail("Expected: " + expStatus + "\nFound: " + response);
    }
  }

  /**
   * Returns the output stream of a fake connection.
   * @return output stream
   * @throws MalformedURLException exception
   */
  private static ByteArrayOutputStream fakeOutput() throws MalformedURLException {
    return new FakeHttpConnection().getOutputStream();
  }
}

/**
 * Fake HTTP connection.
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
final class FakeHttpConnection extends HttpURLConnection {
  /** Connection output stream. */
  final ByteArrayOutputStream out = new ByteArrayOutputStream();
  /** Request headers. */
  Map<String, List<String>> headers = new HashMap<>();
  /** Content-type. */
  String contentType;
  /** Content. */
  byte[] content;

  /**
   * Constructor.
   * @throws MalformedURLException exception
   */
  FakeHttpConnection() throws MalformedURLException {
    super(new URL("http://fake-test.com"));
  }

  @Override
  public ByteArrayInputStream getInputStream() {
    return new ByteArrayInputStream(content);
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public int getResponseCode() {
    return 200;
  }

  @Override
  public String getResponseMessage() {
    return "OK";
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    return headers;
  }

  @Override
  public String getHeaderField(final String field) {
    final List<String> values = headers.get(field);
    if(values == null) return "";
    final StringBuilder sb = new StringBuilder();
    for(final String v : values) {
      if(sb.length() > 0) sb.append(';');
      sb.append(v);
    }
    return sb.toString();
  }

  @Override
  public ByteArrayOutputStream getOutputStream() {
    return out;
  }

  @Override
  public void disconnect() {
  }

  @Override
  public boolean usingProxy() {
    return false;
  }

  @Override
  public void connect() {
  }
}
