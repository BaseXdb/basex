package org.basex.build.json;

import java.util.*;

import org.basex.util.options.*;

/**
 * Options for processing JSON documents.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class JsonOptions extends Options {
  /** Option: merge type information. */
  public static final BooleanOption MERGE = new BooleanOption("merge", false);
  /** Option: include string types. */
  public static final BooleanOption STRINGS = new BooleanOption("strings", false);
  /** Option: lax conversion of names to QNames. */
  public static final BooleanOption LAX = new BooleanOption("lax", false);
  /** Option: format. */
  public static final EnumOption<JsonFormat> FORMAT = new EnumOption<>("format", JsonFormat.DIRECT);

  /** JSON formats. */
  public enum JsonFormat {
    /** Direct.        */ DIRECT,
    /** Attributes.    */ ATTRIBUTES,
    /** JsonML.        */ JSONML,
    /** Map (non-XML). */ MAP;

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
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
}
