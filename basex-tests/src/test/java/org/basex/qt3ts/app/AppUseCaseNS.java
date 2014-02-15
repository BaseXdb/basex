package org.basex.qt3ts.app;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the UseCaseNS.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppUseCaseNS extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void nsQueriesResultsQ1() {
    final XQuery query = new XQuery(
      "\n" +
      "        <Q1> { for $n in distinct-values( for $i in (//* | //@*) return namespace-uri($i) ) return <ns>{$n}</ns> } </Q1>",
      ctx);
    try {
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Q1><ns>http://www.example.com/AuctionWatch</ns><ns>http://www.example.com/auctioneers#anyzone</ns><ns>http://www.w3.org/1999/xlink</ns><ns>http://www.w3.org/2001/XMLSchema</ns><ns>http://www.example.com/auctioneers#eachbay</ns><ns>http://www.example.org/music/records</ns><ns>http://www.example.com/auctioneers#yabadoo</ns><ns>http://www.w3.org/XML/1998/namespace</ns></Q1>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void nsQueriesResultsQ2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace music = \"http://www.example.org/music/records\"; \n" +
      "        <Q2> { //music:title } </Q2>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Q2><title xmlns=\"http://www.example.org/music/records\" xmlns:ma=\"http://www.example.com/AuctionWatch\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\">In a Silent Way</title><title xmlns=\"http://www.example.org/music/records\" xmlns:ma=\"http://www.example.com/AuctionWatch\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\">Think of One ...</title></Q2>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void nsQueriesResultsQ3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace dt = \"http://www.w3.org/2001/XMLSchema\"; \n" +
      "        <Q3> { //*[@dt:*] } </Q3>",
      ctx);
    try {
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Q3><ma:Open xmlns:dt=\"http://www.w3.org/2001/XMLSchema\" xmlns:ma=\"http://www.example.com/AuctionWatch\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\" dt:type=\"timeInstant\">2000-03-21:07:41:34-05:00</ma:Open><ma:Close xmlns:dt=\"http://www.w3.org/2001/XMLSchema\" xmlns:ma=\"http://www.example.com/AuctionWatch\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\" dt:type=\"timeInstant\">2000-03-23:07:41:34-05:00</ma:Close><ma:Open xmlns:dt=\"http://www.w3.org/2001/XMLSchema\" xmlns:ma=\"http://www.example.com/AuctionWatch\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\" dt:type=\"timeInstant\">2000-03-19:17:03:00-04:00</ma:Open><ma:Close xmlns:dt=\"http://www.w3.org/2001/XMLSchema\" xmlns:ma=\"http://www.example.com/AuctionWatch\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\" dt:type=\"timeInstant\">2000-03-29:17:03:00-04:00</ma:Close></Q3>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void nsQueriesResultsQ4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace xlink = \"http://www.w3.org/1999/xlink\"; \n" +
      "        <Q4 xmlns:xlink=\"http://www.w3.org/1999/xlink\"> { for $hr in //@xlink:href return <ns>{ $hr }</ns> } </Q4>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Q4 xmlns:xlink=\"http://www.w3.org/1999/xlink\"><ns xlink:href=\"http://www.example.com/item/0321K372910\"/><ns xlink:href=\"http://auction.eachbay.com/members?get=RecordsRUs\"/><ns xlink:href=\"http://auction.anyzone.com/members/VintageRecordFreak\"/><ns xlink:href=\"http://auctions.yabadoo.com/auction/13143816\"/><ns xlink:href=\"http://auction.eachbay.com/showRating/user=VintageRecordFreak\"/><ns xlink:href=\"http://auction.eachbay.com/showRating/user=StarsOn45\"/></Q4>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void nsQueriesResultsQ5() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace music = \"http://www.example.org/music/records\"; \n" +
      "        <Q5 xmlns:music=\"http://www.example.org/music/records\"> { //music:record[music:remark/@xml:lang = \"de\"] } </Q5>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Q5 xmlns:music=\"http://www.example.org/music/records\"><record xmlns=\"http://www.example.org/music/records\" xmlns:ma=\"http://www.example.com/AuctionWatch\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\">\n            <artist>Wynton Marsalis</artist>\n            <title>Think of One ...</title>\n            <recorded>1983</recorded>\n            <label>Columbia Records</label>\n            <remark xml:lang=\"en\"> Columbia Records 12\" 33-1/3 rpm LP,\n                #FC-38641, Stereo. The record is still clean and shiny\n                and looks unplayed (looks like NM condition).  The\n                cover has very light surface and edge wear.\n            </remark>\n            <remark xml:lang=\"de\"> Columbia Records 12\" 33-1/3 rpm LP,\n                #FC-38641, Stereo. Die Platte ist noch immer sauber\n                und gl&#228;nzend und sieht ungespielt aus\n                (NM Zustand). Das Cover hat leichte Abnutzungen an\n                Oberfl&#228;che und Ecken.\n            </remark>\n        </record></Q5>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void nsQueriesResultsQ6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ma = \"http://www.example.com/AuctionWatch\"; \n" +
      "        declare namespace anyzone = \"http://www.example.com/auctioneers#anyzone\"; \n" +
      "        <Q6 xmlns:ma=\"http://www.example.com/AuctionWatch\"> { //ma:Auction[@anyzone:ID]/ma:Schedule/ma:Close } </Q6>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Q6 xmlns:ma=\"http://www.example.com/AuctionWatch\"><ma:Close xmlns:dt=\"http://www.w3.org/2001/XMLSchema\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\" dt:type=\"timeInstant\">2000-03-23:07:41:34-05:00</ma:Close></Q6>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void nsQueriesResultsQ7() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ma = \"http://www.example.com/AuctionWatch\"; \n" +
      "        <Q7 xmlns:xlink=\"http://www.w3.org/1999/xlink\"> { \n" +
      "            for $a in //ma:Auction \n" +
      "            let $seller_id := $a/ma:Trading_Partners/ma:Seller/*:ID, \n" +
      "                $buyer_id := $a/ma:Trading_Partners/ma:High_Bidder/*:ID \n" +
      "            where namespace-uri(exactly-one($seller_id)) = namespace-uri($buyer_id) \n" +
      "            return $a/ma:AuctionHomepage } </Q7>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Q7 xmlns:xlink=\"http://www.w3.org/1999/xlink\"><ma:AuctionHomepage xmlns:ma=\"http://www.example.com/AuctionWatch\" xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\" xlink:type=\"simple\" xlink:href=\"http://auctions.yabadoo.com/auction/13143816\"/></Q7>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void nsQueriesResultsQ8() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ma = \"http://www.example.com/AuctionWatch\"; \n" +
      "        <Q8 xmlns:ma=\"http://www.example.com/AuctionWatch\" \n" +
      "            xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" \n" +
      "            xmlns:xlink=\"http://www.w3.org/1999/xlink\"> { \n" +
      "                for $s in //ma:Trading_Partners/(ma:Seller | ma:High_Bidder) \n" +
      "                where $s/*:NegativeComments = 0 \n" +
      "                return $s } </Q8>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Q8 xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:eachbay=\"http://www.example.com/auctioneers#eachbay\" xmlns:ma=\"http://www.example.com/AuctionWatch\"><ma:High_Bidder xmlns:anyzone=\"http://www.example.com/auctioneers#anyzone\" xmlns:yabadoo=\"http://www.example.com/auctioneers#yabadoo\">\n            <eachbay:ID>VintageRecordFreak</eachbay:ID>\n            <eachbay:PositiveComments>232</eachbay:PositiveComments>\n            <eachbay:NeutralComments>0</eachbay:NeutralComments>\n            <eachbay:NegativeComments>0</eachbay:NegativeComments>\n            <ma:MemberInfoPage xlink:type=\"simple\" xlink:href=\"http://auction.eachbay.com/showRating/user=VintageRecordFreak\" xlink:role=\"ma:MemberInfoPage\"/>            \n        </ma:High_Bidder></Q8>", false)
    );
  }
}
