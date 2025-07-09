package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FileWrite extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws IOException, QueryException {
    return write(false, qc);
  }

  /**
   * Writes items to a file.
   * @param append append flag
   * @param qc query context
   * @return empty value
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final Empty write(final boolean append, final QueryContext qc)
      throws QueryException, IOException {
    final Path path = toTarget(arg(0), qc);
    final Iter input = arg(1).iter(qc);
    final SerializerOptions options = toSerializerOptions(arg(2), qc);

    try(PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      try(Serializer ser = Serializer.get(out, options)) {
        for(Item item; (item = qc.next(input)) != null;) {
          ser.serialize(item);
        }
      }
      return Empty.VALUE;
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }
}
