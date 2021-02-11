package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final B64 archive = toB64(exprs[0], qc, false);
    final TokenSet hs = entries(1, qc);

    final TokenList tl = new TokenList();
    try(ArchiveIn in = ArchiveIn.get(archive.input(info), info)) {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(!ze.isDirectory() && (hs == null || hs.remove(token(ze.getName())) != 0)) {
          final ArrayOutput out = new ArrayOutput();
          in.write(out);
          tl.add(out.finish());
        }
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    return tl;
  }
}
