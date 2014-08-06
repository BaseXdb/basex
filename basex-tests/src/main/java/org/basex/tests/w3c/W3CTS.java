package org.basex.tests.w3c;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * XQuery Test Suite wrapper.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class W3CTS extends Main {
  // Try "ulimit -n 65536" if Linux tells you "Too many open files."

  /** Inspect flag. */
  private static final byte[] INSPECT = token("Inspect");
  /** Fragment flag. */
  private static final byte[] FRAGMENT = token("Fragment");
  /** XML flag. */
  private static final byte[] XML = token("XML");
  /** XML flag. */
  private static final byte[] IGNORE = token("Ignore");
  /** Replacement pattern. */
  private static final Pattern SLASH = Pattern.compile("/", Pattern.LITERAL);

  /** Database context. */
  protected final Context context = new Context();
  /** Path to the XQuery Test Suite. */
  protected String path = "";
  /** Data reference. */
  protected Data data;

  /** Log file. */
  private final String pathlog;
  /** Test suite input. */
  private final String input;
  /** Test suite id. */
  private final String testid;

  /** Query path. */
  private String queries;
  /** Expected results. */
  private String expected;
  /** Reported results. */
  private String results;

  /** Maximum length of result output. */
  private int maxout = 500;

  /** Query filter string. */
  private String single;
  /** Flag for printing current time functions into log file. */
  private boolean currTime;
  /** Flag for creating report files. */
  private boolean reporting;
  /** Verbose flag. */
  private boolean verbose;
  /** Minimum time in ms to include query in performance statistics. */
  private int timer = Integer.MAX_VALUE;
  /** Minimum conformance. */
  private boolean minimum;
  /** Print compilation steps. */
  private boolean compile;
  /** test-group to use. */
  private String group;

  /** Cached source files. */
  private final HashMap<String, String> srcs = new HashMap<>();
  /** Cached module files. */
  private final HashMap<String, String> mods = new HashMap<>();
  /** Cached collections. */
  private final HashMap<String, String[]> colls = new HashMap<>();

  /** OK log. */
  private final StringBuilder logOK = new StringBuilder();
  /** OK log. */
  private final StringBuilder logOK2 = new StringBuilder();
  /** Error log. */
  private final StringBuilder logErr = new StringBuilder();
  /** Error log. */
  private final StringBuilder logErr2 = new StringBuilder();
  /** File log. */
  private final StringBuilder logReport = new StringBuilder();

  /** Error counter. */
  private int err;
  /** Error2 counter. */
  private int err2;
  /** OK counter. */
  private int ok;
  /** OK2 counter. */
  private int ok2;

  /**
   * Constructor.
   * @param args command-line arguments
   * @param nm name of test
   */
  protected W3CTS(final String[] args, final String nm) {
    super(args);
    input = nm + "Catalog" + IO.XMLSUFFIX;
    testid = nm.substring(0, 4);
    pathlog = testid.toLowerCase(Locale.ENGLISH) + ".log";
    context.globalopts.set(GlobalOptions.DBPATH, sandbox().path() + "/data");
  }

  /**
   * Runs the test suite.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void run() throws QueryException, IOException {
    try {
      parseArgs();
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }

    queries = path + "Queries/XQuery/";
    expected = path + "ExpectedTestResults/";
    results = path + "ReportingResults/Results/";
    /* Reports. */
    final String report = path + "ReportingResults/";
    /* Test sources. */
    final String sources = path + "TestSources/";

    final Performance perf = new Performance();
    context.options.set(MainOptions.CHOP, false);

    data = MemBuilder.build(new IOFile(path + input));

    final DBNode root = new DBNode(data, 0);
    Util.outln(NL + Util.className(this) + " Test Suite " + text("/*:test-suite/@version", root));

    Util.outln("Caching Sources...");
    for(final Item node : nodes("//*:source", root)) {
      final String val = (path + text("@FileName", node)).replace('\\', '/');
      srcs.put(text("@ID", node), val);
    }

    Util.outln("Caching Modules...");
    for(final Item node : nodes("//*:module", root)) {
      final String val = (path + text("@FileName", node)).replace('\\', '/');
      mods.put(text("@ID", node), val);
    }

    Util.outln("Caching Collections...");
    for(final Item node : nodes("//*:collection", root)) {
      final String cname = text("@ID", node);
      final StringList dl = new StringList();
      for(final Item doc : nodes("*:input-document", node)) {
        dl.add(sources + string(doc.string(null)) + IO.XMLSUFFIX);
      }
      colls.put(cname, dl.toArray());
    }
    init(root);

    if(reporting) {
      Util.outln("Delete old results...");
      new IOFile(results).delete();
    }

    if(verbose) Util.outln();
    final Value nodes = minimum ?
      nodes("//*:test-group[starts-with(@name, 'Minim')]//*:test-case", root) :
      group != null ? nodes("//*:test-group[@name eq '" + group +
          "']//*:test-case", root) : nodes("//*:test-case", root);

    long total = nodes.size();
    Util.out("Parsing " + total + " Queries");
    int t = 0;
    for(final Item node : nodes) {
      if(!parse(node)) break;
      if(!verbose && t++ % 500 == 0) Util.out(".");
    }
    Util.outln();
    total = ok + ok2 + err + err2;

    final String time = perf.getTime();
    Util.outln("Writing log file..." + NL);
    try(final PrintOutput po = new PrintOutput(path + pathlog)) {
      po.println("TEST RESULTS ________________________________________________");
      po.println(NL + "Total #Queries: " + total);
      po.println("Correct / Empty Results: " + ok + " / " + ok2);
      po.print("Conformance (w/Empty Results): ");
      po.println(pc(ok, total) + " / " + pc(ok + ok2, total));
      po.println("Wrong Results / Errors: " + err + " / " + err2 + NL);
      po.println("WRONG _______________________________________________________");
      po.print(NL + logErr);
      po.println("WRONG (ERRORS) ______________________________________________");
      po.print(NL + logErr2);
      po.println("CORRECT? (EMPTY) ____________________________________________");
      po.print(NL + logOK2);
      po.println("CORRECT _____________________________________________________");
      po.print(NL + logOK);
      po.println("_____________________________________________________________");
    }

    if(reporting) {
      try(final PrintOutput po = new PrintOutput(report + Prop.NAME + IO.XMLSUFFIX)) {
        print(po, report + Prop.NAME + "Pre" + IO.XMLSUFFIX);
        po.print(logReport.toString());
        print(po, report + Prop.NAME + "Pos" + IO.XMLSUFFIX);
      }
    }

    Util.outln("Total #Queries: " + total);
    Util.outln("Correct / Empty results: " + ok + " / " + ok2);
    Util.out("Conformance (w/empty results): ");
    Util.outln(pc(ok, total) + " / " + pc(ok + ok2, total));
    Util.outln("Total Time: " + time);

    context.close();
    sandbox().delete();
  }

  /**
   * Calculates the percentage of correct queries.
   * @param v value
   * @param t total value
   * @return percentage
   */
  private static String pc(final int v, final long t) {
    return (t == 0 ? 100 : v * 10000 / t / 100d) + "%";
  }

  /**
   * Parses the specified test case.
   * @param root root node
   * @throws QueryException query exception
   * @throws IOException I/O exception
   * @return true if the query, specified by {@link #single}, was evaluated
   */
  private boolean parse(final Item root) throws QueryException, IOException {
    final String pth = text("@FilePath", root);
    final String outname = text("@name", root);
    if(single != null && !outname.startsWith(single)) return true;

    final Performance perf = new Performance();
    if(verbose) Util.out("- " + outname);

    boolean inspect = false;
    boolean correct = true;

    final Value states = states(root);
    final long ss = states.size();
    for(int s = 0; s < ss; s++) {
      final Item state = states.itemAt(s);
      final String inname = text("*:query/@name", state);
      final IOFile query = new IOFile(queries + pth + inname + IO.XQSUFFIX);
      context.options.set(MainOptions.QUERYPATH, query.path());
      final String in = read(query);
      String er = null;
      Value value = null;

      final Value cont = nodes("*:contextItem", state);
      Value curr = null;
      if(!cont.isEmpty()) {
        final String p = srcs.get(string(cont.itemAt(0).string(null)));
        final Data d = MemBuilder.build(IO.get(p));
        curr = DBNodeSeq.get(d.resources.docs(), d, true, true);
      }

      context.options.set(MainOptions.QUERYINFO, compile);
      final QueryProcessor qp = new QueryProcessor(in, context);
      if(curr != null) qp.context(curr);
      context.options.set(MainOptions.QUERYINFO, false);

      final ArrayOutput ao = new ArrayOutput();
      final TokenBuilder files = new TokenBuilder();

      try {
        files.add(file(nodes("*:input-file", state),
            nodes("*:input-file/@variable", state), qp, s == 0));
        files.add(file(nodes("*:defaultCollection", state), null, qp, s == 0));
        var(nodes("*:input-URI", state), nodes("*:input-URI/@variable", state), qp);
        eval(nodes("*:input-query/@name", state), nodes("*:input-query/@variable", state), pth, qp);

        parse(qp, state);

        for(final Item node : nodes("*:module", root)) {
          final String uri = text("@namespace", node);
          final String file = IO.get(mods.get(string(node.string(null))) + IO.XQSUFFIX).path();
          qp.module(uri, file);
        }

        // evaluate query
        value = qp.value();

        // serialize query
        final SerializerOptions sp = new SerializerOptions();
        sp.set(SerializerOptions.INDENT, NO);
        final Serializer ser = Serializer.get(ao, sp);
        for(final Item it : value) ser.serialize(it);
        ser.close();

      } catch(final Exception ex) {
        if(!(ex instanceof QueryException || ex instanceof IOException)) {
          Util.errln("\n*** " + outname + " ***");
          Util.errln(in + '\n');
          Util.stack(ex);
        }
        er = ex.getMessage();
        if(er.startsWith(STOPPED_AT)) er = er.substring(er.indexOf('\n') + 1);
        if(!er.isEmpty() && er.charAt(0) == '[')
          er = er.replaceAll("\\[(.*?)\\] (.*)", "$1 $2");
        // unexpected error - dump stack trace
      }

      // print compilation steps
      if(compile) {
        Util.errln("---------------------------------------------------------");
        Util.err(qp.info());
        Util.errln(in);
      }

      final Value expOut = nodes("*:output-file/text()", state);
      final TokenList result = new TokenList();
      for(final Item item : expOut) {
        final String resFile = string(item.string(null));
        final IOFile exp = new IOFile(expected + pth + resFile);
        result.add(read(exp).replaceAll("\r\n|\r|\n", Prop.NL));
      }

      final Value cmpFiles = nodes("*:output-file/@compare", state);
      boolean xml = false, frag = false, ignore = false;
      for(final Item item : cmpFiles) {
        final byte[] type = item.string(null);
        xml |= eq(type, XML);
        frag |= eq(type, FRAGMENT);
        ignore |= eq(type, IGNORE);
      }

      String expError = text("*:expected-error/text()", state);

      final StringBuilder log = new StringBuilder(pth + inname + IO.XQSUFFIX);
      if(!files.isEmpty()) log.append(" [").append(files).append(']');
      log.append(NL);

      // Remove comments.
      log.append(norm(in)).append(NL);
      final String logStr = log.toString();
      // skip queries with variable results
      final boolean print = currTime || !logStr.contains("current-");

      boolean correctError = false;
      if(er != null && (expOut.size() == 0 || !expError.isEmpty())) {
        expError = error(pth + outname, expError);
        final String code = er.substring(0, Math.min(8, er.length()));
        for(final String e : SLASH.split(expError)) {
          if(code.equals(e)) {
            correctError = true;
            break;
          }
        }
      }

      if(correctError) {
        if(print) {
          logOK.append(logStr);
          logOK.append("[Right] ");
          logOK.append(norm(er));
          logOK.append(NL);
          logOK.append(NL);
          addLog(pth, outname + ".log", er);
        }
        ++ok;
      } else if(er == null) {
        int r = -1;
        final int rs = result.size();

        while(!ignore && ++r < rs) {
          inspect |= r < cmpFiles.size() && eq(cmpFiles.itemAt(r).string(null), INSPECT);

          final String expect = string(result.get(r));
          final String actual = ao.toString();
          if(expect.equals(actual)) break;

          if(xml || frag) {
            try {
              final ValueIter vb = toIter(expect.replaceAll("^<\\?xml.*?\\?>", "").trim(), frag);
              if(new DeepCompare().equal(value.iter(), vb)) break;
              vb.reset();
              if(new DeepCompare().equal(toIter(actual, frag), vb)) break;
            } catch(final Throwable ex) {
              Util.errln('\n' + outname + ':');
              Util.stack(ex);
            }
          }
        }
        if((rs > 0 || !expError.isEmpty()) && r == rs && !inspect) {
          if(print) {
            if(expOut.size() == 0) result.add(error(pth + outname, expError));
            logErr.append(logStr);
            logErr.append('[' + testid + " ] ");
            logErr.append(norm(string(result.get(0))));
            logErr.append(NL);
            logErr.append("[Wrong] ");
            logErr.append(norm(ao.toString()));
            logErr.append(NL);
            logErr.append(NL);
            addLog(pth, outname + (xml ? IO.XMLSUFFIX : ".txt"), ao.toString());
          }
          correct = false;
          ++err;
        } else {
          if(print) {
            logOK.append(logStr);
            logOK.append("[Right] ");
            logOK.append(norm(ao.toString()));
            logOK.append(NL);
            logOK.append(NL);
            addLog(pth, outname + (xml ? IO.XMLSUFFIX : ".txt"), ao.toString());
          }
          ++ok;
        }
      } else {
        if(expOut.size() == 0 || !expError.isEmpty()) {
          if(print) {
            logOK2.append(logStr);
            logOK2.append('[' + testid + " ] ");
            logOK2.append(norm(expError));
            logOK2.append(NL);
            logOK2.append("[Rght?] ");
            logOK2.append(norm(er));
            logOK2.append(NL);
            logOK2.append(NL);
            addLog(pth, outname + ".log", er);
          }
          ++ok2;
        } else {
          if(print) {
            logErr2.append(logStr);
            logErr2.append('[' + testid + " ] ");
            logErr2.append(norm(string(result.get(0))));
            logErr2.append(NL);
            logErr2.append("[Wrong] ");
            logErr2.append(norm(er));
            logErr2.append(NL);
            logErr2.append(NL);
            addLog(pth, outname + ".log", er);
          }
          correct = false;
          ++err2;
        }
      }
      //if(curr != null) Close.close(curr.data, context);
      qp.close();
    }

    if(reporting) {
      logReport.append("    <test-case name=\"");
      logReport.append(outname);
      logReport.append("\" result='");
      logReport.append(correct ? "pass" : "fail");
      if(inspect) logReport.append("' todo='inspect");
      logReport.append("'/>");
      logReport.append(NL);
    }

    // print verbose/timing information
    final long nano = perf.time();
    final boolean slow = nano / 1000000 > timer;
    if(verbose) {
      if(slow) Util.out(": " + Performance.getTime(nano, 1));
      Util.outln();
    } else if(slow) {
      Util.out(NL + "- " + outname + ": " + Performance.getTime(nano, 1));
    }

    return single == null || !outname.equals(single);
  }

  /**
   * Creates an item iterator for the given XML fragment.
   * @param xml fragment
   * @param frag fragment flag
   * @return iterator
   */
  private ValueIter toIter(final String xml, final boolean frag) {
    try {
      final String str = frag ? "<X>" + xml + "</X>" : xml;
      final Data d = MemBuilder.build(IO.get(str));
      final IntList il = new IntList();
      for(int p = frag ? 2 : 0; p < d.meta.size; p += d.size(p, d.kind(p))) il.add(p);
      return DBNodeSeq.get(il, d, false, false).iter();
    } catch(final IOException ex) {
      return Str.get(Long.toString(System.nanoTime())).iter();
    }
  }

  /**
   * Removes comments from the specified string.
   * @param in input string
   * @return result
   */
  private String norm(final String in) {
    return QueryProcessor.removeComments(in, maxout);
  }

  /**
   * Initializes the input files, specified by the context nodes.
   * @param nodes nodes
   * @param vars variables
   * @param qp query processor
   * @param first call
   * @return string with input files
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  private byte[] file(final Value nodes, final Value vars, final QueryProcessor qp,
      final boolean first) throws QueryException, BaseXException {

    final TokenBuilder tb = new TokenBuilder();
    final long ns = nodes.size();
    for(int n = 0; n < ns; ++n) {
      final byte[] nm = nodes.itemAt(n).string(null);
      String src = srcs.get(string(nm));
      if(!tb.isEmpty()) tb.add(", ");
      tb.add(nm);

      // assign document
      final String dbname = new IOFile(src).dbname();
      // updates: drop updated document or open updated database
      if(updating()) {
        if(first) {
          new DropDB(dbname).execute(context);
        } else {
          src = dbname;
        }
      }

      final Value value = qp.qc.resources.doc(new QueryInput(src), qp.sc.baseIO(), null);
      qp.bind(string(vars.itemAt(n).string(null)), value);
    }
    return tb.finish();
  }

  /**
   * Assigns the nodes to the specified variables.
   * @param nodes nodes
   * @param vars variables
   * @param qp query processor
   * @throws QueryException query exception
   */
  private void var(final Value nodes, final Value vars, final QueryProcessor qp)
      throws QueryException {

    final long ns = nodes.size();
    for(int n = 0; n < ns; ++n) {
      final String nm = string(nodes.itemAt(n).string(null));
      final String src = srcs.get(nm);
      final Item it = src == null ? coll(nm, qp) : Str.get(src);
      qp.bind(string(vars.itemAt(n).string(null)), it);
    }
  }

  /**
   * Assigns a collection.
   * @param name collection name
   * @param qp query processor
   * @return expression
   * @throws QueryException query exception
   */
  private Uri coll(final String name, final QueryProcessor qp) throws QueryException {
    qp.qc.resources.addCollection(name, colls.get(name), qp.sc.baseIO());
    return Uri.uri(name);
  }

  /**
   * Evaluates the the input files and assigns the result to the specified variables.
   * @param nodes nodes
   * @param vars variables
   * @param pth file path
   * @param qp query processor
   * @throws QueryException query exception
   */
  private void eval(final Value nodes, final Value vars, final String pth, final QueryProcessor qp)
      throws QueryException {

    final long ns = nodes.size();
    for(int n = 0; n < ns; ++n) {
      final String file = pth + string(nodes.itemAt(n).string(null)) + IO.XQSUFFIX;
      final String in = read(new IOFile(queries + file));
      final QueryProcessor xq = new QueryProcessor(in, context);
      qp.bind(string(vars.itemAt(n).string(null)), xq.value());
      xq.close();
    }
  }

  /**
   * Adds a log file.
   * @param pth file path
   * @param name file name
   * @param msg message
   * @throws IOException I/O exception
   */
  private void addLog(final String pth, final String name, final String msg) throws IOException {
    if(reporting) {
      final File file = new File(results + pth);
      if(!file.exists()) file.mkdirs();
      try(final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(results + pth + name), UTF8))) {
        bw.write(msg);
      }
    }
  }

  /**
   * Returns an error message.
   * @param nm test name
   * @param error XQTS error
   * @return error message
   */
  private String error(final String nm, final String error) {
    final String error2 = expected + nm + ".log";
    final IO file = new IOFile(error2);
    return file.exists() ? error + '/' + read(file) : error;
  }

  /**
   * Returns the resulting query text (text node or attribute value).
   * @param qu query
   * @param root root node
   * @return attribute value
   * @throws QueryException query exception
   */
  protected String text(final String qu, final Value root) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    final Value nodes = nodes(qu, root);
    final long rs = nodes.size();
    for(int r = 0; r < rs; ++r) {
      if(r != 0) tb.add('/');
      tb.add(nodes.itemAt(r).string(null));
    }
    return tb.toString();
  }

  /**
   * Returns the resulting query nodes.
   * @param qu query
   * @param root root node
   * @return attribute value
   * @throws QueryException query exception
   */
  protected Value nodes(final String qu, final Value root) throws QueryException {
    return new QueryProcessor(qu, context).context(root).value();
  }

  /**
   * Adds the specified file to the writer.
   * @param po writer
   * @param f file path
   * @throws IOException I/O exception
   */
  private static void print(final PrintOutput po, final String f) throws IOException {
    try(final BufferedReader br = new BufferedReader(new FileReader(f))) {
      for(String line; (line = br.readLine()) != null;) po.println(line);
    }
  }

  /**
   * Returns the contents of the specified file.
   * @param fl file to be read
   * @return content
   */
  private static String read(final IO fl) {
    try {
      return fl.string();
    } catch(final IOException ex) {
      Util.errln(ex);
      return "";
    }
  }

  /**
   * Initializes the test.
   * @param root root node reference
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected void init(final DBNode root) throws QueryException { }

  /**
   * Performs test specific parsings.
   * @param qp query processor
   * @param root root nodes reference
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected void parse(final QueryProcessor qp, final Item root) throws QueryException { }

  /**
   * Returns all query states.
   * @param root root node
   * @return states
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected Value states(final Item root) throws QueryException {
    return root;
  }

  /**
   * Updating flag.
   * @return flag
   */
  protected boolean updating() {
    return false;
  }

  @Override
  protected final void parseArgs() throws IOException {
    final MainParser arg = new MainParser(this);
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'r') {
          reporting = true;
          currTime = true;
        } else if(c == 'C') {
          currTime = true;
        } else if(c == 'c') {
          compile = true;
        } else if(c == 'm') {
          minimum = true;
        } else if(c == 'g') {
          group = arg.string();
        } else if(c == 'p') {
          path = arg.string() + '/';
        } else if(c == 't') {
          timer = arg.number();
        } else if(c == 'v') {
          verbose = true;
        } else {
          throw arg.usage();
        }
      } else {
        single = arg.string();
        maxout = Integer.MAX_VALUE;
      }
    }
  }

  /**
   * Returns the sandbox database path.
   * @return database path
   */
  private IOFile sandbox() {
    return new IOFile(Prop.TMP, testid);
  }

  @Override
  public String header() {
    return Util.info(S_CONSOLE, Util.className(this));
  }

  @Override
  public String usage() {
    return " [options] [pat]" + NL +
        " [pat] perform tests starting with a pattern" + NL +
        " -c     print compilation steps" + NL +
        " -C     run tests depending on current time" + NL +
        " -g     <test-group> test group to test" + NL +
        " -h     show this help" + NL +
        " -m     minimum conformance" + NL +
        " -p     change path" + NL +
        " -r     create report" + NL +
        " -t[ms] list slowest queries" + NL +
        " -v     verbose output";
  }
}
