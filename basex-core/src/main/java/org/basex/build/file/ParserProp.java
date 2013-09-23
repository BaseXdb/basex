package org.basex.build.file;

import java.io.*;

import org.basex.core.*;
import org.basex.query.util.json.JsonParser.Spec;
import org.basex.util.*;

/**
 * This class contains parser properties.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ParserProp extends AProp {
  /** Parser option: column separator. */
  public static final Object[] SEPARATOR = { "separator", "comma" };
  /** Parser option: header line. */
  public static final Object[] HEADER = { "header", false };
  /** Parser option: line. */
  public static final Object[] LINES = { "lines", true };
  /** Parser option: flat. */
  public static final Object[] FLAT = { "flat", false };
  /** Parser option: encoding. */
  public static final Object[] ENCODING = { "encoding", Token.UTF8 };
  /** Parser option: jsonml format. */
  public static final Object[] JSONML = { "jsonml", false };
  /** Parser option: JSON spec. */
  public static final Object[] SPEC = { "spec", Spec.RFC4627.name() };
  /** Parser option: JSON {@code unescape}. */
  public static final Object[] UNESCAPE = { "unescape", true };
  /** Parser option: XML format (obsolete). */
  public static final Object[] FORMAT = { "format", "verbose" };

  /**
   * Constructor.
   */
  public ParserProp() {
    super();
  }

  /**
   * Constructor, specifying initial properties.
   * @param s property string. Properties are separated with commas ({@code ,}),
   * key/values with the equality character ({@code =}).
   * @throws IOException I/O exception
   */
  public ParserProp(final String s) throws IOException {
    parse(s);
  }
}
