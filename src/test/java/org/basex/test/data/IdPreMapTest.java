package org.basex.test.data;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.basex.index.IdPreMap;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.basex.util.list.IntList;
import org.junit.Before;
import org.junit.Test;

/**
 * ID -> PRE mapping test.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class IdPreMapTest {
  /** Verbose flag. */
  private static final boolean VERBOSE = false;
  /** Number of update operations to execute in each test. */
  private static final int ITERATIONS = 200;
  /** Initial number of records. */
  private static final int BASEID = 2000;
  /** Random number generator. */
  private static final Random RANDOM = new Random();
  /** ID -> PRE map to compare to. */
  private DummyIdPreMap basemap;
  /** ID -> PRE map to test. */
  private IdPreMap testedmap;
  /** Sequence of inserted PRE values. */
  private IntList insertedpres;
  /** Sequence of deleted PRE values. */
  private IntList deletedpres;

  /** Set-up method. */
  @Before
  public void setUp() {
    final int[] map = new int[BASEID + 1];
    for(int i = 0; i < map.length; ++i) map[i] = i;
    basemap = new DummyIdPreMap(map);
    testedmap = new IdPreMap(BASEID);
    insertedpres = new IntList(ITERATIONS);
    deletedpres = new IntList(ITERATIONS);
  }

  /** Insert correctness: insert values at random positions. */
  @Test
  public void testInsertCorrectness() {
    final int n = BASEID + ITERATIONS;
    for(int id = BASEID + 1; id <= n; ++id) {
      insert(RANDOM.nextInt(id), id);
      check();
    }
  }

  /** Delete correctness: delete values at random positions. */
  @Test
  public void testDeleteCorrectness() {
    for(int id = BASEID + 1; id > 0; --id) {
      delete(RANDOM.nextInt(id));
      check();
    }
  }

  /** Delete correctness: delete values at random positions. */
  @Test
  public void testDeleteCorrectness2() {
    final int n = BASEID + ITERATIONS;
    for(int id = BASEID + 1; id <= n; ++id) insert(RANDOM.nextInt(id), id);

    for(int id = n; id > 0; --id) {
      delete(RANDOM.nextInt(id));
      check();
    }
  }

  /** Correctness: randomly insert/delete value at random positions. */
  @Test
  public void testInsertDeleteCorrectness() {
    for(int i = 0, cnt = BASEID + 1, id = BASEID + 1; i < ITERATIONS; ++i) {
      // can't delete if all records have been deleted:
      if(RANDOM.nextBoolean() || id == 0) insert(RANDOM.nextInt(cnt++), id++);
      else delete(RANDOM.nextInt(cnt--));
      check();
    }
  }

  /** Insert performance: insert at random positions. */
  @Test
  public void testInsertPerformance() {
    if(VERBOSE) Util.err("Tested mapping: ");
    testInsertPerformance(testedmap);
  }

  /** Delete performance: delete at random positions. */
  @Test
  public void testDeletePerformance() {
    if(VERBOSE) Util.err("Tested mapping: ");
    testDeletePerformance(testedmap, basemap);
  }

  /** Search performance: insert at random positions and the search. */
  @Test
  public void testSearchPerformance() {
    if(VERBOSE) Util.err("Tested mapping: ");
    testSearchPerformance(testedmap);
  }

  /** Dummy insert performance: insert at random positions. */
  @Test
  public void testInsertPerformanceDummy() {
    if(VERBOSE) Util.err("Dummy mapping: ");
    testInsertPerformance(basemap);
  }

  /** Dummy delete performance: delete at random positions. */
  @Test
  public void testDeletePerformanceDummy() {
    if(VERBOSE) Util.err("Dummy mapping: ");
    testDeletePerformance(basemap, basemap.copy());
  }

  /** Dummy search performance: insert at random positions and the search. */
  @Test
  public void testSearchPerformanceDummy() {
    if(VERBOSE) Util.err("Dummy mapping: ");
    testSearchPerformance(basemap);
  }

  /**
   * Insert performance: insert at random positions.
   * @param m tested map
   */
  private static void testInsertPerformance(final IdPreMap m) {
    // prepare <pre, id> pairs:
    final int[][] d = new int[ITERATIONS][2];
    for(int i = 0, id = BASEID + 1; i < d.length; ++id, ++i) {
      d[i][0] = RANDOM.nextInt(id);
      d[i][1] = id;
    }
    // perform the actual test:
    final Performance p = new Performance();
    for(int i = 0; i < d.length; ++i) m.insert(d[i][0], d[i][1], 1);
    if(VERBOSE) Util.errln(d.length + " records inserted in: " + p);
  }

  /**
   * Delete performance: delete at random positions.
   * @param m tested map
   * @param b base map
   */
  private static void testDeletePerformance(final IdPreMap m,
      final DummyIdPreMap b) {
    // prepare <pre, id> pairs:
    final int[][] d = new int[BASEID + 1][2];
    for(int i = 0, id = BASEID + 1; i < d.length; --id, ++i) {
      d[i][0] = RANDOM.nextInt(id);
      d[i][1] = b.id(d[i][0]);
      b.delete(d[i][0], d[i][1], -1);
    }
    // perform the test:
    final Performance p = new Performance();
    for(final int[] dd : d) m.delete(dd[0], dd[1], -1);
    if(VERBOSE) Util.errln(d.length + " records deleted in: " + p);
  }

  /**
   * Search performance: insert at random positions and then search.
   * @param m tested map
   */
  private static void testSearchPerformance(final IdPreMap m) {
    final int n = BASEID + ITERATIONS;
    for(int id = BASEID + 1; id <= n; ++id) m.insert(RANDOM.nextInt(id), id, 1);

    final Performance p = new Performance();
    for(int i = 0; i < n; ++i) m.pre(i);
    if(VERBOSE) Util.errln(n + " records found in: " + p);
  }

  /**
   * Insert a &lt;pre, id&gt; pair in {@link #basemap} and {@link #testedmap}.
   * @param pre pre value
   * @param id id value
   */
  private void insert(final int pre, final int id) {
    insertedpres.add(pre);
    //if(VERBOSE) Util.errln("insert(" + pre + ", " + id + ")");
    testedmap.insert(pre, id, 1);
    //if(VERBOSE) Util.errln(testedmap);
    basemap.insert(pre, id, 1);
  }

  /**
   * Delete a &lt;pre, id&gt; pair from {@link #basemap} and {@link #testedmap}.
   * @param pre pre value
   */
  private void delete(final int pre) {
    deletedpres.add(pre);
    //if(VERBOSE) Util.errln("delete(" + pre + ", " + basemap.id(pre) + ")");
    testedmap.delete(pre, basemap.id(pre), -1);
    //if(VERBOSE) Util.errln(testedmap);
    basemap.delete(pre, basemap.id(pre), -1);
  }

  /** Check the two mappings. */
  private void check() {
    for(int pre = 0; pre < basemap.size(); pre++) {
      final int id = basemap.id(pre);
      final int p = testedmap.pre(id);
      if(pre != p) fail("Wrong PRE for ID = " + id + ": expected " + pre
          + ", actual " + p + "\nInserted PREs: " + insertedpres
          + "\nDelete PREs: " + deletedpres);
    }
  }
}

