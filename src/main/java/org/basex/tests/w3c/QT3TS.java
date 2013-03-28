package org.basex.tests.w3c;

import static org.basex.core.Prop.*;
import static org.basex.tests.w3c.QT3Constants.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.Compare.Flag;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.tests.bxapi.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.*;

/**
 * Driver for the XQuery/XPath/XSLT 3.* Test Suite, located at
 * {@code http://dev.w3.org/2011/QT3-test-suite/}. The driver needs to be
 * executed from the test suite directory.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QT3TS {
  /** Test suite id. */
  private final String testid = "qt3ts";
  /** Path to the test suite (ignored if {@code null}). */
  private String basePath;

  /** Maximum length of result output. */
  private int maxout = 2000;

  /** Correct results. */
  private final TokenBuilder right = new TokenBuilder();
  /** Wrong results. */
  private final TokenBuilder wrong = new TokenBuilder();
  /** Ignored tests. */
  private final TokenBuilder ignore = new TokenBuilder();

  /** Number of total queries. */
  private int total;
  /** Number of tested queries. */
  private int tested;
  /** Number of correct queries. */
  private int correct;
  /** Number of ignored queries. */
  private int ignored;

  /** Current base uri. */
  private String base;

  /** Slow queries flag. */
  private TreeMap<Long, String> slow;
  /** Query filter string. */
  private String single = "";
  /** Verbose flag. */
  private boolean verbose;
  /** Error code flag. */
  private boolean errors = true;
  /** Also print ignored files. */
  private boolean ignoring;
  /** All flag. */
  private boolean all;

  /** Database context. */
  protected final Context ctx = new Context();
  /** Global environments. */
  private final ArrayList<QT3Env> genvs = new ArrayList<QT3Env>();

  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    try {
      new QT3TS().run(args);
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Runs all tests.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private void run(final String[] args) throws Exception {
    ctx.mprop.set(MainProp.DBPATH, sandbox().path() + "/data");
    parseArguments(args);

    final Performance perf = new Performance();
    ctx.prop.set(Prop.CHOP, false);
    ctx.prop.set(Prop.INTPARSE, false);

    final XQuery qdoc = new XQuery("doc('" + file(null, CATALOG) + "')", ctx);
    final XdmValue doc = qdoc.value();
    final String version = asString("*:catalog/@version", doc);
    Util.outln(NL + "QT3 Test Suite " + version);
    Util.out("Parsing queries");

    final XQuery qenv = new XQuery("*:catalog/*:environment", ctx).context(doc);
    for(final XdmItem ienv : qenv) genvs.add(new QT3Env(ctx, ienv));
    qenv.close();

    final XQuery qset = new XQuery(
        "for $f in //*:test-set/@file return string($f)", ctx).context(doc);
    for(final XdmItem it : qset) testSet(it.getString());
    qset.close();
    qdoc.close();

    final StringBuilder result = new StringBuilder();
    result.append(" Rate    : ").append(pc(correct, tested)).append(NL);
    result.append(" Correct : ").append(correct).append(NL);
    result.append(" Tested  : ").append(tested).append(NL);
    result.append(" Total   : ").append(total).append(NL);
    result.append(" Ignored : ").append(ignored).append(NL);

    final String path = new File(testid + ".log").getCanonicalPath();
    Util.outln(NL + "Writing log file '" + path + "'..." + NL);
    final PrintOutput po = new PrintOutput(path);
    po.println("QT3TS RESULTS __________________________" + NL);
    po.println(result.toString());
    po.println("WRONG __________________________________" + NL);
    po.print(wrong.finish());
    if(all || !single.isEmpty()) {
      po.println("CORRECT ________________________________" + NL);
      po.print(right.finish());
    }
    if(ignoring) {
      po.println("IGNORED ________________________________" + NL);
      po.print(ignore.finish());
    }
    po.close();

    Util.out(result);
    Util.outln(" Time    : " + perf);

    if(slow != null && !slow.isEmpty()) {
      Util.outln(NL + "Slow queries:");
      for(final Map.Entry<Long, String> l : slow.entrySet()) {
        Util.outln("- " + -(l.getKey() / 1000000) + " ms: " + l.getValue());
      }
    }

    ctx.close();
    sandbox().delete();
  }

  /**
   * Runs a single test set.
   * @param name name of test set
   * @throws Exception exception
   */
  private void testSet(final String name) throws Exception {
    final XQuery qdoc = new XQuery("doc(' " + file(null, name) + "')", ctx);
    final XdmValue doc = qdoc.value();
    final XQuery qset = new XQuery("*:test-set", ctx).context(doc);
    final XdmValue set = qset.value();
    base = IO.get(doc.getBaseURI()).dirPath();
    qdoc.close();

    if(supported(set)) {
      // parse environment of test-set
      final XQuery qenv = new XQuery("*:environment", ctx).context(set);
      final ArrayList<QT3Env> envs = new ArrayList<QT3Env>();
      for(final XdmItem ienv : qenv) envs.add(new QT3Env(ctx, ienv));
      qenv.close();

      // run all test cases
      final XQuery qts = new XQuery("*:test-case", ctx).context(set);
      for(final XdmItem its : qts) {
        try {
          testCase(its, envs);
        } catch(final IOException ex) {
          Util.debug(ex);
        }
      }
      qts.close();
    }
    qset.close();
  }

  /**
   * Runs a single test case.
   * @param test node
   * @param envs environments
   * @throws Exception exception
   */
  private void testCase(final XdmItem test, final ArrayList<QT3Env> envs)
      throws Exception {

    if(total++ % 500 == 0) Util.out(".");

    if(!supported(test)) {
      if(ignoring) ignore.add(asString("@name", test)).add(NL);
      ignored++;
      return;
    }

    // skip queries that do not match filter
    final String name = asString("@name", test);
    if(!name.startsWith(single)) {
      if(ignoring) ignore.add(name).add(NL);
      ignored++;
      return;
    }

    tested++;

    // expected result
    final XQuery qexp = new XQuery("*:result/*[1]", ctx).context(test);
    final XdmValue expected = qexp.value();

    // use XQuery 1.0 if XQ10 or XP20 is specified
    final XQuery q = new XQuery("*:dependency[@type='spec']" +
        "[matches(@value,'(XQ10|XP20)([^+]|$)')]", ctx);
    if(q.context(test).next() != null) ctx.prop.set(Prop.XQUERY3, false);
    q.close();

    // check if environment is defined in test-case
    QT3Env e = null;
    final XQuery qenv = new XQuery("*:environment[*]", ctx).context(test);
    final XdmValue ienv = qenv.next();
    if(ienv != null) e = new QT3Env(ctx, ienv);
    qenv.close();

    // set base uri
    String b = base;
    if(e == null) {
      final String env = asString("*:environment/@ref", test);
      if(!env.isEmpty()) {
        // check if environment is defined in test-set
        e = envs(envs, env);
        // check if environment is defined in catalog
        if(e == null) {
          e = envs(genvs, env);
          b = null;
        }
        if(e == null) Util.errln("%: environment '%' not found.", name, env);
      }
    }

    // retrieve query to be run
    final Performance perf = new Performance();
    final String qfile = asString("*:test/@file", test);
    String string;
    if(qfile.isEmpty()) {
      // get query string
      string = asString("*:test", test);
    } else {
      // get query from file
      string = string(new IOFile(base, qfile).read());
    }

    if(verbose) Util.outln(name);
    final XQuery query = new XQuery(string, ctx);
    if(b != null) query.baseURI(b);

    // add modules
    final String qm = "for $m in *:module return ($m/@uri, $m/@file)";
    final XQuery qmod = new XQuery(qm, ctx).context(test);
    while(true) {
      final XdmItem uri = qmod.next();
      if(uri == null) break;
      final XdmItem file = qmod.next();
      if(file == null) break;
      query.addModule(uri.getString(), base + file.getString());
    }

    final QT3Result result = new QT3Result();
    try {
      if(e != null) {
        // bind namespaces
        for(final HashMap<String, String> ns : e.namespaces) {
          query.namespace(ns.get(PREFIX), ns.get(URI));
        }
        // bind variables
        for(final HashMap<String, String> par : e.params) {
          query.bind(par.get(NNAME), new XQuery(par.get(SELECT), ctx).value());
        }
        // bind documents
        for(final HashMap<String, String> src : e.sources) {
          // add document reference
          final String file = file(b, src.get(FILE));
          query.addDocument(src.get(URI), file);
          final String role = src.get(ROLE);
          if(role == null) continue;
          final Object call = Function.DOC.get(Str.get(file));
          if(role.equals(".")) query.context(call);
          else query.bind(role, call);
        }
        // bind resources
        for(final HashMap<String, String> src : e.resources) {
          query.addResource(src.get(URI), file(b, src.get(FILE)));
        }
        // bind collections
        query.addCollection(e.collURI, e.collSources.toArray());
        if(e.collContext) {
          query.context(Function.COLLECTION.get(Str.get(e.collURI)));
        }
        // set base uri
        if(e.baseURI != null) query.baseURI(e.baseURI);
      }

      // run query
      result.value = query.value();
    } catch(final XQueryException ex) {
      result.exc = ex;
      result.value = null;
    } catch(final Throwable ex) {
      // unexpected error (potential bug)
      result.error = ex;
      Util.errln("Query: " + name);
      ex.printStackTrace();
    }

    if(slow != null) {
      final long l = perf.time();
      if(l > 100000000) slow.put(-l, name);
    }

    // revert to XQuery as default
    ctx.prop.set(Prop.XQUERY3, true);

    final String msg = test(result, expected);
    final TokenBuilder tmp = new TokenBuilder();
    tmp.add(name).add(NL);
    tmp.add(noComments(string)).add(NL);

    boolean err = result.value == null;
    String res;
    try {
      res = result.error != null ? result.error.toString() : result.exc != null ?
          result.exc.getCode() + ": " + result.exc.getLocalizedMessage() :
          asString("serialize(., map { 'indent' := 'no' })", result.value);
    } catch(final XQueryException ex) {
      res = ex.getCode() + ": " + ex.getLocalizedMessage();
      err = true;
    }

    tmp.add(err ? "Error : " : "Result: ").add(noComments(res)).add(NL);
    if(msg == null) {
      tmp.add(NL);
      right.add(tmp.finish());
      correct++;
    } else {
      tmp.add("Expect: " + noComments(msg)).add(NL).add(NL);
      wrong.add(tmp.finish());
    }

    query.close();
    qexp.close();
  }

  /**
   * Removes comments from the specified string.
   * @param in input string
   * @return result
   */
  private String noComments(final String in) {
    return QueryProcessor.removeComments(in, maxout);
  }

  /**
   * Checks if the current query is supported.
   * @param node query context
   * @return result of check
   */
  private boolean supported(final XdmValue node) {
    /* feature: schemaImport schemaValidation staticTyping
         schemaValidation schemaImport collection-stability
         directory-as-collection-uri staticTyping xpath-1.0-compatibility
         namespace-axis schema-location-hint
       spec: XQ10+ XP30+ XQ30+ XT30+
       xsd-version: 1.1
       language, limits, calendar, format-integer-sequence, default-language
     */

    // skip schema import, schema validation, and XML 1.1
    final XQuery q = new XQuery(
        "*:environment/*:collation | *:dependency[" +
        "@type='feature' and" +
        " @value=('schemaImport','schemaValidation','namespace-axis') or " +
        "@type='xml-version' and @value='1.1' or" +
        "@type='xsd-version' and @value='1.1' or" +
        "@type='spec' and contains(@value, 'XT30')]", ctx).context(node);

    try {
      return q.next() == null;
    } finally {
      q.close();
    }
  }

  /**
   * Returns the specified environment, or {@code null}.
   * @param envs environments
   * @param ref reference
   * @return environment
   */
  private static QT3Env envs(final ArrayList<QT3Env> envs, final String ref) {
    for(final QT3Env e : envs) if(e.name.equals(ref)) return e;
    return null;
  }

  /**
   * Tests the result of a test case.
   * @param result resulting value
   * @param expected expected result
   * @return optional expected test suite result
   */
  private String test(final QT3Result result, final XdmValue expected) {
    final String type = expected.getName();
    final XdmValue value = result.value;

    try {
      String msg;
      if(type.equals("error")) {
        msg = assertError(result, expected);
      } else if(type.equals("all-of")) {
        msg = allOf(result, expected);
      } else if(type.equals("any-of")) {
        msg = anyOf(result, expected);
      } else if(value != null) {
        if(type.equals("assert")) {
          msg = assertQuery(value, expected);
        } else if(type.equals("assert-count")) {
          msg = assertCount(value, expected);
        } else if(type.equals("assert-deep-eq")) {
          msg = assertDeepEq(value, expected);
        } else if(type.equals("assert-empty")) {
          msg = assertEmpty(value);
        } else if(type.equals("assert-eq")) {
          msg = assertEq(value, expected);
        } else if(type.equals("assert-false")) {
          msg = assertBoolean(value, false);
        } else if(type.equals("assert-permutation")) {
          msg = assertPermutation(value, expected);
        } else if(type.equals("assert-xml")) {
          msg = assertSerialization(value, expected);
        } else if(type.equals("assert-serialization-error")) {
          msg = assertSerialError(value, expected);
        } else if(type.equals("assert-string-value")) {
          msg = assertStringValue(value, expected);
        } else if(type.equals("assert-true")) {
          msg = assertBoolean(value, true);
        } else if(type.equals("assert-type")) {
          msg = assertType(value, expected);
        } else {
          msg = "Test type not supported: " + type;
        }
      } else {
        msg = expected.toString();
      }
      return msg;
    } catch(final Exception ex) {
      ex.printStackTrace();
      return "Exception: " + ex.getMessage();
    }
  }

  /**
   * Tests error.
   * @param result query result
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertError(final QT3Result result, final XdmValue expect) {
    final String exp = asString('@' + CODE, expect);
    if(result.exc == null) return exp;
    final String res = result.exc.getCode();
    return !errors || exp.equals("*") || exp.equals(res) ? null : exp;
  }

  /**
   * Tests all-of.
   * @param res resulting value
   * @param exp expected result
   * @return optional expected test suite result
   */
  private String allOf(final QT3Result res, final XdmValue exp) {
    final XQuery query = new XQuery("*", ctx).context(exp);
    try {
      final TokenBuilder tb = new TokenBuilder();
      for(final XdmItem it : query) {
        final String msg = test(res, it);
        if(msg != null) tb.add(tb.isEmpty() ? "" : ", ").add(msg);
      }
      return tb.isEmpty() ? null : tb.toString();
    } finally {
      query.close();
    }
  }

  /**
   * Tests any-of.
   * @param res resulting value
   * @param exp expected result
   * @return optional expected test suite result
   */
  private String anyOf(final QT3Result res, final XdmValue exp) {
    final XQuery query = new XQuery("*", ctx).context(exp);
    final TokenBuilder tb = new TokenBuilder();
    try {
      for(final XdmItem it : query) {
        final String msg = test(res, it);
        if(msg == null) return null;
        tb.add(tb.isEmpty() ? "" : ", ").add(msg);
      }
      return "any of { " + tb + " }";
    } finally {
      query.close();
    }
  }

  /**
   * Tests assertion.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertQuery(final XdmValue value, final XdmValue expect) {
    final String exp = expect.getString();
    final XQuery query = new XQuery(exp, ctx);
    try {
      return query.bind("result", value).value().getBoolean() ? null : exp;
    } catch(final XQueryException ex) {
      // should not occur
      return ex.getException().getMessage();
    } finally {
      query.close();
    }
  }

  /**
   * Tests count.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private static String assertCount(final XdmValue value, final XdmValue expect) {
    final long exp = expect.getInteger();
    final int res = value.size();
    return exp == res ? null : Util.info("% items (% found)", exp, res);
  }

  /**
   * Tests equality.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertEq(final XdmValue value, final XdmValue expect) {
    final XQuery query = new XQuery(expect.getString(), ctx);
    try {
      final XdmItem exp = query.next();
      final XdmItem res = value instanceof XdmItem ? (XdmItem) value : null;
      return exp.equal(res) ? null : exp.toString();
    } catch(final XQueryException err) {
      return err.getException().getMessage();
    } finally {
      query.close();
    }
  }

  /**
   * Tests deep equals.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertDeepEq(final XdmValue value, final XdmValue expect) {
    final XQuery query = new XQuery(expect.getString(), ctx);
    try {
      final XdmValue exp = query.value();
      return exp.deepEqual(value) ? null : exp.toString();
    } finally {
      query.close();
    }
  }

  /**
   * Tests permutation.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertPermutation(final XdmValue value, final XdmValue expect) {
    final XQuery query = new XQuery(expect.getString(), ctx);
    try {
      // cache expected results
      final HashSet<String> exp = new HashSet<String>();
      for(final XdmItem it : query) exp.add(it.getString());
      // cache actual results
      final HashSet<String> res = new HashSet<String>();
      for(final XdmItem it : value) res.add(it.getString());

      if(exp.size() != res.size())
        return Util.info("% results (found: %)", exp.size(), res.size());

      for(final String s : exp.toArray(new String[exp.size()])) {
        if(!res.contains(s)) return Util.info("% (missing)", s);
      }
      for(final String s : res.toArray(new String[exp.size()])) {
        if(!exp.contains(s))
          return Util.info("% (missing in expected result)", s);
      }
      return null;
    } finally {
      query.close();
    }
  }

  /**
   * Tests the serialized result.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertSerialization(final XdmValue value, final XdmValue expect) {
    final String file = asString("@file", expect);
    final boolean norm = asBoolean("@normalize-space=('true','1')", expect);
    final boolean pref = asBoolean("@ignore-prefixes=('true','1')", expect);

    try {
      String exp = normNL(file.isEmpty() ?
          expect.getString() : string(new IOFile(base, file).read()));
      if(norm) exp = string(norm(token(exp)));

      final String res = normNL(asString("serialize(., map{ 'indent':='no' })", value));
      if(exp.equals(res)) return null;
      final String r = normNL(asString(
          "serialize(., map{ 'indent':='no', 'omit-xml-declaration':='no' })", value));
      if(exp.equals(r)) return null;

      // include check for comments, processing instructions and namespaces
      String flags = "'" + Flag.ALLNODES + "'";
      if(!pref) flags += ",'" + Flag.NAMESPACES + "'";
      final String query = Function.DEEP_EQUAL_OPT.args("<X>" + exp + "</X>",
          "<X>" + res + "</X>" , "(" + flags + ")");
      return asBoolean(query, expect) ? null : exp;
    } catch(final IOException ex) {
      return Util.message(ex);
    }
  }

  /**
   * Tests a serialization error.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertSerialError(final XdmValue value, final XdmValue expect) {
    final String exp = asString('@' + CODE, expect);
    try {
      value.toString();
      return exp;
    } catch(final RuntimeException qe) {
      final String res = qe.getMessage().replaceAll("\\[|\\].*\r?\n?.*", "");
      return !errors || exp.equals("*") || exp.equals(res) ? null :
        Util.info("% (found: %)", exp, res);
    }
  }

  /**
   * Tests string value.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertStringValue(final XdmValue value, final XdmValue expect) {
    String exp = expect.getString();
    // normalize space
    final boolean norm = asBoolean("@normalize-space=('true','1')", expect);
    if(norm) exp = string(norm(token(exp)));

    final TokenBuilder tb = new TokenBuilder();
    int c = 0;
    for(final XdmItem it : value) {
      if(c != 0) tb.add(' ');
      tb.add(it.getString());
      c++;
    }

    final String res = norm ? string(norm(tb.finish())) : tb.toString();
    return exp.equals(res) ? null : exp;
  }

  /**
   * Tests boolean.
   * @param value resulting value
   * @param exp expected
   * @return optional expected test suite result
   */
  private static String assertBoolean(final XdmValue value, final boolean exp) {
    return value.getType().eq(SeqType.BLN) && value.getBoolean() == exp ?
        null : Util.info(exp);
  }

  /**
   * Tests empty sequence.
   * @param value resulting value
   * @return optional expected test suite result
   */
  private static String assertEmpty(final XdmValue value) {
    return value == XdmEmpty.EMPTY ? null : "";
  }

  /**
   * Tests type.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertType(final XdmValue value, final XdmValue expect) {
    final String exp = expect.getString();

    try {
      final XQuery query = new XQuery("$result instance of " + exp, ctx);
      return query.bind("result", value).value().getBoolean() ? null :
        Util.info("Type '%' (found: '%')", exp, value.getType().toString());
    } catch(final XQueryException ex) {
      // should not occur
      return ex.getException().getMessage();
    }
  }

  /**
   * Returns the string representation of a query result.
   * @param query query string
   * @param value optional context value
   * @return optional expected test suite result
   */
  String asString(final String query, final XdmValue value) {
    return XQuery.string(query, value, ctx);
  }

  /**
   * Returns the boolean representation of a query result.
   * @param query query string
   * @param value optional context value
   * @return optional expected test suite result
   */
  boolean asBoolean(final String query, final XdmValue value) {
    final XQuery qp = new XQuery(query, ctx).context(value);
    try {
      final XdmItem it = qp.next();
      return it != null && it.getBoolean();
    } finally {
      qp.close();
    }
  }

  /**
   * Returns the path to a given file.
   * @param b base path, possibly {@code null}
   * @param file file name
   * @return path to the file
   */
  private String file(final String b, final String file) {
    final String dir = b != null ? b : basePath;
    return dir == null ? file : new File(dir, file).getAbsolutePath();
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
   * Normalizes newline characters.
   * @param in input string
   * @return result
   */
  private static String normNL(final String in) {
    return in.replaceAll("\r\n|\r|\n", NL);
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  private void parseArguments(final String[] args) throws IOException {
    final Args arg = new Args(args, this, " -v [pat]" + NL +
        " [pat] perform tests starting with a pattern" + NL +
        " -a  save all tests" + NL +
        " -d  debugging mode" + NL +
        " -e  ignore error codes" + NL +
        " -i  also save ignored files" + NL +
        " -p  path to the test suite" + NL +
        " -s  print slow queries" + NL +
        " -v  verbose output",
        Util.info(Text.CONSOLE, Util.name(this)));

    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'v') {
          verbose = true;
        } else if(c == 'a') {
          all = true;
        } else if(c == 'd') {
          ctx.mprop.set(MainProp.DEBUG, true);
        } else if(c == 'i') {
          ignoring = true;
        } else if(c == 'e') {
          errors = false;
        } else if(c == 's') {
          slow = new TreeMap<Long, String>();
        } else if(c == 'p') {
          final File f = new File(arg.string());
          if(!f.isDirectory()) arg.usage();
          basePath = f.getCanonicalPath();
        } else {
          arg.usage();
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
    /** Query result. */
    XdmValue value;
    /** Query exception. */
    XQueryException exc;
    /** Query error. */
    Throwable error;
  }

  /**
   * Returns the sandbox database path.
   * @return database path
   */
  private IOFile sandbox() {
    return new IOFile(Prop.TMP, testid);
  }
}
