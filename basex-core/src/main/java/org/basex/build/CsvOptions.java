package org.basex.build;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CsvOptions extends Options {
  /** Option: encoding. */
  public static final StringOption ENCODING = new StringOption("encoding", Token.UTF8);
  /** Option: column separator. */
  public static final StringOption SEPARATOR = new StringOption("separator", "comma");
  /** Option: header line. */
  public static final BooleanOption HEADER = new BooleanOption("header", false);
  /** Option: lax conversion of strings to QNames. */
  public static final BooleanOption LAX = new BooleanOption("lax", true);

  /** CSV separators. */
  public static enum CsvSep {
    /** Comma.     */ COMMA(','),
    /** Semicolon. */ SEMICOLON(';'),
    /** Colon.     */ COLON(':'),
    /** Tab.       */ TAB('\t'),
    /** Space.     */ SPACE(' ');

    /** Character. */
    private final int ch;

    /**
     * Constructor.
     * @param c mapped character
     */
    private CsvSep(final int c) {
      ch = c;
    }

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ENGLISH);
    }
  }

  /**
   * Constructor.
   */
  public CsvOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws BaseXException database exception
   */
  public CsvOptions(final String opts) throws BaseXException {
    super(opts);
  }

  /**
   * Returns the separator character.
   * @return separator
   * @throws QueryIOException query I/O exception
   */
  public int separator() throws QueryIOException {
    // set separator
    final String sep = get(SEPARATOR);
    final String val = sep.toLowerCase(Locale.ENGLISH);
    for(final CsvSep s : CsvSep.values()) if(val.equals(s.toString())) return s.ch;
    if(sep.length() != 1) BXCS_CONFSEP.thrwIO(sep);
    return sep.charAt(0);
  }
}
