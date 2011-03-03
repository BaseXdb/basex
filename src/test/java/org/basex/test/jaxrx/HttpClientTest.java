package org.basex.test.jaxrx;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;
import java.net.HttpURLConnection;
import org.basex.api.jaxrx.JaxRxServer;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.XQuery;
import org.basex.query.QueryException;
import org.basex.query.item.FElem;
import org.basex.query.item.ANode;
import org.basex.query.item.Type;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.NodeIter;
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
    final Command put = new XQuery("http:send-request(" +
        "<http:request method='put' status-only='true'>" +
          "<http:body media-type='text/xml'>" +
            "<books>" +
              "<book id='1'>" +
                "<name>Sherlock Holmes</name>" +
                "<author>Doyle</author>" +
              "</book>" +
              "<book id='2'>" +
                "<name>Winnetou</name>" +
                "<author>May</author>" +
              "</book>" +
              "<book id='3'>" +
                "<name>Tom Sawyer</name>" +
                "<author>Twain</author>" +
              "</book>" +
            "</books>" +
          "</http:body>" +
        "</http:request>, 'http://localhost:8984/basex/jax-rx/books')");
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
    final Command postQuery = new XQuery("http:send-request(" +
        "<http:request method='post'>" +
          "<http:body media-type='application/query+xml'>" +
            "<query xmlns='http://jax-rx.sourceforge.net'>" +
              "<text>//book/name</text>" +
            "</query>" +
          "</http:body>" +
        "</http:request>, 'http://localhost:8984/basex/jax-rx/books')");
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
    final Command postAdd = new XQuery("http:send-request(" +
        "<http:request method='post' status-only='true'>" +
          "<http:body media-type='text/xml'>" +
            "<book id='4'>" +
              "<name>The Celebrated Jumping Frog of Calaveras County</name>" +
              "<author>Twain</author>" +
            "</book>" + "</http:body>" +
        "</http:request>, 'http://localhost:8984/basex/jax-rx/books')");
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
    final Command get1 = new XQuery("http:send-request(" +
        "<http:request method='get' " +
        "href='http://localhost:8984/basex/jax-rx/books'/>)");
    get1.execute(context);
    checkResponse(get1, HttpURLConnection.HTTP_OK, 2);
    assertTrue(((AxisIter) get1.result()).get(1).type == Type.DOC);

    // GET2 - with override-media-type='text/plain'
    final Command get2 = new XQuery("http:send-request(" +
        "<http:request method='get' override-media-type='text/plain'/>," +
        "'http://localhost:8984/basex/jax-rx/books')");
    get2.execute(context);
    checkResponse(get2, HttpURLConnection.HTTP_OK, 2);
    assertTrue(((AxisIter) get2.result()).get(1).type == Type.STR);

    // Get3 - with status-only='true'
    final Command get3 = new XQuery("http:send-request(" +
        "<http:request method='get' status-only='true'/>," +
        "'http://localhost:8984/basex/jax-rx/books')");
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
    final Command delete = new XQuery("http:send-request(" +
        "<http:request method='delete' status-only='true'/>, " +
        "'http://localhost:8984/basex/jax-rx/books')");
    delete.execute(context);
    checkResponse(delete, HttpURLConnection.HTTP_OK, 1);
  }
  
  @Test
  public void testMultipartPost() throws Exception {
    
  }

  /*
   * Test sending of HTTP requests.
   * @throws Exception exception
  @Test
  public void sendSimple() throws Exception {
    // causes a runtime exception
    new XQuery("http:send-request(<http:request/>)").execute(context);
  }
   */

  /**
   * Checks the response to an HTTP request.
   * @param c command
   * @param expStatus expected status
   * @param itemsCount expected number of items
   * @throws QueryException query exception
   */
  private void checkResponse(final Command c, final int expStatus,
      final int itemsCount) throws QueryException {
    assertTrue(c.result() instanceof AxisIter);
    final AxisIter res = (AxisIter) c.result();
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
