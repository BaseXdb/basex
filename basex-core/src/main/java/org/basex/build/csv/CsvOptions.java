package org.basex.build.csv;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team 2005-24, BSD License
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
  /** Option: row delimiter. */
  public static final StringOption ROW_DELIMITER = new StringOption("row-delimiter", "\n");
  /** Option: quote character. */
  public static final StringOption QUOTE_CHARACTER = new StringOption("quote", "\"");
  /** Option: trim whitespace. */
  public static final BooleanOption TRIM_WHITSPACE = new BooleanOption("trim-whitespace", false);
  /** Option: strict quoting. */
  public static final BooleanOption STRICT_QUOTING = new BooleanOption("strict-quoting", false);
  /** Option: trim-rows. */
  public static final BooleanOption TRIM_ROWS = new BooleanOption("trim-rows", false);
  /** Option: select-columns. */
  public static final NumbersOption SELECT_COLUMNS = new NumbersOption("select-columns");

  /** CSV formats. */
  public enum CsvFormat {
    /** Default.    */ DIRECT,
    /** Attributes. */ ATTRIBUTES,
    /** XQuery.     */ XQUERY;

    @Override
    public String toString() {
      return EnumOption.string(this);
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
      return EnumOption.string(this);
    }
  }

  @Override
  public synchronized void assign(final String name, final String value) throws BaseXException {
    super.assign(name, value);
    final int s = separator(), r = rowDelimiter(), q = quoteCharacter();
    if(s == -1) throw new BaseXException("Invalid separator: '%'", get(SEPARATOR));
    if(r == -1) throw new BaseXException("Invalid row delimiter: '%'", get(ROW_DELIMITER));
    if(q == -1) throw new BaseXException("Invalid quote character: '%'", get(QUOTE_CHARACTER));
    if(s == q || r == s || q == r) throw new BaseXException("Duplicate CSV delimiter error: '%'",
        get(s == q || r == s ? SEPARATOR : QUOTE_CHARACTER));
  }

  @Override
  public synchronized void assign(final Item name, final Value value, final InputInfo info)
      throws QueryException {
    super.assign(name, value, info);
    final int s = separator(), r = rowDelimiter(), q = quoteCharacter();
    if(s == -1) throw OPTION_X.get(info, "Invalid separator: '%'", get(SEPARATOR));
    if(r == -1) throw CSV_SINGLECHAR_X_X.get(info, ROW_DELIMITER.name(), get(ROW_DELIMITER));
    if(q == -1) throw CSV_SINGLECHAR_X_X.get(info, QUOTE_CHARACTER.name(), get(QUOTE_CHARACTER));
    if(s == q || r == s || q == r) throw CSV_DELIMITER_X.get(info,
        get(s == q || r == s ? SEPARATOR : QUOTE_CHARACTER));
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
    if(sep.codePointCount(0, sep.length()) == 1) {
      final int cp = sep.codePointAt(0);
      if(XMLToken.valid(cp)) return cp;
    }
    return -1;
  }

  /**
   * Returns the row delimiter character or {@code -1} if character is invalid.
   * @return separator
   */
  public int rowDelimiter() {
    final String rd = get(ROW_DELIMITER);
    if(rd.codePointCount(0, rd.length()) == 1) {
      final int cp = rd.codePointAt(0);
      if(XMLToken.valid(cp)) return cp;
    }
    return -1;
  }

  /**
   * Returns the quote character or {@code -1} if character is invalid.
   * @return separator
   */
  public int quoteCharacter() {
    final String q = get(QUOTE_CHARACTER);
    if(q.codePointCount(0, q.length()) == 1) {
      final int cp = q.codePointAt(0);
      if(XMLToken.valid(cp)) return cp;
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
