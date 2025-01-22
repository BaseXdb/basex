package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.list.*;

/**
 * This class serializes a map as CSV. The input must conform to the result format of
 * fn:parse-csv.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class CsvMapSerializer extends CsvSerializer {
  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  public CsvMapSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts);
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(sep && level == 0) out.print(' ');

    if(!(item instanceof XQMap))
      throw CSV_SERIALIZE_X_X.getIO("Top level must be a map, found " + item.type, item);

    final XQMap m = (XQMap) item;
    final TokenList tl = new TokenList();
    try {
      // print header
      if(header) {
        if(!m.contains(FnParseCsv.COLUMNS)) throw CSV_SERIALIZE_X.getIO("Map has no 'columns' key");
        row(m.get(FnParseCsv.COLUMNS), tl);
      }
      // print rows
      if(!m.contains(FnParseCsv.ROWS)) throw CSV_SERIALIZE_X.getIO("Map has no 'rows' key");
      for(final Item record : m.get(FnParseCsv.ROWS)) row(((XQArray) record).iterable(), tl);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
  }

  /**
   * Serializes a single line (header or contents).
   * @param line line to be serialized
   * @param tl token list
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void row(final Iterable<? extends Value> line, final TokenList tl)
      throws QueryException, IOException {
    for(final Value value : line) {
      if(!value.isItem()) throw CSV_SERIALIZE_X_X.getIO(
          "Item expected, found " + value.seqType(), value);
      tl.add(((Item) value).string(null));
    }
    record(tl);
  }
}
