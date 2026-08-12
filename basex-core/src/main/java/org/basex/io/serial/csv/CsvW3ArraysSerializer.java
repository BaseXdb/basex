package org.basex.io.serial.csv;

import java.io.*;

import org.basex.io.serial.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

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
    if(!(item instanceof final XQArray array)) throw typeError("Array", item);
    w3(array);
  }
}
