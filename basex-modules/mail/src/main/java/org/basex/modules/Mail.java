package org.basex.modules;

import static org.basex.modules.MailOptions.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.http.MediaType;
import org.eclipse.angus.mail.util.MailConnectException;

import jakarta.activation.*;
import jakarta.mail.*;
import jakarta.mail.Message.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.*;

/**
 * Mail package.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Mail extends QueryModule {
  /** Module prefix. */
  private static final byte[] PREFIX = Token.token("mail");
  /** Module namespace URI. */
  private static final byte[] URI = Token.token("http://basex.org/modules/mail");

  /**
   * Sends a mail. Example:
   * <pre>
   * {
   *   'from': 'john@server.net',
   *   'to': ('jean@client1.org', 'joe@client2.org'),
   *   'cc': 'joe@client3.org',
   *   'bcc': 'joe@client4.org',
   *   'user': 'thatsme',
   *   'password': 'topsecret',
   *   'subject': 'Vacation',
   *   'message': 'Hi all, I am leaving for vacation...',
   *   'html': '&lt;p&gt;Hi all, I am leaving for vacation...&lt;/p&gt;',
   *   'host': 'smtp.gmail.com',
   *   'port': 587,
   *   'tls': true(),
   *   'reply-to': '...',
   *   'attachment': ('/path/to/file.jpg', { 'name': 'list.txt', 'content': 'milk, eggs' }),
   *   'headers': { 'X-Priority': '1' },
   *   'timeout': 30
   * }
   * </pre>
   * @param map map
   * @throws QueryException query exception
   */
  public void send(final XQMap map) throws QueryException {
    final MailOptions mail = new MailOptions();
    mail.assign(map, queryContext, null);

    final String host = mail.get(HOST), user = mail.get(USER), password = mail.get(PASSWORD);
    final String from = mail.get(FROM);
    if((user == null) != (password == null)) {
      throw error("options", "User and password must be specified together.");
    }
    if(from == null) throw error("options", "No sender specified.");
    final boolean ssl = mail.get(SSL), tls = mail.get(TLS);
    final long timeout = ((Item) mail.get(TIMEOUT)).dec(null).multiply(Dec.BD_1000).longValue();

    final Properties props = new Properties(System.getProperties());
    if(host != null) props.setProperty("mail.smtp.host", host);
    if(props.getProperty("mail.smtp.host") == null && props.getProperty("mail.host") == null) {
      throw error("options", "No host specified.");
    }
    if(mail.contains(PORT)) props.setProperty("mail.smtp.port", mail.get(PORT).toString());
    if(mail.contains(BOUNCE)) props.setProperty("mail.smtp.from", mail.get(BOUNCE));
    props.setProperty("mail.smtp.ssl.enable", Boolean.toString(ssl));
    props.setProperty("mail.smtp.starttls.required", Boolean.toString(tls));
    props.setProperty("mail.smtp.ssl.checkserveridentity", "true");
    props.setProperty("mail.smtp.usesocketchannels", "true");
    if(timeout > 0) {
      for(final String key : new String[] { "connectiontimeout", "timeout", "writetimeout" }) {
        props.setProperty("mail.smtp." + key, Long.toString(timeout));
      }
    }

    final Thread ct = Thread.currentThread();
    final ClassLoader cl = ct.getContextClassLoader();
    ct.setContextClassLoader(getClass().getClassLoader());
    try {
      final MimeMessage msg = new MimeMessage(Session.getInstance(props));
      msg.setFrom(address(from));
      msg.setRecipients(RecipientType.TO, addresses(mail.get(TO)));
      msg.setRecipients(RecipientType.CC, addresses(mail.get(CC)));
      msg.setRecipients(RecipientType.BCC, addresses(mail.get(BCC)));
      if(msg.getAllRecipients() == null) throw error("options", "No recipient specified.");
      msg.setReplyTo(addresses(mail.get(REPLY_TO)));
      if(mail.contains(SUBJECT)) msg.setSubject(mail.get(SUBJECT), Strings.UTF8);
      headers(msg, mail.get(HEADERS));

      final String text = mail.get(MESSAGE), html = mail.get(HTML);
      final Value attachments = mail.get(ATTACHMENT);
      if(attachments.isEmpty()) {
        body(msg, text, html);
      } else {
        final MimeMultipart mixed = new MimeMultipart();
        final MimeBodyPart body = new MimeBodyPart();
        body(body, text, html);
        mixed.addBodyPart(body);
        for(final Item item : attachments) mixed.addBodyPart(attachment(item));
        msg.setContent(mixed);
      }

      // socket channels are interruptible: stopping the query aborts the connection
      Job.run(() -> {
        try {
          if(user != null) Transport.send(msg, user, password);
          else Transport.send(msg);
        } catch(final MessagingException ex) {
          throw new IOException(ex);
        }
        return null;
      });
    } catch(final IOException ex) {
      throw error(ex.getCause() instanceof final MessagingException me ? me : ex);
    } catch(final MessagingException | InterruptedException ex) {
      throw error(ex);
    } finally {
      ct.setContextClassLoader(cl);
    }
  }

  /**
   * Assigns the body to a message or body part: plain text, or text and HTML as alternatives.
   * @param part message or body part
   * @param text plain text
   * @param html HTML text (can be {@code null})
   * @throws MessagingException messaging exception
   */
  private static void body(final MimePart part, final String text, final String html)
      throws MessagingException {
    if(html == null) {
      part.setText(text, Strings.UTF8);
    } else {
      final MimeMultipart alternative = new MimeMultipart("alternative");
      final MimeBodyPart textPart = new MimeBodyPart(), htmlPart = new MimeBodyPart();
      textPart.setText(text, Strings.UTF8);
      htmlPart.setText(html, Strings.UTF8, "html");
      alternative.addBodyPart(textPart);
      alternative.addBodyPart(htmlPart);
      part.setContent(alternative);
    }
  }

  /**
   * Adds headers to a message.
   * @param msg message
   * @param headers map with headers (empty sequence: none)
   * @throws QueryException query exception
   * @throws MessagingException messaging exception
   * @throws IOException I/O exception
   */
  private static void headers(final MimeMessage msg, final Value headers)
      throws QueryException, MessagingException, IOException {
    if(!(headers instanceof final XQMap map)) return;
    for(final Item key : map.keys()) {
      final String name = Token.string(key.string(null));
      msg.removeHeader(name);
      for(final Item value : map.get(key)) {
        msg.addHeader(name, MimeUtility.fold(name.length() + 2,
            MimeUtility.encodeText(Token.string(value.string(null)), Strings.UTF8, null)));
      }
    }
  }

  /**
   * Creates an attachment.
   * @param item file path or map with attachment data
   * @return body part
   * @throws QueryException query exception
   * @throws MessagingException messaging exception
   * @throws IOException I/O exception
   */
  private MimeBodyPart attachment(final Item item)
      throws QueryException, MessagingException, IOException {
    final MimeBodyPart part = new MimeBodyPart();
    if(item instanceof final XQMap map) {
      final AttachmentOptions options = new AttachmentOptions();
      options.assign(map, queryContext, null);
      final String name = options.get(AttachmentOptions.NAME);
      final Value content = options.get(AttachmentOptions.CONTENT);
      if(name == null || content.isEmpty()) {
        throw error("options", "Attachments need a name and content.");
      }
      final String type = options.get(AttachmentOptions.TYPE);
      final MediaType mt = type != null ? new MediaType(type) : MediaType.get(name);
      final byte[] data;
      String ct = mt.toString();
      if(content instanceof final Bin bin) {
        data = bin.binary(null);
      } else {
        // strings are encoded as UTF-8
        data = ((Item) content).string(null);
        if(mt.isTextual() && mt.parameter("charset") == null) ct += "; charset=UTF-8";
      }
      part.setDataHandler(new DataHandler(new ByteArrayDataSource(data, ct)));
      part.setFileName(name);
    } else {
      final String path = Token.string(item.string(null)).trim();
      final File file = IOUrl.isFileURL(path) ? ((IOFile) IO.get(path)).file() : new File(path);
      if(!file.isFile()) throw error("not-found", "Attachment not found: " + path);
      part.attachFile(file, MediaType.get(file.getName()).toString(), null);
    }
    return part;
  }

  /**
   * Parses addresses.
   * @param strings address strings
   * @return addresses
   * @throws AddressException address exception
   */
  private static InternetAddress[] addresses(final String[] strings) throws AddressException {
    final int sl = strings.length;
    final InternetAddress[] addresses = new InternetAddress[sl];
    for(int s = 0; s < sl; s++) addresses[s] = address(strings[s]);
    return addresses;
  }

  /**
   * Parses an address and converts an internationalized domain name to ASCII.
   * @param string address string
   * @return address
   * @throws AddressException address exception
   */
  private static InternetAddress address(final String string) throws AddressException {
    final InternetAddress address = new InternetAddress(string.trim(), true);
    final String addr = address.getAddress();
    final int at = addr.lastIndexOf('@');
    if(at != -1) {
      try {
        address.setAddress(addr.substring(0, at + 1) + IDN.toASCII(addr.substring(at + 1)));
      } catch(final IllegalArgumentException ex) {
        throw new AddressException(ex.getMessage(), addr);
      }
    }
    return address;
  }

  /**
   * Returns a query exception for a mail exception.
   * @param ex exception
   * @return query exception
   */
  private static QueryException error(final Exception ex) {
    final String code = ex instanceof AddressException ? "address" :
      ex instanceof AuthenticationFailedException ? "auth" :
      ex instanceof MailConnectException ? "connect" : "error";
    final Throwable root = Util.rootException(ex);
    final String message = Util.message(ex);
    return error(code, root == ex ? message : message + "; " + Util.message(root));
  }

  /**
   * Returns a query exception with an error code in the module namespace.
   * @param code error code
   * @param message error message
   * @return query exception
   */
  private static QueryException error(final String code, final String message) {
    return new QueryException(null, new QNm(PREFIX, code, URI), "Mail: %", message);
  }
}
