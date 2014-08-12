package org.basex.qt3ts.app;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the UseCaseR.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppUseCaseR extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ1() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $i in $items//item_tuple \n" +
      "            where $i/start_date <= xs:date(\"1999-01-31\") \n" +
      "                and $i/end_date >= xs:date(\"1999-01-31\") \n" +
      "                and contains(exactly-one($i/description), \"Bicycle\") \n" +
      "            order by $i/itemno \n" +
      "            return <item_tuple> { $i/itemno } { $i/description } </item_tuple> } \n" +
      "        </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><item_tuple><itemno>1003</itemno><description>Old Bicycle</description></item_tuple><item_tuple><itemno>1007</itemno><description>Racing Bicycle</description></item_tuple></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ10() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $highbid in $bids//bid_tuple, \n" +
      "                $user in $users//user_tuple \n" +
      "            where $user/userid = $highbid/userid and $highbid/bid = max($bids//bid_tuple[itemno=$highbid/itemno]/bid) \n" +
      "            order by exactly-one($highbid/itemno) \n" +
      "            return <high_bid> { $highbid/itemno } { $highbid/bid } \n" +
      "                     <bidder>{ $user/name/text() }</bidder> \n" +
      "                   </high_bid> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><high_bid><itemno>1001</itemno><bid>55</bid><bidder>Mary Doe</bidder></high_bid><high_bid><itemno>1002</itemno><bid>1200</bid><bidder>Mary Doe</bidder></high_bid><high_bid><itemno>1003</itemno><bid>20</bid><bidder>Jack Sprat</bidder></high_bid><high_bid><itemno>1004</itemno><bid>40</bid><bidder>Tom Jones</bidder></high_bid><high_bid><itemno>1007</itemno><bid>225</bid><bidder>Roger Smith</bidder></high_bid></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ11() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $highbid := max($bids//bid_tuple/bid) \n" +
      "        return <result> { for $item in $items//item_tuple, \n" +
      "                              $b in $bids//bid_tuple[itemno = $item/itemno] \n" +
      "                          where $b/bid = $highbid \n" +
      "                          return <expensive_item> { $item/itemno } { $item/description } \n" +
      "                                    <high_bid>{ $highbid }</high_bid> \n" +
      "                                 </expensive_item> \n" +
      "               } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><expensive_item><itemno>1002</itemno><description>Motorcycle</description><high_bid>1200</high_bid></expensive_item></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ12() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:bid_summary() as element()* { \n" +
      "            for $i in distinct-values($bids//itemno) \n" +
      "            let $b := $bids//bid_tuple[itemno = $i] \n" +
      "            return <bid_count> \n" +
      "                        <itemno>{ $i }</itemno> \n" +
      "                        <nbids>{ count($b) }</nbids> \n" +
      "                   </bid_count> };\n" +
      "        <result> { \n" +
      "            let $bid_counts := local:bid_summary(), \n" +
      "                $maxbids := max($bid_counts/nbids), \n" +
      "                $maxitemnos := $bid_counts[nbids = $maxbids] \n" +
      "                for $item in $items//item_tuple, \n" +
      "                    $bc in $bid_counts \n" +
      "                where $bc/nbids = $maxbids and $item/itemno = $bc/itemno \n" +
      "                return <popular_item> { $item/itemno } { $item/description } \n" +
      "                        <bid_count>{ $bc/nbids/text() }</bid_count> \n" +
      "                       </popular_item> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><popular_item><itemno>1001</itemno><description>Red Bicycle</description><bid_count>5</bid_count></popular_item><popular_item><itemno>1002</itemno><description>Motorcycle</description><bid_count>5</bid_count></popular_item></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ13() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $uid in distinct-values($bids//userid), \n" +
      "                $u in $users//user_tuple[userid = $uid] \n" +
      "            let $b := $bids//bid_tuple[userid = $uid] \n" +
      "            order by exactly-one($u/userid) \n" +
      "            return <bidder> { $u/userid } { $u/name } <bidcount>{ count($b) }</bidcount> <avgbid>{ avg($b/bid) }</avgbid> </bidder> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><bidder><userid>U01</userid><name>Tom Jones</name><bidcount>2</bidcount><avgbid>220</avgbid></bidder><bidder><userid>U02</userid><name>Mary Doe</name><bidcount>5</bidcount><avgbid>387</avgbid></bidder><bidder><userid>U03</userid><name>Dee Linquent</name><bidcount>2</bidcount><avgbid>487.5</avgbid></bidder><bidder><userid>U04</userid><name>Roger Smith</name><bidcount>5</bidcount><avgbid>266</avgbid></bidder><bidder><userid>U05</userid><name>Jack Sprat</name><bidcount>2</bidcount><avgbid>110</avgbid></bidder></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ14() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $i in distinct-values($items//itemno) \n" +
      "            let $b := $bids//bid_tuple[itemno = $i] \n" +
      "            let $avgbid := avg($b/bid) \n" +
      "            where count($b) >= 3 \n" +
      "            order by $avgbid descending \n" +
      "            return <popular_item> <itemno>{ $i }</itemno> <avgbid>{ $avgbid }</avgbid> </popular_item> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><popular_item><itemno>1002</itemno><avgbid>800</avgbid></popular_item><popular_item><itemno>1007</itemno><avgbid>200</avgbid></popular_item><popular_item><itemno>1001</itemno><avgbid>45</avgbid></popular_item></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ15() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $u in $users//user_tuple \n" +
      "            let $b := $bids//bid_tuple[userid=$u/userid and bid>=100] \n" +
      "            where count($b) > 1 \n" +
      "            return <big_spender>{ $u/name/text() }</big_spender> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><big_spender>Mary Doe</big_spender><big_spender>Dee Linquent</big_spender><big_spender>Roger Smith</big_spender></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ16() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $u in $users//user_tuple \n" +
      "            let $b := $bids//bid_tuple[userid = $u/userid] \n" +
      "            order by exactly-one($u/userid) \n" +
      "            return <user> { $u/userid } { $u/name } { \n" +
      "                if (empty($b)) \n" +
      "                then <status>inactive</status> \n" +
      "                else <status>active</status> } </user> \n" +
      "        } </result>",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><user><userid>U01</userid><name>Tom Jones</name><status>active</status></user><user><userid>U02</userid><name>Mary Doe</name><status>active</status></user><user><userid>U03</userid><name>Dee Linquent</name><status>active</status></user><user><userid>U04</userid><name>Roger Smith</name><status>active</status></user><user><userid>U05</userid><name>Jack Sprat</name><status>active</status></user><user><userid>U06</userid><name>Rip Van Winkle</name><status>inactive</status></user></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ17() {
    final XQuery query = new XQuery(
      "\n" +
      "        <frequent_bidder> { \n" +
      "            for $u in $users//user_tuple \n" +
      "            where every $item in $items//item_tuple \n" +
      "                  satisfies some $b in $bids//bid_tuple \n" +
      "                            satisfies ($item/itemno = $b/itemno and $u/userid = $b/userid) \n" +
      "            return $u/name \n" +
      "        } </frequent_bidder>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<frequent_bidder/>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ18() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $u in $users//user_tuple \n" +
      "            order by $u/name \n" +
      "            return <user> { $u/name } { \n" +
      "                for $b in distinct-values($bids//bid_tuple [userid = $u/userid]/itemno) \n" +
      "                for $i in $items//item_tuple[itemno = $b] \n" +
      "                let $descr := $i/description/text() \n" +
      "                order by exactly-one($descr) \n" +
      "                return <bid_on_item>{ $descr }</bid_on_item> } </user> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><user><name>Dee Linquent</name><bid_on_item>Motorcycle</bid_on_item><bid_on_item>Racing Bicycle</bid_on_item></user><user><name>Jack Sprat</name><bid_on_item>Old Bicycle</bid_on_item><bid_on_item>Racing Bicycle</bid_on_item></user><user><name>Mary Doe</name><bid_on_item>Motorcycle</bid_on_item><bid_on_item>Red Bicycle</bid_on_item></user><user><name>Rip Van Winkle</name></user><user><name>Roger Smith</name><bid_on_item>Motorcycle</bid_on_item><bid_on_item>Old Bicycle</bid_on_item><bid_on_item>Racing Bicycle</bid_on_item><bid_on_item>Red Bicycle</bid_on_item></user><user><name>Tom Jones</name><bid_on_item>Motorcycle</bid_on_item><bid_on_item>Tricycle</bid_on_item></user></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ2() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $i in $items//item_tuple \n" +
      "            let $b := $bids//bid_tuple[itemno = $i/itemno] \n" +
      "            where contains(exactly-one($i/description), \"Bicycle\") \n" +
      "            order by $i/itemno \n" +
      "            return <item_tuple> { $i/itemno } { $i/description } <high_bid>{ max($b/bid) }</high_bid> </item_tuple> } \n" +
      "        </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><item_tuple><itemno>1001</itemno><description>Red Bicycle</description><high_bid>55</high_bid></item_tuple><item_tuple><itemno>1003</itemno><description>Old Bicycle</description><high_bid>20</high_bid></item_tuple><item_tuple><itemno>1007</itemno><description>Racing Bicycle</description><high_bid>225</high_bid></item_tuple><item_tuple><itemno>1008</itemno><description>Broken Bicycle</description><high_bid/></item_tuple></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ3() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $u in $users//user_tuple \n" +
      "            for $i in $items//item_tuple \n" +
      "            where $u/rating > \"C\" and $i/reserve_price > 1000 and $i/offered_by = $u/userid \n" +
      "            return <warning> { $u/name } { $u/rating } { $i/description } { $i/reserve_price } </warning> } \n" +
      "        </result>\n" +
      "     ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><warning><name>Dee Linquent</name><rating>D</rating><description>Helicopter</description><reserve_price>50000</reserve_price></warning></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ4() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $i in $items//item_tuple \n" +
      "            where empty ($bids//bid_tuple[itemno = $i/itemno]) \n" +
      "            return <no_bid_item> { $i/itemno } { $i/description } </no_bid_item> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><no_bid_item><itemno>1005</itemno><description>Tennis Racket</description></no_bid_item><no_bid_item><itemno>1006</itemno><description>Helicopter</description></no_bid_item><no_bid_item><itemno>1008</itemno><description>Broken Bicycle</description></no_bid_item></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ5() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            unordered ( \n" +
      "                for $seller in $users//user_tuple, \n" +
      "                    $buyer in $users//user_tuple, \n" +
      "                    $item in $items//item_tuple, \n" +
      "                    $highbid in $bids//bid_tuple \n" +
      "                where $seller/name = \"Tom Jones\" \n" +
      "                  and $seller/userid = $item/offered_by \n" +
      "                  and contains(exactly-one($item/description), \"Bicycle\") \n" +
      "                  and $item/itemno = $highbid/itemno \n" +
      "                  and $highbid/userid = $buyer/userid \n" +
      "                  and $highbid/bid = max( $bids//bid_tuple [itemno = $item/itemno]/bid ) \n" +
      "                return <jones_bike> { $item/itemno } { $item/description } \n" +
      "                        <high_bid>{ $highbid/bid }</high_bid> \n" +
      "                        <high_bidder>{ $buyer/name }</high_bidder> \n" +
      "                       </jones_bike> ) \n" +
      "        } </result>\n" +
      "     ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><jones_bike><itemno>1001</itemno><description>Red Bicycle</description><high_bid><bid>55</bid></high_bid><high_bidder><name>Mary Doe</name></high_bidder></jones_bike></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ6() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            for $item in $items//item_tuple \n" +
      "            let $b := $bids//bid_tuple[itemno = $item/itemno] \n" +
      "            let $z := max($b/bid) \n" +
      "            where exactly-one($item/reserve_price) * 2 < $z \n" +
      "            return <successful_item> { $item/itemno } { $item/description } { $item/reserve_price } \n" +
      "                    <high_bid>{$z }</high_bid> \n" +
      "                   </successful_item> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><successful_item><itemno>1002</itemno><description>Motorcycle</description><reserve_price>500</reserve_price><high_bid>1200</high_bid></successful_item><successful_item><itemno>1004</itemno><description>Tricycle</description><reserve_price>15</reserve_price><high_bid>40</high_bid></successful_item></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ7() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $allbikes := $items//item_tuple [contains(exactly-one(description), \"Bicycle\") or contains(exactly-one(description), \"Tricycle\")] \n" +
      "        let $bikebids := $bids//bid_tuple[itemno = $allbikes/itemno] \n" +
      "        return <high_bid> { max($bikebids/bid) } </high_bid>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<high_bid>225</high_bid>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ8() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $item := $items//item_tuple [end_date >= xs:date(\"1999-03-01\") and end_date <= xs:date(\"1999-03-31\")] \n" +
      "            return <item_count> { count($item) } </item_count>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<item_count>3</item_count>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void rdbQueriesResultsQ9() {
    final XQuery query = new XQuery(
      "\n" +
      "        <result> { \n" +
      "            let $end_dates := $items//item_tuple/end_date \n" +
      "            for $m in distinct-values(\n" +
      "                        for $e in $end_dates \n" +
      "                        return month-from-date($e)) \n" +
      "            let $item := $items//item_tuple[year-from-date(exactly-one(end_date)) = 1999 and month-from-date(exactly-one(end_date)) = $m] \n" +
      "            order by $m \n" +
      "            return <monthly_result> \n" +
      "                    <month>{ $m }</month> \n" +
      "                    <item_count>{ count($item) }</item_count>\n" +
      "                   </monthly_result> \n" +
      "        } </result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$users", node(file("docs/users.xml")));
      query.bind("$items", node(file("docs/items.xml")));
      query.bind("$bids", node(file("docs/bids.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><monthly_result><month>1</month><item_count>1</item_count></monthly_result><monthly_result><month>2</month><item_count>2</item_count></monthly_result><monthly_result><month>3</month><item_count>3</item_count></monthly_result><monthly_result><month>4</month><item_count>1</item_count></monthly_result><monthly_result><month>5</month><item_count>1</item_count></monthly_result></result>", false)
    );
  }
}
