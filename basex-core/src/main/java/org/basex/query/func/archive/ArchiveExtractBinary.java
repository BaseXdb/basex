package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class ArchiveExtractBinary extends ArchiveFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final byte[] content : extract(qc)) {
      vb.add(B64.get(content));
    }
    return vb.value(this);
  }

  /**
   * Extracts contents from an archive.
   * @param qc query context
   * @return contents
   * @throws QueryException query exception
   */
  final TokenList extract(final QueryContext qc) throws QueryException {
    final HashSet<String> entries = toEntries(arg(1), qc);
    final TokenList tl = new TokenList();
    try {
      final Object archive = toInput(arg(0), qc, entries != null);
      if(archive instanceof Bin) {
        try(BufferInput bi = ((Bin) archive).input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
          while(in.more() && (entries == null || !entries.isEmpty())) {
            final ZipEntry ze = in.entry();
            if(ze.isDirectory() || entries != null && !entries.remove(ze.getName())) continue;
            final ArrayOutput out = new ArrayOutput();
            in.write(out);
            tl.add(out.finish());
          }
        }
      } else {
        try(ZipFile zip = new ZipFile(archive.toString())) {
          for(final String entry : entries) {
            final ZipEntry ze = zip.getEntry(entry);
            if(ze == null || ze.isDirectory()) continue;
            final ArrayOutput out = new ArrayOutput();
            IO.write(zip.getInputStream(ze), out);
            tl.add(out.finish());
          }
        }
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    return tl;
  }
}
