package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;

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
 * Insert expression.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class Insert extends Update {
  /** Insertion type. */
  public enum Mode {
    /** Into.   */ INTO,
    /** First.  */ FIRST,
    /** Last.   */ LAST,
    /** Before. */ BEFORE,
    /** After.  */ AFTER
  }

  /** Insertion mode. */
  private final Mode mode;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param src source expression
   * @param mode insertion mode
   * @param trg target expression
   */
  public Insert(final InputInfo info, final Expr src, final Mode mode, final Expr trg) {
    super(info, trg, src);
    this.mode = mode;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = arg(0).iter(qc);
    final boolean sibling = mode == Mode.BEFORE || mode == Mode.AFTER;
    FBuilder builder = null;

    for(Item item; (item = iter.next()) != null;) {
      final Type type = item.type;
      if(!(type instanceof NodeType)) throw (sibling ? UPTRGTYP2_X : UPTRGTYP_X).get(info, item);

      final ANode node = (ANode) item, parent = node.parent();
      if(sibling) {
        if(type.oneOf(NodeType.ATTRIBUTE, NodeType.DOCUMENT_NODE))
          throw UPTRGTYP2_X.get(info, node);
        if(parent == null) throw UPPAREMPTY_X.get(info, node);
      } else if(!type.oneOf(NodeType.ELEMENT, NodeType.DOCUMENT_NODE)) {
        throw UPTRGTYP_X.get(info, node);
      }

      if(builder == null) builder = builder(arg(1), qc);
      final Updates updates = qc.updates();

      // no update primitive is created if node list is empty
      ANodeList list = builder.attributes;
      if(list != null) {
        final ANode target = sibling ? parent : node;
        if(target.type != NodeType.ELEMENT)
          throw (sibling ? UPATTELM_X : UPATTELM2_X).get(info, target);

        final DBNode dbnode = updates.determineDataRef(target, qc);
        checkPerm(qc, Perm.WRITE, dbnode.data().meta.name);
        final NodeUpdate up = new InsertAttribute(dbnode.pre(), dbnode.data(), info,
            checkNS(list, target));
        updates.add(up, qc);
      }

      // no update primitive is created if node list is empty
      list = builder.children;
      if(list != null) {
        final DBNode dbnode = updates.determineDataRef(node, qc);
        checkPerm(qc, Perm.WRITE, dbnode.data().meta.name);
        final NodeUpdate up = switch(mode) {
          case BEFORE -> new InsertBefore(dbnode.pre(), dbnode.data(), info, list);
          case AFTER -> new InsertAfter(dbnode.pre(), dbnode.data(), info, list);
          case FIRST -> new InsertIntoAsFirst(dbnode.pre(), dbnode.data(), info, list);
          case LAST -> new InsertIntoAsLast(dbnode.pre(), dbnode.data(), info, list);
          default -> new InsertInto(dbnode.pre(), dbnode.data(), info, list);
        };
        updates.add(up, qc);
      }
    }
    return Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Insert(info, arg(1).copy(cc, vm), mode, arg(0).copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Insert ins && mode == ins.mode && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(QueryText.INSERT).token(QueryText.NODES).token(arg(1)).token(QueryText.INTO).
      token(arg(0));
  }
}
