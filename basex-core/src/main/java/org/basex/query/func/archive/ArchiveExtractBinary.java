package org.basex.query.func.archive;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class ArchiveExtractBinary extends ArchiveFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] b : extract(qc)) vb.add(new B64(b));
    return vb;
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
    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(!ze.isDirectory() && (hs == null || hs.delete(token(ze.getName())) != 0))
          tl.add(in.read());
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      in.close();
    }
    return tl;
  }
}
