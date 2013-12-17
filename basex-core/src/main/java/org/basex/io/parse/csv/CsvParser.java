package org.basex.io.parse.csv;

import org.basex.build.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * A CSV parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class CsvParser extends InputParser {
  /** Converter. */
  private final CsvConverter conv;
  /** Header flag. */
  private final boolean header;
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
   * @param in input
   * @param opts options
   * @param cnv converter
   */
  private CsvParser(final String in, final CsvParserOptions opts, final CsvConverter cnv) {
    super(in);
    header = opts.get(CsvOptions.HEADER);
    separator = opts.separator();
    quotes = opts.get(CsvOptions.QUOTES);
    conv = cnv;
  }

  /**
   * Parses the input string, directs the parse events to the given handler and returns
   * the resulting value.
   * @param input input string
   * @param opts options
   * @param conv converter
   * @throws QueryIOException parse exception
   */
  static void parse(final String input, final CsvParserOptions opts, final CsvConverter conv)
      throws QueryIOException {
    new CsvParser(input, opts, conv).parse();
  }

  /**
   * Parses a CSV expression.
   * @throws QueryIOException query I/O exception
   */
  private void parse() throws QueryIOException {
    final TokenBuilder entry = new TokenBuilder();
    data = !header;

    for(char ch; (ch = consume()) != 0;) {
      if(quoted) {
        // quoted state
        if(ch == '"') {
          if(!consume('"')) {
            quoted = false;
            continue;
          }
        }
        entry.add(XMLToken.valid(ch) ? ch : '?');
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
        // parse any other character
        entry.add(XMLToken.valid(ch) ? ch : '?');
      }
    }
    record(entry, !entry.isEmpty());
  }

  /**
   * Adds a new record and entry.
   * @param entry entry to be added
   * @param record add new record
   * @throws QueryIOException query I/O exception
   */
  private void record(final TokenBuilder entry, final boolean record) throws QueryIOException {
    if(record && first && data) conv.record();
    if(record || !first) {
      if(data) {
        conv.entry(entry.finish());
      } else {
        conv.header(entry.finish());
      }
      entry.reset();
    }
  }
}
