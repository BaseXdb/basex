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
 * This class serializes map data as CSV. The input must conform to the XQuery CSV representation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class CsvXQuerySerializer extends CsvSerializer {
  /**
   * Constructor.
   * @param os output stream
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public CsvXQuerySerializer(final OutputStream os, final SerializerOptions opts)
      throws IOException {
    super(os, opts);
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(sep && level == 0) out.print(' ');

    if(!(item instanceof Map))
      throw BXCS_SERIAL_X.getIO("Top level must be a map; " + item.type + " found");

    final Map m = (Map) item;
    final TokenList tl = new TokenList();
    try {
      // print header
      if(header) {
        if(!m.contains(CsvXQueryConverter.NAMES, null))
          throw BXCS_SERIAL_X.getIO("Map has no 'names' key");
        record(m.get(CsvXQueryConverter.NAMES, null), tl);
      }
      // print records
      if(!m.contains(CsvXQueryConverter.RECORDS, null))
        throw BXCS_SERIAL_X.getIO("Map has no 'records' key");
      for(final Item record : m.get(CsvXQueryConverter.RECORDS, null)) {
        record(record, tl);
      }
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
  private void record(final Value line, final TokenList tl) throws QueryException, IOException {
    if(!(line instanceof Array))
      throw BXCS_SERIAL_X.getIO("Array expected; " + line.type + "found");
    for(final Value entry : ((Array) line).members()) {
      if(!(entry instanceof AStr))
        throw BXCS_SERIAL_X.getIO("Single item expected; " + entry.type + "found");
      tl.add(((Item) entry).string(null));
    }
    record(tl);
  }
}
