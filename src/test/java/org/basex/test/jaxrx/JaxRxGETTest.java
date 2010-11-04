package org.basex.test.jaxrx;

import static org.junit.Assert.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.Test;

/**
 * This class tests some JAX-RX features.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class JaxRxGETTest extends JaxRxTest {
  /** GET Test.
   * @throws Exception exception */
  @Test
  public void testGET() throws Exception {
    assertEquals("123", get("query=1+to+3&wrap=no"));
  }

  /** GET Test.
   * @throws Exception exception */
  @Test
  public void testGET2() throws Exception {
    assertEquals(WRAP1 + WRAP2, get("query=()"));
  }

  /** GET Test.
   * @throws IOException I/O exception */
  @Test
  public void testGETBind() throws IOException {
    assertEquals("123", get(
      "wrap=no&query=declare+variable+$x+external;$x&var=$x=123"));
  }

  /** GET Test.
   * @throws IOException I/O exception */
  @Test
  public void testGETBind2() throws IOException {
    assertEquals("124", get("wrap=no&var=x=123&" +
      "query=declare+variable+$x+as+xs:integer+external;$x%2b1"));
  }

  /** GET Test.*/
  @Test
  public void testGETErr1() {
    try {
      get("query=(");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /** GET Test.*/
  @Test
  public void testGETErr2() {
    try {
      get("query=()&output=wrp=no");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /** GET Test.*/
  @Test
  public void testGETErr3() {
    try {
      get("query=()&wrap=n");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /**
   * PUT Test.
   * @throws Exception exception
   */
  @Test
  public void testPUT() throws Exception {
    final URL url = new URL(ROOT + "/rest");
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");
    final OutputStream out = new BufferedOutputStream(conn.getOutputStream());
    final InputStream in = new BufferedInputStream(
      new FileInputStream("etc/xml/input.xml"));
    int i;
    while((i = in.read()) != -1) out.write(i);
    in.close();
    out.close();
    assertEquals(201, conn.getResponseCode());
    conn.disconnect();
  }
}
