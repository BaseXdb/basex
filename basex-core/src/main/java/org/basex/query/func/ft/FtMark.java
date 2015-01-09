package org.basex.query.func.ft;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class FtMark extends StandardFunc {
  /** Marker element. */
  private static final byte[] MARK = Token.token("mark");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return mark(qc, false);
  }

  /**
   * Performs the mark function.
   * @param qc query context
   * @param ex extract flag
   * @return iterator
   * @throws QueryException query exception
   */
  final Iter mark(final QueryContext qc, final boolean ex) throws QueryException {
    byte[] m = MARK;
    int l = ex ? 150 : Integer.MAX_VALUE;

    if(exprs.length > 1) {
      // name of the marker element; default is <mark/>
      m = toToken(exprs[1], qc);
      if(!XMLToken.isQName(m)) throw valueError(info, AtomType.QNM, m);
    }
    if(exprs.length > 2) {
      l = (int) toLong(exprs[2], qc);
    }
    final byte[] mark = m;
    final int len = l;

    return new Iter() {
      final FTPosData ftd = new FTPosData();
      Iter ir;
      ValueIter vi;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(vi != null) {
            final Item it = vi.next();
            if(it != null) return it;
            vi = null;
          }
          final FTPosData tmp = qc.ftPosData;
          try {
            qc.ftPosData = ftd;
            if(ir == null) ir = qc.iter(exprs[0]);
            final Item it = ir.next();
            if(it == null) return null;

            // copy node to main memory data instance
            final MemData md = new MemData(qc.context.options);
            final DataBuilder db = new DataBuilder(md);
            db.ftpos(mark, qc.ftPosData, len).build(toDBNode(it));

            final IntList il = new IntList();
            final int s = md.meta.size;
            for(int p = 0; p < s; p += md.size(p, md.kind(p))) il.add(p);
            vi = DBNodeSeq.get(il, md, false, false).iter();
          } finally {
            qc.ftPosData = tmp;
          }
        }
      }
    };
  }
}
