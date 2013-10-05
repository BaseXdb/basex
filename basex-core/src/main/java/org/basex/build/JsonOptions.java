package org.basex.build;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This class contains JSON options.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JsonOptions extends Options {
  /** Option: encoding. */
  public static final Option ENCODING = new Option("encoding", Token.UTF8);
  /** Option: parser specification. */
  public static final Option SPEC = new Option("spec", JsonSpec.RFC4627.desc);
  /** Option: unescape special characters. */
  public static final Option UNESCAPE = new Option("unescape", true);
  /** Option: JSON format (default, jsonml, plain, map). */
  public static final Option FORMAT = new Option("format", "default");
  /** Option: lax conversion of names to QNames. */
  public static final Option LAX = new Option("lax", false);

  /** JSON specs. */
  public static enum JsonSpec {
    /** Parse the input according to RFC 4627.           */ RFC4627("RFC4627"),
    /** Parse the input according to ECMA-262.           */ ECMA_262("ECMA-262"),
    /** Parse the input being as compatible as possible. */ LIBERAL("liberal");

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
    /** Default.       */ DEFAULT,
    /** JsonML.        */ JSONML,
    /** Plain (jpcs).  */ PLAIN,
    /** Map (non-XML). */ MAP;

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ENGLISH);
    }
  }

  /**
   * Default constructor.
   */
  public JsonOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws IOException I/O exception
   */
  public JsonOptions(final String opts) throws IOException {
    parse(opts, true);
  }

  /**
   * Returns the specification.
   * @return spec
   * @throws QueryException query exception
   */
  public JsonSpec spec() throws QueryException {
    final String spec = get(SPEC);
    for(final JsonSpec s : JsonSpec.values()) if(s.desc.equals(spec)) return s;
    throw BXJS_CONFIG.thrw(null, "Unknown spec '" + spec + "'");
  }

  /**
   * Returns the specification.
   * @return spec
   * @throws SerializerException serializer exception
   */
  public JsonFormat format() throws SerializerException {
    final String form = get(FORMAT);
    for(final JsonFormat f : JsonFormat.values()) if(f.toString().equals(form)) return f;
    throw BXJS_CONFIG.thrwSerial("Unknown format '" + form + "'");
  }
}
