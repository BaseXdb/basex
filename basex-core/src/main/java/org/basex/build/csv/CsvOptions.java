package org.basex.build.csv;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class CsvOptions extends Options {
  /** Option: separator. */
  public static final StringOption SEPARATOR = new StringOption("separator", ",");
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
  /** Option: quote character. */
  public static final StringOption QUOTE_CHARACTER = new StringOption("quote-character", "\"");
  /** Option: comment marker. */
  public static final StringOption COMMENT_MARKER = new StringOption("comment-marker", "");
  /** Option: trim whitespace. */
  public static final BooleanOption TRIM_WHITESPACE = new BooleanOption("trim-whitespace", false);
  /** Option: strict quoting (implies QUOTES). */
  public static final BooleanOption STRICT_QUOTING = new BooleanOption("strict-quoting", false);
  /** Option: trim-rows. */
  public static final BooleanOption TRIM_ROWS = new BooleanOption("trim-rows", false);
  /** Option: select-columns. */
  public static final NumbersOption SELECT_COLUMNS =
      new NumbersOption("select-columns", POSITIVE_INTEGER_ZM);

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
  public synchronized void assign(final Item name, final Value value, final QueryContext qc,
      final InputInfo info) throws QueryException {
    super.assign(name, value, qc, info);
    validate(info);
  }

  /**
   * Validates options.
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private void validate(final InputInfo info) throws QueryException {
    final IntSet chars = new IntSet();
    for(final StringOption option : Arrays.asList(SEPARATOR, QUOTE_CHARACTER, COMMENT_MARKER)) {
      final String value = get(option);
      // an empty comment marker indicates that comments are not recognized
      if(option == COMMENT_MARKER && value.isEmpty()) continue;
      final int cp = option == SEPARATOR ? separator() : checkCodepoint(value);
      if(cp == -1) throw CSV_SINGLECHAR_X_X.get(info, option.name(), value);
      // newlines are reserved for delimiting rows
      if(cp == '\n') throw CSV_NEWLINE_X.get(info, option.name());
      if(!chars.add(cp)) throw CSV_DELIMITER_X.get(info, value);
    }
    // required type is not enforced if the options are assigned as a string
    for(final int column : get(SELECT_COLUMNS)) {
      if(column < 1) throw typeError(Itr.get(column), BasicType.POSITIVE_INTEGER, info);
    }
  }

  /**
   * Returns the separator character or {@code -1} if the character is invalid.
   * @return separator
   */
  public int separator() {
    final String sep = get(SEPARATOR);
    final CsvSep s = Enums.get(CsvSep.class, sep);
    return s != null ? s.sep : checkCodepoint(sep);
  }

  /**
   * Returns the quote character or {@code -1} if character is invalid.
   * @return quote character
   */
  public int quoteCharacter() {
    return checkCodepoint(get(QUOTE_CHARACTER));
  }

  /**
   * Returns the comment marker or {@code -1} if comments are not recognized.
   * @return comment marker
   */
  public int commentMarker() {
    final String marker = get(COMMENT_MARKER);
    return marker.isEmpty() ? -1 : checkCodepoint(marker);
  }

  /**
   * Validates a single codepoint passed as a string.
   * @param string string to be checked
   * @return codepoint or {@code -1}
   */
  private static int checkCodepoint(final String string) {
    return switch(string) {
      case "\\b" -> '\b';
      case "\\f" -> '\f';
      case "\\n" -> '\n';
      case "\\r" -> '\r';
      case "\\t" -> '\t';
      default -> {
        if(string.codePointCount(0, string.length()) == 1) {
          final int cp = string.codePointAt(0);
          if(XMLToken.valid(cp)) yield cp;
        }
        yield -1;
      }
    };
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
