package org.basex.io.parse.csv;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.in.*;
import org.basex.util.*;

/**
 * A CSV parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class CsvParser {
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
  /** Parse quotes.  */
  private final boolean quotes;

  /** First entry of a line. */
  private boolean first = true;
  /** Quoted state.  */
  private boolean quoted;
  /** Data mode. */
  private boolean data;

  /**
   * Constructor.
   * @param input input
   * @param opts options
   * @param conv converter
   */
  private CsvParser(final TextInput input, final CsvParserOptions opts, final CsvConverter conv) {
    this.input = input;
    this.conv = conv;
    header = opts.get(CsvOptions.HEADER);
    separator = opts.separator();
    quotes = opts.get(CsvOptions.QUOTES);
    backslashes = opts.get(CsvOptions.BACKSLASHES);
  }

  /**
   * Parses the input string, directs the parse events to the given handler and returns
   * the resulting value.
   * @param input input string
   * @param opts options
   * @param conv converter
   * @throws IOException I/O exception
   */
  static void parse(final TextInput input, final CsvParserOptions opts, final CsvConverter conv)
      throws IOException {
    new CsvParser(input, opts, conv).parse();
  }

  /**
   * Parses a CSV expression.
   * @throws IOException query I/O exception
   */
  private void parse() throws IOException {
    final TokenBuilder entry = new TokenBuilder();
    data = !header;

    int ch = input.read();
    while(ch != -1) {
      if(quoted) {
        // quoted state
        if(backslashes) {
          if(ch == '\\') {
            ch = bs();
            if(ch == -1) break;
          } else if(ch == '"') {
            quoted = false;
            ch = input.read();
            continue;
          }
        } else {
          if(ch == '"') {
            ch = input.read();
            if(ch != '"') {
              quoted = false;
              continue;
            }
          }
        }
        add(entry, ch);
      } else if(quotes && ch == '"') {
        // parse quote
        quoted = true;
      } else if(ch == separator) {
        // parse separator
        record(entry, true);
        first = false;
      } else if(ch == '\n') {
        // parse newline
        record(entry, !entry.isEmpty());
        first = true;
        data = true;
      } else {
        if(backslashes) {
          if(ch == '\\') ch = bs();
          if(ch == -1) break;
        }
        // parse any other character
        add(entry, ch);
      }
      ch = input.read();
    }
    record(entry, !entry.isEmpty());
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
    entry.add(XMLToken.valid(ch) ? ch : Token.INVALID);
  }

  /**
   * Adds a new record and entry.
   * @param entry entry to be added
   * @param record add new record
   * @throws IOException I/O exception
   */
  private void record(final TokenBuilder entry, final boolean record) throws IOException {
    if(record && first && data) conv.record();
    if(record || !first) {
      if(data) {
        conv.entry(entry.next());
      } else {
        conv.header(entry.next());
      }
    }
  }
}
