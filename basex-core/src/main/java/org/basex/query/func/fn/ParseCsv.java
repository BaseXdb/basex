package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * CSV parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseCsv extends Parse {
  /**
   * Returns an XDM value for the parsed data.
   * @param qc query context
   * @param format format (can be {@code null})
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final Value parse(final QueryContext qc, final CsvFormat format) throws QueryException {
    return parse(qc, format, toTokenOrNull(arg(0), qc));
  }

  /**
   * Returns a document node for the parsed data.
   * @param qc query context
   * @param format format
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final Value doc(final QueryContext qc, final CsvFormat format) throws QueryException {
    final Item item;
    try {
      item = unparsedText(qc, false, false, null);
    } catch(final QueryException ex) {
      throw error(ex, ex.error() == INVCHARS_X ? CSV_ERROR_X : null);
    }
    return item.isEmpty() ? Empty.VALUE : parse(qc, format, item.string(info));
  }

  /**
   * Parses the specified string.
   * @param data data to parse (can be {@code null})
   * @param qc query context
   * @param format format
   * @return resulting item
   * @throws QueryException query exception
   */
  private Value parse(final QueryContext qc, final CsvFormat format, final byte[] data)
      throws QueryException {

    // (hopefully temporary) special case
    if(data == null && format != CsvFormat.W3_MAP) return Empty.VALUE;

    // parse options
    final Options copts = format == CsvFormat.W3_MAP || format == CsvFormat.W3_XML ?
      new CsvW3Options() : format == CsvFormat.W3_ARRAYS ? new CsvW3ArraysOptions() :
      new CsvParserOptions();
    toOptions(arg(1), copts, qc);

    // transfer to common CSV options instance
    final CsvParserOptions cpopts = format == CsvFormat.W3_MAP || format == CsvFormat.W3_XML ?
      ((CsvW3Options) copts).finish(info, format) : format == CsvFormat.W3_ARRAYS ?
      ((CsvW3ArraysOptions) copts).finish(info, format) :
      (CsvParserOptions) copts;
    if(format != null) cpopts.set(CsvOptions.FORMAT, format);

    // convert data
    final CsvConverter converter = CsvConverter.get(cpopts);
    try {
      return converter.convert(new IOContent(data != null ? data : Token.EMPTY), info, qc);
    } catch(final IOException ex) {
      throw CSV_ERROR_X.get(info, ex);
    }
  }
}
