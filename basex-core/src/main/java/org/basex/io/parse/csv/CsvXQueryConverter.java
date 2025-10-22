package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class converts CSV data to an XQuery representation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class CsvXQueryConverter extends CsvConverter {
  /** String array type. */
  private static final ArrayType STRING_ARRAY = ArrayType.get(Types.STRING_O);

  /** Field names. */
  public static final Str NAMES = Str.get("names");
  /** Records. */
  public static final Str RECORDS = Str.get("records");

  /** Rows. */
  private final ItemList rows = new ItemList();
  /** Current row. */
  private TokenList row;

  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvXQueryConverter(final CsvParserOptions opts) {
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
    final MapBuilder mb = new MapBuilder();
    if(!headers.isEmpty()) mb.put(NAMES, XQArray.items(StrSeq.get(headers)));
    return mb.put(RECORDS, rows.value(STRING_ARRAY)).map();
  }
}
