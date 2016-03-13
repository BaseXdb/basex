package org.basex.http;

import static org.basex.core.Text.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.QueryError.ErrType;
import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.http.HttpRequest.Part;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the server-based HTTP Client.
 *
 * @author BaseX Team 2005-16, BSD License
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
  @BeforeClass
  public static void start() throws Exception {
    init(RESTURL, true);
    ctx = new Context();
  }

  /**
   * Test sending of HTTP PUT requests.
   * @throws Exception exception
   */
  @Test
  public void put() throws Exception {
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='put' status-only='true'>"
        + "<http:body media-type='text/xml'>" + BOOKS + "</http:body>"
        + "</http:request>", RESTURL), ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_CREATED);
    }
  }

  /**
   * Test sending of HTTP POST requests.
   * @throws Exception exception
   */
  @Test
  public void putPost() throws Exception {
    // PUT - query
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='put' status-only='true'>"
        + "<http:body media-type='text/xml'>" + BOOKS + "</http:body>"
        + "</http:request>", RESTURL), ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_CREATED);
    }

    // POST - query
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='post'>"
        + "<http:body media-type='application/xml'>"
        + "<query xmlns='" + Prop.URL + "/rest'>"
        + "<text><![CDATA[<x>1</x>]]></text>"
        + "</query>"
        + "</http:body>"
        + "</http:request>", RESTURL), ctx)) {
        checkResponse(qp.value(), 2, HttpURLConnection.HTTP_OK);
    }

    // Execute the same query but with content set from $bodies
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
       "<http:request method='post'>"
        + "<http:body media-type='application/xml'/>"
        + "</http:request>",
        RESTURL,
        "<query xmlns='" + Prop.URL + "/rest'>"
        + "<text><![CDATA[<x>1</x>]]></text>"
        + "</query>"), ctx)) {
      checkResponse(qp.value(), 2, HttpURLConnection.HTTP_OK);
    }
  }

  /**
   * Test sending of HTTP GET requests.
   * @throws Exception exception
   */
  @Test
  public void postGet() throws Exception {
    // GET1 - just send a GET request
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='get' href='" + REST_ROOT + "'/>"), ctx)) {
      final Value v = qp.value();
      checkResponse(v, 2, HttpURLConnection.HTTP_OK);

      assertEquals(NodeType.DOC, v.itemAt(1).type);
    }

    // GET2 - with override-media-type='text/plain'
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='get' override-media-type='text/plain'/>", REST_ROOT), ctx)) {
      final Value v = qp.value();
      checkResponse(v, 2, HttpURLConnection.HTTP_OK);

      assertEquals(AtomType.STR, v.itemAt(1).type);
    }

    // Get3 - with status-only='true'
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='get' status-only='true'/>", REST_ROOT), ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_OK);
    }
  }

  /**
   * Test sending of HTTP DELETE requests.
   * @throws Exception exception
   */
  @Test
  public void postDelete() throws Exception {
    // add document to be deleted
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='put'>"
        + "<http:body media-type='text/xml'><ToBeDeleted/></http:body>"
        + "</http:request>", RESTURL), ctx)) {
      qp.value();
    }

    // DELETE
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='delete' status-only='true'/>", RESTURL), ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_OK);
    }
  }

  /**
   * Test sending of HTTP request without any attributes - error shall be thrown
   * that mandatory attributes are missing.
   */
  @Test
  public void sendEmptyReq() {
    try {
      new XQuery(_HTTP_SEND_REQUEST.args("<http:request/>")).execute(ctx);
    } catch(final BaseXException ex) {
      assertTrue(ex.getMessage().contains(ErrType.HC.toString()));
    }
  }

  /**
   * Tests http:send-request((),()).
   */
  @Test
  public void sendReqNoParams() {
    final Command cmd = new XQuery(_HTTP_SEND_REQUEST.args("()"));
    try {
      cmd.execute(ctx);
    } catch(final BaseXException ex) {
      assertTrue(ex.getMessage().contains(ErrType.HC.toString()));
    }
  }

  /**
   * Tests an erroneous query.
   * @throws Exception exception
   */
  @Test
  public void error() throws Exception {
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='get'/>", RESTURL + "unknown") + "[1]/@status/data()", ctx)) {
      assertEquals("404", qp.value().serialize().toString());
    }
  }

  /**
   * Tests RequestParser.parse() with normal (not multipart) request.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void parseRequest() throws IOException, QueryException {
    // Simple HTTP request with no errors
    final String req = "<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='POST' href='" + REST_ROOT + "'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:body media-type='text/xml'>" + "Test body content"
        + "</http:body>" + "</http:request>";
    final DBNode dbNode = new DBNode(new IOContent(req));
    final HttpRequestParser rp = new HttpRequestParser(null);
    final HttpRequest r = rp.parse(dbNode.children().next(), null);

    assertEquals(2, r.attributes.size());
    assertEquals(2, r.headers.size());
    assertTrue(r.bodyContent.size() != 0);
    assertEquals(1, r.payloadAttrs.size());
  }

  /**
   * Tests RequestParser.parse() with multipart request.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void parseMultipartReq() throws IOException, QueryException {
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
    final HttpRequest r = rp.parse(dbNode1.children().next(), null);

    assertEquals(2, r.attributes.size());
    assertEquals(2, r.headers.size());
    assertTrue(r.isMultipart);
    assertEquals(3, r.parts.size());

    // check parts
    final Iterator<Part> i = r.parts.iterator();
    Part part = i.next();
    assertEquals(2, part.headers.size());
    assertEquals(1, part.bodyContent.size());
    assertEquals(1, part.bodyAttrs.size());

    part = i.next();
    assertEquals(1, part.headers.size());
    assertEquals(1, part.bodyContent.size());
    assertEquals(1, part.bodyAttrs.size());

    part = i.next();
    assertEquals(0, part.headers.size());
    assertEquals(1, part.bodyContent.size());
    assertEquals(1, part.bodyAttrs.size());
  }

  /**
   * Tests parsing of multipart request when the contents for each part are set
   * from the $bodies parameter.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void parseMultipartReqBodies() throws IOException, QueryException {
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
    final ItemList bodies = new ItemList();
    bodies.add(Str.get("Part1"));
    bodies.add(Str.get("Part2"));
    bodies.add(Str.get("Part3"));

    final HttpRequestParser rp = new HttpRequestParser(null);
    final HttpRequest r = rp.parse(dbNode1.children().next(), bodies.iter());

    assertEquals(2, r.attributes.size());
    assertEquals(2, r.headers.size());
    assertTrue(r.isMultipart);
    assertEquals(3, r.parts.size());

    // check parts
    final Iterator<Part> i = r.parts.iterator();
    Part part = i.next();
    assertEquals(2, part.headers.size());
    assertEquals(1, part.bodyContent.size());
    assertEquals(1, part.bodyAttrs.size());

    part = i.next();
    assertEquals(1, part.headers.size());
    assertEquals(1, part.bodyContent.size());
    assertEquals(1, part.bodyAttrs.size());

    part = i.next();
    assertEquals(0, part.headers.size());
    assertEquals(1, part.bodyContent.size());
    assertEquals(1, part.bodyAttrs.size());
  }

  /**
   * Tests basic authentication.
   * @throws Exception Exception
   */
  @Test
  public void basic() throws Exception {
    // correct credentials
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request xmlns:http='http://expath.org/ns/http-client' "
        + "method='GET' href='" + REST_ROOT + "' send-authorization='true' "
        + "auth-method='Basic' username='admin' password='admin'/>"), ctx)) {
      checkResponse(qp.value(), 2, HttpURLConnection.HTTP_OK);
    }
    // wrong credentials
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request xmlns:http='http://expath.org/ns/http-client' " +
        "method='GET' href='" + REST_ROOT + "' send-authorization='true' " +
        "auth-method='Basic' username='unknown' password='wrong'/>") +
        "[. instance of node()][@status = '401']", ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_UNAUTHORIZED);
    }
  }

  /**
   * Test digest authentication.
   * @throws Exception exception
   */
  @Test
  public void digest() throws Exception {
    // correct credentials
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request xmlns:http='http://expath.org/ns/http-client' method='GET' " +
        "send-authorization='true' auth-method='Digest' username='admin' password='admin' " +
        "href='" + REST_ROOT + "'/>"), ctx)) {
      checkResponse(qp.value(), 2, HttpURLConnection.HTTP_OK);
    }
    // wrong credentials
    try(final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request xmlns:http='http://expath.org/ns/http-client' method='GET' " +
        "send-authorization='true' auth-method='Digest' username='unknown' password='wrong' " +
        "href='" + REST_ROOT + "?query=()'/>") +
        "[. instance of node()][@status = '401']", ctx)) {
      checkResponse(qp.value(), 1, HttpURLConnection.HTTP_UNAUTHORIZED);
    }
  }

  /**
   * Tests if errors are thrown when some mandatory attributes are missing in a
   * <http:request/>, <http:body/> or <http:multipart/>.
   * @throws IOException I/O Exception
   */
  @Test
  public void errors() throws IOException {

    // Incorrect requests
    final List<byte[]> falseReqs = new ArrayList<>();

    // Request without method
    final byte[] falseReq1 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "href='" + REST_ROOT + "'/>");
    falseReqs.add(falseReq1);

    // Request with send-authorization and no credentials
    final byte[] falseReq2 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='GET' href='" + REST_ROOT + "' "
        + "send-authorization='true'/>");
    falseReqs.add(falseReq2);

    // Request with send-authorization and only username
    final byte[] falseReq3 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='GET' href='" + REST_ROOT + "' "
        + "send-authorization='true' username='test'/>");
    falseReqs.add(falseReq3);

    // Request with body that has no media-type
    final byte[] falseReq4 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='POST' href='" + REST_ROOT + "'>" + "<http:body>"
        + "</http:body>" + "</http:request>");
    falseReqs.add(falseReq4);

    // Request with multipart that has no media-type
    final byte[] falseReq5 = token("<http:request method='POST' "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "href='" + REST_ROOT + "'>" + "<http:multipart boundary='xxx'>"
        + "</http:multipart>" + "</http:request>");
    falseReqs.add(falseReq5);

    // Request with multipart with part that has a body without media-type
    final byte[] falseReq6 = token("<http:request method='POST' "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "href='" + REST_ROOT + "'>" + "<http:multipart boundary='xxx'>"
        + "<http:header name='hdr1' value-='val1'/>"
        + "<http:body media-type='text/plain'>" + "Part1" + "</http:body>"
        + "<http:header name='hdr1' value-='val1'/>"
        + "<http:body>" + "Part1" + "</http:body>"
        + "</http:multipart>" + "</http:request>");
    falseReqs.add(falseReq6);

    // Request with schema different from http
    final byte[] falseReq7 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "href='ftp://basex.org'/>");
    falseReqs.add(falseReq7);

    // Request with content and method which must be empty
    final byte[] falseReq8 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='DELETE' href='" + REST_ROOT + "'>"
        + "<http:body media-type='text/plain'>" + "</http:body>"
        + "</http:request>");
    falseReqs.add(falseReq8);

    for(final byte[] falseReq : falseReqs) {
      final DBNode dbNode = new DBNode(new IOContent(falseReq));
      try {
        final HttpRequestParser rp = new HttpRequestParser(null);
        rp.parse(dbNode.children().next(), null);
        fail("Exception not thrown");
      } catch (final QueryException ex) {
        assertTrue(ex.getMessage().contains(ErrType.HC.toString()));
      }
    }

  }

  /**
   * Tests method setRequestContent of HttpClient.
   * @throws IOException I/O Exception
   */
  @Test
  public void writeMultipartMessage() throws IOException {
    final HttpRequest req = new HttpRequest();
    req.isMultipart = true;
    req.payloadAttrs.put("media-type", "multipart/alternative");
    req.payloadAttrs.put("boundary", "boundary42");
    final Part p1 = new Part();
    p1.headers.put("Content-Type", "text/plain; charset=us-ascii");
    p1.bodyAttrs.put("media-type", "text/plain");
    final String plain = "...plain text....";
    p1.bodyContent.add(Str.get(plain + '\n'));

    final Part p2 = new Part();
    p2.headers.put("Content-Type", "text/richtext");
    p2.bodyAttrs.put("media-type", "text/richtext");
    final String rich = ".... richtext version...";
    p2.bodyContent.add(Str.get(rich));

    final Part p3 = new Part();
    p3.headers.put("Content-Type", "text/x-whatever");
    p3.bodyAttrs.put("media-type", "text/x-whatever");
    final String fancy = ".... fanciest formatted version...";
    p3.bodyContent.add(Str.get(fancy));

    req.parts.add(p1);
    req.parts.add(p2);
    req.parts.add(p3);

    final FakeHttpConnection fakeConn = new FakeHttpConnection(new URL("http://www.test.com"));
    HttpClient.setRequestContent(fakeConn.getOutputStream(), req);
    final String expResult = "--boundary42" + CRLF
        + "Content-Type: text/plain; charset=us-ascii" + CRLF + CRLF
        + plain + Prop.NL + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/richtext" + CRLF + CRLF
        + rich + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/x-whatever" + CRLF + CRLF
        + fancy + CRLF
        + "--boundary42--" + CRLF;

    // Compare results
    assertEquals(expResult, fakeConn.getOutputStream().toString());
  }

  /**
   * Tests writing of request content with different combinations of the body
   * attributes media-type and method.
   * @throws IOException IO exception
   */
  @Test
  public void writeMessage() throws IOException {
    // Case 1: No method, media-type='text/xml'
    final HttpRequest req1 = new HttpRequest();
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL("http://www.test.com"));
    req1.payloadAttrs.put(SerializerOptions.MEDIA_TYPE.name(), "text/xml");
    // Node child
    final FElem e1 = new FElem("a").add("a");
    req1.bodyContent.add(e1);
    // String item child
    req1.bodyContent.add(Str.get("<b>b</b>"));
    HttpClient.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals("<a>a</a>&lt;b&gt;b&lt;/b&gt;", fakeConn1.out.toString(Strings.UTF8));

    // Case 2: No method, media-type='text/plain'
    final HttpRequest req2 = new HttpRequest();
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL("http://www.test.com"));
    req2.payloadAttrs.put(SerializerOptions.MEDIA_TYPE.name(), "text/plain");
    // Node child
    final FElem e2 = new FElem("a").add("a");
    req2.bodyContent.add(e2);
    // String item child
    req2.bodyContent.add(Str.get("<b>b</b>"));
    HttpClient.setRequestContent(fakeConn2.getOutputStream(), req2);
    assertEquals("a<b>b</b>", fakeConn2.out.toString());

    // Case 3: method='text', media-type='text/xml'
    final HttpRequest req3 = new HttpRequest();
    final FakeHttpConnection fakeConn3 = new FakeHttpConnection(new URL("http://www.test.com"));
    req3.payloadAttrs.put(SerializerOptions.MEDIA_TYPE.name(), "text/xml");
    req3.payloadAttrs.put("method", "text");
    // Node child
    final FElem e3 = new FElem("a").add("a");
    req3.bodyContent.add(e3);
    // String item child
    req3.bodyContent.add(Str.get("<b>b</b>"));
    HttpClient.setRequestContent(fakeConn3.getOutputStream(), req3);
    assertEquals("a<b>b</b>", fakeConn3.out.toString());
  }

  /**
   * Tests writing of body content when @method is raw and output is xs:base64Binary.
   * @throws IOException I/O Exception
   */
  @Test
  public void writeBase64() throws IOException {
    // Case 1: content is xs:base64Binary
    final HttpRequest req1 = new HttpRequest();
    req1.payloadAttrs.put("method", SerialMethod.BASEX.toString());
    req1.bodyContent.add(new B64(token("test")));
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL("http://www.test.com"));
    HttpClient.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals(fakeConn1.out.toString(Strings.UTF8), "test");

    // Case 2: content is a node
    final HttpRequest req2 = new HttpRequest();
    req2.payloadAttrs.put("method", SerialMethod.BASEX.toString());
    final FElem e3 = new FElem("a").add("test");
    req2.bodyContent.add(e3);
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL("http://www.test.com"));
    HttpClient.setRequestContent(fakeConn2.getOutputStream(), req2);
    assertEquals(fakeConn2.out.toString(), "<a>test</a>");
  }

  /**
   * Tests writing of body content when @method is raw and output is xs:hexBinary.
   * @throws IOException I/O Exception
   */
  @Test
  public void writeHex() throws IOException {
    // Case 1: content is xs:hexBinary
    final HttpRequest req1 = new HttpRequest();
    req1.payloadAttrs.put("method", SerialMethod.BASEX.toString());
    req1.bodyContent.add(new Hex(token("test")));
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL("http://www.test.com"));
    HttpClient.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals(fakeConn1.out.toString(Strings.UTF8), "test");

    // Case 2: content is a node
    final HttpRequest req2 = new HttpRequest();
    req2.payloadAttrs.put("method", SerialMethod.BASEX.toString());
    final FElem e3 = new FElem("a").add("test");
    req2.bodyContent.add(e3);
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL("http://www.test.com"));
    HttpClient.setRequestContent(fakeConn2.getOutputStream(), req2);
    assertEquals(fakeConn2.out.toString(), "<a>test</a>");
  }

  /**
   * Tests writing of request content when @src is set.
   * @throws IOException I/O Exception
   */
  @Test
  public void writeFromResource() throws IOException {
    // Create a file form which will be read
    final IOFile file = new IOFile(Prop.TMP, Util.className(FnHttpTest.class));
    file.write(token("test"));

    // Request
    final HttpRequest req = new HttpRequest();
    req.payloadAttrs.put("src", file.url());
    req.payloadAttrs.put("method", "binary");
    // HTTP connection
    final FakeHttpConnection fakeConn = new FakeHttpConnection(new URL("http://www.test.com"));
    HttpClient.setRequestContent(fakeConn.getOutputStream(), req);

    // Delete file
    file.delete();

    assertEquals(fakeConn.out.toString(Strings.UTF8), "test");
  }

  /**
   * Tests response handling with specified charset in the header
   * 'Content-Type'.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void responseWithCharset() throws IOException, QueryException {
    // Create fake HTTP connection
    final FakeHttpConnection conn = new FakeHttpConnection(new URL("http://www.test.com"));
    // Set content type
    conn.contentType = "text/plain; charset=CP1251";
    // set content encoded in CP1251
    final String test = "\u0442\u0435\u0441\u0442";
    conn.content = Charset.forName("CP1251").encode(test).array();
    final ItemList res = new HttpResponse(null, ctx.options).getResponse(conn, true, null);
    // compare results
    assertEquals(test, string(res.get(1).string(null)));
  }

  /**
   * Tests ResponseHandler.getResponse() with multipart response.
   * @throws IOException I/O Exception
   * @throws Exception exception
   */
  @Test
  public void multipartResponse() throws Exception {
    // Create fake HTTP connection
    final FakeHttpConnection conn = new FakeHttpConnection(new URL("http://www.test.com"));
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
    contTypeVal.add("multipart/alternative");
    contTypeVal.add("boundary=\"boundary42\"");
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
    final ItemList returned = new HttpResponse(null, ctx.options).getResponse(conn, true, null);

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
    expected.add(new DBNode(new IOContent(response)).children().next());
    expected.add(Str.get("...plain text....\n\n"));
    expected.add(Str.get(".... richtext...\n"));
    expected.add(Str.get(".... fanciest formatted version  \n...\n"));
    compare(expected, returned);
  }

  /**
   * Tests ResponseHandler.getResponse() with multipart response having preamble and epilogue.
   * @throws IOException I/O Exception
   * @throws Exception exception
   */
  @Test
  public void multipartRespPreamble() throws Exception {
    // Create fake HTTP connection
    final FakeHttpConnection conn = new FakeHttpConnection(new URL("http://www.test.com"));
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
    contTypeVal.add("multipart/mixed");
    contTypeVal.add("boundary=\"simple boundary\"");
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
    final ItemList returned = new HttpResponse(null, ctx.options).getResponse(conn, true, null);

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
    expected.add(new DBNode(new IOContent(response)).children().next());
    expected.add(Str.get("This is implicitly typed plain ASCII text.\n"
        + "It does NOT end with a linebreak.\n"));
    expected.add(Str.get("This is explicitly typed plain ASCII text.\n"
        + "It DOES end with a linebreak.\n\n"));

    compare(expected, returned);
  }

  /**
   * Compares results.
   * @param expected expected result
   * @param returned returned result
   * @throws Exception exception
   */
  private static void compare(final ItemList expected, final ItemList returned) throws Exception {

    // Compare response with expected result
    assertEquals("Different number of results", expected.size(), returned.size());

    final long es = expected.size();
    for(int e = 0; e < es; e++) {
      final Item exp = expected.get(e), ret = returned.get(e);
      if(!new DeepEqual().equal(exp, ret)) {
        final TokenBuilder tb = new TokenBuilder("Result ").addLong(e).add(" differs:\nReturned: ");
        tb.addExt(ret.serialize()).add("\nExpected: ").addExt(exp.serialize());
        fail(tb.toString());
      }
    }
  }

  /**
   * Tests nested multipart responses.
   * @throws Exception exception
   */
  @Test
  public void nestedMultipart() throws Exception {
    // Create fake HTTP connection
    final String boundary = "batchresponse_4c4c5223-efa7-4aba-9865-fb4cb102cfd2";

    final FakeHttpConnection conn = new FakeHttpConnection(new URL("http://www.test.com"));
    final Map<String, List<String>> hdrs = new HashMap<>();
    final List<String> contTypeVal = new ArrayList<>();
    contTypeVal.add("multipart/mixed");
    contTypeVal.add("boundary=\"" + boundary + "\"");
    hdrs.put("Content-Type", contTypeVal);

    conn.headers = hdrs;
    conn.contentType = "multipart/alternative; boundary=\"" + boundary + "\"";
    conn.content = new IOFile("src/test/resources/response.txt").read();

    new HttpResponse(null, ctx.options).getResponse(conn, true, null);
  }


  /**
   * Checks the response to an HTTP request.
   * @param v query result
   * @param itemsCount expected number of items
   * @param expStatus expected status
   */
  private static void checkResponse(final Value v, final int itemsCount, final int expStatus) {
    assertEquals(itemsCount, v.size());
    assertTrue(v.itemAt(0) instanceof FElem);
    final FElem response = (FElem) v.itemAt(0);
    assertNotNull(response.attributes());
    if(!eq(response.attribute(STATUS), token(expStatus))) {
      fail("Expected: " + expStatus + "\nFound: " + response);
    }
  }
}

/**
 * Fake HTTP connection.
 * @author BaseX Team 2005-16, BSD License
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
   * @param uri uri
   */
  FakeHttpConnection(final URL uri) {
    super(uri);
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
    final StringBuilder sb = new StringBuilder();
    for(final String v : values) sb.append(v).append(';');
    return sb.substring(0, sb.length() - 1);
  }

  @Override
  public OutputStream getOutputStream() {
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
