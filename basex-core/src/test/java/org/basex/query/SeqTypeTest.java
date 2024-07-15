package org.basex.query;

import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.Occ.*;
import static org.basex.query.value.type.SeqType.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.value.type.*;
import org.basex.query.value.type.RecordType.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link SeqType} class.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class SeqTypeTest {
  /** Occurrences. */
  private static final Occ[] OCCS = { ZERO, ZERO_OR_ONE, EXACTLY_ONE, ZERO_OR_MORE, ONE_OR_MORE };

  /** Tests for {@link Occ#intersect(Occ)}. */
  @Test public void occIntersect() {
    final Occ[][] table = {
      { ZERO, ZERO,        null,        ZERO,         null        },
      { ZERO, ZERO_OR_ONE, EXACTLY_ONE, ZERO_OR_ONE,  EXACTLY_ONE },
      { null, EXACTLY_ONE, EXACTLY_ONE, EXACTLY_ONE,  EXACTLY_ONE },
      { ZERO, ZERO_OR_ONE, EXACTLY_ONE, ZERO_OR_MORE, ONE_OR_MORE },
      { null, EXACTLY_ONE, EXACTLY_ONE, ONE_OR_MORE,  ONE_OR_MORE }
    };
    compute(table, Occ::intersect);
  }

  /** Tests for {@link Occ#union(Occ)}. */
  @Test public void occUnion() {
    final Occ[][] table = {
      { ZERO,         ZERO_OR_ONE,  ZERO_OR_ONE,  ZERO_OR_MORE, ZERO_OR_MORE },
      { ZERO_OR_ONE,  ZERO_OR_ONE,  ZERO_OR_ONE,  ZERO_OR_MORE, ZERO_OR_MORE },
      { ZERO_OR_ONE,  ZERO_OR_ONE,  EXACTLY_ONE,  ZERO_OR_MORE, ONE_OR_MORE  },
      { ZERO_OR_MORE, ZERO_OR_MORE, ZERO_OR_MORE, ZERO_OR_MORE, ZERO_OR_MORE },
      { ZERO_OR_MORE, ZERO_OR_MORE, ONE_OR_MORE,  ZERO_OR_MORE, ONE_OR_MORE  }
    };
    compute(table, Occ::union);
  }

  /** Tests for {@link Occ#add(Occ)}. */
  @Test public void occAdd() {
    final Occ[][] table = {
      { ZERO,         ZERO_OR_ONE,  EXACTLY_ONE,  ZERO_OR_MORE, ONE_OR_MORE },
      { ZERO_OR_ONE,  ZERO_OR_MORE, ONE_OR_MORE,  ZERO_OR_MORE, ONE_OR_MORE },
      { EXACTLY_ONE,  ONE_OR_MORE,  ONE_OR_MORE,  ONE_OR_MORE,  ONE_OR_MORE },
      { ZERO_OR_MORE, ZERO_OR_MORE, ONE_OR_MORE,  ZERO_OR_MORE, ONE_OR_MORE },
      { ONE_OR_MORE,  ONE_OR_MORE,  ONE_OR_MORE,  ONE_OR_MORE,  ONE_OR_MORE }
    };
    compute(table, Occ::add);
  }

  /** Tests for {@link Occ#multiply(Occ)}. */
  @Test public void occMultiply() {
    final Occ[][] table = {
      { ZERO, ZERO,         ZERO,         ZERO,         ZERO         },
      { ZERO, ZERO_OR_ONE,  ZERO_OR_ONE,  ZERO_OR_MORE, ZERO_OR_MORE },
      { ZERO, ZERO_OR_ONE,  EXACTLY_ONE,  ZERO_OR_MORE, ONE_OR_MORE  },
      { ZERO, ZERO_OR_MORE, ZERO_OR_MORE, ZERO_OR_MORE, ZERO_OR_MORE },
      { ZERO, ZERO_OR_MORE, ONE_OR_MORE,  ZERO_OR_MORE, ONE_OR_MORE  }
    };
    compute(table, Occ::multiply);
  }

  /**
   * Computes occurrences.
   * @param table result table
   * @param func function for computing the result
   */
  private static void compute(final Occ[][] table, final BiFunction<Occ, Occ, Occ> func) {
    final int ol = OCCS.length;
    for(int o = 0; o < ol; o++) {
      for(int p = 0; p < ol; p++) {
        final Occ occ = func.apply(OCCS[o], OCCS[p]);
        final String exp = table[o][p] == null ? "null" : table[o][p].name();
        final String res = occ == null ? "null" : occ.name();
        assertEquals(exp, res, "(" + o + ": " + OCCS[o].name() + ", " + p + ": " +
            OCCS[p].name() + ')');
      }
    }
  }

  /** Tests for {@link Occ#instanceOf(Occ)}. */
  @Test public void occInstanceOf() {
    assertTrue(EXACTLY_ONE.instanceOf(ZERO_OR_MORE));
    assertFalse(ZERO_OR_MORE.instanceOf(EXACTLY_ONE));
    final int bits = 0x014F90E1;

    final int ol = OCCS.length;
    for(int o = 0; o < ol; o++) {
      for(int p = 0; p < ol; p++) {
        final boolean inst = (bits >>> 5 * p + o & 1) != 0;
        assertEquals(inst, OCCS[o].instanceOf(OCCS[p]), "(" + o + ", " + p + ')');
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

    // enums
    final SeqType
      // enum('a')
      e1 = SeqType.get(new EnumType(new TokenSet("a")), EXACTLY_ONE),
      // enum('b')
      e2 = SeqType.get(new EnumType(new TokenSet("b")), EXACTLY_ONE),
      // enum('a', 'b')
      e3 = SeqType.get(new EnumType(new TokenSet("a", "b")), EXACTLY_ONE);
    assertTrue(e1.instanceOf(e3));
    assertFalse(e1.instanceOf(e2));
    assertFalse(e3.instanceOf(e1));
    assertTrue(e3.instanceOf(e3));
    assertTrue(e1.instanceOf(STRING_O));
    assertFalse(STRING_O.instanceOf(e3));
    assertFalse(e1.instanceOf(LANGUAGE_O));
    assertFalse(LANGUAGE_O.instanceOf(e3));

    final SeqType
      // (xs:date | xs:string)
      c1 = SeqType.get(new ChoiceItemType(Arrays.asList(DATE_O, STRING_O)), EXACTLY_ONE),
      // (element() | xs:string)
      c2 = SeqType.get(new ChoiceItemType(Arrays.asList(ELEMENT_O, STRING_O)), EXACTLY_ONE),
      // (xs:NMTOKENS | xs:string)
      c3 = SeqType.get(new ChoiceItemType(Arrays.asList(NMTOKENS_O, STRING_O)), EXACTLY_ONE),
      // (array(*) | xs:string)
      c4 = SeqType.get(new ChoiceItemType(Arrays.asList(ARRAY_O, STRING_O)), EXACTLY_ONE),
      // (map(*) | xs:string)
      c5 = SeqType.get(new ChoiceItemType(Arrays.asList(MAP_O, STRING_O)), EXACTLY_ONE),
      // (function(*) | xs:string)
      c6 = SeqType.get(new ChoiceItemType(Arrays.asList(FUNCTION_O, STRING_O)), EXACTLY_ONE);

    assertTrue(c1.instanceOf(c1));
    assertFalse(c1.instanceOf(c2));
    assertFalse(c1.instanceOf(c3));
    assertFalse(c1.instanceOf(c4));
    assertFalse(c1.instanceOf(c5));
    assertFalse(c1.instanceOf(c6));
    assertFalse(c1.instanceOf(DATE_O));
    assertTrue(DATE_O.instanceOf(c1));
    assertFalse(c1.instanceOf(STRING_O));
    assertTrue(STRING_O.instanceOf(c1));
    assertFalse(c2.instanceOf(c1));
    assertTrue(c2.instanceOf(c2));
    assertFalse(c2.instanceOf(c3));
    assertFalse(c2.instanceOf(c4));
    assertFalse(c2.instanceOf(c5));
    assertFalse(c2.instanceOf(c6));
    assertFalse(c2.instanceOf(ELEMENT_O));
    assertTrue(ELEMENT_O.instanceOf(c2));
    assertFalse(c2.instanceOf(STRING_O));
    assertTrue(STRING_O.instanceOf(c2));
    assertFalse(c3.instanceOf(c1));
    assertFalse(c3.instanceOf(c2));
    assertTrue(c3.instanceOf(c3));
    assertFalse(c3.instanceOf(c4));
    assertFalse(c3.instanceOf(c5));
    assertFalse(c3.instanceOf(c6));
    assertFalse(c3.instanceOf(NMTOKENS_O));
    assertTrue(NMTOKENS_O.instanceOf(c3));
    assertFalse(c3.instanceOf(STRING_O));
    assertTrue(STRING_O.instanceOf(c3));
    assertFalse(c4.instanceOf(c1));
    assertFalse(c4.instanceOf(c2));
    assertFalse(c4.instanceOf(c3));
    assertTrue(c4.instanceOf(c4));
    assertFalse(c4.instanceOf(c5));
    assertTrue(c4.instanceOf(c6));
    assertFalse(c4.instanceOf(ARRAY_O));
    assertTrue(ARRAY_O.instanceOf(c4));
    assertFalse(c4.instanceOf(STRING_O));
    assertTrue(STRING_O.instanceOf(c4));
    assertFalse(c5.instanceOf(c1));
    assertFalse(c5.instanceOf(c2));
    assertFalse(c5.instanceOf(c3));
    assertFalse(c5.instanceOf(c4));
    assertTrue(c5.instanceOf(c5));
    assertTrue(c5.instanceOf(c6));
    assertFalse(c5.instanceOf(MAP_O));
    assertTrue(MAP_O.instanceOf(c5));
    assertFalse(c5.instanceOf(STRING_O));
    assertTrue(STRING_O.instanceOf(c5));
    assertFalse(c6.instanceOf(c1));
    assertFalse(c6.instanceOf(c2));
    assertFalse(c6.instanceOf(c3));
    assertFalse(c6.instanceOf(c4));
    assertFalse(c6.instanceOf(c5));
    assertTrue(c6.instanceOf(c6));
    assertFalse(c6.instanceOf(FUNCTION_O));
    assertTrue(FUNCTION_O.instanceOf(c6));
    assertFalse(c6.instanceOf(STRING_O));
    assertTrue(STRING_O.instanceOf(c6));

    assertTrue(RECORD_O.instanceOf(FUNCTION_O));
    assertTrue(MAP_O.instanceOf(RECORD_O));
    assertTrue(RECORD_O.instanceOf(MAP_O));
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
    combine(ATTRIBUTE_O, op);
    combine(ELEMENT_O, op);
    combine(NODE_O, op);

    combine(STRING_O, INTEGER_O, ANY_ATOMIC_TYPE_O, op);
    combine(STRING_O, STRING_O, STRING_O, op);
    combine(STRING_O, ATTRIBUTE_O, ITEM_O, op);
    combine(NORMALIZED_STRING.seqType(), STRING_O, STRING_O, op);
    combine(STRING_O, NORMALIZED_STRING.seqType(), STRING_O, op);

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
      f5 = FuncType.get(INTEGER_O, BOOLEAN_O).seqType(),
      // function(xs:boolean) as xs:integer?
      f6 = FuncType.get(INTEGER_ZO, BOOLEAN_O).seqType();

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
    combine(m1, f2, f6, op);
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

    // enums
    final SeqType
      // enum('a')
      e1 = SeqType.get(new EnumType(new TokenSet("a")), EXACTLY_ONE),
      // enum('b')
      e2 = SeqType.get(new EnumType(new TokenSet("b")), EXACTLY_ONE),
      // enum('a', 'b')
      e3 = SeqType.get(new EnumType(new TokenSet("a", "b")), EXACTLY_ONE);

    combine(e1, e2, e3, op);
    combine(e1, e3, e3, op);
    combine(e3, op);
    combine(e1, STRING_O, STRING_O, op);
    combine(e1, LANGUAGE_O, STRING_O, op);
    combine(e1, INTEGER_O, ANY_ATOMIC_TYPE_O, op);

    final SeqType
      // (xs:date | xs:string)
      c1 = SeqType.get(new ChoiceItemType(Arrays.asList(DATE_O, STRING_O)), EXACTLY_ONE),
      // (element() | xs:string)
      c2 = SeqType.get(new ChoiceItemType(Arrays.asList(ELEMENT_O, STRING_O)), EXACTLY_ONE),
      // (xs:NMTOKENS | xs:string)
      c3 = SeqType.get(new ChoiceItemType(Arrays.asList(NMTOKENS_O, STRING_O)), EXACTLY_ONE),
      // (array(*) | xs:string)
      c4 = SeqType.get(new ChoiceItemType(Arrays.asList(ARRAY_O, STRING_O)), EXACTLY_ONE),
      // (map(*) | xs:string)
      c5 = SeqType.get(new ChoiceItemType(Arrays.asList(MAP_O, STRING_O)), EXACTLY_ONE),
      // (function(*) | xs:string)
      c6 = SeqType.get(new ChoiceItemType(Arrays.asList(FUNCTION_O, STRING_O)), EXACTLY_ONE);

    combine(c1, op);
    combine(c1, DATE_O, ANY_ATOMIC_TYPE_O, op);
    combine(c1, STRING_O, ANY_ATOMIC_TYPE_O, op);
    combine(c2, op);
    combine(c2, ELEMENT_O, ITEM_O, op);
    combine(c2, STRING_O, ITEM_O, op);
    combine(c3, op);
    combine(c3, NMTOKENS_O, ITEM_O, op);
    combine(c3, STRING_O, ITEM_O, op);
    combine(c4, op);
    combine(c4, ARRAY_O, ITEM_O, op);
    combine(c4, STRING_O, ITEM_O, op);
    combine(c5, op);
    combine(c5, MAP_O, ITEM_O, op);
    combine(c5, STRING_O, ITEM_O, op);
    combine(c6, op);
    combine(c6, FUNCTION_O, ITEM_O, op);
    combine(c6, STRING_O, ITEM_O, op);

    final TokenObjMap<Field> fld1 = new TokenObjMap<>(),
        fld2 = new TokenObjMap<>(),
        fld3 = new TokenObjMap<>(),
        fld4 = new TokenObjMap<>(),
        fld5 = new TokenObjMap<>(),
        fld6 = new TokenObjMap<>(),
        fld7 = new TokenObjMap<>();
    fld1.put(Token.token("a"), new Field(false, INTEGER_O));
    fld2.put(Token.token("a"), new Field(false, STRING_O));
    fld3.put(Token.token("a"), new Field(false, ANY_ATOMIC_TYPE_O));
    fld4.put(Token.token("a"), new Field(false, INTEGER_O));
    fld5.put(Token.token("a"), new Field(true, INTEGER_O));
    fld6.put(Token.token("b"), new Field(true, INTEGER_O));
    fld7.put(Token.token("a"), new Field(true, INTEGER_O));
    fld7.put(Token.token("b"), new Field(true, INTEGER_O));
    final SeqType
      // record(a as xs:integer)
      r1 = SeqType.get(new RecordType(false, fld1), EXACTLY_ONE),
      // record(a as xs:string)
      r2 = SeqType.get(new RecordType(false, fld2), EXACTLY_ONE),
      // record(a as xs:anyAtomicType)
      r3 = SeqType.get(new RecordType(false, fld3), EXACTLY_ONE),
      // record(a as xs:integer, *)
      r4 = SeqType.get(new RecordType(true, fld4), EXACTLY_ONE),
      // record(a as xs:integer?, *)
      r5 = SeqType.get(new RecordType(true, fld5), EXACTLY_ONE),
      // record(b as xs:integer?, *)
      r6 = SeqType.get(new RecordType(true, fld6), EXACTLY_ONE),
      // record(b as xs:integer?, *)
      r7 = SeqType.get(new RecordType(true, fld7), EXACTLY_ONE);

    combine(RECORD_O, FUNCTION_O, FUNCTION_O, op);
    combine(RECORD_O, MAP_O, MAP_O, op);
    combine(RECORD_O, r1, RECORD_O, op);
    combine(FUNCTION_O, r1, FUNCTION_O, op);
    combine(MAP_O, r1, MAP_O, op);
    combine(r1, r2, r3, op);
    combine(r1, r3, r3, op);
    combine(r4, r1, r4, op);
    combine(r5, r1, r5, op);
    combine(r5, r4, r5, op);
    combine(r1, r6, r6, op);
    combine(r2, r6, r6, op);
    combine(r4, r6, r7, op);
    combine(r5, r6, r7, op);
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
      f6 = FuncType.get(BOOLEAN_O, BOOLEAN_O).seqType(),
      // function(xs:boolean) as xs:integer?
      f7 = FuncType.get(INTEGER_ZO, BOOLEAN_O).seqType();

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
    combine(m4, f7, m4, op);

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

    // enums
    final SeqType
      // enum('a')
      e1 = SeqType.get(new EnumType(new TokenSet("a")), EXACTLY_ONE),
      // enum('b')
      e2 = SeqType.get(new EnumType(new TokenSet("b")), EXACTLY_ONE),
      // enum('a', 'b')
      e3 = SeqType.get(new EnumType(new TokenSet("a", "b")), EXACTLY_ONE);

    combine(e1, e2, null, op);
    combine(e1, e3, e1, op);
    combine(e3, op);
    combine(e1, STRING_O, e1, op);
    combine(e1, LANGUAGE_O, null, op);
    combine(e1, INTEGER_O, null, op);

    final SeqType
      // (xs:date | xs:string)
      c1 = SeqType.get(new ChoiceItemType(Arrays.asList(DATE_O, STRING_O)), EXACTLY_ONE),
      // (element() | xs:string)
      c2 = SeqType.get(new ChoiceItemType(Arrays.asList(ELEMENT_O, STRING_O)), EXACTLY_ONE),
      // (xs:NMTOKENS | xs:string)
      c3 = SeqType.get(new ChoiceItemType(Arrays.asList(NMTOKENS_O, STRING_O)), EXACTLY_ONE),
      // (array(*) | xs:string)
      c4 = SeqType.get(new ChoiceItemType(Arrays.asList(ARRAY_O, STRING_O)), EXACTLY_ONE),
      // (map(*) | xs:string)
      c5 = SeqType.get(new ChoiceItemType(Arrays.asList(MAP_O, STRING_O)), EXACTLY_ONE),
      // (function(*) | xs:string)
      c6 = SeqType.get(new ChoiceItemType(Arrays.asList(FUNCTION_O, STRING_O)), EXACTLY_ONE);

    combine(c1, op);
    combine(c1, DATE_O, DATE_O, op);
    combine(c1, STRING_O, STRING_O, op);
    combine(c1, INTEGER_O, null, op);
    combine(c2, op);
    combine(c2, ELEMENT_O, ELEMENT_O, op);
    combine(c2, STRING_O, STRING_O, op);
    combine(c2, INTEGER_O, null, op);
    combine(c3, op);
    combine(c3, NMTOKENS_O, NMTOKENS_O, op);
    combine(c3, STRING_O, STRING_O, op);
    combine(c3, INTEGER_O, null, op);
    combine(c4, op);
    combine(c4, ARRAY_O, ARRAY_O, op);
    combine(c4, STRING_O, STRING_O, op);
    combine(c4, INTEGER_O, null, op);
    combine(c5, op);
    combine(c5, MAP_O, MAP_O, op);
    combine(c5, STRING_O, STRING_O, op);
    combine(c5, INTEGER_O, null, op);
    combine(c6, op);
    combine(c6, FUNCTION_O, FUNCTION_O, op);
    combine(c6, STRING_O, STRING_O, op);
    combine(c6, INTEGER_O, null, op);

    final TokenObjMap<Field> fld1 = new TokenObjMap<>(),
        fld2 = new TokenObjMap<>(),
        fld3 = new TokenObjMap<>(),
        fld4 = new TokenObjMap<>(),
        fld5 = new TokenObjMap<>(),
        fld6 = new TokenObjMap<>(),
        fld7 = new TokenObjMap<>();
    fld1.put(Token.token("a"), new Field(false, INTEGER_O));
    fld2.put(Token.token("a"), new Field(false, STRING_O));
    fld3.put(Token.token("a"), new Field(false, ANY_ATOMIC_TYPE_O));
    fld4.put(Token.token("a"), new Field(false, INTEGER_O));
    fld5.put(Token.token("a"), new Field(true, INTEGER_O));
    fld6.put(Token.token("b"), new Field(true, INTEGER_O));
    fld7.put(Token.token("a"), new Field(false, INTEGER_O));
    fld7.put(Token.token("b"), new Field(false, INTEGER_O));
    final SeqType
      // record(a as xs:integer)
      r1 = SeqType.get(new RecordType(false, fld1), EXACTLY_ONE),
      // record(a as xs:string)
      r2 = SeqType.get(new RecordType(false, fld2), EXACTLY_ONE),
      // record(a as xs:anyAtomicType)
      r3 = SeqType.get(new RecordType(false, fld3), EXACTLY_ONE),
      // record(a as xs:integer, *)
      r4 = SeqType.get(new RecordType(true, fld4), EXACTLY_ONE),
      // record(a as xs:integer?, *)
      r5 = SeqType.get(new RecordType(true, fld5), EXACTLY_ONE),
      // record(b as xs:integer?, *)
      r6 = SeqType.get(new RecordType(true, fld6), EXACTLY_ONE),
      // record(b as xs:integer?, *)
      r7 = SeqType.get(new RecordType(true, fld7), EXACTLY_ONE);

    combine(RECORD_O, FUNCTION_O, RECORD_O, op);
    combine(RECORD_O, MAP_O, RECORD_O, op);
    combine(RECORD_O, r1, r1, op);
    combine(FUNCTION_O, r1, r1, op);
    combine(MAP_O, r1, r1, op);
    combine(r1, r2, null, op);
    combine(r1, r3, r1, op);
    combine(r4, r1, r1, op);
    combine(r5, r1, r1, op);
    combine(r5, r4, r4, op);
    combine(r1, r6, r1, op);
    combine(r2, r6, r2, op);
    combine(r4, r6, r7, op);
    combine(r5, r6, r7, op);
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
