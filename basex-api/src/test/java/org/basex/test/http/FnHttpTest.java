package org.basex.test.http;

import static org.basex.core.Text.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.Err.ErrType;
import org.basex.query.util.http.*;
import org.basex.query.util.http.HTTPRequest.Part;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the server-based HTTP Client.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public class FnHttpTest extends HTTPTest {
  /** Example url. */
  static final String ROOT = "http://" + LOCALHOST + ":9998/rest/";
  /** Example url. */
  static final String RESTURL = ROOT + NAME;

  /** Status code. */
  private static final byte[] STATUS = token("status");
  /** Body attribute media-type. */
  private static final byte[] MEDIATYPE = token("media-type");
  /** Body attribute method. */
  private static final byte[] METHOD = token("method");
  /** Books document. */
  private static final String BOOKS = "<books>" + "<book id='1'>"
      + "<name>Sherlock Holmes</name>" + "<author>Doyle</author>" + "</book>"
      + "<book id='2'>" + "<name>Winnetou</name>" + "<author>May</author>"
      + "</book>" + "<book id='3'>" + "<name>Tom Sawyer</name>"
      + "<author>Twain</author>" + "</book>" + "</books>";
  /** Carriage return/line feed. */
  private static final String CRLF = "\r\n";

  /** Local database context. */
  protected static Context ctx;

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
    final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='put' status-only='true'>"
        + "<http:body media-type='text/xml'>" + BOOKS + "</http:body>"
        + "</http:request>", RESTURL), ctx);
    checkResponse(qp.execute(), HttpURLConnection.HTTP_CREATED, 1);
    qp.close();
  }

  /**
   * Test sending of HTTP POST requests.
   * @throws Exception exception
   */
  @Test
  public void post() throws Exception {
    // POST - query
    QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='post'>"
        + "<http:body media-type='application/xml'>"
        + "<query xmlns='" + URL + "/rest'>"
        + "<text>1</text>"
        + "<parameter name='wrap' value='yes'/>"
        + "</query>" + "</http:body>"
        + "</http:request>", RESTURL), ctx);
    checkResponse(qp.execute(), HttpURLConnection.HTTP_OK, 2);
    qp.close();

    // Execute the same query but with content set from $bodies
    qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
       "<http:request method='post'>"
        + "<http:body media-type='application/xml'/></http:request>",
        RESTURL,
        "<query xmlns='" + URL + "/rest'>"
        + "<text>1</text>"
        + "<parameter name='wrap' value='yes'/>"
        + "</query>"), ctx);
    checkResponse(qp.execute(), HttpURLConnection.HTTP_OK, 2);
    qp.close();
  }

  /**
   * Test sending of HTTP GET requests.
   * @throws Exception exception
   */
  @Test
  public void postGet() throws Exception {
    // GET1 - just send a GET request
    QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='get' href='" + ROOT + "'/>"), ctx);
    Result r = qp.execute();
    checkResponse(r, HttpURLConnection.HTTP_OK, 2);

    assertEquals(NodeType.DOC, ((ValueBuilder) r).item[1].type);
    qp.close();

    // GET2 - with override-media-type='text/plain'
    qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='get' override-media-type='text/plain'/>", ROOT), ctx);
    r = qp.execute();
    checkResponse(r, HttpURLConnection.HTTP_OK, 2);

    assertEquals(AtomType.STR, ((ValueBuilder) r).item[1].type);
    qp.close();

    // Get3 - with status-only='true'
    qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='get' status-only='true'/>", ROOT), ctx);
    checkResponse(qp.execute(), HttpURLConnection.HTTP_OK, 1);
    qp.close();
  }

  /**
   * Test sending of HTTP DELETE requests.
   * @throws Exception exception
   */
  @Test
  public void postDelete() throws Exception {
    // add document to be deleted
    QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='put'>"
        + "<http:body media-type='text/xml'><ToBeDeleted/></http:body>"
        + "</http:request>", RESTURL), ctx);
    qp.execute();
    qp.close();

    // DELETE
    qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='delete' status-only='true'/>", RESTURL), ctx);
    checkResponse(qp.execute(), HttpURLConnection.HTTP_OK, 1);
    qp.close();
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
      assertTrue(contains(token(ex.getMessage()), token(ErrType.HC.toString())));
    }
  }

  /**
   * Tests http:send-request((),()).
   */
  @Test
  public void sendReqNoParams() {
    final Command c = new XQuery(_HTTP_SEND_REQUEST.args("()"));
    try {
      c.execute(ctx);
    } catch(final BaseXException ex) {
      assertTrue(contains(token(ex.getMessage()), token(ErrType.HC.toString())));
    }
  }

  /**
   * Tests an erroneous query.
   * @throws QueryException query exception
   */
  @Test
  public void error() throws QueryException {
    final QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        "<http:request method='get'/>", RESTURL + "unknown") + "[1]/@status/data()", ctx);
    assertEquals("404", qp.execute().toString());
    qp.close();
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
        + "method='POST' href='http://basex.org'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:body media-type='text/xml'>" + "Test body content"
        + "</http:body>" + "</http:request>";
    final DBNode dbNode = new DBNode(new IOContent(req), ctx.options);
    final HTTPRequestParser rp = new HTTPRequestParser(null);
    final HTTPRequest r = rp.parse(dbNode.children().next(), null);

    assertEquals(2, r.attrs.size());
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
        + "method='POST' href='http://basex.org'>"
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

    final DBNode dbNode1 = new DBNode(new IOContent(multiReq), ctx.options);
    final HTTPRequestParser rp = new HTTPRequestParser(null);
    final HTTPRequest r = rp.parse(dbNode1.children().next(), null);

    assertEquals(2, r.attrs.size());
    assertEquals(2, r.headers.size());
    assertTrue(r.isMultipart);
    assertEquals(3, r.parts.size());

    // check parts
    final Iterator<Part> i = r.parts.iterator();
    Part part;
    part = i.next();
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
        + "method='POST' href='http://basex.org'>"
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

    final DBNode dbNode1 = new DBNode(new IOContent(multiReq), ctx.options);
    final ValueBuilder bodies = new ValueBuilder();
    bodies.add(Str.get("Part1"));
    bodies.add(Str.get("Part2"));
    bodies.add(Str.get("Part3"));

    final HTTPRequestParser rp = new HTTPRequestParser(null);
    final HTTPRequest r = rp.parse(dbNode1.children().next(), bodies);

    assertEquals(2, r.attrs.size());
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
   * Tests if errors are thrown when some mandatory attributes are missing in a
   * <http:request/>, <http:body/> or <http:multipart/>.
   * @throws IOException I/O Exception
   */
  @Test
  public void errors() throws IOException {

    // Incorrect requests
    final List<byte[]> falseReqs = new ArrayList<byte[]>();

    // Request without method
    final byte[] falseReq1 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "href='http://basex.org'/>");
    falseReqs.add(falseReq1);

    // Request with send-authorization and no credentials
    final byte[] falseReq2 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='GET' href='http://basex.org' "
        + "send-authorization='true'/>");
    falseReqs.add(falseReq2);

    // Request with send-authorization and only username
    final byte[] falseReq3 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='GET' href='http://basex.org' "
        + "send-authorization='true' username='test'/>");
    falseReqs.add(falseReq3);

    // Request with body that has no media-type
    final byte[] falseReq4 = token("<http:request "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "method='POST' href='http://basex.org'>" + "<http:body>"
        + "</http:body>" + "</http:request>");
    falseReqs.add(falseReq4);

    // Request with multipart that has no media-type
    final byte[] falseReq5 = token("<http:request method='POST' "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "href='http://basex.org'>" + "<http:multipart boundary='xxx'>"
        + "</http:multipart>" + "</http:request>");
    falseReqs.add(falseReq5);

    // Request with multipart with part that has a body without media-type
    final byte[] falseReq6 = token("<http:request method='POST' "
        + "xmlns:http='http://expath.org/ns/http-client' "
        + "href='http://basex.org'>" + "<http:multipart boundary='xxx'>"
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
        + "method='DELETE' href='http://basex.org'>"
        + "<http:body media-type='text/plain'>" + "</http:body>"
        + "</http:request>");
    falseReqs.add(falseReq8);

    final Iterator<byte[]> i = falseReqs.iterator();
    while(i.hasNext()) {
      final DBNode dbNode = new DBNode(new IOContent(i.next()), ctx.options);
      try {
        final HTTPRequestParser rp = new HTTPRequestParser(null);
        rp.parse(dbNode.children().next(), null);
        fail("Exception not thrown");
      } catch(final QueryException ex) {
        assertTrue(contains(token(ex.getMessage()), token(ErrType.HC.toString())));
      }
    }

  }

  /**
   * Tests method setRequestContent of HttpClient.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void writeMultipartMessage() throws IOException, QueryException {
    final String plain = "...plain text....";
    final String rich = ".... richtext version...";
    final String fancy = ".... fanciest formatted version...";

    final HTTPRequest req = new HTTPRequest();
    req.isMultipart = true;
    req.payloadAttrs.put(token("media-type"), token("multipart/alternative"));
    req.payloadAttrs.put(token("boundary"), token("boundary42"));
    final Part p1 = new Part();
    p1.headers.put(token("Content-Type"), token("text/plain; charset=us-ascii"));
    p1.bodyAttrs.put(token("media-type"), token("text/plain"));
    p1.bodyContent.add(Str.get(plain + '\n'));

    final Part p2 = new Part();
    p2.headers.put(token("Content-Type"), token("text/richtext"));
    p2.bodyAttrs.put(token("media-type"), token("text/richtext"));
    p2.bodyContent.add(Str.get(rich));

    final Part p3 = new Part();
    p3.headers.put(token("Content-Type"), token("text/x-whatever"));
    p3.bodyAttrs.put(token("media-type"), token("text/x-whatever"));
    p3.bodyContent.add(Str.get(fancy));

    req.parts.add(p1);
    req.parts.add(p2);
    req.parts.add(p3);

    final FakeHttpConnection fakeConn = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    final HTTPClient hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn.getOutputStream(), req);
    final String expResult = "--boundary42" + CRLF
        + "Content-Type: text/plain; charset=us-ascii" + CRLF + CRLF
        + plain + Prop.NL + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/richtext" + CRLF + CRLF
        + rich + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/x-whatever" + CRLF + CRLF
        + fancy + CRLF
        + "--boundary42--" + CRLF;

    // Compare results
    final String fake = fakeConn.getOutputStream().toString();
    assertEquals(expResult, fake);
  }

  /**
   * Tests writing of request content with different combinations of the body
   * attributes media-type and method.
   * @throws IOException IO exception
   * @throws QueryException query exception
   */
  @Test
  public void writeMessage() throws IOException, QueryException {

    // Case 1: No method, media-type='text/xml'
    final HTTPRequest req1 = new HTTPRequest();
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    req1.payloadAttrs.put(MEDIATYPE, token("text/xml"));
    // Node child
    final FElem e1 = new FElem("a").add("a");
    req1.bodyContent.add(e1);
    // String item child
    req1.bodyContent.add(Str.get("<b>b</b>"));
    HTTPClient hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals("<a>a</a>&lt;b&gt;b&lt;/b&gt;", fakeConn1.out.toString());

    // Case 2: No method, media-type='text/plain'
    final HTTPRequest req2 = new HTTPRequest();
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    req2.payloadAttrs.put(MEDIATYPE, token("text/plain"));
    // Node child
    final FElem e2 = new FElem("a").add("a");
    req2.bodyContent.add(e2);
    // String item child
    req2.bodyContent.add(Str.get("<b>b</b>"));
    hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn2.getOutputStream(), req2);
    assertEquals("a<b>b</b>", fakeConn2.out.toString());

    // Case 3: method='text', media-type='text/xml'
    final HTTPRequest req3 = new HTTPRequest();
    final FakeHttpConnection fakeConn3 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    req3.payloadAttrs.put(MEDIATYPE, token("text/xml"));
    req3.payloadAttrs.put(token("method"), token("text"));
    // Node child
    final FElem e3 = new FElem("a").add("a");
    req3.bodyContent.add(e3);
    // String item child
    req3.bodyContent.add(Str.get("<b>b</b>"));
    hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn3.getOutputStream(), req3);
    assertEquals("a<b>b</b>", fakeConn3.out.toString());
  }

  /**
   * Tests writing of body content when @method is http:base64Binary.
   * @throws QueryException query exception
   * @throws IOException I/O Exception
   */
  @Test
  public void writeBase64() throws IOException, QueryException {
    // Case 1: content is xs:base64Binary
    final HTTPRequest req1 = new HTTPRequest();
    req1.payloadAttrs.put(METHOD, token("http:base64Binary"));
    req1.bodyContent.add(new B64(token("dGVzdA==")));
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    HTTPClient hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals(fakeConn1.out.toString(), "dGVzdA==");

    // Case 2: content is a node
    final HTTPRequest req2 = new HTTPRequest();
    req2.payloadAttrs.put(METHOD, token("http:base64Binary"));
    final FElem e3 = new FElem("a").add("dGVzdA==");
    req2.bodyContent.add(e3);
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn2.getOutputStream(), req2);
    assertEquals(fakeConn2.out.toString(), "dGVzdA==");
  }

  /**
   * Tests writing of body content when @method is http:hexBinary.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void writeHex() throws IOException, QueryException {
    // Case 1: content is xs:hexBinary
    final HTTPRequest req1 = new HTTPRequest();
    req1.payloadAttrs.put(METHOD, token("http:hexBinary"));
    req1.bodyContent.add(new Hex(token("74657374")));
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    HTTPClient hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals(fakeConn1.out.toString(), "74657374");

    // Case 2: content is a node
    final HTTPRequest req2 = new HTTPRequest();
    req2.payloadAttrs.put(METHOD, token("http:base64Binary"));
    final FElem e3 = new FElem("a").add("74657374");
    req2.bodyContent.add(e3);
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn2.getOutputStream(), req2);
    assertEquals(fakeConn2.out.toString(), "74657374");
  }

  /**
   * Tests writing of request content when @src is set.
   * @throws QueryException query exception
   * @throws IOException I/O Exception
   */
  @Test
  public void writeFromResource() throws IOException, QueryException {
    // Create a file form which will be read
    final File f = new File(Prop.TMP + Util.name(FnHttpTest.class));
    final FileOutputStream out = new FileOutputStream(f);
    out.write(token("test"));
    out.close();

    // Request
    final HTTPRequest req = new HTTPRequest();
    req.payloadAttrs.put(token("src"), token("file:" + f.getPath()));
    // HTTP connection
    final FakeHttpConnection fakeConn = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    final HTTPClient hc = new HTTPClient(null, ctx.options);
    hc.setRequestContent(fakeConn.getOutputStream(), req);

    // Delete file
    f.delete();

    assertEquals(fakeConn.out.toString(), "test");
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
    final FakeHttpConnection conn = new FakeHttpConnection(new URL(
        "http://www.test.com"));

    final String test = "\u0442\u0435\u0441\u0442";

    // Set content type
    conn.contentType = "text/plain; charset=CP1251";
    // set content encoded in CP1251
    conn.content = Charset.forName("CP1251").encode(test).array();
    final Iter i = new HTTPResponse(null, ctx.options).getResponse(
        conn, Bln.FALSE.string(), null);
    // compare results
    assertEquals(test, string(i.get(1).string(null)));
  }

  /**
   * Tests ResponseHandler.getResponse() with multipart response.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void multipartResponse() throws IOException, QueryException {
    // Create fake HTTP connection
    final FakeHttpConnection conn = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    final Map<String, List<String>> hdrs = new HashMap<String, List<String>>();
    final List<String> fromVal = new ArrayList<String>();
    fromVal.add("Nathaniel Borenstein <nsb@bellcore.com>");
    // From: Nathaniel Borenstein <nsb@bellcore.com>
    hdrs.put("From", fromVal);
    final List<String> mimeVal = new ArrayList<String>();
    mimeVal.add("1.0");
    // MIME-Version: 1.0
    hdrs.put("MIME-version", mimeVal);
    final List<String> subjVal = new ArrayList<String>();
    subjVal.add("Formatted text mail");
    // Subject: Formatted text mail
    hdrs.put("Subject", subjVal);
    final List<String> contTypeVal = new ArrayList<String>();
    contTypeVal.add("multipart/alternative");
    contTypeVal.add("boundary=\"boundary42\"");
    // Content-Type: multipart/alternative; boundary=boundary42
    hdrs.put("Content-Type", contTypeVal);

    conn.headers = hdrs;
    conn.contentType = "multipart/alternative; boundary=\"boundary42\"";
    conn.content = token("--boundary42" + CRLF
        + "Content-Type: text/plain; charset=us-ascii" + CRLF + CRLF
        + "...plain text version of message goes here...." + CRLF + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/richtext" + CRLF + CRLF
        + ".... richtext version of same message goes here ..." + CRLF
        + "--boundary42" + CRLF + "Content-Type: text/x-whatever" + CRLF + CRLF
        + ".... fanciest formatted version of same  "
        + "message  goes  here" + CRLF + "..."  + CRLF + "--boundary42--");
    final Iter i = new HTTPResponse(null, ctx.options).getResponse(
        conn, Bln.FALSE.string(), null);

    // Construct expected result
    final ValueBuilder resultIter = new ValueBuilder();
    final String reqItem = "<http:response "
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

    final DBNode dbNode = new DBNode(new IOContent(reqItem), ctx.options);
    resultIter.add(dbNode.children().next());
    resultIter.add(Str.get("...plain text version of message "
        + "goes here....\n\n"));
    resultIter.add(Str.get(".... richtext version of same message "
        + "goes here ...\n"));
    resultIter.add(Str.get(".... fanciest formatted version of same  "
        + "message  goes  here\n...\n"));

    // Compare response with expected result
    assertTrue(Compare.deep(resultIter, i, null));
  }

  /**
   * Tests ResponseHandler.getResponse() with multipart response having preamble
   * and epilogue.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void multipartRespPreamble() throws IOException, QueryException {

    // Create fake HTTP connection
    final FakeHttpConnection conn = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    final Map<String, List<String>> hdrs = new HashMap<String, List<String>>();
    final List<String> fromVal = new ArrayList<String>();
    fromVal.add("Nathaniel Borenstein <nsb@bellcore.com>");
    // From: Nathaniel Borenstein <nsb@bellcore.com>
    hdrs.put("From", fromVal);
    final List<String> mimeVal = new ArrayList<String>();
    mimeVal.add("1.0");
    final List<String> toVal = new ArrayList<String>();
    toVal.add("Ned Freed <ned@innosoft.com>");
    // To: Ned Freed <ned@innosoft.com>
    hdrs.put("To", toVal);
    // MIME-Version: 1.0
    hdrs.put("MIME-version", mimeVal);
    final List<String> subjVal = new ArrayList<String>();
    subjVal.add("Formatted text mail");
    // Subject: Formatted text mail
    hdrs.put("Subject", subjVal);
    final List<String> contTypeVal = new ArrayList<String>();
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
    final Iter i = new HTTPResponse(null, ctx.options).getResponse(
        conn, Bln.FALSE.string(), null);

    // Construct expected result
    final ValueBuilder resultIter = new ValueBuilder();
    final String reqItem = "<http:response "
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

    final DBNode dbNode = new DBNode(new IOContent(reqItem), ctx.options);
    resultIter.add(dbNode.children().next());
    resultIter.add(Str.get("This is implicitly typed plain ASCII text.\n"
        + "It does NOT end with a linebreak.\n"));
    resultIter.add(Str.get("This is explicitly typed plain ASCII text.\n"
        + "It DOES end with a linebreak.\n\n"));

    // Compare response with expected result
    assertTrue(Compare.deep(resultIter, i, null));
  }

  /**
   * Checks the response to an HTTP request.
   * @param r query result
   * @param expStatus expected status
   * @param itemsCount expected number of items
   * @throws QueryException query exception
   */
  static void checkResponse(final Result r, final int expStatus, final int itemsCount)
      throws QueryException {

    assertTrue(r instanceof Iter);
    final Iter res = (Iter) r;
    assertEquals(itemsCount, r.size());
    assertTrue(res.get(0) instanceof FElem);
    final FElem response = (FElem) res.get(0);
    assertNotNull(response.attributes());
    for(final ANode attr : response.attributes()) {
      if(eq(attr.name(), STATUS) && !eq(attr.string(), token(expStatus))) {
        fail("Expected: " + expStatus + "\nFound: " + string(attr.string()));
      }
    }
  }
}

/**
 * Fake HTTP connection.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
final class FakeHttpConnection extends HttpURLConnection {
  /** Request headers. */
  Map<String, List<String>> headers;
  /** Content-type. */
  String contentType;
  /** Content. */
  byte[] content;
  /** Connection output stream. */
  final ByteArrayOutputStream out;

  /**
   * Constructor.
   * @param u uri
   */
  FakeHttpConnection(final URL u) {
    super(u);
    out = new ByteArrayOutputStream();
    headers = new HashMap<String, List<String>>();
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
