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
   * @throws Exception exception
   */
  private INEXTest(final String db) throws Exception {
    new Open(db).execute(context, null);

    // open query file
    final BufferedWriter out =
      new BufferedWriter(new FileWriter(new File("INEX/INEX1.log")));
    final PrintOutput sub = new PrintOutput("INEX/sub.xml");
    final XMLSerializer xml = new XMLSerializer(sub, false, true);

    final File file = new File("INEX/co1.que");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }
    Prop.serialize = true;
    Prop.info = true;

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
      //final int ctid = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract query
      s0 = line.indexOf('/', s1);
      String q = line.substring(s0);

      q = q.replaceAll("\\. ", ".//text() ");
      q = replaceDedicatedNodes(q);
      
      q = xqm + "for $i score $s in " + q 
        + " order by $s return (basex:sum-path($i), $s)";

      // process query
      final Process proc = new XQuery(q);

      if(!proc.execute(context)) {
        System.out.println("- " + proc.info());
        System.out.println("Query: " + q);
      } else {
        // extract and print processing time
        final String info = proc.info();
        proc.output(new NullOutput());
        final int i = info.indexOf("Evaluating: ");
        final int j = info.indexOf(" ms", i);
        final String time = info.substring(i + "Evaluating: ".length() + 2, j);

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
  private static String replaceDedicatedNodes(final String str) {
    byte[] text = new byte[]{'/', 't', 'e', 'x', 't', '(', ')'};
    byte[] b = str.getBytes();
    
    int bs = -1, st = -1, c = -1;
    boolean f = false;
    TokenList tl = new TokenList();
    byte[] path = null;
    
    for (int i = 0; i < b.length; i++) {
      if (b[i] == '.' && i + 1 < b.length) {
        if (b[i + 1] != '/') {
          final byte[] bn = new byte[b.length + 1 + text.length];
          System.arraycopy(b, 0, bn, 0, i + 1);
          bn[i + 1] = '/';
          System.arraycopy(text, 0, bn, i + 2, text.length);
          System.out.println(new String(bn));
          System.arraycopy(b, i + 1, bn, i + 2 + text.length, b.length - i - 1);
          b = bn;          
        } else {
          i++;
          while (i < b.length && b[i] != ' ') i++;
          final byte[] bn = new byte[b.length + text.length];
          System.arraycopy(b, 0, bn, 0, i);
          System.arraycopy(text, 0, bn, i, text.length);
          System.arraycopy(b, i, bn, i + text.length, b.length - i); 
          b = bn;
        }
      } else if (b[i] == '[') {        
        bs = i;
      } else if (bs > -1) {
        if (b[i] == '(') {
          st = i + 1;
          if (path == null) {
            // copy path before dedicated elements
            path = new byte[i - bs - 1];
            System.arraycopy(b, bs + 1, path, 0, path.length);
          }
        } else if (st > -1) {
          if (b[i] == '|' || f && b[i] == ')') {
            // copy dedicated element names
            byte[] tok = new byte[i - st + text.length];
            System.arraycopy(b, st, tok, 0, i - st);
            System.arraycopy(text, 0, tok, i - st, text.length);
            tl.add(tok);            
            st = i + 1;
            f = true;
            if (b[i] == ')') {
              st = -1;
              f = false;
              c = i + 1;
            }
          }
        } 
        
        if (b[i] == ']' && tl.size() > 0) {
          // rewrite query
          byte[][] n = new byte[tl.size()][];
          int l;
          // calculate size for new query
          int size = 0;
          for (int j = 0; j < n.length; j++) 
            size += path.length + tl.get(j).length 
              + 1 + i - c + ((j + 1 < n.length) ? 4 : 0);
          
          final byte[] bn = new byte[bs + 2 + size + b.length - i];
          // place path before predicate
          System.arraycopy(b, 0, bn, 0, bs + 1);
          int os = bs + 1;
          for (int j = 0; j < n.length; j++) {
            final int tokl = tl.get(j).length;
            l = tokl + 1 + i - c;
            n[j] = new byte[l];
            // copy dedicated element name
            System.arraycopy(tl.get(j), 0, n[j], 0, tokl);
            n[j][tokl] = ' ';
            // copy expression after dedicated path
            System.arraycopy(b, c, n[j], tokl + 1, i - c);
            // place path before dedicated element
            System.arraycopy(path, 0, bn, os, path.length);
            os += path.length;
            // place dedicated element and expression
            System.arraycopy(n[j], 0, bn, os, l);
            os += l;
            if (j + 1 < n.length) {
              // place or expression
              System.arraycopy(new byte[]{' ', 'o', 'r', ' '}, 0, bn, os, 4);
              os += 4;
            }
          }
          // place lasting expressions after the processed predicate
          System.arraycopy(b, i, bn, os, b.length - i);
          // init values for next predicate
          i = bn.length - (b.length - i);
          b = bn;
          tl = new TokenList();
        } 
      } else if (b[i] == '/' && i + 1 < b.length && i + 2 < b.length 
          &&  b[i + 1] == '/' &&  b[i + 2] == '(') {
        i += 3;
        final int j = i - 1;
        int s = i;
        while (i < b.length && b[i] != ')') {
          if (b[i] == '|') {
            final byte[] tok = new byte[i - s];
            System.arraycopy(b, s, tok, 0, tok.length);
            tl.add(tok);
            s = i + 1;
          }
          i++;
        }
        if (tl.size() > 0) {
          final byte[] tok = new byte[i - s];
          System.arraycopy(b, s, tok, 0, tok.length);
          tl.add(tok);          
        }
        
        int size = 0;
        for (int k = 0; k < tl.size(); k++) {
          size += j + s + tl.get(k).length + b.length - 
          i + ((k < tl.size() - 1) ? 3 : 0);
        }
        
        final byte[] bn = new byte[size];
        int off = 0;
        for (int k = 0; k < tl.size(); k++) {
          // copy path
          System.arraycopy(b, 0, bn, off, j);
          off += j;
          // copy element
          System.arraycopy(tl.get(k), 0, bn, off, tl.get(k).length);
          off += tl.get(k).length;
          // copy lasting
          System.arraycopy(b, i + 1, bn, off, b.length - i - 1);
          off += b.length - i - 1;
          if (k < tl.size() - 1) {
            System.arraycopy(new byte[]{' ', 'o', 'r', ' '}, 0, bn, off, 4);
            off += 4;
          }
          System.out.println(new String(bn));
        }
        b = bn;
        i++;
      }
    }
    return new String(b);
  }

  /**
   * Main test method.
   * @param args command line arguments (ignored)
   */
  public static void main(final String[] args) {
    /*System.out.println(replaceDedicatedNodes("//(p|sec)[. ftcontains " +
      "(\"Vincent\" ftand \"van|\" ftand \"Gogh\")]//image[. " +
      "ftcontains (\"sunflowers\")]"));
   */ System.out.println(replaceDedicatedNodes("//sec[. ftcontains " +
        "(\"Vincent\" ftand \"van|\" ftand \"Gogh\")]"));
    //new INEXTest(args.length == 1 ? args[0] : "pages999");
  }
}
