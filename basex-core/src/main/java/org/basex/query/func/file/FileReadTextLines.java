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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileReadTextLines extends FileReadFn {
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
  public Value eval(final QueryContext qc) throws IOException, QueryException {
    final TokenBuilder tb = new TokenBuilder();
    final TokenList tl = new TokenList();
    final long[] minMax = minMax(qc);
    try(NewlineInput ni = input(qc)) {
      for(long c = 1; c < minMax[1] && ni.readLine(tb); c++) {
        qc.checkStop();
        if(c >= minMax[0]) tl.add(tb.toArray());
      }
      return StrSeq.get(tl);
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
    final ParseOptions options = options(path, qc);
    final String encoding = options.get(ParseOptions.ENCODING);
    final boolean fallback = options.get(ParseOptions.FALLBACK);
    return new NewlineInput(new IOFile(path), encoding).fallback(fallback);
  }

  /**
   * Returns the offset to the first and last line to be read.
   * @param qc query context
   * @return offsets
   * @throws QueryException query exception
   */
  private long[] minMax(final QueryContext qc) throws QueryException {
   final Long offset = toLongOrNull(arg(3), qc);
    final Long length = toLongOrNull(arg(4), qc);

    final long off = offset != null ? offset : 1;
    return new long[] { off, add(off, length != null ? length : Long.MAX_VALUE) };
  }

  /**
   * Adds two values, saturating at the integer limits.
   * @param value1 first value
   * @param value2 second value
   * @return sum
   */
  private static long add(final long value1, final long value2) {
    final long sum = value1 + value2;
    // overflow: sum has a different sign than both operands
    return ((value1 ^ sum) & (value2 ^ sum)) < 0 ?
      value1 < 0 ? Long.MIN_VALUE : Long.MAX_VALUE : sum;
  }

  /**
   * Merges new bounds into a {@link FileReadTextLines} call.
   * @param func original function (argument is an instance of the function of this class)
   * @param start first item to return (starting from 0)
   * @param length number of items to return
   * @param cc compilation context
   * @return optimized function instance; original function otherwise
   * @throws QueryException query exception
   */
  public static Expr merge(final StandardFunc func, final long start, final long length,
      final CompileContext cc) throws QueryException {

    final Expr[] args = func.arg(0).args();
    final int al = args.length;

    final Expr options = al > 1 ? args[1] : Empty.VALUE;
    final Expr fallback = al > 2 ? args[2] : Empty.VALUE;

    // skip optimization if existing function cannot be merged with new bounds
    if(!(options instanceof Value) || !(fallback instanceof Value) ||
       al > 3 && !(args[3] instanceof Itr) ||
       al > 4 && !(args[4] instanceof Itr)) return func;

    // old bounds: first line, exclusive end line
    final long off = al > 3 ? ((Itr) args[3]).itr() : 1;
    final long end = add(off, al > 4 ? ((Itr) args[4]).itr() : Long.MAX_VALUE);

    // merge with new bounds: increase first line, decrease end line
    final long first = add(Math.max(off, 1), start);
    final long last = Math.min(end, add(first, length));

    // create new function instance
    final Expr[] newArgs = { args[0], options, fallback, Itr.get(first),
      Itr.get(last > first ? last - first : 0) };
    return cc.function(Function._FILE_READ_TEXT_LINES, func.info(), newArgs);
  }
}
