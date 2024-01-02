package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArchiveEntries extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final B64 archive = toB64(arg(0), qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    try(BufferInput bi = archive.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(ze.isDirectory()) continue;

        final FBuilder elem = FElem.build(Q_ENTRY).declareNS();
        long size = ze.getSize();
        if(size != -1) elem.add(Q_SIZE, size);
        size = ze.getTime();
        if(size != -1) elem.add(Q_LAST_MODIFIED, Dtm.get(size).string(info));
        size = ze.getCompressedSize();
        if(size != -1) elem.add(Q_COMPRESSED_SIZE, size);
        vb.add(elem.add(ze.getName()).finish());
      }
      return vb.value(this);
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }
}
