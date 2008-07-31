package org.basex.build.mediovis;

import static org.basex.util.Token.token;

/**
 * MAB2 Fields, needed for importing MAB2 data as XML.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface MAB2 {
  // Query tokens
  
  /** Root tag. */
  byte[] ROOT = token("MEDIOVIS");
  /** Hits Attribute. */
  byte[] HITS = token("hits");
  /** Approximate Hits Attribute. */
  byte[] APPROX = token("appr");

  // XML database tokens

  /** Root Tag. */
  byte[] LIBRARY = token("LIBRARY");
  /** Medium Tag. */
  byte[] MEDIUM = token("MEDIUM");
  /** ID Attribute. */
  byte[] MV_ID = token("mv_id");
  /** ID Attribute. */
  byte[] BIB_ID = token("bib_id");
  /** Subordinate titles attribute. */
  byte[] MAX = token("max");

  /** Tag. */
  byte[] DESCRIPTION = token("DES");
  /** Tag. */
  byte[] DETAILS = token("DET");
  /** Tag. */
  byte[] FORMAT = token("FRM");
  /** Tag. */
  byte[] INSTITUTE = token("INS");
  /** Tag. */
  byte[] ISBN = token("ISB");
  /** Tag. */
  byte[] POSTER = token("POS");
  /** Tag. */
  byte[] GENRE = token("GEN");
  /** Tag. */
  byte[] LANGUAGE = token("LAN");
  /** Tag. */
  byte[] NOTE = token("ANN");
  /** Tag. */
  byte[] ORIGINAL = token("ORI");
  /** Tag. */
  byte[] PERSON = token("PER");
  /** Tag. */
  byte[] PUBLISHER = token("PUB");
  /** Tag. */
  byte[] SIGNATURE = token("SIG");
  /** Tag. */
  byte[] SUBJECT = token("SEC");
  /** Tag. */
  byte[] SUBTITLE = token("SUB");
  /** Tag. */
  byte[] TITLE = token("TIT");
  /** Tag. */
  byte[] TOWN = token("TOW");
  /** Tag. */
  byte[] TYPE = token("TYP");
  /** Tag. */
  byte[] YEAR = token("YEA");
  /** Missing MV ID. */
  byte[] MISSING = token("#");
  /** Semicolon. */
  byte[] SEMI = token("; ");
}
