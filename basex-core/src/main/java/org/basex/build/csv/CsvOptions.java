package org.basex.build.csv;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class CsvOptions extends Options {
  /** Option: column separator. */
  public static final StringOption SEPARATOR =
      new StringOption("separator", CsvSep.COMMA.toString());
  /** Option: format. */
  public static final EnumOption<CsvFormat> FORMAT = new EnumOption<>("format", CsvFormat.DIRECT);
  /** Option: header line. */
  public static final BooleanOption HEADER = new BooleanOption("header", false);
  /** Option: backslash flag . */
  public static final BooleanOption BACKSLASHES = new BooleanOption("backslashes", false);
  /** Option: lax conversion of strings to QNames. */
  public static final BooleanOption LAX = new BooleanOption("lax", true);
  /** Option: parse quotes. */
  public static final BooleanOption QUOTES = new BooleanOption("quotes", true);

  /** CSV formats. */
  public enum CsvFormat {
    /** Default.    */ DIRECT,
    /** Attributes. */ ATTRIBUTES,
    /** XQuery.     */ XQUERY;

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
    }
  }

  /** CSV separators. */
  public enum CsvSep {
    /** Comma.     */ COMMA(','),
    /** Semicolon. */ SEMICOLON(';'),
    /** Colon.     */ COLON(':'),
    /** Tab.       */ TAB('\t'),
    /** Space.     */ SPACE(' ');

    /** Character. */
    public final char sep;

    /**
     * Constructor.
     * @param sep separator character
     */
    CsvSep(final char sep) {
      this.sep = sep;
    }

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
    }
  }

  @Override
  public synchronized void assign(final String name, final String value) throws BaseXException {
    super.assign(name, value);
    check();
  }

  @Override
  public synchronized void assign(final Item name, final Value value, final boolean error,
      final InputInfo ii) throws BaseXException, QueryException {
    super.assign(name, value, error, ii);
    check();
  }

  /**
   * Checks the separator character.
   * @throws BaseXException database exception
   */
  private void check() throws BaseXException {
    if(separator() == -1) throw new BaseXException("Invalid separator: '%'", get(SEPARATOR));
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

  /**
   * Default constructor.
   */
  public CsvOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  CsvOptions(final CsvOptions opts) {
    super(opts);
  }
}
