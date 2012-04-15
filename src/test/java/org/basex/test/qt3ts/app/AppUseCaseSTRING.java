package org.basex.test.qt3ts.app;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the UseCaseSTRING.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppUseCaseSTRING extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void stringQueriesResultsQ1() {
    final XQuery query = new XQuery(
      "//news_item/title[contains(., \"Foobar Corporation\")]",
      ctx);
    query.context(node(file("docs/string.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<title>Foobar Corporation releases its new line of Foo products\n   today</title><title>Foobar Corporation is suing Gorilla Corporation for\n   patent infringement </title>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void stringQueriesResultsQ2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $input-context1 := $string;\n" +
      "        declare variable $input-context2 := $company-data;\n" +
      "\n" +
      "        declare function local:partners($company as xs:string) as element()*\n" +
      "        {\n" +
      "            let $c := $input-context2//company[name = $company]\n" +
      "            return $c//partner\n" +
      "        };\n" +
      "\n" +
      "        let $foobar_partners := local:partners(\"Foobar Corporation\")\n" +
      "\n" +
      "        for $item in $input-context1//news_item\n" +
      "        where\n" +
      "          some $t in $item//title satisfies\n" +
      "            (contains(exactly-one($t/text()), \"Foobar Corporation\")\n" +
      "            and (some $partner in $foobar_partners satisfies\n" +
      "              contains(exactly-one($t/text()), $partner/text())))\n" +
      "          or (some $par in $item//par satisfies\n" +
      "           (contains(string($par), \"Foobar Corporation\")\n" +
      "             and (some $partner in $foobar_partners satisfies\n" +
      "                contains(string($par), $partner/text()))))\n" +
      "        return\n" +
      "            <news_item>\n" +
      "                { $item/title }\n" +
      "                { $item/date }\n" +
      "            </news_item>\n" +
      "      ",
      ctx);
    query.bind("$string", node(file("docs/string.xml")));
    query.bind("$company-data", node(file("docs/company-data.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<news_item><title> Gorilla Corporation acquires YouNameItWeIntegrateIt.com </title><date>1-20-2000</date></news_item><news_item><title>Foobar Corporation releases its new line of Foo products\n   today</title><date>1-20-2000</date></news_item><news_item><title>Foobar Corporation is suing Gorilla Corporation for\n   patent infringement </title><date>1-20-2000</date></news_item>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void stringQueriesResultsQ4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:partners($c as xs:string) as element()* { \n" +
      "            let $c := $company-data//company[name = $c] \n" +
      "            return $c//partner \n" +
      "        }; \n" +
      "        for $item in $string//news_item, \n" +
      "            $c in $company-data//company \n" +
      "        let $partners := local:partners(exactly-one($c/name)) \n" +
      "        where contains(string($item), $c/name) \n" +
      "          and (some $p in $partners satisfies contains(string($item), $p) and $item/news_agent != $c/name) \n" +
      "        return $item",
      ctx);
    query.bind("$string", node(file("docs/string.xml")));
    query.bind("$company-data", node(file("docs/company-data.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<news_item>\n   <title> Gorilla Corporation acquires YouNameItWeIntegrateIt.com </title>\n   <content>\n      <par> Today, Gorilla Corporation announced that it will purchase\n          YouNameItWeIntegrateIt.com. The shares of\n          YouNameItWeIntegrateIt.com dropped $3.00 as a result of this\n          announcement.\n      </par>\n\n      <par> As a result of this acquisition, the CEO of\n          YouNameItWeIntegrateIt.com Bill Smarts resigned. He did not\n          announce what he will do next.  Sources close to\n          YouNameItWeIntegrateIt.com hint that Bill Smarts might be\n          taking a position in Foobar Corporation.\n      </par>\n\n      <par>YouNameItWeIntegrateIt.com is a leading systems integrator\n          that enables <quote>brick and mortar</quote> companies to\n          have a presence on the web.\n      </par>\n\n   </content>\n   <date>1-20-2000</date>\n   <author>Mark Davis</author>\n   <news_agent>News Online</news_agent>\n</news_item><news_item> <title>Foobar Corporation is suing Gorilla Corporation for\n   patent infringement </title>\n   <content>\n      <par> In surprising developments today, Foobar Corporation\n         announced that it is suing Gorilla Corporation for patent\n         infringement. The patents that were mentioned as part of the\n         lawsuit are considered to be the basis of Foobar\n         Corporation's <quote>Wireless Foo</quote> line of products.\n      </par>\n      <par>The tension between Foobar and Gorilla Corporations has\n         been increasing ever since the Gorilla Corporation acquired\n         more than 40 engineers who have left Foobar Corporation,\n         TheAppCompany Inc. and YouNameItWeIntegrateIt.com over the\n         past 3 months. The engineers who have left the Foobar\n         corporation and its partners were rumored to be working on\n         the next generation of server products and applications which\n         will directly compete with Foobar's Foo 20.9 servers. Most of\n         the engineers have relocated to Hawaii where the Gorilla\n         Corporation's server development is located.\n      </par>\n   </content>\n   <date>1-20-2000</date>\n   <news_agent>Reliable News Corporation</news_agent>\n</news_item>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void stringQueriesResultsQ5() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $item in //news_item \n" +
      "        where contains(string(exactly-one($item/content)), \"Gorilla Corporation\") \n" +
      "        return <item_summary> { concat($item/title,\". \") } \n" +
      "                              { concat($item/date,\". \") } \n" +
      "                              { string(($item//par)[1]) } \n" +
      "               </item_summary>\n" +
      "      ",
      ctx);
    query.context(node(file("docs/string.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<item_summary> Gorilla Corporation acquires YouNameItWeIntegrateIt.com . 1-20-2000.  Today, Gorilla Corporation announced that it will purchase\n          YouNameItWeIntegrateIt.com. The shares of\n          YouNameItWeIntegrateIt.com dropped $3.00 as a result of this\n          announcement.\n      </item_summary><item_summary>Foobar Corporation is suing Gorilla Corporation for\n   patent infringement . 1-20-2000.  In surprising developments today, Foobar Corporation\n         announced that it is suing Gorilla Corporation for patent\n         infringement. The patents that were mentioned as part of the\n         lawsuit are considered to be the basis of Foobar\n         Corporation's Wireless Foo line of products.\n      </item_summary>", false)
    );
  }
}
