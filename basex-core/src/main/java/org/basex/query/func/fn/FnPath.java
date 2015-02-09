package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnPath extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ANode node = toEmptyNode(arg(0, qc), qc);
    if(node == null) return null;

    final TokenList tl = new TokenList();
    while(node.parent() != null) {
      int i = 1;
      final TokenBuilder tb = new TokenBuilder();
      if(node.type == NodeType.ATT) {
        tb.add('@');
        final QNm qnm = node.qname();
        final byte[] uri = qnm.uri();
        if(uri.length != 0) tb.add("Q{").add(qnm.uri()).add('}');
        tb.add(qnm.local());
      } else if(node.type == NodeType.ELM) {
        final QNm qnm = node.qname();
        final AxisIter ai = node.precedingSibling();
        for(ANode fs; (fs = ai.next()) != null;) {
          final QNm q = fs.qname();
          if(q != null && q.eq(qnm)) i++;
        }
        tb.add("Q{").add(qnm.uri()).add('}').add(qnm.local());
        tb.add('[').add(Integer.toString(i)).add(']');
      } else if(node.type == NodeType.COM || node.type == NodeType.TXT) {
        final AxisIter ai = node.precedingSibling();
        for(ANode fs; (fs = ai.next()) != null;) if(fs.type == node.type) i++;
        tb.addExt(node.seqType() + "[%]", i);
      } else if(node.type == NodeType.PI) {
        final QNm qnm = node.qname();
        final AxisIter ai = node.precedingSibling();
        for(ANode fs; (fs = ai.next()) != null;) {
          if(fs.type == node.type && fs.qname().eq(qnm)) i++;
        }
        tb.add(node.type.string()).add('(').add(qnm.local());
        tb.add(")[").add(Integer.toString(i)).add(']');
      }
      tl.add(tb.finish());
      node = node.parent();
    }

    final TokenBuilder tb = new TokenBuilder();
    // add root function
    if(node.type != NodeType.DOC) tb.add("Q{").add(QueryText.FN_URI).add("}root()");
    // add all steps in reverse order
    for(int i = tl.size() - 1; i >= 0; --i) tb.add('/').add(tl.get(i));
    return Str.get(tb.isEmpty() ? Token.SLASH : tb.finish());
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(DBLocking.CTX)) && super.accept(visitor);
  }
}
