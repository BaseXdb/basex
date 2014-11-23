package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArchiveExtractText extends ArchiveExtractBinary {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final String enc = toEncoding(2, ARCH_ENCODING_X, qc);
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] b : extract(qc)) vb.add(Str.get(encode(b, enc, qc)));
    return vb;
  }
}
