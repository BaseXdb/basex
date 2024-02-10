package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.Map.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Store Module.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class StoreModuleTest extends SandboxTest {
  /** Invalid characters for database names. */
  private static final char[] INVALID = ",*?;\\/:\"<>|".toCharArray();

  /** Initializes a test. */
  @BeforeEach public void initTest() {
    final Function clear = _STORE_CLEAR;
    query(clear.args());
  }

  /** Test method. */
  @Test public void storeClear() {
    final Function func = _STORE_CLEAR;
    query(_STORE_PUT.args("key", "CLEAR"));
    query(_STORE_KEYS.args(), "key");
    query(func.args(), "");
    query(_STORE_KEYS.args(), "");
  }

  /** Test method. */
  @Test public void storeDelete() {
    final Function func = _STORE_DELETE;
    query(_STORE_PUT.args("key", "CLEAR"));
    query(_STORE_WRITE.args("DELETE"));
    query(_STORE_KEYS.args(), "key");
    query(func.args("DELETE"));
    query(_STORE_KEYS.args(), "key");
    error(func.args("DELETE"), STORE_NOTFOUND_X);

    error(func.args("unknown"), STORE_NOTFOUND_X);
    // invalid names
    error(func.args(""), STORE_NAME_X);
    for(final char ch : INVALID) error(func.args(ch), STORE_NAME_X);
  }

  /** Test method. */
  @Test public void storeGet() {
    final Function func = _STORE_GET;
    query(func.args("key"), "");
  }

  /** Test method. */
  @Test public void storeGetOrPut() {
    final Function func = _STORE_GET_OR_PUT;
    query(_STORE_GET.args("key"), "");
    query(func.args("key", " function() { 'GET-OR-PUT' }"), "GET-OR-PUT");
    query(_STORE_GET.args("key"), "GET-OR-PUT");
    query(_STORE_KEYS.args(), "key");
    query(func.args("key", " function() { 'NOT' + 'INVOKED' }"), "GET-OR-PUT");
    query(_STORE_GET.args("key"), "GET-OR-PUT");
    query(_STORE_KEYS.args(), "key");
  }

  /** Test method. */
  @Test public void storeKeys() {
    final Function func = _STORE_KEYS;
    for(int i = 0; i < 3; i++) query(_STORE_PUT.args(Integer.toString(i), i));
    query(func.args() + " => sort()", "0\n1\n2");
    query(_STORE_CLEAR.args());
    query(func.args(), "");
  }

  /** Test method. */
  @Test public void storeList() {
    final Function func = _STORE_LIST;
    query(_STORE_WRITE.args());
    query(_STORE_WRITE.args("LIST1"));
    query(_STORE_WRITE.args("LIST2"));
    query(func.args(), "LIST1\nLIST2");
    query(func.args() + " ! " + _STORE_DELETE.args(" ."));
    query(func.args(), "");
  }

  /** Test method. */
  @Test public void storePut() {
    final Function func = _STORE_PUT;
    query(func.args("key", "PUT"), "");
    query(_STORE_GET.args("key"), "PUT");
    query(_STORE_KEYS.args(), "key");
    query(func.args("key", " ()"), "");
    query(_STORE_GET.args("key"), "");
    query(_STORE_KEYS.args(), "");
    query(func.args("key", " map:merge((1 to 100000) ! map:entry(., .))"), "");
    query(_STORE_KEYS.args(), "key");
    query(_STORE_GET.args("key") + " => map:size()", 100000);

    error(func.args("key", " true#0"), BASEX_STORE_X);
    error(func.args("key", " [ function() { 123 } ]"), BASEX_STORE_X);
    error(func.args("key", " map { 0: concat(1, ?) }"), BASEX_STORE_X);
    error(func.args("key", " Q{java.util.Random}new()"), BASEX_STORE_X);
  }

  /** Test method. */
  @Test public void storeRead() {
    final Function func = _STORE_READ;
    query(func.args());
    query(_STORE_PUT.args("key", "READ"));
    query(_STORE_KEYS.args(), "key");
    query(_STORE_WRITE.args());
    query(_STORE_CLEAR.args());
    query(_STORE_KEYS.args(), "");
    query(func.args());
    query(_STORE_KEYS.args(), "key");
    query(_STORE_WRITE.args("READ"));
    query(_STORE_CLEAR.args());
    query(_STORE_KEYS.args(), "");
    query(func.args("READ"));
    query(_STORE_KEYS.args(), "key");

    error(func.args("unknown"), STORE_NOTFOUND_X);

    // invalid names
    error(func.args(""), STORE_NAME_X);
    for(final char ch : INVALID) error(func.args(ch), STORE_NAME_X);
  }

  /** Test method. */
  @Test public void storeRemove() {
    final Function func = _STORE_REMOVE;
    query(_STORE_PUT.args("key", "REMOVE"));
    query(_STORE_KEYS.args(), "key");
    query(func.args("key"), "");
    query(_STORE_KEYS.args(), "");
  }

  /** Test method. */
  @Test public void storeWrite() {
    final Function func = _STORE_WRITE;
    query(_STORE_PUT.args("key", "WRITE"));
    query(func.args());
    query(_STORE_KEYS.args(), "key");
    query(func.args("store"));
    try {
      query(_STORE_KEYS.args(), "key");
    } finally {
      query(_STORE_DELETE.args("store"));
    }

    // invalid names
    error(func.args(""), STORE_NAME_X);
    for(final char ch : INVALID) error(func.args(ch), STORE_NAME_X);
  }

  /** Test method. */
  @Test public void values() {
    unroll(true);

    final Function put = _STORE_PUT;
    query(put.args("xs:untypedAtomic", " xs:untypedAtomic('x')"));
    query(put.args("xs:string", " xs:string('x')"));
    query(put.args("xs:normalizedString", " xs:normalizedString('x')"));
    query(put.args("xs:token", " xs:token('x')"));
    query(put.args("xs:language", " xs:language('x')"));
    query(put.args("xs:NMTOKEN", " xs:NMTOKEN('x')"));
    query(put.args("xs:Name", " xs:Name('x')"));
    query(put.args("xs:NCName", " xs:NCName('x')"));
    query(put.args("xs:ID", " xs:ID('x')"));
    query(put.args("xs:IDREF", " xs:IDREF('x')"));
    query(put.args("xs:ENTITY", " xs:ENTITY('x')"));
    query(put.args("xs:float", " xs:float(1)"));
    query(put.args("xs:numeric", " xs:numeric(1)"));
    query(put.args("xs:double", " xs:double(1)"));
    query(put.args("xs:decimal", " xs:decimal(1)"));
    query(put.args("xs:integer", " xs:integer(1)"));
    query(put.args("xs:nonPositiveInteger", " xs:nonPositiveInteger(-1)"));
    query(put.args("xs:negativeInteger", " xs:negativeInteger(-1)"));
    query(put.args("xs:long", " xs:long(1)"));
    query(put.args("xs:int", " xs:int(1)"));
    query(put.args("xs:short", " xs:short(1)"));
    query(put.args("xs:byte", " xs:byte(1)"));
    query(put.args("xs:nonNegativeInteger", " xs:nonNegativeInteger(1)"));
    query(put.args("xs:unsignedLong", " xs:unsignedLong(1)"));
    query(put.args("xs:unsignedInt", " xs:unsignedInt(1)"));
    query(put.args("xs:unsignedShort", " xs:unsignedShort(1)"));
    query(put.args("xs:unsignedByte", " xs:unsignedByte(1)"));
    query(put.args("xs:positiveInteger", " xs:positiveInteger(1)"));
    query(put.args("xs:duration", " xs:duration('P1Y')"));
    query(put.args("xs:yearMonthDuration", " xs:yearMonthDuration('P1Y')"));
    query(put.args("xs:dayTimeDuration", " xs:dayTimeDuration('P1D')"));
    query(put.args("xs:dateTime", " xs:dateTime('2001-01-01T01:01:01')"));
    query(put.args("xs:date", " xs:date('2001-01-01')"));
    query(put.args("xs:time", " xs:time('01:01:01')"));
    query(put.args("xs:gYearMonth", " xs:gYearMonth('2001-01')"));
    query(put.args("xs:gYear", " xs:gYear('2001')"));
    query(put.args("xs:gMonthDay", " xs:gMonthDay('--01-01')"));
    query(put.args("xs:gDay", " xs:gDay('---01')"));
    query(put.args("xs:gMonth", " xs:gMonth('--01')"));
    query(put.args("xs:boolean", " xs:boolean('true')"));
    query(put.args("xs:base64Binary", " xs:base64Binary('1234')"));
    query(put.args("xs:hexBinary", " xs:hexBinary('4142')"));
    query(put.args("xs:anyURI", " xs:anyURI('x')"));
    query(put.args("xs:QName", " xs:QName('x')"));

    query(put.args("text", " text { ' VALUE ' }"));
    query(put.args("processing-instruction", " processing-instruction name { ' VALUE ' }"));
    query(put.args("comment", " comment { ' VALUE ' }"));
    query(put.args("element", " <a b='c'><b>X</b></a>"));
    query(put.args("document-node1", " document { <a/> }"));
    query(put.args("document-node2", " document { text { 'x' } }"));
    query(put.args("attribute", " attribute a { ' VALUE ' }"));

    query(put.args("BlnSeq", " (0 to 4) ! xs:boolean(. mod 2)"));
    query(put.args("BytSeq", " (0 to 4) ! xs:byte(.)"));
    query(put.args("DblSeq", " (0 to 4) ! xs:double(.)"));
    query(put.args("DecSeq", " (0 to 4) ! xs:decimal(.)"));
    query(put.args("FltSeq", " (0 to 4) ! xs:float(.)"));
    query(put.args("IntSeq", " (0 to 4, reverse(0 to 4))"));
    query(put.args("ShrSeq", " (0 to 4) ! xs:short(.)"));
    query(put.args("StrSeq", " (0 to 4) ! xs:string(.)"));
    query(put.args("SingletonSeq", " (1 to 1000) ! 1"));
    query(put.args("RangeSeq", " (1 to 1000)"));

    query(put.args("EmptySequence", " ()"));
    query(put.args("SmallSequenceSameType", " (1 to 2) ! <x/>"));
    query(put.args("SmallSequenceDifferentTypes", " (1, 2, [ 3 ], map { 4: '4' })"));
    query(put.args("BigSequenceSameType", " (1 to 1000) ! <x/>"));
    query(put.args("BigSequenceDifferentTypes", " ((1 to 10) ! string(), 11, 12.5, 13.8e0)"));

    query(put.args("XQMap1", " map { }"));
    query(put.args("XQMap2", " map { 1: () }"));
    query(put.args("XQMap3", " map { 'a': 'A', 'b': [ 'B' ], 'c': map { 'C': 'CC' } }"));
    query(put.args("XQMap4", " map:merge((1 to 1000) ! map:entry(., string(.)))"));

    query(put.args("XQArray1", " array { }"));
    query(put.args("XQArray2", " array { 1 }"));
    query(put.args("XQArray3", " array { 1, [ 2 ], map { 'C': 'CC' } }"));
    query(put.args("XQArray4", " array { 1 to 1000 }"));
    query(put.args("XQArray5", " [ 1 to 1000 ]"));

    // write store
    query(_STORE_WRITE.args());
    final HashMap<String, String> values = new HashMap<>();
    for(final String key : query(_STORE_KEYS.args()).split("\\s")) {
      values.put(key, query(_STORE_GET.args(key)));
    }

    // check if items of all types have the same representation after reading the store
    query(_STORE_READ.args());
    for(final Entry<String, String> entry : values.entrySet()) {
      assertEquals(entry.getValue(), query(_STORE_GET.args(entry.getKey())));
    }
  }
}
