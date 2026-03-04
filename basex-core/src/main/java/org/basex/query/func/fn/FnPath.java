package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
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
    public static final ValueOption ORIGIN = new ValueOption("origin", Types.GNODE_ZO);
    /** Option. */
    public static final BooleanOption LEXICAL = new BooleanOption("lexical", false);
    /** Option. */
    public static final ValueOption NAMESPACES = new ValueOption("namespaces", Types.MAP_O);
    /** Option. */
    public static final BooleanOption INDEXES = new BooleanOption("indexes", true);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    GNode node = toGNodeOrNull(context(qc), qc);
    final PathOptions options = toOptions(arg(1), new PathOptions(), qc);
    if(node == null) return Empty.VALUE;

    final TokenBuilder tb = new TokenBuilder();
    final TokenList steps = new TokenList();
    final boolean indexes = options.get(PathOptions.INDEXES);
    final Value ns = options.get(PathOptions.NAMESPACES);
    final XQMap namespaces = ns.isEmpty() ? XQMap.empty() : toMap(ns, qc);
    final boolean lexical = options.get(PathOptions.LEXICAL);
    final Value origin = options.get(PathOptions.ORIGIN);

    boolean relative = false;
    while(true) {
      final GNode parent = node.parent();
      final Type type = node.type;
      final Kind kind = type instanceof final NodeType nt ? nt.kind() : null;
      if(parent == null) {
        if(!kind.oneOf(Kind.DOCUMENT, Kind.JNODE)) {
          tb.add(name(Function.ROOT.definition().name, false, lexical, namespaces, qc)).add("()");
        }
        break;
      }
      // step: name/type
      final QNm qname = node.qname();
      if(kind == Kind.ATTRIBUTE) {
        tb.add('@').add(name(qname, true, lexical, namespaces, qc));
      } else if(kind == Kind.ELEMENT) {
        tb.add(name(qname, false, lexical, namespaces, qc));
      } else if(kind == Kind.PROCESSING_INSTRUCTION) {
        tb.add(kind.toString(Token.string(qname.local())));
      } else if(kind.oneOf(Kind.COMMENT, Kind.TEXT)) {
        tb.add(type.toString());
      } else if(parent instanceof final JNode jparent) {
        final JNode jnode = (JNode) node;
        final Item key = jnode.key;
        byte[] get = null;
        final byte[] string = key.string(info);
        if(jparent.value instanceof XQArray) {
          tb.add("*[").add(key).add("]");
        } else if(key instanceof AStr || key instanceof Atm || key instanceof Uri) {
          if(XMLToken.isNCName(string)) {
            tb.add(string);
          } else {
            get = QueryString.toQuoted(string);
          }
        } else if(key instanceof ANum) {
          get = string;
        } else if(key instanceof final QNm qnm) {
          tb.add(qnm.eqName());
        } else if(key instanceof Bln) {
          get = Token.concat(string, "()");
        } else {
          get = Token.concat(key.type.toString(), '(', QueryString.toQuoted(string), ')');
        }
        if(get != null) tb.add("get(").add(get).add(')');
      }
      // optional index
      if(indexes && !kind.oneOf(Kind.ATTRIBUTE, Kind.JNODE)) {
        int p = 1;
        for(final GNode nd : node.precedingSiblingIter(false)) {
          qc.checkStop();
          final QNm qnm = nd.qname();
          if(nd.kind() == kind && (qnm == null || nd.qname().eq(qname))) {
            p++;
          }
        }
        tb.add('[').addInt(p).add(']');
      }
      steps.add(tb.next());
      node = parent;

      // root node: finalize traversal
      if(origin instanceof final GNode nd && node.is(nd)) {
        relative = true;
        break;
      }
    }
    if(origin instanceof GNode && !relative) throw PATH_X.get(info, origin);

    // add all steps in reverse order; cache element paths
    for(int s = steps.size() - 1; s >= 0; --s) {
      if(!(tb.isEmpty() && origin instanceof GNode)) tb.add('/');
      tb.add(steps.get(s));
    }
    return Str.get(tb.isEmpty() ? Token.cpToken('/') : tb.finish());
  }

  /**
   * Returns a name string for the specified QName.
   * @param qnm QName
   * @param attr attribute flag
   * @param namespaces namespaces
   * @param lexical lexical flag
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  private byte[] name(final QNm qnm, final boolean attr, final boolean lexical,
      final XQMap namespaces, final QueryContext qc) throws QueryException {

    if(lexical) return qnm.string();
    for(final Item prefix : namespaces.keys()) {
      if(Token.eq(qnm.uri(), toToken(namespaces.get(prefix), qc))) {
        return new QNm(toToken(prefix), qnm.local(), qnm.uri()).string();
      }
    }
    return attr ? qnm.unique() : qnm.eqName();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(true, false, cc.qc.focus.value);
  }
}
