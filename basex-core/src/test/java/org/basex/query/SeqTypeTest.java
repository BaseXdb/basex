package org.basex.query;

import static org.basex.query.value.type.Occ.*;
import static org.basex.query.value.type.SeqType.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.*;

import org.basex.query.value.type.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link SeqType} class.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Leo Woerteler
 */
public final class SeqTypeTest {
  /** Tests for {@link Occ#intersect(Occ)}. */
  @Test public void occIntersect() {
    final Occ[] occs = { ZERO, ZERO_ONE, ONE, ZERO_MORE, ONE_MORE };
    final Occ[][] table = {
        { ZERO, ZERO,     null, ZERO,      null     },
        { ZERO, ZERO_ONE, ONE,  ZERO_ONE,  ONE      },
        { null, ONE,      ONE,  ONE,       ONE      },
        { ZERO, ZERO_ONE, ONE,  ZERO_MORE, ONE_MORE },
        { null, ONE,      ONE,  ONE_MORE,  ONE_MORE }
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
    final Occ[] occs = { ZERO, ZERO_ONE, ONE, ZERO_MORE, ONE_MORE };
    final Occ[][] table = {
        { ZERO,      ZERO_ONE,  ZERO_ONE,  ZERO_MORE, ZERO_MORE },
        { ZERO_ONE,  ZERO_ONE,  ZERO_ONE,  ZERO_MORE, ZERO_MORE },
        { ZERO_ONE,  ZERO_ONE,  ONE,       ZERO_MORE, ONE_MORE  },
        { ZERO_MORE, ZERO_MORE, ZERO_MORE, ZERO_MORE, ZERO_MORE },
        { ZERO_MORE, ZERO_MORE, ONE_MORE,  ZERO_MORE, ONE_MORE  }
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
    final Occ[] occs = { ZERO, ZERO_ONE, ONE, ZERO_MORE, ONE_MORE };

    assertTrue(ONE.instanceOf(ZERO_MORE));
    assertFalse(ZERO_MORE.instanceOf(ONE));
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
    assertTrue(BLN_O.instanceOf(AAT_ZM));
    assertFalse(AAT_ZM.instanceOf(BLN_O));
    assertTrue(DBL_O.instanceOf(DBL_ZM));
    assertFalse(DBL_ZM.instanceOf(DBL_O));

    // functions
    final SeqType f = FuncType.get(DEC_ZO, BLN_O).seqType();
    assertFalse(f.instanceOf(ITR_O));
    assertTrue(f.instanceOf(ITEM_O));
    assertTrue(f.instanceOf(FUNC_O));
    assertTrue(f.instanceOf(f));
    assertTrue(f.instanceOf(FUNC_ZO));
    assertFalse(FUNC_O.instanceOf(f));
    assertFalse(f.instanceOf(FuncType.get(DEC_ZO, BLN_O, ITR_O).seqType()));
    assertFalse(f.instanceOf(FuncType.get(DEC_ZO, AAT_O).seqType()));
    assertFalse(f.instanceOf(FuncType.get(BLN_O, BLN_O).seqType()));

    // maps
    final MapType m = MapType.get(AtomType.STR, ITR_O);
    assertTrue(m.instanceOf(m));
    assertTrue(m.instanceOf(AtomType.ITEM));
    assertTrue(m.instanceOf(ANY_FUNC));
    assertTrue(m.instanceOf(ANY_MAP));
    assertTrue(m.instanceOf(MapType.get(AtomType.AAT, ITR_O)));
    assertTrue(m.instanceOf(MapType.get(AtomType.STR, ITR_O)));
    assertTrue(m.instanceOf(MapType.get(AtomType.STR, ITR_ZO)));
    assertFalse(m.instanceOf(MapType.get(AtomType.ITR, ITEM_ZM)));
    assertFalse(m.instanceOf(ANY_ARRAY));
    assertFalse(m.instanceOf(MapType.get(AtomType.STR, BLN_O)));

    final ArrayType a = ArrayType.get(ITR_O);
    assertTrue(a.instanceOf(a));
    assertTrue(a.instanceOf(AtomType.ITEM));
    assertTrue(a.instanceOf(ANY_FUNC));
    assertTrue(a.instanceOf(ANY_ARRAY));
    assertTrue(a.instanceOf(ArrayType.get(ITR_O)));
    assertTrue(a.instanceOf(ArrayType.get(ITR_O)));
    assertTrue(a.instanceOf(ArrayType.get(ITR_ZO)));
    assertFalse(a.instanceOf(ANY_MAP));
    assertFalse(a.instanceOf(ArrayType.get(BLN_O)));

    // nodes
    assertTrue(ATT_O.instanceOf(NOD_O));
    assertTrue(ATT_O.instanceOf(ATT_O));
    assertFalse(ATT_O.instanceOf(ELM_O));
    assertFalse(ELM_O.instanceOf(f));
    assertFalse(NOD_O.instanceOf(ELM_O));
    assertFalse(ITEM_O.instanceOf(ELM_O));
    assertTrue(ELM_O.instanceOf(ITEM_O));
  }

  /** Tests for {@link SeqType#union(SeqType)}. */
  @Test public void union() {
    final BiFunction<SeqType, SeqType, SeqType> op = SeqType::union;

    combine(EMP, op);
    combine(STR_O, op);
    combine(ITR_O, op);
    combine(ATT_O, op);
    combine(ITEM_O, op);
    combine(AtomType.NST.seqType(), op);
    combine(AtomType.JAVA.seqType(), op);
    combine(ATT_O, op);
    combine(ELM_O, op);
    combine(NOD_O, op);

    combine(STR_O, ITR_O, AAT_O, op);
    combine(STR_O, STR_O, STR_O, op);
    combine(STR_O, ATT_O, ITEM_O, op);
    combine(AtomType.NST.seqType(), STR_O, STR_O, op);
    combine(STR_O, AtomType.NST.seqType(), STR_O, op);
    combine(STR_O, AtomType.JAVA.seqType(), ITEM_O, op);

    combine(ATT_O, ELM_O, NOD_O, op);
    combine(NOD_O, ELM_O, NOD_O, op);
    combine(ELM_O, ELM_O, ELM_O, op);
    combine(ELM_O, STR_O, ITEM_O, op);

    combine(MAP_O, ITEM_O, ITEM_O, op);
    combine(MAP_O, FUNC_O, FUNC_O, op);
    combine(MAP_O, ARRAY_O, FUNC_O, op);

    // functions
    final SeqType
      // function(xs:boolean) as xs:decimal?
      f1 = FuncType.get(DEC_ZO, BLN_O).seqType(),
      // function(xs:boolean) as xs:nonNegativeInteger
      f2 = FuncType.get(AtomType.NNI.seqType(), BLN_O).seqType(),
      // function(xs:boolean, xs:boolean) as xs:nonNegativeInteger
      f3 = FuncType.get(AtomType.NNI.seqType(), BLN_O, BLN_O).seqType(),
      // function(xs:integer) as xs:nonNegativeInteger
      f4 = FuncType.get(AtomType.NNI.seqType(), ITR_O).seqType(),
      // function(xs:boolean) as xs:integer
      f5 = FuncType.get(ITR_O, BLN_O).seqType();

    combine(f1, op);
    combine(f2, op);
    combine(f3, op);
    combine(f4, op);
    combine(f5, op);

    combine(f1, ITR_O, ITEM_O, op);
    combine(f1, FUNC_O, FUNC_O, op);
    combine(f1, f2, f1, op);
    combine(f1, f3, FUNC_O, op);
    combine(f1, f4, FUNC_O, op);
    combine(f1, f5, f1, op);
    combine(f2, f3, FUNC_O, op);
    combine(f2, f4, FUNC_O, op);
    combine(f2, f5, f5, op);
    combine(f3, f4, FUNC_O, op);
    combine(f3, f5, FUNC_O, op);
    combine(f4, f5, FUNC_O, op);

    final SeqType
      // map(xs:anyAtomicType, xs:integer)
      m1 = MapType.get(AtomType.AAT, ITR_O).seqType(),
      // map(xs:boolean, xs:integer)
      m2 = MapType.get(AtomType.BLN, ITR_O).seqType(),
      // map(xs:boolean, xs:nonNegativeInteger)
      m3 = MapType.get(AtomType.BLN, AtomType.NNI.seqType()).seqType(),
      // map(xs:integer, xs:integer)
      m4 = MapType.get(AtomType.ITR, ITR_O).seqType();

    combine(m1, op);
    combine(m2, op);
    combine(m3, op);
    combine(m4, op);

    combine(MAP_O, m1, MAP_O, op);
    combine(m1, ITR_O, ITEM_O, op);
    combine(m1, f1, f1, op);
    combine(m1, f2, f5, op);
    combine(m1, m2, m1, op);
    combine(m1, m3, m1, op);
    combine(m2, m4, m1, op);

    final SeqType
      // array(xs:integer)
      a1 = ArrayType.get(ITR_O).seqType(),
      // array(xs:integer)
      a2 = ArrayType.get(AAT_O).seqType(),
      // array(xs:nonNegativeInteger)
      a3 = ArrayType.get(AtomType.NNI.seqType()).seqType(),
      // array(xs:boolean)
      a4 = ArrayType.get(BLN_O).seqType();

    combine(a1, op);
    combine(a2, op);
    combine(a3, op);
    combine(a4, op);

    combine(ARRAY_O, a1, ARRAY_O, op);
    combine(a1, ITR_O, ITEM_O, op);
    combine(a1, a2, a2, op);
    combine(a1, a3, a1, op);
    combine(a1, f1, FUNC_O, op);
    combine(a1, f2, FUNC_O, op);
    combine(a2, a4, a2, op);
  }

  /** Tests for {@link SeqType#intersect(SeqType)}. */
  @Test public void intersect() {
    final BiFunction<SeqType, SeqType, SeqType> op = SeqType::intersect;

    combine(EMP, op);
    combine(STR_O, op);
    combine(ITR_O, op);
    combine(ATT_O, op);
    combine(ITEM_O, op);
    combine(AtomType.NST.seqType(), op);
    combine(AtomType.JAVA.seqType(), op);
    combine(ATT_O, op);
    combine(ELM_O, op);
    combine(NOD_O, op);

    combine(EMP, ITEM_O, null, op);
    combine(ATT_O, ATT_O, ATT_O, op);
    combine(ATT_O, NOD_O, ATT_O, op);
    combine(ATT_O, ELM_O, null, op);

    combine(MAP_O, ITEM_O, MAP_O, op);
    combine(MAP_O, FUNC_O, MAP_O, op);
    combine(MAP_O, ARRAY_O, null, op);

    // functions
    final SeqType
      // function(xs:boolean) as xs:decimal?
      f1 = FuncType.get(DEC_ZO, BLN_O).seqType(),
      // function(xs:boolean) as xs:nonNegativeInteger
      f2 = FuncType.get(AtomType.NNI.seqType(), BLN_O).seqType(),
      // function(xs:boolean, xs:boolean) as xs:nonNegativeInteger
      f3 = FuncType.get(AtomType.NNI.seqType(), BLN_O, BLN_O).seqType(),
      // function(xs:integer) as xs:nonNegativeInteger
      f4 = FuncType.get(AtomType.NNI.seqType(), ITR_O).seqType(),
      // function(xs:boolean) as xs:integer
      f5 = FuncType.get(ITR_O, BLN_O).seqType(),
      // function(xs:boolean) as xs:boolean
      f6 = FuncType.get(BLN_O, BLN_O).seqType();

    combine(f1, op);
    combine(f2, op);
    combine(f3, op);
    combine(f4, op);
    combine(f5, op);
    combine(f6, op);

    combine(NOD_O, ITR_O, null, op);
    combine(f1, ITR_O, null, op);
    combine(f1, f1, f1, op);
    combine(f1, f2, f2, op);
    combine(f1, f5, f5, op);
    combine(f1, f4, FuncType.get(AtomType.NNI.seqType(), AAT_O).seqType(), op);
    combine(f2, f3, null, op);
    combine(f5, f6, null, op);

    final SeqType
      // map(xs:anyAtomicType, xs:integer)
      m1 = MapType.get(AtomType.AAT, ITR_O).seqType(),
      // map(xs:boolean, xs:integer)
      m2 = MapType.get(AtomType.BLN, ITR_O).seqType(),
      // map(xs:boolean, xs:nonNegativeInteger)
      m3 = MapType.get(AtomType.BLN, AtomType.NNI.seqType()).seqType(),
      // map(xs:integer, xs:integer)
      m4 = MapType.get(AtomType.ITR, ITR_O).seqType();

    combine(m1, op);
    combine(m2, op);
    combine(m3, op);
    combine(m4, op);

    combine(m1, f1, m1, op);
    combine(m1, ITEM_O, m1, op);
    combine(m1, ITR_O, null, op);
    combine(m1, m2, m2, op);
    combine(m2, MapType.get(AtomType.BLN, BLN_O).seqType(), null, op);
    combine(m1, FUNC_O, m1, op);
    combine(m1, f3, null, op);
    combine(m1, f6, null, op);
    combine(m1, FuncType.get(ITR_O, ITEM_O).seqType(), null, op);
    combine(m1, m3, MapType.get(AtomType.BLN, AtomType.NNI.seqType()).seqType(), op);
    combine(m2, m4, null, op);
    combine(m4, f5, m4, op);

    final SeqType
      // array(xs:integer)
      a1 = ArrayType.get(ITR_O).seqType(),
      // array(xs:integer)
      a2 = ArrayType.get(ITR_O).seqType(),
      // array(xs:nonNegativeInteger)
      a3 = ArrayType.get(AtomType.NNI.seqType()).seqType(),
      // array(xs:integer)
      a4 = ArrayType.get(ITR_O).seqType();

    combine(a1, op);
    combine(a2, op);
    combine(a3, op);
    combine(a4, op);

    combine(a1, ITEM_O, a1, op);
    combine(a1, ITR_O, null, op);
    combine(a1, a2, a1, op);
    combine(a1, a3, ArrayType.get(AtomType.NNI.seqType()).seqType(), op);
    combine(a2, a4, a1, op);
    combine(a2, ArrayType.get(BLN_O).seqType(), null, op);
    combine(a1, FUNC_O, a1, op);
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
