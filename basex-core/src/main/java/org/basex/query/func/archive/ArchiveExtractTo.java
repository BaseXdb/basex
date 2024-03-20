package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class ArchiveExtractTo extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Path path = toPath(arg(0), qc);
    final B64 archive = toB64(arg(1), qc);
    final HashSet<String> entries = entries(arg(2), qc);

    try(BufferInput bi = archive.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
      while(in.more()) {
        if(entries != null && entries.isEmpty()) break;
        final ZipEntry ze = in.entry();
        final String name = ze.getName();
        if(entries == null || entries.remove(name)) {
          final Path file = path.resolve(name);
          if(ze.isDirectory()) {
            Files.createDirectories(file);
          } else {
            Files.createDirectories(file.getParent());
            try(BufferOutput out = new BufferOutput(new IOFile(file.toFile()))) {
              in.write(out);
            }
          }
        }
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
