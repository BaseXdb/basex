package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.basex.test.server.*;
import org.junit.*;

/**
 * This class tests the XQuery database functions prefixed with "client".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNClientTest extends AdvancedQueryTest {
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = createServer();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    server.stop();
  }

  /**
   * Test method for the connect() function.
   */
  @Test
  public void clientConnect() {
    check(_CLIENT_CONNECT);
    query(connect());
    query(EXISTS.args(" " + connect()));
    // BXCL0001: connection errors
    error(_CLIENT_CONNECT.args(Text.LOCALHOST, 9999, Text.ADMIN, ""), Err.BXCL_CONN);
    error(_CLIENT_CONNECT.args("xxx", 9999, Text.ADMIN, Text.ADMIN), Err.BXCL_CONN);
  }

  /**
   * Test method for the execute() function.
   */
  @Test
  public void clientExecute() {
    check(_CLIENT_EXECUTE);
    contains(_CLIENT_EXECUTE.args(connect(), new ShowUsers()), Text.USERHEAD[0]);
    query("let $a := " + connect() + ", $b := " + connect() + " return (" +
        _CLIENT_EXECUTE.args("$a", new XQuery("1")) + "," +
        _CLIENT_EXECUTE.args("$b", new XQuery("2")) + ")", "1 2");
    // BXCL0004: connection errors
    error(_CLIENT_EXECUTE.args(connect(), "x"), Err.BXCL_COMMAND);
  }

  /**
   * Test method for the query() function.
   */
  @Test
  public void clientQuery() {
    check(_CLIENT_QUERY);
    contains(_CLIENT_EXECUTE.args(connect(), new ShowUsers()), Text.USERHEAD[0]);
    query("let $a := " + connect() + ", $b := " + connect() + " return " +
        _CLIENT_QUERY.args("$a", "1") + "+" + _CLIENT_QUERY.args("$b", "2"), "3");
    query(_CLIENT_QUERY.args(connect(), "\"$a*2\"", " map{ 'a':=1 }"), "2");
    // query errors
    error(_CLIENT_QUERY.args(connect(), "x"), Err.XPNOCTX);
    error(_CLIENT_QUERY.args(connect(), "\"$a\"", " map{ 'a':=(1,2) }"), Err.BXCL_ITEM);
  }

  /**
   * Test method for the correct return of all XDM data types.
   */
  @Test
  public void clientQueryTypes() {
    final Object[][] types = XdmInfoTest.TYPES;
    for(final Object[] type : types) {
      if(type == null || type.length < 3) continue;
      query(_CLIENT_QUERY.args(connect(), " " + "\"" + type[1] + "\""), type[2]);
    }
  }

  /**
   * Test method for the close() function.
   */
  @Test
  public void clientClose() {
    check(_CLIENT_CLOSE);
    query(connect() + " ! " + _CLIENT_CLOSE.args(" ."));
    // BXCL0002: session not available
    error(_CLIENT_CLOSE.args("xs:anyURI('unknown')"), Err.BXCL_NOTAVL);
    // BXCL0002: session has already been closed
    error(connect() + " ! (" + _CLIENT_CLOSE.args(" .") + ", " +
        _CLIENT_CLOSE.args(" .") + ")", Err.BXCL_NOTAVL);
  }

  /**
   * Returns a successful connect string.
   * @return connect string
   */
  private static String connect() {
    return _CLIENT_CONNECT.args(Text.LOCALHOST, 9999, Text.ADMIN, Text.ADMIN);
  }
}
