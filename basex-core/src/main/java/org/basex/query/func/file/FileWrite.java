package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FileWrite extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    write(false, qc);
    return Empty.VALUE;
  }

  /**
   * Writes items to a file.
   * @param append append flag
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final synchronized void write(final boolean append, final QueryContext qc)
      throws QueryException, IOException {

    final Path path = toParent(toPath(arg(0), qc));
    final Iter input = arg(1).iter(qc);
    final Item options = arg(2).item(qc, info);
    final SerializerOptions sopts = FuncOptions.serializer(options, info);

    try(PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      try(Serializer ser = Serializer.get(out, sopts)) {
        for(Item item; (item = qc.next(input)) != null;) {
          ser.serialize(item);
        }
      }
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }
}
