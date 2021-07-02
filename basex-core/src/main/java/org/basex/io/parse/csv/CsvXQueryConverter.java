package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * This class converts CSV data to an XQuery representation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CsvXQueryConverter extends CsvConverter {
  /** Field names. */
  public static final Str NAMES = Str.get("names");
  /** Records. */
  public static final Str RECORDS = Str.get("records");

  /** Rows. */
  private final ItemList rows = new ItemList();
  /** Current row. */
  private ArrayBuilder row;

  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvXQueryConverter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  protected void header(final byte[] string) {
    headers.add(string);
  }

  @Override
  protected void record() {
    if(row != null) rows.add(row.freeze(SeqType.STRING_ARRAY));
    row = new ArrayBuilder();
  }

  @Override
  protected void entry(final byte[] value) {
    row.append(Str.get(value));
  }

  @Override
  protected void init(final String uri) {
  }

  @Override
  protected XQMap finish() throws QueryIOException {
    if(row != null) rows.add(row.freeze(SeqType.STRING_ARRAY));
    try {
      XQMap map = XQMap.empty();
      if(!headers.isEmpty()) {
        final ArrayBuilder names = new ArrayBuilder();
        for(final byte[] header : headers) names.append(Str.get(header));
        map = map.put(NAMES, names.freeze(SeqType.STRING_ARRAY), null);
      }
      return map.put(RECORDS, rows.value(), null);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
