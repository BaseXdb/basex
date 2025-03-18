package org.basex.query.func.fn;

import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.SeqType.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnAtomicTypeAnnotation extends StandardFunc {

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = toAtomItem(arg(0), qc);
    return annotate(value.type.atomic(), qc, ii);
  }

  /**
   * Creates a type annotation for the specified atomic type.
   * @param type the type to be annotated
   * @param qc query context
   * @param ii input info
   * @return the type annotation
   * @throws QueryException query exception
   */
  protected static Item annotate(final AtomType type, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    if(type == null) return Empty.VALUE;

    final AtomType baseType;
    final Variety variety;
    AtomType primType = null;
    FuncItem constructor = null;
    switch(type) {
      case ANY_TYPE:
        baseType = null;
        variety = Variety.mixed;
        break;
      case UNTYPED:
        baseType = ANY_TYPE;
        variety = Variety.mixed;
        break;
      case ANY_SIMPLE_TYPE:
        baseType = ANY_TYPE;
        variety = null;
        break;
      case ANY_ATOMIC_TYPE:
        baseType = ANY_SIMPLE_TYPE;
        variety = Variety.atomic;
        break;
      default:
        final AtomType parent = type.parent;
        baseType = parent == NUMERIC ? ANY_ATOMIC_TYPE : parent;
        variety = Variety.atomic;
        for(primType = type; !primType.parent.oneOf(ANY_ATOMIC_TYPE, NUMERIC, null);)
          primType = primType.parent;
        if(!type.oneOf(QNAME, NOTATION))
          constructor = (FuncItem) Functions.item(type.qname(), 1, true, ii, qc, true);
    }

    final MapBuilder mb = new MapBuilder();
    mb.put("name", type.qname());
    mb.put("is-simple", Bln.get(!type.oneOf(ANY_TYPE, UNTYPED)));
    mb.put("base-type", TypeAnnotation.funcItem(baseType, ii));
    if(primType != null) mb.put("primitive-type", TypeAnnotation.funcItem(primType, ii));
    if(variety != null) mb.put("variety", variety.name());
    if(type.atomic() != null) mb.put("matches", Matches.funcItem(type, qc, ii));
    if(constructor != null) mb.put("constructor", constructor);
    return mb.map();
  }

  /** The variety of a type. */
  private enum Variety {
    /** Mixed.  */ mixed,
    /** List.   */ list,
    /** Atomic. */ atomic
  };

  /**
   * Function creating the type annotation for a given atomic type.
   */
  private static final class TypeAnnotation extends Arr {
    /** Function type. */
    private static final FuncType FUNC_TYPE = FuncType.get(MAP_O);
    /** The type to be annotated. */
    private final AtomType type;

    /**
     * Constructor.
     * @param info input info
     * @param type the type to be annotated
     */
    private TypeAnnotation(final InputInfo info, final AtomType type) {
      super(info, MAP_O);
      this.type = type;
    }

    /**
     * Create a function item for a new instance.
     * @param type the type to be matched
     * @param info input info
     * @return the function item
     */
    public static Value funcItem(final AtomType type, final InputInfo info) {
      return new FuncItem(info, new TypeAnnotation(info, type), new Var[] { }, AnnList.EMPTY,
          FUNC_TYPE, 0, null);
    }

    @Override
    public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
      return annotate(type, qc, ii);
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
      return new TypeAnnotation(info, type);
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token("type-annotation").params(exprs);
    }
  }

  /**
   * Function checking if an item matches a given type.
   */
  private static final class Matches extends Arr {
    /** Function type. */
    private static final FuncType FUNC_TYPE = FuncType.get(BOOLEAN_O, ANY_ATOMIC_TYPE_O);
    /** The type to be matched. */
    final AtomType type;

    /**
     * Constructor.
     * @param info input info
     * @param type the type to be matched
     * @param args the arguments
     */
    private Matches(final InputInfo info, final AtomType type, final Expr... args) {
      super(info, BOOLEAN_O, args);
      this.type = type;
    }

    /**
     * Create a function item for a new instance.
     * @param type the type to be matched
     * @param qc query context
     * @param info input info
     * @return the function item
     */
    public static Value funcItem(final AtomType type, final QueryContext qc, final InputInfo info) {
      final Var var = new VarScope().addNew(new QNm("value"), ANY_ATOMIC_TYPE_O, qc, info);
      final Var[] params = { var };
      return new FuncItem(info, new Matches(info, type, new VarRef(info, var)), params,
          AnnList.EMPTY, FUNC_TYPE, params.length, null);
    }

    @Override
    public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
      final Item value = toAtomItem(arg(0), qc);
      return Bln.get(value.instanceOf(type));
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
      return new Matches(info, type, copyAll(cc, vm, args()));
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token("matches").params(exprs);
    }
  }
}
