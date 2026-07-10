package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileDescendants extends FileList {
  /** Descendants options. */
  public static class DescendantsOptions extends Options {
    /** Option: filter. */
    public static final ValueOption FILTER = new ValueOption("filter", Types.FUNCTION_O);
    /** Option: recurse. */
    public static final ValueOption RECURSE = new ValueOption("recurse", Types.FUNCTION_O);
    /** Option: maximum recursion depth. */
    public static final ValueOption DEPTH = new ValueOption("depth", Types.INTEGER_ZO);
  }

  @Override
  public Value eval(final QueryContext qc) throws QueryException, IOException {
    final Path dir = toPath(arg(0), qc);
    final DescendantsOptions options = toOptions(arg(1), new DescendantsOptions(), qc);

    final Value filterValue = options.get(DescendantsOptions.FILTER);
    final Value recurseValue = options.get(DescendantsOptions.RECURSE);
    final Value depthValue = options.get(DescendantsOptions.DEPTH);
    final FItem filter = filterValue.isEmpty() ? constantFn(true) :
        toFunction(filterValue, 1, qc);
    final FItem recurse = recurseValue.isEmpty() ? constantFn(true) :
        toFunction(recurseValue, 1, qc);
    final int depth = depthValue.isEmpty() ? Integer.MAX_VALUE :
        (int) Math.min(toLong(depthValue.itemAt(0)), Integer.MAX_VALUE);

    final TokenList list = new TokenList();
    list(dir, recurse, new HofArgs(1), null, -1, filter, new HofArgs(1), list,
        depth, true, qc);
    return StrSeq.get(list);
  }

  @Override
  public int hofOffsets() {
    return functionOption(1) ? Integer.MAX_VALUE : 0;
  }
}
