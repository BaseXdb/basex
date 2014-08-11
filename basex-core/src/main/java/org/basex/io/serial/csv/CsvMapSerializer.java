package org.basex.io.serial.csv;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * This class serializes map data as JSON. The input must conform to the rules
 * defined in the {@link JsonMapConverter} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class CsvMapSerializer extends CsvSerializer {
  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public CsvMapSerializer(final OutputStream os, final SerializerOptions opts) throws IOException {
    super(os, opts);
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(sep && lvl == 0) print(' ');

    if(!(item instanceof Map))
      throw BXCS_SERIAL_X.getIO("Top level must be a map; " + item.type + " found");

    try {
      final TokenList tl = new TokenList();
      final Map map = (Map) item;

      // check validity of keys
      final Value keys = map.keys();
      long rows = 0;
      for(final Item key : keys) {
        if(key.type != AtomType.ITR) throw BXCS_SERIAL_X.getIO("Key " + key + " is not numeric");
        final long n = key.itr(null);
        if(n <= 0) throw BXCS_SERIAL_X.getIO("Key '" + n + "' is no positive integer");
        rows = Math.max(rows, n);
      }

      // iterate through all rows
      for(int i = 0; i < rows; i++) {
        final Value row = map.get(Int.get(i + 1), null);
        if(row.size() == 1 && row instanceof Map) {
          final Map r = (Map) row;
          if(i == 0) {
            for(final Item key : r.keys()) tl.add(key.string(null));
            record(tl);
            tl.reset();
          }
          for(final Item key : r.keys()) {
            final Value val = r.get(key, null);
            if(val.size() != 1) throw BXCS_SERIAL_X.getIO("Single value expected as entry.");
            tl.add(((Item) val).string(null));
          }
        } else {
          for(final Item it : row) tl.add(it.string(null));
        }
        record(tl);
        tl.reset();
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
  }
}
