package org.basex.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.basex.BaseX;
import org.basex.util.Array;

/**
 * This class stores the table on disk and reads it block-wise.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableDiskAccessNIO extends TableAccess {
  /** Max entries per block. */
  private static final int ENTRIES = IO.BLOCKSIZE >>> IO.NODEPOWER;
  /** Entries per new block. */
  private static final int NEWENTRIES = (int) (IO.BLOCKFILL * ENTRIES);

  /** Bytebuffer window list. */
  private ByteBufferList mbbuffer;

  /** Temp buffer for creating a new block. */
  private final ByteBuffer tmpblock;

  /** Read Write file channel. */
  private FileChannel fc;

  /** File storing all blocks. */
  private final RandomAccessFile file;
  /** Name of the database. */
  private final String db;
  /** Filename. */
  private final String fn;

  /** Index array storing the FirstPre values. this one is sorted ascending. */
  private int[] firstPres;
  /** Index array storing the BlockNumbers. */
  private int[] blocks;

  /** Number of the first entry in the current block. */
  private int firstPre = -1;
  /** FirstPre of the next block. */
  private int nextPre = -1;
  /** Number of the current block. */
  private int block = -1;

  /** Number of entries in the index (used blocks). */
  private int indexSize;
  /** Number of blocks in the data file (including unused). */
  private int nrBlocks;
  /** Index number of the current block, referencing indexBlockNumber array. */
  private int index = -1;
  /** Number of entries in the storage. */
  private int count;

  /** Whether the index is dirty. */
  private boolean indexdirty;

  /**
   * Constructor.
   * @param nm name of the database
   * @param f prefix for all files (no ending)
   * @throws IOException in case files cannot be read
   */
  public TableDiskAccessNIO(final String nm, final String f)
  throws IOException {
    db = nm;
    fn = f;

    // READ INFO FILE AND INDEX
    final DataInput in = new DataInput(nm, f + 'i');
    nrBlocks = in.readNum();
    indexSize = in.readNum();
    count = in.readNum();
    firstPres = in.readNums();
    blocks = in.readNums();
    in.close();

    // create a mappedbytebuffer list
    mbbuffer = new ByteBufferList(blocks.length);

    // INITIALIZE FILE
    file = new RandomAccessFile(IO.dbfile(nm, f), "rw");
    fc = file.getChannel();
    tmpblock = ByteBuffer.allocateDirect(IO.BLOCKSIZE);
    readBlock(0, 0, indexSize > 1 ? firstPres[1] : count);
  }

  /**
   * Searches for the block containing the entry for that pre. then it
   * reads the block and returns it's offset inside the block.
   * @param pre pre of the entry to search for
   * @return offset of the entry in currentBlock
   */
  private synchronized int cursor(final int pre) {
    int fp = firstPre;
    int np = nextPre;
    if(pre >= fp && pre < np) return (pre - fp) << IO.NODEPOWER;

    final int last = indexSize - 1;
    int low = 0;
    int high = last;
    int mid = index;
    while(low <= high) {
      if(pre < fp) high = mid - 1;
      else if(pre >= np) low = mid + 1;
      else break;
      mid = (high + low) >>> 1;
      fp = firstPres[mid];
      np = mid == last ? fp + ENTRIES : firstPres[mid + 1];
    }
    if(low > high) BaseX.notexpected("Invalid Data Access [pre:" + pre +
        ",indexSize:" + indexSize + ",access:" + low + ">" + high + "]");

    readBlock(mid, fp, np);
    return (pre - firstPre) << IO.NODEPOWER;
  }

  /**
   * Fetches the requested block into blockNumber and set firstPre and
   * blockSize.
   * @param ind index number of the block to fetch
   * @param first first entry in that block
   * @param next first entry in the next block
   */
  private synchronized void readBlock(final int ind, final int first,
      final int next) {

    try {
      final int b = blocks[ind];
      // check if block is already mapped
      if(mbbuffer.get(b) == null) {
        mbbuffer.set(fc.map(FileChannel.MapMode.READ_WRITE,
            b * IO.BLOCKSIZE, IO.BLOCKSIZE), b);
      }
      // else choose mapped block
      block = b;
      index = ind;
      firstPre = first;
      nextPre = next;
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Fetches next block.
   */
  private synchronized void nextBlock() {
    readBlock(index + 1, nextPre, index + 2 >= indexSize ? count :
      firstPres[index + 2]);
  }

  @Override
  public synchronized void flush() throws IOException {
    if(!indexdirty) return;
    final DataOutput out = new DataOutput(db, fn + 'i');
    out.writeNum(nrBlocks);
    out.writeNum(indexSize);
    out.writeNum(count);
    out.writeNums(firstPres);
    out.writeNums(blocks);
    out.close();
    indexdirty = false;
  }

  @Override
  public synchronized void close() throws IOException {
    flush();
    file.close();
  }

  @Override
  public synchronized int read1(final int pos, final int off) {
    final int o = off + cursor(pos);
    return mbbuffer.get(block).get(o) & 0xFF;
  }

  @Override
  public synchronized int read2(final int pos, final int off) {
    final int o = off + cursor(pos);
    return ((mbbuffer.get(block).get(o) & 0xFF) << 8)
    + (mbbuffer.get(block).get(o + 1) & 0xFF);
  }

  @Override
  public synchronized int read4(final int pos, final int off) {
    final int o = off + cursor(pos);
    return ((mbbuffer.get(block).get(o) & 0xFF) << 24)
        + ((mbbuffer.get(block).get(o + 1) & 0xFF) << 16)
        + ((mbbuffer.get(block).get(o + 2) & 0xFF) << 8)
        + (mbbuffer.get(block).get(o + 3) & 0xFF);
  }

  @Override
  public synchronized long read5(final int pos, final int off) {
    final int o = off + cursor(pos);
    return ((long) (mbbuffer.get(block).get(o) & 0xFF) << 32)
        + ((long) (mbbuffer.get(block).get(o + 1) & 0xFF) << 24)
        + ((mbbuffer.get(block).get(o + 2) & 0xFF) << 16)
        + ((mbbuffer.get(block).get(o + 3) & 0xFF) << 8)
        + (mbbuffer.get(block).get(o + 4) & 0xFF);
  }

  @Override
  public synchronized void write1(final int pos, final int off, final int v) {
    final int o = off + cursor(pos);
    mbbuffer.get(block).put(o, (byte) v);
  }

  @Override
  public synchronized void write2(final int pos, final int off, final int v) {
    final int o = off + cursor(pos);
    mbbuffer.get(block).put(o, (byte) (v >>> 8));
    mbbuffer.get(block).put(o + 1, (byte) v);
  }

  @Override
  public synchronized void write4(final int pos, final int off, final int v) {
    final int o = off + cursor(pos);
    mbbuffer.get(block).put(o, (byte) (v >>> 24));
    mbbuffer.get(block).put(o + 1, (byte) (v >>> 16));
    mbbuffer.get(block).put(o + 2, (byte) (v >>> 8));
    mbbuffer.get(block).put(o + 3, (byte) v);
  }

  @Override
  public synchronized void write5(final int pos, final int off, final long v) {
    final int o = off + cursor(pos);
    mbbuffer.get(block).put(o, (byte) (v >>> 32));
    mbbuffer.get(block).put(o + 1, (byte) (v >>> 24));
    mbbuffer.get(block).put(o + 2, (byte) (v >>> 16));
    mbbuffer.get(block).put(o + 3, (byte) (v >>> 8));
    mbbuffer.get(block).put(o + 4, (byte) v);
  }

  /* Note to delete method: Freed blocks are currently ignored. */

  @Override
  public synchronized void delete(final int first, final int nr) {
    // mark index as dirty and get first block
    indexdirty = true;
    cursor(first);

    // some useful variables to make code more readable
    int from = first - firstPre;
    final int last = first + nr - 1;

    // check if all entries are in current block => handle and return
    if(last < nextPre) {
      copy(mbbuffer.get(block), from + nr, mbbuffer.get(block),
          from, nextPre - last - 1);

      updatePre(nr);

      // if whole block was deleted, remove it from the index
      if(nextPre == firstPre) {
        Array.move(firstPres, index + 1, -1, indexSize - index - 1);
        Array.move(blocks, index + 1, -1, indexSize - index - 1);
        indexSize--;
        readBlock(index, firstPre, index + 2 > indexSize ? count :
          firstPres[index + 1]);
      }
      return;
    }

    // handle blocks whose entries are to be deleted entirely

    // first count them
    int unused = 0;
    while(nextPre <= last) {
      if(from == 0) unused++;
      nextBlock();
      from = 0;
    }
    // now remove them from the index
    if(unused > 0) {
      Array.move(firstPres, index, -unused, indexSize - index);
      Array.move(blocks, index, -unused, indexSize - index);
      indexSize -= unused;
      index -= unused;
    }

    // delete entries at beginning of current (last) block
    copy(mbbuffer.get(block), last - firstPre + 1, mbbuffer.get(block),
        0, nextPre - last - 1);

    // update index entry for this block
    firstPres[index] = first;
    firstPre = first;
    updatePre(nr);
  }

  /**
   * Updates the firstPre index entries.
   * @param nr number of entries to move
   */
  private synchronized void updatePre(final int nr) {
    // update index entries for all following blocks and reduce counter
    for(int i = index + 1; i < indexSize; i++) firstPres[i] -= nr;
    count -= nr;
    nextPre = index + 1 >= indexSize ? count : firstPres[index + 1];
  }

  @Override
  public synchronized void insert(final int pre, final byte[] entries) {
    indexdirty = true;
    final int nr = entries.length >>> IO.NODEPOWER;
    count += nr;
    cursor(pre);

    final int ins = pre - firstPre + 1;

    // all entries fit in current block
    if(nr < ENTRIES - nextPre + firstPre) {
      // shift following entries forward and insert next entries
      copy(mbbuffer.get(block), ins, mbbuffer.get(block),
          ins + nr, nextPre - pre);
      copy(entries, 0, mbbuffer.get(block), ins, nr);

      // update index entries
      for(int i = index + 1; i < indexSize; i++) firstPres[i] += nr;
      nextPre += nr;
      return;
    }

    // we need to reorganize entries

    // save entries to be added into new block after inserted blocks
    final int move = nextPre - pre - 1;
    final byte[] rest = new byte[move << IO.NODEPOWER];

    copy(mbbuffer.get(block), ins, rest, 0, move);

    // make room in index for new blocks
//    int newBlocks = (int) Math.ceil((double) nr / NEWENTRIES) + 1;
    int newBlocks = (int) ((double) (nr + NEWENTRIES - 1) / NEWENTRIES) + 1;
    // in case we insert at block boundary
    if(pre == nextPre - 1) newBlocks--;

    // resize the index
    firstPres = Array.resize(firstPres, nrBlocks, nrBlocks + newBlocks);
    blocks = Array.resize(blocks, nrBlocks, nrBlocks + newBlocks);

    Array.move(firstPres, index + 1, newBlocks, indexSize - index - 1);
    Array.move(blocks, index + 1, newBlocks, indexSize - index - 1);

    // add blocks for new entries
    int remain = nr;
    int pos = 0;
    while(remain > 0) {
      newBlock();
      copy(entries, pos, mbbuffer.get(block), 0, Math.min(remain, NEWENTRIES));

      firstPres[++index] = nr - remain + pre + 1;
      blocks[index] = block;
      indexSize++;
      remain -= NEWENTRIES;
      pos += NEWENTRIES;
    }

    // add remaining part of split block
    if(rest.length > 0) {
      newBlock();
      copy(rest, 0, mbbuffer.get(block), 0, move);

      firstPres[++index] = pre + nr + 1;
      blocks[index] = block;
      indexSize++;
    }

    // update index entries
    for(int i = index + 1; i < indexSize; i++) firstPres[i] += nr;

    // update cached variables
    firstPre = pre + 1;
    if(rest.length > 0) firstPre += nr;
    nextPre = index + 1 >= indexSize ? count : firstPres[index + 1];
  }

  /**
   * Creates a new, empty block.
   */
  private synchronized void newBlock() {
    //append a new block
    try {
      fc.position(fc.size());
      fc.write(tmpblock);
    } catch(final IOException e) {
      e.printStackTrace();
    }
    block = nrBlocks++;
    try {
      mbbuffer.add(fc.map(FileChannel.MapMode.READ_WRITE,
          block * IO.BLOCKSIZE, IO.BLOCKSIZE));
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Convenience method for copying blocks.
   * @param s source buffer
   * @param sp source position
   * @param d destination buffer
   * @param dp destination position
   * @param l source length
   */
  private synchronized void copy(final ByteBuffer s, final int sp,
      final ByteBuffer d, final int dp, final int l) {
    byte[] tmp = new byte[l << IO.NODEPOWER];
    s.position(sp << IO.NODEPOWER);
    s.get(tmp);
    d.position(dp << IO.NODEPOWER);
    s.put(tmp);
//    System.arraycopy(s, sp << IO.NODEPOWER, d, dp << IO.NODEPOWER,
//        l << IO.NODEPOWER);
  }

  /**
   * Convenience method for copying blocks.
   * @param s source array
   * @param sp source position
   * @param d destination buffer
   * @param dp destination position
   * @param l source length
   */
  private synchronized void copy(final byte[] s, final int sp,
      final ByteBuffer d, final int dp, final int l) {
    byte[] tmp = new byte[l << IO.NODEPOWER];
    System.arraycopy(s, sp << IO.NODEPOWER, tmp, 0,
        l << IO.NODEPOWER);
    d.position(dp << IO.NODEPOWER);
    d.put(tmp, dp << IO.NODEPOWER, l << IO.NODEPOWER);
//    System.arraycopy(s, sp << IO.NODEPOWER, d, dp << IO.NODEPOWER,
//        l << IO.NODEPOWER);
  }

  /**
   * Convenience method for copying blocks.
   * @param s source buffer
   * @param sp source position
   * @param d destination array
   * @param dp destination position
   * @param l source length
   */
  private synchronized void copy(final ByteBuffer s, final int sp,
      final byte[] d, final int dp, final int l) {
    byte[] tmp = new byte[l << IO.NODEPOWER];
    s.position(sp << IO.NODEPOWER);
    s.get(tmp);
    System.arraycopy(tmp, 0, d, dp << IO.NODEPOWER,
        l << IO.NODEPOWER);
  }

  /**
   * Return the entryCount; needed for JUnit tests.
   * @return number of entries in storage.
   */
  public synchronized int size() {
    return count;
  }

  /**
   * Return the number of used blocks; needed for JUnit tests.
   * @return number of used blocks.
   */
  public synchronized int blocks() {
    return indexSize;
  }
}
