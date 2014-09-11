package org.basex.query.func.archive;

import static org.basex.query.func.archive.ArchiveText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArchiveEntries extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
    final ValueBuilder vb = new ValueBuilder();
    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(ze.isDirectory()) continue;
        final FElem e = new FElem(Q_ENTRY).declareNS();
        long s = ze.getSize();
        if(s != -1) e.add(SIZE, token(s));
        s = ze.getTime();
        if(s != -1) e.add(LAST_MOD, new Dtm(s, info).string(info));
        s = ze.getCompressedSize();
        if(s != -1) e.add(COMP_SIZE, token(s));
        e.add(ze.getName());
        vb.add(e);
      }
      return vb;
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      in.close();
    }
  }
}
