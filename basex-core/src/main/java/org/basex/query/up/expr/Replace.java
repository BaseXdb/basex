package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.util.list.*;
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
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class Replace extends Update {
  /** 'Value of' flag. */
  private final boolean value;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param trg target expression
   * @param src source expression
   * @param value replace value of
   */
  public Replace(final InputInfo info, final Expr trg, final Expr src, final boolean value) {
    super(info, trg, src);
    this.value = value;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = arg(0).iter(qc);
    FBuilder builder = null;

    for(Item item; (item = iter.next()) != null;) {
      final Type type = item.type;
      if(!(type instanceof NodeType) || type == NodeType.DOCUMENT_NODE)
        throw UPTRGNODE_X.get(info, item);

      final XNode targ = (XNode) item;
      final Updates updates = qc.updates();
      final DBNode dbnode = updates.determineDataRef(targ, qc);
      checkPerm(qc, Perm.WRITE, dbnode.data().meta.name);

      // replace node
      if(builder == null) builder = builder(arg(1), qc);
      if(value) {
        // replace value of node
        byte[] text = Token.EMPTY;
        if(builder.children != null) text = builder.children.get(0).string();
        else if(builder.attributes != null) text = builder.attributes.get(0).string();

        // check validity of future comments or PIs
        if(type == NodeType.COMMENT) FComm.parse(text, info);
        if(type == NodeType.PROCESSING_INSTRUCTION) FPI.parse(text, info);

        updates.add(new ReplaceValue(dbnode.pre(), dbnode.data(), info, text), qc);
      } else {
        final XNode parent = targ.parent();
        if(parent == null) throw UPNOPAR_X.get(info, targ);

        final ANodeList list;
        if(type == NodeType.ATTRIBUTE) {
          // replace attribute node
          if(builder.children != null) throw UPWRATTR_X.get(info, builder.children.get(0));
          list = builder.attributes != null ? checkNS(builder.attributes, parent) : new ANodeList();
        } else {
          // replace non-attribute node
          if(builder.attributes != null) throw UPWRELM_X.get(info, targ);
          list = builder.children != null ? builder.children : new ANodeList();
        }
        // conforms to specification: insertion sequence may be empty
        updates.add(new ReplaceNode(dbnode.pre(), dbnode.data(), info, list), qc);
      }
    }
    return Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Replace(info, arg(0).copy(cc, vm), arg(1).copy(cc, vm), value));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Replace rplc && value == rplc.value &&
        super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(REPLACE);
    if(value) qs.token(VALUEE).token(OF);
    qs.token(NODE).token(arg(0)).token(WITH).token(arg(1));
  }
}
