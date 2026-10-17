package org.basex.modules;

import org.basex.util.options.*;

/**
 * E-Mail options.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class MailOptions extends Options {
  /** Option. */
  public static final StringOption SUBJECT = new StringOption("subject");
  /** Option. */
  public static final StringsOption BCC = new StringsOption("bcc");
  /** Option. */
  public static final StringsOption CC = new StringsOption("cc");
  /** Option. */
  public static final StringOption FROM = new StringOption("from");
  /** Option. */
  public static final StringsOption TO = new StringsOption("to");
  /** Option. */
  public static final StringsOption REPLY_TO = new StringsOption("reply-to");
  /** Option. */
  public static final StringOption HOST = new StringOption("host");
  /** Option. */
  public static final StringOption MESSAGE = new StringOption("message");
  /** Option. */
  public static final StringOption USER = new StringOption("user");
  /** Option. */
  public static final StringOption PASSWORD = new StringOption("password");
  /** Option. */
  public static final NumberOption PORT = new NumberOption("port");
  /** Option. */
  public static final BooleanOption SSL = new BooleanOption("ssl");
  /** Option. */
  public static final BooleanOption TLS = new BooleanOption("tls");
  /** Option. */
  public static final StringsOption ATTACHMENT = new StringsOption("attachment");
}
