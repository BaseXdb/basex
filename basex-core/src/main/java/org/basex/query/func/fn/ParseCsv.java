package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.in.*;
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
public abstract class ParseCsv extends ParseFn {
  /**
   * Returns an XQuery value for the parsed data.
   * @param qc query context
   * @param format format (can be {@code null})
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final Value parse(final QueryContext qc, final CsvFormat format) throws QueryException {
    final byte[] source = toTokenOrNull(arg(0), qc);
    if(source == null) return Empty.VALUE;
    try(NewlineInput nli = new NewlineInput(source)) {
      return parse(qc, format, nli);
    } catch(final IOException ex) {
      throw CSV_ERROR_X.get(info, ex);
    }
  }

  /**
   * Returns a document node for the parsed data.
   * @param qc query context
   * @param format format
   * @return resulting item
   * @throws QueryException query exception
   */
  protected final Value doc(final QueryContext qc, final CsvFormat format) throws QueryException {
    final Item source = arg(0).atomItem(qc, info);
    return source.isEmpty() ? Empty.VALUE : parse(source, true, format, CSV_ERROR_X, qc);
  }

  @Override
  final Value parse(final TextInput ti, final Object options, final QueryContext qc)
      throws QueryException, IOException {
    return parse(qc, (CsvFormat) options, ti);
  }

  /**
   * Parses the specified string.
   * @param qc query context
   * @param format format
   * @param ti text input
   * @return resulting item
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private Value parse(final QueryContext qc, final CsvFormat format, final TextInput ti)
      throws QueryException, IOException {

    // parse options
    final Options copts = format == CsvFormat.W3 || format == CsvFormat.W3_XML ?
      new CsvW3Options() : format == CsvFormat.W3_ARRAYS ? new CsvW3ArraysOptions() :
      new CsvParserOptions();
    toOptions(arg(1), copts, qc);

    // transfer to common CSV options instance
    final CsvParserOptions cpopts;
    if(format == CsvFormat.W3 || format == CsvFormat.W3_XML || format == CsvFormat.W3_ARRAYS) {
      cpopts = ((CsvW3ArraysOptions) copts).finish(info, format);
    } else {
      cpopts = (CsvParserOptions) copts;
      final Value hdr = copts.get(CsvOptions.HEADER);
      if(hdr instanceof final Str str) {
        final Boolean b = Strings.toBoolean(string(str.string()));
        if(b != null) copts.put(CsvOptions.HEADER, Bln.get(b));
      }
    }
    if(format != null) cpopts.set(CsvOptions.FORMAT, format);

    // convert data
    final CsvConverter converter = CsvConverter.get(cpopts);
    return converter.convert(ti, "", info, qc);
  }
}
