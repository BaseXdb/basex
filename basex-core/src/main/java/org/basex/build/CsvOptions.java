package org.basex.build;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class CsvOptions extends Options {
  /** Option: column separator. */
  public static final StringOption SEPARATOR = new StringOption("separator", "comma");
  /** Option: header line. */
  public static final BooleanOption HEADER = new BooleanOption("header", false);
  /** Option: lax conversion of strings to QNames. */
  public static final BooleanOption LAX = new BooleanOption("lax", true);
  /** Option: format. */
  public static final StringOption FORMAT = new StringOption("format", CsvFormat.DIRECT.toString());

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

  /** CSV formats. */
  public static enum CsvFormat {
    /** Default.    */ DIRECT,
    /** Attributes. */ ATTRIBUTES;

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
    final String sep = get(SEPARATOR);
    final String val = sep.toLowerCase(Locale.ENGLISH);
    for(final CsvSep s : CsvSep.values()) if(val.equals(s.toString())) return s.ch;
    if(sep.length() != 1) BXCS_CONFIG.thrwIO(
        "Separator must be single character; '" + sep + "' found");
    return sep.charAt(0);
  }

  /**
   * Returns the specification.
   * @return spec
   * @throws QueryIOException query I/O exception
   */
  public CsvFormat format() throws QueryIOException {
    final String form = get(FORMAT);
    for(final CsvFormat f : CsvFormat.values()) if(f.toString().equals(form)) return f;
    throw BXCS_CONFIG.thrwIO("Format '" + form + "' is not supported");
  }
}
