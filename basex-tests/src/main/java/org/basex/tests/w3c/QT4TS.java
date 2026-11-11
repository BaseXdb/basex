package org.basex.tests.w3c;

import static org.basex.tests.w3c.QT3Constants.*;
import static org.basex.util.Prop.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.regex.*;

import javax.xml.namespace.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.validate.*;
import org.basex.query.util.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.tests.bxapi.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.Options.*;

/**
 * Driver for the QT4 test suite, located at {@code https://github.com/qt4cg/qt4tests}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QT4TS extends Main {
  /** XML declaration pattern. */
  private static final Pattern XML_DECL = Pattern.compile("^<\\?xml.*?>\r?\n?");
  /** Newline pattern. */
  private static final Pattern NEWLINES = Pattern.compile("\r\n|\r|\n");
  /** Sandbox for databases. */
  private static final IOFile SANDPIT = new IOFile(TEMPDIR, "qt");

  /** Sandbox of this instance. */
  private final IOFile sandpit;
  /** Output directory of fn:put tests (can be {@code null}). */
  private IOFile results;
  /** Path to the test suite (ignored if {@code null}). */
  private String basePath;
  /** Number of threads for running test sets in parallel. */
  private int threads = 4;
  /** Maximum length of result output. */
  private int maxout = 2000;

  /** Correct results. */
  private final TokenBuilder right = new TokenBuilder();
  /** Wrong results. */
  private final TokenBuilder wrong = new TokenBuilder();
  /** Ignored tests. */
  private final TokenBuilder ignore = new TokenBuilder();
  /** Report builder. */
  private QT3TSReport report;

  /** Number of total queries. */
  private int total;
  /** Number of tested queries. */
  private int tested;
  /** Number of correct queries. */
  private int correct;
  /** Number of ignored queries. */
  private int ignored;

  /** Current test set (can be {@code null}). */
  private TestSet current;

  /** Slow queries (can be {@code null}). */
  private TreeMap<Long, String> slow;
  /** Query filter string. */
  private String single = "";
  /** Verbose flag. */
  private boolean verbose;
  /** Error code flag. */
  private boolean errors = true;
  /** Also log ignored tests. */
  private boolean ignoring;
  /** All flag. */
  private boolean all;

  /** Database context. */
  private final Context ctx = new Context();
  /** Global environments. */
  private final ArrayList<QT3Env> genvs = new ArrayList<>();
  /** Files of all test sets. */
  private final StringList setFiles = new StringList();
  /** Parsed test sets, shared by tests that bind the catalog (can be {@code null}). */
  private ArrayList<Data> setDatas;
  /** Parsed source documents of the current test set, indexed by URI and path. */
  private final Map<String, Data> sourceDatas = new HashMap<>();

  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    try {
      new QT4TS(args).run();
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  QT4TS(final String[] args) {
    super(args);
    sandpit = SANDPIT;
  }

  /**
   * Constructor for running single test cases (e.g., from JUnit).
   * @param path path to the test suite
   * @param sandbox sandbox directory
   * @throws IOException I/O exception
   */
  QT4TS(final String path, final String sandbox) throws IOException {
    super(new String[0]);
    sandpit = new IOFile(sandbox);
    basePath = new File(path).getCanonicalPath();
    prepare();
  }

  /**
   * Constructor for a worker that runs test sets in parallel.
   * @param main main instance
   * @param id id of the worker
   */
  private QT4TS(final QT4TS main, final int id) {
    super(new String[0]);
    sandpit = new IOFile(SANDPIT, "worker" + id);
    basePath = main.basePath;
    maxout = main.maxout;
    single = main.single;
    verbose = main.verbose;
    errors = main.errors;
    ignoring = main.ignoring;
    all = main.all;
    if(main.slow != null) slow = new TreeMap<>();
    genvs.addAll(main.genvs);
    setFiles.add(main.setFiles);
    init();
  }

  /**
   * Initializes the database context.
   */
  private void init() {
    sandpit.md();
    ctx.soptions.set(StaticOptions.DBPATH, new IOFile(sandpit, "data").path());
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.XML);
    sopts.set(SerializerOptions.OMIT_XML_DECLARATION, YesNo.NO);
    ctx.options.set(MainOptions.SERIALIZER, sopts);
    ctx.options.set(MainOptions.DTD, true);
  }

  /**
   * Runs all tests.
   * @throws Exception exception
   */
  private void run() throws Exception {
    parseArgs();

    final Performance perf = new Performance();

    final XdmValue doc = prepare();
    final String version = XQuery.string("*:catalog/@version", doc, ctx);
    Util.println(NL + "QT Test Suite " + version);
    Util.println("Test directory: " + file(false, "."));
    Util.print("Parsing queries");

    // the report is built in the order of the tests
    if(threads == 1 || report != null) {
      for(final String file : setFiles) testSet(file);
    } else {
      parallel();
    }

    final StringBuilder result = new StringBuilder();
    result.append(" Rate    : ").append(pc(correct, tested)).append(NL);
    result.append(" Total   : ").append(total).append(NL);
    result.append(" Tested  : ").append(tested).append(NL);
    result.append(" Wrong   : ").append(tested - correct).append(NL);
    result.append(" Ignored : ").append(ignored).append(NL);

    // save log data
    Util.println(NL + "Writing log file...");
    try(PrintOutput po = new PrintOutput(new IOFile("tests.log"))) {
      po.println("QT4TS RESULTS __________________________" + NL);
      po.println(result.toString());
      po.println("WRONG __________________________________" + NL);
      po.print(wrong.finish());
      if(logCorrect()) {
        po.println("CORRECT ________________________________" + NL);
        po.print(right.finish());
      }
      if(ignoring) {
        po.println("IGNORED ________________________________" + NL);
        po.print(ignore.finish());
      }
    }

    // save report
    if(report != null) {
      ctx.options.get(MainOptions.SERIALIZER).set(SerializerOptions.OMIT_XML_DECLARATION,
          YesNo.YES);
      final String file = "ReportingResults/results_" + NAME + '_' + VERSION + IO.XMLSUFFIX;
      new IOFile(file).write(report.create(ctx));
      Util.println("Creating report '" + file + "'...");
    }

    Util.print(NL + result);
    Util.println(" Time    : " + perf);

    if(slow != null && !slow.isEmpty()) {
      Util.println(NL + "Slow queries:");
      for(final Entry<Long, String> l : slow.entrySet()) {
        Util.println("- " + Performance.formatNano(-l.getKey()) + ": " + l.getValue());
      }
    }

    close();
  }

  /**
   * Checks if correct results are logged.
   * @return result of check
   */
  private boolean logCorrect() {
    return all || !single.isEmpty();
  }

  /**
   * Closes the context and removes the sandbox.
   */
  void close() {
    ctx.close();
    sandpit.delete();
    if(results != null) results.delete();
  }

  /**
   * Initializes the context, resets the output directory of fn:put tests and opens the catalog.
   * @return catalog document
   * @throws BaseXException database exception
   */
  private XdmValue prepare() throws BaseXException {
    init();
    // output directory of fn:put tests (results/ is reserved for test runs)
    results = new IOFile(file(false, "results/sandpit"));
    results.delete();
    results.md();
    final XdmValue doc = new XQuery("doc('" + file(false, CATALOG) + "')", ctx).value();
    for(final XdmItem ienv : new XQuery("*:catalog/*:environment", ctx).context(doc))
      genvs.add(new QT3Env(ctx, ienv));

    for(final XdmItem item : new XQuery("for $f in //*:test-set/@file return string($f)",
        ctx).context(doc)) setFiles.add(item.getString());
    return doc;
  }

  /**
   * Returns the test set files.
   * @return files
   */
  List<String> testSetFiles() {
    return List.of(setFiles.toArray());
  }

  /**
   * Runs the test sets in parallel and merges the results in the order of the catalog.
   * @throws Exception exception
   */
  private void parallel() throws Exception {
    final int sets = setFiles.size();
    final byte[][][] logs = new byte[sets][][];
    final AtomicInteger next = new AtomicInteger();
    final ArrayList<Future<QT4TS>> futures = new ArrayList<>(threads);
    final ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      for(int t = 0; t < threads; t++) {
        final QT4TS worker = new QT4TS(this, t);
        futures.add(pool.submit(() -> {
          for(int s; (s = next.getAndIncrement()) < sets;) {
            worker.testSet(setFiles.get(s));
            logs[s] = new byte[][] { worker.right.toArray(), worker.wrong.toArray(),
              worker.ignore.toArray() };
            worker.right.reset();
            worker.wrong.reset();
            worker.ignore.reset();
          }
          worker.ctx.close();
          return worker;
        }));
      }
      for(final Future<QT4TS> future : futures) {
        final QT4TS worker = future.get();
        total += worker.total;
        tested += worker.tested;
        correct += worker.correct;
        ignored += worker.ignored;
        if(slow != null) slow.putAll(worker.slow);
      }
    } finally {
      pool.shutdownNow();
    }
    for(final byte[][] log : logs) {
      right.add(log[0]);
      wrong.add(log[1]);
      ignore.add(log[2]);
    }
  }

  /**
   * Runs a single test set.
   * @param name name of test set
   * @throws Exception exception
   */
  private void testSet(final String name) throws Exception {
    final TestSet ts = open(name);
    if(!ts.supported) {
      total += ts.cases.size();
      ignored += ts.cases.size();
    } else {
      if(report != null) report.addSet(ts.name);
      for(final String test : ts.cases.keySet()) testCase(ts, test);
    }
  }

  /**
   * A parsed test set.
   */
  final class TestSet {
    /** File name. */
    final String file;
    /** Name of the test set. */
    final String name;
    /** Base URI. */
    final String uri;
    /** Base directory. */
    final String dir;
    /** Supported flag. */
    final boolean supported;
    /** Test cases, indexed by name. */
    final Map<String, XdmItem> cases = new LinkedHashMap<>();
    /** Environments (empty if unsupported). */
    final ArrayList<QT3Env> envs = new ArrayList<>();
    /** Unsupported test cases (empty if the test set is unsupported). */
    final Set<String> unsupported;

    /**
     * Constructor.
     * @param file file name
     * @throws BaseXException database exception
     */
    private TestSet(final String file) throws BaseXException {
      this.file = file;
      final XdmValue doc = new XQuery("doc('" + file(false, file) + "')", ctx).value();
      final XdmValue set = new XQuery("*:test-set", ctx).context(doc).value();
      name = attr(set, "name");
      final IO base = IO.get(doc.getBaseURI());
      uri = base.path();
      dir = base.dir();
      for(final XdmItem test : new XQuery("*:test-case", ctx).context(set))
        cases.put(attribute(test, "name"), test);
      supported = supported(set);
      if(supported) {
        for(final XdmItem ienv : new XQuery("*:environment", ctx).context(set))
          envs.add(new QT3Env(ctx, ienv));
        unsupported = unsupported(set);
      } else {
        unsupported = Set.of();
      }
    }

    /**
     * Checks if a test case is ignored.
     * @param test name of test case
     * @return result of check
     */
    boolean ignored(final String test) {
      return !supported || unsupported.contains(test);
    }
  }

  /**
   * Makes a test set current, unless it already is, and releases the data of the previous one.
   * @param name name of test set
   * @return test set
   * @throws BaseXException database exception
   */
  TestSet open(final String name) throws BaseXException {
    if(current == null || !current.file.equals(name)) {
      current = new TestSet(name);
      setDatas = null;
      sourceDatas.clear();
    }
    return current;
  }

  /**
   * Returns the parsed test sets.
   * @return databases
   */
  private ArrayList<Data> setDatas() {
    if(setDatas == null) {
      setDatas = new ArrayList<>(setFiles.size());
      for(final String name : setFiles) {
        final XdmValue doc = new XQuery("doc('" + file(false, name) + "')", ctx).value();
        setDatas.add(((DBNode) doc.internal()).data());
      }
    }
    return setDatas;
  }

  /**
   * Runs a single test case.
   * @param ts test set
   * @param name name of test case
   * @throws Exception exception
   */
  private void testCase(final TestSet ts, final String name) throws Exception {
    if(total++ % 500 == 0) Util.print(".");

    // skip unsupported queries and queries that do not match filter
    if(ts.ignored(name) || !name.startsWith(single)) {
      if(ignoring) ignore.add(name).add(NL);
      ignored++;
      return;
    }

    tested++;

    final Outcome outcome = execute(name);
    if(slow != null && outcome.nanos > 100000000) slow.put(-outcome.nanos, name);
    if(outcome.ok) {
      if(outcome.text != null) right.add(outcome.text).add(NL);
      correct++;
    } else {
      wrong.add(outcome.text).add(NL);
    }
    if(report != null) report.addTest(name, outcome.ok);
  }

  /**
   * Test case outcome.
   * @param text name, query, result (and expected result if wrong) (can be {@code null})
   * @param ok success flag
   * @param nanos run time
   */
  record Outcome(byte[] text, boolean ok, long nanos) { }

  /**
   * Runs a test case of the current test set.
   * @param name name of test case
   * @return outcome
   * @throws Exception exception
   */
  Outcome execute(final String name) throws Exception {
    try {
      return evaluate(name);
    } catch(final IOException ex) {
      return new Outcome(token(name + NL + "Error : " + ex + NL), false, 0);
    }
  }

  /**
   * Evaluates a test case of the current test set.
   * @param name name of test case
   * @return outcome
   * @throws Exception exception
   */
  private Outcome evaluate(final String name) throws Exception {
    final XdmItem test = current.cases.get(name);

    // expected result
    final XdmItem result = child(test, "result");
    final XdmItem first = result != null ? child(result, null) : null;
    final XdmValue expected = first != null ? first : XdmEmpty.EMPTY;

    // check if environment is defined in test-case
    QT3Env env = null;
    final XdmItem ienv = child(test, "environment");
    if(ienv != null && child(ienv, null) != null) env = new QT3Env(ctx, ienv);

    // parse local environment
    boolean base = true;
    if(env == null && ienv != null) {
      final String e = attr(ienv, "ref");
      if(!e.isEmpty() && !e.equals("empty")) {
        // check if environment is defined in test-set
        env = envs(current.envs, e);
        // check if environment is defined in catalog
        if(env == null) {
          env = envs(genvs, e);
          base = false;
        }
        if(env == null) Util.errln("%: environment '%' not found.", name, e);
      }
    }

    if(verbose) Util.println(name);

    // run all steps: mutable documents are shared, the first error or the last result is tested
    final Performance perf = new Performance();
    final QT3Result returned = new QT3Result();
    returned.env = env;
    final Map<String, XdmValue> mutable = new LinkedHashMap<>();
    final StringJoiner queries = new StringJoiner(NL);
    for(final XdmItem step : children(test, "test")) {
      queries.add(step(test, step, env, base, mutable, returned));
      if(returned.value == null) break;
    }
    final long nanos = perf.nanoRuntime();

    final String exp = test(returned, expected);
    // the log text is only needed for wrong results, or if correct results are logged
    if(exp == null && !logCorrect()) return new Outcome(null, true, nanos);

    final TokenBuilder tmp = new TokenBuilder();
    tmp.add(name).add(NL);
    tmp.add(QueryParser.removeComments(queries.toString(), maxout)).add(NL);

    boolean err = returned.value == null;
    String res;
    try {
      if(returned.error != null) {
        res = returned.error.toString();
      } else if(returned.xqerror != null) {
        res = returned.xqerror.getCode() + ": " + returned.xqerror.getLocalizedMessage();
      } else {
        res = serialize(returned);
      }
    } catch(final XQueryException ex) {
      res = ex.getCode() + ": " + ex.getLocalizedMessage();
      err = true;
    } catch(final Throwable ex) {
      Util.debug(ex);
      res = returned.value.toString();
    }

    tmp.add(err ? "Error : " : "Result: ").add(normSpecial(res)).add(NL);
    if(exp != null) tmp.add("Expect: ").add(normSpecial(exp)).add(NL);
    return new Outcome(tmp.finish(), exp == null, nanos);
  }

  /**
   * Runs a single step of a test case.
   * @param test test case
   * @param step test step
   * @param env environment (can be {@code null})
   * @param base use base URI of test set
   * @param mutable mutable documents, bound by role
   * @param returned query result
   * @return query string
   * @throws Exception exception
   */
  private String step(final XdmItem test, final XdmItem step, final QT3Env env,
      final boolean base, final Map<String, XdmValue> mutable, final QT3Result returned)
      throws Exception {

    // retrieve query to be run
    final String qfile = attr(step, "file");
    String string;
    String qbase = current.uri;
    if(qfile.isEmpty()) {
      // get query string
      string = step.getString();
    } else {
      // get query from file: the file supplies the static base URI
      final IOFile io = new IOFile(current.dir, qfile);
      string = string(io.read());
      qbase = io.path();
    }

    // bind variables
    if(env != null) {
      for(final HashMap<String, String> par : env.params) {
        final String decl = par.get(DECLARED);
        if(decl == null || decl.equals("false")) {
          string = "declare variable $" + par.get(NNAME) + " external;" + string;
        }
      }
      // bind documents
      for(final HashMap<String, String> src : env.sources) {
        final String role = src.get(ROLE);
        if(role != null && role.startsWith("$") && !"true".equals(src.get(DECLARED))) {
          string = "declare variable " + role + " external;" + string;
        }
      }
      // declare default collation (prepended last so the setter precedes the variable declarations)
      if(env.collations != null && "true".equals(env.collations.get("default"))) {
        string = "declare default collation '" + env.collations.get(URI) + "';" + string;
      }
    }

    final XQuery query = new XQuery(string, ctx);
    if(base) query.baseURI(qbase);

    // add modules, collect module locations
    // (test cases modules-30, -31, -32, -33, -40, -41, -42, d1e78807j)
    final Map<String, IO> locations = new HashMap<>();
    for(final XdmItem module : children(test, "module")) {
      final String path = attribute(module, "file");
      if(path == null) continue;
      final String uri = attribute(module, "uri"), location = attribute(module, "location");
      final IOFile file = new IOFile(current.dir, path);
      if(uri != null) query.addModule(uri, file.path());
      if(location != null) locations.put(location, file);
    }

    returned.value = null;
    try {
      environment(query, env, true);
      if(env != null) {
        // subsequent steps: first mutable document is the default context value
        if(!mutable.isEmpty()) query.context(mutable.values().iterator().next());
        // bind documents (mutable documents are parsed once and shared by all steps)
        for(final HashMap<String, String> src : env.sources) {
          final String file = src.get(FILE), role = src.get(ROLE);
          if(file == null) continue;

          final String path = file(base, file);
          locations.put(src.get(URI), new IOFile(current.dir, file));
          // catalog checks: share the test sets instead of parsing them for each test
          if(new IOFile(path).eq(new IOFile(file(false, CATALOG)))) {
            query.qp().qc.resources.addDatabases(setDatas());
          }
          // share parsed documents with other tests of the test set, unless they were updated
          final String key = src.get(URI) + '|' + path;
          final Data data = sourceDatas.get(key);
          if(data != null && data.modifications() == 0) {
            query.qp().qc.resources.addDatabases(List.of(data));
          } else {
            sourceDatas.put(key, query.addDocument(src.get(URI), path));
          }
          if(role == null) continue;

          XdmValue doc = mutable.get(role);
          if(doc == null) {
            doc = query.document(src.get(URI) != null ? src.get(URI) : path);
            if("true".equals(src.get(MUTABLE))) mutable.put(role, doc);
          }
          if(role.equals(".")) query.context(doc);
          else query.variable(role, doc);
        }
        // bind resources (also used as full-text stop word lists and thesauri)
        final HashMap<String, IO> ft = new HashMap<>();
        for(final HashMap<String, String> src : env.resources) {
          final String file = src.get(FILE);
          if(file == null) continue;
          final String uri = src.get(URI);
          final String path = file(base, file);
          final String encoding = src.get(QT3Constants.ENCODING);
          query.addResource(uri, path, encoding);
          final IOFile io = new IOFile(path);
          if(encoding == null) locations.put(uri, io);
          ft.put(uri, io);
        }
        if(!ft.isEmpty()) query.qp().qc.resources.ftmaps(ft, ft);
        // bind collections (from source files, or from a query producing arbitrary items)
        if(!env.collQuery.isEmpty()) {
          final XQuery cq = new XQuery(env.collQuery, ctx).baseURI(current.uri);
          query.addCollection(env.collURI, cq.value());
        } else {
          query.addCollection(env.collURI, env.collSources.toArray());
        }
        if(env.collContext) {
          query.context(query.collection(env.collURI));
        }
        // bind context item
        if(env.context != null) {
          query.context(env.context);
        }
        // set base URI
        if(env.baseURI != null) query.baseURI(env.baseURI);
        // bind decimal formats
        for(final Entry<QName, DecFormatOptions> df : env.decFormats.entrySet()) {
          query.decimalFormat(df.getKey(), df.getValue());
        }
      }

      if(!locations.isEmpty()) {
        final QueryProcessor qp = query.qp();
        qp.uriResolver(new UriResolver() {
          @Override
          public IO resolve(final String path, final String uri, final Uri bas) {
            qp.uriResolver(null);
            final IO io = qp.sc.resolve(path, uri);
            qp.uriResolver(this);
            final IO file = locations.get(io.path());
            return file == null ? io : file;
          }
        });
      }

      // run query
      returned.value = query.value();
      returned.query = query;
    } catch(final XQueryException ex) {
      returned.xqerror = ex;
    } catch(final Throwable ex) {
      // unexpected error (potential bug)
      returned.error = ex;
      Util.errln("Query: " + attribute(test, "name"));
      Util.stack(ex);
    }
    return string;
  }

  /**
   * Assigns the query environment.
   * @param query query
   * @param env environment (can be {@code null})
   * @return query
   */
  private XQuery environment(final XQuery query, final QT3Env env) {
    return environment(query, env, false);
  }

  /**
   * Assigns the query environment.
   * @param query query
   * @param env environment (can be {@code null})
   * @param init initializes the sandbox
   * @return query
   */
  private XQuery environment(final XQuery query, final QT3Env env, final boolean init) {
    if(env != null) {
      // bind namespaces
      for(final HashMap<String, String> ns : env.namespaces) {
        query.namespace(ns.get(PREFIX), ns.get(URI));
      }
      // bind variables
      for(final HashMap<String, String> par : env.params) {
        query.variable(par.get(NNAME), new XQuery(par.get(SELECT), ctx).value());
      }
      // directory with test files
      if(env.sandpit) {
        final Path source = init ? Paths.get(current.dir, "sandpit") : null;
        query.sandpit(source, sandpit);
      }
    }
    return query;
  }

  /**
   * Normalizes special characters in the specified string.
   * @param in input string
   * @return result
   */
  private static String normSpecial(final String in) {
    return XML_DECL.matcher(in).replaceFirst("").replace("\n", "%0A").replace("\r", "%0D").
        replace("\t", "%09");
  }

  /** Flags for dependencies that are not supported. */
  private static final String NOSUPPORT =
    "('schema-location-hint', 'schemaImport', 'schemaValidation', " +
    "'staticTyping', 'typedData', 'fullText-defaults', 'fullText-ignore', 'fullText-units')";

  /** Tests cases to be skipped due to deviations from the spec, or
   * as the testing effort does not justify the outcome. */
  private static final String SKIP = "(" +
    // positional args of fold functions - removed in https://github.com/qt4cg/qtspecs/pull/1867
    "'Keywords-array-fold-left-1', 'Keywords-array-fold-right-1', " +
    "'Keywords-fn-fold-left-1', 'Keywords-fn-fold-right-1', " +
    "'fold-left-010', 'fold-right-008', " +
    // else of braced if - removed in https://github.com/qt4cg/qtspecs/pull/1712
    "'CondExpr-Braced-002', 'CondExpr-Braced-003', 'CondExpr-Braced-004', 'CondExpr-Braced-005', " +
    "'CondExpr-Braced-006', 'CondExpr-Braced-007', 'CondExpr-Braced-008', 'CondExpr-Braced-009', " +
    "'CondExpr-Braced-010', 'CondExpr-Braced-011', 'CondExpr-Braced-012', 'CondExpr-Braced-013', " +
    "'CondExpr-Braced-014', 'CondExpr-Braced-015', 'CondExpr-Braced-016', 'CondExpr-Braced-017', " +
    "'CondExpr-Braced-018', 'CondExpr-Braced-020', 'CondExpr-Braced-021', 'CondExpr-Braced-022', " +
    "'K-CondExpr-Braced-7', 'braced-if-001', 'braced-if-004', 'braced-if-005', " +
    "'braced-if-007', 'braced-if-008', 'PathExpr-10', " +
    // reserved keywords in computed node constructors (supported by the BaseX parser)
    "'K2-ComputeConAttr-65', 'K2-ComputeConAttr-66', 'K2-ComputeConAttr-72', " +
    "'K2-ComputeConElem-20', 'K2-ComputeConElem-21', 'K2-ComputeConElem-26', " +
    "'K2-ComputeConPI-16', 'K2-ComputeConPI-17', " +
    "'K2-ForExprWithout-42a', 'K2-ForExprWithout-43a', 'K2-ForExprWithout-44a', " +
    "'nscons-047', 'nscons-048', " +
    // use-character-maps can be declared in the prolog: BaseX supports a lexical string syntax
    "'Serialization-023', " +
    // too much effort to support in the test suite
    "'fn-available-environment-variables-011', " +
    "'environment-variable-005', 'environment-variable-006', 'environment-variable-007', " +
    // depend on the capabilities of the external XSLT and XSD processors
    "'fn-transform-67', 'fo-test-fn-xsd-validator-002', " +
    // XQuery Update: targets can be empty or contain multiple nodes
    "'id-insert-expr-027', 'id-insert-expr-028', 'id-rename-expr-007', 'id-rename-expr-008', " +
    "'id-rename-expr-015', 'id-replace-expr-013', 'id-replace-expr-019', 'id-replace-expr-026', " +
    "'id-replace-expr-027', 'id-replace-expr-028', 'statictyp-xqupd-003', 'statictyp-xqupd-013', " +
    "'statictyp-xqupd-014', 'statictyp-xqupd-015', 'statictyp-xqupd-016', 'statictyp-xqupd-021', " +
    "'statictyp-xqupd-022', 'statictyp-xqupd-023', 'statictyp-xqupd-024', 'statictyp-xqupd-047', " +
    "'statictyp-xqupd-048', 'statictyp-xqupd-049', 'statictyp-xqupd-053', 'statictyp-xqupd-054', " +
    "'statictyp-xqupd-055', " +
    // XQuery Update: copy-namespaces modes are ignored by updates
    "'id-insert-expr-081-no-inherit', 'id-insert-expr-082-no-inherit', " +
    "'id-insert-expr-083-no-inherit', 'id-insert-expr-084-no-inherit', " +
    "'id-insert-expr-085-no-inherit', 'id-insert-expr-086-no-inherit', " +
    "'id-rename-expr-033-no-inherit', 'id-rename-expr-034-no-inherit', " +
    "'id-replace-expr-045-no-inherit', " +
    "'propagateNamespaces02', 'propagateNamespaces06', " +
    // ID attributes are detected by their name
    "'removeType-valskip-ren-003a', " +
    "'')";

  /** Query that generates a result if the context test set or test case is not supported. */
  private static final String UNSUPPORTED = "(*:dependency[" +
    // skip various features
    "@type = 'feature' and @value = " + NOSUPPORT + " and string(@satisfied) = ('', 'true') or " +
    // skip supported features when test asks for non-support
    "@type = 'feature' and not(@value = " + NOSUPPORT + ") and string(@satisfied) = 'false' or " +
    // skip revalidation modes other than skip, and tests that ask for non-support of skip
    "@type = 'revalidation' and (@value = 'skip') = (@satisfied = 'false') or " +
    // skip fully-normalized Unicode tests that ask for non-support
    "@type = 'unicode-normalization-form' and @value = 'FULLY-NORMALIZED' and " +
    "@satisfied = 'false' or " +
    // skip tests locked to a historical Unicode version (JDK uses a much newer one)
    "@type = 'unicode-version' and @value = ('5.2', '6.0', '6.2') or " +
    // skip xml 1.1 tests and tests for other xsd versions than the one of the schema processor
    "@type = 'xml-version' and @value = ('1.1', '1.0:4-') or " +
    "@type = 'xsd-version' and @value != '" + ValidateXsd.version() + "' or " +
    // skip default-language
    "@type = 'default-language' and @value != 'en' or " +
    // skip non-XQuery tests and XQUF 1.0 tests
    "@type = 'spec' and not(matches(@value, 'XQ(\\d\\d\\+|40)|XQUF(\\d\\d\\+|30)'))" +
    "]," +
    // skip tests where BaseX implementation deviates from the spec
    "@name[. = " + SKIP + "])";

  /**
   * Checks if the current test set is supported.
   * @param set test set
   * @return result of check
   */
  private boolean supported(final XdmValue set) {
    return all || new XQuery(UNSUPPORTED, ctx).context(set).value().size() == 0;
  }

  /**
   * Returns the names of the unsupported test cases of a test set.
   * @param set test set
   * @return names
   */
  private Set<String> unsupported(final XdmValue set) {
    final Set<String> names = new HashSet<>();
    if(all) return names;
    final String query = "*:test-case[" + UNSUPPORTED + "]/string(@name)";
    for(final XdmItem name : new XQuery(query, ctx).context(set)) names.add(name.getString());
    return names;
  }

  /**
   * Returns the value of an attribute.
   * @param node node
   * @param name attribute name
   * @return value, or {@code null} if the attribute does not exist
   */
  private static String attribute(final XdmItem node, final String name) {
    final byte[] value = ((XNode) node.internal()).attribute(new QNm(name));
    return value != null ? string(value) : null;
  }

  /**
   * Returns the value of an attribute of an element.
   * @param elem element
   * @param name attribute name
   * @return value, or an empty string if the attribute does not exist
   */
  private static String attr(final XdmValue elem, final String name) {
    final String value = attribute((XdmItem) elem, name);
    return value != null ? value : "";
  }

  /**
   * Checks if a boolean attribute of an element is true.
   * @param elem element
   * @param name attribute name
   * @return result of check
   */
  private static boolean flag(final XdmValue elem, final String name) {
    final String value = attr(elem, name);
    return value.equals("true") || value.equals("1");
  }

  /**
   * Returns child elements.
   * @param node node
   * @param name local name (can be {@code null})
   * @return child elements
   */
  private static ArrayList<XdmItem> children(final XdmItem node, final String name) {
    final ArrayList<XdmItem> list = new ArrayList<>();
    final byte[] local = name != null ? token(name) : null;
    for(final GNode child : ((XNode) node.internal()).childIter()) {
      if(child.kind() == Kind.ELEMENT && (local == null || eq(local(child.name()), local))) {
        list.add(XdmItem.get(child));
      }
    }
    return list;
  }

  /**
   * Returns the first child element.
   * @param node node
   * @param name local name (can be {@code null})
   * @return child element, or {@code null} if it does not exist
   */
  private static XdmItem child(final XdmItem node, final String name) {
    final ArrayList<XdmItem> list = children(node, name);
    return list.isEmpty() ? null : list.getFirst();
  }

  /**
   * Returns the specified environment.
   * @param envs environments
   * @param ref reference
   * @return environment, or {@code null} if it is not found
   */
  private static QT3Env envs(final ArrayList<QT3Env> envs, final String ref) {
    for(final QT3Env env : envs) {
      if(env.name.equals(ref)) return env;
    }
    return null;
  }

  /**
   * Tests the result of a test case.
   * @param result query result
   * @param expected expected result
   * @return {@code null} if test was successful; otherwise, expected test suite result
   */
  private String test(final QT3Result result, final XdmValue expected) {
    final String type = expected.getName().getLocalPart();
    try {
      final String msg;
      if(type.equals("error")) {
        msg = assertError(result, expected);
      } else if(type.equals("assert-serialization-error")) {
        msg = assertSerializationError(result, expected);
      } else if(type.equals("all-of")) {
        msg = allOf(result, expected);
      } else if(type.equals("not")) {
        msg = not(result, expected);
      } else if(type.equals("any-of")) {
        msg = anyOf(result, expected);
      } else if(result.value != null) {
        msg = switch(type) {
          case "assert" -> assertQuery(result, expected);
          case "assert-count" -> assertCount(result, expected);
          case "assert-deep-eq" -> assertDeepEq(result, expected, true);
          case "assert-empty" -> assertEmpty(result);
          case "assert-eq" -> assertEq(result, expected);
          case "assert-false" -> assertBoolean(result, false);
          case "assert-permutation" -> assertDeepEq(result, expected, false);
          case "assert-xml" -> assertXML(result, expected);
          case "serialization-matches" -> serializationMatches(result, expected);
          case "assert-string-value" -> assertStringValue(result, expected);
          case "assert-true" -> assertBoolean(result, true);
          case "assert-type" -> assertType(result, expected);
          default -> "Test type not supported: " + type;
        };
      } else {
        msg = expected.toString();
      }
      return msg;
    } catch(final Exception ex) {
      return "Flawed test case: " + Util.message(ex);
    }
  }

  /**
   * Tests error.
   * @param result query result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private String assertError(final QT3Result result, final XdmValue expected) {
    final String exp = attr(expected, CODE);
    if(result.xqerror == null) return exp;
    if(!errors || exp.equals("*")) return null;

    final byte[][] eqname = QNm.parseExpanded(token(exp), false);
    final QNm expErr = eqname != null ? new QNm(eqname[0], eqname[1]) :
      new QNm(exp, QueryText.ERROR_URI);
    return expErr.eq(result.xqerror.getException().qname()) ? null : exp;
  }

  /**
   * Tests not.
   * @param result query result
   * @param exp expected result
   * @return expected test suite result, or {@code null}
   */
  private String not(final QT3Result result, final XdmValue exp) {
    for(final XdmItem item : children((XdmItem) exp, null)) {
      if(test(result, item) != null) return null;
    }
    return "not(...)";
  }

  /**
   * Tests all-of.
   * @param result query result
   * @param exp expected result
   * @return expected test suite result, or {@code null}
   */
  private String allOf(final QT3Result result, final XdmValue exp) {
    final TokenBuilder tb = new TokenBuilder();
    for(final XdmItem item : children((XdmItem) exp, null)) {
      final String msg = test(result, item);
      if(msg != null) tb.add(tb.isEmpty() ? "" : ", ").add(msg);
    }
    return tb.isEmpty() ? null : tb.toString();
  }

  /**
   * Tests any-of.
   * @param result query result
   * @param exp expected result
   * @return expected test suite result, or {@code null}
   */
  private String anyOf(final QT3Result result, final XdmValue exp) {
    final TokenBuilder tb = new TokenBuilder();
    for(final XdmItem item : children((XdmItem) exp, null)) {
      final String msg = test(result, item);
      if(msg == null) return null;
      tb.add(tb.isEmpty() ? "" : ", ").add(msg);
    }
    return "any of { " + tb + " }";
  }

  /**
   * Tests assertion.
   * @param result query result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private String assertQuery(final QT3Result result, final XdmValue expected) {
    final String exp = expected.getString();
    try {
      final String query = "declare variable $result external; " + exp;
      // relative URIs are resolved against the test set
      final XQuery xq = new XQuery(query, ctx).baseURI(current.uri);
      return environment(xq, result.env).context(result.value).variable(
          "$result", result.value).value().getBoolean() ? null : exp;
    } catch(final XQueryException ex) {
      // should not occur
      return ex.getException().getMessage();
    }
  }

  /**
   * Tests count.
   * @param result query result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private static String assertCount(final QT3Result result, final XdmValue expected) {
    final long exp = expected.getInteger();
    final int res = result.value.size();
    return exp == res ? null : Util.info("% items (% found)", exp, res);
  }

  /**
   * Tests equality.
   * @param result query result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private String assertEq(final QT3Result result, final XdmValue expected) {
    final String exp = expected.getString();
    try {
      final String query = "declare variable $returned external; " +
          "count($returned) = 1 and $returned eq " + exp;
      return environment(new XQuery(query, ctx), result.env).variable("$returned", result.value).
          value().getBoolean() ? null : exp;
    } catch(final XQueryException err) {
      // numeric overflow: try simple string comparison
      return err.getCode().equals("FOAR0002") && exp.equals(result.value.getString()) ?
        null : err.getException().getMessage();
    }
  }

  /**
   * Tests deep equals or permutation.
   * @param result query result
   * @param expected expected result
   * @param ordered compare in order
   * @return expected test suite result, or {@code null}
   */
  private String assertDeepEq(final QT3Result result, final XdmValue expected,
      final boolean ordered) {
    final XQuery query = new XQuery(expected.getString(), ctx).baseURI(current.uri);
    final XdmValue exp = environment(query, result.env).value();
    return exp.deepEqual(result.value, ordered) ? null : exp.toString();
  }

  /**
   * Tests the serialized result.
   * @param result query result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private String assertXML(final QT3Result result, final XdmValue expected) {
    final String file = attr(expected, "file");
    final boolean normalizeSpace = flag(expected, "normalize-space");
    final boolean ignorePrefixes = flag(expected, "ignore-prefixes");

    String expctd = expected.getString();
    try {
      if(!file.isEmpty()) expctd = string(new IOFile(current.dir, file).read());
      // the XML declaration is not part of the data model
      expctd = XML_DECL.matcher(normNL(expctd)).replaceFirst("");
      if(normalizeSpace) expctd = string(normalize(token(expctd)));

      final String rslt = normNL(serializeXML(result.value));
      if(expctd.equals(rslt)) return null;

      // include check for comments, processing instructions and namespaces
      final String options = " { '" + DeepEqualOptions.NAMESPACE_PREFIXES.name() + "': " +
          !ignorePrefixes + "(), '" + DeepEqualOptions.COMMENTS.name() + "': true(), '" +
          DeepEqualOptions.PROCESSING_INSTRUCTIONS.name() + "': true() }";
      final String query = Function.DEEP_EQUAL.args(" <X>" + expctd + "</X>",
          " <X>" + rslt + "</X>", options);
      return new XQuery(query, ctx).value().getBoolean() ? null : expctd;
    } catch(final IOException ex) {
      return Util.info("% (found: %)", expctd, ex);
    }
  }

  /**
   * Serializes a value with the XML output method, as done by fn:serialize.
   * @param value value
   * @return serialized value
   * @throws IOException I/O exception
   */
  private static String serializeXML(final XdmValue value) throws IOException {
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.XML);
    return value.internal().serializeUnencoded(sopts).toString();
  }

  /**
   * Tests the serialized result.
   * @param result query result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private String serializationMatches(final QT3Result result, final XdmValue expected) {
    try {
      final String flags = attr(expected, "flags");
      final XdmValue returned = XdmValue.get(Str.get(serialize(result)));
      final String query = "declare variable $returned external;"
          + "declare variable $expected external;"
          + "matches($returned, string($expected), '" + flags + "')";

      return environment(new XQuery(query, ctx), result.env).variable("returned", returned).
          variable("expected", expected).value().getBoolean() ? null : expected.getString();
    } catch(final Exception ex) {
      return Util.info("% (found: %)", expected.getString(), ex);
    }
  }

  /**
   * Tests a serialization error.
   * @param result returned result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private String assertSerializationError(final QT3Result result, final XdmValue expected) {
    final String expCode = attr(expected, CODE);
    if(result.xqerror != null && matches(expCode, result.xqerror.getException().qname())) {
      return null;
    }

    try {
      if(result.value != null) serialize(result);
      return expCode;
    } catch(final QueryException ex) {
      return matches(expCode, ex.qname()) ? null : Util.info("% (found: %)", expCode, ex);
    } catch(final IOException ex) {
      return Util.info("% (found: %)", expCode, ex);
    }
  }

  /**
   * Checks if an error code matches the expected code.
   * @param code expected code
   * @param qname error code
   * @return result of check
   */
  private boolean matches(final String code, final QNm qname) {
    return !errors || code.equals("*") || eq(qname.local(), token(code));
  }

  /**
   * Serializes values.
   * @param result query result
   * @return serialized result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private static String serialize(final QT3Result result) throws QueryException, IOException {
    try {
      final ArrayOutput ao = new ArrayOutput();
      try(Serializer ser = result.query.qp().serializer(ao)) {
        result.value.internal().serialize(ser);
      }
      return ao.toString();
    } catch(final QueryIOException ex) {
      throw ex.getCause();
    }
  }

  /**
   * Tests string value.
   * @param result query result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private String assertStringValue(final QT3Result result, final XdmValue expected) {
    String exp = expected.getString();
    // normalize space
    final boolean norm = flag(expected, "normalize-space");
    if(norm) exp = string(normalize(token(exp)));

    final StringJoiner sj = new StringJoiner(" ");
    for(final XdmItem item : result.value) sj.add(item.getString());
    final String res = norm ? string(normalize(token(sj.toString()))) : sj.toString();
    return exp.equals(res) ? null : exp;
  }

  /**
   * Tests boolean.
   * @param result query result
   * @param expected expected
   * @return expected test suite result, or {@code null}
   */
  private static String assertBoolean(final QT3Result result, final boolean expected) {
    final XdmValue returned = result.value;
    return returned.getType().eq(Types.BOOLEAN_O) && returned.getBoolean() == expected ?
        null : Util.info(expected);
  }

  /**
   * Tests empty sequence.
   * @param result query result
   * @return expected test suite result, or {@code null}
   */
  private static String assertEmpty(final QT3Result result) {
    return result.value == XdmEmpty.EMPTY ? null : "";
  }

  /**
   * Tests type.
   * @param result query result
   * @param expected expected result
   * @return expected test suite result, or {@code null}
   */
  private String assertType(final QT3Result result, final XdmValue expected) {
    final String exp = expected.getString();
    try {
      final String query = "declare variable $returned external; $returned instance of " + exp;
      final XQuery xquery = environment(new XQuery(query, ctx), result.env);
      final XdmValue returned = result.value;
      return xquery.variable("returned", returned).value().getBoolean() ? null :
        Util.info("Type '%' (found: '%')", exp, returned.getType().toString());
    } catch(final XQueryException ex) {
      // should not occur
      return ex.getException().getMessage();
    }
  }


  /**
   * Returns the path to a given file.
   * @param base base flag
   * @param file file name
   * @return path to the file
   */
  String file(final boolean base, final String file) {
    final String dir = base ? current.dir : basePath;
    return dir == null ? file : new IOFile(dir, file).path();
  }

  /**
   * Calculates the percentage of correct queries.
   * @param v value
   * @param t total value
   * @return percentage
   */
  private static String pc(final int v, final long t) {
    return (t == 0 ? 100 : v * 10000 / t / 100.0d) + "%";
  }

  /**
   * Normalizes newline characters.
   * @param in input string
   * @return result
   */
  private static String normNL(final String in) {
    return NEWLINES.matcher(in).replaceAll(NL);
  }

  @Override
  protected void parseArgs() throws IOException {
    final MainParser arg = new MainParser(this);
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'v') {
          verbose = true;
        } else if(c == 'a') {
          all = true;
        } else if(c == 'd') {
          debug = true;
        } else if(c == 'i') {
          ignoring = true;
        } else if(c == 'e') {
          errors = false;
        } else if(c == 'r') {
          report = new QT3TSReport();
        } else if(c == 's') {
          slow = new TreeMap<>();
        } else if(c == 't') {
          threads = Math.max(1, arg.number());
        } else if(c == 'p') {
          final File f = new File(arg.string());
          if(!f.isDirectory()) throw arg.usage();
          basePath = f.getCanonicalPath();
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
   * Structure for storing XQuery results.
   */
  static class QT3Result {
    /** Test environment. */
    QT3Env env;
    /** Query instance. */
    XQuery query;
    /** Query result. */
    XdmValue value;
    /** Query exception. */
    XQueryException xqerror;
    /** Query error. */
    Throwable error;
  }

  @Override
  public String header() {
    return Util.info(Text.S_CONSOLE_X, Util.className(this));
  }

  @Override
  public String usage() {
    return " [options] [pat]" + NL +
        " [pat] perform tests starting with a pattern" + NL +
        " -a  save all tests" + NL +
        " -d  debugging mode" + NL +
        " -e  ignore error codes" + NL +
        " -i  also save ignored tests" + NL +
        " -p  path to the test suite" + NL +
        " -r  generate report file" + NL +
        " -s  print slow queries" + NL +
        " -t  number of threads (default: 4)" + NL +
        " -v  verbose output";
  }
}
