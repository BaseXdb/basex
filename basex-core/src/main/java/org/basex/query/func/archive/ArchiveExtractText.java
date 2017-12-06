package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ArchiveExtractText extends ArchiveExtractBinary {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String encoding = toEncoding(2, ARCHIVE_ENCODE1_X, qc);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final byte[] bytes : extract(qc)) vb.add(Str.get(encode(bytes, encoding, qc)));
    return vb.value();
  }
}
