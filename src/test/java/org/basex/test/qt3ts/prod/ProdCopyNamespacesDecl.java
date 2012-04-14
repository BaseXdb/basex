package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CopyNamespacesDecl production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCopyNamespacesDecl extends QT3TestSet {

  /**
   *  A prolog may not contain two copy-namespaces declarations. .
   */
  @org.junit.Test
  public void kCopyNamespacesProlog1() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve, no-inherit; declare copy-namespaces no-preserve, no-inherit; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0055")
    );
  }

  /**
   *  A 'declare copy-namespaces' declaration specifying no-preserve and no-inherit. .
   */
  @org.junit.Test
  public void kCopyNamespacesProlog2() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve, no-inherit; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A 'declare copy-namespaces' declaration specifying no-preserve and no-inherit. .
   */
  @org.junit.Test
  public void kCopyNamespacesProlog3() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve, inherit; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A 'declare copy-namespaces' declaration specifying no-preserve and no-inherit in a wrong order . .
   */
  @org.junit.Test
  public void kCopyNamespacesProlog4() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-inherit, no-preserve; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A 'declare copy-namespaces' declaration specifying preserve and inherit in a wrong order . .
   */
  @org.junit.Test
  public void kCopyNamespacesProlog5() {
    final XQuery query = new XQuery(
      "declare copy-namespaces inherit, preserve; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Use no-preserve, inherit with the default namespace. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog1() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve, inherit; <doc> <a>{<b xmlns:p=\"http://example.com/\"/>}</a> <a><b xmlns:p=\"http://example.com/\"/></a> </doc>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<doc><a><b/></a><a><b xmlns:p=\"http://example.com/\"/></a></doc>", false)
    );
  }

  /**
   *  Ensure the 'copy-namespaces' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog2() {
    final XQuery query = new XQuery(
      "copy-namespaces lt copy-namespaces",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Ensure the 'namespace' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog3() {
    final XQuery query = new XQuery(
      "namespace lt namespace",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Verify that the default element namespace is not touched when using no-inherit. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog4() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve, no-inherit; <e xmlns=\"http://example.com/\"> { <b/> } </e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e xmlns=\"http://example.com/\"><b/></e>", false)
    );
  }

  /**
   *  Have a namespace which is used further down. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog5() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve, no-inherit; <e xmlns:appearsUnused=\"http://example.com/\"> { <b> <appearsUnused:c/> </b> } </e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e xmlns:appearsUnused=\"http://example.com/\"><b><appearsUnused:c xmlns:appearsUnused=\"http://example.com/\"/></b></e>", false)
    );
  }

  /**
   *  Check in-scope bindings of constructed nodes. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog6() {
    final XQuery query = new XQuery(
      "declare namespace ns = \"http://example.com/\"; <e xmlns:appearsUnused=\"http://example.com/\"> { <b> <appearsUnused:c/> </b> } </e>/(for $n in (., b, b/ns:c), $i in in-scope-prefixes($n) order by $i return ($i, '|'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "appearsUnused | appearsUnused | appearsUnused | xml | xml | xml |")
    );
  }

  /**
   *  Have an unused prefix further down the hierarchy. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog7() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve, no-inherit; <e> { <b> <c xmlns:unused=\"http://example.com\"/> </b> } </e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e><b><c/></b></e>", false)
    );
  }

  /**
   *  Copy an element with no children. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog8() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve,inherit; <a> { <b>{()}</b> } </a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a><b/></a>", false)
    );
  }

  /**
   *  Use direct element constructors inside enclosed expressions. .
   */
  @org.junit.Test
  public void k2CopyNamespacesProlog9() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve, no-inherit; declare variable $e1 := <e1 xmlns:namespace1=\"http://www.namespace1.com\"/>; declare variable $e2 := <e2 xmlns:namespace2=\"http://www.namespace2.com\">{$e1}</e2>; for $n in <e3 xmlns:namespace3=\"http://www.namespace3.com\">{$e2}</e3>/e2/e1, $ps in in-scope-prefixes($n) order by $ps return $ps, '|', for $n in <e3 xmlns:namespace3=\"http://www.namespace3.com\">{<e2 xmlns:namespace2=\"http://www.namespace2.com\">{<e1 xmlns:namespace1=\"http://www.namespace1.com\"/>}</e2>}</e3>/e2/e1, $ps in in-scope-prefixes($n) order by $ps return $ps",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "namespace1 xml | namespace1 namespace2 namespace3 xml")
    );
  }

  /**
   *  Evaluation of a prolog with more than one copy-namespace declaration. .
   */
  @org.junit.Test
  public void copynamespace1() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve, no-inherit; declare copy-namespaces no-preserve, no-inherit; declare variable $input-context1 external; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0055")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit" . Use global variables and namespaces with prefixes. .
   */
  @org.junit.Test
  public void copynamespace10() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,inherit; declare variable $existingElement := <existingElement xmlns:existingNamespace=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement>; declare variable $new := <newElement xmlns:newNamespace = \"http://www.mynamespace.com\">{$existingElement}</newElement>; declare variable $input-context1 external; for $var in (in-scope-prefixes($new/existingElement)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "existingNamespace newNamespace xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit" . Use global variables and namespaces with prefixes. Same prefix different namespace URI. .
   */
  @org.junit.Test
  public void copynamespace11() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,inherit; declare variable $existingElement := <existingElement xmlns:existingNamespace=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement>; declare variable $new := <newElement xmlns:existingNamespace = \"http://www.mynamespace.com\">{$existingElement}</newElement>; declare variable $input-context1 external; for $var in (in-scope-prefixes($new/existingElement)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "existingNamespace xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit" . Use global variables and namespaces with prefixes. diferent prefix same namespace URI. .
   */
  @org.junit.Test
  public void copynamespace12() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,inherit; declare variable $existingElement := <existingElement xmlns:existingNamespace=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement>; declare variable $new := <newElement xmlns:newNamespace = \"http://www.existingnamespace.com\">{$existingElement}</newElement>; declare variable $input-context1 external; for $var in (in-scope-prefixes($new/existingElement)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "existingNamespace newNamespace xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit" . Use global variables. only the XML prefix is used. .
   */
  @org.junit.Test
  public void copynamespace13() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,inherit; declare variable $existingElement := <existingElement>{\"Existing Content\"}</existingElement>; declare variable $new := <newElement>{$existingElement}</newElement>; declare variable $input-context1 external; for $var in (in-scope-prefixes(exactly-one($new/existingElement))) return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit" . Use global variables where prefix differes in case. .
   */
  @org.junit.Test
  public void copynamespace14() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,inherit; declare variable $existingElement := <existingElement xmlns:somespace=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement>; declare variable $new := <newElement xmlns:SOMESPACE=\"http://www.another.com\">{$existingElement}</newElement>; declare variable $input-context1 external; for $var in (in-scope-prefixes($new/existingElement)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "SOMESPACE somespace xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit" . with multiple inclusions. Examines last element in subtree. .
   */
  @org.junit.Test
  public void copynamespace15() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,inherit; declare variable $element1 := <element1 xmlns:namespace1=\"http://www.namespace1.com\"></element1>; declare variable $element2 := <element2 xmlns:namespace2=\"http://www.namespace2.com\">{$element1}</element2>; declare variable $element3 := <element3 xmlns:namespace3=\"http://www.namespace3.com\">{$element2}</element3>; declare variable $input-context1 external; for $var in (in-scope-prefixes($element3/element2/element1)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "namespace1 namespace2 namespace3 xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "no-preserve inherit" . with multiple inclusions. Examines last element in subtree. .
   */
  @org.junit.Test
  public void copynamespace16() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve,inherit; declare variable $element1 := <element1 xmlns:namespace1=\"http://www.namespace1.com\"></element1>; declare variable $element2 := <element2 xmlns:namespace2=\"http://www.namespace2.com\">{$element1}</element2>; declare variable $element3 := <element3 xmlns:namespace3=\"http://www.namespace3.com\">{$element2}</element3>; declare variable $input-context1 external; for $var in (in-scope-prefixes($element3/element2/element1)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "namespace3 xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "no-preserve no-inherit" . with multiple inclusions. Examines last element in subtree. .
   */
  @org.junit.Test
  public void copynamespace17() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve,no-inherit; declare variable $element1 := <element1 xmlns:namespace1=\"http://www.namespace1.com\"></element1>; declare variable $element2 := <element2 xmlns:namespace2=\"http://www.namespace2.com\">{$element1}</element2>; declare variable $element3 := <element3 xmlns:namespace3=\"http://www.namespace3.com\">{$element2}</element3>; declare variable $input-context1 external; for $var in (in-scope-prefixes($element3/element2/element1)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve no-inherit" . with multiple inclusions. Examines last element in subtree. .
   */
  @org.junit.Test
  public void copynamespace18() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,no-inherit; declare variable $element1 := <element1 xmlns:namespace1=\"http://www.namespace1.com\"></element1>; declare variable $element2 := <element2 xmlns:namespace2=\"http://www.namespace2.com\">{$element1}</element2>; declare variable $element3 := <element3 xmlns:namespace3=\"http://www.namespace3.com\">{$element2}</element3>; declare variable $input-context1 external; for $var in (in-scope-prefixes($element3/element2/element1)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "namespace1 xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit" . with multiple inclusions. Examines mid element in subtree. .
   */
  @org.junit.Test
  public void copynamespace19() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,inherit; declare variable $element1 := <element1 xmlns:namespace1=\"http://www.namespace1.com\"></element1>; declare variable $element2 := <element2 xmlns:namespace2=\"http://www.namespace2.com\">{$element1}</element2>; declare variable $element3 := <element3 xmlns:namespace3=\"http://www.namespace3.com\">{$element2}</element3>; declare variable $input-context1 external; for $var in (in-scope-prefixes($element3/element2)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "namespace2 namespace3 xml")
    );
  }

  /**
   *  Evaluates that default namespace is overriden by local namespace. .
   */
  @org.junit.Test
  public void copynamespace2() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,no-inherit; declare default element namespace \"http://example.org/names\"; declare variable $input-context1 external; let $new := <newElement xmlns = \"http://www.example.com/mynamespace\">{element original {\"Original Content\"}}</newElement> return $new//*",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<original xmlns=\"http://www.example.com/mynamespace\">Original Content</original>", false)
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "no-preserve inherit" . with multiple inclusions. Examines mid element in subtree. .
   */
  @org.junit.Test
  public void copynamespace20() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve,inherit; declare variable $element1 := <element1 xmlns:namespace1=\"http://www.namespace1.com\"></element1>; declare variable $element2 := <element2 xmlns:namespace2=\"http://www.namespace2.com\">{$element1}</element2>; declare variable $element3 := <element3 xmlns:namespace3=\"http://www.namespace3.com\">{$element2}</element3>; declare variable $input-context1 external; for $var in (in-scope-prefixes($element3/element2)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "namespace3 xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "no-preserve no-inherit" . with multiple inclusions. Examines mid element in subtree. .
   */
  @org.junit.Test
  public void copynamespace21() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve,no-inherit; declare variable $element1 := <element1 xmlns:namespace1=\"http://www.namespace1.com\"></element1>; declare variable $element2 := <element2 xmlns:namespace2=\"http://www.namespace2.com\">{$element1}</element2>; declare variable $element3 := <element3 xmlns:namespace3=\"http://www.namespace3.com\">{$element2}</element3>; declare variable $input-context1 external; for $var in (in-scope-prefixes($element3/element2)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve no-inherit" . with multiple inclusions. Examines mid element in subtree. .
   */
  @org.junit.Test
  public void copynamespace22() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,no-inherit; declare variable $element1 := <element1 xmlns:namespace1=\"http://www.namespace1.com\"></element1>; declare variable $element2 := <element2 xmlns:namespace2=\"http://www.namespace2.com\">{$element1}</element2>; declare variable $element3 := <element3 xmlns:namespace3=\"http://www.namespace3.com\">{$element2}</element3>; declare variable $input-context1 external; for $var in (in-scope-prefixes($element3/element2)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "namespace2 xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve no-inherit". .
   */
  @org.junit.Test
  public void copynamespace3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace foo = \"http://example.org\"; \n" +
      "        declare copy-namespaces preserve,no-inherit; \n" +
      "        let $existingElement := <existingElement xmlns=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement> \n" +
      "        let $new := <foo:newElement xmlns = \"http://www.mynamespace.com\">{$existingElement}</foo:newElement> \n" +
      "        return in-scope-prefixes(exactly-one($new/child::node()))\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"\", \"xml\"")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit". .
   */
  @org.junit.Test
  public void copynamespace4() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://example.org\"; declare copy-namespaces preserve,inherit; declare variable $input-context1 external; let $existingElement := <existingElement xmlns=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement> let $new := <foo:newElement xmlns = \"http://www.mynamespace.com\">{$existingElement}</foo:newElement> for $var in (in-scope-prefixes($new//child::*)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, " foo xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve no-inherit" . The copies element use the same prefix. .
   */
  @org.junit.Test
  public void copynamespace5() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://example.org\"; declare copy-namespaces preserve,no-inherit; declare variable $input-context1 external; let $existingElement := <foo:existingElement xmlns=\"http://www.existingnamespace.com\">{\"Existing Content\"}</foo:existingElement> let $new := <foo:newElement xmlns = \"http://www.mynamespace.com\">{$existingElement}</foo:newElement> for $var in (in-scope-prefixes($new//child::*)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, " foo xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve inherit" . The copies element use the same prefix. .
   */
  @org.junit.Test
  public void copynamespace6() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://example.org\"; declare copy-namespaces preserve,inherit; declare variable $input-context1 external; let $existingElement := <foo:existingElement xmlns=\"http://www.existingnamespace.com\">{\"Existing Content\"}</foo:existingElement> let $new := <foo:newElement xmlns = \"http://www.mynamespace.com\">{$existingElement}</foo:newElement> for $var in (in-scope-prefixes($new//child::*)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, " foo xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "no-preserve no-inherit" . Use global variables and namespaces with prefixes. .
   */
  @org.junit.Test
  public void copynamespace7() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve,no-inherit; declare variable $existingElement := <existingElement xmlns:existingNamespace=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement>; declare variable $new := <newElement xmlns = \"http://www.mynamespace.com\">{$existingElement}</newElement>; declare variable $input-context1 external; for $var in (in-scope-prefixes($new/existingElement)) order by exactly-one($var )ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "preserve no-inherit" . Use global variables and namespaces with prefixes. .
   */
  @org.junit.Test
  public void copynamespace8() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,no-inherit; declare variable $existingElement := <existingElement xmlns:existingNamespace=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement>; declare variable $new := <newElement xmlns = \"http://www.mynamespace.com\">{$existingElement}</newElement>; declare variable $input-context1 external; for $var in (in-scope-prefixes($new/existingElement)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "existingNamespace xml")
    );
  }

  /**
   *  Evaluates copy namespace declaration with value set to "no-preserve inherit" . Use global variables and namespaces with prefixes. .
   */
  @org.junit.Test
  public void copynamespace9() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve,inherit; declare variable $existingElement := <existingElement xmlns:existingNamespace=\"http://www.existingnamespace.com\">{\"Existing Content\"}</existingElement>; declare variable $new := <newElement xmlns:newNamespace = \"http://www.mynamespace.com\">{$existingElement}</newElement>; declare variable $input-context1 external; for $var in (in-scope-prefixes($new/existingElement)) order by exactly-one($var) ascending return $var",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "newNamespace xml")
    );
  }
}
