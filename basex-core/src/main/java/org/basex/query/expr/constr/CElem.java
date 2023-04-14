package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;

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
      final QNm nm = qname(true, cc.qc, true);
      if(nm != null) {
        name = nm;
        exprType.assign(SeqType.get(NodeType.ELEMENT, Occ.EXACTLY_ONE,
            Test.get(NodeType.ELEMENT, nm, null)));
      }
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
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int s = addNS();
    try {
      // adds in-scope namespaces
      final Atts inscopeNS = new Atts();
      final int nl = nspaces.size();
      for(int i = 0; i < nl; i++) inscopeNS.add(nspaces.name(i), nspaces.value(i));

      // create and check QName
      QNm nm = qname(true, qc, false);
      final byte[] nmPrefix = nm.prefix(), nmUri = nm.uri();
      if(eq(nmPrefix, XML) ^ eq(nmUri, XML_URI)) throw CEXML.get(info, nmPrefix, nmUri);
      if(eq(nmUri, XMLNS_URI)) throw CEINV_X.get(info, nmUri);
      if(eq(nmPrefix, XMLNS)) throw CEINV_X.get(info, nmPrefix);
      if(!nm.hasURI() && nm.hasPrefix()) throw NOQNNAMENS_X.get(info, nmPrefix);

      // create node
      final Constr constr = new Constr(info, sc);
      final FElem node = new FElem(nm, inscopeNS, constr.atts, constr.children);

      // add child and attribute nodes
      constr.add(qc, exprs);
      if(constr.errAtt != null) throw NOATTALL_X.get(info, constr.errAtt);
      if(constr.errNS != null) throw NONSALL_X.get(info, constr.errNS);
      if(constr.duplAtt != null) throw CATTDUPL_X.get(info, constr.duplAtt);
      if(constr.duplNS != null) throw DUPLNSCONS_X.get(info, constr.duplNS);

      // add namespace for element name (unless its prefix is "xml")
      final Atts cns = constr.nspaces;
      if(!eq(nmPrefix, XML)) {
        // check declaration of default namespace
        final int cnsDef = cns.get(EMPTY);
        if(cnsDef != -1) {
          final byte[] uri = cns.value(cnsDef);
          if(!nm.hasURI()) throw EMPTYNSCONS_X.get(info, uri);
          final int scope = inscopeNS.get(EMPTY);
          final byte[] scopeUri = scope != -1 ? inscopeNS.value(scope) : sc.ns.uri(EMPTY);
          if(scopeUri != null && !eq(scopeUri, uri)) throw DUPLNSCONS_X.get(info, uri);
        }

        // check if element has a namespace
        final byte[] uri = sc.ns.uri(nmPrefix);
        if(nm.hasURI()) {
          // add to statically known namespaces
          if(!computed && (uri == null || !eq(uri, nmUri))) sc.ns.add(nmPrefix, nmUri);
          // add to in-scope namespaces
          if(!inscopeNS.contains(nmPrefix)) inscopeNS.add(nmPrefix, nmUri);
        } else if(uri != null) {
          // element has no namespace: assign default uri
          nm = qc.qnmPool.get(nm.string(), normalize(uri));
        }
      }

      // add constructed namespaces
      final int cl = cns.size();
      for(int c = 0; c < cl; c++) addNS(cns.name(c), cns.value(c), inscopeNS);

      // add namespaces for attributes
      final int al = constr.atts.size();
      for(int a = 0; a < al; a++) {
        final ANode att = constr.atts.get(a);
        final QNm qnm = att.qname();
        // skip attributes without prefixes or URIs
        if(!qnm.hasPrefix() || !qnm.hasURI()) continue;

        // skip XML namespace
        final byte[] apref = qnm.prefix();
        if(eq(apref, XML)) continue;

        final byte[] auri = qnm.uri();
        final byte[] npref = addNS(apref, auri, inscopeNS);
        if(npref != null) {
          final QNm aname = new QNm(concat(npref, COLON, qnm.local()), auri);
          constr.atts.set(a, new FAttr(aname, att.string()));
        }
      }

      // update and optimize child nodes
      for(final ANode ch : constr.children) ch.optimize();
      // return generated and optimized node
      return node.optimize();

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
   * Adds the specified namespace to the namespace array.
   * If the prefix is already used for another URI, a new name is generated.
   * @param pref prefix
   * @param uri uri
   * @param ns namespaces
   * @return resulting prefix or {@code null}
   */
  private static byte[] addNS(final byte[] pref, final byte[] uri, final Atts ns) {
    final byte[] u = ns.value(pref);
    if(u == null) {
      // add undeclared namespace
      ns.add(pref, uri);
    } else if(!eq(u, uri)) {
      // prefixes with different URIs exist; new one must be replaced
      byte[] apref = null;
      // check if one of the existing prefixes can be adopted
      final int nl = ns.size();
      for(int n = 0; n < nl; n++) {
        if(eq(ns.value(n), uri)) apref = ns.name(n);
      }
      // if negative, generate a new one that is not used yet
      if(apref == null) {
        int i = 1;
        do {
          apref = concat(pref, "_", i++);
        } while(ns.contains(apref));
        ns.add(apref, uri);
      }
      return apref;
    }
    return null;
  }

  /**
   * Adds namespaces to the namespace stack.
   * @return old position in namespace stack
   */
  private int addNS() {
    final NSContext ns = sc.ns;
    final int size = ns.size(), nl = nspaces.size();
    for(int n = 0; n < nl; n++) ns.add(nspaces.name(n), nspaces.value(n));
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
