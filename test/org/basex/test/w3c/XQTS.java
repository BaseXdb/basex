package org.basex.test.w3c;

import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.basex.BaseX;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Proc;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.data.Result;
import org.basex.io.IO;
import org.basex.io.CachedOutput;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQResult;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.func.FNIndex;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.item.FAttr;
import org.basex.query.xquery.item.FDoc;
import org.basex.query.xquery.item.FElem;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.util.Var;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * XQuery Test Suite Wrapper.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class XQTS {
  // Try "ulimit -n 65536" if Linux tells you "Too many open files."
  
  /** History Path. */
  private static final String PATHHIST = "xqts.hist";
  /** Log File. */
  private static final String PATHLOG = "xqts.log";

  /** XQuery Test Suite Root Directory. */
  private static final String ROOT = "/";
  //private static final String ROOT = "h:/";
  /** Path to the XQuery Test Suite. */
  //private String tests = ROOT + "xqts102/";
  private String tests = ROOT + "home/db/xml/xqts102/";
  /** Query Path. */
  private String queries;
  /** Expected Results. */
  private String expected;
  /** Reported Results. */
  private String results;
  /** Reports. */
  private String report;
  /** Test Sources. */
  private String sources;
  /** Inspect flag. */
  private static final byte[] INSPECT = token("Inspect");

  /** Maximum length of result output. */
  private static int maxout = 500;

  /** Delimiter. */
  private static final String DELIM = "#~%~#";
  /** Replacement pattern. */
  private static final Pattern CHOP = Pattern.compile(DELIM, Pattern.LITERAL);
  /** Replacement pattern. */
  private static final Pattern SLASH = Pattern.compile("/", Pattern.LITERAL);

  /** Query filter string. */
  private String single;
  /** Flag for printing current time functions into log file. */
  private boolean currTime;
  /** Flag for creating report files. */
  private boolean reporting;
  /** Verbose flag. */
  private boolean verbose;

  /** Cached source files. */
  private final HashMap<String, String> srcFiles =
    new HashMap<String, String>();
  /** Cached module files. */
  private final HashMap<String, String> modules = new HashMap<String, String>();
  /** Cached collections. */
  private final HashMap<String, FDoc> colls = new HashMap<String, FDoc>();

  /** OK log. */
  private final StringBuilder logOK = new StringBuilder();
  /** OK log. */
  private final StringBuilder logOK2 = new StringBuilder();
  /** Error log. */
  private final StringBuilder logErr = new StringBuilder();
  /** Error log. */
  private final StringBuilder logErr2 = new StringBuilder();
  /** File log. */
  private final StringBuilder logFile = new StringBuilder();

  /** Error counter. */
  private int err;
  /** Error2 counter. */
  private int err2;
  /** OK counter. */
  private int ok;
  /** OK2 counter. */
  private int ok2;

  /** Data reference. */
  private Data data;

  /**
   * Main method of the test class.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQTS(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private XQTS(final String[] args) throws Exception {
    // modifying internal query arguments...
    for(final String arg : args) {
      if(arg.equals("-r")) {
        reporting = true;
        currTime = true;
      } else if(arg.equals("-t")) {
        currTime = true;
      } else if(arg.equals("-v")) {
        verbose = true;
      } else if(arg.startsWith("-s")) {
        tests = ROOT + arg.substring(2) + "/";
      } else if(!arg.startsWith("-")) {
        single = arg;
        maxout *= 10;
      } else {
        BaseX.outln("\nBaseX vs. XQuery Test Suite\n" +
            " [pat] perform only tests with the specified pattern\n" +
            " -s[tests] performs the specified tests\n" + 
            " -h show this help\n" +
            " -r create report\n" +
            " -v verbose output");
        return;
      }
    }
    
    queries = tests + "Queries/XQuery/";
    expected = tests + "ExpectedTestResults/";
    results = tests + "ReportingResults/Results/";
    report = tests + "ReportingResults/";
    sources = tests + "TestSources/";

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final String dat = sdf.format(Calendar.getInstance().getTime());
    Prop.read();
    Prop.xqformat = false;

    final Performance perf = new Performance();

    final Context context = new Context();
    if(!Proc.execute(context, Commands.CHECK, tests + "XQTSCatalog.xml")) {
      BaseX.outln("XQTSCatalog.xml not found.");
      return;
    }
    data = context.data();

    final Nodes root = new Nodes(0, data);
    BaseX.outln("\nBaseX vs. XQuery Test Suite " +
        text("/test-suite/@version", root));

    BaseX.outln("\nCaching Sources...");
    for(final int s : nodes("//source", root).pre) {
      final Nodes srcRoot = new Nodes(s, data);
      final String source = (tests +
          text("@FileName", srcRoot)).replace('\\', '/');
      srcFiles.put(text("@ID", srcRoot), source);
    }

    BaseX.outln("Caching Modules...");
    for(final int s : nodes("//module", root).pre) {
      final Nodes srcRoot = new Nodes(s, data);
      final String module = (tests +
          text("@FileName", srcRoot)).replace('\\', '/');
      modules.put(text("@ID", srcRoot), module);
    }

    BaseX.outln("Caching Collections...");
    for(final int c : nodes("//collection", root).pre) {
      final Nodes nodes = new Nodes(c, data);
      final String cname = text("@ID", nodes);
      final FDoc doc = coll(nodes("input-document", nodes), cname);
      colls.put(cname, doc);
    }

    if(reporting) {
      BaseX.outln("Delete old results...");
      delete(new File[] { new File(results) });
    }

    BaseX.out("Parsing Queries");
    final Nodes nodes = nodes("//test-case | //ts:test-case", root);
    for(int t = 0; t < nodes.size; t++) {
      if(!parse(new Nodes(nodes.pre[t], data))) break;
      if(t % 1000 == 0) BaseX.out(".");
    }
    BaseX.outln("");

    final String time = perf.getTimer();

    final int total = ok + ok2 + err + err2;

    BaseX.outln("Writing log file...\n");
    BufferedWriter bw = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(PATHLOG), UTF8));
    bw.write("TEST RESULTS ==================================================");
    bw.write(Prop.NL + Prop.NL + "Total #Queries: " + total + Prop.NL);
    bw.write("Correct / Empty Results: " + ok + " / " + ok2 + Prop.NL);
    bw.write("Conformance (w/Empty Results): ");
    bw.write(pc(ok, total) + " / " + pc(ok + ok2, total) + Prop.NL);
    bw.write("Wrong Results / Errors: " + err + " / " + err2 + Prop.NL);
    bw.write("Total Time: " + time + Prop.NL + Prop.NL);
    bw.write("WRONG =========================================================");
    bw.write(Prop.NL + Prop.NL + logErr + Prop.NL);
    bw.write("WRONG (ERRORS) ================================================");
    bw.write(Prop.NL + Prop.NL + logErr2 + Prop.NL);
    bw.write("CORRECT? (EMPTY) ==============================================");
    bw.write(Prop.NL + Prop.NL + logOK2 + Prop.NL);
    bw.write("CORRECT =======================================================");
    bw.write(Prop.NL + Prop.NL + logOK + Prop.NL);
    bw.write("===============================================================");
    bw.close();

    bw = new BufferedWriter(new FileWriter(PATHHIST, true));
    bw.write(dat + "\t" + ok + "\t" + ok2 + "\t" + err + "\t" + err2 + Prop.NL);
    bw.close();

    if(reporting) {
      bw = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(report + "BaseX.xml"), UTF8));
      write(bw, report + "BaseXPre.xml");
      bw.write(logFile.toString());
      write(bw, report + "BaseXPos.xml");
      bw.close();
    }

    BaseX.outln("Total #Queries: " + total);
    BaseX.outln("Correct / Empty results: " + ok + " / " + ok2);
    BaseX.out("Conformance (w/empty results): ");
    BaseX.outln(pc(ok, total) + " / " + pc(ok + ok2, total));
    BaseX.outln("Total Time: " + time);
  }

  /**
   * Calculate percentage of correct queries.
   * @param v value
   * @param t total value
   * @return percentage
   */
  private String pc(final int v, final int t) {
    return (t == 0 ? 100 : (v * 10000 / t) / 100.0) + "%";
  }

  /**
   * Parses the specified test case.
   * @param root root node
   * @throws Exception exception
   * @return true if the query, specified by {@link #single}, was evaluated.
   */
  private boolean parse(final Nodes root) throws Exception {
    final String path = text("@FilePath", root);
    final String outname = text("@name", root);
    String inname = text("query/@name", root);
    if(inname == null) inname = outname;
    if(verbose) BaseX.outln(inname);

    if(single != null && !outname.startsWith(single)) return true;

    final IO file = new IO(queries + path + inname + ".xq");
    final String input = read(file);

    String output = "";
    String error = null;
    Result res = null;

    final TokenBuilder files = new TokenBuilder();
    try {
      final Context context = new Context();
      
      final XQueryProcessor xq = new XQueryProcessor(input, file);
      final Nodes cont = nodes("contextItem", root);
      if(cont.size != 0) Proc.execute(context, Commands.CHECK,
          sources + string(data.atom(cont.pre[0])) + IO.XMLSUFFIX);

      final XQContext ctx = xq.ctx;
      files.add(file(nodes("input-file", root),
          nodes("input-file/@variable", root), ctx));
      files.add(file(nodes("input-URI", root),
          nodes("input-URI/@variable", root), ctx));
      files.add(file(nodes("defaultCollection", root), null, ctx));

      var(nodes("input-query/@name", root),
          nodes("input-query/@variable", root), path, ctx);

      for(final int p : nodes("module", root).pre) {
        final String ns = text("@namespace", new Nodes(p, data));
        final String f = modules.get(string(data.atom(p))) + ".xq";
        xq.module(ns, f);
      }

      final CachedOutput out = new CachedOutput();
      res = xq.query(context.current());
      res.serialize(new XMLSerializer(out));
      output = norm(out.finish());
    } catch(final QueryException ex) {
      error = ex.getMessage();
      if(error.startsWith("Stopped at")) {
        error = error.substring(error.indexOf('\n') + 1);
      }

      if(error.startsWith("[")) {
        final int i = error.indexOf("]");
        error = error.substring(1).substring(0, i - 1) +
          error.substring(i + 1);
      }
    } catch(final Exception ex) {
      final ByteArrayOutputStream bw = new ByteArrayOutputStream();
      ex.printStackTrace(new PrintStream(bw));
      error = bw.toString();
    } catch(final Error ex) {
      final ByteArrayOutputStream bw = new ByteArrayOutputStream();
      ex.printStackTrace(new PrintStream(bw));
      error = bw.toString();
    }

    final Nodes outFiles = nodes("output-file/text()", root);
    final Nodes cmpFiles = nodes("output-file/@compare", root);
    final StringBuilder tb = new StringBuilder();
    for(int o = 0; o < outFiles.size; o++) {
      if(o != 0) tb.append(DELIM);
      final String resFile = string(data.atom(outFiles.pre[o]));
      tb.append(read(new IO(expected + path + resFile)));
    }
    String result = tb.toString();
    String expError = text("expected-error/text()", root);

    final StringBuilder log = new StringBuilder(path + inname + ".xq");
    if(files.size != 0) {
      log.append(" [");
      log.append(files);
      log.append("]");
    }
    log.append(Prop.NL);

    /** Remove comments. */
    log.append(compact(input));
    log.append(Prop.NL);
    final String logStr = log.toString();
    final boolean print = currTime || !logStr.contains("current-") &&
        !logStr.contains("implicit-timezone");

    if(reporting) {
      logFile.append("    <test-case name=\"");
      logFile.append(outname);
      logFile.append("\" result='");
    }

    boolean rightCode = false;
    if(error != null && (outFiles.size == 0 || expError.length() != 0)) {
      expError = error(path + outname, expError);
      final String code = error.substring(0, Math.min(8, error.length()));
      for(final String er : SLASH.split(expError)) {
        if(code.equals(er)) {
          rightCode = true;
          break;
        }
      }
    }

    if(rightCode) {
      if(print) {
        logOK.append(logStr);
        logOK.append("[Right] ");
        logOK.append(error);
        logOK.append(Prop.NL);
        logOK.append(Prop.NL);
        addLog(path, outname + ".log", error);
      }
      if(reporting) logFile.append("pass");
      ok++;
    } else if(error == null) {
      boolean inspect = false;
      final String[] split = CHOP.split(result, 0);
      int s = -1;
      while(++s < split.length) {
        inspect |= s < cmpFiles.pre.length && eq(data.atom(cmpFiles.pre[s]),
            INSPECT);
        if(split[s].equals(output)) break;
      }

      if(s == split.length && !inspect) {
        if(print) {
          if(outFiles.size == 0) result = error(path + outname, expError);
          logErr.append(logStr);
          logErr.append("[XQTS ] ");
          logErr.append(chop(result));
          logErr.append(Prop.NL);
          logErr.append("[Wrong] ");
          logErr.append(chop(output));
          logErr.append(Prop.NL);
          logErr.append(Prop.NL);
          final Item it = ((XQResult) res).item();
          final boolean nodes = it instanceof Node && it.type != Type.TXT;
          addLog(path, outname + (nodes ? ".xml" : ".txt"), output);
        }
        if(reporting) logFile.append("fail");
        err++;
      } else {
        if(print) {
          logOK.append(logStr);
          logOK.append("[Right] ");
          logOK.append(chop(output));
          logOK.append(Prop.NL);
          logOK.append(Prop.NL);
          final Item it = ((XQResult) res).item();
          final boolean nodes = it instanceof Node && it.type != Type.TXT;
          addLog(path, outname + (nodes ? ".xml" : ".txt"), output);
        }
        if(reporting) {
          logFile.append("pass");
          if(inspect) logFile.append("' todo='inspect");
        }
        ok++;
      }
    } else {
      if(outFiles.size == 0 || expError.length() != 0) {
        if(print) {
          logOK2.append(logStr);
          logOK2.append("[XQTS ] ");
          logOK2.append(expError);
          logOK2.append(Prop.NL);
          logOK2.append("[Rght?] ");
          logOK2.append(error);
          logOK2.append(Prop.NL);
          logOK2.append(Prop.NL);
          addLog(path, outname + ".log", error);
        }
        if(reporting) logFile.append("pass");
        ok2++;
      } else {
        if(print) {
          logErr2.append(logStr);
          logErr2.append("[XQTS ] ");
          logErr2.append(chop(result));
          logErr2.append(Prop.NL);
          logErr2.append("[Wrong] ");
          logErr2.append(error);
          logErr2.append(Prop.NL);
          logErr2.append(Prop.NL);
          addLog(path, outname + ".log", error);
        }
        if(reporting) logFile.append("fail");
        err2++;
      }
    }
    if(reporting) {
      logFile.append("'/>");
      logFile.append(Prop.NL);
    }

    return single == null || !outname.equals(single);
  }

  /**
   * Removes comments and double string.
   * @param in input string
   * @return result
   */
  private String compact(final String in) {
    final StringBuilder sb = new StringBuilder();
    int m = 0;
    boolean s = false;
    for(int c = 0, cl = in.length(); c < cl; c++) {
      char ch = in.charAt(c);
      if(ch == '(' && c + 1 < cl && in.charAt(c + 1) == ':') {
        if(m == 0 && !s) {
          sb.append(' ');
          s = true;
        }
        m++;
        c++;
      } else if(m != 0 && ch == ':' && c + 1 < cl && in.charAt(c + 1) == ')') {
        m--;
        c++;
      } else if(m == 0) {
        if(!s || ch > ' ') sb.append(ch);
        s = ch <= ' ';
      }
    }
    return sb.toString().trim();
  }
  
  /**
   * Initializes the input files, specified by the context nodes.
   * @param nod variables
   * @param var documents
   * @param ctx xquery context
   * @return string with input files
   * @throws QueryException query exception
   */
  private byte[] file(final Nodes nod, final Nodes var,
      final XQContext ctx) throws QueryException {

    final TokenBuilder tb = new TokenBuilder();
    for(int c = 0; c < nod.size; c++) {
      final byte[] name = data.atom(nod.pre[c]);
      final String src = srcFiles.get(string(name));
      if(tb.size != 0) tb.add(", ");
      tb.add(name);

      if(src == null) {
        // assign collection
        ctx.addColl(colls.get(string(name)));

        if(var != null) {
          final Var v = new Var(new QNm(data.atom(var.pre[c])));
          ctx.vars.addGlobal(v.item(Uri.uri(name)));
        }
      } else {
        // assign document
        final Fun fun = FNIndex.get().get(token("doc"), Uri.FN,
            new Expr[] { Str.get(token(src)) });
        final Var v = new Var(new QNm(data.atom(var.pre[c])));
        ctx.vars.addGlobal(v.expr(fun));
      }
    }
    return tb.finish();
  }

  /**
   * Evaluates the the input files and assigns the result to the specified
   * variables.
   * @param nod variables
   * @param var documents
   * @param path file path
   * @param ctx xquery context
   * @throws Exception exception
   */
  private void var(final Nodes nod, final Nodes var, final String path,
      final XQContext ctx) throws Exception {

    for(int c = 0; c < nod.size; c++) {
      final String file = path + string(data.atom(nod.pre[c])) + ".xq";
      final String input = read(new IO(queries + file));
      final XQueryProcessor qu = new XQueryProcessor(input);
      final XQResult result = (XQResult) qu.query(null);
      final Var v = new Var(new QNm(data.atom(var.pre[c])));
      ctx.vars.addGlobal(v.item(result.item()));
    }
  }

  /**
   * Creates a collection.
   * @param doc collection documents
   * @param cname name of collection
   * @return collection
   * @throws Exception exception
   */
  private FDoc coll(final Nodes doc, final String cname)
      throws Exception {
    final NodIter docs = new NodIter();
    for(int c = 0; c < doc.size; c++) {
      final NodIter atts = new NodIter();
      final String file = sources + string(data.atom(doc.pre[c])) + ".xml";
      atts.add(new FAttr(new QNm(token("href")), token(file), null));
      docs.add(new FElem(new QNm(token("doc")),
          new NodIter(), atts, EMPTY, new FAttr[] {}, null));
    }

    final NodIter coll = new NodIter();
    coll.add(new FElem(new QNm(token("collection")),
        docs, new NodIter(), EMPTY, new FAttr[] {}, null));
    return new FDoc(coll, token(cname));
  }

  /**
   * Adds a log file.
   * @param path file path
   * @param name file name
   * @param msg message
   * @throws Exception exception
   */
  private void addLog(final String path, final String name, final String msg)
      throws Exception {

    if(reporting) {
      final File file = new File(results + path);
      if(!file.exists()) file.mkdirs();
      final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(results + path + name), UTF8));
      bw.write(msg);
      bw.close();
    }
  }

  /**
   * Returns an XQTS error message.
   * @param name test name
   * @param error XQTS error
   * @return error message
   * @throws Exception exception
   */
  private String error(final String name, final String error) throws Exception {
    final String error2 = expected + name + ".log";
    final IO file  = new IO(error2);
    return file.exists() ? error + "/" + read(file) : error;
  }

  /**
   * Returns the resulting query text (text node or attribute value).
   * Adds a "ts:" prefix due to the missing XPath namespaces support.
   * @param qu query
   * @param root root node
   * @return attribute value
   * @throws Exception exception
   */
  private String text(final String qu, final Nodes root) throws Exception {
    final Nodes n = nodes(qu, root);
    final TokenBuilder sb = new TokenBuilder();
    for(int i = 0; i < n.size; i++) {
      if(i != 0) sb.add('/');
      sb.add(data.atom(n.pre[i]));
    }
    return sb.toString();
  }

  /**
   * Returns the resulting query nodes.
   * Adds a "ts:" prefix due to the missing XPath namespaces support.
   * @param qu query
   * @param root root node
   * @return attribute value
   * @throws Exception exception
   */
  private Nodes nodes(final String qu, final Nodes root) throws Exception {
    final Nodes n = new XPathProcessor(qu).queryNodes(root);
    return n.size != 0 || qu.startsWith("@") ? n :
      new XPathProcessor("ts:" + qu).queryNodes(root);
  }

  /**
   * Recursively deletes a directory.
   * @param path deletion path
   */
  private void delete(final File[] path) {
    for(final File f : path) {
      if(f.isDirectory()) delete(f.listFiles());
      f.delete();
    }
  }

  /**
   * Adds the specified file to the writer.
   * @param bw writer
   * @param f file path
   * @throws Exception exception
   */
  private void write(final BufferedWriter bw, final String f) throws Exception {
    final BufferedReader br = new BufferedReader(new
        InputStreamReader(new FileInputStream(f), UTF8));
    String line;
    while((line = br.readLine()) != null) {
      bw.write(line);
      bw.write(Prop.NL);
    }
    br.close();
  }

  /**
   * Returns the contents of the specified file.
   * @param f file to be read
   * @return content
   * @throws IOException I/O exception
   */
  private String read(final IO f) throws IOException {
    final StringBuilder sb = new StringBuilder();
    final BufferedReader br = new BufferedReader(new
        InputStreamReader(new FileInputStream(f.path()), UTF8));
    String l;
    while((l = br.readLine()) != null) {
      l = l.trim();
      if(l.length() == 0) continue;
      sb.append(l.indexOf(" />") != -1 ? l.replaceAll(" />", "/>") : l);
      sb.append(' ');
    }
    br.close();
    return sb.toString().trim();
  }

  /**
   * Chops the specified string to a maximum of 100 characters.
   * @param string string
   * @return chopped string
   */
  private String chop(final String string) {
    if(string == null) return "";
    final String str = CHOP.matcher(string).replaceAll(" / ");
    final int sl = str.length();
    return sl < maxout ? str :
      new StringBuilder(str.substring(0, maxout)).append("...").toString();
  }

  /**
   * Normalizes the specified string.
   * @param string string
   * @return normalized string
   */
  private String norm(final byte[] string) {
    final String str = string(string);
    final StringBuilder sb = new StringBuilder();
    boolean nl = true;
    for(int l = 0; l < str.length(); l++) {
      final char c = str.charAt(l);
      if(nl) {
        nl = c >= 0 && c <= ' ';
      } else {
        nl = c == '\r' || c == '\n';
        if(nl) {
          // delete trailing whitespaces
          while(sb.charAt(sb.length() - 1) <= ' ')
            sb.deleteCharAt(sb.length() - 1);
          sb.append(' ');
        }
      }
      if(!nl) sb.append(c);
    }
    return sb.toString().trim();
  }
}
