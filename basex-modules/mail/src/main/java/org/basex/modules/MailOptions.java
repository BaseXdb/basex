package org.basex.modules;

import java.math.*;

import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.options.*;

/**
 * E-Mail options.
 *
 * @author BaseX Team, BSD License
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
  public static final StringOption MESSAGE = new StringOption("message", "");
  /** Option. */
  public static final StringOption USER = new StringOption("user");
  /** Option. */
  public static final StringOption PASSWORD = new StringOption("password");
  /** Option. */
  public static final NumberOption PORT = new NumberOption("port");
  /** Option. */
  public static final BooleanOption SSL = new BooleanOption("ssl", false);
  /** Option. */
  public static final BooleanOption TLS = new BooleanOption("tls", false);
  /** Option: file paths and maps with attachment data. */
  public static final ValueOption ATTACHMENT = new ValueOption("attachment",
      ChoiceItemType.get(BasicType.STRING, Types.MAP).seqType(Occ.ZERO_OR_MORE));
  /** Option: HTML message. */
  public static final StringOption HTML = new StringOption("html");
  /** Option: additional headers. */
  public static final ValueOption HEADERS = new ValueOption("headers",
      Types.STRING_ZM.mapType(BasicType.STRING).seqType(Occ.ZERO_OR_ONE));
  /** Option: envelope sender address for bounces. */
  public static final StringOption BOUNCE = new StringOption("bounce");
  /** Option: timeout in seconds ({@code 0}: none). */
  public static final ValueOption TIMEOUT = new ValueOption("timeout",
      BasicType.DECIMAL.seqType(), Dec.get(BigDecimal.valueOf(60)));

  /**
   * Attachment options.
   */
  public static final class AttachmentOptions extends Options {
    /** Option: file name. */
    public static final StringOption NAME = new StringOption("name");
    /** Option: binary or textual content. */
    public static final ValueOption CONTENT = new ValueOption("content", ChoiceItemType.get(
        BasicType.BASE64_BINARY, BasicType.HEX_BINARY, BasicType.STRING).seqType());
    /** Option: content type. */
    public static final StringOption TYPE = new StringOption("type");
  }
}
