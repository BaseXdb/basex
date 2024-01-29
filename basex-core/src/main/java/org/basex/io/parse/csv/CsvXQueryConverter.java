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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CsvXQueryConverter extends CsvConverter {
  /** String array type. */
  private static final ArrayType STRING_ARRAY = ArrayType.get(SeqType.STRING_O);

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
    headers.add(shared.token(string));
  }

  @Override
  protected void record() {
    if(row != null) rows.add(row.array(STRING_ARRAY));
    row = new ArrayBuilder();
  }

  @Override
  protected void entry(final byte[] value) {
    row.append(Str.get(shared.token(value)));
  }

  @Override
  protected void init(final String uri) {
  }

  @Override
  protected XQMap finish() throws QueryException {
    if(row != null) rows.add(row.array(STRING_ARRAY));
    final MapBuilder mb = new MapBuilder();
    if(!headers.isEmpty()) {
      final ArrayBuilder names = new ArrayBuilder();
      for(final byte[] header : headers) names.append(Str.get(header));
      mb.put(NAMES, names.array(STRING_ARRAY));
    }
    return mb.put(RECORDS, rows.value(STRING_ARRAY)).map();
  }
}
