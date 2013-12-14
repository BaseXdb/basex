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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNNode extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNNode(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    // functions have 0 or 1 arguments...
    final Item it = (expr.length == 0 ? checkCtx(ctx) : expr[0]).item(ctx, info);
    final ANode node = it == null ? null : checkNode(it);

    switch(sig) {
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
          if(!bu.isValid()) throw FUNCAST.get(ii, bu.type, bu);
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
        return super.item(ctx, ii);
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
        tb.addExt(n.type() + "[%]", i);
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
    return flag == Flag.X30 && expr.length == 0 &&
        oneOf(sig, DOCUMENT_URI, NODE_NAME, NILLED) ||
        flag == Flag.CTX && expr.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (expr.length != 0 || visitor.lock(DBLocking.CTX)) && super.accept(visitor);
  }
}
