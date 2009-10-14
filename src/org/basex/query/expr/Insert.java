package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.primitives.InsertAfterPrimitive;
import org.basex.query.up.primitives.InsertAttribute;
import org.basex.query.up.primitives.InsertBeforePrimitive;
import org.basex.query.up.primitives.InsertIntoFirstPrimitive;
import org.basex.query.up.primitives.InsertIntoLastPrimitive;
import org.basex.query.up.primitives.InsertIntoPrimitive;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;

/**
 * Insert expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Insert extends Arr {
  /** First flag. */
  final boolean first;
  /** Last flag. */
  final boolean last;
  /** After flag. */
  final boolean before;
  /** Before flag. */
  final boolean after;

  /**
   * Constructor.
   * @param src source expression
   * @param f first flag
   * @param l last
   * @param b before
   * @param a after
   * @param trg target expression
   */
  public Insert(final Expr src, final boolean f, final boolean l,
      final boolean b, final boolean a, final Expr trg) {
    super(trg, src);
    first = f;
    last = l;
    before = b;
    after = a;
  }
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // check source constraints
    final Iter s = SeqIter.get(expr[1].iter(ctx));
    final SeqIter seq = new SeqIter();
    final SeqIter aSeq = new SeqIter();
    Item i = s.next();
    // e needed to track order of attribute / non attribute nodes XUTY0004
    boolean e = false;
    while(i != null) {
      // [LK] check  use of txt node constructor
      if(i.type.num || i.type.str) seq.add(new FTxt(i.str(), null));
      else if(i instanceof Nod) {
        final Nod tn = (Nod) i;
        final int k = Nod.kind(tn.type);
        if(k == Data.ATTR) {
          if(e) Err.or(UPNOATTRPER, this);
          aSeq.add(tn);
        } else {
          e = true;
          if(Nod.kind(tn.type) == Data.DOC) seq.add(tn.child());
          else seq.add(tn);
        }
      } else Err.or(UPDATE, this);
      i = s.next();
    }
    seq.reset();
    
    final boolean into = !(before || after);
    // check target constraints
    final Iter t = SeqIter.get(expr[0].iter(ctx));
    i = t.next();
    if(i == null) Err.or(UPSEQEMP, this);
    if(!(i instanceof Nod) || t.size() > 1) Err.or(UPTRGTYP, this);
    final Nod n = (Nod) i;
    final int k = Nod.kind(n.type);
    if(into && (!(k == Data.ELEM || k == Data.DOC))) Err.or(UPTRGTYP, this);
    if(before || after) {
      if(k == Data.ATTR) Err.or(UPTRGTYP2, this);
      if(n.parent() == null) Err.or(UPPAREMPTY, this);
    }
    
    if(!(n instanceof DBNode)) {
      if(aSeq.size() > 0) ctx.updates.
        addPrimitive(new InsertAttribute(n, aSeq, -1));
      if(seq.size() > 0) ctx.updates.
      addPrimitive(new InsertAttribute(n, seq, -1));
      return Iter.EMPTY;
    }
    
    final DBNode dbn = (DBNode) n;
    final DBNode par = (DBNode) dbn.parent();
    // [LK] check par == null?
    if(aSeq.size() > 0) 
      if(into) ctx.updates.addPrimitive(
        new InsertAttribute(dbn, aSeq, -1));
      if(before || after) {
        if(par == null) Err.or(UPDATE, this);
        ctx.updates.addPrimitive(
            new InsertAttribute(par, aSeq, -1));
      }
    if(seq.size() > 0) {
      UpdatePrimitive up = null;
      if(into)
        if(first) up = new InsertIntoFirstPrimitive(dbn, seq, -1);
        else if(last) up = new InsertIntoLastPrimitive(dbn, seq, -1);
        else up = new InsertIntoPrimitive(dbn, seq, -1);
      // [LK] debug!
      else if(before) up = new InsertBeforePrimitive(dbn, seq, -1);
      else if(after) up = new InsertAfterPrimitive(dbn, seq, -1);
      else Err.or(UPDATE, this);
      ctx.updates.addPrimitive(up);
    }
    return Iter.EMPTY;
  }

  @Override
  public String toString() {
    return null;
  }
}