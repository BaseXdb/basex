package org.basex.query.simple;

import org.basex.query.*;

/**
 * Simple XQuery namespace tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class NSTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<x/>";

    queries = new Object[][] {
      { "NUFP 1", str("http://www.w3.org/XML/1998/namespace"),
        "string(namespace-uri-for-prefix('xml', <X/>))" },
      { "NUFP 2", str(""),
        "string(namespace-uri-for-prefix('xs', <X/>))" },
      { "NUFP 3", str("http://www.w3.org/2001/XMLSchema"),
        "string(namespace-uri-for-prefix('xs', <xs:X/>))" },
      { "NUFP 4", itr(1),
        "count(string(namespace-uri-for-prefix((), <X/>)))" },
      { "NUFP 5", itr(1),
        "count(string(namespace-uri-for-prefix('', <X/>)))" },
      { "NUFP 6", itr(0, 0, 0, 0),
        "count(namespace-uri-for-prefix('', <a/>))," +
        "count(namespace-uri-for-prefix((), <a/>))," +
        "count(namespace-uri-for-prefix('', <a xmlns=''/>))," +
        "count(namespace-uri-for-prefix((), <a xmlns=''/>))" },
      { "NUFP 7", str("U"), "string(<x xmlns:n='U' a='{ " +
        "namespace-uri-for-prefix('n', <n:x/>) }'/>/@a)" },
      { "NUFP 8", str("U"), "string(<x a='{ " +
        "namespace-uri-for-prefix('n', <n:x/>) }' xmlns:n='U'/>/@a)" },
      { "NUFP 9", str("O"),
        "string(<a xmlns:o='O'>{ namespace-uri-for-prefix('o', <e/>) }</a>)" },

      { "NU 1", str("u"),
        "string(<e xmlns:p='u'>{ namespace-uri(<p:e/>) }</e>)" },
      { "NU 2", str("u"),
        "string(<e xmlns='u'>{ namespace-uri(<a/>) }</e>)" },
      { "NU 3", str(""),
        "string(<e>{ namespace-uri(<a/>) }</e>)" },
      { "NU 4", str("u"),
        "declare default element namespace 'u';" +
        "string(<e>{ namespace-uri(<a/>) }</e>)" },
      { "NU 5", str("u"),
        "string(<e xmlns='u'>{namespace-uri-from-QName(xs:QName('a'))}</e>)" },

      { "ISP 1", str("xml"),
        "string(element { QName('U', 'p:e') } { in-scope-prefixes(<e/>) })" },
      { "ISP 2", dbl(2),
        "number(<e xmlns:p='u'>{count(in-scope-prefixes(<e p:x='a'/>))}</e>)" },
      { "ISP 3", itr(2, 2, 1, 1),
        "declare default element namespace 'x'; " +
        "count(in-scope-prefixes(<a/>)), " +
        "count(in-scope-prefixes(<a/>)), " +
        "count(in-scope-prefixes(<a xmlns=''/>)), " +
        "count(in-scope-prefixes(<a xmlns=''/>))" },
      { "ISP 4", dbl(2),
        "number(<e xmlns:n='O'> { count( in-scope-prefixes(<e/>) ) } </e>)" },

      { "EC1", bool(true), "exists(<e xmlns:p='u'>{ <a/>/p:x }</e>)" },

      { "NC1", itr(1), "count(namespace p { 'u' })" },
      { "NC2", "namespace p {''}" },
      { "NC3", "namespace p {'http://www.w3.org/2000/xmlns/'}" },
      { "NC4", "namespace xml {''}" },
      { "NC5", "namespace xml {'http://www.w3.org//XML/1998/namespace'}" },
      { "NC6", "namespace xmlns {'u'}" },

      { "DCN1 0",
        "declare copy-namespaces no-preserve no-inherit; 1" },

      { "STEP 1", str("X"),
        "string(<e a='{ <e>X</e>/self::e }' xmlns='A'/>/@*)" },

      /* Buggy queries:

      // expected error: XQDY0102
      { "NSCon 1", "<e xmlns='x'>{ namespace {''} { 'y' } }</e>" },
      { "NSCon 2", "<e xmlns:p='x'>{ namespace {'p'} { 'y' } }</e>" },

      // function prefix is declared by element constructor
      { "FuncX 1", dbl(1),
        "number(<b a='{ p:count(5) }' " +
        "xmlns:p='http://www.w3.org/2005/xpath-functions'/>/@*)" },
      // error expected: variable namespace must be invalidated by
      // element namespace declaration
      { "VarDecl X1", "declare namespace x='a'; " +
        "let $x:x := 1 return <x a='{ $x:x }' xmlns:x='b'/>" },
        */
    };
  }
}
