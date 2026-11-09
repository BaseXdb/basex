package org.basex.modules;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.core.cmd.XQuery;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.Token;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import com.icegreen.greenmail.junit5.*;
import com.icegreen.greenmail.util.*;

import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Mail test class, sending mails to an in-memory SMTP server.
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
   * Connects to the specified SSL port, and checks the identity of the server.
   */
  @Test public void ssl() {
    final GreenMail smtps = new GreenMail(ServerSetupTest.SMTPS);
    smtps.start();
    // trust the self-signed certificate, whose name does not match the host
    System.setProperty("mail.smtp.ssl.trust", "localhost");
    try {
      final QueryException ex = assertThrows(QueryException.class, () -> query(
          "{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'message': 'hi',"
          + " 'host': 'localhost', 'ssl': true(), 'port': " + smtps.getSmtps().getPort() + " }"));
      assertTrue(ex.getMessage().contains("No name matching localhost"), ex.getMessage());
      assertEquals(0, smtps.getReceivedMessages().length);
    } finally {
      System.clearProperty("mail.smtp.ssl.trust");
      smtps.stop();
    }
  }

  /**
   * Refuses to send a mail if STARTTLS is requested, but not supported by the server.
   */
  @Test public void tls() {
    final QueryException ex = assertThrows(QueryException.class, () -> query(
        "{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'message': 'hi', 'tls': true() }"));
    assertTrue(ex.getMessage().contains("STARTTLS is required"), ex.getMessage());
    assertEquals(0, SMTP.getReceivedMessages().length);
  }

  /**
   * Stopping a query aborts a pending connection.
   * @throws Exception exception
   */
  @Test public void stopQuery() throws Exception {
    final Context ctx = new Context();
    try(ServerSocket silent = new ServerSocket(0)) {
      final XQuery cmd = new XQuery(
          "import module namespace mail = 'http://basex.org/modules/mail'; mail:send("
          + "{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'host': 'localhost',"
          + " 'port': " + silent.getLocalPort() + ", 'timeout': 0 })");
      final AtomicReference<Throwable> caught = new AtomicReference<>();
      final Thread worker = new Thread(() -> {
        try {
          cmd.execute(ctx);
        } catch(final Throwable th) {
          caught.set(th);
        }
      });
      worker.start();
      // wait until the client has connected, then stop it while it waits for the greeting
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
    assertEquals("auth", code(ex));
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
   * Sends an HTML mail with a plain-text alternative.
   * @throws Exception exception
   */
  @Test public void html() throws Exception {
    query("{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'message': 'hi',"
        + " 'html': '<p>hi</p>' }");

    final MimeMessage message = SMTP.getReceivedMessages()[0];
    assertTrue(message.isMimeType("multipart/alternative"));
    final Multipart multipart = (Multipart) message.getContent();
    assertEquals(2, multipart.getCount());
    assertEquals("hi", ((String) multipart.getBodyPart(0).getContent()).trim());
    assertTrue(multipart.getBodyPart(1).isMimeType("text/html"));
    assertEquals("<p>hi</p>", ((String) multipart.getBodyPart(1).getContent()).trim());
  }

  /**
   * Sends attachments from a file URI and from binary and textual data, along with HTML.
   * @throws Exception exception
   */
  @Test public void dataAttachments() throws Exception {
    query("{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'message': 'hi', 'html': '<p/>',"
        + " 'attachment': ('" + ATTACHMENT.toUri() + "',"
        + " { 'name': 'a.bin', 'content': xs:base64Binary('AAEC') },"
        + " { 'name': 'b.txt', 'content': 'grün' },"
        + " { 'name': 'c.dat', 'content': xs:hexBinary('41'), 'type': 'text/x-test' },"
        + " { 'name': 'd.txt', 'content': 'grün', 'type': 'text/plain' }) }");

    final MimeMessage message = SMTP.getReceivedMessages()[0];
    final Multipart multipart = (Multipart) message.getContent();
    assertEquals(6, multipart.getCount());
    assertTrue(multipart.getBodyPart(0).isMimeType("multipart/alternative"));
    assertEquals(ATTACHMENT.getFileName().toString(), multipart.getBodyPart(1).getFileName());

    final BodyPart bin = multipart.getBodyPart(2), txt = multipart.getBodyPart(3);
    final BodyPart dat = multipart.getBodyPart(4);
    assertEquals("a.bin", bin.getFileName());
    try(InputStream is = bin.getInputStream()) {
      assertArrayEquals(new byte[] { 0, 1, 2 }, is.readAllBytes());
    }
    assertEquals("b.txt", txt.getFileName());
    assertTrue(txt.isMimeType("text/plain"));
    assertEquals("grün", txt.getContent());
    assertTrue(dat.isMimeType("text/x-test"));
    assertEquals(Part.ATTACHMENT, dat.getDisposition());
    // explicit type: charset is added for string content
    assertEquals("grün", multipart.getBodyPart(5).getContent());
  }

  /**
   * Rejects invalid attachment data.
   */
  @Test public void invalidAttachment() {
    assertThrows(QueryException.class, () -> query("{ 'from': 'from@basex.org',"
        + " 'to': 'to@basex.org', 'attachment': { 'name': 'a.txt' } }"));
    assertThrows(QueryException.class, () -> query("{ 'from': 'from@basex.org',"
        + " 'to': 'to@basex.org', 'attachment': { 'name': 'a.txt', 'content': 'x', 'size': 1 } }"));
    assertThrows(QueryException.class, () -> query("{ 'from': 'from@basex.org',"
        + " 'to': 'to@basex.org', 'attachment': { 'name': 'a.txt', 'content': 1 } }"));
    assertEquals(0, SMTP.getReceivedMessages().length);
  }

  /**
   * Sends custom headers, an envelope sender and a timeout.
   * @throws Exception exception
   */
  @Test public void headers() throws Exception {
    query("{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'message': 'hi',"
        + " 'headers': { 'X-Priority': '1', 'X-Tag': ('a', 'ä') },"
        + " 'bounce': 'bounce@basex.org', 'timeout': 5.5 }");

    final MimeMessage message = SMTP.getReceivedMessages()[0];
    assertEquals("1", message.getHeader("X-Priority")[0]);
    final String[] tags = message.getHeader("X-Tag");
    assertEquals(2, tags.length);
    assertEquals("a", tags[0]);
    assertEquals("ä", MimeUtility.decodeText(tags[1]));
    assertEquals("<bounce@basex.org>", message.getHeader("Return-Path")[0]);
  }

  /**
   * Converts internationalized domain names to ASCII.
   * @throws Exception exception
   */
  @Test public void idn() throws Exception {
    query("{ 'from': 'Jörg <from@müller.de>', 'to': 'to@müller.de', 'message': 'hi' }");

    final MimeMessage[] messages = SMTP.getReceivedMessages();
    assertEquals(1, messages.length);
    assertEquals("to@xn--mller-kva.de",
        ((InternetAddress) messages[0].getRecipients(Message.RecipientType.TO)[0]).getAddress());
    final InternetAddress from = (InternetAddress) messages[0].getFrom()[0];
    assertEquals("from@xn--mller-kva.de", from.getAddress());
    assertEquals("Jörg", from.getPersonal());
  }

  /**
   * Rejects a user without password.
   */
  @Test public void userWithoutPassword() {
    final QueryException ex = assertThrows(QueryException.class, () -> query(
        "{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'user': 'user', 'message': 'hi' }"));
    assertTrue(ex.getMessage().contains("password"), ex.getMessage());
    assertEquals("options", code(ex));
    assertEquals(0, SMTP.getReceivedMessages().length);
  }

  /**
   * Rejects a missing attachment.
   */
  @Test public void missingAttachment() {
    final QueryException ex = assertThrows(QueryException.class, () -> query(
        "{ 'from': 'from@basex.org', 'to': 'to@basex.org', 'attachment': 'unknown.jpg' }"));
    assertTrue(ex.getMessage().contains("Attachment not found"), ex.getMessage());
    assertEquals("not-found", code(ex));
    assertEquals(0, SMTP.getReceivedMessages().length);
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
    assertEquals("connect", code(ex));
  }

  /**
   * Rejects an invalid address.
   */
  @Test public void invalidAddress() {
    final QueryException ex = assertThrows(QueryException.class,
        () -> query("{ 'from': 'from@basex.org', 'to': 'to@@basex.org' }"));
    assertEquals("address", code(ex));
  }

  /**
   * Returns the local name of the error code, and checks its namespace URI.
   * @param ex query exception
   * @return local name
   */
  private static String code(final QueryException ex) {
    final QNm qname = ex.qname();
    assertEquals("http://basex.org/modules/mail", Token.string(qname.uri()));
    return Token.string(qname.local());
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
