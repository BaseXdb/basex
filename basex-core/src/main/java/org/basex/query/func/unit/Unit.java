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
import org.basex.query.expr.constr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XQUnit tests: Testing single modules.
 *
 * @author BaseX Team 2005-23, BSD License
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
    final FBuilder suite = new FBuilder(new FElem(TESTSUITE)).add(NAME, file.url());
    final ArrayList<StaticFunc> beforeModule = new ArrayList<>(0);
    final ArrayList<StaticFunc> afterModule = new ArrayList<>(0);
    final ArrayList<StaticFunc> before = new ArrayList<>(0);
    final ArrayList<StaticFunc> after = new ArrayList<>(0);
    final ArrayList<QNm> beforeFilter = new ArrayList<>();
    final ArrayList<QNm> afterFilter = new ArrayList<>();
    final ArrayList<StaticFunc> test = new ArrayList<>(0);
    final Performance perf = new Performance();

    try(QueryContext qc = new QueryContext(ctx)) {
      input = file.string();
      qc.parse(input, file.path());

      // loop through all functions
      for(final StaticFunc sf : qc.functions.funcs()) {
        // find Unit annotations
        final AnnList anns = sf.anns;
        boolean unit = false;
        for(final Ann ann : anns) {
          unit = unit || ann.definition != null && eq(ann.definition.uri, QueryText.UNIT_URI);
        }
        if(!unit) continue;

        // Unit function:
        if(anns.contains(PRIVATE)) throw UNIT_PRIVATE_X.get(null, sf.name.local());
        if(sf.params.length > 0) throw UNIT_NOARGS_X.get(null, sf.name.local());

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
        if(vs == 2 && eq(EXPECTED, value.itemAt(0).string(null))) {
          code = QNm.resolve(value.itemAt(1).string(null), QueryText.ERROR_URI, sf.sc, sf.info);
        } else if(vs != 0) {
          throw BASEX_ANNOTATION2_X_X.get(ann.info, ann, arguments(vs));
        }

        final FBuilder testcase = new FBuilder(new FElem(TESTCASE)).add(NAME, sf.name.local());
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
              testcase.add(new FBuilder(new FElem(FAILURE)).
                  add(new FBuilder(new FElem(EXPECTED)).add(code.prefixId())));
            }
          } catch(final QueryException ex) {
            addError(ex, testcase, code);
          }
        } else {
          // skip test
          final Value ignored = ignore.value();
          testcase.add(SKIPPED, ignored.isEmpty() ? EMPTY : ignored.itemAt(0).string(null));
          skipped++;
        }
        suite.add(testcase.add(TIME, time(perf2)));
      }

      // run finalizing tests
      for(final StaticFunc sf : afterModule) eval(sf);

    } catch(final QueryException ex) {
      if(current == null) {
        // handle errors caused by parsing or compilation
        addError(ex, suite, null);
      } else {
        // handle errors caused by initializing or finalizing unit functions
        final FBuilder init = new FBuilder(new FElem(TESTINIT)).
            add(NAME, current.name.local()).add(TIME, time(perf));
        addError(ex, init, null);
        suite.add(init);
      }
    }

    if(!suite.isEmpty()) {
      suite.add(TIME, time(perf));
      suite.add(TESTS, token(tests));
      suite.add(FAILURES, token(failures));
      suite.add(ERRORS, token(errors));
      suite.add(SKIPPED, token(skipped));
      suites.add(suite);
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
      if(name.length != 0) return QNm.resolve(name, sf.name.uri(), sf.sc, sf.info);
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
        error = new FBuilder(new FElem(FAILURE));
      } else {
        errors++;
        error = new FBuilder(new FElem(ERROR));
      }
      error.add(LINE, token(ex.line())).add(COLUMN, token(ex.column()));
      final String url = IO.get(ex.file()).url();
      if(!file.url().equals(url)) error.add(URI, url);

      if(ex instanceof UnitException) {
        // unit exception: add expected and returned values
        final UnitException ue = (UnitException) ex;
        error.add(element(ue.returned, RETURNED, ue.count));
        error.add(element(ue.expected, EXPECTED, ue.count));
      } else if(!fail) {
        // exception other than failure: add type
        error.add(TYPE, ex.qname().prefixId(QueryText.ERROR_URI));
      }

      // add info
      final Value value = ex.value();
      if(value != null && value.isItem()) {
        error.add(element((Item) value, INFO, -1));
      } else {
        // otherwise, add error message
        error.add(new FBuilder(new FElem(INFO)).add(ex.getLocalizedMessage()));
      }
      testcase.add(error);
    }
  }

  /**
   * Creates a new element.
   * @param item item
   * @param name name (expected/returned)
   * @param count item count (ignore it {@code -1})
   * @return element
   */
  private static FBuilder element(final Item item, final byte[] name, final int count) {
    final FBuilder elem = new FBuilder(new FElem(name));
    if(item != null) {
      if(item instanceof ANode) {
        elem.add((ANode) item);
      } else {
        try {
          elem.add(item.string(null));
        } catch(final QueryException ex) {
          Util.debug(ex);
          elem.add(item.toString());
        }
      }
      if(count != -1) elem.add(ITEM, token(count)).add(TYPE, item.type.toString());
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
    return DTDur.get(p.ns() / 1000000).string(null);
  }
}
