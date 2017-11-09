package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.convert.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * Functions on archives.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
abstract class ArchiveFn extends StandardFunc {
  /**
   * Checks if the specified item is a string or element.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  final Item checkElemToken(final Item it) throws QueryException {
    if(it instanceof AStr || ENTRY.eq(it)) return it;
    throw ELMSTR_X_X_X.get(info, Q_ENTRY.prefixId(), it.type, it);
  }

  /**
   * Encodes the specified string to another encoding.
   * @param value value to be encoded
   * @param encoding encoding
   * @param qc query context
   * @return encoded string
   * @throws QueryException query exception
   */
  final byte[] encode(final byte[] value, final String encoding, final QueryContext qc)
      throws QueryException {
    try {
      final boolean val = qc.context.options.get(MainOptions.CHECKSTRINGS);
      return ConvertFn.toString(new ArrayInput(value), encoding, val);
    } catch(final IOException ex) {
      throw ARCH_ENCODE_X.get(info, ex);
    }
  }

  /**
   * Returns all archive entries from the specified argument.
   * A {@code null} reference is returned if no entries are specified.
   * @param e argument index
   * @param qc query context
   * @return set with all entries
   * @throws QueryException query exception
   */
  final TokenSet entries(final int e, final QueryContext qc) throws QueryException {
    TokenSet hs = null;
    if(e < exprs.length) {
      // filter result to specified entries
      hs = new TokenSet();
      final Iter names = qc.iter(exprs[e]);
      for(Item en; (en = names.next()) != null;) {
        qc.checkStop();
        hs.add(checkElemToken(en).string(info));
      }
    }
    return hs;
  }
}
