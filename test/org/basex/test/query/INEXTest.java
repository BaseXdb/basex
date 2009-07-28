package org.basex.test.query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import org.basex.util.Array;
import org.basex.util.TokenList;

import static org.basex.util.Token.*;

/**
 * INEX Performance Test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class INEXTest {
  /** Database Context. */
  private final Context context = new Context();
  /** Stop after the specified number of queries. */
  private static final int STOPAFTER = Integer.MAX_VALUE;
  /** Query counter. */
  int curr = 0;
  /** Flag for short output. */
  private final boolean s = true;
  /** Kind of task. */
  private final String[] task = new String[] {"adhoc", "budget10", "budget100",
      "budget1000", "budget10000"};
  /** Kind of type. */
  private final String[] type = new String[] {"focused", "thorough", "article"};
  /** Kind of query. */
  private String[] query = new String[] {"automatic", "manual"};
  
  /** Method used to sum pathes. */
  private final String xqm =
    "declare namespace basex = \"http://www.basex.com\"; " +
    "declare function basex:sum-path ( $n as node()? )  as xs:string { " +
    " string-join( for $a in $n/ancestor-or-self::* " +
    " let $ssn := $a/../*[name() = name($a)] " +
    " return concat(name($a),'[',basex:index-of($ssn,$a),']'), '/')};" +
    "declare function basex:index-of (" +
    " $n as node()* , $ntf as node() )  as xs:integer* { " +
     "  for $s in (1 to count($n)) return $s[$n[$s] is $ntf]};";

  /**
   * Constructor.
   * @param db database instance
   * @param exe boolean for execution of the queries
   * @throws Exception exception
   */
  private INEXTest(final String db, final boolean exe) throws Exception {
    new Open(db).execute(context);

    // open query file
    final BufferedWriter out =
      new BufferedWriter(new FileWriter(new File("INEX/INEX1.log")));
    final PrintOutput sub = new PrintOutput("INEX/sub.xml");
    final XMLSerializer xml = new XMLSerializer(sub, false, true);

    final File file = new File("INEX/co.que");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }
    context.prop.set(Prop.INFO, true);

    if(s) {
      // print header in output file
      xml.openElement(token("efficiency-submission"),
          token("participant-id"), token("1111111"),
          token("run-id"), token("1111111"),
          token("taks"), token(task[0]),
          token("type"), token(type[0]),
          token("query"), token(query[0]),
          token("sequential"), token("yes")
      );
      xml.emptyElement(token("topic-fields"),
          token("co_title"), token("no"),
          token("cas_title"), token("no"),
          token("xpath_title"), token("yes"),
          token("text_predicates"), token("no"),
          token("description"), token("no"),
          token("narrative"), token("no")
      );
      xml.emptyElement(token("general_description"));
      xml.emptyElement(token("ranking_description"));
      xml.emptyElement(token("indexing_description"));
      xml.emptyElement(token("caching_description"));
    }

    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    final InputStreamReader isr = new InputStreamReader(fis, "UTF8");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    while((line = br.readLine()) != null) {
      // extract topic id
      int s0 = line.indexOf('"');
      int s1 = line.indexOf('"', s0 + 1);
      final int tid = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract content id
      s0 = line.indexOf('"', s1 + 1);
      s1 = line.indexOf('"', s0 + 1);
//      final int ctid = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract query
      s0 = line.indexOf('/', s1);
      String q = line.substring(s0);

//      q = q.replaceAll("\\. ", ".//text() ");
      final String qo = q;
      q = replaceElements(q);
      
      if (exe) {
        q = xqm + "for $i score $s in " + q 
          + " order by $s return (basex:sum-path($i), $s)";
  
//        Prop.allInfo = true;
        context.prop.set(Prop.ALLINFO, true); 
        // process query
        final Process proc = new XQuery(q);
        
        if(!proc.execute(context)) {
          System.out.println("- " + proc.info());
          System.out.println("Query: " + q);
        } else {
          // extract and print processing time
          final String info = proc.info();
          proc.output(new NullOutput());
          if (info.indexOf("Applying full-text index") < 0) { 
            System.out.println("No index usage: " + qo);
            System.out.println(q);
            System.out.println("****");
            
          }
          final int i = info.indexOf("Evaluating: ");
          final int j = info.indexOf(" ms", i);
          final String time = info.substring(i 
              + "Evaluating: ".length() + 2, j);
  
          if(s) {
            xml.openElement(token("topic"),
                token("topic-id"), token(tid),
                token("total_time_ms"), token(time)
            );
            xml.openElement(token("result"));
            xml.openElement(token("file"));
            xml.text(token(file.toString()));
            xml.closeElement();
  
            final Result val = proc.result();
            if(val instanceof SeqIter) {
              final SeqIter itr = (SeqIter) val;
              Item a;
              int r = 1;
              while((a = itr.next()) != null) {
                if(a instanceof Str) {
                  xml.openElement(token("path"));
                  xml.text(a.str());
                  xml.closeElement();
                  xml.openElement(token("rank"));
                  xml.text(token(r++));
                  xml.closeElement();
                } else if(a instanceof Dbl) {
                  xml.openElement(token("rsv"));
                  xml.text(a.str());
                  xml.closeElement();
                }
              }
            }
            xml.closeElement();
            xml.closeElement();
            sub.flush();
          }
        }
      }
      
      if(++curr >= STOPAFTER) break;
    }
    br.close();

    if(s) {
      xml.closeElement();
    }

    xml.close();
    sub.close();
    out.close();
  }
  
  /**
   * Replace dedicated nodes by an or expression.
   * [(a|b) ftcontains "c"] => [a ftcontains "c" or [b ftcontains "c"]
   * 
   * @param str Sting query to be replaced
   * @return replaced query String
   */
    private static String replaceElements(final String str) {
    byte[] b = str.getBytes();
    final byte[] or = new byte[]{' ', 'o', 'r', ' '};
    final byte[] co = new byte[]{' ', ',', ' '};
    final byte[] txt = new byte[]{'/', 't', 'e', 'x', 't', '(', ')'};
    int i = 0;
    int sp = -1, sb = -1, sbb = -1, os = -1, ebb = -1;
    byte[] path = new byte[]{};;
    TokenList tl;
    while(i < b.length) {
      switch (b[i]) {
        case '/': 
          sp = sp == -1 ? i : sp;
          break;
        case '(':
          sb = i;
          i++;
          if (b[i] == ')' || b[i] == ' ') break;
          boolean f = false;
          while(i < b.length && b[i] != ')' && !f) { 
            f = b[i] == '"' || b[i] == '\'';
            i++;
          }
          if (f) break;
          i = sb;
          
          path = new byte[i - sp];
          System.arraycopy(b, sp, path, 0, path.length);
          
          tl = new TokenList();
          os = i + 1;
          while (i < b.length && b[i] != ')') {
            if (b[i] == '|') {
              final byte[] tok = new byte[i - os];
              System.arraycopy(b, os, tok, 0, tok.length);
              tl.add(tok);
              os = i + 1;
            }
            i++;
          }
          if (tl.size() > 0) {
            final byte[] tok = new byte[i - os];
            System.arraycopy(b, os, tok, 0, tok.length);
            tl.add(tok);
            
            // backup path
            path = new byte[sb - sp];
            System.arraycopy(b, sp, path, 0, path.length);
          }
          i++;
          
          // check if predicate before or after ( | )
          if (sbb > -1 && ebb > -1) {
            // predicate before ( | )
            os = 0;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            for (int k = 0; k < tok.length; k++) {
              bn = Array.add(bn, b, 0, ebb + 1);
              bn = Array.add(bn, b, ebb + 1, sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              if (k < tl.size() - 1) 
                bn = Array.add(bn, co, 0, co.length);
            }
            i = bn.length;
            b = bn;
          } else if (sbb == -1 && ebb == -1) {
            // predicate after ( | )
            sbb = i;
            while (sbb < b.length && b[sbb] != '[') sbb++;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            for (int k = 0; k < tok.length; k++) {
              bn = Array.add(bn, b, 0, sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              bn = Array.add(bn, b, sbb, b.length);
              if (k < tok.length - 1) 
                bn = Array.add(bn, or, 0, or.length);
            }
            i = sb;
            b = bn;            
          } else {
            // ( | ) inside predicate
            ebb = i + 1;
            while (ebb < b.length && !(b[ebb] == ']' || 
                ebb + 2 < b.length && b[ebb] == ' ' && b[ebb + 1] == 'o' 
                  && b[ebb + 2] == 'r' ||
                ebb + 3 < b.length && b[ebb] == ' ' && b[ebb + 1] == 'a' 
                  && b[ebb + 2] == 'n' && b[ebb + 3] == 'd'
                  )) ebb++;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            bn = Array.add(bn, b, 0, sbb);
            for (int k = 0; k < tl.size(); k++) {
              bn = Array.add(bn, b, sbb + (k == 0 ? 0 : 1), sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              bn = Array.add(bn, b, i, ebb);
              if (k < tok.length - 1) 
                bn = Array.add(bn, or, 0, or.length);
            }
            bn = Array.add(bn, b, ebb, b.length);
            b = bn;
            ebb = -1; // new
          }
          break;
        case '[': 
          sbb = i;
          break;
        case ']': 
          ebb = i;
          break;
        case 'o':
          if (ebb > 0 && i + 1 < b.length && b[i + 1] == 'r') {
            sp = -1;
            sb = -1; 
            sbb = -1; 
            os = -1; 
            ebb = -1;
          }
          break;
        case '.':
          if (b[i + 1] != '/') {
            // . => .//text()
            final byte[] bn = new byte[b.length + 1 + txt.length];
            System.arraycopy(b, 0, bn, 0, i + 1);
            bn[i + 1] = '/';
            System.arraycopy(txt, 0, bn, i + 2, txt.length);
            System.arraycopy(b, i + 1, bn, i + 2 + txt.length, 
                b.length - i - 1);
            b = bn;
          } else {
            // .//foo => .//foo/text()
            final int j = i;
            while(i < b.length && b[i] != ' ') i++;
            if (b.length - 6 > 0 && b[i - 6] == 't' && b[i - 5] == 'e' 
              && b[i - 4] == 'x' && b[i - 3] == 't' && b[i - 2] == '(' 
              && b[i - 1] == ')') break;
              
            final byte[] bn = new byte[b.length + txt.length];
            System.arraycopy(b, 0, bn, 0, i);
            System.arraycopy(txt, 0, bn, i, txt.length);
            System.arraycopy(b, i, bn, i + txt.length, b.length - i);
            b = bn;
            i = j;
          }
        break;
      }
      i++;
    }
    return new String(b);
  }
  
  /**
   * Main test method.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new INEXTest("pages999", true);
  /*  System.out.println("1.: " + replaceElements(
        "//sec[. ftcontains \"b\" ] or //sec[. ftcontains \"c\" ]"));
    System.out.println("2.: " + replaceElements(
        "//sec[.//ab ftcontains \"b\" ]")); 
    System.out.println("3.: " + replaceElements(
        "//sec[.//(a|b) ftcontains \"b\" ]"));
    System.out.println("4.: " + replaceElements(
        "//(a|b)[. ftcontains \"b\" ]"));
    System.out.println("5.: " + replaceElements(
      "//(a|b)[.//cd ftcontains \"b\" ]"));
    System.out.println("6.: " + replaceElements(
      "//a[.//d ftcontains \"b\" ]//(e|f)"));
    System.out.println("7.: " + replaceElements(
      "//(a|b)[.//(c|d) ftcontains \"bla\"]"));
    System.out.println("8.: " + replaceElements(
      "article[. ftcontains (\"Nobel\" ftand \"prize\")]"));
    System.out.println("9.: " + replaceElements(
    "article[.//(a|b) ftcontains \"Nobel\" and 
    .//st ftcontains (\"references\" ]"));
    System.out.println("10.: " + replaceElements(
    "//article[.//(sec|p) ftcontains (\"mean\" ftand \"average\" " +
    "ftand \"precision\") and ( .//st ftcontains (\"references\") " +
    "or .//st ftcontains (\"see\" ftand \"also\") ) and .//image " +
    "ftcontains (\"precision\" ftand \"recall\")]"));

  /* */
  }
}
