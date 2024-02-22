package org.basex.build.json;

import org.basex.query.value.type.*;
import org.basex.util.options.*;

/**
 * Options for parsing JSON documents.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class JsonParserOptions extends JsonOptions {
  /** Option: unescape special characters (parse-json, json-to-xml). */
  public static final BooleanOption ESCAPE = new BooleanOption("escape", false);
  /** Option: liberal parsing (parse-json, json-to-xml). */
  public static final BooleanOption LIBERAL = new BooleanOption("liberal", false);
  /** Option: fallback function (parse-json, json-to-xml). */
  public static final ValueOption FALLBACK = new ValueOption("fallback", SeqType.FUNCTION_ZO);
  /** Option: number-parser function (parse-json, json-to-xml). */
  public static final ValueOption NUMBER_PARSER =
      new ValueOption("number-parser", SeqType.FUNCTION_ZO);
  /** Option: handle duplicates (parse-json, json-to-xml). */
  public static final EnumOption<JsonDuplicates> DUPLICATES =
      new EnumOption<>("duplicates", JsonDuplicates.class);
  /** Option: null item (parse-json). */
  public static final ValueOption NULL = new ValueOption("null", SeqType.ITEM_ZM);
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
      return EnumOption.string(name());
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
