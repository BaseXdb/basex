package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;

/**
 * This class serializes a sequence of arrays as CSV. The input must conform to the result
 * format of fn:csv-to-arrays.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class CsvArraysSerializer extends CsvSerializer {
  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @param copts csv options
   * @throws IOException I/O exception
   */
  public CsvArraysSerializer(final OutputStream os, final SerializerOptions sopts,
      final CsvOptions copts) throws IOException {
    super(os, sopts, copts);
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(!(item instanceof XQArray))
      throw CSV_SERIALIZE_X_X.getIO("Array expected, found " + item.seqType(), item);
    final TokenList tl = new TokenList();
    try {
      for(final Value value : ((XQArray) item).iterable()) {
        if(!value.isItem()) throw CSV_SERIALIZE_X_X.getIO(
            "Item expected, found " + value.seqType(), value);
        tl.add(((Item) value).string(null));
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    record(tl);
  }
}
