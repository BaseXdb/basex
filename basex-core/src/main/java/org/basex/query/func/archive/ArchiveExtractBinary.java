package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

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
    for(final byte[] bytes : extract(qc)) vb.add(B64.get(bytes));
    return vb.value(this);
  }

  /**
   * Extracts entries from the archive.
   * @param qc query context
   * @return text entries
   * @throws QueryException query exception
   */
  final TokenList extract(final QueryContext qc) throws QueryException {
    final HashSet<String> entries = entries(arg(1), qc);
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
            try(InputStream is = zip.getInputStream(ze); BufferInput in = new BufferInput(is)) {
              for(int b; (b = in.read()) != -1;) out.write(b);
            }
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
