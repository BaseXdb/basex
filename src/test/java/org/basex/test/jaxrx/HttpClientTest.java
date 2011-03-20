package org.basex.test.jaxrx;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.basex.api.jaxrx.JaxRxServer;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.XQuery;
import org.basex.data.XMLSerializer;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.FElem;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.ValueIter;
import org.basex.query.util.ResponseHandler;
import org.basex.util.Token;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the HTTP Client.
 * 
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * 
 */
public final class HttpClientTest {
  /** Status code. */
  private static final byte[] STATUS = token("status");

  /** Database context. */
  protected static Context context;
  /** JAX-RX server. */
  private static JaxRxServer jaxrx;

  private static String multipart = "This is the preamble.  It is to be ignored, though it\r\n"
      + "is a handy place for mail composers to include an\r\n"
      + "explanatory note to non-MIME compliant readers.\r\n"
      + "--simple boundary\r\n\r\n"
      + "This is implicitly typed plain ASCII text.\r\n"
      + "It does NOT end with a linebreak.\r\n"
      + "--simple boundary\r\n"
      + "Content-type: text/xml; charset=us-ascii\r\n\r\n"
      + "This is explicitly typed plain ASCII text.\r\n"
      + "It DOES end with a linebreak.\r\n\r\n"
      + "--simple boundary--\r\n"
      + "This is the epilogue.  It is also to be ignored.";

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

  // @Test
  // public void testMultipartPost() throws BaseXException, QueryException {
  // final Command multipartPost = new XQuery("http:send-request("
  // + "<http:request method='post' status-only='true'>"
  // + "<http:multipart media-type='multipart/mixed' boundary='AaB03x'>"
  // + "<part>"
  // + "<http:header name='Content-Disposition' value='form-data'/>"
  // + "<http:body media-type='text/xml'>" + "<book id='35'>"
  // + "<name>The Celebrated Jumping Frog of Calaveras County</name>"
  // + "<author>Twain</author>" + "</book>" + "</http:body>" + "</part>"
  // + "<part>"
  // + "<http:header name='Content-Disposition' value='form-data'/>"
  // + "<http:header name='Content-Type' value='text/plain'/>"
  // + "<http:body media-type='text/xml'>" + "<book id='100'>"
  // + "<name>The Celebrated Jumping Frog of Calaveras County</name>"
  // + "<author>Twain</author>" + "</book>" + "</http:body>" + "</part>"
  // + "</http:multipart>"
  // + "</http:request>, 'http://localhost:8984/basex/jax-rx/books')");
  // multipartPost.execute(context);
  // //checkResponse(multipartPost, HttpURLConnection.HTTP_CREATED, 1);
  // System.out.println();
  // }

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
    assertTrue(((ItemCache) get1.result()).get(1).type == Type.DOC);

    // GET2 - with override-media-type='text/plain'
    final Command get2 = new XQuery("http:send-request("
        + "<http:request method='get' override-media-type='text/plain'/>,"
        + "'http://localhost:8984/basex/jax-rx/books')");
    get2.execute(context);
    checkResponse(get2, HttpURLConnection.HTTP_OK, 2);
    assertTrue(((ItemCache) get2.result()).get(1).type == Type.STR);

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

  // @Test
  // public void testMultipart() throws Exception {
  // final IOContent io = new IOContent(
  // token("<http:multipart media-type='multipart/form-data' boundary='AaB03x'>"
  // + "<part>"
  // + "<http:header name='Content-Disposition' value='form-data'/>"
  // + "<http:body media-type='text/plain'>"
  // + "Larry"
  // + "</http:body>"
  // + "</part>"
  // + "<part>"
  // + "<http:header name='Content-Disposition' value='form-data'/>"
  // + "<http:header name='Content-Type' value='text/plain'/>"
  // + "<http:body media-type='text/plain'>"
  // + "...file contents"
  // + "</http:body>" + "</part>" + "</http:multipart>"));

