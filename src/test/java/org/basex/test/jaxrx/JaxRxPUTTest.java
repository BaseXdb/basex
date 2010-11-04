package org.basex.test.jaxrx;

import static org.junit.Assert.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
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
public final class JaxRxPUTTest extends JaxRxTest {
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

  /**
   * DELETE Test.
   * @throws Exception exception
   */
  @Test
  public void testDELETE() throws Exception {
    delete("/rest");
  }
}
