package org.basex.build;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.options.*;

/**
 * Options for processing JSON documents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonOptions extends Options {
  /** Option: parser specification. */
  public static final StringOption SPEC = new StringOption("spec", JsonSpec.RFC4627.toString());
  /** Option: lax conversion of names to QNames. */
  public static final BooleanOption LAX = new BooleanOption("lax", false);
  /** Option: format. */
  public static final StringOption FORMAT = new StringOption("format",
      JsonFormat.DIRECT.toString());

  /** JSON specs. */
  public static enum JsonSpec {
    /** Parse the input according to RFC 4627.           */ RFC4627("RFC4627"),
    /** Parse the input being as compatible as possible. */ LIBERAL("liberal"),
    /** Parse the input according to ECMA-262.           */ ECMA_262("ECMA-262");

    /** Description. */
    private final String desc;

    /**
     * Constructor.
     * @param dsc description
     */
    private JsonSpec(final String dsc) {
      desc = dsc;
    }

    @Override
    public String toString() {
      return desc;
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

  /**
   * Returns the specification.
   * @return spec
   * @throws QueryIOException query I/O exception
   */
  public final JsonSpec spec() throws QueryIOException {
    final String spec = get(SPEC);
    for(final JsonSpec s : JsonSpec.values()) if(s.desc.equals(spec)) return s;
    throw BXJS_CONFIG.thrwIO("Spec '" + spec + "' is not supported");
  }

  /**
   * Returns the specification.
   * @return spec
   * @throws QueryIOException query I/O exception
   */
  public final JsonFormat format() throws QueryIOException {
    final String form = get(FORMAT);
    for(final JsonFormat f : JsonFormat.values()) if(f.toString().equals(form)) return f;
    throw BXJS_CONFIG.thrwIO("Format '" + form + "' is not supported");
  }
}
