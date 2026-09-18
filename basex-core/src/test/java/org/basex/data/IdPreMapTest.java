package org.basex.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.index.*;
import org.basex.io.*;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * ID -> PRE mapping test.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
public final class IdPreMapTest {
  /** Number of update operations to execute in each test. */
  private static final int ITERATIONS = 200;
  /** Initial number of records. */
  private static final int BASEID = 2000;
  /** Random number generator. */
  private static final Random RANDOM = new Random();
  /** ID-PRE map to compare to. */
  private DummyIdPreMap basemap;
  /** ID-PRE map to test. */
  private IdPreMap testedmap;
  /** Sequence of inserted PRE values. */
  private IntList insertedpres;
  /** Sequence of deleted PRE values. */
  private IntList deletedpres;

  /** Set-up method. */
  @BeforeEach public void setUp() {
    final int ml = BASEID + 1;
    final int[] map = new int[ml];
    for(int m = 0; m < ml; m++) map[m] = m;
    basemap = new DummyIdPreMap(map);
    testedmap = new IdPreMap(BASEID);
    insertedpres = new IntList(ITERATIONS);
    deletedpres = new IntList(ITERATIONS);
  }

  /** Insert correctness: insert values at the end. */
  @Test public void appendCorrectness() {
    final int n = BASEID + ITERATIONS;
    for(int id = BASEID + 1; id <= n; id++) {
      insert(id, id);
      check();
    }
  }

  /** Insert correctness: insert values at the end. */
  @Test public void deleteFromEndCorrectness() {
    for(int id = BASEID; id >= 0; --id) {
      delete(id);
      check();
    }
  }

  /** Insert correctness: insert values at random positions. */
  @Test public void insertCorrectness() {
    final int n = BASEID + ITERATIONS;
    for(int id = BASEID + 1; id <= n; id++) {
      insert(RANDOM.nextInt(id), id);
      check();
    }
  }

  /** Delete correctness: delete values at random positions. */
  @Test public void deleteCorrectness() {
    for(int id = BASEID + 1; id > 0; --id) {
      delete(RANDOM.nextInt(id));
      check();
    }
  }

  /** Delete correctness: delete values at random positions. */
  @Test public void deleteCorrectness2() {
    final int n = BASEID + ITERATIONS;
    for(int id = BASEID + 1; id <= n; id++) insert(RANDOM.nextInt(id), id);

    for(int id = n; id > 0; --id) {
      delete(RANDOM.nextInt(id));
      check();
    }
  }

  /** Correctness: randomly insert/delete value at random positions. */
  @Test public void insertDeleteCorrectness() {
    for(int i = 0, cnt = BASEID + 1, id = BASEID + 1; i < ITERATIONS; i++) {
      // can't delete if all records have been deleted:
      if(RANDOM.nextBoolean() || cnt == 0) insert(RANDOM.nextInt(++cnt), id++);
      else delete(RANDOM.nextInt(cnt--));
      check();
    }
  }

  /** Deleted base IDs. */
  @Test public void deletedBaseIds() {
    final IdPreMap map = new IdPreMap(10);
    // delete in the middle
    map.markDeleted(5, 5);
    map.delete(5, 5, -1);
    assertEquals(-1, map.pre(5));
    assertEquals(4, map.pre(4));
    assertEquals(5, map.pre(6));
    // delete at the end
    map.markDeleted(10, 10);
    map.delete(9, 10, -1);
    assertEquals(-1, map.pre(10));
    assertEquals(8, map.pre(9));
    // inserted IDs are never marked
    map.insert(4, 11, 1);
    map.markDeleted(11, 11);
    assertEquals(4, map.pre(11));
  }

  /** Deleted base IDs: merging of ranges. */
  @Test public void deletedRanges() {
    final IdPreMap map = new IdPreMap(20);
    map.markDeleted(3, 4);
    map.markDeleted(8, 9);
    map.markDeleted(14, 15);
    map.markDeleted(2, 2);
    map.markDeleted(10, 12);
    map.markDeleted(5, 7);
    assertTrue(map.toString().contains("[2, 12, 14, 15]"), map.toString());
    for(int id = 0; id <= 20; id++) {
      final boolean del = id >= 2 && id <= 12 || id == 14 || id == 15;
      assertEquals(del ? -1 : id, map.pre(id), "ID " + id);
    }
  }

