package org.basex.ft;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.util.Array;
import org.basex.util.Map;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * Simple Thesaurus for fulltext requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class Thesaurus {
  /** Thesaurus result. */
  private Map<byte[][]> results = new Map<byte[][]>();
  /** Thesaurus root references. */
  private Thes[] thes = {};

  /**
   * Reads a thesaurus file.
   * @param fl file reference
   * @return true if everything went alright
   */
  public boolean read(final IO fl) {
    try {
      final Data data = new MemBuilder().build(new DirParser(fl), "");
      final Thes th = new Thes();
      th.root = new Nodes(0, data);
      thes = Array.add(thes, th);
      return true;
    } catch(final IOException ex) {
      return false;
    }
  }

  /**
   * Merges two thesaurus definitions.
   * @param th second thesaurus
   */
  public void merge(final Thesaurus th) {
    for(final Thes t : th.thes) {
      boolean f = false;
      for(final Thes tt : thes) f |= tt.eq(t);
      if(!f) thes = Array.add(thes, t);
    }
  }

  /**
   * Sets a relationship.
   * @param rs relationship
   */
  public void rs(final byte[] rs) {
    thes[thes.length - 1].rel = rs;
  }

  /**
   * Sets levels.
   * @param mn minimum level
   * @param mx maximum level
   */
  public void level(final long mn, final long mx) {
    thes[thes.length - 1].min = mn;
    thes[thes.length - 1].max = mx;
  }

  /**
   * Finds a thesaurus term.
   * @param term term to be found
   * @return result list
   */
  public byte[][] find(final byte[] term) {
    byte[][] result = results.get(term);
    
    if(result == null) {
      try {
        final TokenList tl = new TokenList();
        for(final Thes th : thes) {
          final TokenBuilder tb = new TokenBuilder();
          tb.add("/*:thesaurus/*:entry[*:term = '");
          tb.add(term);
          tb.add("']//*:synonym");
          if(th.rel.length != 0) {
            tb.add("[*:relationship = '");
            tb.add(th.rel);
            tb.add("']");
          }
          tb.add("/*:term");
          System.out.println(tb);
          
          final QueryProcessor qp = new QueryProcessor(tb.toString(), th.root);
          for(final Item it : qp.iter()) tl.add(it.str());
        }
        result = tl.finish();
        results.add(term, result);
      } catch(final QueryException ex) {
        BaseX.debug(ex);
      }
    }    
    return result;
  }
  
  /** Single thesaurus entry. */
  static class Thes {
    /** Thesaurus root references. */
    Nodes root;
    /** Relationship. */
    byte[] rel = Token.EMPTY;
    /** Minimum level. */
    long min;
    /** Maximum level. */
    long max = Long.MAX_VALUE;

    /**
     * Compares two thesaurus instances.
     * @param th instance to be compared
     * @return result of check
     */
    public boolean eq(final Thes th) {
      if(root == null || th.root == null) return root == th.root;
      return root.data.meta.file.eq(th.root.data.meta.file) &&
          min == th.min && max == th.max && Token.eq(rel, th.rel);
    }
  }
}
