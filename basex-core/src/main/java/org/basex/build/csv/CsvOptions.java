package org.basex.build.csv;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class CsvOptions extends Options {
  /** Option: separator. Ignored if {@code FIELD_DELIMITER} is assigned. */
  public static final StringOption SEPARATOR = new StringOption("separator", ",");
  /** Option: field delimiter. */
  public static final StringOption FIELD_DELIMITER = new StringOption("field-delimiter");
  /** Option: format. */
  public static final EnumOption<CsvFormat> FORMAT = new EnumOption<>("format", CsvFormat.DIRECT);
  /** Option: header. */
  public static final ValueOption HEADER = new ValueOption("header", ITEM_ZM, Bln.FALSE);
  /** Option: backslash flag . */
  public static final BooleanOption BACKSLASHES = new BooleanOption("backslashes", false);
  /** Option: lax conversion of strings to QNames. */
  public static final BooleanOption LAX = new BooleanOption("lax", true);
  /** Option: parse quotes. */
  public static final BooleanOption QUOTES = new BooleanOption("quotes", true);
  /** Option: row delimiter. */
  public static final StringOption ROW_DELIMITER = new StringOption("row-delimiter", "\n");
  /** Option: quote character. */
  public static final StringOption QUOTE_CHARACTER = new StringOption("quote-character", "\"");
  /** Option: trim whitespace. */
  public static final BooleanOption TRIM_WHITESPACE = new BooleanOption("trim-whitespace", false);
  /** Option: strict quoting (implies QUOTES). */
  public static final BooleanOption STRICT_QUOTING = new BooleanOption("strict-quoting", false);
  /** Option: trim-rows. */
  public static final BooleanOption TRIM_ROWS = new BooleanOption("trim-rows", false);
  /** Option: select-columns. */
  public static final NumbersOption SELECT_COLUMNS = new NumbersOption("select-columns");

  /** CSV formats. */
  public enum CsvFormat {
    /** Default. */ DIRECT,
    /** Attributes. */ ATTRIBUTES,
    /** fn:parse-csv. */ W3,
    /** fn:csv-to-arrays. */ W3_ARRAYS,
    /** fn:csv-to-xml. */ W3_XML,
    /** XQuery (deprecated; use {@link #W3}). */ XQUERY;

    @Override
    public String toString() {
      return Enums.string(this);
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
      return Enums.string(this);
    }
  }

  @Override
  public synchronized void assign(final String name, final String value) throws BaseXException {
    super.assign(name, value);
    try {
      validate(null);
    } catch(final QueryException ex) {
      throw new BaseXException(ex.getLocalizedMessage());
    }
  }

  @Override
  public synchronized void assign(final Item name, final Value value, final InputInfo info)
      throws QueryException {
    super.assign(name, value, info);
    validate(info);
  }

  /**
   * Validates options.
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private void validate(final InputInfo info) throws QueryException {
    final StringOption separator = get(FIELD_DELIMITER) != null ? FIELD_DELIMITER : SEPARATOR;
    final int f = fieldDelimiter(), r = rowDelimiter(), q = quoteCharacter();
    final StringOption option = f == -1 ? separator : r == -1 ? ROW_DELIMITER : q == -1 ?
      QUOTE_CHARACTER : null;
    if(option != null) throw CSV_SINGLECHAR_X_X.get(info, option.name(), get(option));
    if(f == q || r == f || q == r) throw CSV_DELIMITER_X.get(info,
        get(f == q || r == f ? separator : QUOTE_CHARACTER));
  }

  /**
   * Returns the separator character or {@code -1} if character is invalid.
   * @return separator
   */
  public int fieldDelimiter() {
    String sep = get(FIELD_DELIMITER);
    if(sep == null) {
      sep = get(SEPARATOR);
      final CsvSep s = Enums.get(CsvSep.class, sep);
      if(s != null) return s.sep;
    }
    return checkCodepoint(sep);
  }

  /**
   * Returns the row delimiter character or {@code -1} if character is invalid.
   * @return separator
   */
  public int rowDelimiter() {
    return checkCodepoint(get(ROW_DELIMITER));
  }

  /**
   * Returns the quote character or {@code -1} if character is invalid.
   * @return separator
   */
  public int quoteCharacter() {
    return checkCodepoint(get(QUOTE_CHARACTER));
  }

  /**
   * Validates a single code point passed as a string.
   * @param single single character string
   * @return code point or {@code -1}
   */
  private static int checkCodepoint(final String single) {
    if(single.codePointCount(0, single.length()) == 1) {
      final int cp = single.codePointAt(0);
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
  public CsvOptions(final CsvOptions opts) {
    super(opts);
  }
}
