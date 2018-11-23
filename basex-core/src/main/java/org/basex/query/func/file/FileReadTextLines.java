package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FileReadTextLines extends FileRead {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);

    final Path path = toPath(0, qc);
    final String encoding = toEncoding(1, FILE_UNKNOWN_ENCODING_X, qc);
    final boolean validate = exprs.length < 3 || !toBoolean(exprs[2], qc);
    final long start = exprs.length < 4 ? 1 : toLong(exprs[3], qc);
    final long length = exprs.length < 5 ? Long.MAX_VALUE : toLong(exprs[4], qc);

    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());

    try(NewlineInput ni = new NewlineInput(new IOFile(path.toFile()))) {
      ni.encoding(encoding).validate(validate);

      // end exceeds maximum: use maximum (too large to be reached)
      final long end = start + length < 0 ? Long.MAX_VALUE : start + length;
      final TokenBuilder tb = new TokenBuilder();
      final TokenList tl = new TokenList();
      for(long c = 1; c < end; c++) {
        if(!ni.readLine(tb)) break;
        if(c >= start) tl.add(tb.toArray());
      }
      return StrSeq.get(tl);

    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }

  /**
   * Creates an updated version of {@link FileReadTextLines}.
   * @param start first item to return (starting from 0)
   * @param length number of items to return
   * @param cc compilation context
   * @return optimized expression, or calling function
   * @throws QueryException query exception
   */
  public Expr opt(final long start, final long length, final CompileContext cc)
      throws QueryException {

    // [CG] merge start/length values
    final int al = exprs.length;
    if(al > 3) return this;

    final ExprList list = new ExprList().add(exprs);
    if(al < 2) list.add(Str.get(Strings.UTF8));
    if(al < 3) list.add(Bln.FALSE);
    list.add(Int.get(start + 1)).add(Int.get(length));
    return cc.function(Function._FILE_READ_TEXT_LINES, info, list.finish());
  }
}
