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
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Rename expression.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Lukas Kircher
 */
public final class Rename extends Update {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param trg target expression
   * @param name new name expression
   */
  public Rename(final StaticContext sc, final InputInfo info, final Expr trg, final Expr name) {
    super(sc, info, trg, name);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final Item item = iter.next();

    // check target constraints
    if(item == null) throw UPSEQEMP_X.get(info, Util.className(this));
    final Item item2 = iter.next();
    if(item2 != null) throw UPWRTRGSINGLE_X.get(info, ValueBuilder.concat(item, item2, qc));

    final CNode ex;
    final Type type = item.type;
    if(type == NodeType.ELEMENT) {
      ex = new CElem(sc, info, false, exprs[1], new Atts());
    } else if(type == NodeType.ATTRIBUTE) {
      ex = new CAttr(sc, info, false, exprs[1], Empty.VALUE);
    } else if(type == NodeType.PROCESSING_INSTRUCTION) {
      ex = new CPI(sc, info, false, exprs[1], Empty.VALUE);
    } else {
      throw UPWRTRGTYP_X.get(info, item);
    }

    final QNm newName = ((ANode) ex.item(qc, info)).qname();
    final ANode target = (ANode) item;

    // check namespace conflicts...
    final Type tt = target.type;
    final boolean elem = tt == NodeType.ELEMENT, attr = tt == NodeType.ATTRIBUTE;
    if(elem || attr) {
      final byte[] newPrefix = newName.prefix(), newUri = newName.uri();
      if(elem || newPrefix.length > 0) {
        final Atts nspaces = target.nsScope(sc);
        final int ns = nspaces.size();
        for(int n = 0; n < ns; n++) {
          final byte[] prefix = nspaces.name(n), uri = nspaces.value(n);
          if(eq(prefix, newPrefix) && !eq(uri, newUri))
            throw UPNSCONFL_X_X.get(info, newName, new QNm(prefix, uri));
        }
      }
    }

    final Updates updates = qc.updates();
    final DBNode dbn = updates.determineDataRef(target, qc);
    updates.add(new RenameNode(dbn.pre(), dbn.data(), info, newName), qc);
    return Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Rename(sc, info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Rename && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(RENAME).token(NODE).token(exprs[0]).token(AS).token(exprs[1]);
  }
}
