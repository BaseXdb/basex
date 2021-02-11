package org.basex.query.func.ft;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FtMark extends StandardFunc {
  /** Marker element. */
  private static final byte[] MARK = Token.token("mark");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return mark(qc, false);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
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
      if(!XMLToken.isQName(m)) throw valueError(AtomType.QNAME, m, info);
    }
    if(exprs.length > 2) {
      l = (int) toLong(exprs[2], qc);
    }
    final byte[] mark = m;
    final int len = l;

    return new Iter() {
      final FTPosData ftd = new FTPosData();
      Iter iter;
      BasicIter<Item> ir;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ir != null) {
            final Item item = ir.next();
            if(item != null) return item;
            ir = null;
          }
          final FTPosData tmp = qc.ftPosData;
          try {
            qc.ftPosData = ftd;
            if(iter == null) iter = exprs[0].iter(qc);
            final Item item = iter.next();
            if(item == null) return null;

            // copy node to main memory data instance
            final MemData md = new MemData(qc.context.options);
            final DataBuilder db = new DataBuilder(md, qc);
            db.ftpos(mark, qc.ftPosData, len).build(toDBNode(item));

            final IntList il = new IntList();
            final int s = md.meta.size;
            for(int p = 0; p < s; p += md.size(p, md.kind(p))) il.add(p);
            ir = DBNodeSeq.get(il.finish(), md, FtMark.this).iter();
          } finally {
            qc.ftPosData = tmp;
          }
        }
      }
    };
  }
}
