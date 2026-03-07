package org.basex.query.func.unit;

import static org.basex.query.QueryError.*;
import static org.basex.query.ann.Annotation.*;
import static org.basex.query.func.unit.Constants.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * XQUnit tests: Testing single modules.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class Unit {
  /** Database context. */
  private final Context ctx;
  /** File. */
  private final IOFile file;
  /** Parent job. */
  private final Job job;

  /** Query string. */
  private String input;
  /** Currently processed function. */
  private StaticFunc current;

  /** Failures. */
  int failures;
  /** Errors. */
  int errors;
  /** Skipped. */
  int skipped;
  /** Tests. */
  int tests;

  /**
   * Constructor.
   * @param file file
   * @param ctx database context
   * @param job job
   */
  Unit(final IOFile file, final Context ctx, final Job job) {
    this.file = file;
    this.ctx = ctx;
    this.job = job;
  }

  /**
   * Runs all tests.
   * @param suites root element
   * @throws IOException query exception
   */
  public void test(final FBuilder suites) throws IOException {
    final FBuilder suite = FElem.build(Q_TESTSUITE).attr(Q_NAME, file.url());
    final ArrayList<StaticFunc> beforeModule = new ArrayList<>(0);
    final ArrayList<StaticFunc> afterModule = new ArrayList<>(0);
    final ArrayList<StaticFunc> before = new ArrayList<>(0);
    final ArrayList<StaticFunc> after = new ArrayList<>(0);
    final ArrayList<QNm> beforeFilter = new ArrayList<>();
    final ArrayList<QNm> afterFilter = new ArrayList<>();
    final ArrayList<StaticFunc> test = new ArrayList<>(0);
    final Performance perf = new Performance();

    try(QueryContext qc = new QueryContext(ctx)) {
      input = file.readString();
      qc.parse(input, file.path());

      // loop through all functions
      for(final StaticFunc sf : qc.functions) {
        // find Unit annotations
        final AnnList anns = sf.anns;
        boolean unit = false;
        for(final Ann ann : anns) {
          final Annotation ad = ann.definition;
          unit = unit || ad != null && eq(ad.name.uri(), QueryText.UNIT_URI);
        }
        if(!unit) continue;

        // Unit function:
        if(anns.contains(PRIVATE)) throw UNIT_PRIVATE_X.get(null, sf.name.local());
        if(sf.arity() > 0) throw UNIT_NOARGS_X.get(null, sf.name.local());

        if(anns.contains(_UNIT_BEFORE_MODULE)) beforeModule.add(sf);
        if(anns.contains(_UNIT_AFTER_MODULE)) afterModule.add(sf);
        final Ann b = anns.get(_UNIT_BEFORE);
        if(b != null) {
          before.add(sf);
          beforeFilter.add(name(sf, b));
        }
        final Ann a = anns.get(_UNIT_AFTER);
        if(a != null) {
          after.add(sf);
          afterFilter.add(name(sf, a));
        }
        if(anns.contains(_UNIT_TEST)) test.add(sf);
      }

      // call initializing functions before first test
      for(final StaticFunc sf : beforeModule) eval(sf);

      for(final StaticFunc sf : test) {
        // check arguments
        final AnnList anns = sf.anns;
        final Ann ann = anns.get(_UNIT_TEST);
        final Value value = ann.value();
        final long vs = value.size();

        // expected error code
        QNm code = null;
        if(vs == 2 && eq(Q_EXPECTED.string(), value.itemAt(0).string(null))) {
          code = QNm.parse(value.itemAt(1).string(null), QueryText.ERROR_URI, null, sf.sc, sf.info);
        } else if(vs != 0) {
          throw BASEX_ANN2_X_X.get(ann.info, ann, arity(arguments(vs), new IntList().add(0)));
        }

        final FBuilder testcase = FElem.build(Q_TESTCASE).attr(Q_NAME, sf.name.local());
        tests++;

        final Performance perf2 = new Performance();
        final Ann ignore = anns.get(_UNIT_IGNORE);
        if(ignore == null) {
          try {
            // call functions marked with "before"
            int l = before.size();
            for(int i = 0; i < l; i++) {
              final QNm name = beforeFilter.get(i);
              if(name == null || name.eq(sf.name)) eval(before.get(i));
            }
            // call function
            eval(sf);
            // call functions marked with "after"
            l = after.size();
            for(int i = 0; i < l; i++) {
              final QNm name = afterFilter.get(i);
              if(name == null || name.eq(sf.name)) eval(after.get(i));
            }

            if(code != null) {
              failures++;
              testcase.node(FElem.build(Q_FAILURE).node(FElem.build(Q_EXPECTED).
                  text(code.prefixId())));
            }
          } catch(final QueryException ex) {
            addError(ex, testcase, code);
          }
        } else {
          // skip test
          final Value ignored = ignore.value();
          testcase.attr(Q_SKIPPED, ignored.isEmpty() ? EMPTY : ignored.itemAt(0).string(null));
          skipped++;
        }
        suite.node(testcase.attr(Q_TIME, time(perf2)));
      }

      // run finalizing tests
      for(final StaticFunc sf : afterModule) eval(sf);
    } catch(final QueryException ex) {
      if(current == null) {
        // handle errors caused by parsing or compilation
        addError(ex, suite, null);
      } else {
        // handle errors caused by initializing or finalizing unit functions
        final FBuilder init = FElem.build(Q_TESTINIT).attr(Q_NAME, current.name.local()).
            attr(Q_TIME, time(perf));
        addError(ex, init, null);
        suite.node(init);
      }
    }

    if(!suite.isEmpty()) {
      suites.node(suite.attr(Q_TIME, time(perf)).attr(Q_TESTS, tests).attr(Q_FAILURES, failures).
        attr(Q_ERRORS, errors).attr(Q_SKIPPED, skipped));
    }
  }

  /**
   * Returns an annotation argument as QName.
   * @param sf static function
   * @param ann annotation
   * @return QName or {@code null}
   * @throws QueryException query exception
   */
  private static QNm name(final StaticFunc sf, final Ann ann) throws QueryException {
    final Value value = ann.value();
    if(!value.isEmpty()) {
      final byte[] name = value.itemAt(0).string(null);
      if(name.length != 0) return QNm.parse(name, sf.name.uri(), null, sf.sc, sf.info);
    }
    return null;
  }

  /**
   * Adds an error element to the specified test case.
   * @param ex exception
   * @param testcase testcase element
   * @param code error code (can be {@code null})
   */
  private void addError(final QueryException ex, final FBuilder testcase, final QNm code) {
    final QNm name = ex.qname();
    if(code == null || !code.eq(name)) {
      final FBuilder error;
      final boolean fail = UNIT_FAIL.eq(name);
      if(fail) {
        failures++;
        error = FElem.build(Q_FAILURE);
      } else {
        errors++;
        error = FElem.build(Q_ERROR);
      }
      error.attr(Q_LINE, ex.line()).attr(Q_COLUMN, ex.column());
      final String url = IO.get(ex.path()).url();
      if(!file.url().equals(url)) error.attr(Q_URI, url);

      if(ex instanceof final UnitException ue) {
        // unit exception: add expected and returned values
        error.node(element(ue.returned, Q_RETURNED, ue.count));
        error.node(element(ue.expected, Q_EXPECTED, ue.count));
      } else if(!fail) {
        // exception other than failure: add type
        error.attr(Q_TYPE, ex.qname().prefixId(QueryText.ERROR_URI));
      }

      // add info
      final Value value = ex.value();
      if(value != null && value.size() == 1) {
        error.node(element((Item) value, Q_INFO, -1));
      } else {
        // otherwise, add error message
        error.node(FElem.build(Q_INFO).text(ex.getLocalizedMessage()));
      }
      testcase.node(error);
    }
  }

  /**
   * Creates a new element.
   * @param item item
   * @param name name (expected/returned)
   * @param count item count (ignore it {@code -1})
   * @return element
   */
  private static FBuilder element(final Item item, final QNm name, final int count) {
    final FBuilder elem = FElem.build(name);
    if(item != null) {
      if(item instanceof final XNode node) {
        elem.node(node);
      } else {
        try {
          elem.text(item.string(null));
        } catch(final QueryException ex) {
          Util.debug(ex);
          elem.text(item);
        }
      }
      if(count != -1) elem.attr(Q_ITEM, count).attr(Q_TYPE, item.type);
    }
    return elem;
  }

  /**
   * Evaluates a function.
   * @param func function to evaluate
   * @throws QueryException query exception
   */
  private void eval(final StaticFunc func) throws QueryException {
    current = func;

    try(QueryContext qc = job.pushJob(new QueryContext(ctx))) {
      qc.parse(input, file.path());
      qc.assign(func);
      // ignore results
      for(final Iter iter = qc.iter(); qc.next(iter) != null;);
    } finally {
      job.popJob();
    }
  }

  /**
   * Returns a token representation of the measured time.
   * @param p performance
   * @return time
   */
  static byte[] time(final Performance p) {
    return DTDur.get(p.nanoRuntime() / 1000000).string(null);
  }
}
