package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.parse.csv.*;
import org.basex.io.serial.*;
import org.basex.query.*;
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
public final class CsvW3Serializer extends CsvSerializer {
  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  public CsvW3Serializer(final OutputStream os, final SerializerOptions sopts)
      throws IOException {
    super(os, sopts);
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(!(item instanceof final XQMap map)) throw typeError("Top-level map", item);
    final TokenList tl = new TokenList();
    try {
      // print header
      if(header) {
        final Value columns = map.getOrNull(CsvConverter.COLUMNS);
        if(columns == null) throw CSV_SERIALIZE_X.getIO("Map has no 'columns' key");
        row(columns, tl);
      }
      // print rows
      final Value rows = map.getOrNull(CsvConverter.ROWS);
      if(rows == null) throw CSV_SERIALIZE_X.getIO("Map has no 'rows' key");
      for(final Item record : rows) {
        if(!(record instanceof final XQArray array)) throw typeError("Array", record);
        row(array.members(), tl);
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
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
      if(!(value instanceof final Item item) || item.size() != 1) throw typeError("Item", value);
      tl.add(item.string(null));
    }
    record(tl);
  }

  /**
   * Returns a type error.
   * @param expected expected type
   * @param found found value
   * @return error
   */
  private static QueryIOException typeError(final String expected, final Value found) {
    return CSV_SERIALIZE_X_X.getIO(expected + " expected, " + found.seqType() + " found ", found);
  }
}
