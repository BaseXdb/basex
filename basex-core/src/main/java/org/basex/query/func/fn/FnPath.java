package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnPath extends ContextFn {
  /** Path options. */
  public static class PathOptions extends Options {
    /** Option. */
    public static final ValueOption NAMESPACES = new ValueOption("namespaces", SeqType.MAP_O, null);
    /** Option. */
    public static final BooleanOption INDEXES = new BooleanOption("indexes", true);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ANode node = toNodeOrNull(context(qc), qc);
    final PathOptions options = toOptions(arg(1), new PathOptions(), qc);
    if(node == null) return Empty.VALUE;

    final TokenBuilder tb = new TokenBuilder();
    final TokenList steps = new TokenList();
    final boolean indexes = options.get(PathOptions.INDEXES);
    final Value ns = options.get(PathOptions.NAMESPACES);
    final XQMap namespaces = ns == null ? XQMap.empty() : toMap(ns, qc);

    while(true) {
      // root node: finalize traversal
      final ANode parent = node.parent();
      final NodeType type = (NodeType) node.type;
      if(parent == null) {
        if(type != NodeType.DOCUMENT_NODE)
          tb.add(name(new QNm("root", QueryText.FN_URI), false, namespaces, qc)).add("()");
        break;
      }
      // step: name/type
      final QNm qname = node.qname();
      if(type == NodeType.ATTRIBUTE) {
        tb.add('@').add(name(qname, true, namespaces, qc));
      } else if(type == NodeType.ELEMENT) {
        tb.add(name(qname, false, namespaces, qc));
      } else if(type == NodeType.PROCESSING_INSTRUCTION) {
        tb.add(type.toString(Token.string(qname.local())));
      } else if(type.oneOf(NodeType.COMMENT, NodeType.TEXT)) {
        tb.add(type.toString());
      }
      // optional index
      if(indexes && type != NodeType.ATTRIBUTE) {
        int p = 1;
        for(final ANode nd : node.precedingSiblingIter(false)) {
          qc.checkStop();
          if(nd.type == type && (type.oneOf(NodeType.COMMENT, NodeType.TEXT) ||
              nd.qname().eq(qname))) p++;
        }
        tb.add('[').addInt(p).add(']');
      }
      steps.add(tb.next());
      node = parent;
    }

    // add all steps in reverse order; cache element paths
    for(int s = steps.size() - 1; s >= 0; --s) tb.add('/').add(steps.get(s));
    return Str.get(tb.isEmpty() ? Token.cpToken('/') : tb.finish());
  }

  /**
   * Returns a name string for the specified QName.
   * @param qnm QName
   * @param attr attribute flag
   * @param namespaces namespaces
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  private byte[] name(final QNm qnm, final boolean attr, final XQMap namespaces,
      final QueryContext qc) throws QueryException {
    for(final Item prefix : namespaces.keys()) {
      if(Token.eq(qnm.uri(), toToken(namespaces.get(prefix), qc)))
        return new QNm(toToken(prefix), qnm.local(), qnm.uri()).string();
    }
    return attr ? qnm.unique() : qnm.eqName();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(true, false, cc.qc.focus.value);
  }
}
