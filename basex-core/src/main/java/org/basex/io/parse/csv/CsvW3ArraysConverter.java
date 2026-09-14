package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class converts CSV data to the representation defined by fn:csv-to-arrays.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class CsvW3ArraysConverter extends CsvConverter {
  /** String array type. */
  private static final ArrayType STRING_ARRAY = ArrayType.get(Types.STRING_O);

  /** Rows. */
  private final ItemList rows = new ItemList();
  /** Current row (can be {@code null}). */
  private TokenList row;

  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvW3ArraysConverter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  protected final void header(final byte[] string) {
    headers.add(shared.token(string));
  }

  @Override
  protected final void record() {
    if(row != null) rows.add(XQArray.items(StrSeq.get(row.next())));
    else row = new TokenList();
  }

  @Override
  protected final void entry(final byte[] value) {
    row.add(shared.token(value));
  }

  @Override
  protected final void init(final String uri) {
  }

  @Override
  protected Value finish(final InputInfo ii, final QueryContext qc) throws QueryException {
    if(row != null) rows.add(XQArray.items(StrSeq.get(row.next())));
    return rows.value(STRING_ARRAY);
  }
}
