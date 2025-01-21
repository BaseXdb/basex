package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveExtractText extends ArchiveExtractBinary {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String encoding = toEncodingOrNull(arg(2), ARCHIVE_ENCODE1_X, qc);
    final TokenList tl = new TokenList();
    for(final byte[] content : extract(qc)) {
      tl.add(encode(content, encoding, true, qc));
    }
    return StrSeq.get(tl);
  }
}
