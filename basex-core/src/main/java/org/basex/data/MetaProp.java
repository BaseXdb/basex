package org.basex.data;

import java.util.*;

import org.basex.util.*;

/**
 * This class provides meta properties.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum MetaProp {
  /** Property. */
  NAME(false) {
    @Override
    public String value(final MetaData meta) { return meta.name; }
  },
  /** Property. */
  SIZE(false) {
    @Override
    public Long value(final MetaData meta) { return meta.dbSize(); }
  },
  /** Property. */
  NODES(false) {
    @Override
    public Integer value(final MetaData meta) { return meta.size; }
  },
  /** Property. */
  DOCUMENTS(false) {
    @Override
    public Integer value(final MetaData meta) { return meta.ndocs; }
  },
  /** Property. */
  BINARIES(false) {
    @Override
    public Integer value(final MetaData meta) {
      return meta.dir != null ? meta.binaryDir().descendants().size() : 0;
    }
  },
  /** Property. */
  TIMESTAMP(false) {
    @Override
    public String value(final MetaData meta) { return DateTime.format(new Date(meta.dbTime())); }
  },
  /** Property. */
  UPTODATE(false) {
    @Override
    public Boolean value(final MetaData meta) { return meta.uptodate; }
  },
  /** Property. */
  INPUTPATH(false) {
    @Override
    public String value(final MetaData meta) { return meta.original; }
  },
  /** Property. */
  INPUTSIZE(false) {
    @Override
    public Long value(final MetaData meta) { return meta.inputsize; }
  },
  /** Property. */
  INPUTDATE(false) {
    @Override
    public String value(final MetaData meta) { return DateTime.format(new Date(meta.time)); }
  },

  /** Property. */
  TEXTINDEX(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.textindex; }
  },
  /** Property. */
  ATTRINDEX(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.attrindex; }
  },
  /** Property. */
  TOKENINDEX(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.tokenindex; }
  },
  /** Property. */
  FTINDEX(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.ftindex; }
  },
  /** Property. */
  TEXTINCLUDE(true) {
    @Override
    public String value(final MetaData meta) { return meta.textinclude; }
  },
  /** Property. */
  ATTRINCLUDE(true) {
    @Override
    public String value(final MetaData meta) { return meta.attrinclude; }
  },
  /** Property. */
  TOKENINCLUDE(true) {
    @Override
    public String value(final MetaData meta) { return meta.tokeninclude; }
  },
  /** Property. */
  FTINCLUDE(true) {
    @Override
    public String value(final MetaData meta) { return meta.ftinclude; }
  },
  /** Property. */
  LANGUAGE(true) {
    @Override
    public String value(final MetaData meta) { return meta.language.toString(); }
  },
  /** Property. */
  STEMMING(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.stemming; }
  },
  /** Property. */
  CASESENS(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.casesens; }
  },
  /** Property. */
  DIACRITICS(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.diacritics; }
  },
  /** Property. */
  STOPWORDS(true) {
    @Override
    public String value(final MetaData meta) { return meta.stopwords; }
  },
  /** Property. */
  UPDINDEX(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.updindex; }
  },
  /** Property. */
  AUTOOPTIMIZE(true) {
    @Override
    public Boolean value(final MetaData meta) { return meta.autooptimize; }
  },
  /** Property. */
  MAXCATS(true) {
    @Override
    public Integer value(final MetaData meta) { return meta.maxcats; }
  },
  /** Property. */
  MAXLEN(true) {
    @Override
    public Integer value(final MetaData meta) { return meta.maxlen; }
  },
  /** Property. */
  SPLITSIZE(true) {
    @Override
    public Integer value(final MetaData meta) { return meta.splitsize; }
  };

  /** Index property. */
  public final boolean index;

  /**
   * Constructor.
   * @param index index property
   */
  MetaProp(final boolean index) {
    this.index = index;
  }

  /** Cached enums (faster). */
  public static final MetaProp[] VALUES = values();

  /**
   * Returns the value of a property.
   * @param meta meta data
   * @return value
   */
  public abstract Object value(MetaData meta);

  /**
   * Returns a property matching the specified string.
   * @param name name of enumeration
   * @return permission, or {@code null} if no match is found
   */
  public static MetaProp get(final String name) {
    for(final MetaProp prop : VALUES) {
      if(prop.toString().toLowerCase(Locale.ENGLISH).equals(name)) return prop;
    }
    return null;
  }
}