/**
 * Dummy implementation of ID -> PRE map: very slow, but simple and correct.
 * @author Dimitar Popov
 */
class DummyIdPreMap extends IdPreMap {
  /** ID list. */
  private final ArrayList<Integer> ids;

  /**
   * Constructor.
   * @param i initial list of ids.
   */
  public DummyIdPreMap(final int[] i) {
    super(i.length - 1);
    ids = new ArrayList<Integer>(i.length);
    for(final int element : i)
      ids.add(element);
  }

  @Override
  public void insert(final int pre, final int id, final int c) {
    ids.add(pre, id);
  }

  @Override
  public void delete(final int pre, final int id, final int c) {
    ids.remove(pre);
  }

  @Override
  public int pre(final int id) {
    return ids.indexOf(id);
  }

  /**
   * Size of the map.
   * @return number of stored records
   */
  @Override
  public int size() {
    return ids.size();
  }

  /**
   * ID of the record with a given PRE.
   * @param pre record PRE
   * @return record ID
   */
  public int id(final int pre) {
    return ids.get(pre);
  }

  /**
   * Create a copy of the current object.
   * @return deep copy of the object
   */
  public DummyIdPreMap copy() {
    final int[] a = new int[ids.size()];
    for(int i = size() - 1; i >= 0; --i) a[i] = ids.get(i).intValue();
    return new DummyIdPreMap(a);
  }
}
