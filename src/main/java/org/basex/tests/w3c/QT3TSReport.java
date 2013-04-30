package org.basex.tests.w3c;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.out.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * QT3TS Report builder.
 *
 * @author BaseX Team 2005-12, BSD License
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
  private static final byte[] URI = token("http://www.w3.org/2012/08/qt-fots-results");
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

    final FElem root = element("test-suite-result", null);

    // submission element
    final FElem submission = element("submission", root);
    submission.add(new QNm("anonymous"), "false");

    final FElem created = element("created", submission);
    add(created, "by", Prop.AUTHOR);
    add(created, "email", "cg@basex.org");
    add(created, "organization", Prop.ENTITY);
    add(created, "on", date);

    final FElem testRun = element("test-run", submission);
    add(testRun, "test-suite-version", "CVS");
    add(testRun, "date-run", date);

    element("notes", submission);

    // product element
    final FElem product = element("product", root);
    add(product, "vendor", Prop.ENTITY);
    add(product, "name", Prop.NAME);
    add(product, "version", Prop.VERSION);
    add(product, "released", "true");
    add(product, "open-source", "true");
    add(product, "language", "XQ30");

    // dependency element
    for(final String[] deps : DEPENDENCIES) {
      final FElem dependency = element("dependency", product);
      add(dependency, "type", deps[0]);
      add(dependency, "value", deps[1]);
      add(dependency, "satisfied", deps[2]);
    }

    // test-set elements
    FElem ts = null;
    for(final String[] test : tests) {
      if(test.length == 1) {
        ts = element("test-set", root);
        add(ts, "name", test[0]);
      } else {
        final FElem tc = element("test-case", ts);
        add(tc, "name", test[0]);
        add(tc, "result", test[1]);
      }
    }
    return root.serialize();
  }

  /**
   * Creates a new element.
   * @param name name of element
   * @param root optional root node
   * @return element node
   */
  private FElem element(final String name, final FElem root) {
    final QNm qn = new QNm(name, URI);
    if(root == null) return new FElem(qn, new Atts(EMPTY, URI));

    final FElem elem = new FElem(qn);
    root.add(elem);
    return elem;
  }

  /**
   * Adds attributes to the specified element.
   * @param elem element
   * @param name attribute name
   * @param value attribute value
   * @return element node
   */
  private FElem add(final FElem elem, final String name, final String value) {
    return elem.add(new QNm(name), value);
  }
}
