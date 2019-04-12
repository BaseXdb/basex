package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Transform expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class TransformWith extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param source source expression
   * @param modify modify expression
   */
  public TransformWith(final InputInfo info, final Expr source, final Expr modify) {
    super(info, SeqType.NOD_ZM, source, modify);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cc.pushFocus(null);
    try {
      return super.compile(cc);
    } finally {
      cc.removeFocus();
    }
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    return adoptType(exprs[0]);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(exprs[0]);
    final Expr modify = exprs[1];
    modify.checkUp();
    if(!modify.isVacuous() && !modify.has(Flag.UPD)) throw UPMODIFY.get(info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Updates upd = qc.updates();
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;

    final ValueBuilder vb = new ValueBuilder(qc);
    try {
      final Iter iter = exprs[0].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        if(!(item instanceof ANode)) throw UPSOURCE_X.get(info, item);

        // create main memory copy of node
        item = ((ANode) item).copy(qc);
        // set resulting node as context
        qf.value = item;

        final Updates updates = new Updates(true);
        qc.updates = updates;
        updates.addData(item.data());

        if(!exprs[1].value(qc).isEmpty()) throw UPMODIFY.get(info);

        updates.prepare(qc);
        updates.apply(qc);
        vb.add(item);
      }
    } finally {
      qc.updates = upd;
      qf.value = cv;
    }
    return vb.value(this);
  }

  @Override
  public boolean has(final Flag... flags) {
    if(Flag.UPD.in(flags) && exprs[0].has(Flag.UPD)) return true;
    final Flag[] flgs = Flag.UPD.remove(flags);
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new TransformWith(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof TransformWith && super.equals(obj);
  }

  @Override
  public String toString() {
    return toString(' ' + QueryText.UPDATE + ' ');
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }
}
