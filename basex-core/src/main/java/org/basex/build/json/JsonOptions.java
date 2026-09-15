package org.basex.build.json;

import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for processing JSON documents.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class JsonOptions extends Options {
  /** Option: merge type information (custom). */
  public static final BooleanOption MERGE = new BooleanOption("merge", false);
  /** Option: include string types (custom). */
  public static final BooleanOption STRINGS = new BooleanOption("strings", false);
  /** Option: lax conversion of names to QNames (custom). */
  public static final BooleanOption LAX = new BooleanOption("lax", false);
  /** Option: format (custom). */
  public static final EnumOption<JsonFormat> FORMAT = new EnumOption<>("format", JsonFormat.DIRECT);
  /** Option: options for converting between JSON and XML (w3-mapping). */
  public static final ValueOption MAPPING = new ValueOption("mapping", Types.MAP_ZO);

  /** JSON formats. */
  public enum JsonFormat {
    /** Direct. */ DIRECT,
    /** Attributes. */ ATTRIBUTES,
    /** JsonML. */ JSONML,
    /** fn:parse-json. */ W3,
    /** fn:json-to-xml. */ W3_XML,
    /** fn:map-to-element, fn:element-to-map. */ W3_MAPPING;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /**
   * Default constructor.
   */
  public JsonOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public JsonOptions(final JsonOptions opts) {
    super(opts);
  }

  /**
   * Returns an assigned option that is only supported by the w3-mapping format.
   * @return option or {@code null}
   */
  public final Option<?> elementsOption() {
    return get(FORMAT) != JsonFormat.W3_MAPPING && !get(MAPPING).isEmpty() ? MAPPING : null;
  }
}
