package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArchiveEntries extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
    final ValueBuilder vb = new ValueBuilder(qc);
    try(ArchiveIn in = ArchiveIn.get(archive.input(info), info)) {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(ze.isDirectory()) continue;
        final FElem elem = new FElem(Q_ENTRY).declareNS();
        long size = ze.getSize();
        if(size != -1) elem.add(SIZE, token(size));
        size = ze.getTime();
        if(size != -1) elem.add(LAST_MODIFIED, Dtm.get(size).string(info));
        size = ze.getCompressedSize();
        if(size != -1) elem.add(COMPRESSED_SIZE, token(size));
        elem.add(ze.getName());
        vb.add(elem);
      }
      return vb.value(this);
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }
}
