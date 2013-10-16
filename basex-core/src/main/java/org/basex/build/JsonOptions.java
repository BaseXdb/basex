package org.basex.build;

import java.util.*;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Options for processing JSON documents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonOptions extends Options {
  /** Option: parser specification. */
  public static final EnumOption<JsonSpec> SPEC =
      new EnumOption<JsonSpec>("spec", JsonSpec.RFC4627);
  /** Option: format. */
  public static final EnumOption<JsonFormat> FORMAT =
      new EnumOption<JsonFormat>("format", JsonFormat.DIRECT);
  /** Option: lax conversion of names to QNames. */
  public static final BooleanOption LAX =
      new BooleanOption("lax", false);

  /** JSON specs. */
  public static enum JsonSpec {
    /** Parse the input according to RFC 4627.           */ RFC4627("RFC4627"),
    /** Parse the input being as compatible as possible. */ LIBERAL("liberal"),
    /** Parse the input according to ECMA-262.           */ ECMA_262("ECMA-262");

    /** String. */
    private final String string;

    /**
     * Constructor.
     * @param str description
     */
    private JsonSpec(final String str) {
      string = str;
    }

    @Override
    public String toString() {
      return string;
    }
  }

  /** JSON formats. */
  public static enum JsonFormat {
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
  protected JsonOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws BaseXException database exception
   */
  protected JsonOptions(final String opts) throws BaseXException {
    super(opts);
  }
}
