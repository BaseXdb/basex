package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

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
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class Rename extends Update {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param trg target expression
   * @param name new name expression
   */
  public Rename(final InputInfo info, final Expr trg, final Expr name) {
    super(info, trg, name);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = arg(0).unwrappedIter(qc);
    final Item name = arg(1).atomItem(qc, info);
    final EnumMap<Kind, QNm> names = new EnumMap<>(Kind.class);

    for(Item item; (item = qc.next(iter)) != null;) {
      final Kind kind = item.type.kind();
      final boolean element = kind == Kind.ELEMENT, attribute = kind == Kind.ATTRIBUTE;
      final boolean pi = kind == Kind.PROCESSING_INSTRUCTION;
      if(!(element || attribute || pi)) throw UPWRTRGTYP_X.get(info, item);

      QNm newName = names.get(kind);
      if(newName == null) {
        final CNode cname = element ? new CElem(info, false, name, new Atts(), new Atts()) :
          attribute ? new CAttr(info, true, name, Empty.VALUE) :
          new CPI(info, false, name, Empty.VALUE);
        newName = ((XNode) cname.item(qc, info)).qname();
        names.put(kind, newName);
      }

      // check for namespace conflicts
      final XNode target = (XNode) item;
      QNm qname = newName;
      if(attribute) {
        final XNode parent = target.parent();
        if(parent != null && newName.hasPrefix()) {
          qname = checkNS(newName, parent.nsScope(null), qc);
        }
      } else if(element) {
        // undeclarations of the default namespace (empty URI) are no bindings
        final byte[] newPrefix = newName.prefix(), uri = target.nsScope(null).value(newPrefix);
        if(uri != null && uri.length != 0 && !eq(uri, newName.uri()))
          throw UPNSCONFL_X_X.get(info, newName, new QNm(newPrefix, uri));
      }

      final Updates updates = qc.updates();
      final DBNode dbnode = updates.determineDataRef(target, qc);
      checkWrite(dbnode, qc);
      updates.add(new RenameNode(dbnode.pre(), dbnode.data(), info, qname), qc);
    }
    return Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Rename(info, arg(0).copy(cc, vm), arg(1).copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Rename && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.open().token(RENAME).token(NODE).token(arg(0)).token(AS).token(arg(1)).close();
  }
}
