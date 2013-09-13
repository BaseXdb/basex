package org.basex.tests.w3c;

import static org.basex.core.Prop.NL;
import static org.basex.tests.w3c.QT3Constants.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.namespace.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.Compare.Mode;
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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class QT3TS {
  /** EQName pattern. */
  private static final Pattern BIND = Pattern.compile("^Q\\{(.*?)\\}(.+)$");

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

  /** Current base uri. */
  private String baseURI;
  /** Current base directory. */
  private String baseDir;

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
    init();

    final Performance perf = new Performance();
    ctx.prop.set(Prop.CHOP, false);
    ctx.prop.set(Prop.INTPARSE, false);
    ctx.prop.set(Prop.SERIALIZER, "omit-xml-declaration=no,indent=no");

    final XdmValue doc = new XQuery("doc('" + file(false, CATALOG) + "')", ctx).value();
    final String version = asString("*:catalog/@version", doc);
    Util.outln(NL + "QT3 Test Suite " + version);
    Util.outln("Test directory: " + new File(".").getCanonicalPath());
    Util.out("Parsing queries");

    for(final XdmItem ienv : new XQuery("*:catalog/*:environment", ctx).context(doc))
      genvs.add(new QT3Env(ctx, ienv));

    for(final XdmItem it : new XQuery("for $f in //*:test-set/@file return string($f)",
        ctx).context(doc)) testSet(it.getString());

    final StringBuilder result = new StringBuilder();
    result.append(" Rate    : ").append(pc(correct, tested)).append(NL);
    result.append(" Total   : ").append(total).append(NL);
    result.append(" Tested  : ").append(tested).append(NL);
    result.append(" Wrong   : ").append(tested - correct).append(NL);
    result.append(" Ignored : ").append(ignored).append(NL);

    // save log data
    Util.outln(NL + "Writing log file '" + testid + ".log'...");
    final PrintOutput po = new PrintOutput(testid + ".log");
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

    // save report
    if(report != null) {
      final String file = "ReportingResults/results_" +
          Prop.NAME + "_" + Prop.VERSION + IO.XMLSUFFIX;
      new IOFile(file).write(report.create(ctx).toArray());
      Util.outln("Creating report '" + file + "'...");
    }

    Util.out(NL + result);
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
    final XdmValue doc = new XQuery("doc(' " + file(false, name) + "')", ctx).value();
    final XdmValue set = new XQuery("*:test-set", ctx).context(doc).value();
    final IO base = IO.get(doc.getBaseURI());
    baseURI = base.path();
    baseDir = base.dirPath();

    if(supported(set)) {
      // parse environment of test-set
      final ArrayList<QT3Env> envs = new ArrayList<QT3Env>();
      for(final XdmItem ienv : new XQuery("*:environment", ctx).context(set))
        envs.add(new QT3Env(ctx, ienv));

      if(report != null) report.addSet(asString("@name", set));

      // run all test cases
      for(final XdmItem its : new XQuery("*:test-case", ctx).context(set)) {
        try {
          testCase(its, envs);
        } catch(final IOException ex) {
          Util.debug(ex);
        }
      }
    }
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
    final XdmValue expected = new XQuery("*:result/*[1]", ctx).context(test).value();

    // use XQuery 1.0 if XQ10 or XP20 is specified
    if(new XQuery("*:dependency[@type='spec'][matches(@value,'(XQ10)([^+]|$)')]", ctx).
        context(test).next() != null) ctx.prop.set(Prop.XQUERY3, false);

    // check if environment is defined in test-case
    QT3Env e = null;
    final XdmValue ienv = new XQuery("*:environment[*]", ctx).context(test).value();
    if(ienv.size() != 0) e = new QT3Env(ctx, ienv);

    // parse local environment
    boolean base = true;
    if(e == null) {
      final String env = asString("*:environment/@ref", test);
      if(!env.isEmpty()) {
        // check if environment is defined in test-set
        e = envs(envs, env);
        // check if environment is defined in catalog
        if(e == null) {
          e = envs(genvs, env);
          base = false;
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
      string = string(new IOFile(baseDir, qfile).read());
    }

    if(verbose) Util.outln(name);
    final XQuery query = new XQuery(string, ctx);
    if(base) query.baseURI(baseURI);

    // add modules
    final String qm = "for $m in *:module return ($m/@uri, $m/@file)";
    final XQuery qmod = new XQuery(qm, ctx).context(test);
    while(true) {
      final XdmItem uri = qmod.next();
      if(uri == null) break;
      final XdmItem file = qmod.next();
      if(file == null) break;
      query.addModule(uri.getString(), baseDir + file.getString());
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
          final String file = file(base, src.get(FILE));
          query.addDocument(src.get(URI), file);
          final String role = src.get(ROLE);
          if(role == null) continue;
          final Object call = Function.DOC.get(Str.get(file));
          if(role.equals(".")) query.context(call);
          else query.bind(role, call);
        }
        // bind resources
        for(final HashMap<String, String> src : e.resources) {
          query.addResource(src.get(URI), file(base, src.get(FILE)), src.get(ENCODING));
        }
        // bind collections
        query.addCollection(e.collURI, e.collSources.toArray());
        if(e.collContext) {
          query.context(Function.COLLECTION.get(Str.get(e.collURI)));
        }
        // bind context item
        if(e.context != null) {
          query.context(e.context);
        }
        // set base uri
        if(e.baseURI != null) query.baseURI(e.baseURI);
        // bind decimal formats
        for(final Map.Entry<QName, HashMap<String, String>> df :
          e.decFormats.entrySet()) {
          query.decimalFormat(df.getKey(), df.getValue());
        }
      }

      // run query
      result.value = query.value();
      result.sprop = query.serializer();
    } catch(final XQueryException ex) {
      result.err = ex;
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

    final String exp = test(result, expected);
    final TokenBuilder tmp = new TokenBuilder();
    tmp.add(name).add(NL);
    tmp.add(noComments(string)).add(NL);

    boolean err = result.value == null;
    String res;
    try {
      if(result.error != null) {
        res = result.error.toString();
      } else if(result.err != null) {
        res = result.err.getCode() + ": " + result.err.getLocalizedMessage();
      } else {
        result.sprop.set(SerializerProp.S_OMIT_XML_DECLARATION, "yes");
        res = serialize(result.value, result.sprop);
      }
    } catch(final XQueryException ex) {
      res = ex.getCode() + ": " + ex.getLocalizedMessage();
      err = true;
    } catch(final Throwable ex) {
      res = "Unexpected: " + ex.toString();
      err = true;
    }

    tmp.add(err ? "Error : " : "Result: ").add(noComments(res)).add(NL);
    if(exp == null) {
      tmp.add(NL);
      right.add(tmp.finish());
      correct++;
    } else {
      tmp.add("Expect: " + noComments(exp)).add(NL).add(NL);
      wrong.add(tmp.finish());
    }
    if(report != null) report.addTest(name, exp == null);
  }

  /**
   * Removes comments from the specified string.
   * @param in input string
   * @return result
   */
  private String noComments(final String in) {
    return QueryProcessor.removeComments(in, maxout);
  }

  /** Flags for dependencies that are not supported. */
  private static final String NOSUPPORT =
    "('schema-location-hint','schemaAware','schemaImport'," +
    "'schemaValidation','staticTyping')";

  /**
   * Checks if the current test case is supported.
   * @param test test case
   * @return result of check
   */
  private boolean supported(final XdmValue test) {
    // the following query generates a result if the specified test is not supported
    return new XQuery(
      "*:environment/*:collation |" + // skip collation tests
      "*:dependency[" +
      // skip schema imports, schema validation, namespace axis, static typing
      "@type = 'feature' and (" +
      " @value = " + NOSUPPORT + " and (@satisfied = 'true' or empty(@satisfied)) or" +
      " @value != " + NOSUPPORT + "and @satisfied = 'false') or " +
      // skip fully-normalized unicode tests
      "@type = 'unicode-normalization-form' and @value = 'FULLY-NORMALIZED' or " +
      // skip xml/xsd 1.1 tests
      "@type = ('xml-version', 'xsd-version') and @value = ('1.1', '1.0:4-') or " +
      // skip non-XQuery tests
      "@type = 'spec' and not(contains(@value, 'XQ'))" +
      "]", ctx).context(test).value().size() == 0;
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
   * @return {@code null} if test was successful; otherwise, expected test suite result
   */
  private String test(final QT3Result result, final XdmValue expected) {
    final String type = expected.getName().getLocalPart();
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
          msg = assertXML(value, expected);
        } else if(type.equals("serialization-matches")) {
          msg = serializationMatches(value, expected, result.sprop);
        } else if(type.equals("assert-serialization-error")) {
          msg = assertSerializationError(value, expected, result.sprop);
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
    if(result.err == null) return exp;
    if(!errors || exp.equals("*")) return null;

    final QNm resErr = result.err.getException().qname();

    String name = exp, uri = string(QueryText.ERRORURI);
    final Matcher m = BIND.matcher(exp);
    if(m.find()) {
      uri = m.group(1);
      name = m.group(2);
    }
    final QNm expErr = new QNm(name, uri);
    return expErr.eq(resErr) ? null : exp;
  }

  /**
   * Tests all-of.
   * @param res resulting value
   * @param exp expected result
   * @return optional expected test suite result
   */
  private String allOf(final QT3Result res, final XdmValue exp) {
    final TokenBuilder tb = new TokenBuilder();
    for(final XdmItem it : new XQuery("*", ctx).context(exp)) {
      final String msg = test(res, it);
      if(msg != null) tb.add(tb.isEmpty() ? "" : ", ").add(msg);
    }
    return tb.isEmpty() ? null : tb.toString();
  }

  /**
   * Tests any-of.
   * @param res resulting value
   * @param exp expected result
   * @return optional expected test suite result
   */
  private String anyOf(final QT3Result res, final XdmValue exp) {
    final TokenBuilder tb = new TokenBuilder();
    for(final XdmItem it : new XQuery("*", ctx).context(exp)) {
      final String msg = test(res, it);
      if(msg == null) return null;
      tb.add(tb.isEmpty() ? "" : ", ").add(msg);
    }
    return "any of { " + tb + " }";
  }

  /**
   * Tests assertion.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertQuery(final XdmValue value, final XdmValue expect) {
    final String exp = expect.getString();
    try {
      return new XQuery(exp, ctx).bind("result", value).value().getBoolean() ? null : exp;
    } catch(final XQueryException ex) {
      // should not occur
      return ex.getException().getMessage();
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
    try {
      final XdmItem exp = new XQuery(expect.getString(), ctx).next();
      final XdmItem res = value instanceof XdmItem ? (XdmItem) value : null;
      return exp.equal(res) ? null : exp.toString();
    } catch(final XQueryException err) {
      // try simple string comparison
      return expect.getString().equals(value.getString()) ? null :
        err.getException().getMessage();
    }
  }

  /**
   * Tests deep equals.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertDeepEq(final XdmValue value, final XdmValue expect) {
    final XdmValue exp = new XQuery(expect.getString(), ctx).value();
    return exp.deepEqual(value) ? null : exp.toString();
  }

  /**
   * Tests permutation.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertPermutation(final XdmValue value, final XdmValue expect) {
    // cache expected results
    final HashSet<String> exp = new HashSet<String>();
    for(final XdmItem it : new XQuery(expect.getString(), ctx))
      exp.add(it.getString());
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
  }

  /**
   * Tests the serialized result.
   * @param value resulting value
   * @param expect expected result
   * @return optional expected test suite result
   */
  private String assertXML(final XdmValue value, final XdmValue expect) {
    final String file = asString("@file", expect);
    final boolean norm = asBoolean("@normalize-space=('true','1')", expect);
    final boolean pref = asBoolean("@ignore-prefixes=('true','1')", expect);

    String exp = expect.getString();
    try {
      if(!file.isEmpty()) exp = string(new IOFile(baseDir, file).read());
      exp = normNL(exp);
      if(norm) exp = string(norm(token(exp)));

      final String res = normNL(asString("serialize(., map{ 'indent':='no' })", value));
      if(exp.equals(res)) return null;
      final String r = normNL(asString(
          "serialize(., map{ 'indent':='no', 'omit-xml-declaration':='no' })", value));
      if(exp.equals(r)) return null;

      // include check for comments, processing instructions and namespaces
      String flags = "'" + Mode.ALLNODES + "'";
      if(!pref) flags += ",'" + Mode.NAMESPACES + "'";
      final String query = Function.DEEP_EQUAL_OPT.args("<X>" + exp + "</X>",
          "<X>" + res + "</X>" , "(" + flags + ")");
      return asBoolean(query, expect) ? null : exp;
    } catch(final IOException ex) {
      return Util.info("% (found: %)", exp, ex);
    }
  }

  /**
   * Tests the serialized result.
   * @param value resulting value
   * @param expect expected result
   * @param sprop serialization properties
   * @return optional expected test suite result
   */
  private String serializationMatches(final XdmValue value, final XdmValue expect,
      final SerializerProp sprop) {

    final String exp = expect.getString();
    final String flags = asString("@flags", expect);
    final int flgs = flags.contains("i") ? Pattern.CASE_INSENSITIVE : 0;
    final Pattern pat = Pattern.compile("^.*" + exp + ".*", flgs |
        Pattern.MULTILINE | Pattern.DOTALL);

    try {
      return pat.matcher(".*" + serialize(value, sprop) + ".*").matches() ? null : exp;
    } catch(final IOException ex) {
      return Util.info("% (found: %)", exp, ex);
    }
  }


  /**
   * Tests a serialization error.
   * @param value resulting value
   * @param expect expected result
   * @param sprop serialization properties
   * @return optional expected test suite result
   */
  private String assertSerializationError(final XdmValue value, final XdmValue expect,
      final SerializerProp sprop) {

    final String exp = asString('@' + CODE, expect);
    try {
      serialize(value, sprop);
      return exp;
    } catch(final SerializerException ex) {
      if(!errors || exp.equals("*")) return null;
      final QueryException qe = ex.getCause();
      final String code = string(qe.qname().local());
      if(code.equals(exp)) return null;
      return Util.info("% (found: %)", exp, ex);
    } catch(final IOException ex) {
      return Util.info("% (found: %)", exp, ex);
    }
  }

  /**
   * Serializes values.
   * @param value resulting value
   * @param sprop serialization properties
   * @return optional expected test suite result
   * @throws IOException I/O exception
   */
  private String serialize(final XdmValue value, final SerializerProp sprop)
      throws IOException {

    final ArrayOutput ao = new ArrayOutput();
    final Serializer ser = Serializer.get(ao, sprop);
    for(final Item it : value.internal()) ser.serialize(it);
    ser.close();
    return ao.toString();
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
    final XdmValue val = new XQuery(query, ctx).context(value).value();
    return val.size() != 0 && val.getBoolean();
  }

  /**
   * Returns the path to a given file.
   * @param base base flag.
   * @param file file name
   * @return path to the file
   */
  private String file(final boolean base, final String file) {
    final String dir = base ? baseDir : basePath;
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
        " -r  generate report file" + NL +
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
        } else if(c == 'r') {
          report = new QT3TSReport();
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
   * Adds an environment variable required for function tests. Reference:
   *http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java
   */
  @SuppressWarnings("unchecked")
  private static void init() {
    final Map<String, String> ne = new HashMap<String, String>();
    ne.put("QTTEST", "42");
    ne.put("QTTEST2", "other");
    ne.put("QTTESTEMPTY", "");
    try {
      final Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
      final Field f = pe.getDeclaredField("theEnvironment");
      f.setAccessible(true);
      ((Map<String, String>) f.get(null)).putAll(ne);
      final Field f2 = pe.getDeclaredField("theCaseInsensitiveEnvironment");
      f2.setAccessible(true);
      ((Map<String, String>) f2.get(null)).putAll(ne);
    } catch(final NoSuchFieldException ex) {
      try {
        for(final Class<?> cl : Collections.class.getDeclaredClasses()) {
          if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
            final Field f = cl.getDeclaredField("m");
            f.setAccessible(true);
            ((Map<String, String>) f.get(System.getenv())).putAll(ne);
          }
        }
      } catch(final Exception e2) {
        Util.errln("Test environment variable could not be set:" + e2);
      }
    } catch(final Exception e1) {
      Util.errln("Test environment variable could not be set: " + e1);
    }
  }

  /**
   * Structure for storing XQuery results.
   */
  static class QT3Result {
    /** Serialization parameters. */
    SerializerProp sprop;
    /** Query result. */
    XdmValue value;
    /** Query exception. */
    XQueryException err;
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
