package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.Map.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Caching Module.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class CacheModuleTest extends SandboxTest {
  /** Invalid characters for database names. */
  private static final char[] INVALID = ",*?;\\/:\"<>|".toCharArray();

  /** Initializes a test. */
  @BeforeEach public void initTest() {
    final Function clear = _CACHE_CLEAR;
    query(clear.args());
  }

  /** Test method. */
  @Test public void cacheClear() {
    final Function func = _CACHE_CLEAR;
    query(_CACHE_PUT.args("key", "CLEAR"));
    query(_CACHE_KEYS.args(), "key");
    query(func.args(), "");
    query(_CACHE_KEYS.args(), "");
  }

  /** Test method. */
  @Test public void cacheDelete() {
    final Function func = _CACHE_DELETE;
    query(_CACHE_PUT.args("key", "CLEAR"));
    query(_CACHE_WRITE.args("DELETE"));
    query(_CACHE_KEYS.args(), "key");
    query(func.args("DELETE"));
    query(_CACHE_KEYS.args(), "key");
    error(func.args("DELETE"), CACHE_NOTFOUND_X);

    error(func.args("unknown"), CACHE_NOTFOUND_X);
    // invalid names
    error(func.args(""), CACHE_NAME_X);
    for(final char ch : INVALID) error(func.args(ch), CACHE_NAME_X);
  }

  /** Test method. */
  @Test public void cacheGet() {
    final Function func = _CACHE_GET;
    query(func.args("key"), "");
  }

  /** Test method. */
  @Test public void cacheGetOrPut() {
    final Function func = _CACHE_GET_OR_PUT;
    query(_CACHE_GET.args("key"), "");
    query(func.args("key", " function() { 'GET-OR-PUT' }"), "GET-OR-PUT");
    query(_CACHE_GET.args("key"), "GET-OR-PUT");
    query(_CACHE_KEYS.args(), "key");
    query(func.args("key", " function() { 'NOT' + 'INVOKED' }"), "GET-OR-PUT");
    query(_CACHE_GET.args("key"), "GET-OR-PUT");
    query(_CACHE_KEYS.args(), "key");
  }

  /** Test method. */
  @Test public void cacheKeys() {
    final Function func = _CACHE_KEYS;
    for(int i = 0; i < 3; i++) query(_CACHE_PUT.args(Integer.toString(i), i));
    query(func.args() + " => sort()", "0\n1\n2");
    query(_CACHE_CLEAR.args());
    query(func.args(), "");
  }

  /** Test method. */
  @Test public void cacheList() {
    final Function func = _CACHE_LIST;
    query(_CACHE_WRITE.args());
    query(_CACHE_WRITE.args("LIST1"));
    query(_CACHE_WRITE.args("LIST2"));
    query(func.args(), "LIST1\nLIST2");
    query(func.args() + " ! " + _CACHE_DELETE.args(" ."));
    query(func.args(), "");
  }

  /** Test method. */
  @Test public void cachePut() {
    final Function func = _CACHE_PUT;
    query(func.args("key", "PUT"), "");
    query(_CACHE_GET.args("key"), "PUT");
    query(_CACHE_KEYS.args(), "key");
    query(func.args("key", " ()"), "");
    query(_CACHE_GET.args("key"), "");
    query(_CACHE_KEYS.args(), "");
    query(func.args("key", " map:merge((1 to 100000) ! map:entry(., .))"), "");
    query(_CACHE_KEYS.args(), "key");
    query(_CACHE_GET.args("key") + " => map:size()", 100000);

    error(func.args("key", " true#0"), BASEX_CACHE_X);
    error(func.args("key", " [ function() { 123 } ]"), BASEX_CACHE_X);
    error(func.args("key", " map { 0: concat(1, ?) }"), BASEX_CACHE_X);
    error(func.args("key", " Q{java.util.Random}new()"), BASEX_CACHE_X);
  }

  /** Test method. */
  @Test public void cacheRead() {
    final Function func = _CACHE_READ;
    query(func.args());
    query(_CACHE_PUT.args("key", "READ"));
    query(_CACHE_KEYS.args(), "key");
    query(_CACHE_WRITE.args());
    query(_CACHE_CLEAR.args());
    query(_CACHE_KEYS.args(), "");
    query(func.args());
    query(_CACHE_KEYS.args(), "key");
    query(_CACHE_WRITE.args("READ"));
    query(_CACHE_CLEAR.args());
    query(_CACHE_KEYS.args(), "");
    query(func.args("READ"));
    query(_CACHE_KEYS.args(), "key");

    error(func.args("unknown"), CACHE_NOTFOUND_X);

    // invalid names
    error(func.args(""), CACHE_NAME_X);
    for(final char ch : INVALID) error(func.args(ch), CACHE_NAME_X);
  }

  /** Test method. */
  @Test public void cacheRemove() {
    final Function func = _CACHE_REMOVE;
    query(_CACHE_PUT.args("key", "REMOVE"));
    query(_CACHE_KEYS.args(), "key");
    query(func.args("key"), "");
    query(_CACHE_KEYS.args(), "");
  }

  /** Test method. */
  @Test public void cacheWrite() {
    final Function func = _CACHE_WRITE;
    query(_CACHE_PUT.args("key", "WRITE"));
    query(func.args());
    query(_CACHE_KEYS.args(), "key");
    query(func.args("cache"));
    query(_CACHE_KEYS.args(), "key");

    // invalid names
    error(func.args(""), CACHE_NAME_X);
    for(final char ch : INVALID) error(func.args(ch), CACHE_NAME_X);
  }

  /** Test method. */
  @Test public void values() {
    unroll(true);

    final Function put = _CACHE_PUT;
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

    // write cache
    query(_CACHE_WRITE.args());
    final HashMap<String, String> values = new HashMap<>();
    for(final String key : query(_CACHE_KEYS.args()).split("\\s")) {
      values.put(key, query(_CACHE_GET.args(key)));
    }

    // check if items of all types have the same representation after reading the cache
    query(_CACHE_READ.args());
    for(final Entry<String, String> entry : values.entrySet()) {
      assertEquals(entry.getValue(), query(_CACHE_GET.args(entry.getKey())));
    }
  }
}