  /** Deleted base IDs: last record, no other updates. */
  @Test public void deletedLastBaseId() {
    final IdPreMap map = new IdPreMap(10);
    map.markDeleted(10, 10);
    map.delete(10, 10, -1);
    assertEquals(-1, map.pre(10));
    assertEquals(9, map.pre(9));
  }

  /**
   * Deleted base IDs: persistence, files without trailing block.
   * @throws IOException I/O exception
   */
  @Test public void deletedBaseIdsIO() throws IOException {
    final IOFile file = new IOFile(Prop.TEMPDIR, "IdPreMapTest.idp");
    try {
      final IdPreMap map = new IdPreMap(10);
      map.markDeleted(5, 5);
      map.delete(5, 5, -1);
      map.write(file);
      assertEquals(-1, new IdPreMap(file).pre(5));
      assertEquals(5, new IdPreMap(file).pre(6));

      // file written by an older version
      try(DataOutput out = new DataOutput(file)) {
        out.writeNum(10);
        out.writeNum(0);
        for(int i = 0; i < 5; i++) out.writeNums(new int[0]);
      }
      assertEquals(5, new IdPreMap(file).pre(5));
    } finally {
      file.delete();
    }
  }

  /**
   * Replaying the log of random operations yields the same map as a complete write.
   * @throws IOException I/O exception
   */
  @Test public void replay() throws IOException {
    final IOFile file = new IOFile(Prop.TEMPDIR, "IdPreMapTest.idp");
    final IOFile log = new IOFile(Prop.TEMPDIR, "IdPreMapTest.idpl");
    try {
      final IntList ids = new IntList();
      for(int id = 0; id <= BASEID; id++) ids.add(id);
      int lastid = BASEID;
      final IdPreMap map = new IdPreMap(BASEID);
      long length = map.write(file, log, 0, false);
      int complete = 0, appended = 0;
      for(int i = 0; i < ITERATIONS * 5; i++) {
        final int ops = RANDOM.nextInt(5) + 1;
        for(int o = 0; o < ops; o++) {
          final int pre = RANDOM.nextInt(ids.size() + 1);
          if(RANDOM.nextBoolean() || pre == ids.size()) {
            final int c = RANDOM.nextInt(10) + 1;
            for(int n = 0; n < c; n++) ids.insert(pre + n, lastid + 1 + n);
            map.insert(pre, lastid + 1, c);
            lastid += c;
          } else {
            final int id = ids.get(pre), baseid = map.baseid();
            int c = Math.min(RANDOM.nextInt(10) + 1, ids.size() - pre);
            // base IDs ascend with PRE values, inserted nodes have no base descendants
            if(id > baseid) {
              int n = 1;
              while(n < c && ids.get(pre + n) > baseid) n++;
              c = n;
            } else {
              int last = pre + c - 1;
              while(ids.get(last) > baseid) last--;
              map.markDeleted(id, ids.get(last));
            }
            map.delete(pre, id, -c);
            for(int n = 0; n < c; n++) ids.remove(pre);
          }
        }
        length = map.write(file, log, length, false);
        if(length == 0) complete++;
        else appended++;

        final IdPreMap read = new IdPreMap(file);
        if(length != 0) read.replay(log, length);
        final int[] pres = new int[lastid + 1];
        Arrays.fill(pres, -1);
        final int is = ids.size();
        for(int p = 0; p < is; p++) pres[ids.get(p)] = p;
        for(int id = 0; id <= lastid; id++) {
          assertEquals(pres[id], map.pre(id), "ID " + id);
          assertEquals(pres[id], read.pre(id), "ID " + id);
        }
      }
      assertTrue(complete > 1, "Complete writes: " + complete);
      assertTrue(appended > complete, "Appended: " + appended + ", complete: " + complete);

      // complete write
      assertEquals(0, map.write(file, log, length, true));
    } finally {
      file.delete();
      log.delete();
    }
  }

  /** Insert performance: insert at random positions. */
  @Test public void insertPerformance() {
    insertPerformance(testedmap);
  }

  /** Delete performance: delete at random positions. */
  @Test public void deletePerformance() {
    deletePerformance(testedmap, basemap);
  }

