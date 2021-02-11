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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the functions of the Client Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ClientModuleTest extends SandboxTest {
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void start() throws IOException {
    server = createServer();
  }

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    stopServer(server);
  }

  /** Test method. */
  @Test public void close() {
    final Function func = _CLIENT_CLOSE;
    // successful queries
    query(connection() + " ! " + func.args(" ."));
    // BXCL0002: session not available
    error(func.args(" xs:anyURI('unknown')"), CLIENT_ID_X);
    // BXCL0002: session has already been closed
    error(connection() + " ! (" + func.args(" .") + ", " + func.args(" .") + ')', CLIENT_ID_X);
  }

  /** Test method. */
  @Test public void connect() {
    final Function func = _CLIENT_CONNECT;
    // successful queries
    query(connection());
    query("exists(" + connection() + ")", true);
    // BXCL0001: connection errors
    error(func.args(Text.S_LOCALHOST, DB_PORT, ADMIN, ""), CLIENT_CONNECT_X);
    error(func.args("x\\o//x", DB_PORT, ADMIN, ADMIN), CLIENT_CONNECT_X);
  }

  /** Test method. */
  @Test public void execute() {
    final Function func = _CLIENT_EXECUTE;
    // successful queries
    contains(func.args(connection(), new ShowUsers()), S_USERINFO[0]);
    query("let $a :=" + connection() + ", $b :=" + connection() + " return (" +
        func.args(" $a", new XQuery("1")) + ',' +
        func.args(" $b", new XQuery("2")) + ')', "1\n2");
    // BXCL0004: connection errors
    error(func.args(connection(), "x"), CLIENT_COMMAND_X);
  }

  /** Test method. */
  @Test public void info() {
    final Function func = _CLIENT_INFO;
    // check if the info string is not empty
    query("let $a := " + connection() + " return (" + _CLIENT_EXECUTE.args(" $a", "xquery 123") +
        " and " + func.args(" $a") + ')', true);
  }

  /** Test method. */
  @Test public void query() {
    final Function func = _CLIENT_QUERY;
    // successful queries
    contains(_CLIENT_EXECUTE.args(connection(), new ShowUsers()), S_USERINFO[0]);
    // multiple sessions
    query("let $a := " + connection() + ", $b := " + connection() + " return " +
        func.args(" $a", "1") + '+' + func.args(" $b", "2"), 3);
    // arguments
    query(func.args(connection(), "declare variable $a external; $a*2",
        " map { 'a': 1 }"), 2);
    query(func.args(connection(), "declare variable $a external; count($a)",
        " map { 'a': () }"), 0);
    query(func.args(connection(), "declare variable $a external; count($a)",
        " map { 'a': (1 to 5) }"), 5);
    query(func.args(connection(), "declare context item external; .",
        " map { '': (1,<a/>,'a') }"), "1\n<a/>\na");
    // binary data
    query(func.args(connection(), "xs:hexBinary('41')"), "A");
    query(func.args(connection(), "xs:base64Binary('QQ==')"), "A");
    // serialization parameters (should be ignored)
    query(func.args(connection(),
      SerializerOptions.METHOD.arg("text") + "<xml/>"), "<xml/>");
    query(func.args(connection(),
      SerializerOptions.ENCODING.arg("US-ASCII") + "'\u00e4'"), "\u00e4");
    query(func.args(connection(), "xs:base64Binary('QQ==')"), "A");
    // query errors: returning function items
    error(func.args(connection(), "function() { }"), CLIENT_FITEM_X);
    error(func.args(connection(), "true#0"), CLIENT_FITEM_X);
    error(func.args(connection(), "array { }"), CLIENT_FITEM_X);
    error(func.args(connection(), "map { }"), CLIENT_FITEM_X);
    // query errors: server-side errors
    error(func.args(connection(), "x"), NOCTX_X);
  }

  /** Test method for the correct return of all XDM data types. */
  @Test public void queryTypes() {
    final Object[][] types = XdmInfoTest.TYPES;
    for(final Object[] type : types) {
      if(type == null || type.length < 3) continue;
      query(SerializerOptions.BINARY.arg("no") +
          _CLIENT_QUERY.args(connection(), " \"" + type[1] + '"'), type[2]);
    }
  }

  /**
   * Returns a successful connect string.
   * @return connect string
   */
  private static String connection() {
    return _CLIENT_CONNECT.args(Text.S_LOCALHOST, DB_PORT, ADMIN, ADMIN);
  }
}
