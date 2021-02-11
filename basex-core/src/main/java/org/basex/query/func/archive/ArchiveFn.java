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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class ArchiveFn extends StandardFunc {
  /**
   * Checks if the specified item is a string or element.
   * @param item item to be checked
   * @return item
   * @throws QueryException query exception
   */
  final Item checkElemToken(final Item item) throws QueryException {
    if(item instanceof AStr || ENTRY.matches(item)) return item;
    throw ELMSTR_X_X_X.get(info, Q_ENTRY.prefixId(), item.type, item);
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
      final boolean validate = qc.context.options.get(MainOptions.CHECKSTRINGS);
      return ConvertFn.toString(new ArrayInput(value), encoding, validate);
    } catch(final IOException ex) {
      throw ARCHIVE_ENCODE2_X.get(info, ex);
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
      final Iter names = exprs[e].iter(qc);
      for(Item en; (en = qc.next(names)) != null;) {
        hs.add(checkElemToken(en).string(info));
      }
    }
    return hs;
  }
}
