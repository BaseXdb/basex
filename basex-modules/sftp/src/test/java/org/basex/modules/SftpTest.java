package org.basex.modules;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.apache.sshd.common.config.keys.*;
import org.apache.sshd.common.file.virtualfs.*;
import org.apache.sshd.server.*;
import org.apache.sshd.server.keyprovider.*;
import org.apache.sshd.sftp.server.*;
import org.basex.core.*;
import org.basex.core.cmd.XQuery;
import org.basex.io.out.ArrayOutput;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.*;
import org.junit.jupiter.api.io.*;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

/**
 * SFTP test class, accessing an in-memory SFTP server. The {@link #main} method can be used to
 * check the module in isolation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SftpTest {
  /** Test user. */
  private static final String USER = "user";
  /** Test password. */
  private static final String PASSWORD = "secret";
  /** Test user with an empty password. */
  private static final String ANONYMOUS = "anonymous";
  /** Module namespace URI. */
  private static final String URI = "http://basex.org/modules/sftp";
  /** Query prolog. */
  private static final String PROLOG = "import module namespace sftp = '" + URI + "'; ";
  /** Options entry that disables host key checking. */
  private static final String NO_CHECK = "'StrictHostKeyChecking': 'no'";

  /** Root directory of the SFTP server. */
  @TempDir
  static Path root;
  /** SFTP server. */
  private static SshServer server;
  /** Host keys of the SFTP server. */
  private static SimpleGeneratorHostKeyProvider keys;
  /** Public key of the test user (can be {@code null}). */
  private static PublicKey userKey;

  /**
   * Starts the SFTP server.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void start() throws IOException {
    keys = new SimpleGeneratorHostKeyProvider();
    keys.setAlgorithm("EC");
    keys.setKeySize(256);

    server = SshServer.setUpDefaultServer();
    server.setHost("localhost");
    server.setPort(0);
    server.setKeyPairProvider(keys);
    server.setPasswordAuthenticator((user, password, session) ->
      USER.equals(user) && PASSWORD.equals(password) ||
      ANONYMOUS.equals(user) && password.isEmpty());
    // password authentication only
    server.setKeyboardInteractiveAuthenticator(null);
    server.setPublickeyAuthenticator((user, key, session) ->
      USER.equals(user) && KeyUtils.compareKeys(key, userKey));
    server.setSubsystemFactories(List.of(new SftpSubsystemFactory()));
    server.setFileSystemFactory(new VirtualFileSystemFactory(root));
    server.start();
  }

  /**
   * Stops the SFTP server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    server.stop(true);
  }

  /**
   * Checks that no session is left open on the server.
   * @throws InterruptedException interrupted exception
   */
  @AfterEach public void sessions() throws InterruptedException {
    // the server closes sessions asynchronously
    for(int i = 0; i < 50 && !server.getActiveSessions().isEmpty(); i++) Thread.sleep(100);
    assertEquals(0, server.getActiveSessions().size(), "open sessions");
  }

  /**
   * Test method.
   * @param args ignored
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    testJava();
    testXQuery();
  }

  /**
   * Java test method.
   */
  public static void testJava() {
    new Sftp();
  }

  /**
   * Java XQuery test method.
   * @throws Exception exception
   */
  public static void testXQuery() throws Exception {
    final String query =
      "import module namespace sftp = 'org.basex.modules.sftp'; " +
      "()";
    final Context ctx = new Context();
    try(QueryProcessor qp = new QueryProcessor(query, ctx)) {
      System.out.println(qp.value().serialize());
    } finally {
      ctx.close();
    }
  }

  /**
   * Uploads, lists, retrieves, moves and deletes text files.
   * @throws Exception exception
   */
  @Test public void text() throws Exception {
    assertEquals("hello", query(
        "sftp:put-text($s, 'hello', 'a.txt'),",
        "sftp:get-text($s, 'a.txt')"));
    assertEquals("hello", Files.readString(root.resolve("a.txt")));

    assertEquals("a.txt", query("sftp:list($s, '.')[. = 'a.txt']"));
    assertEquals("5 false", query(
        "let $a := sftp:attributes($s, 'a.txt')",
        "return ($a?size, $a?dir)"));

    query("sftp:move($s, 'a.txt', 'b.txt')");
    assertFalse(Files.exists(root.resolve("a.txt")));
    assertTrue(Files.exists(root.resolve("b.txt")));
    query("sftp:delete($s, 'b.txt')");
    assertFalse(Files.exists(root.resolve("b.txt")));
  }

  /**
   * Uploads and retrieves binary data.
   * @throws Exception exception
   */
  @Test public void binary() throws Exception {
    assertEquals("AAEC/w==", query(
        "sftp:put-binary($s, xs:base64Binary('AAEC/w=='), 'bin'),",
        "xs:string(sftp:get-binary($s, 'bin'))"));
    assertArrayEquals(new byte[] { 0, 1, 2, -1 }, Files.readAllBytes(root.resolve("bin")));
  }

  /**
   * Transfers local files.
   * @param temp temporary directory
   * @throws Exception exception
   */
  @Test public void files(@TempDir final Path temp) throws Exception {
    final Path up = temp.resolve("up.txt"), down = temp.resolve("down.txt");
    Files.writeString(up, "local file", StandardCharsets.UTF_8);
    query("sftp:put-file($s, '" + path(up) + "', 'remote.txt'),",
        "sftp:get-file($s, 'remote.txt', '" + path(down) + "')");
    assertEquals("local file", Files.readString(root.resolve("remote.txt")));
    assertEquals("local file", Files.readString(down));
  }

  /**
   * Navigates through directories.
   * @throws Exception exception
   */
  @Test public void directories() throws Exception {
    Files.createDirectory(root.resolve("dir"));
    assertEquals("true", query("sftp:attributes($s, 'dir')?dir"));
    assertEquals("x.txt", query(
        "sftp:cd($s, 'dir'),",
        "sftp:put-text($s, 'x', 'x.txt'),",
        "sftp:list($s, '.')"));
    assertTrue(Files.exists(root.resolve("dir/x.txt")));
    query("sftp:delete($s, 'dir/x.txt'),", "sftp:delete-dir($s, 'dir')");
    assertFalse(Files.exists(root.resolve("dir")));
  }

  /**
   * Creates directories and checks for existence.
   * @throws Exception exception
   */
  @Test public void createDir() throws Exception {
    assertEquals("false true true", query(
        "sftp:exists($s, 'p/q/r'),",
        "sftp:create-dir($s, 'p/q/r'),",
        "sftp:exists($s, 'p/q/r'),",
        "sftp:create-dir($s, 'p/q'),",
        "sftp:exists($s, 'p')"));
    assertTrue(Files.isDirectory(root.resolve("p/q/r")));
  }

  /**
   * Lists directory entries with attributes.
   * @throws Exception exception
   */
  @Test public void entries() throws Exception {
    Files.createDirectories(root.resolve("e/sub"));
    Files.writeString(root.resolve("e/f.txt"), "abc");
    assertEquals("f.txt 3 false sub true", query(
        "for $e in sftp:entries($s, 'e')",
        "order by $e?name",
        "return ($e?name, if ($e?dir) then () else $e?size, $e?dir)"));
  }

  /**
   * Logs in with a private key and verifies the host key with a known-hosts file.
   * @param temp temporary directory
   * @throws Exception exception
   */
  @Test public void publicKey(@TempDir final Path temp) throws Exception {
    final Path identity = temp.resolve("id"), protectedId = temp.resolve("id-protected");
    final Path knownHosts = temp.resolve("known_hosts");
    final KeyPair user = KeyPair.genKeyPair(new JSch(), KeyPair.ECDSA, 256);
    user.writePrivateKey(identity.toString());
    user.writePrivateKey(protectedId.toString(), Token.token("pässword"));
    final ArrayOutput pub = new ArrayOutput();
    user.writePublicKey(pub, "");
    userKey = PublicKeyEntry.parsePublicKeyEntry(pub.toString().trim()).resolvePublicKey(
        null, null, null);

    final int port = server.getPort();
    final StringBuilder hosts = new StringBuilder();
    for(final java.security.KeyPair kp : keys.loadKeys(null)) {
      hosts.append("[localhost]:").append(port).append(' ');
      hosts.append(PublicKeyEntry.toString(kp.getPublic())).append('\n');
    }
    Files.writeString(knownHosts, hosts);
    assertEquals("true", run("sftp:exists(" + connect(port, "", "'IdentityFile': '"
        + path(identity) + "', 'KnownHosts': '" + path(knownHosts) + "'") + ", '.')"));

    final String id = "'IdentityFile': '" + path(protectedId) + "', " + NO_CHECK;
    assertEquals("true", run("sftp:exists(" + connect(port, "", id
        + ", 'Passphrase': 'pässword'") + ", '.')"));
    error("passphrase", () -> run(connect(port, "", id + ", 'Passphrase': 'wrong'")));
    error("passphrase", () -> run(connect(port, "", id)));
  }

  /**
   * Returns extended attributes.
   * @param temp temporary directory
   * @throws Exception exception
   */
  @Test public void attributes(@TempDir final Path temp) throws Exception {
    Files.createDirectory(root.resolve("attr"));
    Files.writeString(root.resolve("attr/f.txt"), "abc");
    assertEquals("3 false false true", query(
        "let $a := sftp:attributes($s, 'attr/f.txt')",
        "return ($a?size, $a?dir, $a?link, $a?last-accessed instance of xs:dateTime)"));

    // symbolic links require privileges on Windows
    try {
      Files.createSymbolicLink(root.resolve("attr/link"), root.resolve("attr/f.txt"));
      Files.createSymbolicLink(root.resolve("attr/broken"), temp.resolve("missing"));
    } catch(final IOException | UnsupportedOperationException ex) {
      Assumptions.abort("Symbolic links cannot be created: " + ex);
    }
    assertEquals("3 true", query(
        "let $a := sftp:attributes($s, 'attr/link')",
        "return ($a?size, $a?link)"));
    assertEquals("broken true f.txt false link true", query(
        "for $e in sftp:entries($s, 'attr') order by $e?name",
        "return ($e?name, $e?link)"));
  }

  /**
   * Reads and writes text in different encodings.
   * @throws Exception exception
   */
  @Test public void encoding() throws Exception {
    assertEquals("ä ä", query(
        "sftp:put-text($s, 'ä', 'latin1.txt', 'ISO-8859-1'),",
        "sftp:get-text($s, 'latin1.txt', 'ISO-8859-1'),",
        "sftp:append-text($s, 'ä', 'utf8.txt'),",
        "sftp:get-text($s, 'utf8.txt')"));
    assertArrayEquals(new byte[] { (byte) 0xE4 }, Files.readAllBytes(root.resolve("latin1.txt")));

    Files.write(root.resolve("invalid.txt"), new byte[] { 'a', 0 });
    error("encoding", () -> query("sftp:get-text($s, 'invalid.txt')"));
    error("encoding", () -> query("sftp:get-text($s, 'latin1.txt', 'unknown')"));
    error("encoding", () -> query("sftp:put-text($s, 'x', 'x.txt', 'unknown')"));
    error("encoding", () -> query("sftp:put-text($s, '€', 'x.txt', 'ISO-8859-1')"));
  }

  /**
   * Appends data.
   * @throws Exception exception
   */
  @Test public void append() throws Exception {
    assertEquals("ab", query(
        "sftp:append-text($s, 'a', 'append.txt'),",
        "sftp:append-text($s, 'b', 'append.txt', 'US-ASCII'),",
        "sftp:get-text($s, 'append.txt')"));
    assertEquals("AAE=", query(
        "sftp:append-binary($s, xs:base64Binary('AA=='), 'append.bin'),",
        "sftp:append-binary($s, xs:base64Binary('AQ=='), 'append.bin'),",
        "string(sftp:get-binary($s, 'append.bin'))"));
  }

  /**
   * Retrieves XML documents.
   * @throws Exception exception
   */
  @Test public void getXml() throws Exception {
    assertEquals("x", query(
        "sftp:put-text($s, '<a>x</a>', 'doc.xml'),",
        "sftp:get-xml($s, 'doc.xml')/a/string()"));
    error("parse", () -> query("sftp:put-text($s, '<a>', 'bad.xml'), sftp:get-xml($s, 'bad.xml')"));
  }

  /**
   * Returns the working directory and changes permissions.
   * @throws Exception exception
   */
  @Test public void pwdChmod() throws Exception {
    Files.createDirectories(root.resolve("wd"));
    assertEquals("true", query("sftp:cd($s, 'wd'), ends-with(sftp:pwd($s), '/wd')"));
    error("format", () -> query("sftp:put-text($s, 'x', 'mode.txt'),",
        "sftp:chmod($s, 'mode.txt', '9')"));
    query("sftp:put-text($s, 'x', 'mode.txt'), sftp:chmod($s, 'mode.txt', '640')");
  }

  /**
   * Returns log entries.
   * @throws Exception exception
   */
  @Test public void logs() throws Exception {
    assertEquals("true", query("exists(sftp:logs())"));
    assertEquals("true", run("empty(sftp:logs())"));
  }

  /**
   * Checks that internal methods are not exposed as functions.
   * @throws Exception exception
   */
  @Test public void internal() throws Exception {
    assertEquals("false false false false", run(
        "for $f in ('close', 'is-enabled', 'log', 'run')",
        "return some(0 to 2, fn($arity) {",
        "  exists(function-lookup(QName('" + URI + "', $f), $arity))",
        "})"));
  }

  /**
   * Reports errors.
   */
  @Test public void errors() {
    final int port = server.getPort();
    error("not-found", () -> query("sftp:get-text($s, 'missing.txt')"));
    error("no-dir", () -> query("sftp:put-text($s, 'x', 'file'), sftp:create-dir($s, 'file/x')"));
    error("connect", () -> run(connect(port, "wrong", NO_CHECK)));
    // unknown host key
    error("connect", () -> run(connect(port, PASSWORD, "")));
    error("options", () -> run(connect(port, PASSWORD, "'ProxyPort': 'x'")));
    error("options", () -> run(connect(port, PASSWORD, "'Passphrase': 'x'")));
  }

  /**
   * Logs in with an empty password.
   * @throws Exception exception
   */
  @Test public void emptyPassword() throws Exception {
    assertEquals("true", run("sftp:exists(sftp:connect('localhost', '" + ANONYMOUS + "', '', "
        + server.getPort() + ", { " + NO_CHECK + " }), '.')"));
  }

  /**
   * Aborts connections to an unresponsive server after the specified timeout.
   * @throws Exception exception
   */
  @Test public void timeout() throws Exception {
    try(ServerSocket silent = new ServerSocket(0)) {
      final long start = System.nanoTime();
      error("connect", () -> run(connect(silent.getLocalPort(), PASSWORD, "'Timeout': 500")));
      assertTrue(System.nanoTime() - start < 10_000_000_000L, "timeout ignored");
    }
    error("options", () -> run(connect(server.getPort(), PASSWORD, "'Timeout': -1")));
  }

  /**
   * Stopping a query aborts a pending connection.
   * @throws Exception exception
   */
  @Test public void stopQuery() throws Exception {
    final Context ctx = new Context();
    try(ServerSocket silent = new ServerSocket(0)) {
      final XQuery cmd = new XQuery(PROLOG + connect(silent.getLocalPort(), PASSWORD, ""));
      final AtomicReference<Throwable> caught = new AtomicReference<>();
      final Thread worker = new Thread(() -> {
        try {
          cmd.execute(ctx);
        } catch(final Throwable th) {
          caught.set(th);
        }
      });
      worker.start();
      // wait until the client has connected, then stop the pending handshake
      silent.setSoTimeout(10_000);
      try(Socket client = silent.accept()) {
        cmd.stop();
        worker.join(10_000);
      }
      assertFalse(worker.isAlive(), "connection was not aborted");
      assertNotNull(caught.get());
    } finally {
      ctx.close();
    }
  }

  /**
   * Closes sessions that are not disconnected by the query.
   * @throws Exception exception
   */
  @Test public void close() throws Exception {
    assertEquals("true", run("sftp:exists(" + connect(server.getPort(), PASSWORD, NO_CHECK)
        + ", '.')"));
  }

  /**
   * Checks that a query raises an error with the specified code in the module namespace.
   * @param code expected error code
   * @param query query
   */
  private static void error(final String code, final Executable query) {
    final QueryException ex = assertThrows(QueryException.class, query);
    assertEquals("sftp:" + code, Token.string(ex.qname().string()), ex.getMessage());
    assertEquals(URI, Token.string(ex.qname().uri()));
  }

  /**
   * Returns a connect expression for the test user.
   * @param port port
   * @param password password
   * @param options entries of the options map (empty string: no options)
   * @return expression
   */
  private static String connect(final int port, final String password, final String options) {
    return "sftp:connect('localhost', '" + USER + "', '" + password + "', " + port
        + (options.isEmpty() ? "" : ", { " + options + " }") + ')';
  }

  /**
   * Connects to the server, runs the specified query lines and disconnects.
   * @param lines query lines
   * @return serialized result
   * @throws Exception exception
   */
  private static String query(final String... lines) throws Exception {
    return run("let $s := " + connect(server.getPort(), PASSWORD, NO_CHECK),
        "return (", String.join(" ", lines), ", sftp:disconnect($s))");
  }

  /**
   * Runs a query.
   * @param lines query lines
   * @return serialized result
   * @throws Exception exception
   */
  private static String run(final String... lines) throws Exception {
    final String query = PROLOG + String.join(" ", lines);
    final Context ctx = new Context();
    try(QueryProcessor qp = new QueryProcessor(query, ctx)) {
      return qp.value().serialize().toString().replaceAll("\\s+", " ").trim();
    } finally {
      ctx.close();
    }
  }

  /**
   * Returns a path in a form that can be embedded in a query.
   * @param path path
   * @return string
   */
  private static String path(final Path path) {
    return path.toAbsolutePath().toString().replace('\\', '/');
  }
}
