package org.basex.query.func;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.util.*;
import org.basex.query.*;
import org.basex.server.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the functions of the Client Module.
 *
 * @author BaseX Team 2005-14, BSD License
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
    stopServer(server);
  }

  /** Test method. */
  @Test
  public void connect() {
    query(conn());
    query(EXISTS.args(' ' + conn()));
    // BXCL0001: connection errors
    error(_CLIENT_CONNECT.args(Text.S_LOCALHOST, 9999, Text.S_ADMIN, ""), Err.BXCL_CONN);
    error(_CLIENT_CONNECT.args("xxx", 9999, Text.S_ADMIN, Text.S_ADMIN), Err.BXCL_CONN);
  }

  /** Test method. */
  @Test
  public void execute() {
    contains(_CLIENT_EXECUTE.args(conn(), new ShowUsers()), Text.S_USERINFO[0]);
    query("let $a := " + conn() + ", $b := " + conn() + " return (" +
        _CLIENT_EXECUTE.args("$a", new XQuery("1")) + ',' +
        _CLIENT_EXECUTE.args("$b", new XQuery("2")) + ')', "1 2");
    // BXCL0004: connection errors
    error(_CLIENT_EXECUTE.args(conn(), "x"), Err.BXCL_COMMAND);
  }

  /** Test method. */
  @Test
  public void info() {
    // check if the info string is not empty
    query("let $a := " + conn() + " return (" +
        _CLIENT_EXECUTE.args("$a", "xquery 123") + " and " +
        _CLIENT_INFO.args("$a") + ')', "true");
  }

  /** Test method. */
  @Test
  public void query() {
    contains(_CLIENT_EXECUTE.args(conn(), new ShowUsers()), Text.S_USERINFO[0]);
    query("let $a := " + conn() + ", $b := " + conn() + " return " +
        _CLIENT_QUERY.args("$a", "1") + '+' + _CLIENT_QUERY.args("$b", "2"), "3");
    query(_CLIENT_QUERY.args(conn(), "\"declare variable $a external; $a*2\"",
        " map { 'a': 1 }"), "2");
    // query errors
    error(_CLIENT_QUERY.args(conn(), "x"), Err.NOCTX);
    error(_CLIENT_QUERY.args(conn(), "\"$a\"", " map { 'a': (1,2) }"), Err.BXCL_ITEM);
  }

  /** Test method for the correct return of all XDM data types. */
  @Test
  public void queryTypes() {
    final Object[][] types = XdmInfoTest.TYPES;
    for(final Object[] type : types) {
      if(type == null || type.length < 3) continue;
      query(_CLIENT_QUERY.args(conn(), " \"" + type[1] + '"'), type[2]);
    }
  }

  /** Test method. */
  @Test
  public void close() {
    query(conn() + " ! " + _CLIENT_CLOSE.args(" ."));
    // BXCL0002: session not available
    error(_CLIENT_CLOSE.args("xs:anyURI('unknown')"), Err.BXCL_NOTAVL);
    // BXCL0002: session has already been closed
    error(conn() + " ! (" + _CLIENT_CLOSE.args(" .") + ", " +
        _CLIENT_CLOSE.args(" .") + ')', Err.BXCL_NOTAVL);
  }

  /**
   * Returns a successful connect string.
   * @return connect string
   */
  private static String conn() {
    return _CLIENT_CONNECT.args(Text.S_LOCALHOST, 9999, Text.S_ADMIN, Text.S_ADMIN);
  }
}
