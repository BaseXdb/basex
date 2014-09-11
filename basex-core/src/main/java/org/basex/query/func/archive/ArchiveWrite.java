package org.basex.query.func.archive;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArchiveWrite extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final Path path = toPath(0, qc);
    final B64 archive = toB64(exprs[1], qc, false);
    final TokenSet hs = entries(2, qc);

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        final String name = ze.getName();
        if(hs == null || hs.delete(token(name)) != 0) {
          final Path file = path.resolve(name);
          if(ze.isDirectory()) {
            Files.createDirectories(file);
          } else {
            Files.createDirectories(file.getParent());
            Files.write(file, in.read());
          }
        }
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      in.close();
    }
    return null;
  }
}
