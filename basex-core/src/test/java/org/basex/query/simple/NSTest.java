package org.basex.query.simple;

import org.basex.query.*;

/**
 * Simple XQuery namespace tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class NSTest extends QueryTest {
  static {
    queries = new Object[][] {
      { "NUFP 1", strings("http://www.w3.org/XML/1998/namespace"),
        "string(namespace-uri-for-prefix('xml', <X/>))" },
      { "NUFP 2", strings(""),
        "string(namespace-uri-for-prefix('xs', <X/>))" },
      { "NUFP 3", strings("http://www.w3.org/2001/XMLSchema"),
        "string(namespace-uri-for-prefix('xs', <xs:X/>))" },
      { "NUFP 4", integers(1),
        "count(string(namespace-uri-for-prefix((), <X/>)))" },
      { "NUFP 5", integers(1),
        "count(string(namespace-uri-for-prefix('', <X/>)))" },
      { "NUFP 6", integers(0, 0, 0, 0),
        "count(namespace-uri-for-prefix('', <a/>))," +
        "count(namespace-uri-for-prefix((), <a/>))," +
        "count(namespace-uri-for-prefix('', <a xmlns=''/>))," +
        "count(namespace-uri-for-prefix((), <a xmlns=''/>))" },
      { "NUFP 7", strings("U"), "string(<x xmlns:n='U' a='{ " +
        "namespace-uri-for-prefix('n', <n:x/>) }'/>/@a)" },
      { "NUFP 8", strings("U"), "string(<x a='{ " +
        "namespace-uri-for-prefix('n', <n:x/>) }' xmlns:n='U'/>/@a)" },

      { "NU 1", strings("u"),
        "string(<e xmlns:p='u'>{ namespace-uri(<p:e/>) }</e>)" },
      { "NU 2", strings("u"),
        "string(<e xmlns='u'>{ namespace-uri(<a/>) }</e>)" },
      { "NU 3", strings(""),
        "string(<e>{ namespace-uri(<a/>) }</e>)" },
      { "NU 4", strings("u"),
        "declare default element namespace 'u';" +
        "string(<e>{ namespace-uri(<a/>) }</e>)" },
      { "NU 5", strings("u"),
        "string(<e xmlns='u'>{namespace-uri-from-QName(xs:QName('a'))}</e>)" },

      { "ISP 1", strings("xml"),
        "string(element { QName('U', 'p:e') } { in-scope-prefixes(<e/>) })" },
      { "ISP 2", doubles(2),
        "number(<e xmlns:p='u'>{count(in-scope-prefixes(<e p:x='a'/>))}</e>)" },
      { "ISP 3", integers(2, 2, 1, 1),
        "declare default element namespace 'x'; " +
        "count(in-scope-prefixes(<a/>)), " +
        "count(in-scope-prefixes(<a/>)), " +
        "count(in-scope-prefixes(<a xmlns=''/>)), " +
        "count(in-scope-prefixes(<a xmlns=''/>))" },

      { "EC1", booleans(true), "exists(<e xmlns:p='u'>{ <a/>/p:x }</e>)" },

      { "NC1", integers(1), "count(namespace p { 'u' })" },
      { "NC2", "namespace p {''}" },
      { "NC3", "namespace p {'http://www.w3.org/2000/xmlns/'}" },
      { "NC4", "namespace xml {''}" },
      { "NC5", "namespace xml {'http://www.w3.org//XML/1998/namespace'}" },
      { "NC6", "namespace xmlns {'u'}" },

      { "DCN1 0",
        "declare copy-namespaces no-preserve no-inherit; 1" },

      { "STEP 1", strings("X"),
        "string(<e a='{ <e>X</e>/self::e }' xmlns='A'/>/@*)" },

      // expected error: XQDY0102
      { "NSCon 1", "<e xmlns='x'>{ namespace {''} { 'y' } }</e>" },
      { "NSCon 2", strings("p_1", "p", "xml"),
        "declare namespace p = 'A'; <p:l>{ namespace p { 'B' } }</p:l> => in-scope-prefixes()" },

      /* Buggy queries:

      { "NSCon 3", "<p:l xmlns:p='A'>{ namespace {'p'} { 'B' } }</p:l>" },

      // function prefix is declared by element constructor
      { "FuncX 1", dbl(1),
        "number(<b a='{ p:count(5) }' " +
        "xmlns:p='http://www.w3.org/2005/xpath-functions'/>/@*)" },
      // error expected: variable namespace must be invalidated by
      // element namespace declaration
      { "VarDecl X1", "declare namespace x='a'; " +
        "let $x:x := 1 return <x a='{ $x:x }' xmlns:x='b'/>" },

      { "NUFP 9", str("O"),
        "string(<a xmlns:o='O'>{ namespace-uri-for-prefix('o', <e/>) }</a>)" },
      { "ISP 4", dbl(2),
        "number(<e xmlns:n='O'> { count( in-scope-prefixes(<e/>) ) } </e>)" }
      */
    };
  }
}
