package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArchiveExtractText extends ArchiveExtractBinary {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String encoding = toEncodingOrNull(2, ARCHIVE_ENCODE1_X, qc);
    final TokenList tl = new TokenList();
    for(final byte[] bytes : extract(qc)) tl.add(encode(bytes, encoding, qc));
    return StrSeq.get(tl);
  }
}
