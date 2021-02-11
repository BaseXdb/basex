package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract fragment constructor with a QName argument.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class CName extends CNode {
  /** QName. */
  Expr name;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param seqType sequence type
   * @param computed computed constructor
   * @param name name
   * @param cont contents
   */
  CName(final StaticContext sc, final InputInfo info, final SeqType seqType, final boolean computed,
      final Expr name, final Expr... cont) {
    super(sc, info, seqType, computed, cont);
    this.name = name;
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(name);
    super.checkUp();
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    name = name.compile(cc);
    return super.compile(cc);
  }

  /**
   * Optimizes the node value.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  final void optValue(final CompileContext cc) throws QueryException {
    simplifyAll(Simplify.STRING, cc);
    if(allAreValues(true) && (exprs.length != 1 || !(exprs[0] instanceof Str))) {
      exprs = new Expr[] { Str.get(atomValue(cc.qc)) };
    }
  }

  @Override
  final byte[] atomValue(final QueryContext qc) throws QueryException {
    final byte[] value = super.atomValue(qc);
    return value != null ? value : Token.EMPTY;
  }

  /**
   * Evaluates the name expression as a QName.
   * @param elem element
   * @param qc query context
   * @param sctx static context for resolving namespaces (can be {@code null})
   * @return result, or {@code null} if namespace cannot be resolved (at compile time)
   * @throws QueryException query exception
   */
  final QNm qname(final boolean elem, final QueryContext qc, final StaticContext sctx)
      throws QueryException {

    final Item item = checkNoEmpty(name.atomItem(qc, info), AtomType.QNAME);
    final Type type = item.type;
    if(type == AtomType.QNAME) return (QNm) item;
    if(!type.isStringOrUntyped() || type == AtomType.ANY_URI)
      throw STRQNM_X_X.get(info, type, item);

    // check for QName
    final byte[] token = normalize(item.string(info));
    if(XMLToken.isQName(token)) return !(elem || contains(token, ':')) ?
      new QNm(token) : sctx != null ? new QNm(token, sctx) : null;

    // check for EQName
    final String string = string(token);
    if(string.matches("^Q\\{.*\\}.+")) {
      final byte[] local = token(string.replaceAll("^.*?\\}", ""));
      final byte[] uri = normalize(token(string.replaceAll("^Q\\{|\\}.*", "")));
      if(XMLToken.isNCName(local) && !eq(uri, XMLNS_URI) && !contains(uri, '{')) {
        return new QNm(local, uri);
      }
    }

    throw INVNAME_X.get(info, token);
  }

  /**
   * Evaluates the name expression as a NCName.
   * @param empty allow empty name
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  final byte[] ncname(final boolean empty, final QueryContext qc) throws QueryException {
    final Item item = name.atomItem(qc, info);
    if(item != Empty.VALUE) {
      final Type type = item.type;
      if(type.isStringOrUntyped() && type != AtomType.ANY_URI) return trim(item.string(info));
      throw STRNCN_X_X.get(info, type, item);
    }
    if(empty) return EMPTY;
    throw STRNCN_X_X.get(info, SeqType.EMPTY_SEQUENCE_Z, item);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return name.inlineable(ic) && super.inlineable(ic);
  }

  @Override
  public final boolean has(final Flag... flags) {
    return name.has(flags) || super.has(flags);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return name.accept(visitor) && visitAll(visitor, exprs);
  }

  @Override
  public final VarUsage count(final Var var) {
    return name.count(var).plus(super.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = ic.inline(exprs);
    final Expr inlined = name.inline(ic);
    if(inlined != null) {
      name = inlined;
      changed = true;
    }
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public final int exprSize() {
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size + name.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CName && name.equals(((CName) obj).name) && super.equals(obj);
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this), name, exprs);
  }

  @Override
  public final void plan(final QueryString qs, final String kind) {
    qs.token(kind);
    if(name instanceof QNm) qs.token(((QNm) name).id());
    else qs.brace(name);
    super.plan(qs, null);
  }
}
