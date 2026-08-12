package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;

/**
 * This class serializes a map as CSV. The input must conform to the result format of
 * fn:parse-csv.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class CsvW3Serializer extends CsvSerializer {
  /** Map has been serialized. */
  private boolean mapped;

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
    if(mapped) throw SERCSV_X_X.getIO("Single top-level map expected", item);
    mapped = true;
    w3(map);
  }
}
