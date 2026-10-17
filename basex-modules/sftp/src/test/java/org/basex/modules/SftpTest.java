package org.basex.modules;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import org.apache.sshd.common.file.virtualfs.*;
import org.apache.sshd.server.*;
import org.apache.sshd.server.keyprovider.*;
import org.apache.sshd.sftp.server.*;
import org.basex.core.*;
import org.basex.query.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;

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

  /** Root directory of the SFTP server. */
  @TempDir
  static Path root;
  /** SFTP server. */
  private static SshServer server;

  /**
   * Starts the SFTP server.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void start() throws IOException {
    final SimpleGeneratorHostKeyProvider keys = new SimpleGeneratorHostKeyProvider();
    keys.setAlgorithm("EC");
    keys.setKeySize(256);

    server = SshServer.setUpDefaultServer();
    server.setHost("localhost");
    server.setPort(0);
    server.setKeyPairProvider(keys);
    server.setPasswordAuthenticator((user, password, session) ->
      USER.equals(user) && PASSWORD.equals(password));
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
        "sftp:list($s, '.')[. = 'x.txt']"));
    assertTrue(Files.exists(root.resolve("dir/x.txt")));
    query("sftp:delete($s, 'dir/x.txt'),", "sftp:delete-dir($s, 'dir')");
    assertFalse(Files.exists(root.resolve("dir")));
  }

  /**
   * Returns log entries.
   * @throws Exception exception
   */
  @Test public void logs() throws Exception {
    assertEquals("true", query("exists(sftp:logs())"));
  }

  /**
   * Reports errors.
   */
  @Test public void errors() {
    // missing file
    QueryException ex = assertThrows(QueryException.class, () ->
      query("sftp:get-text($s, 'missing.txt')"));
    assertTrue(ex.getMessage().contains("SFTP:"), ex.getMessage());
    // wrong password
    ex = assertThrows(QueryException.class, () -> run(
        "sftp:connect('localhost', '" + USER + "', 'wrong', " + server.getPort() + ", "
        + "{ 'StrictHostKeyChecking': 'no' })"));
    assertTrue(ex.getMessage().contains("SFTP:"), ex.getMessage());
    // unknown host key
    ex = assertThrows(QueryException.class, () -> run(
        "sftp:connect('localhost', '" + USER + "', '" + PASSWORD + "', " + server.getPort()
        + ")"));
    assertTrue(ex.getMessage().contains("SFTP:"), ex.getMessage());
  }

  /**
   * Connects to the server, runs the specified query lines and disconnects, even if an error
   * occurs.
   * @param lines query lines
   * @return serialized result
   * @throws Exception exception
   */
  private static String query(final String... lines) throws Exception {
    return run(
        "let $s := sftp:connect('localhost', '" + USER + "', '" + PASSWORD + "', "
        + server.getPort() + ", { 'StrictHostKeyChecking': 'no' })",
        "let $result := try {",
        String.join(" ", lines),
        "} catch * {",
        "  sftp:disconnect($s),",
        "  error($err:code, $err:description)",
        "}",
        "return ($result, sftp:disconnect($s))");
  }

  /**
   * Runs a query.
   * @param lines query lines
   * @return serialized result
   * @throws Exception exception
   */
  private static String run(final String... lines) throws Exception {
    final String query = "import module namespace sftp = 'http://basex.org/modules/sftp'; "
        + String.join(" ", lines);
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
