package org.basex.server;

import static org.basex.core.Text.*;

import java.io.*;
import java.math.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Runs the XMark tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XMarkTest {
  /** Test user. */
  private static final String USER = "xmark";
  /** Name of database. */
  private static final String DB = "111mb";
  /** Input data of database. */
  private static final String DBFILE = "https://files.basex.org/xml/xmark.xml";

  /** Test directory. */
  private static final IOFile DIR = new IOFile(Prop.TEMPDIR, "XMark");
  /** Output file. */
  private static final IOFile FILE = new IOFile(DIR, "master- " + DB + ".graph");

  /** Maximum time per query (ms). */
  private static final int MAX = 2000;

  /** Queries. */
  private static final String[] QUERIES = {
    "let $auction := . return for $b in $auction/site/people/person[@id = \"person0\"]"
    + "return $b/name/text()",
    "let $auction := . return for $b in $auction/site/open_auctions/open_auction "
    + "return <increase>{$b/bidder[1]/increase/text()}</increase>",
    "let $auction := . return for $b in $auction/site/open_auctions/open_auction "
    + "where zero-or-one($b/bidder[1]/increase/text()) * 2 <= $b/bidder[last()]/increase/text() "
    + "return <increase first=\"{$b/bidder[1]/increase/text()}\" "
    + "last=\"{$b/bidder[last()]/increase/text()}\"/>",
    "let $auction := . return for $b in $auction/site/open_auctions/open_auction "
    + "where some $pr1 in $b/bidder/personref[@person = \"person20\"], "
    + "$pr2 in $b/bidder/personref[@person = \"person51\"] satisfies $pr1 << $pr2 "
    + "return <history>{$b/reserve/text()}</history>",
    "let $auction := . return count( for $i in $auction/site/closed_auctions/closed_auction "
    + "where $i/price/text() >= 40 return $i/price )",
    "let $auction := . return for $b in $auction//site/regions return count($b//item)",
    "let $auction := . return for $p in $auction/site "
    + "return count($p//description) + count($p//annotation) + count($p//emailaddress)",
    "let $auction := . return for $p in $auction/site/people/person "
    + "let $a := for $t in $auction/site/closed_auctions/closed_auction "
    + "where $t/buyer/@person = $p/@id return $t "
    + "return <item person=\"{$p/name/text()}\">{count($a)}</item>",
    "let $auction := . return let $ca := $auction/site/closed_auctions/closed_auction "
    + "return let $ei := $auction/site/regions/europe/item for $p in $auction/site/people/person "
    + "let $a := for $t in $ca where $p/@id = $t/buyer/@person return "
    + "let $n := for $t2 in $ei where $t/itemref/@item = $t2/@id return $t2 "
    + "return <item>{$n/name/text()}</item> return <person name=\"{$p/name/text()}\">{$a}</person>",
    "let $auction := . return for $i in "
    + "distinct-values($auction/site/people/person/profile/interest/@category) "
    + "let $p := for $t in $auction/site/people/person where $t/profile/interest/@category = $i "
    + "return <personne> <statistiques> <sexe>{$t/profile/gender/text()}</sexe> "
    + "<age>{$t/profile/age/text()}</age> <education>{$t/profile/education/text()}</education> "
    + "<revenu>{fn:data($t/profile/@income)}</revenu> </statistiques> <coordonnees> "
    + "<nom>{$t/name/text()}</nom> <rue>{$t/address/street/text()}</rue> "
    + "<ville>{$t/address/city/text()}</ville> <pays>{$t/address/country/text()}</pays> "
    + "<reseau> <courrier>{$t/emailaddress/text()}</courrier> "
    + "<pagePerso>{$t/homepage/text()}</pagePerso> </reseau> </coordonnees> "
    + "<cartePaiement>{$t/creditcard/text()}</cartePaiement> </personne> "
    + "return <categorie>{<id>{$i}</id>, $p}</categorie>",
    "let $auction := . return for $p in $auction/site/people/person let $l := "
    + "for $i in $auction/site/open_auctions/open_auction/initial "
    + "where $p/profile/@income > 5000 * exactly-one($i/text()) "
    + "return $i return <items name=\"{$p/name/text()}\">{count($l)}</items>",
    "let $auction := . return for $p in $auction/site/people/person let $l := "
    + "for $i in $auction/site/open_auctions/open_auction/initial "
    + "where $p/profile/@income > 5000 * exactly-one($i/text()) return $i "
    + "where $p/profile/@income > 50000 "
    + "return <items person=\"{$p/profile/@income}\">{count($l)}</items>",
    "let $auction := . return for $i in $auction/site/regions/australia/item "
    + "return <item name=\"{$i/name/text()}\">{$i/description}</item>",
    "let $auction := . return for $i in $auction/site//item "
    + "where contains(string(exactly-one($i/description)), \"gold\") return $i/name/text()",
    "let $auction := . return for $a in $auction/site/closed_auctions/closed_auction/annotation/"
    + "description/parlist/listitem/parlist/listitem/text/emph/keyword/text() "
    + "return <text>{$a}</text>",
    "let $auction := . return for $a in $auction/site/closed_auctions/closed_auction "
    + "where not( empty( $a/annotation/description/parlist/listitem/parlist/listitem/text/emph/"
    + "keyword/text() ) ) return <person id=\"{$a/seller/@person}\"/>",
    "let $auction := . return for $p in $auction/site/people/person "
    + "where empty($p/homepage/text()) return <person name=\"{$p/name/text()}\"/>",
    "declare namespace local = \"http://www.foobar.org\"; declare function "
    + "local:convert($v as xs:decimal?) as xs:decimal? { 2.20371 * $v (: convert Dfl to Euro :) }; "
    + "let $auction := . return for $i in $auction/site/open_auctions/open_auction "
    + "return local:convert(zero-or-one($i/reserve))",
    "let $auction := . return for $b in $auction/site/regions//item let $k := $b/name/text() "
    + "order by zero-or-one($b/location) ascending empty greatest return "
    + "<item name=\"{$k}\">{$b/location/text()}</item>",
    "let $auction := . return <result> <preferred> {count($auction/site/people/person/profile"
    + "[@income >= 100000])} </preferred> <standard> { count( $auction/site/people/person/"
    + "profile[@income < 100000 and @income >= 30000] ) } </standard> <challenge> {"
    + "count($auction/site/people/person/profile[@income < 30000])} </challenge> <na> { "
    + "count( for $p in $auction/site/people/person where empty($p/profile/@income) return $p ) } "
    + "</na> </result>"
  };

  /** Server flag. */
  private static BaseXServer server;

  /**
   * Initializes the tests.
   * @throws Exception any exception
   */
  @BeforeAll public static void init() throws Exception {
    // only start server if it is not already running
    if(!BaseXServer.ping(StaticOptions.HOST.value(), StaticOptions.PORT.value()))
      server = new BaseXServer();

    try(ClientSession cs = createClient(true)) {
      cs.execute("create db " + DB + " " + DBFILE);
      cs.execute("create user xmark xmark");
      cs.execute("grant read on " + DB + " to xmark");
    }
  }

  /**
   * Initializes the tests.
   * @throws IOException I/O exception
   */
  @AfterAll public static void close() throws IOException {
    // only stop server if it has not been running before starting the tests
    if(server != null) server.stop();
  }

  /**
   * Runs all tests and generates some test output.
   * @throws Exception any exception
   */
  @Test public void test() throws Exception {
    final IntList exclude = new IntList(new int[] { 11, 12 });
    final TokenBuilder tb = new TokenBuilder().add(DB).add(Prop.NL);

    try(ClientSession cs = createClient(false)) {
      cs.execute(new Open(DB));

      // ignore first run
      System.out.println("Warming up...");
      for(int i = 1; i <= 20; i++) {
        if(!exclude.contains(i)) {
          try(ClientQuery cq = cs.query(QUERIES[i - 1])) {
            final Performance p = new Performance();
            cq.execute();
            System.out.println(i + ": " + p);
          } catch(final BaseXException ex) {
            // too slow queries will be stopped after client timeout
            System.out.println(i + ": " + ex);
            exclude.add(i);
          }
        }
      }

      System.out.println(Prop.NL + "Testing...");
      for(int i = 1; i <= 20; i++) {
        tb.add(String.format("%02d", i)).add("  ");
        final BigDecimal max = BigDecimal.valueOf(MAX);
        try(ClientQuery cq = cs.query(QUERIES[i - 1])) {
          if(exclude.contains(i)) {
            tb.add("1000000");
          } else {
            double min = Double.MAX_VALUE;
            BigDecimal total = BigDecimal.valueOf(0);
            int r = 0;
            while(total.compareTo(max) < 0) {
              final Performance p = new Performance();
              cq.execute();
              final double t = Double.parseDouble(p.getTime().replaceAll(" .*", ""));
              total = total.add(BigDecimal.valueOf(t));
              min = Math.min(min, t);
              r++;
            }
            tb.add(Double.toString(min));
            System.out.println(i + ": " + min + " (" + r + " runs, stopped at " + total + " ms)");
          }
        }
        tb.add(Prop.NL);
      }
    }

    DIR.md();
    FILE.write(tb.finish());
  }

  /**
   * Creates a client instance.
   * @param admin admin user
   * @return client instance
   * @throws IOException I/O exception
   */
  private static ClientSession createClient(final boolean admin) throws IOException {
    final String user = admin ? UserText.ADMIN : USER;
    return new ClientSession(S_LOCALHOST, StaticOptions.PORT.value(), user, user);
  }
}
