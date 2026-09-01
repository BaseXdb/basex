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
 */
public final class ShapeConstructor extends StandardFunc {
  /** Shape. */
  private ShapeType shapeType;
  /** Field names. */
  private final QNm[] names;
  /** Values that need no coercion. */
  private final boolean[] typed;

  /**
   * Constructor.
   * @param shapeType shape
   */
  private ShapeConstructor(final ShapeType shapeType) {
    this.shapeType = shapeType;
    final TokenObjectMap<ShapeField> fields = shapeType.fields();
    final int fs = fields.size();
    names = new QNm[fs];
    typed = new boolean[fs];
    for(int f = 1; f <= fs; ++f) {
      names[f - 1] = new QNm(fields.key(f));
    }
  }

  /**
   * Returns a constructor function for a shape.
   * @param sh shape
   * @param ii input info
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
    final int fs = fields.size(), el = exprs.length;
    final Value[] values = new Value[fs];
    for(int f = 0; f < fs; ++f) {
      final ShapeField rf = fields.value(f + 1);
      final Expr expr = f < el ? exprs[f] : rf.init();
      final Value value = expr != null ? expr.value(qc) : Empty.VALUE;
      values[f] = typed[f] ? value : rf.seqType().coerce(value, qc, info, names[f], null);
    }
    return XQMap.get(shapeType, values);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
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
    final int fs = fields.size();
    for(int f = 0; f < fs; ++f) {
      final ShapeField rf = fields.value(f + 1);
      final Expr expr = f < el ? exprs[f] : rf.init();
      typed[f] = expr != null && expr.seqType().instanceOf(rf.seqType());
    }
    return this;
  }

  @Override
  public long structSize() {
    return shapeType.fields().size();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final ShapeConstructor sc &&
        shapeType.eq(sc.shapeType) && Array.equals(exprs, sc.exprs);
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
      Token.token(sh instanceof RecordType ? QueryText.RECORD : QueryText.MAP)).add('(');
    final TokenObjectMap<ShapeField> fields = sh.fields();
    final int max = fields.size(), min = sh.minFields();
    for(int i = 1; i <= max; ++i) {
      if(i > 1) tb.add(", ");
      tb.add(fields.key(i));
      if(i > min) tb.add('?');
    }
    final String description = tb.add(')').toString();

    final SeqType[] params = new SeqType[max];
    for(int i = 0; i < max; ++i) {
      params[i] = fields.value(i + 1).seqType();
    }

    final Supplier<ShapeConstructor> supplier = () -> new ShapeConstructor(sh);
    return new FuncDefinition(supplier, description, params, sh.seqType(),
        EnumSet.noneOf(Flag.class), name == null ? Token.EMPTY : name.uri(), Perm.NONE);
  }
}
