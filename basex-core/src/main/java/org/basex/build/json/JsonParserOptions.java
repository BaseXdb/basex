package org.basex.build.json;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import org.basex.core.*;
import org.basex.query.*;
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
      new EnumOption<>("number-format", JsonNumberFormat.class);
  /** Option: handle duplicates (parse-json, json-to-xml). */
  public static final EnumOption<JsonDuplicates> DUPLICATES =
      new EnumOption<>("duplicates", JsonDuplicates.class);
  /** Option: null item (parse-json). */
  public static final ValueOption NULL = new ValueOption("null", ITEM_ZO);
  /** Option: validation (json-to-xml). */
  public static final BooleanOption VALIDATE = new BooleanOption("validate");
  /** Option: JSON Lines (custom). */
  public static final BooleanOption JSON_LINES = new BooleanOption("json-lines", false);
  /** Option: encoding (custom). */
  public static final StringOption ENCODING = new StringOption(CommonOptions.ENCODING);

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

  /**
   * Checks if the options are compatible with the conversion format.
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public void check(final InputInfo info) throws QueryException {
    final JsonFormat format = get(FORMAT);
    final boolean w3 = format == JsonFormat.W3, maps = w3 || format == JsonFormat.W3_MAPPING;
    if(get(VALIDATE) != null && format != JsonFormat.W3_XML) throw unknown(VALIDATE, info);
    final JsonNumberFormat nf = get(NUMBER_FORMAT);
    if(nf != null && nf != JsonNumberFormat.DOUBLE && !maps) throw unknown(NUMBER_FORMAT, info);
    if(!get(NULL).isEmpty() && !w3) throw unknown(NULL, info);
    final Option<?> option = elementsOption();
    if(option != null) throw unknown(option, info);
    // maps cannot retain duplicates, XML formats cannot pick the last one
    final JsonDuplicates dupl = get(DUPLICATES);
    if(dupl == (maps ? JsonDuplicates.RETAIN : JsonDuplicates.USE_LAST)) {
      throw OPTION_JSON_X.get(info, Util.info("'%':'%' is not supported by the target format.",
          DUPLICATES.name(), dupl));
    }
    if(!get(FALLBACK).isEmpty() && get(ESCAPE)) {
      throw OPTION_JSON_X.get(info, "Escape cannot be combined with fallback function.");
    }
  }

  /**
   * Returns an error for an option that is unknown to the conversion format.
   * @param option option
   * @param info input info (can be {@code null})
   * @return error
   */
  private static QueryException unknown(final Option<?> option, final InputInfo info) {
    return INVALIDOPTION_X.get(info, Options.unknown(option));
  }
}
