package org.basex.build;

import java.util.*;

import org.basex.util.options.*;

/**
 * Options for processing JSON documents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class JsonOptions extends Options {
  /** Option: merge type information. */
  public static final BooleanOption MERGE = new BooleanOption("merge", false);
  /** Option: include string types. */
  public static final BooleanOption STRINGS = new BooleanOption("strings", false);
  /** Option: lax conversion of names to QNames. */
  public static final BooleanOption LAX = new BooleanOption("lax", false);
  /** Option: parser specification. */
  public static final EnumOption<JsonSpec> SPEC = new EnumOption<>("spec", JsonSpec.RFC4627);
  /** Option: format. */
  public static final EnumOption<JsonFormat> FORMAT = new EnumOption<>("format", JsonFormat.DIRECT);

  /** JSON specs. */
  public enum JsonSpec {
    /** Parse the input according to RFC 4627.           */ RFC4627("RFC4627"),
    /** Parse the input being as compatible as possible. */ LIBERAL("liberal"),
    /** Parse the input according to ECMA-262.           */ ECMA_262("ECMA-262");

    /** String. */
    private final String string;

    /**
     * Constructor.
     * @param str description
     */
    JsonSpec(final String str) {
      string = str;
    }

    @Override
    public String toString() {
      return string;
    }
  }

  /** JSON formats. */
  public enum JsonFormat {
    /** Direct.        */ DIRECT,
    /** Attributes.    */ ATTRIBUTES,
    /** JsonML.        */ JSONML,
    /** Map (non-XML). */ MAP;

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ENGLISH);
    }
  }

  /**
   * Default constructor.
   */
  JsonOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  JsonOptions(final JsonOptions opts) {
    super(opts);
  }
}
