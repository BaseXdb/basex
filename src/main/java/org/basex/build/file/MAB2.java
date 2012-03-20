package org.basex.build.file;

import static org.basex.util.Token.token;

/**
 * MAB2 Fields, needed for importing MAB2 data as XML.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
interface MAB2 {
  /** Root tag. */
  byte[] LIBRARY = token("Library");
  /** Medium tag. */
  byte[] MEDIUM = token("Medium");
  /** ID Attribute. */
  byte[] MV_ID = token("mv_id");
  /** ID attribute. */
  byte[] BIB_ID = token("bib_id");
  /** Subordinate titles attribute. */
  byte[] MAX = token("max");

  /** Tag. */
  byte[] DESCRIPTION = token("Desc");
  /** Tag. */
  byte[] DETAILS = token("Detail");
  /** Tag. */
  byte[] FORMAT = token("Format");
  /** Tag. */
  byte[] INSTITUTE = token("Institution");
  /** Tag. */
  byte[] ISBN = token("ISBN");
  /** Tag. */
  byte[] POSTER = token("Poster");
  /** Tag. */
  byte[] GENRE = token("Genre");
  /** Tag. */
  byte[] LANGUAGE = token("Language");
  /** Tag. */
  byte[] NOTE = token("Note");
  /** Tag. */
  byte[] ORIGINAL = token("Original");
  /** Tag. */
  byte[] AUTHOR = token("Author");
  /** Tag. */
  byte[] PUBLISHER = token("Publisher");
  /** Tag. */
  byte[] SIGNATURE = token("Signature");
  /** Tag. */
  byte[] SUBJECT = token("Subject");
  /** Tag. */
  byte[] SUBTITLE = token("Subtitle");
  /** Tag. */
  byte[] TITLE = token("Title");
  /** Tag. */
  byte[] TOWN = token("Town");
  /** Tag. */
  byte[] TYPE = token("Type");
  /** Tag. */
  byte[] YEAR = token("Year");
  /** Tag. */
  byte[] LENDINGS = token("Lendings");
  /** Tag. */
  byte[] STATUS = token("Status");
  /** Semicolon. */
  byte[] SEMI = token("; ");
}
