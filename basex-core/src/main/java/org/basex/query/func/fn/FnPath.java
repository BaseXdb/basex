package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnPath extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ANode node = toEmptyNode(ctxArg(0, qc), qc);
    if(node == null) return null;

    final TokenList tl = new TokenList();
    while(node.parent() != null) {
      int i = 1;
      final TokenBuilder tb = new TokenBuilder();
      if(node.type == NodeType.ATT) {
        tb.add('@').add(node.qname().id());
      } else if(node.type == NodeType.ELM) {
        final QNm qnm = node.qname();
        final BasicNodeIter iter = node.precedingSibling();
        for(ANode fs; (fs = iter.next()) != null;) {
          qc.checkStop();
          final QNm q = fs.qname();
          if(q != null && q.eq(qnm)) i++;
        }
        tb.add(qnm.eqName()).add('[').add(Integer.toString(i)).add(']');
      } else if(node.type == NodeType.COM || node.type == NodeType.TXT) {
        final BasicNodeIter iter = node.precedingSibling();
        for(ANode fs; (fs = iter.next()) != null;) {
          qc.checkStop();
          if(fs.type == node.type) i++;
        }
        tb.addExt(node.seqType() + "[%]", i);
      } else if(node.type == NodeType.PI) {
        final QNm qnm = node.qname();
        final BasicNodeIter iter = node.precedingSibling();
        for(ANode fs; (fs = iter.next()) != null;) {
          qc.checkStop();
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
    if(node.type != NodeType.DOC)
      tb.add(QNm.eqName(QueryText.FN_URI, Token.token("root()")));
    // add all steps in reverse order
    for(int i = tl.size() - 1; i >= 0; --i) tb.add('/').add(tl.get(i));
    return Str.get(tb.isEmpty() ? Token.SLASH : tb.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return exprs.length > 0 ? optFirst() : this;
  }
}
