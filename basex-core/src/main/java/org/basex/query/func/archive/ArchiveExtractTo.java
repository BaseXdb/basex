package org.basex.query.func.archive;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveExtractTo extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Path path = toPath(arg(0), qc).toAbsolutePath().normalize();
    final HashSet<String> entries = toEntries(arg(2), qc);

    forEachEntry(arg(1), qc, entries, (entry, body) -> {
      // re-anchor the entry path under base, silently dropping "." and ".." components
      // so traversing entries like "../foo" extract as "foo" under base. Absolute roots
      // ("/abs/path") drop out because Path iteration excludes the root component.
      Path file = path;
      for(final Path part : Paths.get(entry.getName()).normalize()) {
        final String p = part.toString();
        if(!p.equals("..") && !p.equals(".")) file = file.resolve(part);
      }
      // entry name produced no usable components (e.g., "" or "..") — skip
      if(file.equals(path)) return;

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
