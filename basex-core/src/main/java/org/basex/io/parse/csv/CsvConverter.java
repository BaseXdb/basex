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
  /** CSV options. */
  final CsvParserOptions copts;

  /**
   * Constructor.
   * @param opts json options
   */
  CsvConverter(final CsvParserOptions opts) {
    copts = opts;
  }

  /**
   * Converts the specified input to XML.
   * @param input input
   * @throws IOException I/O exception
   */
  public void convert(final IO input) throws IOException {
    final String encoding = copts.get(CsvParserOptions.ENCODING);
    final String csv = new NewlineInput(input).encoding(encoding).cache().toString();
    CsvParser.parse(csv, copts, this);
  }

  /**
   * Returns a  for the given configuration.
   * @param copts options
   * @return a CSV converter
   */
  public static CsvConverter get(final CsvParserOptions copts) {
    switch(copts.get(CsvOptions.FORMAT)) {
      case MAP: return new CsvMapConverter(copts);
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
  public abstract Item finish() throws QueryIOException;
}
