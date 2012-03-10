package org.basex.build.file;

import java.io.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * This class contains parser properties.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ParserProp extends AProp {
  /** Parser option: column separator (0 = comma, 1 = semicolon, 2 = tab). */
  public static final Object[] SEPARATOR = { "separator", "comma" };
  /** Parser option: header line. */
  public static final Object[] HEADER = { "header", false };
  /** Parser option: XML format. */
  public static final Object[] FORMAT = { "format", "verbose" };
  /** Parser option: line. */
  public static final Object[] LINES = { "lines", true };
  /** Parser option: flat. */
  public static final Object[] FLAT = { "flat", false };
  /** Parser option: encoding. */
  public static final Object[] ENCODING = { "encoding", Token.UTF8 };
  /** Parser option: jsonml format. */
  public static final Object[] JSONML = { "jsonml", false };

  /**
   * Constructor.
   */
  public ParserProp() {
    super(null);
  }

  /**
   * Constructor, specifying initial properties.
   * @param s property string. Properties are separated with commas ({@code ,}),
   * key/values with the equality character ({@code =}).
   * @throws IOException I/O exception
   */
  public ParserProp(final String s) throws IOException {
    this();
    super.properties(s);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final String str : props.keySet()) {
      if(sb.length() != 0) sb.append(',');
      sb.append(str).append('=').append(props.get(str));
    }
    return sb.toString();
  }
}
