package org.basex.io.parse.csv;

import java.util.*;

import org.basex.build.csv.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;

/**
 * This class converts CSV data to an XQuery map.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class CsvMapConverter extends CsvConverter {
  /** All records. */
  private final ArrayList<ItemList> records = new ArrayList<>(1);
  /** Current record. */
  private ItemList record = new ItemList();

  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvMapConverter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  protected void header(final byte[] string) {
    headers.add(string);
  }

  @Override
  protected void record() {
    record = new ItemList();
    if(!headers.isEmpty()) record.add(Map.EMPTY);
    records.add(record);
    col = 0;
  }

  @Override
  protected void entry(final byte[] value) throws QueryIOException {
    if(headers.isEmpty()) {
      record.add(Str.get(value));
    } else {
      byte[] name = headers.get(col++);
      if(name == null) name = ENTRY;
      try {
        record.set(0, ((Map) record.get(0)).put(Str.get(name), Str.get(value), null));
      } catch(final QueryException ex) {
        throw new QueryIOException(ex);
      }
    }
  }

  @Override
  protected Map finish() throws QueryIOException {
    try {
      Map map = Map.EMPTY;
      int row = 1;
      for(final ItemList list : records) {
        map = map.put(Int.get(row++), list.value(), null);
      }
      return map;
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
