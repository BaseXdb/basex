package org.basex.query;

import static org.basex.query.value.type.Occ.*;
import static org.basex.query.value.type.SeqType.*;
import static org.junit.Assert.*;

import org.basex.query.value.type.*;
import org.junit.*;

/**
 * Tests for the {@link SeqType} class.
 *
 * @author BaseX Team 2005-19, BSD License
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
        assertSame("(" + o + ", " + p + ')', table[o][p], occs[o].intersect(occs[p]));
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
        assertSame("(" + o + ", " + p + ')', table[o][p], occs[o].union(occs[p]));
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
        assertEquals("(" + o + ", " + p + ')', inst, occs[o].instanceOf(occs[p]));
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
    assertTrue(m.instanceOf(MapType.get(AtomType.ITR, ITEM_ZM)));
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
    assertTrue(STR_O.union(ITR_O).eq(AAT_O));
    assertTrue(STR_O.union(STR_O).eq(STR_O));
    assertTrue(STR_O.union(ATT_O).eq(ITEM_O));
    assertTrue(AtomType.NST.seqType().union(STR_O).eq(STR_O));
    assertTrue(STR_O.union(AtomType.NST.seqType()).eq(STR_O));
    assertTrue(STR_O.union(AtomType.JAVA.seqType()).eq(ITEM_O));

    assertTrue(ATT_O.union(ELM_O).eq(NOD_O));
    assertTrue(NOD_O.union(ELM_O).eq(NOD_O));
    assertTrue(ELM_O.union(NOD_O).eq(NOD_O));
    assertTrue(ELM_O.union(ELM_O).eq(ELM_O));
    assertTrue(ELM_O.union(STR_O).eq(ITEM_O));

    // functions
    final SeqType
      // function(xs:boolean) as xs:decimal?
      f = FuncType.get(DEC_ZO, BLN_O).seqType(),
      // function(xs:boolean) as xs:nonNegativeInteger
      f2 = FuncType.get(AtomType.NNI.seqType(), BLN_O).seqType(),
      // function(xs:boolean, xs:boolean) as xs:nonNegativeInteger
      f3 = FuncType.get(AtomType.NNI.seqType(), BLN_O, BLN_O).seqType(),
      // function(xs:integer) as xs:nonNegativeInteger
      f4 = FuncType.get(AtomType.NNI.seqType(), ITR_O).seqType(),
      // function(xs:boolean) as xs:integer
      f5 = FuncType.get(ITR_O, BLN_O).seqType();

    union(f, ITR_O, ITEM_O);
    union(f, FUNC_O, FUNC_O);
    union(f2, f3, FUNC_O);
    union(f2, f4, FUNC_O);

    final SeqType
      // map(xs:anyAtomicType, xs:integer)
      m = MapType.get(AtomType.AAT, ITR_O).seqType(),
      // map(xs:boolean, xs:integer)
      m2 = MapType.get(AtomType.BLN, ITR_O).seqType();
      // map(xs:boolean, xs:nonNegativeInteger)
      //m3 = MapType.get(AtomType.BLN, AtomType.NNI.seqType()).seqType(),
      // map(xs:integer, xs:integer)
      //m4 = MapType.get(AtomType.ITR, ITR_O).seqType();

    union(MAP_O, m, MAP_O);
    union(m, ITR_O, ITEM_O);
    union(m, f, f);
    union(m, f2, f5);
    union(m, m2, m2);
    //union(m, m3, m2);
    //union(m2, m4, FUNC_O);

    final SeqType
      // array(xs:integer)
      a = ArrayType.get(ITR_O).seqType(),
      // array(xs:integer)
      a2 = ArrayType.get(ITR_O).seqType(),
      // array(xs:nonNegativeInteger)
      a3 = ArrayType.get(AtomType.NNI.seqType()).seqType();
      // array(xs:integer)
      //a4 = ArrayType.get(ITR_O).seqType();

    union(ARRAY_O, a, ARRAY_O);
    union(a, ITR_O, ITEM_O);
    union(a, a2, a2);
    union(a, a3, a2);

    //union(a, f, f);
    //union(a, f2, f5);
    //union(a2, a4, FUNC_O);
  }

  /**
   * Union test method.
   * @param st1 one argument
   * @param st2 other argument
   * @param expected result
   */
  private static void union(final SeqType st1, final SeqType st2, final SeqType expected) {
    eq(st1.union(st2), expected);
    eq(st2.union(st1), expected);
  }

  /** Tests for {@link SeqType#intersect(SeqType)}. */
  @Test public void intersect() {
    // functions
    final SeqType
      // function(xs:boolean) as xs:decimal?
      f = FuncType.get(DEC_ZO, BLN_O).seqType(),
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

    intersect(get(AtomType.ITEM, ZERO), ITEM_O, null);
    intersect(ATT_O, ATT_O, ATT_O);
    intersect(ATT_O, NOD_O, ATT_O);
    intersect(ATT_O, ELM_O, null);
    intersect(NOD_O, ITR_O, null);
    intersect(f, ITR_O, null);
    intersect(f, f, f);
    intersect(f, f2, f2);
    intersect(f, f5, f5);
    intersect(f, f4, FuncType.get(AtomType.NNI.seqType(), AAT_O).seqType());
    intersect(f2, f3, null);
    intersect(f5, f6, null);

    final SeqType
      // map(xs:anyAtomicType, xs:integer)
      m = MapType.get(AtomType.AAT, ITR_O).seqType(),
      // map(xs:boolean, xs:integer)
      m2 = MapType.get(AtomType.BLN, ITR_O).seqType();
      // map(xs:boolean, xs:nonNegativeInteger)
      //m3 = MapType.get(AtomType.BLN, AtomType.NNI.seqType()).seqType(),
      // map(xs:integer, xs:integer)
      //m4 = MapType.get(AtomType.ITR, ITR_O).seqType();

    intersect(m, f, m);
    intersect(m, ITEM_O, m);
    intersect(m, ITR_O, null);
    intersect(m, m2, m);
    intersect(m2, MapType.get(AtomType.BLN, BLN_O).seqType(), null);
    intersect(m, FUNC_O, m);
    intersect(m, f, m);
    intersect(m, f3, null);
    intersect(m, f6, null);
    intersect(m, FuncType.get(ITR_O, ITEM_O).seqType(), null);

    //intersect(m, m3, MapType.get(AtomType.AAT, AtomType.NNI.seqType()).seqType());
    //intersect(m2, m4, m);
    //intersect(m4, f5, m);

    final SeqType
      // array(xs:integer)
      a = ArrayType.get(ITR_O).seqType(),
      // array(xs:integer)
      a2 = ArrayType.get(ITR_O).seqType(),
      // array(xs:nonNegativeInteger)
      a3 = ArrayType.get(AtomType.NNI.seqType()).seqType(),
      // array(xs:integer)
      a4 = ArrayType.get(ITR_O).seqType();

    intersect(a, ITEM_O, a);
    intersect(a, ITR_O, null);
    intersect(a, a2, a);
    intersect(a, a3, ArrayType.get(AtomType.NNI.seqType()).seqType());
    intersect(a2, a4, a);
    intersect(a2, ArrayType.get(BLN_O).seqType(), null);
    intersect(a, FUNC_O, a);
    intersect(a, f3, null);
    intersect(a, f6, null);
    intersect(a, FuncType.get(ITEM_O).seqType(), null);

    //intersect(a, f, a);
    //intersect(a4, f5, a);
  }

  /**
   * Intersect test method.
   * @param st1 one argument
   * @param st2 other argument
   * @param expected expected result or {@code null}
   */
  private static void intersect(final SeqType st1, final SeqType st2, final SeqType expected) {
    eq(st1.intersect(st2), expected);
    eq(st2.intersect(st1), expected);
  }

  /**
   * Intersect test method.
   * @param s returned type
   * @param r expected result or {@code null}
   */
  private static void eq(final SeqType s, final SeqType r) {
    if(r == null) {
      assertNull("\nExpected: null\nReturned: " + s, s);
    } else {
      assertNotNull("\nExpected: " + r + "\nReturned: " + s, s);
      assertTrue("\nExpected: " + r + "\nReturned: " + s, s.eq(r));
    }
  }
}
