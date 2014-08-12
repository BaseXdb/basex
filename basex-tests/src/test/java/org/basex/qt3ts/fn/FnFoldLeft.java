package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the fn:fold-left() higher-order function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFoldLeft extends QT3TestSet {

  /**
   * Higher Order Functions fold-left function  .
   */
  @org.junit.Test
  public void foldLeft001() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b) { $a + $b }, 0, 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
   * Higher Order Functions fold-left function  .
   */
  @org.junit.Test
  public void foldLeft002() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b) { $a * $b }, 1, (2,3,5,7))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("210")
    );
  }

  /**
   * Higher Order Functions fold-left function  .
   */
  @org.junit.Test
  public void foldLeft003() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b) { $a or $b }, false(), (true(), false(), false()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Higher Order Functions fold-left function  .
   */
  @org.junit.Test
  public void foldLeft004() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b) { $a and $b }, false(), (true(), false(), false()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Higher Order Functions fold-left function  .
   */
  @org.junit.Test
  public void foldLeft005() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b) {($b, $a)}, (), 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("5, 4, 3, 2, 1")
    );
  }

  /**
   * Higher Order Functions fold-left function  .
   */
  @org.junit.Test
  public void foldLeft006() {
    final XQuery query = new XQuery(
      "fold-left(fn:concat(?, \".\", ?), \"\", 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'.1.2.3.4.5'")
    );
  }

  /**
   * Higher Order Functions fold-left function  .
   */
  @org.junit.Test
  public void foldLeft007() {
    final XQuery query = new XQuery(
      "fold-left(fn:concat(\"$f(\", ?, \", \", ?, \")\"), \"$zero\", 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "$f($f($f($f($f($zero, 1), 2), 3), 4), 5)")
    );
  }

  /**
   *  get the employees who worked the maximum number of hours .
   */
  @org.junit.Test
  public void foldLeft008() {
    final XQuery query = new XQuery(
      "\n" +
      "            let $hours := function ($emp as element(employee)) as xs:integer { sum($emp/hours/xs:integer(.)) },\n" +
      "                $highest := function ($f as function(item()) as xs:anyAtomicType, $seq as item()*)  {           \n" +
      "                    fold-left(\n" +
      "                       function($highestSoFar as item()*, $this as item()*) as item()* {\n" +
      "                          if (empty($highestSoFar))\n" +
      "                          then $this\n" +
      "                          else let $thisValue := $f($this),\n" +
      "                                   $highestValue := $f($highestSoFar[1])\n" +
      "                               return if ($thisValue gt $highestValue)\n" +
      "                                      then $this\n" +
      "                                      else if ($thisValue eq $highestValue)\n" +
      "                                           then ($highestSoFar, $this)\n" +
      "                                           else $highestSoFar\n" +
      "                       }, (), $seq)\n" +
      "            }\n" +
      "            \n" +
      "            return $highest($hours, /works/employee) \n" +
      "        ",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertCount(1)
      &&
        assertType("element(employee)")
      &&
        assertQuery("$result/@name = \"John Doe 2\"")
      )
    );
  }

  /**
   * fold-left-009 author Michael Kay, Saxonica implement eg:distinct-nodes-stable() .
   */
  @org.junit.Test
  public void foldLeft009() {
    final XQuery query = new XQuery(
      "\n" +
      "            declare function local:distinct-nodes-stable($seq as node()*) { \n" +
      "                fold-left( function($foundSoFar as node()*, $this as node()) as node()* { \n" +
      "                if ($foundSoFar intersect $this) \n" +
      "                then $foundSoFar \n" +
      "                else ($foundSoFar, $this) }, (), $seq) \n" +
      "            }; \n" +
      "            let $nodes := (<a/>, <b/>, <c/>, <d/>, <e/>, <f/>) \n" +
      "            let $perm := ($nodes[1], $nodes[2], $nodes[4], $nodes[1], $nodes[2], $nodes[3], $nodes[2], $nodes[1]) \n" +
      "            return local:distinct-nodes-stable($perm)/local-name()\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b d c")
    );
  }

  /**
   * Higher Order Functions fold-left function: supplied function has wrong arity.
   */
  @org.junit.Test
  public void foldLeft010() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b, $c){ $a + $b + $c }, 1, 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Higher Order Functions fold-left function: supplied function delivers result of wrong type.
   */
  @org.junit.Test
  public void foldLeft011() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b) as element(foo) { $a + $b }, 1, 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Higher Order Functions fold-left function: 'zero' value is of wrong type.
   */
  @org.junit.Test
  public void foldLeft012() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b) { $a + $b }, \"\", 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Higher Order Functions fold-left function: second argument is of wrong type.
   */
  @org.junit.Test
  public void foldLeft013() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b as element(foo)) { $a + $b }, 1, 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Higher Order Functions fold-left function: first argument is of wrong type.
   */
  @org.junit.Test
  public void foldLeft014() {
    final XQuery query = new XQuery(
      "fold-left(function($a as element(bar), $b) { $a + $b }, 1, 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Higher Order Functions fold-left function.
   */
  @org.junit.Test
  public void foldLeft015() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b){ ($a, $b) }, 1, 1 to 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 1, 2")
    );
  }

  /**
   * Higher Order Functions fold-left function. Dynamic node selection..
   */
  @org.junit.Test
  public void foldLeft016() {
    final XQuery query = new XQuery(
      "\n" +
      "let $html := <html>\n" +
      "  <body>\n" +
      "\t<div id=\"main\">\n" +
      "\t  <p class=\"para\">Hello World!</p>\t\n" +
      "\t</div>\n" +
      "\t<p class=\"para\">Goodbye!</p>\t\n" +
      "  </body>\n" +
      "</html>\n" +
      "let $css-selectors := <selectors>\n" +
      "  <id>main</id>\n" +
      "  <class>para</class>\n" +
      "</selectors>/*\n" +
      "let $interpreter  := function($ctx, $selector){\n" +
      "  typeswitch($selector)\n" +
      "    case $a as element(id) return $ctx//*[@id = $a/text()]\n" +
      "    case $a as element(class) return $ctx//*[@class = $a/text()]\n" +
      "  default return ()\n" +
      "}\n" +
      "let $result := fold-left($interpreter, $html, $css-selectors)\n" +
      "return\n" +
      "  $result/text()\n" +
      "\t ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Hello World!")
    );
  }

  /**
   * Higher Order Functions fold-left function. Display fold-left structural transformation..
   */
  @org.junit.Test
  public void foldLeft017() {
    final XQuery query = new XQuery(
      "fold-left(concat(\"(\", ?, \"+\", ?, \")\"), 0, 1 to 13)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "(((((((((((((0+1)+2)+3)+4)+5)+6)+7)+8)+9)+10)+11)+12)+13)")
    );
  }

  /**
   * Higher Order Functions fold-left function. Count the number of words in a sentence..
   */
  @org.junit.Test
  public void foldLeft018() {
    final XQuery query = new XQuery(
      "\n" +
      "let $text := \"Peter Piper picked a peck of pickled peppers A peck of pickled peppers Peter Piper picked\"\n" +
      "let $tokens := tokenize($text, '\\s')\n" +
      "let $counter := function($result, $word){\n" +
      "  let $word-count := $result[@value = $word]\n" +
      "  return\n" +
      "    if(empty($word-count)) then\n" +
      "      ($result, <word value=\"{$word}\" count=\"1\" />)\n" +
      "    else\n" +
      "    (\n" +
      "      $result except $word-count,\n" +
      "      <word value=\"{$word-count/@value}\" count=\"{number($word-count/@count) + 1}\" />\n" +
      "    )\n" +
      "}\n" +
      "let $words := fold-left($counter, (), $tokens)\n" +
      "return (\n" +
      "  number($words[@value=\"Peter\"]/@count),\n" +
      "  number($words[@value=\"Piper\"]/@count),\n" +
      "  number($words[@value=\"pickled\"]/@count)\n" +
      ")\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("2, 2, 2")
    );
  }

  /**
   * Higher Order Functions fold-left function. Returns the average of the sequence values..
   */
  @org.junit.Test
  public void foldLeft019() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b){ if(empty($a)) then $b else ($a + $b) div 2 }, (), (13, 14, 9, 6))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "8.625")
    );
  }

  /**
   * Higher Order Functions fold-left function. Count the number of items in a sequence..
   */
  @org.junit.Test
  public void foldLeft020() {
    final XQuery query = new XQuery(
      "fold-left(function($a, $b){ $a + 1}, 0, 1 to 1000000)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1000000")
    );
  }
}
