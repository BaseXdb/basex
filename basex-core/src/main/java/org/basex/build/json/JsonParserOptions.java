package org.basex.build.json;

import java.util.*;

import org.basex.util.options.*;

/**
 * Options for parsing JSON documents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class JsonParserOptions extends JsonOptions {
  /** Option: encoding. */
  public static final StringOption ENCODING = new StringOption("encoding");
  /** Option: unescape special characters. */
  public static final BooleanOption UNESCAPE = new BooleanOption("unescape", true);
  /** Option: liberal parsing. */
  public static final BooleanOption LIBERAL = new BooleanOption("liberal", false);
  /** Option: handle duplicates. */
  public static final EnumOption<JsonDuplicates> DUPLICATES =
      new EnumOption<>("duplicates", JsonDuplicates.USE_LAST);

  /** Duplicate handling. */
  public enum JsonDuplicates {
    /** Reject.    */ REJECT,
    /** Use first. */ USE_FIRST,
    /** Use last.  */ USE_LAST;

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }
  }

  /**
   * Default constructor.
   */
  public JsonParserOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public JsonParserOptions(final JsonParserOptions opts) {
    super(opts);
  }
}
