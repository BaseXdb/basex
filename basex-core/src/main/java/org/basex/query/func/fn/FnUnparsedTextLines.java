package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author Christian Gruen
 * @author BaseX Team 2005-18, BSD License
 */
public final class FnUnparsedTextLines extends Parse {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item item = unparsedText(qc, false, true);
    if(item == null) return Empty.SEQ;

    try(NewlineInput ni = new NewlineInput(item.string(info))) {
      final TokenList tl = new TokenList();
      final TokenBuilder tb = new TokenBuilder();
      while(ni.readLine(tb)) tl.add(tb.toArray());
      return StrSeq.get(tl);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Item item = unparsedText(qc, false, true);
    return item == null ? Empty.ITER : new LinesIter(item.string(info));
  }

  /**
   * Line iterator.
   * @author Christian Gruen
   * @author BaseX Team 2005-18, BSD License
   */
  private static final class LinesIter extends Iter {
    /** Token builder. */
    private final TokenBuilder tb = new TokenBuilder();
    /** Input stream. */
    private final NewlineInput nli;

    /**
     * Constructor.
     * @param contents file contents
     */
    private LinesIter(final byte[] contents) {
      try {
        nli = new NewlineInput(contents);
      } catch(final IOException ex) {
        // input has already been converted to the correct encoding
        throw Util.notExpected(ex);
      }
    }

    @Override
    public Str next() {
      try {
        return nli.readLine(tb) ? Str.get(tb.toArray()) : null;
      } catch(final IOException ex) {
        // input has already been converted to the correct encoding
        throw Util.notExpected(ex);
      }
    }
  }
}
