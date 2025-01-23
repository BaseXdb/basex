package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;

import java.io.*;

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
public final class CsvW3ArraysSerializer extends CsvSerializer {
  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  public CsvW3ArraysSerializer(final OutputStream os, final SerializerOptions sopts)
      throws IOException {
    super(os, sopts);
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
