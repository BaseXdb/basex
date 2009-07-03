package org.basex.index;

import org.basex.util.Array;
import org.basex.util.Num;
import org.basex.util.Token;

/**
 * This class indexes tokens in a compressed tree structure.
 * The iterator returns all tokens in a sorted manner.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class ValueTree {
  /** Left nodes. */
  private int[] z = new int[4];
  /** Tokens. */
  private byte[][] t = new byte[1][];
  /** IDs. */
  byte[][] pre = new byte[1][];
  /** Number of pre values. */
  int[] ns = new int[1];
  /** Number of entries. */
  int size;

  /**
   * Indexes the specified token.
   * @param k token
   * @param pr id
   */
  void index(final byte[] k, final int pr) {
    if(size == 0) {
      ++size;
      add(k, pr);
    } else {
      int n = 2;
      while(true) {
        int c = n >>> 1;
        final int d = Token.diff(k, t[c]);
        if(d == 0) {
          pre[c] = Num.add(pre[c], pr);
          ns[c]++;
          return;
        }
        c = d > 0 ? z[n + 1] : z[n];
        if(c == 0) {
          ++size;
          c = size << 1;
          if(c == z.length) z = Array.extend(z);
          add(k, pr);
          if(d > 0) z[n + 1] = c;
          else z[n] = c;
          return;
        }
        n = c;
      }
    }
  }

  /**
   * Adds the specified token and id.
   * @param tok token to be added
   * @param id id to be added
   */
  private void add(final byte[] tok, final int id) {
    if(size == pre.length) {
      pre = Array.extend(pre);
      ns = Array.extend(ns);
      t = Array.extend(t);
    }
    t[size] = tok;
    pre[size] = Num.newNum(id);
    ns[size] = 1;
  }

  /** Integer list. */
  private int[] stack = new int[1];
  /** Current iterator. */
  private int spos;

  /**
   * Initializes the tree iterator. Note that the index structure will be
   * destroyed here to save memory.
   */
  void init() {
    t = null;
    if(size == 0) return;
    int n = 2;
    while(n != 0) {
      push(n);
      n = z[n];
    }
  }

  /**
   * Checks if more nodes can be returned.
   * @return result of check
   */
  boolean more() {
    return spos != 0;
  }

  /**
   * Returns the next pointer.
   * @return pointer
   */
  int next() {
    final int l = stack[spos - 1];
    if(z[l + 1] != 0) {
      int n = z[l + 1];
      push(n);
      while(z[n] != 0) {
        n = z[n];
        push(n);
      }
    } else {
      int c = l;
      int n = ppeek();
      while(n != 0 && c == z[n + 1]) {
        c = n;
        n = ppeek();
      }
    }
    return l >>> 1;
  }

  /**
   * Pushes the specified value to the stack.
   * @param n value to be pushed
   */
  private void push(final int n) {
    if(spos == stack.length) stack = Array.extend(stack);
    stack[spos++] = n;
  }

  /**
   * Pops and peeks a value from the stack.
   * @return resulting value
   */
  private int ppeek() {
    --spos;
    return spos == 0 ? 0 : stack[spos - 1];
  }
}
