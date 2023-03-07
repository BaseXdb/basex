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
 * @author BaseX Team 2005-23, BSD License
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
   * @param extract extract flag
   * @return iterator
   * @throws QueryException query exception
   */
  final Iter mark(final QueryContext qc, final boolean extract) throws QueryException {
    final Item name = defined(1) ? exprs[1].atomItem(qc, info) : Empty.VALUE;
    final Item length = defined(2) ? exprs[2].atomItem(qc, info) : Empty.VALUE;

    final byte[] m = name.isEmpty() ? MARK : toToken(name);
    if(!XMLToken.isQName(m)) throw valueError(AtomType.QNAME, m, info);
    final int l = length.isEmpty() ? extract ? 150 : Integer.MAX_VALUE :
      (int) Math.min(Integer.MAX_VALUE, toLong(length));

    return new Iter() {
      final FTPosData ftd = new FTPosData();
      BasicIter<Item> iter;
      Iter nodes;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(iter != null) {
            final Item item = iter.next();
            if(item != null) return item;
            iter = null;
          }
          final FTPosData tmp = qc.ftPosData;
          try {
            qc.ftPosData = ftd;
            if(nodes == null) nodes = exprs[0].iter(qc);
            final Item item = nodes.next();
            if(item == null) return null;

            // copy node to main memory data instance
            final MemData md = new MemData(qc.context.options);
            final DataBuilder db = new DataBuilder(md, qc);
            db.ftpos(m, qc.ftPosData, l).build(toDBNode(item, true));

            final IntList il = new IntList();
            final int s = md.meta.size;
            for(int p = 0; p < s; p += md.size(p, md.kind(p))) il.add(p);
            iter = DBNodeSeq.get(il.finish(), md, FtMark.this).iter();
          } finally {
            qc.ftPosData = tmp;
          }
        }
      }
    };
  }
}
