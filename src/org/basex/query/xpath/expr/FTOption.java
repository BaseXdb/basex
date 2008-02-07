package org.basex.query.xpath.expr;

import org.basex.query.xpath.values.Num;

/**
 * FTOption; defines options for a fulltext expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTOption {
  /** Case sensitivity enumeration. */
  public enum CASE {
    /** Insensitive search. */ INSENSITIVE,
    /** Sensitive search.   */ SENSITIVE,
    /** Lowercase search.   */ LOWERCASE,
    /** Uppercase search.   */ UPPERCASE;
  };
  
  /** Case sensitivity enumeration. */
  public enum WILD {
    /** Using wildcards. */ WITH,
    /** No wildcards.    */ WITHOUT;
  };
  
  /** Case sensitivity (default: case insensitive). */ 
  public CASE ftCase = CASE.INSENSITIVE;
  /** Wildcard search (default: without wildcards). */ 
  public WILD ftWild = WILD.WITHOUT;


  /** Cardinality Selection. */
  public enum CARDSEL {
    /** count occurence */ FTTIMES
  };
  
  /** Position filter enumeration. */
  public enum POSFILTER {
    /** Ordered selection. */ ORDERED,
    /** Windows selection.   */ WINDOW,
    /** Distance selection.   */ DISTANCE,
    /** Scope selection.   */ SCOPE,
    /** Content selection. */ CONTENT;
  };
  
  /** Scope enumeration. */
  public enum SCOPE {
    /** same */ SAME,
    /** different */ DIFFERENT;
  }
  
  /** Range enumeration */
  public enum RANGE {
    /** exactly */ EXACTLY,
    /** at least */ ATLEAST,
    /** at most */ ATMOST,
    /** from to */ FROMTO;
  }

  /** Unit enumeration for PosFilter Expressions. */ 
  public enum UNIT {
    /** words */ WORDS,
    /** sentences */ SENTENCES,
    /** paragraphs */ PARAGRAPHS;
  }

  /** Unit enumeration for PosFilter Expressions. */ 
  public enum BIGUNIT {
    /** sentences */ SENTENCE,
    /** paragraphs */ PARAGRAPH;
  }

  
  /** Content enumeration for PosFilter Expressions. */
  public enum CONTENT {
    /** at start */ ATSTART,
    /** at end */ ATEND,
    /** entire content */ ENTIRECONTENT;
  }
  


  /** Position Filter (defautl: without filters). */
  public POSFILTER ftPosFilt; 
  /** Use for lowerbound in posfilter expression. */
  public Num from;
  /** use for upperbound in posfilter expression. */
  public Num to;
  /** Unit for Position Filter with distance. */
  public UNIT ftUnit;
  /** BigUnit for Position Filter with distance. */
  public BIGUNIT ftBigUnit;
    /** Range for Position Filter with range. */
  public RANGE ftRange;
  /** Scope for Position Filter. */
  public SCOPE ftScope;
  /** Content for Position Filter. */
  public CONTENT ftContent;
  /** Cardinality Selection in ftcontains expression. */
  public CARDSEL ftTimes;

}

