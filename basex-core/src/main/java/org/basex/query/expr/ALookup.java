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
    if(!(item instanceof XQStruct struct)) throw LOOKUP_X.get(info, item);
    final Expr keys = exprs[1];

    // wildcard: add all values
    if(keys == WILDCARD) {
      if(modifier == Modifier.ITEMS) return struct.items(qc);
      final ValueBuilder vb = new ValueBuilder(qc);
      if(struct instanceof XQArray array) {
        long k = 0;
        for(final Value val : array.iterable()) {
          vb.add(modify(Itr.get(++k), val));
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
      if(struct instanceof XQMap map) {
        value = map.getOrNull(key);
        if(value instanceof FuncItem fi) value = fi.toMethod(struct);
      } else {
        Item index = null;
        if(!deep) {
          index = key;
        } else if(key.type.isNumber()) {
          final Item cast = AtomType.INTEGER.cast(key, qc, info);
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
    if(modifier != Modifier.ITEMS) qs.concat(modifier, "::");

    final Expr keys = exprs[1];
    Object key = null;
    if(keys == WILDCARD) {
      key = WILDCARD.string();
    } else if(keys instanceof Str str) {
      if(XMLToken.isNCName(str.string())) key = str.toJava();
    } else if(keys instanceof Itr itr) {
      final long l = itr.itr();
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
    return switch(modifier) {
      case ITEMS -> value;
      case KEYS -> key;
      case PAIRS -> XQMap.pair(key, value);
      default -> {
        final ArrayBuilder ab = new ArrayBuilder();
        value.forEach(it -> ab.add(it));
        yield ab.array();
      }
    };
  }

  /** Lookup operator modifiers. */
  public enum Modifier {
    /** Items modifier.  */ ITEMS,
    /** Keys modifier.   */ KEYS,
    /** Pairs modifier.  */ PAIRS,
    /** Values modifier. */ VALUES;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }
}
