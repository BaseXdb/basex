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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNNode extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNNode(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    // functions have 0 or 1 arguments...
    final Item it = (expr.length != 0 ? expr[0] : checkCtx(ctx)).item(ctx, info);

    switch(sig) {
      case NODE_NAME:
        QNm qname = it != null ? checkNode(it).qname() : null;
        return qname != null && qname.string().length != 0 ? qname : null;
      case DOCUMENT_URI:
        if(it == null) return null;
        final ANode node = checkNode(it);
        if(node.type != NodeType.DOC) return null;
        final byte[] uri = node.baseURI();
        return uri.length == 0 ? null : Uri.uri(uri, false);
      case NILLED:
        // always false, as no schema information is given
        return it == null || checkNode(it).type != NodeType.ELM ? null : Bln.FALSE;
      case BASE_URI:
        if(it == null) return null;
        ANode n = checkNode(it);
        if(n.type != NodeType.ELM && n.type != NodeType.DOC && n.parent() == null)
          return null;

        Uri base = Uri.EMPTY;
        do {
          if(n == null) return ctx.sc.baseURI().resolve(base, info);
          final Uri bu = Uri.uri(n.baseURI(), false);
          if(!bu.isValid()) FUNCAST.thrw(ii, bu.type, bu);
          base = bu.resolve(base, info);
          if(n.type == NodeType.DOC && n instanceof DBNode) break;
          n = n.parent();
        } while(!base.isAbsolute());
        return base;
      case NAME:
        qname = it != null ? checkNode(it).qname() : null;
        return qname != null ? Str.get(qname.string()) : Str.ZERO;
      case LOCAL_NAME:
        qname = it != null ? checkNode(it).qname() : null;
        return qname != null ? Str.get(qname.local()) : Str.ZERO;
      case NAMESPACE_URI:
        qname = it != null ? checkNode(it).qname() : null;
        return qname != null ? Uri.uri(qname.uri(), false) : Uri.EMPTY;
      case ROOT:
        if(it == null) return null;
        n = checkNode(it);
        while(n.parent() != null) n = n.parent();
        return n;
      case GENERATE_ID:
        return it == null ? Str.ZERO : Str.get(new TokenBuilder(
            QueryText.ID).addInt(checkNode(it).id).finish());
      case HAS_CHILDREN:
        return Bln.get(it != null && checkNode(it).hasChildren());
      case PATH:
        return it != null ? path(it) : null;
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Performs the path function.
   * @param it item to be resolved
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Str path(final Item it) throws QueryException {
    final TokenList tl = new TokenList();
    ANode n = checkNode(it);
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
  public boolean xquery3() {
    return oneOf(sig, GENERATE_ID, PATH, HAS_CHILDREN) ||
        expr.length == 0 && oneOf(sig, DOCUMENT_URI, NODE_NAME, NILLED);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 && xquery3() || u == Use.CTX && expr.length == 0 || super.uses(u);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (expr.length != 0 || visitor.lock(DBLocking.CTX)) && super.accept(visitor);
  }
}
