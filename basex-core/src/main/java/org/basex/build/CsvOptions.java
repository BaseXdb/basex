package org.basex.build;

import java.util.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class CsvOptions extends Options {
  /** Option: column separator. */
  public static final StringOption SEPARATOR =
      new StringOption("separator", CsvSep.COMMA.toString());
  /** Option: format. */
  public static final EnumOption<CsvFormat> FORMAT =
      new EnumOption<CsvFormat>("format", CsvFormat.DIRECT);
  /** Option: header line. */
  public static final BooleanOption HEADER =
      new BooleanOption("header", false);
  /** Option: lax conversion of strings to QNames. */
  public static final BooleanOption LAX =
      new BooleanOption("lax", true);

  /** CSV formats. */
  public static enum CsvFormat {
    /** Default.    */ DIRECT,
    /** Attributes. */ ATTRIBUTES,
    /** Map.        */ MAP;

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ENGLISH);
    }
  }

  /** CSV separators. */
  public static enum CsvSep {
    /** Comma.     */ COMMA(','),
    /** Semicolon. */ SEMICOLON(';'),
    /** Colon.     */ COLON(':'),
    /** Tab.       */ TAB('\t'),
    /** Space.     */ SPACE(' ');

    /** Character. */
    public final char sep;

    /**
     * Constructor.
     * @param sp separator character
     */
    private CsvSep(final char sp) {
      sep = sp;
    }

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ENGLISH);
    }
  }

  @Override
  public synchronized void assign(final String name, final String value) throws BaseXException {
    super.assign(name, value);
    // check if separator contains only one character
    if(options.get(name) == CsvOptions.SEPARATOR && separator() == -1)
      throw new BaseXException("Separator must be single character; '%' found", value);
  }

  /**
   * Returns the separator character or {@code -1} if character is invalid.
   * @return separator
   */
  public int separator() {
    final String sep = get(SEPARATOR);
    for(final CsvSep s : CsvSep.values()) {
      if(sep.equals(s.toString())) return s.sep;
    }
    if(sep.length() == 1) {
      final char ch = sep.charAt(0);
      if(XMLToken.valid(ch)) return ch;
    }
    return -1;
  }
}
