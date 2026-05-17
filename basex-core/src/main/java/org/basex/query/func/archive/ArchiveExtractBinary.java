package org.basex.query.func.archive;

import java.io.*;
import java.util.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class ArchiveExtractBinary extends ArchiveFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final byte[] content : extract(qc)) {
      vb.add(B64.get(content));
    }
    return vb.value(this);
  }

  /**
   * Extracts contents from an archive.
   * @param qc query context
   * @return contents
   * @throws QueryException query exception
   */
  final TokenList extract(final QueryContext qc) throws QueryException {
    final HashSet<String> entries = toEntries(arg(1), qc);
    final TokenList contents = new TokenList();
    forEachEntry(arg(0), qc, entries, (entry, body) -> {
      if(!entry.isDirectory()) {
        final ArrayOutput out = new ArrayOutput();
        try(InputStream is = body.get()) {
          is.transferTo(out);
        }
        contents.add(out.finish());
      }
    });
    return contents;
  }
}
