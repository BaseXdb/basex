package org.basex.query;

import static org.basex.query.value.type.SeqType.*;
import static org.basex.query.value.type.SeqType.Occ.*;
import static org.junit.Assert.*;

import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.junit.*;

/**
 * Tests for the {@link SeqType} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class SeqTypeTest {
  /** Tests for {@link Occ#intersect(Occ)}. */
  @Test public void occIntersectTest() {
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
  @Test public void occUnionTest() {
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
  @Test public void occInstanceOfTest() {
    final Occ[] occs = { ZERO, ZERO_ONE, ONE, ZERO_MORE, ONE_MORE };

    assertTrue(Occ.ONE.instanceOf(ZERO_MORE));
    assertFalse(Occ.ZERO_MORE.instanceOf(ONE));
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
  @Test public void instanceOfTest() {
    assertTrue(BLN.instanceOf(AAT_ZM));
    assertFalse(AAT_ZM.instanceOf(BLN));
    assertTrue(DBL.instanceOf(DBL_ZM));
    assertFalse(DBL_ZM.instanceOf(DBL));

    // functions
    final SeqType f = FuncType.get(DEC_ZO, BLN).seqType();
    assertFalse(f.instanceOf(ITR));
    assertTrue(f.instanceOf(ITEM));
    assertTrue(f.instanceOf(f));
    assertTrue(f.instanceOf(FUN_OZ));
    assertFalse(FUN_O.instanceOf(f));
    assertFalse(f.instanceOf(FuncType.get(DEC_ZO, BLN, ITR).seqType()));
    assertFalse(f.instanceOf(FuncType.get(DEC_ZO, AAT).seqType()));
    assertFalse(f.instanceOf(FuncType.get(BLN, BLN).seqType()));

    // maps
    final SeqType m = MapType.get(AtomType.AAT, ITR).seqType();
    assertTrue(m.instanceOf(f));
    assertFalse(f.instanceOf(m));
    assertFalse(MAP_O.instanceOf(m));
    assertTrue(m.instanceOf(MAP_O));
    assertFalse(m.instanceOf(MapType.get(AtomType.AAT, BLN).seqType()));
    assertFalse(MapType.get(AtomType.BLN, ITR).seqType().instanceOf(m));

    /* arrays
    final SeqType a = ArrayType.get(ITR).seqType();
    assertTrue(a.instanceOf(f));
    assertFalse(f.instanceOf(a));
    assertFalse(ARRAY_O.instanceOf(a));
    assertTrue(a.instanceOf(ARRAY_O));
    assertFalse(a.instanceOf(ArrayType.get(BLN).seqType()));
    assertFalse(ArrayType.get(ITR).seqType().instanceOf(a));
    */

    // nodes
    assertTrue(ATT.instanceOf(NOD));
    assertTrue(ATT.instanceOf(ATT));
    assertFalse(ATT.instanceOf(ELM));
    assertFalse(ELM.instanceOf(f));
    assertFalse(NOD.instanceOf(ELM));
    assertFalse(ITEM.instanceOf(ELM));
    assertTrue(ELM.instanceOf(ITEM));
  }

  /** Tests for {@link SeqType#union(SeqType)}. */
  @Test public void unionTest() {
    assertTrue(STR.union(ITR).eq(AAT));
    assertTrue(STR.union(STR).eq(STR));
    assertTrue(STR.union(ATT).eq(ITEM));
    assertTrue(AtomType.NST.seqType().union(STR).eq(STR));
    assertTrue(STR.union(AtomType.NST.seqType()).eq(STR));
    assertTrue(STR.union(AtomType.JAVA.seqType()).eq(ITEM));

    assertTrue(ATT.union(ELM).eq(NOD));
    assertTrue(NOD.union(ELM).eq(NOD));
    assertTrue(ELM.union(NOD).eq(NOD));
    assertTrue(ELM.union(ELM).eq(ELM));
    assertTrue(ELM.union(STR).eq(ITEM));

    // functions
    final SeqType
      // function(xs:boolean) as xs:decimal?
      f = FuncType.get(DEC_ZO, BLN).seqType(),
      // function(xs:boolean) as xs:nonNegativeInteger
      f2 = FuncType.get(AtomType.NNI.seqType(), BLN).seqType(),
      // function(xs:boolean, xs:boolean) as xs:nonNegativeInteger
      f3 = FuncType.get(AtomType.NNI.seqType(), BLN, BLN).seqType(),
      // function(xs:integer) as xs:nonNegativeInteger
      f4 = FuncType.get(AtomType.NNI.seqType(), ITR).seqType(),
      // function(xs:boolean) as xs:integer
      f5 = FuncType.get(ITR, BLN).seqType();

    union(f, ITR, ITEM);
    union(f, FUN_O, FUN_O);
    union(f2, f3, FUN_O);
    union(f2, f4, FUN_O);

    // maps
    final SeqType
      // map(xs:anyAtomicType, xs:integer)
      m = MapType.get(AtomType.AAT, ITR).seqType(),
      // map(xs:boolean, xs:integer)
      m2 = MapType.get(AtomType.BLN, ITR).seqType(),
      // map(xs:boolean, xs:nonNegativeInteger)
      m3 = MapType.get(AtomType.BLN, AtomType.NNI.seqType()).seqType(),
      // map(xs:integer, xs:integer)
      m4 = MapType.get(AtomType.ITR, ITR).seqType();

    union(MAP_O, m, MAP_O);
    union(m, ITR, ITEM);
    union(m, f, f);
    union(m, f2, f5);
    union(m, m2, m2);
    union(m, m3, m2);
    union(m2, m4, FUN_O);

    /* arrays
    final SeqType
      // array(xs:integer)
      a = ArrayType.get(ITR).seqType(),
      // array(xs:integer)
      a2 = ArrayType.get(ITR).seqType(),
      // array(xs:nonNegativeInteger)
      a3 = ArrayType.get(AtomType.NNI.seqType()).seqType(),
      // array(xs:integer)
      a4 = ArrayType.get(ITR).seqType();

    union(ARRAY_O, a, ARRAY_O);
    union(a, ITR, ITEM);
    union(a, f, f);
    union(a, f2, f5);
    union(a, a2, a2);
    union(a, a3, a2);
    union(a2, a4, FUN_O);
    */
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
  @Test public void intersectTest() {
    // functions
    final SeqType
      // function(xs:boolean) as xs:decimal?
      f = FuncType.get(DEC_ZO, BLN).seqType(),
      // function(xs:boolean) as xs:nonNegativeInteger
      f2 = FuncType.get(AtomType.NNI.seqType(), BLN).seqType(),
      // function(xs:boolean, xs:boolean) as xs:nonNegativeInteger
      f3 = FuncType.get(AtomType.NNI.seqType(), BLN, BLN).seqType(),
      // function(xs:integer) as xs:nonNegativeInteger
      f4 = FuncType.get(AtomType.NNI.seqType(), ITR).seqType(),
      // function(xs:boolean) as xs:integer
      f5 = FuncType.get(ITR, BLN).seqType(),
      // function(xs:boolean) as xs:boolean
      f6 = FuncType.get(BLN, BLN).seqType();

    intersect(get(AtomType.ITEM, 0), ITEM, null);
    intersect(ATT, ATT, ATT);
    intersect(ATT, NOD, ATT);
    intersect(ATT, ELM, null);
    intersect(NOD, ITR, null);
    intersect(f, ITR, null);
    intersect(f, f, f);
    intersect(f, f2, f2);
    intersect(f, f5, f5);
    intersect(f, f4, FuncType.get(AtomType.NNI.seqType(), AAT).seqType());
    intersect(f2, f3, null);
    intersect(f5, f6, null);

    // maps
    final SeqType
      // map(xs:anyAtomicType, xs:integer)
      m = MapType.get(AtomType.AAT, ITR).seqType(),
      // map(xs:boolean, xs:integer)
      m2 = MapType.get(AtomType.BLN, ITR).seqType(),
      // map(xs:boolean, xs:nonNegativeInteger)
      m3 = MapType.get(AtomType.BLN, AtomType.NNI.seqType()).seqType(),
      // map(xs:integer, xs:integer)
      m4 = MapType.get(AtomType.ITR, ITR).seqType();

    intersect(m, f, m);
    intersect(m, ITEM, m);
    intersect(m, ITR, null);
    intersect(m, m2, m);
    intersect(m, m3, MapType.get(AtomType.AAT, AtomType.NNI.seqType()).seqType());
    intersect(m2, m4, m);
    intersect(m2, MapType.get(AtomType.BLN, BLN).seqType(), null);
    intersect(m, FUN_O, m);
    intersect(m, f, m);
    intersect(m4, f5, m);
    intersect(m, f3, null);
    intersect(m, f6, null);
    intersect(m, FuncType.get(ITR, ITEM).seqType(), null);

    /* arrays
    final SeqType
      // array(xs:integer)
      a = ArrayType.get(ITR).seqType(),
      // array(xs:integer)
      a2 = ArrayType.get(ITR).seqType(),
      // array(xs:nonNegativeInteger)
      a3 = ArrayType.get(AtomType.NNI.seqType()).seqType(),
      // array(xs:integer)
      a4 = ArrayType.get(ITR).seqType();

    intersect(a, ITEM, a);
    intersect(a, ITR, null);
    intersect(a, a2, a);
    intersect(a, a3, ArrayType.get(AtomType.NNI.seqType()).seqType());
    intersect(a2, a4, a);
    intersect(a2, ArrayType.get(BLN).seqType(), null);
    intersect(a, FUN_O, a);
    intersect(a, f, a);
    intersect(a4, f5, a);
    intersect(a, f3, null);
    intersect(a, f6, null);
    intersect(a, FuncType.get(ITEM).seqType(), null);
    */
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
