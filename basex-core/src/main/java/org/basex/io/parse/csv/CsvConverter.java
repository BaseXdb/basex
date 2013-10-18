package org.basex.io.parse.csv;

import java.io.*;

import org.basex.build.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * <p>This class converts CSV input to XML.</p>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class CsvConverter {
  /** CSV options. */
  protected final CsvParserOptions copts;

  /**
   * Constructor.
   * @param opts CSV options
   */
  protected CsvConverter(final CsvParserOptions opts) {
    copts = opts;
  }

  /**
   * Converts the specified input to an XQuery item.
   * @param input input
   * @param copts options
   * @return item
   * @throws IOException I/O exception
   */
  public static Item convert(final IO input, final CsvParserOptions copts) throws IOException {
    final String encoding = copts.get(CsvParserOptions.ENCODING);
    return convert(new NewlineInput(input).encoding(encoding).content(), copts);
  }

  /**
   * Converts the specified input to an XQuery item.
   * @param input input stream
   * @param copts options
   * @return item
   * @throws QueryIOException query I/O exception
   */
  public static Item convert(final byte[] input, final CsvParserOptions copts)
      throws QueryIOException {
    return CsvParser.parse(Token.string(input), copts, get(copts));
  }

  /**
   * Returns a {@link CsvConverter} for the given configuration.
   * @param copts options
   * @return a CSV converter
   */
  private static CsvConverter get(final CsvParserOptions copts) {
    switch(copts.get(CsvOptions.FORMAT)) {
      case MAP: return new CsvMapConverter(copts);
      default:  return new CsvDirectConverter(copts);
    }
  }

  /**
   * Adds a new header.
   * @param string string
   * @throws QueryIOException query exception
   */
  abstract void header(final byte[] string) throws QueryIOException;

  /**
   * Adds a new record.
   * @throws QueryIOException query exception
   */
  abstract void record() throws QueryIOException;

  /**
   * Called when an entry is encountered.
   * @param value string
   * @throws QueryIOException query exception
   */
  abstract void entry(final byte[] value) throws QueryIOException;

  /**
   * Returns the resulting XQuery value.
   * @return result
   * @throws QueryIOException query exception
   */
  abstract Item finish() throws QueryIOException;
}
