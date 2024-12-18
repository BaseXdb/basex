package org.basex.io.parse.csv;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * A CSV parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team 2005-24, BSD License
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
  /** Column separator (see {@link CsvOptions#SEPARATOR}). */
  private final int separator;
  /** Row delimiter (see {@link CsvOptions#ROW_DELIMITER}). */
  private final int rowDelimiter;
  /** Quote character (see {@link CsvOptions#QUOTE_CHARACTER}). */
  private final int quoteCharacter;
  /** Parse quotes.  */
  private final boolean quotes;
  /** Trim whitespace. */
  private final boolean trimWhitespace;
  /** Strict quoting. */
  private final boolean strictQuoting;

  /** First entry of a line. */
  private boolean first = true;
  /** Data mode. */
  private boolean data;

  /**
   * Constructor.
   * @param input input
   * @param opts options
   * @param conv converter
   */
  public CsvParser(final TextInput input, final CsvParserOptions opts, final CsvConverter conv) {
    this.input = input;
    this.conv = conv;
    header = opts.get(CsvOptions.HEADER);
    separator = opts.separator();
    rowDelimiter = opts.rowDelimiter();
    quoteCharacter = opts.quoteCharacter();
    quotes = opts.get(CsvOptions.QUOTES);
    backslashes = opts.get(CsvOptions.BACKSLASHES);
    trimWhitespace = opts.get(CsvOptions.TRIM_WHITSPACE);
    strictQuoting = opts.get(CsvOptions.STRICT_QUOTING);
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
            if(strictQuoting && ch != separator && ch != rowDelimiter && ch != -1)
              throw QueryError.CSV_QUOTING_X.get(ii,
                  new String(Character.toChars(quoteCharacter)) + entry
                      + new String(Character.toChars(quoteCharacter))
                      + new String(Character.toChars(ch)));
            continue;
          }
          if(backslashes) add(entry, quoteCharacter);
        } else if(ch == '\\' && backslashes) {
          ch = bs();
        }
        add(entry, ch);
      } else if(ch == quoteCharacter) {
        if(quotes) {
          if(strictQuoting && !entry.isEmpty()) throw QueryError.CSV_QUOTING_X.get(ii,
              entry + new String(Character.toChars(quoteCharacter)));
          // parse quote
          quoted = true;
        } else {
          ch = input.read();
          if(ch != quoteCharacter || backslashes) add(entry, quoteCharacter);
          continue;
        }
      } else if(ch == separator) {
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
      throw QueryError.CSV_QUOTING_X.get(ii, new String(Character.toChars(quoteCharacter)) + entry);
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
    byte[] field = entry.next();
    if(trimWhitespace) field = Token.trim(field);
    final boolean record = !lastRow || field.length > 0;
    if(record && first && data) conv.record();
    if(record || !first) {
      if(first && lastField && field.length == 0) return;
      if(data) {
        conv.entry(field);
      } else {
        conv.header(field);
      }
    }
  }
}
