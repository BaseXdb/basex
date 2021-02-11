package org.basex.build.json;

import java.util.*;

import org.basex.util.options.*;

/**
 * Options for parsing JSON documents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JsonParserOptions extends JsonOptions {
  /** Option: unescape special characters (parse-json, json-to-xml). */
  public static final BooleanOption ESCAPE = new BooleanOption("escape", false);
  /** Option: liberal parsing (parse-json, json-to-xml). */
  public static final BooleanOption LIBERAL = new BooleanOption("liberal", false);
  /** Option: fallback function (parse-json, json-to-xml). */
  public static final FuncOption FALLBACK = new FuncOption("fallback");
  /** Option: handle duplicates (parse-json, json-to-xml). */
  public static final EnumOption<JsonDuplicates> DUPLICATES =
      new EnumOption<>("duplicates", JsonDuplicates.class);
  /** Option: validation (json-to-xml). */
  public static final BooleanOption VALIDATE = new BooleanOption("validate", false);
  /** Option: encoding (custom). */
  public static final StringOption ENCODING = new StringOption("encoding");

  /** Duplicate handling. */
  public enum JsonDuplicates {
    /** Reject.    */ REJECT,
    /** Use first. */ USE_FIRST,
    /** Use last.  */ USE_LAST,
    /** Retain.    */ RETAIN;

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
