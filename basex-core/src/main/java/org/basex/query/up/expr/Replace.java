package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Replace expression.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Lukas Kircher
 */
public final class Replace extends Update {
  /** 'Value of' flag. */
  private final boolean value;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param trg target expression
   * @param src source expression
   * @param value replace value of
   */
  public Replace(final StaticContext sc, final InputInfo info, final Expr trg, final Expr src,
      final boolean value) {
    super(sc, info, trg, src);
    this.value = value;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Constr c = new Constr(ii, sc).add(qc, exprs[1]);
    if(c.errAtt != null) throw UPNOATTRPER_X.get(info, c.errAtt);
    if(c.duplAtt != null) throw UPATTDUPL_X.get(info, c.duplAtt);

    final Iter t = qc.iter(exprs[0]);
    final Item i = t.next();
    // check target constraints
    if(i == null) throw UPSEQEMP_X.get(info, Util.className(this));
    final Type tp = i.type;
    if(!(i instanceof ANode) || tp == NodeType.DOC) throw UPTRGNODE_X.get(info, i);
    final Item i2 = t.next();
    if(i2 != null) throw UPTRGSINGLE_X.get(info, ValueBuilder.concat(i, i2));
    final ANode targ = (ANode) i;
    final Updates updates = qc.updates();
    final DBNode dbn = updates.determineDataRef(targ, qc);

    // replace node
    final ANodeList aList = c.atts;
    ANodeList list = c.children;
    if(value) {
      // replace value of node
      final byte[] txt = list.size() < 1 ? aList.size() < 1 ? EMPTY :
        aList.get(0).string() : list.get(0).string();
      // check validity of future comments or PIs
      if(tp == NodeType.COM) FComm.parse(txt, info);
      if(tp == NodeType.PI) FPI.parse(txt, info);

      updates.add(new ReplaceValue(dbn.pre(), dbn.data(), info, txt), qc);
    } else {
      final ANode par = targ.parent();
      if(par == null) throw UPNOPAR_X.get(info, targ);
      if(tp == NodeType.ATT) {
        // replace attribute node
        if(!list.isEmpty()) throw UPWRATTR_X.get(info, list.get(0));
        list = checkNS(aList, par);
      } else {
        // replace non-attribute node
        if(!aList.isEmpty()) throw UPWRELM_X.get(info, targ);
      }
      // conforms to specification: insertion sequence may be empty
      updates.add(new ReplaceNode(dbn.pre(), dbn.data(), info, list), qc);
    }
    return null;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Replace(sc, info, exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs), value);
  }

  @Override
  public String toString() {
    return REPLACE + (value ? ' ' + VALUEE + ' ' + OF : "") +
      ' ' + NODE + ' ' + exprs[0] + ' ' + WITH + ' ' + exprs[1];
  }
}
