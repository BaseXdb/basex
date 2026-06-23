package org.basex.build.json;

import static org.basex.query.value.type.Types.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for parsing JSON documents.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonParserOptions extends JsonOptions {
  /** Option: unescape special characters (parse-json, json-to-xml). */
  public static final BooleanOption ESCAPE = new BooleanOption("escape", false);
  /** Option: liberal parsing (parse-json, json-to-xml). */
  public static final BooleanOption LIBERAL = new BooleanOption("liberal", false);
  /** Option: fallback function (parse-json, json-to-xml). */
  public static final ValueOption FALLBACK = new ValueOption("fallback", FUNCTION_ZO);
  /** Option: number format (parse-json). */
  public static final EnumOption<JsonNumberFormat> NUMBER_FORMAT =
      new EnumOption<>("number-format", JsonNumberFormat.DOUBLE);
  /** Option: handle duplicates (parse-json, json-to-xml). */
  public static final EnumOption<JsonDuplicates> DUPLICATES =
      new EnumOption<>("duplicates", JsonDuplicates.class);
  /** Option: null item (parse-json). */
  public static final ValueOption NULL = new ValueOption("null", ITEM_ZO);
  /** Option: validation (json-to-xml). */
  public static final BooleanOption VALIDATE = new BooleanOption("validate");
  /** Option: encoding (custom). */
  public static final StringOption ENCODING = CommonOptions.ENCODING;

  /** Number format. */
  public enum JsonNumberFormat {
    /** Double.   */ DOUBLE,
    /** Decimal.  */ DECIMAL,
    /** Adaptive. */ ADAPTIVE;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Duplicate handling. */
  public enum JsonDuplicates {
    /** Reject.    */ REJECT,
    /** Use first. */ USE_FIRST,
    /** Use last.  */ USE_LAST,
    /** Retain.    */ RETAIN;

    @Override
    public String toString() {
      return Enums.string(this);
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
