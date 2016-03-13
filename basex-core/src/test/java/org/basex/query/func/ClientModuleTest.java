package org.basex.query.func;

import static org.basex.core.users.UserText.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the functions of the Client Module.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ClientModuleTest extends AdvancedQueryTest {
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
    error(_CLIENT_CONNECT.args(Text.S_LOCALHOST, DB_PORT, ADMIN, ""), BXCL_CONN_X);
    error(_CLIENT_CONNECT.args("xxx", DB_PORT, ADMIN, ADMIN), BXCL_CONN_X);
  }

  /** Test method. */
  @Test
  public void execute() {
    contains(_CLIENT_EXECUTE.args(conn(), new ShowUsers()), S_USERINFO[0]);
    query("let $a := " + conn() + ", $b := " + conn() + " return (" +
        _CLIENT_EXECUTE.args("$a", new XQuery("1")) + ',' +
        _CLIENT_EXECUTE.args("$b", new XQuery("2")) + ')', "1\n2");
    // BXCL0004: connection errors
    error(_CLIENT_EXECUTE.args(conn(), "x"), BXCL_COMMAND_X);
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
    contains(_CLIENT_EXECUTE.args(conn(), new ShowUsers()), S_USERINFO[0]);
    // multiple sessions
    query("let $a := " + conn() + ", $b := " + conn() + " return " +
        _CLIENT_QUERY.args("$a", "1") + '+' + _CLIENT_QUERY.args("$b", "2"), "3");
    // arguments
    query(_CLIENT_QUERY.args(conn(), "\"declare variable $a external; $a*2\"",
        " map { 'a': 1 }"), "2");
    query(_CLIENT_QUERY.args(conn(), "\"declare variable $a external; count($a)\"",
        " map { 'a': () }"), "0");
    query(_CLIENT_QUERY.args(conn(), "\"declare variable $a external; count($a)\"",
        " map { 'a': (1 to 5) }"), "5");
    query(_CLIENT_QUERY.args(conn(), "\"declare context item external; .\"",
        " map { '': (1,<a/>,'a') }"), "1\n<a/>\na");
    // binary data
    query(_CLIENT_QUERY.args(conn(), "\"xs:hexBinary('41')\""), "A");
    query(_CLIENT_QUERY.args(conn(), "\"xs:base64Binary('QQ==')\""), "A");
    // serialization parameters (should be ignored)
    query(_CLIENT_QUERY.args(conn(),
        "\"" + SerializerOptions.METHOD.arg("text") + "<xml/>\""), "<xml/>");
    query(_CLIENT_QUERY.args(conn(),
        "\"" + SerializerOptions.ENCODING.arg("US-ASCII") + "'\u00e4'\""), "\u00e4");
    query(_CLIENT_QUERY.args(conn(), "\"xs:base64Binary('QQ==')\""), "A");
    // query errors: returning function items
    error(_CLIENT_QUERY.args(conn(), "\"function(){}\""), BXCL_FITEM_X);
    error(_CLIENT_QUERY.args(conn(), "true#0"), BXCL_FITEM_X);
    error(_CLIENT_QUERY.args(conn(), "[]"), BXCL_FITEM_X);
    error(_CLIENT_QUERY.args(conn(), "map{}"), BXCL_FITEM_X);
    // query errors: server-side errors
    error(_CLIENT_QUERY.args(conn(), "x"), NOCTX_X);
  }

  /** Test method for the correct return of all XDM data types. */
  @Test
  public void queryTypes() {
    final Object[][] types = XdmInfoTest.TYPES;
    for(final Object[] type : types) {
      if(type == null || type.length < 3) continue;
      query(SerializerOptions.BINARY.arg("no") +
          _CLIENT_QUERY.args(conn(), " \"" + type[1] + '"'), type[2]);
    }
  }

  /** Test method. */
  @Test
  public void close() {
    query(conn() + " ! " + _CLIENT_CLOSE.args(" ."));
    // BXCL0002: session not available
    error(_CLIENT_CLOSE.args("xs:anyURI('unknown')"), BXCL_NOTAVL_X);
    // BXCL0002: session has already been closed
    error(conn() + " ! (" + _CLIENT_CLOSE.args(" .") + ", " + _CLIENT_CLOSE.args(" .") + ')',
        BXCL_NOTAVL_X);
  }

  /**
   * Returns a successful connect string.
   * @return connect string
   */
  private static String conn() {
    return _CLIENT_CONNECT.args(Text.S_LOCALHOST, DB_PORT, ADMIN, ADMIN);
  }
}
