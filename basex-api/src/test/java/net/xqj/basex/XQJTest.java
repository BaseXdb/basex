package net.xqj.basex;

import static org.junit.Assert.*;

import java.io.*;

import javax.xml.namespace.*;
import javax.xml.xquery.*;

import org.basex.*;
import org.junit.*;

/**
 * Performs simple tests on Charles Forster's XQJ client implementation for BaseX.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class XQJTest extends SandboxTest {
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the tests.
   * @throws IOException I/O exception
   */
  @BeforeClass public static void before() throws IOException {
    server = createServer();
  }

  /**
   * Finishes the tests.
   */
  @AfterClass public static void after() {
    stopServer(server);
  }

  /**
   * Simple query.
   * @throws Exception exception
   */
  @Test public void basicTest() throws Exception {
    final XQDataSource xqds = new BaseXXQDataSource();
    xqds.setProperty("serverName", "localhost");
    xqds.setProperty("port", String.valueOf(DB_PORT));

    final XQConnection conn = xqds.getConnection("admin", "admin");
    try {
      final XQPreparedExpression xqpe =
          conn.prepareExpression("declare variable $x as xs:string external; $x");
      xqpe.bindString(new QName("x"), "Hello World!", null);

      final XQResultSequence rs = xqpe.executeQuery();
      assertTrue(rs.next());
      assertEquals(rs.getItemAsString(null), "Hello World!");
    } finally {
      conn.close();
    }
  }

  /**
   * Simple query.
   * @throws Exception exception
   */
  @Test public void entityTest() throws Exception {
    final XQDataSource xqds = new BaseXXQDataSource();
    xqds.setProperty("serverName", "localhost");
    xqds.setProperty("port", String.valueOf(DB_PORT));

    final XQConnection conn = xqds.getConnection("admin", "admin");
    try {
      final XQPreparedExpression xqpe =
          conn.prepareExpression("declare variable $x as xs:string external; $x");
      xqpe.bindString(new QName("x"), "&amp;", null);

      final XQResultSequence rs = xqpe.executeQuery();
      assertTrue(rs.next());
      assertEquals(rs.getItemAsString(null), "&");
    } finally {
      conn.close();
    }
  }
}
