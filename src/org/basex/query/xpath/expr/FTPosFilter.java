package org.basex.query.xpath.expr;

import java.util.Arrays;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.internal.FTIndex;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Num;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Fulltext position filter expression and FTTimes.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTPosFilter extends FTArrayExpr {
  /** Query context. */
  XPContext ctx;

  /**
   * Constructor.
   * @param e expressions
   * @param ftps FTPositionFilter
   */
  public FTPosFilter(final Expr[] e, final FTPositionFilter ftps) {
    exprs = e;
    ftpos = ftps;
  }


  @Override
  public NodeSet eval(final XPContext context) throws QueryException {
    this.ctx = context;
    int[][] res = null;
    NodeSet n;
    Item i = exprs[0].eval(context);
    if (i instanceof NodeSet) {
      n = (NodeSet) i;
      if (ftpos.ftPosFilt != null) {
        if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.ORDERED) {
          res = calculateFTOrdered(n.ftidpos, n.ftpointer);
        } else if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.WINDOW) {
          res = calculateFTWindow(n.ftidpos, n.ftpointer);
        } else if (ftpos.ftPosFilt.equals(
            FTPositionFilter.POSFILTER.DISTANCE)) {
          res = calculateFTDistance(n.ftidpos, n.ftpointer);
        } else if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.SCOPE) {
          res = calculateFTScope(n.ftidpos, n.ftpointer);
        } else if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.CONTENT) {
          return n;
        } else {
          return new NodeSet(ctx);
        }

        if (res == null) return new NodeSet(ctx);
        return new NodeSet(Array.extractIDsFromData(res), context, res);

      } else if (ftpos.ftTimes != null 
          && ftpos.ftTimes == FTPositionFilter.CARDSEL.FTTIMES) { 
        res = calculateFTTimes(n.ftidpos);
        return new NodeSet(Array.extractIDsFromData(res), context, res);
      } else {
        return null;
      }
    }

    throw new QueryException("No NodeSet found.");
  }

  @Override
  public Expr compile(final XPContext context) throws QueryException {
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = exprs[i].compile(context);
    }
    return this;
  }

  @Override
  public Expr indexEquivalent(final XPContext context, final Step curr) 
  throws QueryException {
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = exprs[i].indexEquivalent(context, curr);
    }

    if (exprs[0] instanceof FTIntersection) {
      FTIntersection e = (FTIntersection) exprs[0];
      e.pres = true;
    } else if (exprs[0] instanceof FTUnion) {
      FTUnion e = (FTUnion) exprs[0];
      e.po = true;
    } else if (exprs[0] instanceof FTUnaryNotExprs 
        || exprs[0] instanceof FTMildNotExprs) {
      ArrayExpr not = (ArrayExpr) exprs[0];
      Expr e = not.exprs[0];
      FTIntersection fti1 = new FTIntersection(not.exprs, true, context);
      //FTIntersection fti2 = new FTIntersection(not.exprs, true, context);

      FTPositionFilter ftposfil = new  FTPositionFilter();
      ftposfil.ftPosFilt = ftpos.ftPosFilt;
      if (ftposfil.ftPosFilt == FTPositionFilter.POSFILTER.WINDOW) {
        ftposfil.ftUnit = ftpos.ftUnit;
        ftposfil.from = ftpos.from;
      } else if (ftposfil.ftPosFilt == FTPositionFilter.POSFILTER.DISTANCE) {
        ftposfil.ftRange = ftpos.ftRange;
        ftposfil.ftUnit = ftpos.ftUnit;
        ftposfil.from = ftpos.from;
        ftposfil.to = ftpos.to;
      } else if (ftposfil.ftPosFilt == FTPositionFilter.POSFILTER.SCOPE) {
        ftposfil.ftScope = ftpos.ftScope;
        ftposfil.ftUnit = ftpos.ftUnit;
      }

      FTPosFilter ftps = new FTPosFilter(new Expr[]{fti1}, ftposfil);
      not.exprs = new Expr[2];
      not.exprs[1] = ftps;
      not.exprs[0] = e;
      return not;
    } else if (exprs[0] instanceof FTIndex && ftpos.ftContent != null) {
      ((FTIndex) exprs[0]).setFTPosFilter(ftpos); 
      ((FTIndex) exprs[0]).setSingle(false);
    } else if (!(ftpos.ftTimes != null 
        && ftpos.ftTimes == FTPositionFilter.CARDSEL.FTTIMES        
    )) { 
      throw new QueryException("FTPosFilter only supported for FTOr and " +
      "FTAnd or FTContent with a single searchstring.");
    } 
    return this;
  }

  @Override
  public int indexSizes(final XPContext context, 
      final Step curr, final int min) {
    return 0;
  }

  /**
   * Checks wether inputvalue order equals query order.
   * Each searchstring (their results are stored in res) is numbered
   * conserning its order in the query. P stores the corresponding
   * order number for each searchresult in res.
   *
   * @param res int[][] result as input
   * @param p int[] pointer
   * @return int[][] id and pos for each result in order 
   */
  public static int[][] calculateFTOrdered(final int[][] res, final int[] p) {
    if(res == null || res[0].length == 0 || p == null) {
      return new int[][]{};
    }

    int[][] maxRes = new int[2][res[0].length * 2];
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
    while (j < res[0].length) {
      cn = c;
      lid = res[0][j];
      //ls = s;
      s = j;
      ls = s;
      pold = p[s + 1];

      while (j < res[0].length && res[0][j] == lid && p[j + 1] == pold) {
        j++;
      }

      e = j;
      if (j == res[0].length) break;
      lpo = p[j + 1];

      while (j < res[0].length  && res[0][j] == lid) {
        i = s; 
        while (i < e) {
          if (res[1][i] > res[1][j]) {
            break;
          }

          if (res[1][i] < res[1][j] && (res[1][ls] <= res[1][i])) {
            // backup result
            stack.add(i);
            if (f) {
              level++;
              f = false;
              ls = s;
            }
            s = i + 1;
          }
          i++;
        }

        if (level == p[0]) {
          for (int k = 0; k < stack.size; k++) {
            maxRes[0][c] = res[0][stack.get(k)];
            maxRes[1][c++] = res[1][stack.get(k)];
          }

          level = 0;
          stack = new IntList();
          f = true;

          while (j < res[0].length && res[0][j] == lid && p[j + 1] == lpo) {
            maxRes[0][c] = res[0][j];
            maxRes[1][c++] = res[1][j++];
          }
          j--;
        }

        j++;

        if (j >= res[0].length) break;

        if (lpo != p[j + 1] && lpo != p[0]) {
          s = e;
          e = j;
          pold = p[s + 1];
          lpo = p[e + 1];
          lid = res[0][s];
          f = true;
        }
      }


      // pos value could be unsorted, because they are ordered by 
      // the query
      if (c < maxRes[0].length && cn < maxRes[0].length) {
        Arrays.sort(maxRes[1], cn, c);
      }
      level = 0; 
      stack = new IntList();
      f = true;
    }

    int[][] ret = new int[2][c];
    System.arraycopy(maxRes[0], 0, ret[0], 0, c);
    System.arraycopy(maxRes[1], 0, ret[1], 0, c);
    return ret;
  }

  /**
   * Searches in a window for occurence of searchvalues. 
   * All results which satisfy the n-scope condition are returned.
   * Windows could be defined out of n words, sentences or paragraphs 
   * @param res Resultset
   * @param p Poiner
   * @return int[] result
   */
  public int[][] calculateFTWindow(final int[][] res, final int[] p) {
    if (res == null || res[0].length == 0 || p == null) {
      return new int[][]{};
    }

    // runvaribale over pointer
    int k = 0;
    // hits found
    int[][] mres = new int [2][res[0].length * 2];
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
    // prozess all entires from resultFTAnd
    for (k = 0; k < res[0].length; k++) {
      // hit was found => jump over equal ids (same node)
      if (lid != res[0][k]) {
        lid = -1;

        // all pointer prozessed and last != 0
        if (k + 1 == p.length) {
          break;
        }

        // data to id (restultFTAnd[k][0]) from db
        data = ctx.local.data.text(res[0][k]);
        // load bytearrayposition, 
        // which is n scopes far away from resultFTAnd[k][1]
        pos = getBytePositionNScopsFollowing(
            res[1][k], data, (int) ftpos.from.num());

        i = k;
        //level = new int[pointer[0]+1][2];
        level = new int[2][p[0] + 1];
        // while ids are equal and window isn't left
        while (i < res[0].length && res[0][k] == res[0][i]
                                                        && res[1][i] <= pos) {

          // save level
          level[0][p[i + 1]] = res[0][i];
          level[1][p[i + 1]] = res[1][i];
          lid = res[0][i];
          i++;
        }

        l = 0;
        // level occupied
        while (l < level[0].length) {
          if (level[0][l] == 0) {
            break;
          }
          l++;
        }

        // copie result
        if (l == level[0].length) {
          System.arraycopy(level[0], 0, mres[0], c, level[0].length);
          System.arraycopy(level[1], 0, mres[1], c, level[0].length);
          c = c + level[0].length;
        }
      }
    }
    if(c == 0) return null;

    int[][] result = new int[2][c];
    System.arraycopy(mres[0], 0, result[0], 0, c);
    System.arraycopy(mres[1], 0, result[1], 0, c);
    return result;
  }


  /**
   * Lookup in data from startPostion for endingposition of an word,
   * which is n words away from the word at startposition.
   *
   * returns:
   * - endPosition, if a word was found
   * - data.length, if any word was found
   *
   * equal prozessing for scope = [senteces|paragraphs]
   *
   * @param s startposition
   * @param data data from database
   * @param nn number
   * @return endPosition of nth word after word at startpos
   */
  public int getBytePositionNScopsFollowing(final int s, final byte[] data, 
      final int nn) {
    if (data == null || nn == 0 || ftpos.ftUnit == null) {
      return -1;
    }

    byte v;
    int n = nn; 
    int sp = s;

    if (ftpos.ftUnit == FTPositionFilter.UNIT.WORDS) {
      // skip n words in Array
      while (sp < data.length && n > 0) {
        v = (byte) ((data[sp] & 0x7F) + (data[sp] < 0 ? 128 : 0));

        // a-z = 97-122 or  A-Z = 65-90
        // wortending reached
        while (Token.ftChar(v)) { 
          sp++;
          if (sp < data.length) {
            v = (byte) ((data[sp] & 0x7F) + (data[sp] < 0 ? 128 : 0));
          } else {
            break;
          }
        }

        if (sp >= data.length) {
          break;
        }
        v = (byte) ((data[sp] & 0x7F) + (data[sp] < 0 ? 128 : 0));
        // skipt emptysymbols or punctuation mark
        while (sp < data.length && !(((v > 64 && v < 91)) 
            || (v > 96 && v < 123))) {
          sp++;
          if (sp < data.length) {
            v = (byte) ((data[sp] & 0x7F) + (data[sp] < 0 ? 128 : 0));
          } else {
            break;
          }
        }

        n--;
      }
      if (sp == data.length) {
        if (n == 0) {
          return sp - 1;
        } else {
          return sp;
        }
      }

      return sp;
    } else if (ftpos.ftUnit == FTPositionFilter.UNIT.SENTENCES 
        || ftpos.ftUnit == FTPositionFilter.UNIT.PARAGRAPHS) {
      int ep = getSEPositions(data, sp, false)[1] + 1;

      while (ep < data.length && n > 0) {
        ep = getSEPositions(data, ep + 1, false)[1] + 1;
        n--;
      }
      return ep;
    }  else {
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
   * @return int[2] start and end position
   */
  public int[] getSEPositions(final byte[] data, final int cpo, 
      final boolean s) {
    int[] sep = new int[2];
    int cp = cpo;
    int intValue;
    if (ftpos.ftUnit != null 
        && ftpos.ftUnit == FTPositionFilter.UNIT.SENTENCES || 
        ftpos.ftBigUnit != null 
        && ftpos.ftBigUnit == FTPositionFilter.BIGUNIT.SENTENCE    
    ) {
      if (s) {
        intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
        // sentence start branded through 33=! 46=. 63=? of the further sentence
        while(cp > 0 && intValue != '!' && intValue != '.'
          && intValue != '?') {
          cp--;
          intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
        }

        // punctuation mark of the further sentece found, 
        // set pointer on next symbole after punctuation mark
        if (cp > 0) {
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
      if (cp == data.length) {
        cp = cp - 1;
      }
      sep[1] = cp;
      return sep;
    } else if(ftpos.ftUnit != null 
        && ftpos.ftUnit == FTPositionFilter.UNIT.PARAGRAPHS || 
        ftpos.ftBigUnit != null 
        && ftpos.ftBigUnit == FTPositionFilter.BIGUNIT.PARAGRAPH) {
      if (s) {
        intValue = data[cp];
        // paragraph branded through further \n
        while(cp > 0) {
          cp--;

          if (intValue == '\n') {
            break;
          } else {
            intValue = data[cp];
            //intValue = (data[currentPosition] & 0x7F) 
            // + (data[currentPosition] < 0 ? 128:0);
          }
        }

        // move pointer about 2 symboles; on paragraphstarting
        if (cp > 0) {
          cp = cp + 2;
        }
        sep[0] = cp;
      }
      cp = cpo;
      intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);

      // look for next paragraph
      while(cp < data.length - 1) {
        cp++;

        if (intValue == 13) {
          intValue = (data[cp] & 0x7F) + (data[cp] < 0 ? 128 : 0);
          if (intValue == 10) {
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
    return null;
  }

  /**
   * Calulcates the distance between two positions (pos1 and pos2), 
   * depending on scope([words|sentences|paragraphs]).
   *
   * @param data text from database
   * @param p1 startpos word1
   * @param p2 startpos word2
   * @return distance in unit ([words|sentences|paragraphs])
   */
  public int calculateDistance(final byte[] data, final int p1, final int p2) {
    int distance = -1;
    int pos;
    int pos1 = p1;
    int pos2 = p2;

    if (pos1 != Math.min(pos1, pos2)) {
      int tmp = pos1;
      pos1 = pos2;
      pos2 = tmp;
    }

    pos = pos1;
    while (pos < pos2) {
      pos = getBytePositionNScopsFollowing(pos, data, 1);
      distance++;
    }

    if (pos2 == pos) {
      return distance;
    } else {
      return -1;
    }
  }


  /**
   * Checks whether searchvalue satisfy the disance criterica.
   *
   * @param res result
   * @param p  pointer at result
   * @return result int [][]
   */
  public int[][] calculateFTDistance(final int[][] res, final int[] p) {
    if (res == null || res[0].length == 0 
        || ftpos.from == null  || ftpos.to == null) {
      return new int[][]{};
    }

    int level = 0;
    int nextId = 0;
    int[][] maxResult = new int[2][res[0].length * 2];
    int counter = 0;
    int j = 0;
    int lastId = res[0][0];
    int distance;

    for (int i = 0; i < res[0].length; i++) {
      // same Id
      if (lastId == res[0][i]) {
        // all pointer processed and last not equal 0
        if (i + 1 == p.length) break;

        if (p[i + 1] == 0) {
          // load data for id (res[0][i]) from db
          byte[] dataFromDB = ctx.local.data.text(res[0][i]);

          // calculate maximum position
          // in case of at least, to == res[0].length
          if (ftpos.ftRange != FTPositionFilter.RANGE.EXACTLY
              && ftpos.to == null) {
            ftpos.to = new Num(dataFromDB.length - 1);
          }

          // skip ids?
          if (nextId > i) {
            i = nextId;
          }
          j = i;

          // skip following ids with  level = 0
          while (j + 1 < p.length && p[j + 1] == 0) j++;

          // attend following ids with greater level
          while (j < res[0].length && lastId == res[0][j]) {
            distance = calculateDistance(dataFromDB, res[1][i], res[1][j]);
            if (ftpos.from.num() <= distance && distance <= ftpos.to.num()) {
              if (level + 1 == p[j + 1]) {
                // hit found
                if (p[j + 1] == p[0]) {
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
              if (ftpos.ftRange == FTPositionFilter.RANGE.EXACTLY) {
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
        level =  0;
        lastId = res[0][i];
        i--;
      }
    }
    if (counter == 0) return null;
    int[][] result = new int[2][counter];
    System.arraycopy(maxResult[0], 0, result[0], 0, counter);
    System.arraycopy(maxResult[1], 0, result[1], 0, counter);

    return result;
  }

  /**
   * Checks wether words are containted in the same oder different
   * sentece or paragraphs (depending von constraint). An id is added
   * to result set, if the first wordpair achieves the scope constraint.
   * the prozessing goes on with the next. 
   *
   * @param res results from other ftqueriesparts
   * @param p pointer int[]
   * @return ids int[][]
   */
  public int[][] calculateFTScope(final int[][] res, final int[] p) {
    if(res == null || res[0] == null || res[0].length == 0 || p == null) {
      return new int[][]{};
    }

    int[][] maxRes = new int[2][res[0].length * 2];
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
    while (j < res[0].length) {
      lid = res[0][j];
      s = j;
      pold = p[s + 1];

      while (j < res[0].length && res[0][j] == lid && p[j + 1] == pold) {
        j++;
      }

      e = j;
      i = s;
      while (j < res[0].length  && res[0][j] == lid) {
        dataFromDB = ctx.local.data.text(res[0][i]);
        while (i < e) {
          startEndPos = getSEPositions(dataFromDB, res[1][i], true);

          if (ftpos.ftScope == FTPositionFilter.SCOPE.SAME) { 
            while (j < res[1].length - 1 && res[1][j] < startEndPos[0]) j++;
            if (startEndPos[0] <= res[1][j] && startEndPos[1] >= res[1][j]) {
              h = true;
            }
          } else if (ftpos.ftScope == FTPositionFilter.SCOPE.DIFFERENT) { 
            if (startEndPos[0] > res[1][j] || startEndPos[1] < res[1][j]) {
              h = true;
            }
          }

          // check constraint
          if (h) {
            // backup result
            stack.add(i);
            stack.add(j);
            if (f) {
              level++;
              f = false;
            }
            h = false;
            if (j < res[0].length - 1 && pold == p[j + 1]) j++;
          }

          i++;
        }

        if (level == p[0]) {
          for (int k = 0; k < stack.size; k++) {
            maxRes[0][c] = res[0][stack.get(k)];
            maxRes[1][c++] = res[1][stack.get(k)];
          }

          j++;
          while ((j < res[0].length && res[0][j] == lid 
              && p[j + 1] == p[stack.get(stack.size - 1) + 1]) 
              && ((ftpos.ftScope == FTPositionFilter.SCOPE.SAME 
                  && startEndPos[0] <= res[1][j] 
                                              && startEndPos[1] >= res[1][j]
              ) || (
                  ftpos.ftScope == FTPositionFilter.SCOPE.DIFFERENT
                  && startEndPos[0] > res[1][j] 
                                             && startEndPos[1] < res[1][j])
              )    
          ) {
            maxRes[0][c] = res[0][j];
            maxRes[1][c++] = res[1][j++];
          }
          j--;

          level = 0;
          stack = new IntList();
          f = true;

        }

        j++;

        if (j >= res[0].length) break;

        if (p[j] != p[j + 1] && p[j] != p[0]) {
          e = j;
          lid = res[0][s];
          f = true;
        }
      }

      level = 0; 
      stack = new IntList();
      f = true;
    }

    if (c == 0) return null;

    if (c == 0) return null;
    int[][] ret = new int[2][c];
    System.arraycopy(maxRes[0], 0, ret[0], 0, c);
    System.arraycopy(maxRes[1], 0, ret[1], 0, c);
    return ret; 
  }

  /**
   * FTTimes compares the appearance and bounds (from, to). 
   * If the form-to codition is satisfied, the
   * size is return, else 0.
   *
   * @param res int[][] ids
   * @return int[][] results
   */
  public int[][] calculateFTTimes(final int[][] res) {
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

    if (count == 0) return null;

    int[][] returnResult = new int[2][count];
    System.arraycopy(maxResult[0], 0, returnResult[0], 0, count);
    System.arraycopy(maxResult[1], 0, returnResult[1], 0, count);
    return returnResult;
  }


  /**
   * Testmethod for calculateFTOrdered.
   * @param args not used
   */
  public static void main(final String[] args) {

    int[][] res = calculateFTOrdered(new int[][] {{
      5, 5,  5,  5,  5,  5, 5,  5, 6,  6,  6,  6,  6,  6, 6,  6},
      { 30, 87, 88, 31, 96, 97, 51, 105, 30, 87, 88, 89, 90, 0, 96, 97 } }, 
      new int[] { 2, 0, 0,  0,  1,  1,  1,  2,  2,   0,  0,  0,  0,
        1,  2, 2,  2 });

    /*  res = calculateFTOrdered(new int[][]{{5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5},
        {4,97,15,25,69,87,86,99,4,97,15,25,69,87,86,99}}, 
        new int[]{2,0,0,1,1,1,1,2,2,0,0,1,1,1,1,2,2});
     */

    for(int[] e : res) {
      for(int i : e) System.out.print(i + ",");
      System.out.println();
    }
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    if (ftpos.ftPosFilt != null) {
      if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.ORDERED) {
        ser.openElement(this, Token.token(fto.ftPosFilt.toString()),  
            Token.token("FTOrdered"));
      } else if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.WINDOW) {
        ser.openElement(this, Token.token(fto.ftPosFilt.toString()),  
            Token.token("FTWindow"), Token.token("from"), 
            Token.token(ftpos.from.num()), Token.token("FTUnit"), 
            Token.token(ftpos.ftUnit.toString()));
      } else if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.DISTANCE) {
        ser.openElement(this, Token.token(fto.ftPosFilt.toString()),  
            Token.token("FTDistance"), Token.token("exactly"), 
            Token.token(ftpos.from.num()), Token.token("FTRange"), 
            Token.token(ftpos.ftRange.toString()), Token.token("FTUnit"), 
            Token.token(ftpos.ftUnit.toString()));
      } else if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.SCOPE) {
        ser.openElement(this, Token.token(fto.ftPosFilt.toString()), 
            Token.token("FTRange"), 
            Token.token(ftpos.ftRange.toString()), Token.token("FTBigUnit"), 
            Token.token(ftpos.ftBigUnit.toString()));
      } else if (ftpos.ftPosFilt == FTPositionFilter.POSFILTER.CONTENT) {
        ser.openElement(this, Token.token(fto.ftPosFilt.toString()), 
            Token.token("FTContent"), 
            Token.token("FTContent"), Token.token(fto.ftContent.toString()));   
      } else {
        ser.openElement(this);
      }
    } else if (ftpos.ftTimes != null 
        && ftpos.ftTimes == FTPositionFilter.CARDSEL.FTTIMES) { 
      ser.openElement(this, Token.token(fto.ftTimes.toString()), 
          Token.token("FTTimes"), 
          Token.token("occures"), 
          Token.token(fto.ftRange.toString() + " times"));   
    } 

    for (Expr e : exprs) e.plan(ser);
    ser.closeElement(this);
  }

}
