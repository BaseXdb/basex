package org.basex.modules;

import static org.basex.modules.MailOptions.*;

import org.apache.commons.mail.*;
import org.basex.query.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Mail package.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Mail extends QueryModule {
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
   *   'host': 'smtp.gmail.com',
   *   'port': '587',
   *   'ssl': 'true',
   *   'tls': 'true',
   *   'reply-to': '...',
   *   'attachment': '/path/to/file.jpg'
   * }
   * </pre>
   * @param map map
   * @throws QueryException query exception
   */
  public void send(final XQMap map) throws QueryException {
    final MailOptions mail = new MailOptions();
    mail.assign(map, queryContext, null);

    final Thread ct = Thread.currentThread();
    final ClassLoader cl = ct.getContextClassLoader();
    try {
      final MultiPartEmail mpe = new MultiPartEmail();
      mpe.setCharset(Strings.UTF8);

      if(mail.contains(SUBJECT)) mpe.setSubject(mail.get(SUBJECT));
      if(mail.contains(MESSAGE)) mpe.setMsg(mail.get(MESSAGE));
      if(mail.contains(HOST)) mpe.setHostName(mail.get(HOST));
      if(mail.contains(PORT)) mpe.setSmtpPort(mail.get(PORT));
      if(mail.contains(FROM)) mpe.setFrom(mail.get(FROM));
      if(mail.contains(SSL)) mpe.setSSLOnConnect(mail.get(SSL));
      if(mail.contains(TLS)) mpe.setStartTLSEnabled(mail.get(TLS));
      for(final String s : mail.get(BCC)) mpe.addBcc(s.trim());
      for(final String s : mail.get(CC)) mpe.addCc(s.trim());
      for(final String s : mail.get(TO)) mpe.addTo(s.trim());
      for(final String s : mail.get(REPLY_TO)) mpe.addReplyTo(s.trim());
      for(final String s : mail.get(ATTACHMENT)) {
        final EmailAttachment ea = new EmailAttachment();
        ea.setPath(s.trim());
        mpe.attach(ea);
      }
      if(mail.contains(USER) && mail.contains(PASSWORD)) {
        mpe.setAuthentication(mail.get(USER), mail.get(PASSWORD));
      }

      ct.setContextClassLoader(getClass().getClassLoader());
      mpe.send();
    } catch(final EmailException ex) {
      throw new QueryException("Mail: " + Util.message(ex));
    } finally {
      ct.setContextClassLoader(cl);
    }
  }
}
