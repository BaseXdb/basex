package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Node functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNNode extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNNode(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // functions have 0 or 1 arguments...
    final Item it = (exprs.length == 0 ? checkCtx(qc) : exprs[0]).item(qc, info);
    final ANode node = it == null ? null : checkNode(it);

    switch(func) {
      case NODE_NAME:
        QNm qname = node != null ? node.qname() : null;
        return qname != null && qname.string().length != 0 ? qname : null;
      case DOCUMENT_URI:
        if(node == null || node.type != NodeType.DOC) return null;
        final byte[] uri = node.baseURI();
        return uri.length == 0 ? null : Uri.uri(uri, false);
      case NILLED:
        // always false, as no schema information is given
        return node == null || node.type != NodeType.ELM ? null : Bln.FALSE;
      case BASE_URI:
        if(node == null) return null;
        if(node.type != NodeType.ELM && node.type != NodeType.DOC &&
            node.parent() == null) return null;

        Uri base = Uri.EMPTY;
        ANode n = node;
        do {
          if(n == null) return sc.baseURI().resolve(base, info);
          final Uri bu = Uri.uri(n.baseURI(), false);
          if(!bu.isValid()) throw INVURI.get(ii, n.baseURI());
          base = bu.resolve(base, info);
          if(n.type == NodeType.DOC && n instanceof DBNode) break;
          n = n.parent();
        } while(!base.isAbsolute());
        return base;
      case NAME:
        qname = node != null ? node.qname() : null;
        return qname != null ? Str.get(qname.string()) : Str.ZERO;
      case LOCAL_NAME:
        qname = node != null ? node.qname() : null;
        return qname != null ? Str.get(qname.local()) : Str.ZERO;
      case NAMESPACE_URI:
        qname = node != null ? node.qname() : null;
        return qname != null ? Uri.uri(qname.uri(), false) : Uri.EMPTY;
      case ROOT:
        n = node;
        while(n != null) {
          final ANode p = n.parent();
          if(p == null) break;
          n = p;
        }
        return n;
      case GENERATE_ID:
        return node == null ? Str.ZERO :
          Str.get(new TokenBuilder(QueryText.ID).addInt(node.id).finish());
      case HAS_CHILDREN:
        return Bln.get(node != null && node.hasChildren());
      case PATH:
        return node != null ? path(node) : null;
      default:
        return super.item(qc, ii);
    }
  }

  /**
   * Performs the path function.
   * @param node node to start from
   * @return resulting iterator
   */
  private static Str path(final ANode node) {
    ANode n = node;
    final TokenList tl = new TokenList();
    while(n.parent() != null) {
      int i = 1;
      final TokenBuilder tb = new TokenBuilder();
      if(n.type == NodeType.ATT) {
        tb.add('@');
        final QNm qnm = n.qname();
        final byte[] uri = qnm.uri();
        if(uri.length != 0) tb.add("Q{").add(qnm.uri()).add('}');
        tb.add(qnm.local());
      } else if(n.type == NodeType.ELM) {
        final QNm qnm = n.qname();
        final AxisIter ai = n.precedingSibling();
        for(ANode fs; (fs = ai.next()) != null;) {
          final QNm q = fs.qname();
          if(q != null && q.eq(qnm)) i++;
        }
        tb.add("Q{").add(qnm.uri()).add('}').add(qnm.local());
        tb.add('[').add(Integer.toString(i)).add(']');
      } else if(n.type == NodeType.COM || n.type == NodeType.TXT) {
        final AxisIter ai = n.precedingSibling();
        for(ANode fs; (fs = ai.next()) != null;) if(fs.type == n.type) i++;
        tb.addExt(n.seqType() + "[%]", i);
      } else if(n.type == NodeType.PI) {
        final QNm qnm = n.qname();
        final AxisIter ai = n.precedingSibling();
        for(ANode fs; (fs = ai.next()) != null;) {
          if(fs.type == n.type && fs.qname().eq(qnm)) i++;
        }
        tb.add(n.type.string()).add('(').add(qnm.local());
        tb.add(")[").add(Integer.toString(i)).add(']');
      }
      tl.add(tb.finish());
      n = n.parent();
    }

    final TokenBuilder tb = new TokenBuilder();
    // add root function
    if(n.type != NodeType.DOC) tb.add("Q{").add(QueryText.FNURI).add("}root()");
    // add all steps in reverse order
    for(int i = tl.size() - 1; i >= 0; --i) tb.add('/').add(tl.get(i));
    return Str.get(tb.isEmpty() ? Token.SLASH : tb.finish());
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.X30 && exprs.length == 0 &&
        oneOf(func, DOCUMENT_URI, NODE_NAME, NILLED) ||
        flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(DBLocking.CTX)) && super.accept(visitor);
  }
}