  /** Search performance: insert at random positions and the search. */
  @Test public void searchPerformance() {
    searchPerformance(testedmap);
  }

  /** Dummy insert performance: insert at random positions. */
  @Test public void insertPerformanceDummy() {
    insertPerformance(basemap);
  }

  /** Dummy delete performance: delete at random positions. */
  @Test public void deletePerformanceDummy() {
    deletePerformance(basemap, basemap.copy());
  }

  /** Dummy search performance: insert at random positions and the search. */
  @Test public void searchPerformanceDummy() {
    searchPerformance(basemap);
  }

  /**
   * Insert performance: insert at random positions.
   * @param m tested map
   */
  private static void insertPerformance(final IdPreMap m) {
    // prepare <PRE/ID> pairs:
    final int[][] d = new int[ITERATIONS][2];
    for(int i = 0, id = BASEID + 1; i < ITERATIONS; id++, i++) {
      d[i][0] = RANDOM.nextInt(id);
      d[i][1] = id;
    }
    // perform the actual test:
    for(final int[] a : d) m.insert(a[0], a[1], 1);
  }

  /**
   * Delete performance: delete at random positions.
   * @param m tested map
   * @param b base map
   */
  private static void deletePerformance(final IdPreMap m, final DummyIdPreMap b) {
    // prepare <PRE/ID> pairs:
    final int dl = BASEID + 1;
    final int[][] d = new int[dl][2];
    for(int i = 0, id = BASEID + 1; i < dl; id--, i++) {
      d[i][0] = RANDOM.nextInt(id);
      d[i][1] = b.id(d[i][0]);
      b.delete(d[i][0], d[i][1], -1);
    }
    // perform the test:
    for(final int[] dd : d) m.delete(dd[0], dd[1], -1);
  }

  /**
   * Search performance: insert at random positions and then search.
   * @param m tested map
   */
  private static void searchPerformance(final IdPreMap m) {
    final int n = BASEID + ITERATIONS;
    for(int id = BASEID + 1; id <= n; id++) m.insert(RANDOM.nextInt(id), id, 1);
    for(int i = 0; i < n; i++) m.pre(i);
  }

  /**
   * Insert a &lt;pre, id&gt; pair in {@link #basemap} and {@link #testedmap}.
   * @param pre PRE value
   * @param id ID value
   */
  private void insert(final int pre, final int id) {
    insertedpres.add(pre);
    testedmap.insert(pre, id, 1);
    basemap.insert(pre, id, 1);
  }

  /**
   * Delete a &lt;pre, id&gt; pair from {@link #basemap} and {@link #testedmap}.
   * @param pre PRE value
   */
  private void delete(final int pre) {
    deletedpres.add(pre);
    testedmap.delete(pre, basemap.id(pre), -1);
    basemap.delete(pre, basemap.id(pre), -1);
  }

  /** Check the two mappings. */
  private void check() {
    final int bs = basemap.size();
    for(int pre = 0; pre < bs; pre++) {
      final int id = basemap.id(pre);
      final int p = testedmap.pre(id);
      if(pre != p) fail("Wrong PRE for ID = " + id + ": expected " + pre
          + ", actual " + p + "\nInserted PREs: " + insertedpres
          + "\nDelete PREs: " + deletedpres);
    }
  }

  /**
   * Dummy implementation of ID -> PRE map: very slow, but simple and correct.
   * @author Dimitar Popov
   */
  private static final class DummyIdPreMap extends IdPreMap {
    /** ID list. */
    private final ArrayList<Integer> ids;

    /**
     * Constructor.
     * @param i initial list of IDs
     */
    DummyIdPreMap(final int[] i) {
      super(i.length - 1);
      ids = new ArrayList<>(i.length);
      for(final int id : i) ids.add(id);
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

    @Override
    public int size() {
      return ids.size();
    }

    /**
     * ID of the record with a given PRE.
     * @param pre record PRE
     * @return record ID
     */
    int id(final int pre) {
      return ids.get(pre);
    }

    /**
     * Create a copy of the current object.
     * @return deep copy of the object
     */
    DummyIdPreMap copy() {
      final int[] a = new int[ids.size()];
      for(int i = size() - 1; i >= 0; --i) a[i] = ids.get(i);
      return new DummyIdPreMap(a);
    }
  }
}
