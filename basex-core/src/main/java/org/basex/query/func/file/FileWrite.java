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
 * @author BaseX Team 2005-21, BSD License
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

    final Path path = checkParentDir(toPath(0, qc));
    final Item opts = exprs.length > 2 ? exprs[2].item(qc, info) : Empty.VALUE;
    final SerializerOptions sopts = FuncOptions.serializer(opts, info);

    try(PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      try(Serializer ser = Serializer.get(out, sopts)) {
        final Iter iter = exprs[1].iter(qc);
        for(Item item; (item = iter.next()) != null;) ser.serialize(item);
      }
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }
}
