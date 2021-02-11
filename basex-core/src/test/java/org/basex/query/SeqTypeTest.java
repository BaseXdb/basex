package org.basex.query;

import static org.basex.query.value.type.Occ.*;
import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.SeqType.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.*;

import org.basex.query.value.type.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link SeqType} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class SeqTypeTest {
  /** Tests for {@link Occ#intersect(Occ)}. */
  @Test public void occIntersect() {
    final Occ[] occs = { ZERO, ZERO_OR_ONE, EXACTLY_ONE, ZERO_OR_MORE, ONE_OR_MORE };
    final Occ[][] table = {
        { ZERO, ZERO,     null, ZERO,      null     },
        { ZERO, ZERO_OR_ONE, EXACTLY_ONE,  ZERO_OR_ONE,  EXACTLY_ONE      },
        { null, EXACTLY_ONE,      EXACTLY_ONE,  EXACTLY_ONE,       EXACTLY_ONE      },
        { ZERO, ZERO_OR_ONE, EXACTLY_ONE,  ZERO_OR_MORE, ONE_OR_MORE },
        { null, EXACTLY_ONE,      EXACTLY_ONE,  ONE_OR_MORE,  ONE_OR_MORE }
    };

    final int ol = occs.length;
    for(int o = 0; o < ol; o++) {
      for(int p = 0; p < ol; p++) {
        assertSame(table[o][p], occs[o].intersect(occs[p]), "(" + o + ", " + p + ')');
      }
    }
  }

  /** Tests for {@link Occ#union(Occ)}. */
  @Test public void occUnion() {
    final Occ[] occs = { ZERO, ZERO_OR_ONE, EXACTLY_ONE, ZERO_OR_MORE, ONE_OR_MORE };
    final Occ[][] table = {
        { ZERO,      ZERO_OR_ONE,  ZERO_OR_ONE,  ZERO_OR_MORE, ZERO_OR_MORE },
        { ZERO_OR_ONE,  ZERO_OR_ONE,  ZERO_OR_ONE,  ZERO_OR_MORE, ZERO_OR_MORE },
        { ZERO_OR_ONE,  ZERO_OR_ONE,  EXACTLY_ONE,       ZERO_OR_MORE, ONE_OR_MORE  },
        { ZERO_OR_MORE, ZERO_OR_MORE, ZERO_OR_MORE, ZERO_OR_MORE, ZERO_OR_MORE },
        { ZERO_OR_MORE, ZERO_OR_MORE, ONE_OR_MORE,  ZERO_OR_MORE, ONE_OR_MORE  }
    };

    final int ol = occs.length;
    for(int o = 0; o < ol; o++) {
      for(int p = 0; p < ol; p++) {
        assertSame(table[o][p], occs[o].union(occs[p]), "(" + o + ", " + p + ')');
      }
    }
  }

  /** Tests for {@link Occ#instanceOf(Occ)}. */
  @Test public void occInstanceOf() {
    final Occ[] occs = { ZERO, ZERO_OR_ONE, EXACTLY_ONE, ZERO_OR_MORE, ONE_OR_MORE };

    assertTrue(EXACTLY_ONE.instanceOf(ZERO_OR_MORE));
    assertFalse(ZERO_OR_MORE.instanceOf(EXACTLY_ONE));
    final int bits = 0x014F90E1;

    final int ol = occs.length;
    for(int o = 0; o < ol; o++) {
      for(int p = 0; p < ol; p++) {
        final boolean inst = (bits >>> 5 * p + o & 1) != 0;
        assertEquals(inst, occs[o].instanceOf(occs[p]), "(" + o + ", " + p + ')');
      }
    }
  }

  /** Tests for {@link SeqType#instanceOf(SeqType)}. */
  @Test public void instanceOf() {
    // atomic items
    assertTrue(BOOLEAN_O.instanceOf(ANY_ATOMIC_TYPE_ZM));
    assertFalse(ANY_ATOMIC_TYPE_ZM.instanceOf(BOOLEAN_O));
    assertTrue(DOUBLE_O.instanceOf(DOUBLE_ZM));
    assertFalse(DOUBLE_ZM.instanceOf(DOUBLE_O));

    // functions
    final SeqType f = FuncType.get(DECIMAL_ZO, BOOLEAN_O).seqType();
    assertFalse(f.instanceOf(INTEGER_O));
    assertTrue(f.instanceOf(ITEM_O));
    assertTrue(f.instanceOf(FUNCTION_O));
    assertTrue(f.instanceOf(f));
    assertTrue(f.instanceOf(FUNCTION_ZO));
    assertFalse(FUNCTION_O.instanceOf(f));
    assertFalse(f.instanceOf(FuncType.get(DECIMAL_ZO, BOOLEAN_O, INTEGER_O).seqType()));
    assertFalse(f.instanceOf(FuncType.get(DECIMAL_ZO, ANY_ATOMIC_TYPE_O).seqType()));
    assertFalse(f.instanceOf(FuncType.get(BOOLEAN_O, BOOLEAN_O).seqType()));

    // maps
    final MapType m = MapType.get(STRING, INTEGER_O);
    assertTrue(m.instanceOf(m));
    assertTrue(m.instanceOf(ITEM));
    assertTrue(m.instanceOf(SeqType.FUNCTION));
    assertTrue(m.instanceOf(SeqType.MAP));
    assertTrue(m.instanceOf(MapType.get(ANY_ATOMIC_TYPE, INTEGER_O)));
    assertTrue(m.instanceOf(MapType.get(STRING, INTEGER_O)));
    assertTrue(m.instanceOf(MapType.get(STRING, INTEGER_ZO)));
    assertFalse(m.instanceOf(MapType.get(INTEGER, ITEM_ZM)));
    assertFalse(m.instanceOf(SeqType.ARRAY));
    assertFalse(m.instanceOf(MapType.get(STRING, BOOLEAN_O)));

    // arrays
    final ArrayType a = ArrayType.get(INTEGER_O);
    assertTrue(a.instanceOf(a));
    assertTrue(a.instanceOf(ITEM));
    assertTrue(a.instanceOf(SeqType.FUNCTION));
    assertTrue(a.instanceOf(SeqType.ARRAY));
    assertTrue(a.instanceOf(ArrayType.get(INTEGER_O)));
    assertTrue(a.instanceOf(ArrayType.get(INTEGER_O)));
    assertTrue(a.instanceOf(ArrayType.get(INTEGER_ZO)));
    assertFalse(a.instanceOf(SeqType.MAP));
    assertFalse(a.instanceOf(ArrayType.get(BOOLEAN_O)));

    // nodes
    assertTrue(ATTRIBUTE_O.instanceOf(NODE_O));
    assertTrue(ATTRIBUTE_O.instanceOf(ATTRIBUTE_O));
    assertFalse(ATTRIBUTE_O.instanceOf(ELEMENT_O));
    assertFalse(ELEMENT_O.instanceOf(f));
    assertFalse(NODE_O.instanceOf(ELEMENT_O));
    assertFalse(ITEM_O.instanceOf(ELEMENT_O));
    assertTrue(ELEMENT_O.instanceOf(ITEM_O));
  }

  /** Tests for {@link SeqType#union(SeqType)}. */
  @Test public void union() {
    final BiFunction<SeqType, SeqType, SeqType> op = SeqType::union;

    combine(EMPTY_SEQUENCE_Z, op);
    combine(STRING_O, op);
    combine(INTEGER_O, op);
    combine(ATTRIBUTE_O, op);
    combine(ITEM_O, op);
    combine(NORMALIZED_STRING.seqType(), op);
    combine(JAVA.seqType(), op);
    combine(ATTRIBUTE_O, op);
    combine(ELEMENT_O, op);
    combine(NODE_O, op);

    combine(STRING_O, INTEGER_O, ANY_ATOMIC_TYPE_O, op);
    combine(STRING_O, STRING_O, STRING_O, op);
    combine(STRING_O, ATTRIBUTE_O, ITEM_O, op);
    combine(NORMALIZED_STRING.seqType(), STRING_O, STRING_O, op);
    combine(STRING_O, NORMALIZED_STRING.seqType(), STRING_O, op);
    combine(STRING_O, JAVA.seqType(), ITEM_O, op);

    combine(ATTRIBUTE_O, ELEMENT_O, NODE_O, op);
    combine(NODE_O, ELEMENT_O, NODE_O, op);
    combine(ELEMENT_O, ELEMENT_O, ELEMENT_O, op);
    combine(ELEMENT_O, STRING_O, ITEM_O, op);

    combine(MAP_O, ITEM_O, ITEM_O, op);
    combine(MAP_O, FUNCTION_O, FUNCTION_O, op);
    combine(MAP_O, ARRAY_O, FUNCTION_O, op);

    // functions
    final SeqType
      // function(xs:boolean) as xs:decimal?
      f1 = FuncType.get(DECIMAL_ZO, BOOLEAN_O).seqType(),
      // function(xs:boolean) as xs:nonNegativeInteger
      f2 = FuncType.get(NON_NEGATIVE_INTEGER.seqType(), BOOLEAN_O).seqType(),
      // function(xs:boolean, xs:boolean) as xs:nonNegativeInteger
      f3 = FuncType.get(NON_NEGATIVE_INTEGER.seqType(), BOOLEAN_O, BOOLEAN_O).seqType(),
      // function(xs:integer) as xs:nonNegativeInteger
      f4 = FuncType.get(NON_NEGATIVE_INTEGER.seqType(), INTEGER_O).seqType(),
      // function(xs:boolean) as xs:integer
      f5 = FuncType.get(INTEGER_O, BOOLEAN_O).seqType();

    combine(f1, op);
    combine(f2, op);
    combine(f3, op);
    combine(f4, op);
    combine(f5, op);

    combine(f1, INTEGER_O, ITEM_O, op);
    combine(f1, FUNCTION_O, FUNCTION_O, op);
    combine(f1, f2, f1, op);
    combine(f1, f3, FUNCTION_O, op);
    combine(f1, f4, FUNCTION_O, op);
    combine(f1, f5, f1, op);
    combine(f2, f3, FUNCTION_O, op);
    combine(f2, f4, FUNCTION_O, op);
    combine(f2, f5, f5, op);
    combine(f3, f4, FUNCTION_O, op);
    combine(f3, f5, FUNCTION_O, op);
    combine(f4, f5, FUNCTION_O, op);

    final SeqType
      // map(xs:anyAtomicType, xs:integer)
      m1 = MapType.get(ANY_ATOMIC_TYPE, INTEGER_O).seqType(),
      // map(xs:boolean, xs:integer)
      m2 = MapType.get(BOOLEAN, INTEGER_O).seqType(),
      // map(xs:boolean, xs:nonNegativeInteger)
      m3 = MapType.get(BOOLEAN, NON_NEGATIVE_INTEGER.seqType()).seqType(),
      // map(xs:integer, xs:integer)
      m4 = MapType.get(INTEGER, INTEGER_O).seqType();

    combine(m1, op);
    combine(m2, op);
    combine(m3, op);
    combine(m4, op);

    combine(MAP_O, m1, MAP_O, op);
    combine(m1, INTEGER_O, ITEM_O, op);
    combine(m1, f1, f1, op);
    combine(m1, f2, f5, op);
    combine(m1, m2, m1, op);
    combine(m1, m3, m1, op);
    combine(m2, m4, m1, op);

    final SeqType
      // array(xs:integer)
      a1 = ArrayType.get(INTEGER_O).seqType(),
      // array(xs:integer)
      a2 = ArrayType.get(ANY_ATOMIC_TYPE_O).seqType(),
      // array(xs:nonNegativeInteger)
      a3 = ArrayType.get(NON_NEGATIVE_INTEGER.seqType()).seqType(),
      // array(xs:boolean)
      a4 = ArrayType.get(BOOLEAN_O).seqType();

    combine(a1, op);
    combine(a2, op);
    combine(a3, op);
    combine(a4, op);

    combine(ARRAY_O, a1, ARRAY_O, op);
    combine(a1, INTEGER_O, ITEM_O, op);
    combine(a1, a2, a2, op);
    combine(a1, a3, a1, op);
    combine(a1, f1, FUNCTION_O, op);
    combine(a1, f2, FUNCTION_O, op);
    combine(a2, a4, a2, op);
  }

  /** Tests for {@link SeqType#intersect(SeqType)}. */
  @Test public void intersect() {
    final BiFunction<SeqType, SeqType, SeqType> op = SeqType::intersect;

    combine(EMPTY_SEQUENCE_Z, op);
    combine(STRING_O, op);
    combine(INTEGER_O, op);
    combine(ATTRIBUTE_O, op);
    combine(ITEM_O, op);
    combine(NORMALIZED_STRING.seqType(), op);
    combine(JAVA.seqType(), op);
    combine(ATTRIBUTE_O, op);
    combine(ELEMENT_O, op);
    combine(NODE_O, op);

    combine(EMPTY_SEQUENCE_Z, ITEM_O, null, op);
    combine(ATTRIBUTE_O, ATTRIBUTE_O, ATTRIBUTE_O, op);
    combine(ATTRIBUTE_O, NODE_O, ATTRIBUTE_O, op);
    combine(ATTRIBUTE_O, ELEMENT_O, null, op);

    combine(MAP_O, ITEM_O, MAP_O, op);
    combine(MAP_O, FUNCTION_O, MAP_O, op);
    combine(MAP_O, ARRAY_O, null, op);

    // functions
    final SeqType
      // function(xs:boolean) as xs:decimal?
      f1 = FuncType.get(DECIMAL_ZO, BOOLEAN_O).seqType(),
      // function(xs:boolean) as xs:nonNegativeInteger
      f2 = FuncType.get(NON_NEGATIVE_INTEGER.seqType(), BOOLEAN_O).seqType(),
      // function(xs:boolean, xs:boolean) as xs:nonNegativeInteger
      f3 = FuncType.get(NON_NEGATIVE_INTEGER.seqType(), BOOLEAN_O, BOOLEAN_O).seqType(),
      // function(xs:integer) as xs:nonNegativeInteger
      f4 = FuncType.get(NON_NEGATIVE_INTEGER.seqType(), INTEGER_O).seqType(),
      // function(xs:boolean) as xs:integer
      f5 = FuncType.get(INTEGER_O, BOOLEAN_O).seqType(),
      // function(xs:boolean) as xs:boolean
      f6 = FuncType.get(BOOLEAN_O, BOOLEAN_O).seqType();

    combine(f1, op);
    combine(f2, op);
    combine(f3, op);
    combine(f4, op);
    combine(f5, op);
    combine(f6, op);

    combine(NODE_O, INTEGER_O, null, op);
    combine(f1, INTEGER_O, null, op);
    combine(f1, f1, f1, op);
    combine(f1, f2, f2, op);
    combine(f1, f5, f5, op);
    combine(f1, f4, FuncType.get(NON_NEGATIVE_INTEGER.seqType(), ANY_ATOMIC_TYPE_O).seqType(), op);
    combine(f2, f3, null, op);
    combine(f5, f6, null, op);

    final SeqType
      // map(xs:anyAtomicType, xs:integer)
      m1 = MapType.get(ANY_ATOMIC_TYPE, INTEGER_O).seqType(),
      // map(xs:boolean, xs:integer)
      m2 = MapType.get(BOOLEAN, INTEGER_O).seqType(),
      // map(xs:boolean, xs:nonNegativeInteger)
      m3 = MapType.get(BOOLEAN, NON_NEGATIVE_INTEGER.seqType()).seqType(),
      // map(xs:integer, xs:integer)
      m4 = MapType.get(INTEGER, INTEGER_O).seqType();

    combine(m1, op);
    combine(m2, op);
    combine(m3, op);
    combine(m4, op);

    combine(m1, f1, m1, op);
    combine(m1, ITEM_O, m1, op);
    combine(m1, INTEGER_O, null, op);
    combine(m1, m2, m2, op);
    combine(m2, MapType.get(BOOLEAN, BOOLEAN_O).seqType(), null, op);
    combine(m1, FUNCTION_O, m1, op);
    combine(m1, f3, null, op);
    combine(m1, f6, null, op);
    combine(m1, FuncType.get(INTEGER_O, ITEM_O).seqType(), null, op);
    combine(m1, m3,
        MapType.get(BOOLEAN, NON_NEGATIVE_INTEGER.seqType()).seqType(), op);
    combine(m2, m4, null, op);
    combine(m4, f5, m4, op);

    final SeqType
      // array(xs:integer)
      a1 = ArrayType.get(INTEGER_O).seqType(),
      // array(xs:integer)
      a2 = ArrayType.get(INTEGER_O).seqType(),
      // array(xs:nonNegativeInteger)
      a3 = ArrayType.get(NON_NEGATIVE_INTEGER.seqType()).seqType(),
      // array(xs:integer)
      a4 = ArrayType.get(INTEGER_O).seqType();

    combine(a1, op);
    combine(a2, op);
    combine(a3, op);
    combine(a4, op);

    combine(a1, ITEM_O, a1, op);
    combine(a1, INTEGER_O, null, op);
    combine(a1, a2, a1, op);
    combine(a1, a3, ArrayType.get(NON_NEGATIVE_INTEGER.seqType()).seqType(), op);
    combine(a2, a4, a1, op);
    combine(a2, ArrayType.get(BOOLEAN_O).seqType(), null, op);
    combine(a1, FUNCTION_O, a1, op);
    combine(a1, f3, null, op);
    combine(a1, f6, null, op);
    combine(a1, FuncType.get(ITEM_O).seqType(), null, op);
    combine(a1, f1, null, op);
    combine(a4, f5, null, op);
  }

  /**
   * Combines two sequences types.
   * @param st1 first type
   * @param st2 second type
   * @param expected expected result or {@code null}
   * @param func combining function
   */
  private static void combine(final SeqType st1, final SeqType st2,
      final SeqType expected, final BiFunction<SeqType, SeqType, SeqType> func) {

    final String message = "\nType 1: " + st1 + "\nType 2: " + st2 +
        "\nExpected: " + expected + "\nReturned: ";

    final SeqType result1 = func.apply(st1, st2), result2 = func.apply(st2, st1);
    if(result1 == null ^ result2 == null || result1 != null && !result1.eq(result2)) {
      fail("Operation is not commutative:" + message + result1 + " vs " + result2 + '\n');
    }

    final Consumer<SeqType> check = result -> {
      final String msg = message + result + '\n';
      if(expected == null) {
        assertNull(result, msg);
      } else {
        assertNotNull(result, msg);
        assertTrue(result.eq(expected), msg);
      }
    };
    check.accept(result1);
    check.accept(result2);
  }

  /**
   * Combines a sequence type with itself.
   * @param st sequence type
   * @param func combining function
   */
  private static void combine(final SeqType st, final BiFunction<SeqType, SeqType, SeqType> func) {
    final SeqType result = func.apply(st, st);
    final String msg = "\nType: " + st + "\nReturned: " + result + '\n';
    assertNotNull(result, msg);
    assertTrue(st.eq(result), msg);
  }
}
