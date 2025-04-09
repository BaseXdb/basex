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
public class FnSchemaType extends StandardFunc {

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QNm name = toQNm(arg(0).atomItem(qc, info));
    Type type = AtomType.find(name, true);
    if(type == null) type = ListType.find(name);
    return annotate(qc, info, type);
  }

  /**
   * Creates a sequence of type annotations for the specified atomic types.
   * @param types the types to be annotated
   * @param qc query context
   * @param info input info
   * @return the type annotation sequence
   * @throws QueryException query exception
   */
  protected static Value annotate(final QueryContext qc, final InputInfo info, final Type... types)
      throws QueryException {

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Type type : types) {
      if(type == null) continue;
      final QNm name;
      final AtomType baseType;
      final Variety variety;
      AtomType primType = null;
      FuncItem members = null;
      FuncItem matches = null;
      boolean constructor = false;
      if(type instanceof ListType) {
        final ListType listType = (ListType) type;
        name = listType.qname();
        baseType = ANY_SIMPLE_TYPE;
        variety = Variety.list;
        members = TypeAnnotation.funcItem(info, listType.atomic());
        constructor = true;
      } else if(type instanceof AtomType) {
        final AtomType atomType = (AtomType) type;
        name = atomType.qname();
        if(atomType.atomic() != null) matches = Matches.funcItem(atomType, qc, info);
        switch(atomType) {
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
          case NUMERIC:
            baseType = ANY_SIMPLE_TYPE;
            variety = Variety.union;
            members = TypeAnnotation.funcItem(info, DOUBLE, FLOAT, DECIMAL);
            constructor = true;
            break;
          default:
            final AtomType parent = atomType.parent();
            baseType = parent == NUMERIC ? ANY_ATOMIC_TYPE : parent;
            variety = Variety.atomic;
            for(primType = atomType; !primType.parent().oneOf(ANY_ATOMIC_TYPE, NUMERIC, null);)
              primType = primType.parent();
            constructor = !type.oneOf(QNAME, NOTATION);
        }
      } else {
        throw Util.notExpected();
      }
      final MapBuilder mb = new MapBuilder();
      mb.put("name", name);
      mb.put("is-simple", Bln.get(!type.oneOf(ANY_TYPE, UNTYPED)));
      mb.put("base-type", TypeAnnotation.funcItem(info, baseType));
      if(primType != null) mb.put("primitive-type", TypeAnnotation.funcItem(info, primType));
      if(variety != null) mb.put("variety", variety.name());
      if(members != null) mb.put("members", members);
      if(matches != null) mb.put("matches", matches);
      if(constructor) {
        mb.put("constructor", (FuncItem) Functions.item(name, 1, true, info, qc, true));
      }
      vb.add(mb.map());
    }
    return vb.value();
  }

  /** The variety of a type. */
  private enum Variety {
    /** Mixed.  */ mixed,
    /** List.   */ list,
    /** Atomic. */ atomic,
    /** Union.  */ union
  };

  /**
   * Function creating the type annotations for given atomic types.
   */
  private static final class TypeAnnotation extends Arr {
    /** Function type. */
    private static final FuncType FUNC_TYPE = FuncType.get(MAP_ZM);
    /** The types to be annotated. */
    private final AtomType[] types;

    /**
     * Constructor.
     * @param info input info
     * @param types the types to be annotated
     */
    private TypeAnnotation(final InputInfo info, final AtomType... types) {
      super(info, FUNC_TYPE.declType);
      this.types = types;
    }

    /**
     * Create a function item for a new instance.
     * @param types the types to be annotated
     * @param info input info
     * @return the function item
     */
    public static FuncItem funcItem(final InputInfo info, final AtomType... types) {
      return new FuncItem(info, new TypeAnnotation(info, types), new Var[] { }, AnnList.EMPTY,
          FUNC_TYPE, 0, null);
    }

    @Override
    public Value value(final QueryContext qc) throws QueryException {
      return annotate(qc, info, types);
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
      return new TypeAnnotation(info, types);
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
    public static FuncItem funcItem(final AtomType type, final QueryContext qc,
        final InputInfo info) {
      final Var var = new VarScope().addNew(new QNm("value"), ANY_ATOMIC_TYPE_O, qc, info);
      final Var[] params = { var };
      return new FuncItem(info, new Matches(info, type, new VarRef(info, var)), params,
          AnnList.EMPTY, FUNC_TYPE, params.length, null);
    }

    @Override
    public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
      final Item value = toAtomItem(arg(0), qc);
      return Bln.get(value.type.instanceOf(type));
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
