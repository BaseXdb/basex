package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.PrintSerializer;
import org.basex.data.Result;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * Evaluates the 'xmark' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XMark extends XPath {
  /** Data reference. */
  private Data data;
  /** Serializer. */
  private PrintSerializer out;
  /** XMark reference. */
  private int xmark;

  @Override
  protected boolean exec() {
    xmark = Token.toInt(cmd.arg(0));
    return xmark < 1 || xmark > 20 ? error(XMARKWHICH) : true;
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    data = context.data();
    out = new PrintSerializer(o);
    try {
      int hits = 0;
      if(!Prop.serialize) out = new PrintSerializer(new NullOutput());
      for(int i = 0; i < Prop.runs; i++) {
        if(i != 0) out = new PrintSerializer(new NullOutput(!Prop.serialize));
        hits = process(xmark);
        out.out.print(NL);
      }
      if(Prop.info) outInfo(out.out, hits);
    } catch(final IOException ex) {
      throw ex;
    } catch(final Exception ex) {
      BaseX.debug(ex);
      info(ex.getMessage());
    }
  }

  /**
   * Processes the specified XMark query.
   * @param nr number of the XMark query
   * @return number of hits
   * @throws Exception exception
   */
  private int process(final int nr) throws Exception {
    switch(nr) {
      case 1: return xmark1();
      case 2: return xmark2();
      case 3: return xmark3();
      case 4: return xmark4();
      case 5: return xmark5();
      case 6: return xmark6();
      case 7: return xmark7();
      case 8: return xmark8();
      case 9: return xmark9();
      case 10: return xmark10();
      case 11: return xmark11();
      case 12: return xmark12();
      case 13: return xmark13();
      case 14: return xmark14();
      case 15: return xmark15();
      case 16: return xmark16();
      case 17: return xmark17();
      case 18: return xmark18();
      case 19: return xmark19();
      case 20: return xmark20();
      default: return 0;
    }
  }

   /**
   * Benchmark Query 1:<br/>
   * Return the name of the item with ID 'item20748' registered in North
   * America.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark1() throws Exception {
    final Nodes b = qu("/site/regions/namerica/item[@id='item0']" +
        "/name/text()");
    b.serialize(out);
    return b.size;
  }

  /**
   * Benchmark Query 2:<br/>
   * Return the initial increases of all open auctions.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark2() throws Exception {
    final Nodes b = qu("/site/open_auctions/open_auction");
    final XPathProcessor stepBidder1 = parse("bidder[1]/increase/text()");

    final int bs = b.size;
    for(int bi = 0; bi < bs; bi++) {
      writeSep(bi);
      writeTag("increase", b, bi, stepBidder1);
    }
    return b.size;
  }

  /**
   * Benchmark Query 3:<br/>
   * Return the first and current increases of all open auctions whose current
   * increase is at least twice as high as the initial increase.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark3() throws Exception {
    final Nodes b = qu("/site/open_auctions/open_auction" +
      "[bidder[1]/increase/text() * 2 <= bidder[last()]/increase/text()]");
    final XPathProcessor stepInc1 = parse("bidder[1]/increase/text()");
    final XPathProcessor stepInc2 = parse("bidder[last()]/increase/text()");

    int hits = 0;
    final int bs = b.size;
    for(int bi = 0; bi < bs; bi++) {
      writeSep(hits++);
      out.out.print("<increase first=\"");
      out.out.printToken(token(b, bi, stepInc1), data.meta.entity);
      out.out.print("\" last=\"");
      out.out.printToken(token(b, bi, stepInc2), data.meta.entity);
      out.out.print("\"/>");
    }
    return hits;
  }

  /**
   * Benchmark Query 4:<br/>
   * List the reserves of those open auctions where a certain person issued
   * issued a big before another person.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark4() throws Exception {
    final Nodes b = qu("/site/open_auctions/open_auction");
    final XPathProcessor stepRef1 = parse(
        "bidder/personref[@person = 'person18829']");
    final XPathProcessor stepRef2 = parse(
        "bidder/personref[@person = 'person10487']");
    final XPathProcessor stepReserve = parse("initial/text()");

    int hits = 0;
    final int bs = b.size;
    for(int bi = 0; bi < bs; bi++) {
      final Nodes c1 = eval(stepRef1, b, bi);
      if(c1.size == 0) continue;
      final Nodes c2 = eval(stepRef2, b, bi);
      if(c2.size == 0) continue;

      if(c1.pre[0] < c2.pre[0]) {
        writeSep(hits++);
        writeTag("history", b, bi, stepReserve);
      }
    }
    return hits;
  }

  /**
   * Benchmark Query 5:<br/>
   * How many sold items cost more than 40?<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark5() throws Exception {
    final Nodes i = qu("/site/closed_auctions/closed_auction[price/" +
        "text() >= 40]/price");
    out.out.print(Token.token(i.size));
    return 1;
  }

  /**
   * Benchmark Query 6:<br/>
   * How many items are listed on all continents?<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark6() throws Exception {
    final Nodes nodes = new Nodes(0, data);
    final Result val = parse("count(/site/regions//item)").eval(nodes);
    val.serialize(out);
    return 1;
  }

  /**
   * Benchmark Query 7:<br/>
   * How many pieces of prose are in our database?<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark7() throws Exception {
    final Result val = parse("count(/site//description) + count(/site//mail) " +
      "+ count(/site//email)").eval(new Nodes(0, data));
    val.serialize(out);
    return 1;
  }

  /**
   * Benchmark Query 8:<br/>
   * List the names of persons and the number of items they bought.
   * (joins person, closed auction)<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark8() throws Exception {
    final Nodes p = qu("/site/people/person");
    final Nodes c = qu("/site/closed_auctions/closed_auction");
    final XPathProcessor stepName = parse("name/text()");
    final XPathProcessor stepID   = parse("@id");

    final int ps = p.size;
    for(int pi = 0; pi < ps; pi++) {
      writeSep(pi);
      out.out.print("<item person=\"");
      out.out.printToken(token(p, pi, stepName), data.meta.entity);
      out.out.print("\">");

      final byte[] token = token(p, pi, stepID);
      parse("count(.[buyer/@person = '" + Token.string(token) + "'])").
        eval(c).serialize(out);
      out.out.print("</item>");
    }
    return p.size;
  }

  /**
   * Benchmark Query 9:<br/>
   * List the names of persons and the names of the items they bought
   * in Europe. (joins person, closed auction, item)<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark9() throws Exception {
    final Nodes p = qu("/site/people/person");
    final Nodes c = qu("/site/closed_auctions/closed_auction");
    final Nodes i = qu("/site/regions/europe/item");
    final XPathProcessor stepName    = parse("name/text()");
    final XPathProcessor stepItemRef = parse("itemref/@item");
    final XPathProcessor stepID      = parse("@id");

    final int ps = p.size;
    for(int pi = 0; pi < ps; pi++) {
      writeSep(pi);
      out.out.print("<person name=\"");
      out.out.printToken(token(p, pi, stepName), data.meta.entity);

      final byte[] token = token(p, pi, stepID);
      final Nodes a = eval(parse(".[buyer/@person = '" +
          Token.string(token) + "']"), c);
      if(a.size == 0) {
        out.out.print("\"/>");
      } else {
        out.out.print("\">");
        final int as = a.size;
        for(int ai = 0; ai < as; ai++) {
          final Nodes n = eval(parse(".[@id = '" +
              Token.string(token(a, ai, stepItemRef)) + "']"), i);
          if(n.size == 0) {
            out.out.print("<item/>");
          } else {
            out.out.print("<item>");
            final int ns = n.size;
            for(int ni = 0; ni < ns; ni++) {
              out.out.printToken(token(n, ni, stepName), data.meta.entity);
            }
            out.out.print("</item>");
          }
        }
        out.out.print("</person>");
      }
    }
    return p.size;
  }

  /**
   * Benchmark Query 10:<br/>
   * List all persons according to their interest;
   * use French markup in the result.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark10() throws Exception {
    final XPathProcessor stepGender     = parse("profile/gender/text()");
    final XPathProcessor stepAge        = parse("profile/age/text()");
    final XPathProcessor stepEducation  = parse("profile/education/text()");
    final XPathProcessor stepIncome     = parse("profile/@income");
    final XPathProcessor stepName       = parse("name/text()");
    final XPathProcessor stepStreet     = parse("address/street/text()");
    final XPathProcessor stepCity       = parse("address/city/text()");
    final XPathProcessor stepCountry    = parse("address/country/text()");
    final XPathProcessor stepEmail      = parse("emailaddress/text()");
    final XPathProcessor stepHomepage   = parse("homepage/text()");
    final XPathProcessor stepCreditcard = parse("creditcard/text()");
    final Nodes person = qu("/site/people/person");

    final byte[][] idist = distinctvalues(
        qu("/site/people/person/profile/interest/@category"));

    final int cs = idist.length;
    for(int ci = 0; ci < cs; ci++) {
      final String category = Token.string(idist[ci]);
      writeSep(ci);
      out.out.print("<categorie>");
      out.out.print(NL);
      out.out.print("<id>");
      out.out.print(category);
      out.out.print("</id>");
      out.out.print(NL);

      final Nodes p = eval(parse(".[profile/interest/@category = '" +
          category + "']"), person);

      final int ps = p.size;
      for(int pi = 0; pi < ps; pi++) {
        out.out.print("<personne>");
        out.out.print(NL);
        out.out.print("  <statistiques>");
        out.out.print(NL);
        out.out.print("    ");
        writeTag("sexe", p, pi, stepGender);
        out.out.print(NL);
        out.out.print("    ");
        writeTag("age", p, pi, stepAge);
        out.out.print(NL);
        out.out.print("    ");
        writeTag("education", p, pi, stepEducation);
        out.out.print(NL);
        out.out.print("    ");
        writeTag("revenu", p, pi, stepIncome);
        out.out.print(NL);
        out.out.print("  </statistiques>");
        out.out.print(NL);
        out.out.print("  <coordonnees>");
        out.out.print(NL);
        out.out.print("    ");
        writeTag("nom", p, pi, stepName);
        out.out.print(NL);
        out.out.print("    ");
        writeTag("rue", p, pi, stepStreet);
        out.out.print(NL);
        out.out.print("    ");
        writeTag("ville", p, pi, stepCity);
        out.out.print(NL);
        out.out.print("    ");
        writeTag("pays", p, pi, stepCountry);
        out.out.print(NL);
        out.out.print("    ");
        out.out.print("<reseau>");
        out.out.print(NL);
        out.out.print("      ");
        writeTag("courrier", p, pi, stepEmail);
        out.out.print(NL);
        out.out.print("      ");
        writeTag("pagePerso", p, pi, stepHomepage);
        out.out.print(NL);
        out.out.print("    </reseau>");
        out.out.print(NL);
        out.out.print("  </coordonnees>");
        out.out.print(NL);
        out.out.print("  ");
        writeTag("cartePaiement", p, pi, stepCreditcard);
        out.out.print(NL);
        out.out.print("</personne>");
        out.out.print(NL);
      }
      out.out.print("</categorie>");
    }
    return idist.length;
  }

  /**
   * Benchmark Query 11:<br/>
   * For each person, list the number of items currently on sale whose
   * price does not exceed 0.02% of the person's income.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark11() throws Exception {
    final Nodes p = qu("/site/people/person");
    final Nodes i = qu("/site/open_auctions/open_auction/initial");
    final XPathProcessor stepIncome = parse("profile/@income");

    final int ps = p.size;
    for(int pi = 0; pi < ps; pi++) {
      writeSep(pi);
      out.out.print("<items name=\"");
      final Nodes tmp = eval(stepIncome, p, pi);
      if(tmp.size != 0) out.out.printToken(data.atom(tmp.pre[0]), 
          data.meta.entity);
      out.out.print("\">");

      if(tmp.size != 0) {
        parse("count(.[" + data.atomNum(tmp.pre[0]) +
          " > 5000 * text()])").eval(i).serialize(out);
      } else {
        out.out.print("0");
      }
      out.out.print("</items>");
    }
    return p.size;
  }

  /**
   * Benchmark Query 12:<br/>
   * For each richer-than-average person, list the number of items currently
   * on sale whose price does not exceed 0.02% of the person's income.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark12() throws Exception {
    final Nodes p = qu("/site/people/person[profile/@income > 50000]");
    final Nodes i = qu("/site/open_auctions/open_auction/initial");
    final XPathProcessor stepIncome = parse("profile/@income");

    final int hits = 0;
    final int ps = p.size;
    for(int pi = 0; pi < ps; pi++) {
      writeSep(pi);
      out.out.print("<items person=\"");
      final Nodes tmp = eval(stepIncome, p, pi);
      out.out.printToken(data.atom(tmp.pre[0]), data.meta.entity);
      out.out.print("\">");

      parse("count(.[" + data.atomNum(tmp.pre[0]) +
          " > 5000 * text()])").eval(i).serialize(out);

      out.out.print("</items>");
    }
    return hits;
  }

  /**
   * Benchmark Query 13:<br/>
   * List the names of items registered in Australia along with their
   * descriptions.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark13() throws Exception {
    final Nodes i = qu("/site/regions/australia/item");
    final XPathProcessor stepName = parse("name/text()");
    final XPathProcessor stepDesc = parse("description");

    final int hits = 0;
    final int is = i.size;
    for(int ii = 0; ii < is; ii++) {
      writeSep(ii);
      out.out.print("<item name=\"");
      out.out.printToken(token(i, ii, stepName), data.meta.entity);
      out.out.print("\">");
      eval(stepDesc, i, ii).serialize(out);
      out.out.print("</item>");
    }
    return hits;
  }

  /**
   * Benchmark Query 14:<br/>
   * Return the names of all items whose description contains the word 'gold'.
   * <br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark14() throws Exception {
    final Nodes i =
      qu("/site//item[contains(description, 'gold')]/name/text()");
    i.serialize(out);
    return i.size;
  }

  /**
   * Benchmark Query 15:<br/>
   * Print the keywords in emphasis in annotations of closed auctions.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark15() throws Exception {
    final Nodes a = qu("/site/closed_auctions/closed_auction/" +
        "annotation/description/parlist/listitem/parlist/listitem/text/" +
        "emph/keyword/text()");

    final int as = a.size;
    for(int ai = 0; ai < as; ai++) {
      writeSep(ai);
      out.out.print("<text>");
      out.out.printToken(data.atom(a.pre[ai]), data.meta.entity);
      out.out.print("</text>");
    }
    return a.size;
  }

  /**
   * Benchmark Query 16:<br/>
   * Confer Q15. Return the IDs of the sellers of those auctions that
   * have one or more keywords in emphasis.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark16() throws Exception {
    final Nodes a = qu("/site/closed_auctions/closed_auction[" +
        "annotation/description/parlist/listitem/parlist/listitem/text/" +
        "emph/keyword/text()]");
    final XPathProcessor stepPerson = parse("seller/@person");

    final int as = a.size;
    for(int ai = 0; ai < as; ai++) {
      writeSep(ai);
      out.out.print("<person id=\"");
      final Nodes tmp = eval(stepPerson, a, ai);
      out.out.printToken(data.atom(tmp.pre[0]), data.meta.entity);
      out.out.print("\"/>");
    }
    return a.size;
  }

  /**
   * Benchmark Query 17:<br/>
   * Which persons don't have a homepage?<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark17() throws Exception {
    final Nodes p = qu("/site/people/person[not(homepage/text())]");
    final XPathProcessor stepName     = parse("name/text()");

    final int ps = p.size;
    for(int pi = 0; pi != ps; pi++) {
      writeSep(pi);
      out.out.print("<person name=\"");
      out.out.printToken(token(p, pi, stepName), data.meta.entity);
      out.out.print("\"/>");
    }
    return p.size;
  }

  /**
   * Benchmark Query 18:<br/>
   * Convert the currency of the reserve of all open auctions to
   * another currency.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark18() throws Exception {
    final Nodes i = qu("/site/open_auctions/open_auction/reserve/text()");

    final int is = i.size;
    for(int ii = 0; ii != is; ii++) {
      writeSep(ii);
      out.out.print(Token.token(convert(data.atomNum(i.pre[ii]))));
    }
    return i.size;
  }

  /**
   * Local function to convert specified double into new double value.
   * @param d value to be converted
   * @return converted value
   */
  private static double convert(final double d) {
    return Math.round(d * 22037100) / 10000000.0d;
  }

  /**
   * Benchmark Query 19:<br/>
   * Give an alphabetically ordered list of all items along with their
   * location.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark19() throws Exception {
    final Nodes b = qu("/site/regions//item");
    final XPathProcessor stepName     = parse("name/text()");
    final XPathProcessor stepLocation = parse("location/text()");
    final Nodes k = eval(stepName, b);
    sort(b, k);

    final int bs = b.size;
    for(int bi = 0; bi < bs; bi++) {
      writeSep(bi);
      out.out.print("<item name=\"");
      out.out.printToken(token(b, bi, stepName), data.meta.entity);
      out.out.print("\">");
      out.out.printToken(token(b, bi, stepLocation), data.meta.entity);
      out.out.print("</item>");
    }
    return b.size;
  }

  /**
   * Benchmark Query 20:<br/>
   * Group customers by their income and output the cardinality of each
   * group.<br/>
   *
   * @return number of hits
   * @throws Exception exception
   */
  private int xmark20() throws Exception {
    out.out.print("<result>");
    out.out.print(NL);
    out.out.print("  <preferred>");
    parse("count(/site/people/person/profile[@income >= 100000])").eval(
        new Nodes(0, data)).serialize(out);
    out.out.print("</preferred>");
    out.out.print(NL);

    out.out.print("  <standard>");
    parse("count(/site/people/person/profile[@income < 100000 and " +
      "@income >= 30000])").eval(new Nodes(0, data)).serialize(out);
    out.out.print("</standard>");
    out.out.print(NL);

    out.out.print("  <challenge>");
    parse("count(/site/people/person/profile[@income < 30000])").eval(
        new Nodes(0, data)).serialize(out);
    out.out.print("</challenge>");
    out.out.print(NL);

    out.out.print("  <na>");
    parse("count(/site/people/person[not(profile/@income)])").eval(
        new Nodes(0, data)).serialize(out);
    out.out.print("</na>");
    out.out.print(NL);
    out.out.print("</result>");
    return 1;
  }

  /**
   * Returns an atomized token for the specified context node.
   * @param in context set
   * @param pre pre value
   * @param xpath xpath steps
   * @return result set
   * @throws QueryException query exception
   */
  private byte[] token(final Nodes in, final int pre,
      final XPathProcessor xpath) throws QueryException {
    return data.atom(eval(xpath, in, pre).pre[0]);
  }

  /**
   * Processes BaseXQueryExpr for the root node.
   * @param xpath xpath
   * @return result set
   * @throws QueryException query exception
   */
  private Nodes qu(final String xpath) throws QueryException {
    return eval(parse(xpath), new Nodes(0, data));
  }

  /**
   * Processes BaseXQueryExpr for the root node.
   * @param xpath xpath
   * @return result set
   * @throws QueryException query exception
   */
  private XPathProcessor parse(final String xpath) throws QueryException {
    final XPathProcessor xp = new XPathProcessor(xpath);
    xp.compile(new Nodes(0, data));
    return xp;
  }

  /**
   * Processes a query for the specified context node.
   * @param xp xpath steps
   * @param in context set
   * @param pre pre value
   * @return result set
   * @throws QueryException query exception
   */
  private Nodes eval(final XPathProcessor xp, final Nodes in,
      final int pre) throws QueryException {
    return eval(xp, new Nodes(in.pre[pre], data));
  }

  /**
   * Processes BaseXQueryExpr for the specified context node.
   * @param xp xpath steps
   * @param in context set
   * @return result set
   * @throws QueryException query exception
   */
  private Nodes eval(final XPathProcessor xp, final Nodes in)
      throws QueryException {
    return (Nodes) xp.eval(in);
  }

  /**
   * Adds a comma to the output stream if necessary.
   * @param c result counter
   * @throws IOException in case of writing problems
   */
  private void writeSep(final int c) throws IOException {
    if(c > 0) {
      out.out.print(",");
      out.out.print(NL);
    }
  }

  /**
   * Writes the tag of the specified context node.
   * @param tag tag name
   * @param in context set
   * @param pre pre value
   * @param steps xpath steps
   * @throws Exception exception
   */
  private void writeTag(final String tag, final Nodes in, final int pre,
      final XPathProcessor steps) throws Exception {
    final Nodes tmp = eval(steps, in, pre);
    out.out.print("<");
    out.out.print(tag);
    if(tmp.size == 0) {
      out.out.print("/>");
    } else {
      out.out.print(">");
      for(int t = 0; t < tmp.size; t++) {
        out.out.printToken(data.atom(tmp.pre[0]), data.meta.entity);
      }
      out.out.print("</");
      out.out.print(tag);
      out.out.print(">");
    }
  }

  /**
   * Creates distinct tokens.
   * @param in context set
   * @return distinct tokens
   */
  private byte[][] distinctvalues(final Nodes in) {
    final int is = in.size;
    int vs = 0;
    final byte[][] values = new byte[in.size][];

    for(int i = 0; i < is; i++) {
      final byte[] token = data.atom(in.pre[i]);
      int j = -1;
      while(++j < vs) if(equal(values[j], token)) break;
      if(j == vs) values[vs++] = token;
    }
    return Array.finish(values, vs);
  }

  /**
   * Sorts the specified input set after the second sort set.
   * @param in input set
   * @param sort sort set
   */
  private void sort(final Nodes in, final Nodes sort) {
    final int size = in.size;
    final byte[][] st = new byte[size][];
    for(int i = 0; i < size; i++) st[i] = data.atom(sort.pre[i]);
    sort(in, st, 0, in.size - 1);
  }

  /**
   * Recursively sorts the specified input set via QuickSort.
   * @param in input set
   * @param sort set to be sorted after
   * @param s start position
   * @param e end position
   */
  private void sort(final Nodes in, final byte[][] sort, final int s,
      final int e) {
    if(e <= s) return;
    int i = s - 1;
    int j = e;
    while(true) {
      while(diff(sort[++i], sort[e]) < 0);
      while(i != j && diff(sort[--j], sort[e]) > 0);
      if(i >= j) break;
      swap(in, sort, i, j);
    }
    swap(in, sort, i, e);
    sort(in, sort, s, i - 1);
    sort(in, sort, i + 1, e);  }

  /**
   * Swaps two entries.
   * @param in input set
   * @param sort sort set
   * @param a first position
   * @param b second position
   */
  private void swap(final Nodes in, final byte[][] sort, final int a,
      final int b) {
    final byte[] tmp = sort[a];
    sort[a] = sort[b];
    sort[b] = tmp;
    swap(in, a, b);
  }

  /**
   * Swaps two pre values.
   * @param nodes node set
   * @param a first position
   * @param b second position
   */
  private void swap(final Nodes nodes, final int a, final int b) {
    final int p = nodes.pre[a];
    nodes.pre[a] = nodes.pre[b];
    nodes.pre[b] = p;
  }

  /**
   * Compares two character arrays for equality.
   * @param token1 first token to be compared
   * @param token2 second token to be compared
   * @return true if the arrays are equal
   */
  private static boolean equal(final byte[] token1, final byte[] token2) {
    final int l = token1.length;
    if(l != token2.length) return false;
    for(int i = 0; i < l; i++) {
      if(token1[i] != token2[i]) return false;
    }
    return true;
  }

  /**
   * Calculates the difference between two tokens.
   * @param to first token
   * @param qu second token
   * @return difference
   */
  private int diff(final byte[] to, final byte[] qu) {
    final int tl = to.length;
    final int ql = qu.length;
    final int l = tl < ql ? tl : ql;
    int i = -1;
    while(++i < l) {
      final int c = to[i] - qu[i];
      if(c != 0) return c;
    }
    return tl - ql;
  }
}
