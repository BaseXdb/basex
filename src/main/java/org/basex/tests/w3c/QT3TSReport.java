package org.basex.tests.w3c;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.out.*;
import org.basex.query.value.node.*;

/**
 * QT3TS Report builder.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class QT3TSReport {
  /** Dependencies. */
  private static final String[][] DEPENDENCIES = {
    { "calendar", "CB", "true" },
    { "default-language", "en", "true" },
    { "feature", "collection-stability", "true" },
    { "feature", "directory-as-collection-uri", "true" },
    { "feature", "higherOrderFunctions", "true" },
    { "feature", "moduleImport", "true" },
    { "feature", "namespace-axis", "true" },
    { "feature", "schemaAware", "false" },
    { "feature", "schemaImport", "false" },
    { "feature", "schema-location-hint", "false" },
    { "feature", "schemaValidation", "false" },
    { "feature", "staticTyping", "false" },
    { "feature", "xpath-1.0-compatibility", "true" },
    { "format-integer-sequence", "\u03B1", "true" },
    { "format-integer-sequence", "\u0661", "true" },
    { "format-integer-sequence", "\u2460", "true" },
    { "format-integer-sequence", "\u2474", "true" },
    { "format-integer-sequence", "\u2488", "true" },
    { "format-integer-sequence", "\u4E00", "true" },
    { "format-integer-sequence", "\uFBF4", "true" },
    { "format-integer-sequence", "Α", "true" },
    { "language", "de", "true" },
    { "language", "xib", "true" },
    { "limits", "year_lt_0", "true" },
    { "unicode-normalization-form", "FULLY-NORMALIZED", "false" },
    { "unicode-normalization-form", "NFD", "true" },
    { "unicode-normalization-form", "NFKC", "true" },
    { "unicode-normalization-form", "NFKD", "true" },
    { "xml-version", "1.0", "true" },
    { "xml-version", "1.0:4-", "false" },
    { "xml-version", "1.0:5+ 1.1", "true" },
    { "xml-version", "1.1", "false" }
  };

  /** URI of test suite. */
  private static final String URI = "http://www.w3.org/2012/08/qt-fots-results";
  /** List of test sets and test cases (test sets only consist of a name; test cases
   * consist of a name and a result (pass/fail). */
  private final ArrayList<String[]> tests = new ArrayList<String[]>();

  /**
   * Adds the name of a test set.
   * @param name name of test set
   */
  public void addSet(final String name) {
    tests.add(new String[] { name });
  }

  /**
   * Adds the name and result of a test case.
   * @param name name of test case
   * @param ok success flag
   */
  public void addTest(final String name, final boolean ok) {
    tests.add(new String[] { name, ok ? "pass" : "fail" });
  }

  /**
   * Creates a report.
   * @param ctx database context
   * @return report stream
   * @throws Exception exception
   */
  public ArrayOutput create(final Context ctx) throws Exception {
    final String dquery = "replace(string(current-date()),'\\+.*','')";
    final String date = new XQuery(dquery).execute(ctx);

    final FElem root = element("test-suite-result");

    // submission element
    final FElem submission = element("submission", root);
    submission.add("anonymous", "false");

    final FElem created = element("created", submission);
    created.add("by", Prop.AUTHOR);
    created.add("email", "cg@basex.org");
    created.add("organization", Prop.ENTITY);
    created.add("on", date);

    final FElem testRun = element("test-run", submission);
    testRun.add("test-suite-version", "CVS");
    testRun.add("date-run", date);

    element("notes", submission);

    // product element
    final FElem product = element("product", root);
    product.add("vendor", Prop.ENTITY);
    product.add("name", Prop.NAME);
    product.add("version", Prop.VERSION);
    product.add("released", "true");
    product.add("open-source", "true");
    product.add("language", "XQ30");

    // dependency element
    for(final String[] deps : DEPENDENCIES) {
      final FElem dependency = element("dependency", product);
      dependency.add("type", deps[0]);
      dependency.add("value", deps[1]);
      dependency.add("satisfied", deps[2]);
    }

    // test-set elements
    FElem ts = null;
    for(final String[] test : tests) {
      if(test.length == 1) {
        ts = element("test-set", root);
        ts.add("name", test[0]);
      } else {
        final FElem tc = element("test-case", ts);
        tc.add("name", test[0]);
        tc.add("result", test[1]);
      }
    }
    return root.serialize();
  }

  /**
   * Creates a root element.
   * @param name name of element
   * @return element node
   */
  private FElem element(final String name) {
    return new FElem(name, URI).declareNS();
  }

  /**
   * Creates an element.
   * @param name name of element
   * @param parent parent node
   * @return element node
   */
  private FElem element(final String name, final FElem parent) {
    final FElem elem = new FElem(name, URI);
    parent.add(elem);
    return elem;
  }
}
