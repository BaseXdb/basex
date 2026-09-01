package org.basex.query.expr.path;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * JNode test.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JNodeTest extends Test {
  /** Key ({@code null} for wildcard, {@link Empty#VALUE} for root node). */
  public final Item key;
  /** Value type. */
  public final SeqType valueType;

  /**
   * Constructor.
   * @param key key ({@code null} for wildcard, {@link Empty#VALUE} for root node)
   * @param valueType value type
   */
  private JNodeTest(final Item key, final SeqType valueType) {
    super(Kind.JNODE);
    this.key = key;
    this.valueType = valueType;
  }

  /**
   * Creates a new JNode test.
   * @param key key ({@code null} for wildcard, {@link Empty#VALUE} for root node)
   * @param valueType value type (can be {@code null})
   * @return test
   */
  public static Test get(final Item key, final SeqType valueType) {
    final SeqType ct = valueType != null ? valueType : Types.ITEM_ZM;
    return ct.eq(Types.ITEM_ZM) && key == null ? NodeTest.JNODE : new JNodeTest(key, ct);
  }

  /**
   * Compares two items.
   * @param s1 first item (can be {@code null})
   * @param s2 second item (can be {@code null})
   * @return result of check
   */
  public static boolean equals(final Item s1, final Item s2) {
    try {
      return s1 == null || s2 == null ? s1 == s2 : s1.atomicEqual(s2);
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Compares two values.
   * @param v1 first value
   * @param v2 second value
   * @return result of check
   */
  public static boolean equals(final Value v1, final Value v2) {
    // if values are compared, reference checks for non-atomic values are sufficient
    if(v1 == v2) return true;

    if(v1 instanceof final Item i1 && v2 instanceof final Item i2 &&
        i1.type.instanceOf(BasicType.ANY_ATOMIC_TYPE)) {
      try {
        return i1.atomicEqual(i2);
      } catch(final QueryException ex) {
        throw Util.notExpected(ex);
      }
    }
    return false;
  }

  @Override
  public boolean matches(final GNode node) {
    if(node instanceof final JNode jnode) {
      return (key == null || (key == Empty.VALUE ? jnode.isRoot() : equals(key, jnode.key))) &&
          valueType.instance(jnode.value);
    }
    return false;
  }

  /**
   * Returns the JNode key that is addressed by the specified name.
   * @param qname name
   * @return key
   */
  public static Item key(final QNm qname) {
    return Str.get(qname.string());
  }

  @Override
  public Item key() {
    return key != Empty.VALUE ? key : null;
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public Boolean subsumes(final Type type) {
    // [ 'yes ']/self::node();
    if(kind == Kind.NODE) return Boolean.TRUE;
    // (<who/>, [ 'knows' ])/self::jnode(no-one)
    final Kind kn = type.kind();
    if(kn == null || kn == Kind.NODE) return null;
    // <no/>/self::jnode(no)
    if(kn != kind) return Boolean.FALSE;
    // { 'yes': () }/self::jnode(yes), { 'maybe': () }/self::jnode(no)
    if(type instanceof final NodeType ntype && ntype.test instanceof final JNodeTest jt)
      return jt.instanceOf(this);
    // ([], [ 'x' ])/*/self::jnode(1)
    return null;
  }

  @Override
  public boolean instanceOf(final Test test) {
    if(test instanceof final JNodeTest jt) {
      final Item k1 = key, k2 = jt.key;
      return (k2 == null || equals(k1, k2)) && valueType.instanceOf(jt.valueType);
    }
    if(test instanceof final NameTest nt && kind.instanceOf(nt.kind)) {
      return nt.scope == NameTest.Scope.ALL || nt.scope == NameTest.Scope.FLEXIBLE &&
        key != null && key != Empty.VALUE && equals(key, key(nt.qname));
    }
    return super.instanceOf(test);
  }

  @Override
  public Test intersect(final Test test) {
    if(test == NodeTest.NODE) return this;
    if(test instanceof final JNodeTest jt) {
      final SeqType ct = valueType.intersect(jt.valueType);
      if(ct == null) return null;
      Item k1 = key;
      final Item k2 = jt.key;
      if(k2 != null) {
        if(k1 != null && !equals(k1, k2)) return null;
        k1 = k2;
      }
      return get(k1, ct);
    } else if(test instanceof NodeTest || test instanceof UnionTest) {
      return test.intersect(this);
    }
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final JNodeTest nt &&
      kind == nt.kind && Objects.equals(key, nt.key) && valueType.eq(nt.valueType);
  }

  @Override
  public String toString(final boolean type) {
    final ArrayList<Object> list = new ArrayList<>();
    if(key != null || !valueType.eq(Types.ITEM_ZM)) {
      Object sel = key;
      if(sel == null) sel = "*";
      else if(sel == Empty.VALUE) sel = "()";
      else if(key instanceof final Str str && XMLToken.isNCName(str.string())) {
        sel = str.string();
      }
      list.add(sel);

      if(!valueType.eq(Types.ITEM_ZM)) list.add(valueType);
    }
    return new QueryString().token(kind.description()).tokens(
        list.toArray(Object[]::new), ", ", true).toString();
  }
}
