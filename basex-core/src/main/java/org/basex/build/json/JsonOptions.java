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
   * Checks if a mapping is assigned that is not supported by the conversion format.
   * @return result of check
   */
  public final boolean unsupportedMapping() {
    return get(FORMAT) != JsonFormat.W3_MAPPING && !get(MAPPING).isEmpty();
  }

  /**
   * Checks if type information is merged (only supported by formats with type attributes).
   * @return result of check
   */
  public final boolean merge() {
    final JsonFormat format = get(FORMAT);
    return get(MERGE) && (format == JsonFormat.DIRECT || format == JsonFormat.ATTRIBUTES);
  }
}
