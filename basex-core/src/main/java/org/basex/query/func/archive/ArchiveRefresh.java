package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.Map.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveRefresh extends ArchiveCreate {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // entries to be updated
    final IO io = toIO(arg(0), qc);
    final Map<String, Entry<Item, Item>> files = toFiles(arg(1), arg(2), qc);

    try {
      if(!localZip(io)) throw ARCHIVE_ZIP_X.get(info, io);

      try(FileSystem fs = FileSystems.newFileSystem(Paths.get(io.path()),
          (ClassLoader /* needed for JDK 13 and up */) null)) {
        for(final Entry<String, Entry<Item, Item>> file : files.entrySet()) {
          final Path path = fs.getPath(file.getKey());
          final Entry<Item, Item> entry = file.getValue();
          final Item header = entry.getKey(), content = entry.getValue();
          if(content == XQArray.empty()) {
            if(Files.exists(path)) Files.delete(path);
          } else {
            if(content instanceof Bin) {
              Files.copy(((Bin) content).input(info), path, StandardCopyOption.REPLACE_EXISTING);
            } else {
              Files.write(path, encode(toBytes(content, qc), encoding(header), false, qc));
            }
            final long time = timestamp(header, qc);
            final FileTime ft = FileTime.fromMillis(time < 0 ? System.currentTimeMillis() : time);
            Files.setLastModifiedTime(path, ft);
          }
        }
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
