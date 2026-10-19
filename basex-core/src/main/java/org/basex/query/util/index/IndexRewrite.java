package org.basex.query.util.index;

import static org.basex.query.expr.path.Axis.*;
import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Index rewriting of path expressions.
 *
 * The following types of queries can be rewritten (in the examples, the equality comparison
 * is used, which will be rewritten to {@link ValueAccess} instances):
 *
 * <pre>
 * 1. A[text() = '...']    : IA('...', A)
 * 2. A[. = '...']         : IA('...', A)
 * 3. text()[. = '...']    : IA('...')
 * 4. A[B = '...']         : IA('...', B)/parent::A
 * 5. A[B/text() = '...']  : IA('...')/parent::B/parent::A
 * 6. A[B/C = '...']       : IA('...', C)/parent::B/parent::A
 * 7. A[@a = '...']        : IA('...', @a)/parent::A
 * 8. @a[. = '...']        : IA('...', @a)</pre>
 *
 * Queries of type 1, 3, 5 will not yield any results if the string to be compared is empty.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexRewrite {
  /** Private constructor. */
  private IndexRewrite() { }

  /**
   * Returns an equivalent expression which accesses an index.
   * @param path path expression
   * @param cc compilation context
   * @param rt root at compile time (can be {@code null})
   * @return original or new expression
   * @throws QueryException query exception
   */
  public static Expr get(final Path path, final CompileContext cc, final Expr rt)
      throws QueryException {

    // skip optimization if path does not start with document nodes
    if(rt == null || !rt.seqType().type.instanceOf(NodeType.DOCUMENT)) return path;

    // cache index access costs
    IndexInfo index = null;
    // cheapest predicate and step
    int predIndex = 0, stepIndex = 0;

    // check if path can be converted to an index access
    final Expr root = path.root;
    final Expr[] steps = path.steps;
    final InputInfo info = path.info();
    final Data data = path.data();
    ArrayList<PathNode> nodes = PathNodes.root(rt, data, false);
    // static vs dynamic access
    final IndexDb db = data != null ?
      new IndexStaticDb(data, info) :
      new IndexDynDb(root == null ? new ContextValue(info) : root, info);
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // only accept descendant steps without positional predicates
      // Example for position predicate: child::x[1] != parent::x[1]
      final Step step = path.axisStep(s);
      if(step == null || !step.axis.down || step.selector != null || step.mayBePositional()) break;
      if(nodes != null) nodes = step.nodes(nodes, false);

      // choose the cheapest index access
      final int el = step.exprs.length;
      for(int e = 0; e < el; e++) {
        final IndexInfo ii = new IndexInfo(db, cc, step, nodes);
        if(!step.exprs[e].indexAccessible(ii)) continue;

        if(ii.costs.results() == 0) {
          // no results...
          cc.info(QueryText.OPTNORESULTS_X, step);
          return Empty.VALUE;
        }

        if(index == null || index.costs.compareTo(ii.costs) > 0) {
          index = ii;
          predIndex = e;
          stepIndex = s;
        }
      }
    }

    // skip rewriting if no index access is possible or if it is too expensive
    if(index == null || data != null && index.costs.tooExpensive(data)) return path;
    // skip optimization if root is no value and if index access is not enforced
    Test rootTest = null;
    if(rt instanceof final Value value && !(value instanceof Dummy)) {
      rootTest = Test.inverted(value);
    } else {
      // continue if index use is enforced (skip context-dependent roots)
      if(!index.enforce() || rt.has(Flag.CTX)) return path;
    }

    // rewrite for index access
    cc.info(index.optInfo);

    // create new root expression
    final ExprList indexSteps = new ExprList();
    final Expr indexRoot;
    if(index.expr instanceof final Path ipath) {
      indexRoot = ipath.root;
      indexSteps.add(ipath.steps);
    } else {
      indexRoot = index.expr;
    }
    // only one hit: update sequence type (a name test may discard the indexed node)
    if(index.costs.results() == 1 && indexRoot instanceof final ParseExpr expr) {
      expr.exprType.assign(expr instanceof IndexAccess && index.test == null &&
        index.costs.size() == 1 ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE);
    }

    // invert steps that occur before index step, rewrite them to predicates
    final Expr indexStep = indexSteps.isEmpty() ? null : indexSteps.peek();
    final ExprList invSteps = new ExprList(), lastPreds = new ExprList();
    if(rootTest != NodeTest.DOCUMENT || data == null || !data.meta.complete ||
        invertSteps(path, data, stepIndex)) {
      for(int s = stepIndex; s >= 0; s--) {
        final Axis axis = path.axisStep(s).axis.invert();
        InputInfo ii;
        Axis newAxis;
        Test newTest;
        Expr[] newPreds;
        if(s == 0) {
          ii = info;
          newAxis = axis;
          if(rootTest != null) {
            // ...::document-node()
            newTest = rootTest;
            newPreds = new Expr[0];
          } else {
            // index use is enforced: ...::document-node()[db:node-id(.) = db:node-id(ROOT)]
            newTest = NodeTest.DOCUMENT;
            newPreds = new Expr[] { new CmpG(info, _DB_NODE_ID.get(info, new ContextValue(ii)),
              _DB_NODE_ID.get(info, rt), CmpOp.EQ) };
          }
        } else {
          final Step step = path.axisStep(s - 1);
          ii = step.info();
          newAxis = step.axis == ATTRIBUTE ? ATTRIBUTE : axis;
          newTest = step.test;
          newPreds = step.exprs;
        }
        // skip step if it is always successful
        if(newAxis != ANCESTOR && newAxis != ANCESTOR_OR_SELF ||
            newTest != NodeTest.XNODE && newTest != NodeTest.DOCUMENT ||
            newPreds.length > 0) {
          final Expr expr = invSteps.isEmpty() ?
            indexStep != null ? indexStep : indexRoot : invSteps.peek();
          invSteps.add(Step.get(cc, expr, ii, newAxis, newTest, newPreds));
        }
      }
    }
    // add created steps, followed by remaining predicates
    if(!invSteps.isEmpty()) {
      lastPreds.add(cc.get(indexStep != null ? indexStep : indexRoot, true,
        () -> Path.get(cc, info, null, invSteps.finish())));
    }
    lastPreds.add(Array.remove(index.step.exprs, predIndex));

    // attach predicates to last step or new self::node() step
    if(!lastPreds.isEmpty()) {
      indexSteps.add(indexStep instanceof Step
          ? ((Step) indexSteps.pop()).addPredicates(lastPreds.finish())
          : Step.self(cc, indexRoot, info, lastPreds.finish()));
    }

    // add remaining steps
    for(int s = stepIndex + 1; s < sl; s++) indexSteps.add(steps[s]);

    return indexSteps.isEmpty() ? indexRoot : Path.get(cc, info, indexRoot, indexSteps.finish());
  }

  /**
   * Checks if steps before index step need to be inverted and traversed.
   * @param path path expression
   * @param data data reference
   * @param i index step
   * @return result of check
   */
  private static boolean invertSteps(final Path path, final Data data, final int i) {
    for(int s = i; s >= 0; s--) {
      final Step step = path.axisStep(s);
      if(s != i) {
        // preceding steps: reject predicates, skip wildcards
        if(step.exprs.length > 0) return true;
        if(step.test instanceof NodeTest) continue;
      }
      // consider child steps with name test
      if(step.axis != CHILD || !(step.test instanceof final NameTest test)) return true;
      // only consider tests that address a single database name
      final byte[] name = test.dbName();
      if(name == null) return true;
      // only support unique paths with nodes on the correct level
      final ArrayList<PathNode> pn = data.paths().desc(name);
      if(pn.size() != 1 || pn.getFirst().level() != s + 1) return true;
    }
    return false;
  }
}
