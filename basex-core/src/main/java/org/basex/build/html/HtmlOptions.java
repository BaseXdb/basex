package org.basex.build.html;

import org.basex.util.options.*;

/**
 * Options for parsing and serializing HTML documents with TagSoup.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class HtmlOptions extends Options {
  /** TagSoup option: html. */
  public static final BooleanOption HTML = new BooleanOption("html", false);
  /** TagSoup option: omit-xml-declaration. */
  public static final BooleanOption OMITXML = new BooleanOption("omit-xml-declaration", false);
  /** TagSoup option: nons. */
  public static final BooleanOption NONS = new BooleanOption("nons", true);
  /** TagSoup option: nobogons. */
  public static final BooleanOption NOBOGONS = new BooleanOption("nobogons", false);
  /** TagSoup option: nodefaults. */
  public static final BooleanOption NODEFAULTS = new BooleanOption("nodefaults", false);
  /** TagSoup option: nocolons. */
  public static final BooleanOption NOCOLONS = new BooleanOption("nocolons", false);
  /** TagSoup option: norestart. */
  public static final BooleanOption NORESTART = new BooleanOption("norestart", false);
  /** TagSoup option: nobogons. */
  public static final BooleanOption IGNORABLE = new BooleanOption("ignorable", false);
  /** TagSoup option: emptybogons. */
  public static final BooleanOption EMPTYBOGONS = new BooleanOption("emptybogons", false);
  /** TagSoup option: any. */
  public static final BooleanOption ANY = new BooleanOption("any", false);
  /** TagSoup option: norootbogons. */
  public static final BooleanOption NOROOTBOGONS = new BooleanOption("norootbogons", false);
  /** TagSoup option: nocdata. */
  public static final BooleanOption NOCDATA = new BooleanOption("nocdata", false);
  /** TagSoup option: lexical. */
  public static final BooleanOption LEXICAL = new BooleanOption("lexical", false);

  /** TagSoup option: method (html). */
  public static final StringOption METHOD = new StringOption("method", "xml");
  /** TagSoup option: doctype-system=systemid. */
  public static final StringOption DOCTYPESYS = new StringOption("doctype-system");
  /** TagSoup option: doctype-public=publicid. */
  public static final StringOption DOCTYPEPUB = new StringOption("doctype-public");
  /** TagSoup option: encoding=encoding. */
  public static final StringOption ENCODING = new StringOption("encoding");

  /**
   * Default constructor.
   */
  public HtmlOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public HtmlOptions(final Options opts) {
    super(opts);
  }
}
