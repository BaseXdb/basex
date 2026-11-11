package org.basex.query.func;

import java.util.*;
import java.util.function.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Constructor function for a shape.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ShapeConstructor extends StandardFunc {
  /** Shape. */
  private ShapeType shapeType;
  /** Values that need no coercion. */
  private final boolean[] typed;

  /**
   * Constructor.
   * @param shapeType shape
   */
  private ShapeConstructor(final ShapeType shapeType) {
    this.shapeType = shapeType;
    typed = new boolean[shapeType.fields().size()];
  }

  /**
   * Returns a constructor function for a shape.
   * @param ii input info
   * @param sh shape
   * @param args constructor arguments
   * @return constructor function
   */
  public static ShapeConstructor get(final InputInfo ii, final ShapeType sh, final Expr[] args) {
    final ShapeConstructor sc = new ShapeConstructor(sh);
    sc.init(ii, definition(sh), args);
    return sc;
  }

  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final TokenObjectMap<ShapeField> fields = shapeType.fields();
    final int fs = fields.size();
    final Value[] values = new Value[fs];
    for(int f = 0; f < fs; f++) {
      final Value value = exprs[f].value(qc);
      values[f] = typed[f] ? value : shapeType.coerce(f + 1, value, qc, info, null);
    }
    return shapeType.build(values, true, qc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // refine the field types of an anonymous shape from the argument types
    final int el = exprs.length;
    final SeqType[] seqTypes = new SeqType[el];
    for(int e = 0; e < el; e++) seqTypes[e] = exprs[e].seqType();
    final ShapeType sh = shapeType.refine(seqTypes);
    if(sh != shapeType) {
      shapeType = cc.qc.shared.shape(sh);
      exprType.assign(shapeType.seqType());
    }
    // skip runtime coercion of values that already match the field type
    final TokenObjectMap<ShapeField> fields = shapeType.fields();
    for(int e = 0; e < el; e++) {
      typed[e] = exprs[e].seqType().coercible(fields.value(e + 1).seqType());
    }
    return this;
  }

  @Override
  public int hofOffsets() {
    // function values are stored, not invoked
    return 0;
  }

  @Override
  protected Expr inlineTypeCheck(final TypeCheck tc, final CompileContext cc)
      throws QueryException {
    // { 'y': Y, 'x': X } coerce as local:point → local:point(X, Y, ())
    final SeqType st = tc.seqType();
    return st.occ.check(1) && TypeRef.deref(st.type) instanceof final RecordType rt &&
      !rt.any() && !(shapeType instanceof RecordType) ? record(info, rt, entries(), cc) : null;
  }

  /**
   * Returns the field values supplied by the arguments of this call.
   * @return field values
   */
  public TokenObjectMap<Expr> entries() {
    final TokenObjectMap<ShapeField> fields = shapeType.fields();
    final int el = exprs.length;
    final TokenObjectMap<Expr> entries = new TokenObjectMap<>(el);
    for(int e = 0; e < el; e++) {
      // values of fixed fields are not supplied by the caller
      if(!fields.value(e + 1).isFixed()) entries.put(fields.key(e + 1), exprs[e]);
    }
    return entries;
  }

  /**
   * Returns an optimized constructor call for a record type.
   * @param ii input info
   * @param rt record type
   * @param supplied supplied field values (can be {@code null})
   * @param cc compilation context
   * @return constructor call, or {@code null} if a field is unknown, fixed or missing
   * @throws QueryException query exception
   */
  public static Expr record(final InputInfo ii, final RecordType rt,
      final TokenObjectMap<Expr> supplied, final CompileContext cc) throws QueryException {
    if(supplied == null) return null;
    final TokenObjectMap<ShapeField> fields = rt.fields();
    final int fs = fields.size();
    final Expr[] args = new Expr[fs];
    int found = 0;
    for(int f = 1; f <= fs; f++) {
      final ShapeField rf = fields.value(f);
      final Expr expr = supplied.get(fields.key(f));
      if(expr != null) {
        // fixed fields must not be supplied
        if(rf.isFixed()) return null;
        args[f - 1] = expr;
        found++;
      } else if(rf.optional()) {
        args[f - 1] = rf.init() != null ? rf.init().copy(cc, new IntObjectMap<>()) : Empty.VALUE;
      } else {
        return null;
      }
    }
    // reject undeclared keys
    return found == supplied.size() ? get(ii, rt, args).optimize(cc) : null;
  }

  @Override
  public ShapeType exactShape() {
    // constructed records have no additional fields
    return shapeType.shape();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final ShapeConstructor sc &&
        shapeType.eq(sc.shapeType) && Arrays.equals(exprs, sc.exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    if(shapeType.name() != null) {
      super.toString(qs);
    } else {
      qs.token("{ ");
      final TokenObjectMap<ShapeField> fields = shapeType.fields();
      int f = 0;
      for(final Expr expr : exprs) {
        if(++f > 1) qs.token(',');
        qs.quoted(fields.key(f)).token(':').token(expr);
      }
      qs.token(" }");
    }
  }

  /**
   * Returns a constructor function definition for this shape.
   * @param sh shape
   * @return constructor function definition
   */
  private static FuncDefinition definition(final ShapeType sh) {
    final QNm name = sh.name();
    final TokenBuilder tb = new TokenBuilder(name != null ? name.local() :
      Token.token(QueryText.MAP)).add('(');
    final TokenObjectMap<ShapeField> fields = sh.fields();
    final int max = fields.size();
    for(int i = 1; i <= max; i++) {
      if(i > 1) tb.add(", ");
      tb.add(fields.key(i));
    }
    final String description = tb.add(')').toString();

    final SeqType[] params = new SeqType[max];
    for(int i = 0; i < max; i++) {
      params[i] = fields.value(i + 1).seqType();
    }

    final Supplier<ShapeConstructor> supplier = () -> new ShapeConstructor(sh);
    return new FuncDefinition(supplier, description, params, sh.seqType(),
        EnumSet.noneOf(Flag.class), name == null ? Token.EMPTY : name.uri(), Perm.NONE);
  }
}
