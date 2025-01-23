package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * This class converts CSV data to the representation defined by fn:csv-to-arrays.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvW3ArraysConverter extends CsvXQueryConverter {
  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvW3ArraysConverter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  protected Value finish(final InputInfo ii, final QueryContext qc) throws QueryException {
    return ((XQMap) super.finish(ii, qc)).get(RECORDS);
  }
}
