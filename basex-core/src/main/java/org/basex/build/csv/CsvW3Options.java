package org.basex.build.csv;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import org.basex.build.csv.CsvOptions.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for fn:parse-csv and fn:csv-to-xml.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class CsvW3Options extends CsvW3ArraysOptions {
  /** parse-csv option header. */
  public static final ValueOption HEADER = new ValueOption("header", ITEM_ZM, Bln.FALSE);
  /** parse-csv option select-columns. */
  public static final NumbersOption SELECT_COLUMNS = new NumbersOption("select-columns");
  /** parse-csv option trim-rows. */
  public static final BooleanOption TRIM_ROWS = new BooleanOption("trim-rows", false);

  @Override
  public CsvParserOptions finish(final InputInfo ii, final CsvFormat format) throws QueryException {
    final Value header = get(HEADER);
    if(!BOOLEAN_O.instance(header) && !STRING_ZM.instance(header))
      throw typeError(header, AtomType.STRING, ii);

    final CsvParserOptions copts = super.finish(ii, format);
    copts.set(CsvOptions.TRIM_ROWS, get(TRIM_ROWS));
    copts.set(CsvOptions.SELECT_COLUMNS, get(SELECT_COLUMNS));
    copts.set(CsvOptions.HEADER, get(HEADER));
    return copts;
  }
}
