package org.basex.io.parse.csv;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * A CSV parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvParser {
  /** Input stream. */
  private final TextInput input;
  /** Converter. */
  private final CsvConverter conv;
  /** Header flag. */
  private final boolean header;
  /** Backslash flag. */
  private final boolean backslashes;
  /** Field delimiter (see {@link CsvOptions#FIELD_DELIMITER}). */
  private final int fieldDelimiter;
  /** Row delimiter (see {@link CsvOptions#ROW_DELIMITER}). */
  private final int rowDelimiter;
  /** Quote character (see {@link CsvOptions#QUOTE_CHARACTER}). */
  private final int quoteCharacter;
  /** Parse quotes.  */
  private final boolean quotes;
  /** Trim whitespace (see {@link CsvOptions#TRIM_WHITESPACE}). */
  private final boolean trimWhitespace;
  /** Trim rows (see {@link CsvOptions#TRIM_ROWS}). */
  private final boolean trimRows;
  /** Disallow field content outside of quotes. */
  private final boolean strictQuoting;
  /** Select columns. */
  private final int[] selectColumns;

  /** First entry of a line. */
  private boolean first = true;
  /** Number of fields in first row. */
  private int rowSize = -1;
  /** Data mode. */
  private boolean data;
  /** Fields of the current row. */
  private final TokenList fields = new TokenList();

  /**
   * Constructor.
   * @param input input
   * @param opts options
   * @param conv converter
   * @throws QueryException query exception
   */
  public CsvParser(final TextInput input, final CsvParserOptions opts, final CsvConverter conv)
      throws QueryException {
    this.input = input;
    this.conv = conv;
    header = opts.get(CsvOptions.HEADER) == Bln.TRUE;
    fieldDelimiter = opts.fieldDelimiter();
    rowDelimiter = opts.rowDelimiter();
    quoteCharacter = opts.quoteCharacter();
    strictQuoting = opts.get(CsvOptions.STRICT_QUOTING);
    quotes = strictQuoting || opts.get(CsvOptions.QUOTES);
    backslashes = opts.get(CsvOptions.BACKSLASHES);
    trimWhitespace = opts.get(CsvOptions.TRIM_WHITESPACE);
    trimRows = opts.get(CsvOptions.TRIM_ROWS);
    selectColumns = opts.get(CsvOptions.SELECT_COLUMNS);
    for(final int sc : selectColumns) {
      if(sc < 1) throw QueryError.typeError(Int.get(sc), SeqType.POSITIVE_INTEGER_O, null);
    }
  }

  /**
   * Parses a CSV expression.
   * @param ii input info (can be @null)
   * @throws QueryException query exception
   * @throws IOException query I/O exception
   */
  public void parse(final InputInfo ii) throws QueryException, IOException {
    final TokenBuilder entry = new TokenBuilder();
    boolean quoted = false;
    data = !header;

    int ch = input.read();
    while(ch != -1) {
      if(quoted) {
        // quoted state
        if(ch == quoteCharacter) {
          ch = input.read();
          if(ch != quoteCharacter) {
            quoted = false;
            if(strictQuoting && ch != fieldDelimiter && ch != rowDelimiter && ch != -1)
              throw QueryError.CSV_QUOTING_X.get(ii, new TokenBuilder().add(
                  quoteCharacter).add(entry).add(quoteCharacter).add(ch));
            continue;
          }
          if(backslashes) add(entry, quoteCharacter);
        } else if(ch == '\\' && backslashes) {
          ch = bs();
        }
        add(entry, ch);
      } else if(ch == quoteCharacter) {
        if(quotes && entry.isEmpty()) {
          // parse quote
          quoted = true;
        } else if (strictQuoting) {
          throw QueryError.CSV_QUOTING_X.get(ii, new TokenBuilder().add(entry).add(quoteCharacter));
        } else {
          ch = input.read();
          if(ch != quoteCharacter || backslashes) add(entry, quoteCharacter);
          continue;
        }
      } else if(ch == fieldDelimiter) {
        // parse separator
        record(entry, false, false);
        first = false;
      } else if(ch == rowDelimiter) {
        // parse newline
        record(entry, false, true);
        first = true;
        data = true;
      } else {
        if(ch == '\\' && backslashes) ch = bs();
        add(entry, ch);
      }
      ch = input.read();
    }
    if(quoted && strictQuoting)
      throw QueryError.CSV_QUOTING_X.get(ii, new TokenBuilder().add(quoteCharacter).add(entry));
    record(entry, true, true);
  }

  /**
   * Parses a backslash character.
   * @return resulting character
   * @throws IOException I/O exception
   */
  private int bs() throws IOException {
    final int ch = input.read();
    if(ch == 'r') return 0xd;
    if(ch == 'n') return 0xa;
    if(ch == 't') return 0x9;
    return ch;
  }

  /**
   * Adds a character.
   * @param entry token builder
   * @param ch character
   */
  private static void add(final TokenBuilder entry, final int ch) {
    if(ch != -1) entry.add(XMLToken.valid(ch) ? ch : Token.REPLACEMENT);
  }

  /**
   * Adds a new record and entry.
   * @param entry entry to be added
   * @param lastRow whether this is the last row
   * @param lastField whether this is the last field of the row
   * @throws IOException I/O exception
   */
  private void record(final TokenBuilder entry, final boolean lastRow, final boolean lastField)
      throws IOException {
    final byte[] next = entry.next();
    final byte[] field = trimWhitespace || !data ? Token.trim(next) : next;
    if(field.length > 0 || !(first && lastField)) fields.add(field);
    if(lastField && !(lastRow && fields.isEmpty())) {
      if(data) conv.record();
      if(rowSize == -1) rowSize = fields.size();
      final int n = selectColumns.length != 0 ? selectColumns.length
                                              : trimRows ? rowSize : fields.size();
      for(int i = 0; i < n; ++i) {
        final int index = selectColumns.length != 0 ? selectColumns[i] - 1 : i;
        final byte[] f = index < fields.size() ? fields.get(index) : Token.EMPTY;
        if(data) {
          conv.entry(f);
        } else {
          conv.header(f);
        }
      }
      fields.reset();
    }
  }
}
