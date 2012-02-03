package org.basex.test.rest;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.basex.api.BaseXHTTP;
import org.basex.build.Parser;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.data.Result;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.ANode;
import org.basex.query.item.AtomType;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Hex;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Compare;
import org.basex.query.util.Err;
import org.basex.query.util.http.HTTPClient;
import org.basex.query.util.http.Request;
import org.basex.query.util.http.Request.Part;
import org.basex.query.util.http.RequestParser;
import org.basex.query.util.http.ResponseHandler;
import org.basex.util.Util;
import org.basex.util.list.StringList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the server-based HTTP Client.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public class HTTPClientTest {
  /** Test database name. */
  private static final String DB = Util.name(HTTPClientTest.class);
  /** Status code. */
  private static final byte[] STATUS = token("status");
  /** Body attribute media-type. */
  private static final byte[] MEDIATYPE = token("media-type");
  /** Body attribute method. */
  private static final byte[] METHOD = token("method");
  /** Example url. */
  private static final String RESTURL =
      "http://" + LOCALHOST + ":9998/rest/" + DB;
  /** Books document. */
  private static final String BOOKS = "<books>" + "<book id='1'>"
      + "<name>Sherlock Holmes</name>" + "<author>Doyle</author>" + "</book>"
      + "<book id='2'>" + "<name>Winnetou</name>" + "<author>May</author>"
      + "</book>" + "<book id='3'>" + "<name>Tom Sawyer</name>"
      + "<author>Twain</author>" + "</book>" + "</books>";
  /** Carriage return/line feed. */
  private static final String CRLF = "\r\n";

  /** Database context. */
  private static final Context CONTEXT = new Context();
  /** HTTP servers. */
  private static BaseXHTTP http;

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
    CONTEXT.close();
  }

  /**
   * Creates a test database.
   * @throws BaseXException database exception
   */
  @Before
  public void init() throws BaseXException {
    new CreateDB(DB, BOOKS).execute(CONTEXT);
    new Close().execute(CONTEXT);
  }

  /**
   * Deletes the test database.
   * @throws BaseXException database exception
   */
  @After
  public void finish() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * Test sending of HTTP PUT requests.
   * @throws Exception exception
   */
  @Test
  public void put() throws Exception {
    final QueryProcessor qp = new QueryProcessor("http:send-request("
        + "<http:request method='put' status-only='true'>"
        + "<http:body media-type='text/xml'>" + BOOKS + "</http:body>"
        + "</http:request>, '" + RESTURL + "')", CONTEXT);
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
    QueryProcessor qp = new QueryProcessor("http:send-request("
        + "<http:request method='post'>"
        + "<http:body media-type='application/xml'>"
        + "<query xmlns='" + URL + "/rest'>"
        + "<text>1</text>"
        + "<parameter name='wrap' value='yes'/>"
        + "</query>" + "</http:body>"
        + "</http:request>, '" + RESTURL + "')", CONTEXT);
    checkResponse(qp.execute(), HttpURLConnection.HTTP_OK, 2);
    qp.close();

    // Execute the same query but with content set from $bodies
    qp = new QueryProcessor("http:send-request("
        + "<http:request method='post'>"
        + "<http:body media-type='application/xml'/></http:request>"
        + ", '" + RESTURL + "',"
        + "<query xmlns='" + URL + "/rest'>"
        + "<text>1</text>"
        + "<parameter name='wrap' value='yes'/>"
        + "</query>)", CONTEXT);
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
    QueryProcessor qp = new QueryProcessor("http:send-request("
        + "<http:request method='get' href='" + RESTURL + "'/>)", CONTEXT);
    Result r = qp.execute();
    checkResponse(r, HttpURLConnection.HTTP_OK, 2);

    assertEquals(NodeType.DOC, ((ItemCache) r).item[1].type);
    qp.close();

    // GET2 - with override-media-type='text/plain'
    qp = new QueryProcessor("http:send-request("
        + "<http:request method='get' override-media-type='text/plain'/>, '"
        + RESTURL + "')", CONTEXT);
    r = qp.execute();
    checkResponse(r, HttpURLConnection.HTTP_OK, 2);

    assertEquals(AtomType.STR, ((ItemCache) r).item[1].type);
    qp.close();

    // Get3 - with status-only='true'
    qp = new QueryProcessor("http:send-request("
        + "<http:request method='get' status-only='true'/>, '" + RESTURL + "')",
        CONTEXT);
    checkResponse(qp.execute(), HttpURLConnection.HTTP_OK, 1);
    qp.close();
  }

  /**
   * Test sending of HTTP DELETE requests.
   * @throws Exception exception
   */
  @Test
  public void postDelete() throws Exception {
    // DELETE
    final QueryProcessor qp = new QueryProcessor("http:send-request("
        + "<http:request method='delete' status-only='true'/>, '"
        + RESTURL + "')",
        CONTEXT);
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
      new XQuery("http:send-request(<http:request/>)").execute(CONTEXT);
    } catch(final BaseXException ex) {
      assertTrue(indexOf(token(ex.getMessage()),
          token(Err.ErrType.FOHC.toString())) != -1);
    }
  }

  /**
   * Tests http:send-request((),()).
   */
  @Test
  public void sendReqNoParams() {
    final Command c = new XQuery("http:send-request(())");
    try {
      c.execute(CONTEXT);
    } catch(final BaseXException ex) {
      assertTrue(indexOf(token(ex.getMessage()),
          token(Err.ErrType.FOHC.toString())) != -1);
    }
  }

  /**
   * Tests RequestParser.parse() with normal(not multipart) request.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void parseRequest() throws IOException, QueryException {
    // Simple HTTP request with no errors
    final byte[] req = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='POST' href='http://basex.org'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:body media-type='text/xml'>" + "Test body content"
        + "</http:body>" + "</http:request>");
    final IO io = new IOContent(req);
    final Parser reqParser = Parser.xmlParser(io, CONTEXT.prop);
    final DBNode dbNode = new DBNode(reqParser, CONTEXT.prop);
    final RequestParser rp = new RequestParser(null);
    final Request r = rp.parse(dbNode.children().next(), null);

    assertTrue(r.attrs.size() == 2);
    assertTrue(r.headers.size() == 2);
    assertTrue(r.bodyContent.size() != 0);
    assertTrue(r.payloadAttrs.size() == 1);
  }

  /**
   * Tests RequestParser.parse() with multipart request.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void parseMultipartReq() throws IOException, QueryException {
    final byte[] multiReq = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='POST' href='http://basex.org'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:multipart media-type='multipart/mixed' boundary='xxxx'>"
        + "<part>" + "<http:header name='p1hdr1' value='p1hdr1val'/>"
        + "<http:header name='p1hdr2' value='p1hdr2val'/>"
        + "<http:body media-type='text/plain'>" + "Part1" + "</http:body>"
        + "</part>" + "<part>"
        + "<http:header name='p2hdr1' value='p2hdr1val'/>"
        + "<http:body media-type='text/plain'>" + "Part2" + "</http:body>"
        + "</part>" + "<part>" + "<http:body media-type='text/plain'>"
        + "Part3" + "</http:body>" + "</part>" + "</http:multipart>"
        + "</http:request>");

    final IO io = new IOContent(multiReq);
    final Parser p = Parser.xmlParser(io, CONTEXT.prop);
    final DBNode dbNode1 = new DBNode(p, CONTEXT.prop);
    final RequestParser rp = new RequestParser(null);
    final Request r = rp.parse(dbNode1.children().next(), null);

    assertTrue(r.attrs.size() == 2);
    assertTrue(r.headers.size() == 2);
    assertTrue(r.isMultipart);
    assertTrue(r.parts.size() == 3);

    // check parts
    final Iterator<Part> i = r.parts.iterator();
    Part part;
    part = i.next();
    assertTrue(part.headers.size() == 2);
    assertTrue(part.bodyContent.size() == 1);
    assertTrue(part.bodyAttrs.size() == 1);

    part = i.next();
    assertTrue(part.headers.size() == 1);
    assertTrue(part.bodyContent.size() == 1);
    assertTrue(part.bodyAttrs.size() == 1);

    part = i.next();
    assertTrue(part.headers.size() == 0);
    assertTrue(part.bodyContent.size() == 1);
    assertTrue(part.bodyAttrs.size() == 1);
  }

  /**
   * Tests parsing of multipart request when the contents for each part are set
   * from the $bodies parameter.
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @Test
  public void parseMultipartReqBodies() throws IOException, QueryException {
    final byte[] multiReq = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='POST' href='http://basex.org'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:multipart media-type='multipart/mixed' boundary='xxxx'>"
        + "<part>" + "<http:header name='p1hdr1' value='p1hdr1val'/>"
        + "<http:header name='p1hdr2' value='p1hdr2val'/>"
        + "<http:body media-type='text/plain'/>" + "</part>" + "<part>"
        + "<http:header name='p2hdr1' value='p2hdr1val'/>"
        + "<http:body media-type='text/plain'/>" + "</part>" + "<part>"
        + "<http:body media-type='text/plain'/>" + "</part>"
        + "</http:multipart>" + "</http:request>");

    final IO io = new IOContent(multiReq);
    final Parser p = Parser.xmlParser(io, CONTEXT.prop);
    final DBNode dbNode1 = new DBNode(p, CONTEXT.prop);

    final ItemCache bodies = new ItemCache();
    bodies.add(Str.get("Part1"));
    bodies.add(Str.get("Part2"));
    bodies.add(Str.get("Part3"));

    final RequestParser rp = new RequestParser(null);
    final Request r = rp.parse(dbNode1.children().next(), bodies);

    assertTrue(r.attrs.size() == 2);
    assertTrue(r.headers.size() == 2);
    assertTrue(r.isMultipart);
    assertTrue(r.parts.size() == 3);

    // check parts
    final Iterator<Part> i = r.parts.iterator();
    Part part = i.next();
    assertTrue(part.headers.size() == 2);
    assertTrue(part.bodyContent.size() == 1);
    assertTrue(part.bodyAttrs.size() == 1);

    part = i.next();
    assertTrue(part.headers.size() == 1);
    assertTrue(part.bodyContent.size() == 1);
    assertTrue(part.bodyAttrs.size() == 1);

    part = i.next();
    assertTrue(part.headers.size() == 0);
    assertTrue(part.bodyContent.size() == 1);
    assertTrue(part.bodyAttrs.size() == 1);
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
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "href='http://basex.org'/>");
    falseReqs.add(falseReq1);

    // Request with send-authorization and no credentials
    final byte[] falseReq2 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='GET' href='http://basex.org' "
        + "send-authorization='true'/>");
    falseReqs.add(falseReq2);

    // Request with send-authorization and only username
    final byte[] falseReq3 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='GET' href='http://basex.org' "
        + "send-authorization='true' username='test'/>");
    falseReqs.add(falseReq3);

    // Request with body that has no media-type
    final byte[] falseReq4 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='POST' href='http://basex.org'>" + "<http:body>"
        + "</http:body>" + "</http:request>");
    falseReqs.add(falseReq4);

    // Request with multipart that has no media-type
    final byte[] falseReq5 = token("<http:request method='POST' "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "href='http://basex.org'>" + "<http:multipart boundary='xxx'>"
        + "</http:multipart>" + "</http:request>");
    falseReqs.add(falseReq5);

    // Request with multipart with part that has a body without media-type
    final byte[] falseReq6 = token("<http:request method='POST' "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "href='http://basex.org'>" + "<http:multipart boundary='xxx'>"
        + "<part>" + "<http:header name='hdr1' value-='val1'/>"
        + "<http:body media-type='text/plain'>" + "Part1" + "</http:body>"
        + "</part>" + "<part>" + "<http:header name='hdr1' value-='val1'/>"
        + "<http:body>" + "Part1" + "</http:body>" + "</part>"
        + "</http:multipart>" + "</http:request>");
    falseReqs.add(falseReq6);

    // Request with schema different from http
    final byte[] falseReq7 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "href='ftp://basex.org'/>");
    falseReqs.add(falseReq7);

    // Request with content and method which must be empty
    final byte[] falseReq8 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='DELETE' href='http://basex.org'>"
        + "<http:body media-type='text/plain'>" + "</http:body>"
        + "</http:request>");
    falseReqs.add(falseReq8);

    final Iterator<byte[]> i = falseReqs.iterator();
    IO io;
    Parser p;
    DBNode dbNode;
    byte[] it;
    while(i.hasNext()) {
      it = i.next();
      io = new IOContent(it);
      p = Parser.xmlParser(io, CONTEXT.prop);
      dbNode = new DBNode(p, CONTEXT.prop);
      try {
        final RequestParser rp = new RequestParser(null);
        rp.parse(dbNode.children().next(), null);
        fail("Exception not thrown");
      } catch(final QueryException ex) {
        assertTrue(indexOf(token(ex.getMessage()),
            token(Err.ErrType.FOHC.toString())) != -1);
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

    final Request req = new Request();
    req.isMultipart = true;
    req.payloadAttrs.add(token("media-type"), token("multipart/alternative"));
    req.payloadAttrs.add(token("boundary"), token("boundary42"));
    final Part p1 = new Part();
    p1.headers.add(token("Content-Type"), token("text/plain; "
        + "charset=us-ascii"));
    p1.bodyAttrs.add(token("media-type"), token("text/plain"));
    p1.bodyContent.add(Str.get(plain + '\n'));

    final Part p2 = new Part();
    p2.headers.add(token("Content-Type"), token("text/richtext"));
    p2.bodyAttrs.add(token("media-type"), token("text/richtext"));
    p2.bodyContent.add(Str.get(rich));

    final Part p3 = new Part();
    p3.headers.add(token("Content-Type"), token("text/x-whatever"));
    p3.bodyAttrs.add(token("media-type"), token("text/x-whatever"));
    p3.bodyContent.add(Str.get(fancy));

    req.parts.add(p1);
    req.parts.add(p2);
    req.parts.add(p3);

    final FakeHttpConnection fakeConn = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    final HTTPClient hc = new HTTPClient(null, CONTEXT.prop);
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
    final Request req1 = new Request();
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    req1.payloadAttrs.add(MEDIATYPE, token("text/xml"));
    // Node child
    final FElem e1 = new FElem(new QNm(token("a")));
    e1.add(new FTxt(token("a")));
    req1.bodyContent.add(e1);
    // String item child
    req1.bodyContent.add(Str.get("<b>b</b>"));
    HTTPClient hc = new HTTPClient(null, CONTEXT.prop);
    hc.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals("<a>a</a> &lt;b&gt;b&lt;/b&gt;", fakeConn1.out.toString());

    // Case 2: No method, media-type='text/plain'
    final Request req2 = new Request();
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    req2.payloadAttrs.add(MEDIATYPE, token("text/plain"));
    // Node child
    final FElem e2 = new FElem(new QNm(token("a")));
    e2.add(new FTxt(token("a")));
    req2.bodyContent.add(e2);
    // String item child
    req2.bodyContent.add(Str.get("<b>b</b>"));
    hc = new HTTPClient(null, CONTEXT.prop);
    hc.setRequestContent(fakeConn2.getOutputStream(), req2);
    assertEquals("a<b>b</b>", fakeConn2.out.toString());

    // Case 3: method='text', media-type='text/xml'
    final Request req3 = new Request();
    final FakeHttpConnection fakeConn3 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    req3.payloadAttrs.add(MEDIATYPE, token("text/xml"));
    req3.payloadAttrs.add(token("method"), token("text"));
    // Node child
    final FElem e3 = new FElem(new QNm(token("a")));
    e3.add(new FTxt(token("a")));
    req3.bodyContent.add(e3);
    // String item child
    req3.bodyContent.add(Str.get("<b>b</b>"));
    hc = new HTTPClient(null, CONTEXT.prop);
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
    final Request req1 = new Request();
    req1.payloadAttrs.add(METHOD, token("http:base64Binary"));
    req1.bodyContent.add(new B64(token("dGVzdA==")));
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    HTTPClient hc = new HTTPClient(null, CONTEXT.prop);
    hc.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals(fakeConn1.out.toString(), "dGVzdA==");

    // Case 2: content is a node
    final Request req2 = new Request();
    req2.payloadAttrs.add(METHOD, token("http:base64Binary"));
    final FElem e3 = new FElem(new QNm(token("a")));
    e3.add(new FTxt(token("dGVzdA==")));
    req2.bodyContent.add(e3);
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    hc = new HTTPClient(null, CONTEXT.prop);
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
    final Request req1 = new Request();
    req1.payloadAttrs.add(METHOD, token("http:hexBinary"));
    req1.bodyContent.add(new Hex(token("74657374")));
    final FakeHttpConnection fakeConn1 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    HTTPClient hc = new HTTPClient(null, CONTEXT.prop);
    hc.setRequestContent(fakeConn1.getOutputStream(), req1);
    assertEquals(fakeConn1.out.toString(), "74657374");

    // Case 2: content is a node
    final Request req2 = new Request();
    req2.payloadAttrs.add(METHOD, token("http:base64Binary"));
    final FElem e3 = new FElem(new QNm(token("a")));
    e3.add(new FTxt(token("74657374")));
    req2.bodyContent.add(e3);
    final FakeHttpConnection fakeConn2 = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    hc = new HTTPClient(null, CONTEXT.prop);
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
    final File f = new File(Prop.TMP + Util.name(HTTPClientTest.class));
    final FileOutputStream out = new FileOutputStream(f);
    out.write(token("test"));
    out.close();

    // Request
    final Request req = new Request();
    req.payloadAttrs.add(token("src"), token("file:" + f.getPath()));
    // HTTP connection
    final FakeHttpConnection fakeConn = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    final HTTPClient hc = new HTTPClient(null, CONTEXT.prop);
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
    final Iter i = new ResponseHandler(null, CONTEXT.prop).getResponse(
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
    final Iter i = new ResponseHandler(null, CONTEXT.prop).getResponse(
        conn, Bln.FALSE.string(), null);

    // Construct expected result
    final ItemCache resultIter = new ItemCache();
    final byte[] reqItem = token("<http:response "
        + "xmlns:http=\"http://expath.org/ns/http-client\" "
        + "status=\"200\" message=\"OK\">"
        + "<http:header name=\"Subject\" value=\"Formatted text mail\"/>"
        + "<http:header name=\"Content-Type\" "
        + "value=\"multipart/alternative;boundary=&quot;boundary42&quot;\"/>"
        + "<http:header name=\"MIME-version\" value=\"1.0\"/>"
        + "<http:header name=\"From\" value=\"Nathaniel Borenstein "
        + "&lt;nsb@bellcore.com&gt;\"/>"
        + "<http:multipart media-type=\"multipart/alternative\" "
        + "boundary=\"boundary42\">" + "<part>"
        + "<http:header name=\"Content-Type\" "
        + "value=\"text/plain; charset=us-ascii\"/>"
        + "<http:body media-type=\"text/plain; charset=us-ascii\"/>"
        + "</part>" + "<part>"
        + "<http:header name=\"Content-Type\" value=\"text/richtext\"/>"
        + "<http:body media-type=\"text/richtext\"/>" + "</part>" + "<part>"
        + "<http:header name=\"Content-Type\" value=\"text/x-whatever\"/>"
        + "<http:body media-type=\"text/x-whatever\"/>" + "</part>"
        + "</http:multipart>" + "</http:response> ");

    final IO io = new IOContent(reqItem);
    final Parser reqParser = Parser.xmlParser(io, CONTEXT.prop);
    final DBNode dbNode = new DBNode(reqParser, CONTEXT.prop);
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
    final Iter i = new ResponseHandler(null, CONTEXT.prop).getResponse(
        conn, Bln.FALSE.string(), null);

    // Construct expected result
    final ItemCache resultIter = new ItemCache();
    final byte[] reqItem = token("<http:response "
        + "xmlns:http=\"http://expath.org/ns/http-client\" "
        + "status=\"200\" message=\"OK\">"
        + "<http:header name=\"Subject\" value=\"Formatted text mail\"/>"
        + "<http:header name=\"To\" value=\"Ned "
        + "Freed &lt;ned@innosoft.com&gt;\"/>"
        + "<http:header name=\"Content-Type\" value=\"multipart/mixed;"
        + "boundary=&quot;simple boundary&quot;\"/>"
        + "<http:header name=\"MIME-version\" value=\"1.0\"/>"
        + "<http:header name=\"From\" value=\"Nathaniel Borenstein "
        + "&lt;nsb@bellcore.com&gt;\"/>"
        + "<http:multipart media-type=\"multipart/mixed\" "
        + "boundary=\"simple boundary\">" + "<part>"
        + "<http:body media-type=\"text/plain\"/>" + "</part>" + "<part>"
        + "<http:header name=\"Content-type\" value=\"text/plain; "
        + "charset=us-ascii\"/>"
        + "<http:body media-type=\"text/plain; charset=us-ascii\"/>"
        + "</part>" + "</http:multipart>" + "</http:response>");

    final IO io = new IOContent(reqItem);
    final Parser reqParser = Parser.xmlParser(io, CONTEXT.prop);
    final DBNode dbNode = new DBNode(reqParser, CONTEXT.prop);
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
  private static void checkResponse(final Result r, final int expStatus,
      final int itemsCount) throws QueryException {

    assertTrue(r instanceof Iter);
    final Iter res = (Iter) r;
    assertEquals(itemsCount, r.size());
    assertTrue(res.get(0) instanceof FElem);
    final FElem response = (FElem) res.get(0);
    assertNotNull(response.attributes());
    final NodeIter resAttr = response.attributes();
    for(ANode attr; (attr = resAttr.next()) != null;) {
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
