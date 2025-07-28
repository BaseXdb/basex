package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.convert.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on archives.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class ArchiveFn extends StandardFunc {
  /**
   * Encodes the specified string to another encoding.
   * @param value value to be encoded
   * @param encoding encoding (can be {@code null})
   * @param enforce enforce conversion for UTF-8
   * @param qc query context
   * @return encoded string
   * @throws QueryException query exception
   */
  final byte[] encode(final byte[] value, final String encoding, final boolean enforce,
      final QueryContext qc) throws QueryException {
    if(encoding == Strings.UTF8 && !enforce) return value;
    try {
      final boolean validate = qc.context.options.get(MainOptions.CHECKSTRINGS);
      return ConvertFn.toString(new ArrayInput(value), encoding, validate);
    } catch(final IOException ex) {
      throw ARCHIVE_ENCODE2_X.get(info, ex);
    }
  }

  /**
   * Returns all archive entries from the specified argument.
   * @param expr expression (can be {@code Empty#UNDEFINED})
   * @param qc query context
   * @return set with all entries, or {@code null} if no entries are specified
   * @throws QueryException query exception
   */
  final HashSet<String> toEntries(final Expr expr, final QueryContext qc) throws QueryException {
    final HashSet<String> entries = new LinkedHashSet<>();
    final Iter names = expr.iter(qc);
    for(Item item; (item = qc.next(names)) != null;) {
      entries.add(toString(item, qc));
    }
    return entries.isEmpty() ? null : entries;
  }

  /**
   * Evaluates an expression to a binary archive item.
   * @param expr expression
   * @param qc query context
   * @return archive
   * @throws QueryException query exception
   */
  final Bin toArchive(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    if(item instanceof final Bin bin) return bin;
    if(item.type.isStringOrUntyped()) {
      return new B64Lazy(new IOFile(toPath(item, qc)), FILE_IO_ERROR_X);
    }
    throw STRBIN_X_X.get(info, item.seqType(), item);
  }

  /**
   * Evaluates an expression to an archive reference.
   * @param expr expression
   * @param qc query context
   * @param direct direct access to entries preferred
   * @return archive reference: {@link Bin} or {@link IO})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final Object toInput(final Expr expr, final QueryContext qc, final boolean direct)
      throws QueryException, IOException {

    final Item archive = expr.atomItem(qc, info);
    if(archive instanceof Bin) return archive;
    if(!archive.type.isStringOrUntyped()) throw STRBIN_X_X.get(info, archive.seqType(), archive);

    final IO io = toIO(archive, qc);
    return direct && localZip(io) ? io : new B64Lazy(io, FILE_IO_ERROR_X);
  }

  /**
   * Checks if the specified resource is a local ZIP archive.
   * @param io IO reference
   * @return result of check
   * @throws IOException I/O exception
   */
  static boolean localZip(final IO io) throws IOException {
    if(io instanceof IOFile) {
      if(io.hasSuffix(IO.ZIPSUFFIX)) return true;
      try(InputStream is = io.inputStream()) {
        if(is.read() == 'P' && is.read() == 'K') return true;
      }
    }
    return false;
  }
}
