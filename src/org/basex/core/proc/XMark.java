package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.data.Result;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.basex.util.Array;

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
  private XMLSerializer out;
  /** XMark reference. */
  private int xmark;

  @Override
  protected boolean exec() {
    xmark = toInt(cmd.arg(0));
    return xmark < 1 || xmark > 20 ? error(XMARKWHICH) : true;
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    data = context.data();
    out = new XMLSerializer(o);
    try {
      int hits = 0;
      if(!Prop.serialize) out = new XMLSerializer(new NullOutput());
      for(int i = 0; i < Prop.runs; i++) {
        if(i != 0) out = new XMLSerializer(new NullOutput(!Prop.serialize));
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
    final Nodes b = qu("/site/regions/namerica/item[@id='item0']/name/text()");
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
    final byte[] inc = token("increase");

    final int bs = b.size;
    for(int bi = 0; bi < bs; bi++) {
      writeSep(bi);
      writeTag(inc, b, bi, stepBidder1);
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
    final byte[] inc = token("increase");
    final byte[] first = token("first");
    final byte[] last = token("last");

    int hits = 0;
    final int bs = b.size;
    for(int bi = 0; bi < bs; bi++) {
      writeSep(hits++);
      out.startElement(inc);
      out.attribute(first, atom(b, bi, stepInc1));
      out.attribute(last, atom(b, bi, stepInc2));
      out.emptyElement();
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
    final byte[] hist = token("history");

    int hits = 0;
    final int bs = b.size;
    for(int bi = 0; bi < bs; bi++) {
      final Nodes c1 = eval(stepRef1, b, bi);
      if(c1.size == 0) continue;
      final Nodes c2 = eval(stepRef2, b, bi);
      if(c2.size == 0) continue;

      if(c1.pre[0] < c2.pre[0]) {
        writeSep(hits++);
        writeTag(hist, b, bi, stepReserve);
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
    final Nodes i = qu(
        "/site/closed_auctions/closed_auction[price/text() >= 40]/price");
    out.out.print(token(i.size));
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
    final byte[] item = token("item");
    final byte[] person = token("person");

    final int ps = p.size;
    for(int pi = 0; pi < ps; pi++) {
      writeSep(pi);
      out.openElement(item, person, atom(p, pi, stepName));
      parse("count(.[buyer/@person = '" + string(atom(p, pi, stepID)) + "'])").
        eval(c).serialize(out);
      out.closeElement(item);
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
    final byte[] person = token("person");
    final byte[] name = token("name");
    final byte[] item = token("item");

    final int ps = p.size;
    for(int pi = 0; pi < ps; pi++) {
      writeSep(pi);
      out.startElement(person);
      out.attribute(name, atom(p, pi, stepName));

      final byte[] token = atom(p, pi, stepID);
      final Nodes a = eval(parse(".[buyer/@person = '" +
          string(token) + "']"), c);
      if(a.size == 0) {
        out.emptyElement();
      } else {
        out.finishElement();
        final int as = a.size;
        for(int ai = 0; ai < as; ai++) {
          final Nodes n = eval(parse(".[@id = '" +
              string(atom(a, ai, stepItemRef)) + "']"), i);
          if(n.size == 0) {
            out.emptyElement(item);
          } else {
            out.openElement(item);
            final int ns = n.size;
            for(int ni = 0; ni < ns; ni++) out.text(atom(n, ni, stepName));
            out.closeElement(item);
          }
        }
        out.closeElement(person);
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
    final byte[] cat = token("categorie");
    final byte[] id = token("id");
    final byte[] pers = token("personne");
    final byte[] stat = token("statistique");
    final byte[] sexe = token("sexe");
    final byte[] age = token("age");
    final byte[] edu = token("education");
    final byte[] rev = token("revenu");
    final byte[] nom = token("nom");
    final byte[] rue = token("rue");
    final byte[] pays = token("pays");
    final byte[] ville = token("ville");
    final byte[] courrier = token("courrier");
    final byte[] page = token("pageperso");
    final byte[] carte = token("cartePaiement");
    final byte[] cord = token("cordonnees");
    final byte[] reseau = token("reseau");

    final byte[][] idist = distinctvalues(
        qu("/site/people/person/profile/interest/@category"));

    final int cs = idist.length;
    for(int ci = 0; ci < cs; ci++) {
      final String category = string(idist[ci]);
      writeSep(ci);
      out.openElement(cat);
      out.out.print(NL);
      out.openElement(id);
      out.out.print(category);
      out.closeElement(cat);
      out.out.print(NL);

      final Nodes p = eval(parse(".[profile/interest/@category = '" +
          category + "']"), person);

      final int ps = p.size;
      for(int pi = 0; pi < ps; pi++) {
        out.openElement(pers);
        out.out.print(NL);
        out.openElement(stat);
        out.out.print(NL);
        out.out.print("    ");
        writeTag(sexe, p, pi, stepGender);
        out.out.print(NL);
        out.out.print("    ");
        writeTag(age, p, pi, stepAge);
        out.out.print(NL);
        out.out.print("    ");
        writeTag(edu, p, pi, stepEducation);
        out.out.print(NL);
        out.out.print("    ");
        writeTag(rev, p, pi, stepIncome);
        out.out.print(NL);
        out.closeElement(stat);
        out.out.print(NL);
        out.openElement(cord);
        out.out.print(NL);
        out.out.print("    ");
        writeTag(nom, p, pi, stepName);
        out.out.print(NL);
        out.out.print("    ");
        writeTag(rue, p, pi, stepStreet);
        out.out.print(NL);
        out.out.print("    ");
        writeTag(ville, p, pi, stepCity);
        out.out.print(NL);
        out.out.print("    ");
        writeTag(pays, p, pi, stepCountry);
        out.out.print(NL);
        out.out.print("    ");
        out.openElement(reseau);
        out.out.print(NL);
        out.out.print("      ");
        writeTag(courrier, p, pi, stepEmail);
        out.out.print(NL);
        out.out.print("      ");
        writeTag(page, p, pi, stepHomepage);
        out.out.print(NL);
        out.closeElement(reseau);
        out.out.print(NL);
        out.closeElement(cord);
        out.out.print(NL);
        out.out.print("  ");
        writeTag(carte, p, pi, stepCreditcard);
        out.out.print(NL);
        out.closeElement(pers);
        out.out.print(NL);
      }
      out.closeElement(cat);
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
    final byte[] items = token("items");
    final byte[] name = token("name");

    final int ps = p.size;
    for(int pi = 0; pi < ps; pi++) {
      writeSep(pi);
      final Nodes tmp = eval(stepIncome, p, pi);
      out.openElement(items, name, tmp.size != 0 ?
          data.atom(tmp.pre[0]) : EMPTY);

      if(tmp.size != 0) {
        parse("count(.[" + data.atomNum(tmp.pre[0]) +
          " > 5000 * text()])").eval(i).serialize(out);
      } else {
        out.out.print("0");
      }
      out.closeElement(items);
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
    final byte[] items = token("items");
    final byte[] pers = token("person");

    final int hits = 0;
    final int ps = p.size;
    for(int pi = 0; pi < ps; pi++) {
      writeSep(pi);
      final Nodes tmp = eval(stepIncome, p, pi);
      out.openElement(items, pers, data.atom(tmp.pre[0]));
      parse("count(.[" + data.atomNum(tmp.pre[0]) +
          " > 5000 * text()])").eval(i).serialize(out);
      out.closeElement(items);
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
    final byte[] item = token("item");
    final byte[] name = token("name");

    final int hits = 0;
    final int is = i.size;
    for(int ii = 0; ii < is; ii++) {
      writeSep(ii);
      out.openElement(item, name, atom(i, ii, stepName));
      eval(stepDesc, i, ii).serialize(out);
      out.closeElement(item);
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
    final byte[] text = token("text");

    final int as = a.size;
    for(int ai = 0; ai < as; ai++) {
      writeSep(ai);
      out.openElement(text);
      out.text(data.atom(a.pre[ai]));
      out.closeElement(text);
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
    final byte[] pers = token("person");
    final byte[] id = token("id");

    final int as = a.size;
    for(int ai = 0; ai < as; ai++) {
      writeSep(ai);
      out.emptyElement(pers, id, data.atom(eval(stepPerson, a, ai).pre[0]));
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
    final byte[] pers = token("person");
    final byte[] name = token("name");

    final int ps = p.size;
    for(int pi = 0; pi != ps; pi++) {
      writeSep(pi);
      out.emptyElement(pers, name, atom(p, pi, stepName));
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
      out.out.print(token(convert(data.atomNum(i.pre[ii]))));
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
    final byte[] item = token("item");
    final byte[] name = token("name");

    final Nodes k = eval(stepName, b);
    sort(b, k);

    final int bs = b.size;
    for(int bi = 0; bi < bs; bi++) {
      writeSep(bi);
      out.openElement(item, name, atom(b, bi, stepName));
      out.text(atom(b, bi, stepLocation));
      out.closeElement(item);
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
    final byte[] res = token("result");
    final byte[] pref = token("preferred");
    final byte[] std = token("standard");
    final byte[] chal = token("challenge");
    final byte[] na = token("na");
    
    out.openElement(res);
    out.out.print(NL);
    out.openElement(pref);
    parse("count(/site/people/person/profile[@income >= 100000])").eval(
        new Nodes(0, data)).serialize(out);
    out.closeElement(pref);
    out.out.print(NL);

    out.openElement(std);
    parse("count(/site/people/person/profile[@income < 100000 and " +
      "@income >= 30000])").eval(new Nodes(0, data)).serialize(out);
    out.closeElement(std);
    out.out.print(NL);

    out.openElement(chal);
    parse("count(/site/people/person/profile[@income < 30000])").eval(
        new Nodes(0, data)).serialize(out);
    out.closeElement(chal);
    out.out.print(NL);

    out.openElement(na);
    parse("count(/site/people/person[not(profile/@income)])").eval(
        new Nodes(0, data)).serialize(out);
    out.closeElement(na);
    out.out.print(NL);
    out.closeElement(res);
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
  private byte[] atom(final Nodes in, final int pre,
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
  private void writeTag(final byte[] tag, final Nodes in, final int pre,
      final XPathProcessor steps) throws Exception {
    final Nodes tmp = eval(steps, in, pre);
    out.startElement(tag);
    if(tmp.size == 0) {
      out.emptyElement();
    } else {
      out.finishElement();
      for(int t = 0; t < tmp.size; t++) out.text(data.atom(tmp.pre[0]));
      out.closeElement(tag);
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
}
