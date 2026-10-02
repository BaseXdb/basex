package org.basex.query.func.archive;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveExtractTo extends ArchiveFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Path path = toPath(arg(0), qc).toAbsolutePath().normalize();
    final HashSet<String> entries = toEntries(arg(2), qc);

    forEachEntry(arg(1), qc, entries, (entry, body) -> {
      // re-anchor the entry path under base; skip entries without usable components
      final Path file = IOFile.resolve(path, entry.getName());
      if(file == null) return;

      if(entry.isDirectory()) {
        Files.createDirectories(file);
      } else {
        Files.createDirectories(file.getParent());
        try(BufferOutput out = new BufferOutput(new IOFile(file));
            InputStream is = body.get()) {
          is.transferTo(out);
        }
      }
      // preserve the entry's modification time on the extracted file/directory
      final long time = entry.getTime();
      if(time >= 0) Files.setLastModifiedTime(file, FileTime.fromMillis(time));
    });
    return Empty.VALUE;
  }
}
