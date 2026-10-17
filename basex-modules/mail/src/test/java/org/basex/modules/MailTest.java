package org.basex.modules;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import com.icegreen.greenmail.junit5.*;
import com.icegreen.greenmail.util.*;

import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Mail test class, sending mails to an in-memory SMTP server. The {@link #main} method can be
 * used to send mails via an installed module and a real mail server, or to a local mock
 * server (argument {@code local}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MailTest {
  /** Test attachment. */
  private static final Path ATTACHMENT =
      Paths.get("src/test/xquery/org/basex/modules/mail/flower.jpg").toAbsolutePath();

  /** Mock SMTP server. */
  @RegisterExtension
  static final GreenMailExtension SMTP = new GreenMailExtension(ServerSetupTest.SMTP);

  /**
   * Test method.
   * @param args {@code local} to send the mails to a local mock server and list them; otherwise,
   *   they are sent to the default mail server.
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    final boolean local = args.length > 0 && "local".equals(args[0]);
    final GreenMail greenMail;
    if(local) {
      greenMail = new GreenMail(ServerSetupTest.SMTP);
      greenMail.start();
    } else {
      greenMail = null;
    }
    try {
      final int port = local ? greenMail.getSmtp().getPort() : 0;
      testJava(port);
      list(greenMail);
      testXQuery(port);
      list(greenMail);
    } finally {
      if(greenMail != null) greenMail.stop();
    }
  }

  /**
   * Java test method.
   * @param port port of the local mock server ({@code 0}: none, use the mail server specified
   *   below)
   * @throws Exception exception
   */
  public static void testJava(final int port) throws Exception {
    final MapBuilder map = new MapBuilder();
    map.put("from", "from@basex.org");
    map.put("to", "to@basex.org");
    map.put("subject", "Hi");
    map.put("message", "hi there");
    if(port != 0) {
      map.put("host", "localhost");
      map.put("port", Itr.get(port));
    }
    new Mail().send(map.map());
  }

  /**
   * Java XQuery test method.
   * @param port port of the local mock server ({@code 0}: none, use the mail server specified
   *   below)
   * @throws Exception exception
   */
  public static void testXQuery(final int port) throws Exception {
    final String server = port != 0 ? ", 'host': 'localhost', 'port': " + port : "";
    final String query =
      "import module namespace mail = 'http://basex.org/modules/mail'; " +
      "mail:send({ 'from': 'from@basex.org', 'to': 'to@basex.org', 'subject': 'Hi', " +
      "'message': 'hi there'" + server + " })";
    try(QueryProcessor qp = new QueryProcessor(query, new Context())) {
      System.out.println(qp.value().serialize());
    }
  }

  /**
   * Lists and removes the messages received by the mock server.
   * @param greenMail mock server ({@code null}: do nothing)
   * @throws Exception exception
   */
  private static void list(final GreenMail greenMail) throws Exception {
    if(greenMail == null) return;
    for(final MimeMessage message : greenMail.getReceivedMessages()) {
      System.out.println(GreenMailUtil.getWholeMessage(message));
    }
    greenMail.purgeEmailFromAllMailboxes();
  }

  /**
   * Sends a mail with all recipient types.
   * @throws Exception exception
   */
  @Test public void send() throws Exception {
    query("{ 'from': 'from@basex.org', 'to': ('to1@basex.org', 'to2@basex.org'),"
        + " 'cc': 'cc@basex.org', 'bcc': 'bcc@basex.org', 'reply-to': 'reply@basex.org',"
        + " 'subject': 'Hi', 'message': 'hi there' }");

    // one message per recipient
    final MimeMessage[] messages = SMTP.getReceivedMessages();
    assertEquals(4, messages.length);
    for(final MimeMessage message : messages) {
      assertEquals("Hi", message.getSubject());
      assertEquals("from@basex.org", message.getFrom()[0].toString());
      assertEquals("reply@basex.org", message.getReplyTo()[0].toString());
      assertEquals(2, message.getRecipients(Message.RecipientType.TO).length);
      assertEquals(1, message.getRecipients(Message.RecipientType.CC).length);
      assertEquals("hi there", text(message).trim());
    }
  }

  /**
   * Sends a mail with authentication: valid credentials are accepted, wrong ones are rejected.
   * @throws Exception exception
   */
  @Test public void authentication() throws Exception {
    SMTP.setUser("user@basex.org", "user", "secret");

    final String options = "{ 'from': 'from@basex.org', 'to': 'user@basex.org', 'user': 'user',"
        + " 'subject': 'Auth', 'message': 'hi', 'password': ";
    final QueryException ex = assertThrows(QueryException.class,
        () -> query(options + "'wrong' }"));
    assertTrue(ex.getMessage().contains("Mail:"), ex.getMessage());
    assertEquals(0, SMTP.getReceivedMessages().length);

    query(options + "'secret' }");
    assertEquals(1, SMTP.getReceivedMessages().length);
  }

  /**
   * Sends a mail with an attachment.
   * @throws Exception exception
   */
  @Test public void attachment() throws Exception {
    final String path = ATTACHMENT.toString().replace('\\', '/');
    query("{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'subject': 'Pic',"
        + " 'message': 'see attachment', 'attachment': '" + path + "' }");

    final MimeMessage[] messages = SMTP.getReceivedMessages();
    assertEquals(1, messages.length);
    assertEquals("see attachment", text(messages[0]).trim());
    final byte[] expected = Files.readAllBytes(ATTACHMENT);
    final Multipart multipart = (Multipart) messages[0].getContent();
    boolean found = false;
    for(int p = 0; p < multipart.getCount(); p++) {
      final BodyPart part = multipart.getBodyPart(p);
      if(ATTACHMENT.getFileName().toString().equals(part.getFileName())) {
        try(InputStream is = part.getInputStream()) {
          assertArrayEquals(expected, is.readAllBytes());
        }
        found = true;
      }
    }
    assertTrue(found, "attachment not found");
  }

  /**
   * Rejects unknown options.
   */
  @Test public void unknownOption() {
    final QueryException ex = assertThrows(QueryException.class, () -> query("{ 'unknown': 1 }"));
    assertTrue(ex.getMessage().contains("Unknown option"), ex.getMessage());
    assertEquals(0, SMTP.getReceivedMessages().length);
  }

  /**
   * Reports an unreachable server.
   */
  @Test public void unreachable() {
    final QueryException ex = assertThrows(QueryException.class, () -> query(
        "{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'subject': 'Hi', 'message': 'hi',"
        + " 'host': 'localhost', 'port': 1 }"));
    assertTrue(ex.getMessage().contains("Mail:"), ex.getMessage());
  }

  /**
   * Runs {@code mail:send} with the specified options. Unless specified, the mock SMTP server
   * is addressed.
   * @param options options map
   * @throws QueryException query exception
   */
  private static void query(final String options) throws QueryException {
    final String map = options.contains("'host'") ? options :
      "map:put(" + options + ", 'host', 'localhost') => map:put('port', "
      + SMTP.getSmtp().getPort() + ")";
    final String query = "import module namespace mail = 'http://basex.org/modules/mail'; "
        + "mail:send(" + map + ')';
    final Context ctx = new Context();
    try(QueryProcessor qp = new QueryProcessor(query, ctx)) {
      assertEquals(0, qp.value().size());
    } finally {
      ctx.close();
    }
  }

  /**
   * Returns the plain text of a message.
   * @param part message or body part
   * @return text
   * @throws Exception exception
   */
  private static String text(final Part part) throws Exception {
    if(part.isMimeType("text/plain")) return (String) part.getContent();
    final StringBuilder sb = new StringBuilder();
    if(part.isMimeType("multipart/*")) {
      final Multipart multipart = (Multipart) part.getContent();
      for(int p = 0; p < multipart.getCount(); p++) {
        final BodyPart body = multipart.getBodyPart(p);
        if(!Part.ATTACHMENT.equalsIgnoreCase(body.getDisposition())) sb.append(text(body));
      }
    }
    return sb.toString();
  }
}
