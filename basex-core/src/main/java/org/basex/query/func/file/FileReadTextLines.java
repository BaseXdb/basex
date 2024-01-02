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
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FileReadTextLines extends FileRead {
  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      final TokenBuilder tb = new TokenBuilder();
      NewlineInput ni;
      long[] minMax;
      long c;

      @Override
      public Str next() throws QueryException {
        try {
          if(ni == null) {
            minMax = minMax(qc);
            ni = input(qc);
            qc.resources.add(ni);
          }
          while(++c < minMax[1] && ni.readLine(tb)) {
            if(c >= minMax[0]) return Str.get(tb.toArray());
          }
          qc.resources.remove(ni);
          return null;
        } catch(final IOException ex) {
          throw FILE_IO_ERROR_X.get(info, ex);
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    final TokenList tl = new TokenList();

    final long[] minMax = minMax(qc);
    try(NewlineInput ni = input(qc)) {
      for(long c = 1; c < minMax[1] && ni.readLine(tb); c++) {
        qc.checkStop();
        if(c >= minMax[0]) tl.add(tb.toArray());
      }
      return StrSeq.get(tl);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }

  /**
   * Returns an input stream to the addressed file.
   * @param qc query context
   * @return input stream
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private NewlineInput input(final QueryContext qc) throws IOException, QueryException {
    final Path path = toPath(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), FILE_UNKNOWN_ENCODING_X, qc);
    final boolean fallback = toBooleanOrFalse(arg(2), qc);

    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());

    final NewlineInput ni = new NewlineInput(new IOFile(path.toFile()));
    ni.encoding(encoding).validate(!fallback);
    return ni;
  }

  /**
   * Returns the offset to the first and last line to be read.
   * @param qc query context
   * @return offsets
   * @throws QueryException query exception
   */
  private long[] minMax(final QueryContext qc) throws QueryException {
   final Item offset = arg(3).atomItem(qc, info);
    final Item length = arg(4).atomItem(qc, info);

    final long off = offset.isEmpty() ? 1 : toLong(offset);
    final long len = length.isEmpty() ? Long.MAX_VALUE : toLong(length);
    final long end = off + len < 0 ? Long.MAX_VALUE : off + len;
    return new long[] { off, end };
  }

  /**
   * Creates an optimized version of a {@link FileReadTextLines} call.
   * @param func original function (argument is an instance of the function of this class)
   * @param start first item to return (starting from 0)
   * @param length number of items to return
   * @param cc compilation context
   * @return optimized function instance; original function otherwise
   * @throws QueryException query exception
   */
  public static Expr opt(final StandardFunc func, final long start, final long length,
      final CompileContext cc) throws QueryException {

    final Expr[] args = func.arg(0).args();
    final int al = args.length;

    final Str encoding = Str.get(Strings.UTF8);
    final Bln validate = Bln.FALSE;

    // skip optimization if existing function cannot be merged with new bounds
    if(al > 1 && !args[1].equals(encoding) ||
       al > 2 && !args[2].equals(validate) ||
       al > 3 && !(args[3] instanceof Int) ||
       al > 4 && !(args[4] instanceof Int)) return func;

    // old bounds
    long s = al > 3 ? ((Int) args[3]).itr() - 1 : 0;
    long l = al > 4 ? ((Int) args[4]).itr() : Long.MAX_VALUE;

    // merge with new bounds: increase start offset, decrease number of lines to retrieve
    s += start;
    if(l < Long.MAX_VALUE) l -= start;
    if(length < l) l = length;

    // create new function instance
    final Expr[] newArgs = { args[0], encoding, validate, Int.get(s + 1), Int.get(l) };
    return cc.function(Function._FILE_READ_TEXT_LINES, func.info(), newArgs);
  }
}
