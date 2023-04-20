package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Element constructor.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CElem extends CName {
  /** Namespaces. */
  private final Atts nspaces;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param computed computed constructor
   * @param name name
   * @param nspaces namespaces or {@code null} if this is a computed constructor
   * @param exprs element contents
   */
  public CElem(final StaticContext sc, final InputInfo info, final boolean computed,
      final Expr name, final Atts nspaces, final Expr... exprs) {
    super(sc, info, SeqType.ELEMENT_O, computed, name, exprs);
    this.nspaces = nspaces;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final int s = addNS();
    try {
      return super.compile(cc);
    } finally {
      sc.ns.size(s);
    }
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    name = name.simplifyFor(Simplify.STRING, cc);
    if(name instanceof Value) {
      final QNm nm = qname(true, cc.qc);
      name = nm;
      exprType.assign(SeqType.get(NodeType.ELEMENT, Occ.EXACTLY_ONE,
          Test.get(NodeType.ELEMENT, nm, null)));
    }

    // merge adjacent and nested text values
    final Predicate<Expr> atomic = arg -> arg.seqType().instanceOf(SeqType.ANY_ATOMIC_TYPE_ZM);
    final Predicate<Expr> text = arg -> arg instanceof CTxt && arg.arg(0) instanceof Item;
    final TokenBuilder tb = new TokenBuilder();
    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr instanceof List && ((Checks<Expr>) arg -> text.test(arg) ||
          arg instanceof Value && atomic.test(arg)).all(expr.args())) {
        boolean more = false;
        for(final Expr arg : expr.args()) {
          if(text.test(arg)) {
            tb.add(((Item) arg.arg(0)).string(info));
            more = false;
          } else {
            for(final Item item : (Value) arg) {
              if(more) tb.add(' ');
              tb.add(item.string(info));
              more = true;
            }
          }
        }
        list.add(Str.get(tb.next()));
      } else if(expr instanceof Seq && atomic.test(expr)) {
        boolean more = false;
        for(final Item item : (Seq) expr) {
          if(more) tb.add(' ');
          tb.add(item.string(info));
          more = true;
        }
        list.add(Str.get(tb.next()));
      } else if(!(expr instanceof Empty)) {
        list.add(expr);
      }
    }
    exprs = list.next();

    if(exprs.length > 1 || exprs.length == 1 && !(exprs[0] instanceof Str)) {
      for(final Expr expr : exprs) {
        if(expr instanceof Item && atomic.test(expr)) {
          tb.add(((Item) expr).string(info));
        } else if(text.test(expr)) {
          tb.add(((Item) expr.arg(0)).string(info));
        } else {
          if(!tb.isEmpty()) list.add(Str.get(tb.next()));
          list.add(expr);
        }
      }
      if(!tb.isEmpty()) list.add(Str.get(tb.finish()));
      exprs = list.finish();
    }

    return this;
  }

  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int s = addNS();
    try {
      // adds in-scope namespaces
      final Atts inscopeNS = new Atts();
      final int ns = nspaces.size();
      for(int n = 0; n < ns; n++) inscopeNS.add(nspaces.name(n), nspaces.value(n));

      // create and check QName
      final QNm nm = qname(true, qc);
      final byte[] nmPrefix = nm.prefix(), nmUri = nm.uri();
      if(eq(nmPrefix, XML) ^ eq(nmUri, XML_URI)) throw CEXML.get(info, nmPrefix, nmUri);
      if(eq(nmUri, XMLNS_URI)) throw CEINV_X.get(info, nmUri);
      if(eq(nmPrefix, XMLNS)) throw CEINV_X.get(info, nmPrefix);
      if(!nm.hasURI() && nm.hasPrefix()) throw NOQNNAMENS_X.get(info, nmPrefix);

      // create node
      final FBuilder elem = FElem.build(nm);

      // add child and attribute nodes
      final Constr constr = new Constr(elem, info, sc, qc).add(exprs);
      if(constr.errAtt != null) throw NOATTALL_X.get(info, constr.errAtt);
      if(constr.errNS != null) throw NONSALL_X.get(info, constr.errNS);
      if(constr.duplAtt != null) throw CATTDUPL_X.get(info, constr.duplAtt);
      if(constr.duplNS != null) throw DUPLNSCONS_X.get(info, constr.duplNS);
      // assign namespaces
      constr.namespaces(inscopeNS, nm);
      // return optimized node
      return elem.finish();

    } finally {
      sc.ns.size(s);
    }
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CElem(sc, info, computed, name.copy(cc, vm), new Atts(nspaces),
        copyAll(cc, vm, exprs)));
  }

  /**
   * Adds namespaces to the namespace stack.
   * @return old position in namespace stack
   */
  private int addNS() {
    final NSContext nsContext = sc.ns;
    final int size = nsContext.size(), ns = nspaces.size();
    for(int n = 0; n < ns; n++) nsContext.add(nspaces.name(n), nspaces.value(n));
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CElem &&
        nspaces.equals(((CElem) obj).nspaces) && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    if(computed) {
      toString(qs, ELEMENT);
    } else {
      final byte[] nm = ((QNm) name).string();
      qs.token('<').token(nm);
      final int el = exprs.length;
      for(int e = 0; e < el; e++) {
        final Expr expr = exprs[e];
        if(expr instanceof CAttr && !((CAttr) expr).computed) {
          qs.token(expr);
        } else {
          qs.token('>');
          boolean constr = false;
          for(int f = e; f < el && !constr; f++) {
            constr = exprs[f] instanceof CNode ? ((CNode) exprs[f]).computed :
              !(exprs[f] instanceof Str);
          }
          if(constr) {
            qs.token('{').tokens(Arrays.copyOfRange(exprs, e, el), SEP).token("}");
          } else {
            for(int f = e; f < el; f++) {
              if(exprs[f] instanceof Str) {
                qs.value(((Str) exprs[f]).string());
              } else {
                qs.token(exprs[f]);
              }
            }
          }
          qs.token('<').token('/').token(nm).token('>');
          return;
        }
      }
      qs.token('/').token('>');
    }
  }
}
