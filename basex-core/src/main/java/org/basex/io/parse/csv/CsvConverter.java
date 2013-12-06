package org.basex.io.parse.csv;

import java.io.*;

import org.basex.build.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * <p>This class converts CSV input to XML.</p>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class CsvConverter {
  /**
   * Converts the specified input to an XQuery item.
   * @param input input
   * @param copts options
   * @return item
   * @throws IOException I/O exception
   */
  public static Item convert(final IO input, final CsvParserOptions copts) throws IOException {
    final String encoding = copts.get(CsvParserOptions.ENCODING);
    return CsvParser.parse(new NewlineInput(input).encoding(encoding).cache().toString(), copts,
        get(copts));
  }

  /**
   * Returns a  for the given configuration.
   * @param copts options
   * @return a CSV converter
   */
  private static CsvConverter get(final CsvParserOptions copts) {
    switch(copts.get(CsvOptions.FORMAT)) {
      case MAP: return new CsvMapConverter();
      default:  return new CsvDirectConverter(copts);
    }
  }

  /**
   * Adds a new header.
   * @param string string
   */
  abstract void header(final byte[] string);

  /**
   * Adds a new record.
   */
  abstract void record();

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
