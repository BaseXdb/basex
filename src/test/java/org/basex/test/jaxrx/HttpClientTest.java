package org.basex.test.jaxrx;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.basex.api.jaxrx.JaxRxServer;
import org.basex.build.Parser;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.XQuery;
import org.basex.data.XMLSerializer;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Bln;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.ValueIter;
import org.basex.query.util.Err;
import org.basex.query.util.ResponseHandler;
import org.basex.util.Token;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.org.apache.xerces.internal.parsers.XMLParser;

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

  private static final byte[] REQ = token("http:request");
  /** http:header element. */
  private static final byte[] HDR = token("http:header");
  /** Header attribute: name. */
  private static final byte[] HDR_NAME = token("name");
  /** Header attribute: value. */
  private static final byte[] HDR_VALUE = token("value");
  /** http:multipart element. */
  private static final byte[] MULTIPART = token("http:multipart");
  /** http:body element. */
  private static final byte[] BODY = token("http:body");
  /** Request attribute: HTTP method. */
  private static final byte[] METHOD = token("method");
  /** Request attribute: href. */
  private static final byte[] HREF = token("href");

  /** Database context. */
  protected static Context context;
  /** JAX-RX server. */
  private static JaxRxServer jaxrx;

  /** Multipart response. */
  private static final String multipart = "--boundary42\r\n"
      + "Content-Type: text/plain; charset=us-ascii\r\n\r\n"
      + "...plain text version of message goes here....'\r\n\r\n"
      + "--boundary42\r\n" + "Content-Type: text/richtext\r\n\r\n"

      + ".... richtext version of same message goes here ...\r\n"
      + "--boundary42\r\n" + "Content-Type: text/x-whatever\r\n\r\n"
      + ".... fanciest formatted version of same  message  goes  here\r\n"
      + "...\r\n" + "--boundary42--";

  /** Multipart response with preamble and epilogue. */
  private static final String multipart_preamble = "This is the preamble.  It is to be ignored, though it\r\n"
      + "is a handy place for mail composers to include an\r\n"
      + "explanatory note to non-MIME compliant readers.\r\n"
      + "--simple boundary\r\n\r\n"
      + "This is implicitly typed plain ASCII text.\r\n"
      + "It does NOT end with a linebreak.\r\n"
      + "--simple boundary\r\n"
      + "Content-type: text/plain; charset=us-ascii\r\n\r\n"
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

  /**
   * Test sending of HTTP request without any attributes - error shall be thrown
   * that mandatory attributes are missing.
   */
  @Test
  public void sendSimple() {
    final Command c = new XQuery("http:send-request(<http:request/>)");
    try {
      c.execute(context);
    } catch(BaseXException ex) {
      assertTrue(indexOf(token(ex.getMessage()),
          token(Err.ErrType.FOHC.toString())) != -1);
    }
  }

  @Test
  public void testRequestParser() {

    // Request attributes
    final NodeCache reqAttrs = new NodeCache();
    reqAttrs.add(new FAttr(new QNm(METHOD), token("POST"), null));
    reqAttrs.add(new FAttr(new QNm(HREF), token("http://www/test.com"), null));

    // Request children
    // 1) Headers + body
    final NodeCache resCh = new NodeCache();
    //Headers
    final NodeCache hdr1Attrs = new NodeCache();
    hdr1Attrs.add(new FAttr(new QNm(HDR_NAME), token("hdr1"), null));
    hdr1Attrs.add(new FAttr(new QNm(HDR_VALUE), token("hdr1val"), null));
    resCh.add(new FElem(new QNm(HDR), null, hdr1Attrs, null, null, null));
    final NodeCache hdr2Attrs = new NodeCache();
    hdr2Attrs.add(new FAttr(new QNm(HDR_NAME), token("hdr2"), null));
    hdr2Attrs.add(new FAttr(new QNm(HDR_VALUE), token("hdr2val"), null));
    resCh.add(new FElem(new QNm(HDR), null, hdr2Attrs, null, null, null));
    //Body
    

  }

  /**
   * Tests parsing of multipart response with headers and content.
   * @throws IOException IO Exception
   * @throws QueryException query exception
   */
  @Test
  public void testMultipartRespWithHdrs() throws IOException, QueryException {
    MyHttpConnection conn = new MyHttpConnection(new URL("http://www.test.com"));
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
    conn.content = token(multipart);
    Iter i = ResponseHandler.getResponse(conn, Bln.FALSE.atom(), null,
        context.prop, null);
    XMLSerializer ser = new XMLSerializer(System.out);
    Item it;
    while((it = i.next()) != null) {
      it.serialize(ser);
    }
    ser.close();

  }

  @Test
  public void testMutipartPreamble() throws IOException, QueryException {

    MyHttpConnection conn = new MyHttpConnection(new URL("http://www.test.com"));

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
    conn.content = token(multipart_preamble);
    Iter i = ResponseHandler.getResponse(conn, Bln.FALSE.atom(), null,
        context.prop, null);
    XMLSerializer ser = new XMLSerializer(System.out);
    Item it;
    while((it = i.next()) != null) {
      it.serialize(ser);
    }
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

  public Map<String, List<String>> headers;
  public String contentType;
  public byte[] content;

  public MyHttpConnection(final URL u) {
    super(u);
  }

  public ByteArrayInputStream getInputStream() {
    return new ByteArrayInputStream(content);
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

  public Map<String, List<String>> getHeaderFields() {
    return headers;
  }

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
