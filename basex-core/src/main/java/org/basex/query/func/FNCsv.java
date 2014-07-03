package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions for parsing CSV input.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class FNCsv extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_OPTIONS = QNm.get("csv:options", CSVURI);

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNCsv(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _CSV_PARSE:     return parse(qc);
      case _CSV_SERIALIZE: return serialize(qc);
      default:             return super.item(qc, ii);
    }
  }

  /**
   * Converts CSV input to an element node.
   * @param qc query context
   * @return element node
   * @throws QueryException query exception
   */
  private Item parse(final QueryContext qc) throws QueryException {
    final byte[] input = checkStr(exprs[0], qc);
    final CsvParserOptions opts = checkOptions(1, Q_OPTIONS, new CsvParserOptions(), qc);
    try {
      final CsvConverter conv = CsvConverter.get(opts);
      conv.convert(new IOContent(input));
      return conv.finish();
    } catch(final IOException ex) {
      throw BXCS_PARSE.get(info, ex);
    }
  }

  /**
   * Serializes the specified XML document as CSV.
   * @param qc query context
   * @return string representation
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    final CsvOptions copts = checkOptions(1, Q_OPTIONS, new CsvOptions(), qc);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.CSV);
    sopts.set(SerializerOptions.CSV, copts);
    return Str.get(delete(serialize(iter, sopts, INVALIDOPT), '\r'));
  }
}
