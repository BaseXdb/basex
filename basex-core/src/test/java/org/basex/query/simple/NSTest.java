package org.basex.query.simple;

import org.basex.query.*;

/**
 * Simple XQuery namespace tests.
 *
 * @author BaseX Team 2005-22, BSD License
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

      { "Ns1", strings("_"),
        "<_ xmlns='a'>{ namespace { '' } { 'a' } }</_> ! name()" },
      { "NS2", strings("_"),
        "<_ xmlns='a'>{ element E { namespace { '' } { 'a' } } }</_> ! name()" },
      { "NS3", strings("_:_"),
        "<_:_ xmlns:_='_'>{ namespace { '' } { 'b' } }</_:_> ! name()" },

      // expected error: XQDY0102
      { "NsError1",
        "<e xmlns='x'>{ namespace { '' } { 'B' } }</e>" },
      { "NsError2", strings("p_1", "p", "xml"),
        "declare namespace p = 'A'; <p:l>{ namespace p { 'B' } }</p:l> => in-scope-prefixes()" },
      { "NsError3",
        "let $e := element E { namespace { '' } { 'B' } } return <_ xmlns='A'>{ $e }</_>" },
      { "NsError4",
        "<_ xmlns='a'>{ namespace { '' } { 'B' } }</_>" },
      { "NsError5",
        "<_ xmlns='A'>{ element E { namespace { '' } { 'B' } } }</_>" },
    };
  }
}
