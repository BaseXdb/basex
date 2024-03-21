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
    final HashSet<String> entries = toEntries(arg(2), qc);
    try {
      final Object archive = toInput(arg(1), qc, entries != null);
      if(archive instanceof Bin) {
        try(BufferInput bi = ((Bin) archive).input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
          while(in.more() && (entries == null || !entries.isEmpty())) {
            final ZipEntry ze = in.entry();
            final String name = ze.getName();
            if(entries != null && !entries.remove(name)) continue;
            final Path file = path.resolve(name);
            if(ze.isDirectory()) {
              Files.createDirectories(file);
            } else {
              Files.createDirectories(file.getParent());
              try(BufferOutput out = new BufferOutput(new IOFile(file))) {
                in.write(out);
              }
            }
          }
        }
      } else {
        try(ZipFile zip = new ZipFile(archive.toString())) {
          for(final String entry : entries) {
            final ZipEntry ze = zip.getEntry(entry);
            if(ze == null) continue;
            final Path file = path.resolve(ze.getName());
            if(ze.isDirectory()) {
              Files.createDirectories(file);
            } else {
              Files.createDirectories(file.getParent());
              IO.write(zip.getInputStream(ze), new IOFile(file).outputStream());
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
