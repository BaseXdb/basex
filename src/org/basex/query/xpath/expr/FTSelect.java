package org.basex.query.xpath.expr;

import java.util.Arrays;

import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.query.QueryException;
import org.basex.query.FTPos.FTUnit;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.internal.FTIndex;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Fulltext position filter expression and FTTimes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTSelect extends FTArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   * @param ftps FTPositionFilter
   */
  public FTSelect(final FTArrayExpr e, final FTPositionFilter ftps) {
    exprs = new FTArrayExpr[] { e };
    ftpos = ftps;
  }

  @Override
  public Item eval(final XPContext ctx) throws QueryException {
    final FTPositionFilter tmp = ctx.ftpos;
    ctx.ftpos = ftpos;
    ftpos.pos.init(ctx.ftitem);
    final Item i = exprs[0].eval(ctx);
    ctx.ftpos = tmp;

    // <SG> ..an own FTIndexSelect could be created for the index version...
    if(i instanceof NodeSet) return indexEval(ctx, (NodeSet) i);

    // sequential traversal..
    return Bool.get(i.bool() && seqEval());
  }

  /**
   * Evaluates the position filters.
   * @return result of check
   */
  private boolean seqEval() {
    if(!ftpos.pos.valid()) return false;

    // ...distance?
    if(ftpos.pos.dunit != null) {
      if(!ftpos.pos.distance(ftpos.dist[0], ftpos.dist[1])) return false;
    }
    // ...window?
    if(ftpos.pos.wunit != null) {
      final long c = (long) ftpos.window.num();
      if(!ftpos.pos.window(c)) return false;
    }
    return true;
  }

  /**
   * Evaluates the index result.
   * @param ctx query context
   * @param n node set
   * @return result
   */
  private NodeSet indexEval(final XPContext ctx, final NodeSet n) {
    int[][] res = n.ftidpos;

    if(ftpos.pos.ordered)
      res = calculateFTOrdered(n.ftidpos, n.ftpointer);
    if(ftpos.pos.wunit != null)
      res = calculateFTWindow(ctx.local.data, n.ftidpos, n.ftpointer);
    if(ftpos.pos.dunit != null)
      res = calculateFTDistance(ctx.local.data, n.ftidpos, n.ftpointer);
    if(ftpos.pos.same || ftpos.pos.different)
      res = calculateFTScope(ctx.local.data, n.ftidpos, n.ftpointer);
    if(ftpos.pos.start || ftpos.pos.end || ftpos.pos.content);
      //return n;

    if(res == null) return new NodeSet(ctx);
    return new NodeSet(Array.extractIDsFromData(res), ctx, res);
  }

  @Override
  public boolean indexOptions(final MetaData meta) {
    // if the following conditions yield true, the index is accessed:
    // - no ftcontent option was specified
    return !ftpos.pos.start && !ftpos.pos.end && !ftpos.pos.content &&
      super.indexOptions(meta);
  }

  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr)
      throws QueryException {

    exprs[0] = exprs[0].indexEquivalent(ctx, curr);
    final FTArrayExpr e = exprs[0];
    
    // to be checked... will probably disappear as soon as pre values are 
    // processed one by one
    if(e instanceof FTIntersection) {
      ((FTIntersection) e).pres = true;
    } else if(e instanceof FTUnion) {
      ((FTUnion) e).po = true;
    } else if(e instanceof FTUnaryNotExprs || e instanceof FTMildNotExprs) {
      final FTArrayExpr not = e;
      final FTArrayExpr ex = not.exprs[0];
      final FTIntersection fti1 = new FTIntersection(not.exprs, true);
      final FTPositionFilter ftposfil = ftpos.clone();

      final FTSelect ftps = new FTSelect(fti1, ftposfil);
      not.exprs = new FTArrayExpr[] { ex, ftps };
      return not;
    } else if(e instanceof FTIndex
        && (ftpos.pos.start || ftpos.pos.end || ftpos.pos.content)) {
      ((FTIndex) e).setFTPosFilter(ftpos);
    }
    return this;
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    return exprs[0].indexSizes(ctx, curr, min);
  }

  /**
   * Checks whether input value order equals query order.
   * Each search string (their results are stored in res) is numbered
   * concerning its order in the query. P stores the corresponding
   * order number for each search result in res.
   *
   * @param res int[][] result as input
   * @param p int[] pointer
   * @return int[][] id and pos for each result in order
   */
  private static int[][] calculateFTOrdered(final int[][] res, final int[] p) {
    if(res == null || res[0].length == 0 || p == null) {
      return new int[][] {};
    }

    final int[][] maxRes = new int[2][res[0].length * 2];
    int c = 0;
    int cn = 0;
    int level = 0;
    int j = 0;
    int lid;
    int lpo;
    int ls = 0;
    IntList stack = new IntList();
    boolean f = true;

    int e; // = j;
    int s = 0;
    int i;
    int pold; // = p[e+1];
    while(j < res[0].length) {
      cn = c;
      lid = res[0][j];
      //ls = s;
      s = j;
      ls = s;
      pold = p[s + 1];

      while(j < res[0].length && res[0][j] == lid && p[j + 1] == pold) {
        j++;
      }

      e = j;
      if(j == res[0].length) break;
      lpo = p[j + 1];

      while(j < res[0].length && res[0][j] == lid) {
        i = s;
        while(i < e) {
          if(res[1][i] > res[1][j]) {
            break;
          }

          if(res[1][i] < res[1][j] && (res[1][ls] <= res[1][i])) {
            // backup result
            stack.add(i);
            if(f) {
              level++;
              f = false;
              ls = s;
            }
            s = i + 1;
          }
          i++;
        }

        if(level == p[0]) {
          for(int k = 0; k < stack.size; k++) {
            maxRes[0][c] = res[0][stack.get(k)];
            maxRes[1][c++] = res[1][stack.get(k)];
          }

          level = 0;
          stack = new IntList();
          f = true;

          while(j < res[0].length && res[0][j] == lid && p[j + 1] == lpo) {
            maxRes[0][c] = res[0][j];
            maxRes[1][c++] = res[1][j++];
          }
          j--;
        }

        j++;

        if(j >= res[0].length) break;

        if(lpo != p[j + 1] && lpo != p[0]) {
          s = e;
          e = j;
          // <SG> pold is not used...
          pold = p[s + 1];
          lpo = p[e + 1];
          lid = res[0][s];
          f = true;
        }
      }

      // pos value could be unsorted, because they are ordered by
      // the query
      if(c < maxRes[0].length && cn < maxRes[0].length) {
        Arrays.sort(maxRes[1], cn, c);
      }
      level = 0;
      stack = new IntList();
      f = true;
    }

    final int[][] ret = new int[2][c];
    System.arraycopy(maxRes[0], 0, ret[0], 0, c);
    System.arraycopy(maxRes[1], 0, ret[1], 0, c);
    return ret;
  }

  /**
   * Searches in a window for occurrence of search values.
   * All results which satisfy the n-scope condition are returned.
   * Windows could be defined out of n words, sentences or paragraphs
   * @param dat data reference
   * @param res result set
   * @param p pointer
   * @return int[] result
   */
  private int[][] calculateFTWindow(final Data dat, final int[][] res,
      final int[] p) {
    if(res == null || res[0].length == 0 || p == null) {
      return new int[][] {};
    }

    // run variable over pointer
    int k = 0;
    // hits found
    final int[][] mres = new int[2][res[0].length * 2];
    int c = 0;
    // position in byteArray
    int pos = -1;
    // data form db to id
    byte[] data;
    // levelArray
    int[][] level;
    // levelTester
    int l;

    int i = 0;
    int lid = -1;
    // process all entries from resultFTAnd
    for(k = 0; k < res[0].length; k++) {
      // hit was found => jump over equal ids (same node)
      if(lid != res[0][k]) {
        lid = -1;

        // all pointer processed and last != 0
        if(k + 1 == p.length) {
          break;
        }

        // data to id (restultFTAnd[k][0]) from db
        data = dat.text(res[0][k]);
        // load byte array position,
        // which is n scopes far away from resultFTAnd[k][1]
        pos = getBytePositionNScopsFollowing(res[1][k], data,
            (int) ftpos.window.num(), ftpos.pos.wunit);

        i = k;
        //level = new int[pointer[0]+1][2];
        level = new int[2][p[0] + 1];
        // while ids are equal and window isn't left
        while(i < res[0].length && res[0][k] == res[0][i] && res[1][i] <= pos) {
          // save level
          level[0][p[i + 1]] = res[0][i];
          level[1][p[i + 1]] = res[1][i];
          lid = res[0][i];
          i++;
        }

        l = 0;
        // level occupied
        while(l < level[0].length) {
          if(level[0][l] == 0) {
            break;
          }
          l++;
        }

        // copy result
        if(l == level[0].length) {
          System.arraycopy(level[0], 0, mres[0], c, level[0].length);
          System.arraycopy(level[1], 0, mres[1], c, level[0].length);
          c = c + level[0].length;
        }
      }
    }

    final int[][] result = new int[2][c];
    System.arraycopy(mres[0], 0, result[0], 0, c);
    System.arraycopy(mres[1], 0, result[1], 0, c);
    return result;
  }

  /**
   * Lookup in data from startPostion for ending position of an word,
   * which is n words away from the word at start position.
   *
   * returns:
   * - endPosition, if a word was found
   * - data.length, if any word was found
   *
   * equal processing for scope = [sentences|paragraphs]
   *
   * @param s start position
   * @param data data from database
   * @param nn number
   * @param unit distance unit
   * @return endPosition of nth word after word at start position
   */
  private int getBytePositionNScopsFollowing(final int s, final byte[] data,
      final int nn, final FTUnit unit) {
    if(data == null || nn == 0 || unit == null) {
      return -1;
    }

    byte v;
    int n = nn;
    int sp = s;

    if(unit == FTUnit.WORDS) {
      // skip n words in Array
      while(sp < data.length && n > 0) {
        v = (byte) ((data[sp] & 0x7F) + (data[sp] < 0 ? 128 : 0));

        // a-z = 97-122 or  A-Z = 65-90
        // word ending reached
        while(Token.ftChar(v)) {
          sp++;
          if(sp < data.length) {
            v = (byte) ((data[sp] & 0x7F) + (data[sp] < 0 ? 128 : 0));
          } else {
            break;
          }
        }

        if(sp >= data.length) {
          break;
        }
        v = (byte) ((data[sp] & 0x7F) + (data[sp] < 0 ? 128 : 0));
        // skip empty symbols or punctuation mark
        while(sp < data.length
            && !(((v > 64 && v < 91)) || (v > 96 && v < 123))) {
          sp++;
          if(sp < data.length) {
            v = (byte) ((data[sp] & 0x7F) + (data[sp] < 0 ? 128 : 0));
          } else {
            break;
          }
        }

        n--;
      }
      if(sp == data.length) {
        if(n == 0) {
          return sp - 1;
        } else {
          return sp;
        }
      }

      return sp;
    } else if(unit == FTUnit.SENTENCES || unit == FTUnit.PARAGRAPHS) {
      int ep = getSEPositions(data, sp, false, unit)[1] + 1;

      while(ep < data.length && n > 0) {
        ep = getSEPositions(data, ep + 1, false, unit)[1] + 1;
        n--;
      }
      return ep;
    } else {
      //error("ERROR: illegal scope - use: [words|sentences|paragraphs]");

    }
    return -1;
  }

  /**
   * Calculate start and end position for a sentence or paragraph;
   * Return the position of the first symbol and the punctuation mark.
   * If any punctuation mark was found, the position of the last symbol
   * is returned.
   *
   * @param data byte[]
   * @param cpo current position
   * @param s boolean deactivates the start position detection
   * @param unit distance unit
   * @return int[2] start and end position
   */
  private static int[] getSEPositions(final byte[] data, final int cpo,
      final boolean s, final FTUnit unit) {
    final int[] sep = new int[2];
    int cp = cpo;
    int intValue;
    if(unit != null && unit == FTUnit.SENTENCES) {
      if(s) {
        intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
        // sentence start branded through 33=! 46=. 63=? of the further sentence
        while(cp > 0 && intValue != '!' && intValue != '.' && intValue != '?') {
          cp--;
          intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
        }

        // punctuation mark of the further sentence found,
        // set pointer on next symbol after punctuation mark
        if(cp > 0) {
          cp++;
        }
        sep[0] = cp;
      }
      cp = cpo;
      intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
      // sentence ending branded through 33=! 46=. 63=? of the current sentence
      while(cp < data.length - 1 && intValue != 33 && intValue != 46
          && intValue != 63) {
        cp++;
        intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
      }
      if(cp == data.length) {
        cp = cp - 1;
      }
      sep[1] = cp;
      return sep;
    } else if(unit != null && unit == FTUnit.PARAGRAPHS) {
      if(s) {
        intValue = data[cp];
        // paragraph branded through further \n
        while(cp > 0) {
          cp--;

          if(intValue == '\n') {
            break;
          } else {
            intValue = data[cp];
            //intValue = (data[currentPosition] & 0x7F)
            // + (data[currentPosition] < 0 ? 128:0);
          }
        }

        // move pointer about 2 symboles; on paragraphstarting
        if(cp > 0) {
          cp = cp + 2;
        }
        sep[0] = cp;
      }
      cp = cpo;
      intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);

      // look for next paragraph
      while(cp < data.length - 1) {
        cp++;

        if(intValue == 13) {
          intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
          if(intValue == 10) {
            break;
          }
        } else {
          intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
        }
      }

      if(cp == data.length) cp--;
      sep[1] = cp;
      return sep;
    }
    return new int[] {};
  }

  /**
   * Calculates the distance between two positions (pos1 and pos2),
   * depending on scope([words|sentences|paragraphs]).
   *
   * @param data text from database
   * @param p1 start pos word1
   * @param p2 start pos word2
   * @return distance in unit ([words|sentences|paragraphs])
   */
  private int calculateDistance(final byte[] data, final int p1, final int p2) {
    int distance = -1;
    int pos;
    int pos1 = p1;
    int pos2 = p2;

    if(pos1 != Math.min(pos1, pos2)) {
      final int tmp = pos1;
      pos1 = pos2;
      pos2 = tmp;
    }

    pos = pos1;
    while(pos < pos2) {
      pos = getBytePositionNScopsFollowing(pos, data, 1, ftpos.pos.dunit);
      distance++;
    }

    if(pos2 == pos) {
      return distance;
    } else {
      return -1;
    }
  }

  /**
   * Checks whether search value satisfy the distance criteria.
   *
   * @param dat data reference
   * @param res result
   * @param p  pointer at result
   * @return result int [][]
   */
  public int[][] calculateFTDistance(final Data dat, final int[][] res,
      final int[] p) {
    if(res == null || res[0].length == 0 || ftpos.dist == null) {
      return new int[][] {};
    }

    int level = 0;
    int nextId = 0;
    final int[][] maxResult = new int[2][res[0].length * 2];
    int counter = 0;
    int j = 0;
    int lastId = res[0][0];
    int distance;

    for(int i = 0; i < res[0].length; i++) {
      // same Id
      if(lastId == res[0][i]) {
        // all pointer processed and last not equal 0
        if(i + 1 == p.length) break;

        if(p[i + 1] == 0) {
          // load data for id (res[0][i]) from db
          final byte[] dataFromDB = dat.text(res[0][i]);

          /* calculate maximum position
          // in case of at least, to == res[0].length
          if(ftpos.dist[0].num() != ftpos.dist[0].num()) {
            ftpos.to = new Num(dataFromDB.length - 1);
          }*/

          // skip ids?
          if(nextId > i) {
            i = nextId;
          }
          j = i;

          // skip following ids with  level = 0
          while(j + 1 < p.length && p[j + 1] == 0)
            j++;

          // attend following ids with greater level
          while(j < res[0].length && lastId == res[0][j]) {
            distance = calculateDistance(dataFromDB, res[1][i], res[1][j]);
            if(ftpos.dist[0] <= distance && distance <= ftpos.dist[1]) {
              if(level + 1 == p[j + 1]) {
                // hit found
                if(p[j + 1] == p[0]) {
                  maxResult[0][counter] = res[0][i];
                  maxResult[1][counter] = res[1][i];
                  counter++;
                  maxResult[0][counter] = res[0][j];
                  maxResult[1][counter] = res[1][j];
                  counter++;
                  level = 0;
                } else {
                  level++;
                }
              }
              j++;
            } else {
              if(ftpos.dist[0] == ftpos.dist[1]) {
                nextId = j;
                level = 0;
                break;
              } else {
                j++;
              }
            }
          }
        }
      } else {
        level = 0;
        lastId = res[0][i];
        i--;
      }
    }

    final int[][] result = new int[2][counter];
    System.arraycopy(maxResult[0], 0, result[0], 0, counter);
    System.arraycopy(maxResult[1], 0, result[1], 0, counter);

    return result;
  }

  /**
   * Checks whether words are contained in the same oder different
   * sentence or paragraphs (depending of constraint). An id is added
   * to result set, if the first word pair achieves the scope constraint.
   * the processing goes on with the next.
   *
   * @param dat data reference
   * @param res results from other ft queries parts
   * @param p pointer int[]
   * @return ids int[][]
   */
  public int[][] calculateFTScope(final Data dat, final int[][] res,
      final int[] p) {
    if(res == null || res[0] == null || res[0].length == 0 || p == null) {
      return new int[][] {};
    }

    final int[][] maxRes = new int[2][res[0].length * 2];
    int c = 0;
    int level = 0;
    int j = 0;
    int lid;
    IntList stack = new IntList();
    boolean f = true;
    boolean h = false;

    int e;
    int s = 0;
    int i = 0;
    int pold;
    int[] startEndPos = new int[2];
    byte[] dataFromDB;
    while(j < res[0].length) {
      lid = res[0][j];
      s = j;
      pold = p[s + 1];

      while(j < res[0].length && res[0][j] == lid && p[j + 1] == pold) {
        j++;
      }

      e = j;
      i = s;
      while(j < res[0].length && res[0][j] == lid) {
        dataFromDB = dat.text(res[0][i]);
        while(i < e) {
          startEndPos = getSEPositions(dataFromDB, res[1][i], true,
              FTUnit.WORDS);

          if(ftpos.pos.same) {
            while(j < res[1].length - 1 && res[1][j] < startEndPos[0])
              j++;
            if(startEndPos[0] <= res[1][j] && startEndPos[1] >= res[1][j]) {
              h = true;
            }
          } else if(ftpos.pos.different) {
            if(startEndPos[0] > res[1][j] || startEndPos[1] < res[1][j]) {
              h = true;
            }
          }

          // check constraint
          if(h) {
            // backup result
            stack.add(i);
            stack.add(j);
            if(f) {
              level++;
              f = false;
            }
            h = false;
            if(j < res[0].length - 1 && pold == p[j + 1]) j++;
          }

          i++;
        }

        if(level == p[0]) {
          for(int k = 0; k < stack.size; k++) {
            maxRes[0][c] = res[0][stack.get(k)];
            maxRes[1][c++] = res[1][stack.get(k)];
          }

          j++;
          while((j < res[0].length && res[0][j] == lid && p[j + 1] ==
            p[stack.get(stack.size - 1) + 1]) &&
            ((ftpos.pos.same && startEndPos[0] <= res[1][j] &&
                startEndPos[1] >= res[1][j]) ||
             (ftpos.pos.different && startEndPos[0] > res[1][j] &&
                 startEndPos[1] < res[1][j]))) {
            maxRes[0][c] = res[0][j];
            maxRes[1][c++] = res[1][j++];
          }
          j--;

          level = 0;
          stack = new IntList();
          f = true;

        }

        j++;

        if(j >= res[0].length) break;

        if(p[j] != p[j + 1] && p[j] != p[0]) {
          e = j;
          lid = res[0][s];
          f = true;
        }
      }

      level = 0;
      stack = new IntList();
      f = true;
    }


    final int[][] ret = new int[2][c];
    System.arraycopy(maxRes[0], 0, ret[0], 0, c);
    System.arraycopy(maxRes[1], 0, ret[1], 0, c);
    return ret;
  }

  /*
   * FTTimes compares the appearance and bounds (from, to).
   * If the form-to codition is satisfied, the
   * size is return, else 0.
   *
   * @param res int[][] ids
   * @return int[][] results
  public int[][] calculateFTTimes(final int[][] res) {
    // <SG> might be placed in FTWords

    if(res == null || res[0] == null || res[0].length == 0  ||
        ftpos.from == null) return new int[][]{};

    if (ftpos.to == null && ftpos.ftRange == FTPositionFilter.RANGE.ATLEAST) {
      ftpos.to = new Num(res[0].length);
    }

    int[][] maxResult = new int[2][res[0].length];
    int count = 0;
    int times = 1;
    int[] stack = new int[res[0].length];

    for(int i = 1; i <= res[0].length; i++) {
      // count same ids
      if (i < res[0].length && res[0][i - 1] == res[0][i]) {
        stack[times] = res[1][i - 1];
        times++;
      } else {
        // different ids
        if (ftpos.from.num() <= times && times <= ftpos.to.num()) {
          // backup id
          for (int t = 0; t < times; t++) {
            maxResult[0][count] = res[0][i - 1];
            maxResult[1][count++] = stack[t];
          }
          maxResult[0][count] = res[0][i - 1];
          maxResult[1][count] = res[1][i - 1];
          count++;
        }
        stack = new int[res[0].length];
        times = 1;
      }
    }

    int[][] returnResult = new int[2][count];
    System.arraycopy(maxResult[0], 0, returnResult[0], 0, count);
    System.arraycopy(maxResult[1], 0, returnResult[1], 0, count);
    return returnResult;
  }
   */
}
