package org.basex.data;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.index.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Base class with common functionality for all ID -> PRE mapping tests.
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public abstract class IdPreMapBulkTestBase {
  /** Number of update operations to execute in each test. */
  int opcount = 7000;
  /** Initial number of records. */
  int baseid = 400;
  /** ID -> PRE map to compare to. */
  private DummyIdPreMap basemap;
  /** ID -> PRE map to test. */
  private IdPreMap testedmap;
  /** Sequence of performed operations and parameters. */
  private ArrayList<int[]> ops;

  /** Set-up method. */
  @Before
  public void setUp() {
    final int ml = baseid + 1;
    final int[] map = new int[ml];
    for(int m = 0; m < ml; m++) map[m] = m;
    basemap = new DummyIdPreMap(map);
    testedmap = new IdPreMap(baseid);
    ops = new ArrayList<>(baseid + opcount);
  }

  /**
   * Insert a &lt;pre, id&gt; pair in {@link #basemap} and {@link #testedmap}.
   * @param pre pre value
   * @param id id value
   * @param c number of inserted records
   */
  final void insert(final int pre, final int id, final int c) {
    ops.add(new int[] { pre, id, c});
    testedmap.insert(pre, id, c);
    basemap.insert(pre, id, c);
  }

  /**
   * Delete a &lt;pre, id&gt; pair from {@link #basemap} and {@link #testedmap}.
   * @param pre pre value
   * @param c number of deleted records
   */
  final void delete(final int pre, final int c) {
    ops.add(new int[] { pre, basemap.id(pre), c});
    testedmap.delete(pre, basemap.id(pre), c);
    basemap.delete(pre, basemap.id(pre), c);
  }

  /** Check the two mappings. */
  final void check() {
    final int bs = basemap.size();
    for(int pre = 0; pre < bs; pre++) {
      final int id = basemap.id(pre);
      final int p = testedmap.pre(id);
      if(pre != p) {
        dump();
        fail("Wrong PRE for ID=" + id + ": expected " + pre + ", actual " + p);
      }
    }
  }

  /** Print inserted and deleted records and the tested map. */
  final void dump() {
    final StringBuilder s = new StringBuilder();
    for(final int[] o : ops) {
      s.append(o[2] > 0 ? "insert(" : "delete(");
      s.append(o[0]);
      s.append(',');
      s.append(o[1]);
      s.append(',');
      s.append(o[2]);
      s.append(");\n");
    }
    Util.errln("Operations:\n" + s);
    Util.errln(testedmap);
  }

  /**
   * Dummy implementation of ID -> PRE map: very slow, but simple and correct.
   * @author BaseX Team 2005-14, BSD License
   * @author Dimitar Popov
   */
  protected static class DummyIdPreMap extends IdPreMap {
    /** ID list. */
    private final ArrayList<Integer> idlist;

    /**
     * Constructor.
     * @param list initial list of ids.
     */
    public DummyIdPreMap(final int[] list) {
      super(list.length - 1);
      idlist = new ArrayList<>(list.length);
      for(final int l : list) idlist.add(l);
    }

    @Override
    public void insert(final int pre, final int id, final int c) {
      for(int i = 0; i < c; ++i) idlist.add(pre + i, id + i);
    }

    @Override
    public void delete(final int pre, final int id, final int c) {
      for(int i = 0; i < -c; ++i) idlist.remove(pre);
    }

    @Override
    public int pre(final int id) {
      return idlist.indexOf(id);
    }

    @Override
    public int size() {
      return idlist.size();
    }

    /**
     * ID of the record with a given PRE.
     * @param pre record PRE
     * @return record ID
     */
    public int id(final int pre) {
      return idlist.get(pre);
    }

    @Override
    public String toString() {
      final StringBuilder spres = new StringBuilder(), sids = new StringBuilder();
      final int is = idlist.size();
      for(int i = 0; i < is; ++i) {
        spres.append(i).append(' ');
        sids.append(idlist.get(i)).append(' ');
      }
      return spres.append('\n').append(sids).toString();
    }
  }
}
