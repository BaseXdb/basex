package org.basex.io.parse.csv;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.build.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.util.list.*;

/**
 * This class converts CSV data to an XQuery map.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class CsvMapConverter extends CsvConverter {
  /** CSV token. */
  private static final byte[] ENTRY = token("entry");

  /** Headers. */
  private final TokenList headers = new TokenList();
  /** All records. */
  private final ArrayList<ValueBuilder> records = new ArrayList<ValueBuilder>();

  /** Current record. */
  private ValueBuilder record = new ValueBuilder();
  /** Current column. */
  private int col;

  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvMapConverter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  public void header(final byte[] string) {
    headers.add(string);
  }

  @Override
  public void record() {
    record = new ValueBuilder();
    if(!headers.isEmpty()) record.add(Map.EMPTY);
    records.add(record);
    col = 0;
  }

  @Override
  public void entry(final byte[] value) throws QueryIOException {
    if(headers.isEmpty()) {
      record.add(Str.get(value));
    } else {
      byte[] name = headers.get(col++);
      if(name == null) name = ENTRY;
      try {
        record.set(((Map) record.get(0)).insert(Str.get(name), Str.get(value), null), 0);
      } catch(final QueryException ex) {
        throw new QueryIOException(ex);
      }
    }
  }

  @Override
  public Map finish() throws QueryIOException {
    try {
      Map map = Map.EMPTY;
      int row = 1;
      for(final ValueBuilder vb : records) {
        map = map.insert(Int.get(row++), vb.value(), null);
      }
      return map;
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
