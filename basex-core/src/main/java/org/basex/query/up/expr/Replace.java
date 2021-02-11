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
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Replace expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final Constr c = new Constr(info, sc).add(qc, exprs[1]);
    if(c.errAtt != null) throw UPNOATTRPER_X.get(info, c.errAtt);
    if(c.duplAtt != null) throw UPATTDUPL_X.get(info, c.duplAtt);

    final Iter iter = exprs[0].iter(qc);
    final Item item = iter.next();
    // check target constraints
    if(item == null) throw UPSEQEMP_X.get(info, Util.className(this));
    final Type type = item.type;
    if(!(item instanceof ANode) || type == NodeType.DOCUMENT_NODE)
      throw UPTRGNODE_X.get(info, item);
    final Item item2 = iter.next();
    if(item2 != null) throw UPTRGSINGLE_X.get(info, ValueBuilder.concat(item, item2, qc));
    final ANode targ = (ANode) item;
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
      if(type == NodeType.COMMENT) FComm.parse(txt, info);
      if(type == NodeType.PROCESSING_INSTRUCTION) FPI.parse(txt, info);

      updates.add(new ReplaceValue(dbn.pre(), dbn.data(), info, txt), qc);
    } else {
      final ANode parent = targ.parent();
      if(parent == null) throw UPNOPAR_X.get(info, targ);
      if(type == NodeType.ATTRIBUTE) {
        // replace attribute node
        if(!list.isEmpty()) throw UPWRATTR_X.get(info, list.get(0));
        list = checkNS(aList, parent);
      } else {
        // replace non-attribute node
        if(!aList.isEmpty()) throw UPWRELM_X.get(info, targ);
      }
      // conforms to specification: insertion sequence may be empty
      updates.add(new ReplaceNode(dbn.pre(), dbn.data(), info, list), qc);
    }
    return Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Replace(sc, info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), value));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Replace && value == ((Replace) obj).value &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(REPLACE);
    if(value) qs.token(VALUEE).token(OF);
    qs.token(NODE).token(exprs[0]).token(WITH).token(exprs[1]);
  }
}
