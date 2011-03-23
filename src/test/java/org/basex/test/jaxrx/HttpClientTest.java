package org.basex.test.jaxrx;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.basex.api.jaxrx.JaxRxServer;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.XQuery;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.func.FNSimple;
import org.basex.query.item.ANode;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FElem;
import org.basex.query.item.NodeType;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.ValueIter;
import org.basex.query.util.Err;
import org.basex.query.util.HTTPClient;
import org.basex.query.util.Request;
import org.basex.query.util.Request.Part;
import org.basex.query.util.RequestParser;
import org.basex.query.util.ResponseHandler;
import org.basex.util.Token;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the HTTP Client.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class HttpClientTest {
  /** Status code. */
  private static final byte[] STATUS = token("status");

  /** Database context. */
  protected static Context context;
  /** JAX-RX server. */
  private static JaxRxServer jaxrx;

  /**
   * Prepare test.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    context = new Context();
    context.prop.set(Prop.CACHEQUERY, true);
    jaxrx = new JaxRxServer("-z");
  }

  /**
   * Finish test.
   */
  @AfterClass
  public static void tearDownAfterClass() {
    context.close();
    jaxrx.stop();
  }

  /**
   * Test sending of HTTP PUT requests.
   * @throws Exception exception
   */
  @Test
  public void testPUT() throws Exception {
    final Command put = new XQuery("http:send-request("
        + "<http:request method='put' status-only='true'>"
        + "<http:body media-type='text/xml'>" + "<books>" + "<book id='1'>"
        + "<name>Sherlock Holmes</name>" + "<author>Doyle</author>" + "</book>"
        + "<book id='2'>" + "<name>Winnetou</name>" + "<author>May</author>"
        + "</book>" + "<book id='3'>" + "<name>Tom Sawyer</name>"
        + "<author>Twain</author>" + "</book>" + "</books>" + "</http:body>"
        + "</http:request>, 'http://localhost:8984/basex/jax-rx/books')");
    put.execute(context);
    checkResponse(put, HttpURLConnection.HTTP_CREATED, 1);
  }

  /**
   * Test sending of HTTP POST Query requests.
   * @throws Exception exception
   */
  @Test
  public void testPOSTQuery() throws Exception {
    // POST - query
    final Command postQuery = new XQuery("http:send-request("
        + "<http:request method='post'>"
        + "<http:body media-type='application/query+xml'>"
        + "<query xmlns='http://jax-rx.sourceforge.net'>"
        + "<text>//book/name</text>" + "</query>" + "</http:body>"
        + "</http:request>, 'http://localhost:8984/basex/jax-rx/books')");
    postQuery.execute(context);
    checkResponse(postQuery, HttpURLConnection.HTTP_OK, 2);
  }

  /**
   * Test sending of HTTP POST Add requests.
   * @throws Exception exception
   */
  @Test
  public void testPOSTAdd() throws Exception {
    // POST - add content
    final Command postAdd = new XQuery("http:send-request("
        + "<http:request method='post' status-only='true'>"
        + "<http:body media-type='text/xml'>" + "<book id='4'>"
        + "<name>The Celebrated Jumping Frog of Calaveras County</name>"
        + "<author>Twain</author>" + "</book>" + "</http:body>"
        + "</http:request>, 'http://localhost:8984/basex/jax-rx/books')");
    postAdd.execute(context);
    checkResponse(postAdd, HttpURLConnection.HTTP_CREATED, 1);
  }

  /**
   * Test sending of HTTP GET requests.
   * @throws Exception exception
   */
  @Test
  public void testPOSTGet() throws Exception {
    // GET1 - just send a GET request
    final Command get1 = new XQuery("http:send-request("
        + "<http:request method='get' "
        + "href='http://localhost:8984/basex/jax-rx/books'/>)");
    get1.execute(context);
    checkResponse(get1, HttpURLConnection.HTTP_OK, 2);

    assertTrue(((ItemCache) get1.result()).item[1].type == NodeType.DOC);

    // GET2 - with override-media-type='text/plain'
    final Command get2 = new XQuery("http:send-request("
        + "<http:request method='get' override-media-type='text/plain'/>,"
        + "'http://localhost:8984/basex/jax-rx/books')");
    get2.execute(context);
    checkResponse(get2, HttpURLConnection.HTTP_OK, 2);

    assertTrue(((ItemCache) get2.result()).item[1].type == AtomType.STR);

    // Get3 - with status-only='true'
    final Command get3 = new XQuery("http:send-request("
        + "<http:request method='get' status-only='true'/>,"
        + "'http://localhost:8984/basex/jax-rx/books')");
    get3.execute(context);
    checkResponse(get3, HttpURLConnection.HTTP_OK, 1);
  }

  /**
   * Test sending of HTTP DELETE requests.
   * @throws Exception exception
   */
  @Test
  public void testPOSTDelete() throws Exception {
    // DELETE
    final Command delete = new XQuery("http:send-request("
        + "<http:request method='delete' status-only='true'/>, "
        + "'http://localhost:8984/basex/jax-rx/books')");
    delete.execute(context);
    checkResponse(delete, HttpURLConnection.HTTP_OK, 1);
  }

  /**
   * Test sending of HTTP request without any attributes - error shall be thrown
   * that mandatory attributes are missing.
   */
  @Test
  public void sendSimple() {
    final Command c = new XQuery("http:send-request(<http:request/>)");
    try {
      c.execute(context);
    } catch(final BaseXException ex) {
      assertTrue(indexOf(token(ex.getMessage()),
          token(Err.ErrType.FOHC.toString())) != -1);
    }
  }

  /**
   * Tests RequestParser.parse() with normal(not multipart) request.
   * @throws IOException IO exception
   * @throws QueryException query exception
   */
  @Test
  public void testParseRequest() throws IOException, QueryException {

    // Simple HTTP request with no errors
    final byte[] req = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='POST' href='http://www.basex.org'>"
        + "<http:header name='hdr1' value='hdr1val'/>"
        + "<http:header name='hdr2' value='hdr2val'/>"
        + "<http:body media-type='text/xml'>" + "Test body content"
        + "</http:body>" + "</http:request>");
    final IO io = new IOContent(req);
    final Parser reqParser = Parser.xmlParser(io, context.prop, "");
    final DBNode dbNode = new DBNode(MemBuilder.build(reqParser, context.prop,
        ""), 0);
    final Request r = RequestParser.parse(dbNode.children().next(), null);

    assertTrue(r.attrs.size() == 2);
    assertTrue(r.headers.size() == 2);
    assertTrue(r.bodyContent.size() != 0);
    assertTrue(r.payloadAttrs.size() == 1);
  }

  /**
   * Tests RequestParser.parse() with multipart request.
   * @throws IOException IO exception
   * @throws QueryException query exception
   */
  @Test
  public void testParseMultipartReq() throws IOException, QueryException {
    final byte[] multiReq = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='POST' href='http://www.basex.org'>"
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
    final Parser p = Parser.xmlParser(io, context.prop, "");
    final DBNode dbNode = new DBNode(MemBuilder.build(p, context.prop, ""), 0);
    final Request r = RequestParser.parse(dbNode.children().next(), null);

    assertTrue(r.attrs.size() == 2);
    assertTrue(r.headers.size() == 2);
    assertTrue(r.isMultipart);
    assertTrue(r.parts.size() == 3);

    // check parts
    final Iterator<Part> i = r.parts.iterator();
    Part part = null;
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
   * Tests if errors are thrown when some mandatory attributes are missing in a
   * <http:request/>, <http:body/> or <http:multipart/>.
   * @throws IOException IO exception
   */
  @Test
  public void testCheckRequest() throws IOException {

    // Incorrect requests
    final List<byte[]> falseReqs = new ArrayList<byte[]>();

    // Request without method
    final byte[] falseReq1 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "href='http://www.basex.org'/>");
    falseReqs.add(falseReq1);

    // Request with send-authorization and no credentials
    final byte[] falseReq2 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='GET' href='http://www.basex.org' "
        + "send-authorization='true'/>");
    falseReqs.add(falseReq2);

    // Request with send-authorization and only username
    final byte[] falseReq3 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='GET' href='http://www.basex.org' "
        + "send-authorization='true' username='test'/>");
    falseReqs.add(falseReq3);

    // Request with body that has no media-type
    final byte[] falseReq4 = token("<http:request "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "method='POST' href='http://www.basex.org'>" + "<http:body>"
        + "</http:body>" + "</http:request>");
    falseReqs.add(falseReq4);

    // Request with multipart that has no media-type
    final byte[] falseReq5 = token("<http:request method='POST' "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "href='http://www.basex.org'>" + "<http:multipart boundary='xxx'>"
        + "</http:multipart>" + "</http:request>");
    falseReqs.add(falseReq5);

    // Request with multipart with part that has a body without media-type
    final byte[] falseReq6 = token("<http:request method='POST' "
        + "xmlns:http=\"http://expath.org/ns/http\" "
        + "href='http://www.basex.org'>" + "<http:multipart boundary='xxx'>"
        + "<part>" + "<http:header name='hdr1' value-='val1'/>"
        + "<http:body media-type='text/plain'>" + "Part1" + "</http:body>"
        + "</part>" + "<part>" + "<http:header name='hdr1' value-='val1'/>"
        + "<http:body>" + "Part1" + "</http:body>" + "</part>"
        + "</http:multipart>" + "</http:request>");
    falseReqs.add(falseReq6);

    final Iterator<byte[]> i = falseReqs.iterator();
    IO io = null;
    Parser p = null;
    DBNode dbNode;
    while(i.hasNext()) {
      io = new IOContent(i.next());
      p = Parser.xmlParser(io, context.prop, "");
      dbNode = new DBNode(MemBuilder.build(p, context.prop, ""), 0);
      try {
        RequestParser.parse(dbNode.children().next(), null);
      } catch(final QueryException ex) {
        assertTrue(indexOf(token(ex.getMessage()),
            token(Err.ErrType.FOHC.toString())) != -1);
      }
    }

  }

  /**
   * Tests method setRequestContent of HttpClient.
   * @throws IOException IO exception
   */
  @Test
  public void testWriteMultipartMessage() throws IOException {
    final Request req = new Request();
    req.isMultipart = true;
    req.payloadAttrs.add(token("media-type"), token("multipart/alternative"));
    req.payloadAttrs.add(token("boundary"), token("boundary42"));
    // TODO: compare with expected content - currently when the content of a
    // request body is text, it is serialized with the XMLSerializer and such
    // symbols as '\r' and <> are returned as entities.
    final Part p1 = new Part();
    p1.headers.add(token("Content-Type"), token("text/plain; "
        + "charset=us-ascii"));
    p1.bodyAttrs.add(token("media-type"), token("text/plain"));
    p1.bodyContent.add(Str.get(token("...plain text version of message "
        + "goes here....\n")));

    final Part p2 = new Part();
    p2.headers.add(token("Content-Type"), token("text/richtext"));
    p2.bodyAttrs.add(token("media-type"), token("text/richtext"));
    p2.bodyContent.add(Str.get(token(".... richtext version "
        + "of same message goes here ...")));

    final Part p3 = new Part();
    p3.headers.add(token("Content-Type"), token("text/x-whatever"));
    p3.bodyAttrs.add(token("media-type"), token("text/x-whatever"));
    p3.bodyContent.add(Str.get(token(".... fanciest formatted version "
        + "of same  message  goes  here...")));

    req.parts.add(p1);
    req.parts.add(p2);
    req.parts.add(p3);

    final FakeHttpConnection fakeConn = new FakeHttpConnection(new URL(
        "http://www.test.com"));
    HTTPClient.setRequestContent(fakeConn.getOutputStream(), req);
    System.out.println(fakeConn.out.toString());
  }

  /**
   * Tests ResponseHandler.getResponse() with multipart response.
   * @throws IOException IO exception
   * @throws QueryException query exception
   */
  @Test
  public void testGetMultipartResponse() throws IOException, QueryException {

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
    conn.content = token("--boundary42\r\n"
        + "Content-Type: text/plain; charset=us-ascii\r\n\r\n"
        + "...plain text version of message goes here....\r\n\r\n"
        + "--boundary42\r\n" + "Content-Type: text/richtext\r\n\r\n"

        + ".... richtext version of same message goes here ...\r\n"
        + "--boundary42\r\n" + "Content-Type: text/x-whatever\r\n\r\n"
        + ".... fanciest formatted version of same  "
        + "message  goes  here\n...\r\n" + "--boundary42--");
    final Iter i = ResponseHandler.getResponse(conn, Bln.FALSE.atom(null), null,
        context.prop, null);

    // Construct expected result
    final ItemCache resultIter = new ItemCache();
    final byte[] reqItem = token("<http:response "
        + "xmlns:http=\"http://expath.org/ns/http\" "
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
    final Parser reqParser = Parser.xmlParser(io, context.prop, "");
    final DBNode dbNode = new DBNode(MemBuilder.build(reqParser, context.prop,
        ""), 0);
    resultIter.add(dbNode.children().next());
    resultIter.add(Str.get(token("...plain text version of message "
        + "goes here....\n\n")));
    resultIter.add(Str.get(token(".... richtext version of same message "
        + "goes here ...\n")));
    resultIter.add(Str.get(token(".... fanciest formatted version of same  "
        + "message  goes  here\n...\n")));

    // Compare response with expected result
    assertTrue(FNSimple.deep(null, resultIter, i));
  }

  /**
   * Tests ResponseHandler.getResponse() with multipart response having preamble
   * and epilogue.
   * @throws IOException IO Exception
   * @throws QueryException query exception
   */
  @Test
  public void testGetMutipartRespPreamble() throws IOException, QueryException {

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
        + "It is to be ignored, though it\r\n"
        + "is a handy place for mail composers to include an\r\n"
        + "explanatory note to non-MIME compliant readers.\r\n"
        + "--simple boundary\r\n\r\n"
        + "This is implicitly typed plain ASCII text.\r\n"
        + "It does NOT end with a linebreak.\r\n" + "--simple boundary\r\n"
        + "Content-type: text/plain; charset=us-ascii\r\n\r\n"
        + "This is explicitly typed plain ASCII text.\r\n"
        + "It DOES end with a linebreak.\r\n\r\n" + "--simple boundary--\r\n"
        + "This is the epilogue.  It is also to be ignored.");
    // Get response as sequence of XQuery items
    final Iter i = ResponseHandler.getResponse(conn, Bln.FALSE.atom(null), null,
        context.prop, null);

    // Construct expected result
    final ItemCache resultIter = new ItemCache();
    final byte[] reqItem = token("<http:response "
        + "xmlns:http=\"http://expath.org/ns/http\" "
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
    final Parser reqParser = Parser.xmlParser(io, context.prop, "");
    final DBNode dbNode = new DBNode(MemBuilder.build(reqParser, context.prop,
        ""), 0);
    resultIter.add(dbNode.children().next());
    resultIter.add(Str.get(token("This is implicitly typed plain ASCII text.\n"
        + "It does NOT end with a linebreak.\n")));
    resultIter.add(Str.get(token("This is explicitly typed plain ASCII text.\n"
        + "It DOES end with a linebreak.\n\n")));

    // Compare response with expected result
    assertTrue(FNSimple.deep(null, resultIter, i));
  }

  /**
   * Checks the response to an HTTP request.
   * @param c command
   * @param expStatus expected status
   * @param itemsCount expected number of items
   * @throws QueryException query exception
   */
  private void checkResponse(final Command c, final int expStatus,
      final int itemsCount) throws QueryException {
    assertTrue(c.result() instanceof ValueIter);
    final ValueIter res = (ValueIter) c.result();
    assertEquals(itemsCount, res.size());
    assertTrue(res.get(0) instanceof FElem);
    final FElem response = (FElem) res.get(0);
    assertNotNull(response.atts());
    final NodeIter resAttr = response.atts();
    ANode attr = null;
    while((attr = resAttr.next()) != null) {
      if(Token.eq(attr.nname(), STATUS)) assertTrue(eq(attr.atom(),
          token(expStatus)));
    }
  }
}

/**
 * Fake HTTP connection.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
class FakeHttpConnection extends HttpURLConnection {

  /** Request headers. */
  public Map<String, List<String>> headers;
  /** Content-type. */
  public String contentType;
  /** Content. */
  public byte[] content;
  /** Connection output stream. */
  public ByteArrayOutputStream out;

  /**
   * Constructor.
   * @param u uri
   */
  public FakeHttpConnection(final URL u) {
    super(u);
    out = new ByteArrayOutputStream();
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
    final Iterator<String> i = values.iterator();
    while(i.hasNext()) {
      sb.append(i.next()).append(';');
    }
    return sb.substring(0, sb.length() - 1);
  }

  @Override
  public OutputStream getOutputStream() {
    return out;
  }

  @Override
  public void disconnect() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean usingProxy() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void connect() {
    // TODO Auto-generated method stub

  }

}
