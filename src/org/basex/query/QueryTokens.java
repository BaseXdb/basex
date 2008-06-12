package org.basex.query;

/**
 * This class contains common tokens for the query implementations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface QueryTokens {
  
  // FULLTEXT TOKENS ==========================================================
  
  /** Parser token. */
  String AND = "and";
  /** Parser token. */
  String AT = "at";
  /** Parser token. */
  String CASE = "case";
  /** Parser token. */
  String COMMENT = "comment";
  /** Parser token. */
  String CONTENT = "content";
  /** Parser token. */
  String DIFFERENT = "different";
  /** Parser token. */
  String DISTANCE = "distance";
  /** Parser token. */
  String END = "end";
  /** Parser token. */
  String ENTIRE = "entire";
  /** Parser token. */
  String EXACTLY = "exactly";
  /** Parser token. */
  String FROM = "from";
  /** Parser token. */
  String FTAND = "ftand";
  /** Parser token. */
  String FTCONTAINS = "ftcontains";
  /** Parser token. */
  String FTNOT = "ftnot";
  /** Parser token. */
  String FTOR = "ftor";
  /** Parser token. */
  String FUZZY = "fuzzy";
  /** Parser token. */
  String IN = "in";
  /** Parser token. */
  String INSENSITIVE = "insensitive";
  /** Parser token. */
  String LEAST = "least";
  /** Parser token. */
  String LOWERCASE = "lowercase";
  /** Parser token. */
  String MOST = "most";
  /** Parser token. */ 
  String NODE = "node";
  /** Parser token. */
  String NOT = "not";
  /** Parser token. */
  String OCCURS = "occurs";
  /** Parser token. */
  String OR = "or";
  /** Parser token. */
  String ORDERED = "ordered";
  /** Parser token. */
  String PARAGRAPH = "paragraph";
  /** Parser token. */
  String PARAGRAPHS = "paragraphs";
  /** Parser token. */
  String PI = "processing-instruction";
  /** Parser token. */
  String SAME = "same";
  /** Parser token. */
  String SENSITIVE = "sensitive";
  /** Parser token. */
  String SENTENCE = "sentence";
  /** Parser token. */
  String SENTENCES = "sentences";
  /** Parser token. */
  String START = "start";
  /** Parser token. */
  String TIMES = "times";
  /** Parser token. */
  String TEXT = "text";
  /** Parser token. */
  String TO = "to";
  /** Parser token. */
  String UPPERCASE = "uppercase";
  /** Parser token. */
  String WILDCARDS = "wildcards";
  /** Parser token. */
  String WINDOW = "window";
  /** Parser token. */
  String WITH = "with";
  /** Parser token. */
  String WITHOUT = "without";
  /** Parser token. */
  String WORDS = "words";

  // FULLTEXT TOKENS (currently only parsed in XQuery) ========================
  
  /** Parser fulltext token. */
  String ALL = "all";
  /** Parser fulltext token. */
  String ANY = "any";
  /** Parser fulltext token. */
  String WORD = "word";
  /** Parser fulltext token. */
  String PHRASE = "phrase";
  /** Parser fulltext token. */
  String DIACRITICS = "diacritics";
  /** Parser fulltext token. */
  String LANGUAGE = "language";
  /** Parser fulltext token. */
  String LEVELS = "levels";
  /** Parser fulltext token. */
  String RELATIONSHIP = "relationship";
  /** Parser fulltext token. */
  String STEMMING = "stemming";
  /** Parser fulltext token. */
  String STOP = "stop";
  /** Parser fulltext token. */
  String THESAURUS = "thesaurus";

  /** Parser token. */
  String DBLCOLON = "::";
  /** Parser token. */
  String QUOTE = "quote";

  /** Skip flag for the syntax highlighter. */
  String SKIP = null;

  // ERROR INFORMATION =======================================================
  
  /** Position info. */
  String STOPPED = "Stopped at ";
  /** Position info. */
  String LINEINFO = "line %";
  /** Position info. */
  String COLINFO = ", column %";
  /** Position info. */
  String FILEINFO = " in %";
  /** Parsing exception. */
  String FOUND = ", found \"%\"";
  /** Parsing exception. */
  String UNENTITY = "Unknown entity \"%\".";
  /** Parsing exception. */
  String INVENTITY = "Invalid entity \"%\".";
}
