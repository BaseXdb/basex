package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract lookup expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ALookup extends Arr {
  /** Wildcard string. */
  public static final Str WILDCARD = Str.get('*');
  /** Modifier. */
  protected final Modifier modifier;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param modifier modifier
   * @param expr context expression and key specifier
   */
  public ALookup(final InputInfo info, final Modifier modifier, final Expr... expr) {
    super(info, SeqType.ITEM_ZM, expr);
    this.modifier = modifier;
  }

  /**
   * Returns the looked up values for the specified input.
   * @param item input item
   * @param deep deep lookup
   * @param qc query context
   * @return supplied value builder
   * @throws QueryException query exception
   */
  final Value valueFor(final Item item, final boolean deep, final QueryContext qc)
      throws QueryException {
    if(!(item instanceof XQStruct)) throw LOOKUP_X.get(info, item);
    final XQStruct struct = (XQStruct) item;
    final Expr keys = exprs[1];

    // wildcard: add all values
    if(keys == WILDCARD) {
      if(hasDefaultModifier()) return struct.items(qc);
      final ValueBuilder vb = new ValueBuilder(qc);
      if(struct instanceof XQArray array) {
        long k = 0;
        for(final Value val : array.iterable()) {
          vb.add(modify(Int.get(++k), val));
        }
      } else {
        ((XQMap) struct).forEach((key, value) -> vb.add(modify(key, value)));
      }
      return vb.value();
    }

    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter ir = keys.atomIter(qc, info);
    for(Item key; (key = ir.next()) != null;) {
      Value value = null;
      if(struct instanceof XQMap) {
        value = ((XQMap) struct).getOrNull(key);
        if(value instanceof FuncItem) {
          value = ((FuncItem) value).toMethod(struct);
        }
      } else {
        final Item index;
        if (!deep) {
          index = key;
        } else if(!key.type.isNumber()) {
          index = null;
        } else {
          final Item cast = (Item) SeqType.INTEGER_O.cast(key, false, qc, info);
          index = key.equal(cast, null, info) ? cast : null;
        }
        if(index != null) value = ((XQArray) struct).getOrNull(index, qc, info);
      }
      if(value != null) vb.add(modify(key, value));
    }
    return vb.value(this);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(exprs[0]).token('?');
    if(this instanceof DeepLookup) qs.token('?');
    if(modifier != Modifier.NONE) qs.concat(modifier, "::");

    final Expr keys = exprs[1];
    Object key = null;
    if(keys == WILDCARD) {
      key = WILDCARD.string();
    } else if(keys instanceof Str) {
      final Str str = (Str) keys;
      if(XMLToken.isNCName(str.string())) key = str.toJava();
    } else if(keys instanceof Int) {
      final long l = ((Int) keys).itr();
      if(l >= 0) key = l;
    }
    if(key != null) qs.token(key);
    else qs.paren(keys);
  }

  /**
   * Applies a modifier to a key-value pair.
   * @param key the key
   * @param value the value
   * @return the modified value
   * @throws QueryException if an error occurs during processing
   */
  private Value modify(final Item key, final Value value) throws QueryException {
    return hasDefaultModifier() ? value : switch(modifier) {
      case PAIRS -> new MapBuilder().put("key", key).put("value", value).map();
      case KEYS -> key;
      case VALUES -> {
        final ArrayBuilder ab = new ArrayBuilder();
        value.forEach(it -> ab.add(it));
        yield ab.array();
      }
      default -> throw Util.notExpected("Unexpected modifier: %", modifier);
    };
  }

  /**
   * Checks whether this instance is using a default modifier.
   * @return true, if the modifier is default
   */
  public boolean hasDefaultModifier() {
    return modifier == Modifier.NONE || modifier == Modifier.ITEMS;
  }

  /** Lookup operator modifiers. */
  public enum Modifier {
    /** Pairs modifier. */
    PAIRS("pairs"),
    /** Keys modifier. */
    KEYS("keys"),
    /** Values modifier. */
    VALUES("values"),
    /** Items modifier. */
    ITEMS("items"),
    /** No modifier. */
    NONE("");

    /** Name of axis. */
    public final String name;

    /**
     * Constructor.
     * @param name modifier string
     */
    Modifier(final String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}