  // Request element
  // FElem req = new FElem(new QNm(token("http:request")), null);
  // req.atts.add(new FAttr(new QNm(token("method")), token("GET"), req));
  // req.atts.add(new FAttr(new QNm(token("href")), token("www.basex.org"),
  // req));
  //
  // // Multipart element
  // FElem multipart = new FElem(new QNm(token("http:multipart")), req);
  // multipart.atts.add(new FAttr(new QNm(token("media-type")),
  // token("multipart/form-data"), multipart));
  // multipart.atts.add(new FAttr(new QNm(token("boundary")), token("AaB03x"),
  // multipart));
  // req.children.add(multipart);
  // // Parts
  // // ---------
  // // Part 1
  // FElem part1 = new FElem(new QNm(token("part")), multipart);
  // FElem part1hdr1 = new FElem(new QNm(token("http:header")), part1);
  // part1hdr1.atts.add(new FAttr(new QNm(token("name")),
  // token("Content-Disposition"), part1hdr1));
  // part1hdr1.atts.add(new FAttr(new QNm(token("value")), token("form-data"),
  // part1hdr1));
  // FElem part1body = new FElem(new QNm(token("http:body")), part1);
  // part1body.atts.add(new FAttr(new QNm(token("media-type")),
  // token("text/plain"), part1body));
  // FTxt part1bodyCont = new FTxt(token("Larry"), part1body);
  // part1body.children.add(part1bodyCont);
  // part1.children.add(part1hdr1);
  // part1.children.add(part1body);
  // multipart.children.add(part1);
  // // Part 2
  // FElem part2 = new FElem(new QNm(token("part")), multipart);
  // FElem part2hdr1 = new FElem(new QNm(token("http:header")), part2);
  // part2hdr1.atts.add(new FAttr(new QNm(token("name")),
  // token("Content-Disposition"), part2hdr1));
  // part2hdr1.atts.add(new FAttr(new QNm(token("value")), token("form-data"),
  // part2hdr1));
  // FElem part2hdr2 = new FElem(new QNm(token("http:header")), part2);
  // part2hdr2.atts.add(new FAttr(new QNm(token("name")),
  // token("Content-Type"),
  // part2hdr2));
  // part2hdr2.atts.add(new FAttr(new QNm(token("value")),
  // token("text/plain"),
  // part2hdr2));
  // FElem part2body = new FElem(new QNm(token("http:body")), part2);
  // part2body.atts.add(new FAttr(new QNm(token("media-type")),
  // token("text/plain"), part2body));
  // part2body.children.add(new FTxt(token("...file contents"), part2body));
  // part2.children.add(part2hdr1);
  // part2.children.add(part2hdr2);
  // part2.children.add(part2body);
  // multipart.children.add(part2);
  //
  // final Parser parser = new XMLParser(io, null, context.prop);
  // ANode multipart = new DBNode(MemBuilder.build(parser, context.prop, ""),
  // 0);
  // HttpClient.writeMultipartContent(multipart.children().next(),
  // token("AaB03x"), null, System.out);
  //
  // }

  /*
   * Test sending of HTTP requests.
   * @throws Exception exception
   * @Test public void sendSimple() throws Exception { // causes a runtime
   * exception new
   * XQuery("http:send-request(<http:request/>)").execute(context); }
   */
  @Test
  public void testMultipartParser() throws IOException, QueryException {
    final FileInputStream io = new FileInputStream(
        "/home/hermione/workspace/HTTPModuleTests/multipart_preamle.txt");
    final ItemCache payloads = new ItemCache();
    final NodeCache result = ResponseHandler.extractParts(io, false, payloads,
        token("--simple boundary"), context.prop, null);
    final FElem el = new FElem(new QNm(token("multipart")), result, null,
        EMPTY, null, null);
    XMLSerializer ser = new XMLSerializer(System.out);
    el.serialize(ser);
    ser.close();
  }

  @Test
  public void testGetResponse() throws IOException, QueryException {
    MyHttpConnection conn = new MyHttpConnection(new URL("http://www.test.com"));
    Map<String, String> hdrs = new HashMap<String, String>();
    hdrs.put("Content-type", "multipart/mixed");
    conn.headers = hdrs;
    conn.contentType = "multipart/mixed; boundary=\"simple boundary\"";
    conn.in = new ByteArrayInputStream(token(multipart));
    Iter i = ResponseHandler.getResponse(conn, true, null, context.prop, null);
    XMLSerializer ser = new XMLSerializer(System.out);
    i.next().serialize(ser);
    ser.close();

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
 * @author hermione
 * 
 */
class MyHttpConnection extends HttpURLConnection {

  public Map<String, String> headers;
  public String contentType;
  public ByteArrayInputStream in;

  public MyHttpConnection(final URL u) {
    super(u);
  }

  public ByteArrayInputStream getInputStream() {
    return in;
  }

  public String getContentType() {
    return contentType;
  }

  public int getResponseCode() {
    return 200;
  }

  public String getResponseMessage() {
    return "OK";
  }

  public String getHeaderField(final String field) {
    return headers.get(field);
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
  public void connect() throws IOException {
    // TODO Auto-generated method stub

  }

}
