package org.basex.io.random;

import java.io.*;
import java.util.concurrent.locks.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class provides a simple, clock-based buffer management.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class Buffers {

  /**
   * The Selector interface stores an index into a Buffers' instance buf array.
   * @author kgb
   */
  public interface Selector {
    /**
     * Sets the index.
     * @param index the new index
     */
    void setSelectedBufferIndex(int index);
    /**
     * Retrieves the previously stored index.
     * @return the previously stored index
     */
    int getSelectedBufferIndex();
  }

  /**
   * Number of buffers (must be 1 << n).
   * NOTE: the maximum number of concurrent query threads
   *   should be kept lower than BUFFERS.
   */
  private static final int BUFFERS = 1 << 5;  // 1 << 4 = 2^4 = 16

  /** Buffers. */
  private final Buffer[] buf = new Buffer[BUFFERS];

  /** File interacting with the buffer contents. */
  private final RandomAccessFile file;

  /** Provides locking of buffer access. */
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Constructor.
   * @param raFile the file we are buffering
   */
  public Buffers(final RandomAccessFile raFile) {
    file = raFile;
    for(int b = 0; b < BUFFERS; ++b) buf[b] = new Buffer();
  }

  /**
   * Returns the bufffer containing the file contents starting at
   * blockNumber * IO.BLOCKSIZE.
   * NOTE: always call freeBuffer() when doen with the acquired buffer.
   * Use try...finally to do so. No write or disk operations will be
   * possible while any thread holds a buffer.
   * @param selector a Selector instance keeping index of the buffer
   *    previously requested by the caller before calling. On
   *    completion of the call, the index of the buffer returned
   *    by this call is stored in this instance.
   * @param blockNumber the part of the file we are interested in,
   *    starting at file position blockNumber * IO.BLOCKSIZE
   * @param write must be set to true if we intend to change the buffer
   * @return the buffer containing data of the selected area
   */
  public Buffer acquireBuffer(final Selector selector, final long blockNumber,
      final boolean write) {

    final int o = selector.getSelectedBufferIndex();
    int off = o;

    if (!write) {
      // first, attempt to get a matching buffer in read-only mode
      lock.readLock().lock();
      do {
        if(buf[off].pos == blockNumber) {
          return buf[off];
        }
      } while((off = off + 1 & BUFFERS - 1) != o);
      lock.readLock().unlock();
    }

    // no matching buffer found - we need to enter write (exclusive) mode
    lock.writeLock().lock();
    if (!write) {
      lock.readLock().lock();
    }

    /*
     * We need to try again whether a matching buffer is available,
     * because we ned to strictly avoid mapping the same file area to
     * more than one buffer at the same time
     */
    do {
      if(buf[off].pos == blockNumber) {
        if (!write) {
          lock.writeLock().unlock();
        }
        return buf[off];
      }
    } while((off = off + 1 & BUFFERS - 1) != o);

    /*
     * Still no matching buffer found - we use the bufffer for the slot right
     * behind the one previously used by the caller, ensuring optimal buffer
     * usage single-threaded and good buffer usage in many other cases.
   */
    off = o + 1 & BUFFERS - 1;
    Buffer bf = buf[off];
    try {
      if(bf.dirty) writeBlock(bf);
      bf.pos = blockNumber;
      final long len = file.length();
      final long pos = blockNumber * IO.BLOCKSIZE;
      file.seek(pos);
      if(pos < len)
        file.readFully(bf.data, 0, (int) Math.min(len - pos, IO.BLOCKSIZE));
    } catch(final IOException ex) {
      Util.stack(ex);
    }

    selector.setSelectedBufferIndex(off);
    if (!write) {
      lock.writeLock().unlock();
    }
    return bf;

  }

  /**
   * Frees a buffer previously qcquired with acquireBuffer().
   */
  public void freeBuffer() {
    if (lock.isWriteLockedByCurrentThread()) {
      lock.writeLock().unlock();
    } else {
      lock.readLock().unlock();
    }
  }


  /**
   * Writes the given buffer to disk. NOTE: caller must hold lock.writeLock().
   * @param bf the buffer to flush to disk
   * @throws IOException if an IOException occurred during file operation
   * */
  private void writeBlock(final Buffer bf) throws IOException {
    file.seek(bf.pos * IO.BLOCKSIZE);
    file.write(bf.data);
    bf.dirty = false;
  }

  /**
   * Flushes all buffers to disk.
   * NOTE: this method must not be called while the calling thread holds a buffer
   *   acquired via acquireBuffer(). Otherwise, the method will block eternally.
   * @throws IOException if an IOException occurred during file operation
   */
  public void flush() throws IOException {
    lock.writeLock().lock();
    for (Buffer bf : buf) {
      if (bf.dirty) {
        writeBlock(bf);
      }
    }
    lock.writeLock().unlock();
  }
}
