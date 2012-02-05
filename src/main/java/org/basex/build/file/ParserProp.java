package org.basex.build.file;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.Locale;

import org.basex.core.AProp;
import org.basex.core.BaseXException;
import org.basex.util.Token;
import org.basex.util.Util;

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

    for(final String ser : s.trim().split(",")) {
      if(ser.isEmpty()) continue;
      final String[] sprop = ser.split("=", 2);
      final String key = sprop[0].trim().toLowerCase(Locale.ENGLISH);
      final Object obj = get(key);
      if(obj == null) {
        final String in = key.toUpperCase(Locale.ENGLISH);
        final String sim = similar(in);
        throw new BaseXException(
            sim != null ? UNKNOWN_OPT_SIMILAR_X : UNKNOWN_OPTION_X, in, sim);
      }
      if(obj instanceof Integer) {
        final int i = sprop.length < 2 ? 0 : Token.toInt(sprop[1]);
        if(i == Integer.MIN_VALUE)
          throw new BaseXException(INVALID_VALUE_X_X, key, sprop[1]);
        set(key, i);
      } else if(obj instanceof Boolean) {
        final String val = sprop.length < 2 ? TRUE : sprop[1];
        set(key, Util.yes(val));
      } else {
        set(key, sprop.length < 2 ? "" : sprop[1]);
      }
    }
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
