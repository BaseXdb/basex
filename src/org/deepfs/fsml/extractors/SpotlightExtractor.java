package org.deepfs.fsml.extractors;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.MetaElem;
import org.basex.build.fs.util.MetaStore;
import org.basex.build.fs.util.ParserUtil;
import org.basex.build.fs.util.MetaStore.MetaType;
import org.basex.build.fs.util.MetaStore.MimeType;
import org.basex.core.Main;
import org.basex.util.Token;
import org.deepfs.fsml.parsers.MP3Parser;
import org.deepfs.util.LibraryLoader;

/**
 * Extracts metadata from Apple's Spotlight.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class SpotlightExtractor {

  static {
    LibraryLoader.load(LibraryLoader.SPOTEXLIBNAME);
  }

  /**
   * Mapping of spotlight content types to DeepFS MIME types.
   * @author Bastian Lemke
   */
  enum SpotlightContentType {
        /** MP3 file. */
    PUBLIC_MP3(MimeType.MP3),
        /** JPEG file. */
    PUBLIC_JPEG(MimeType.JPG),
        /** PNG file. */
    PUBLIC_PNG(MimeType.PNG),
        /** GIF file. */
    COM_COMPUSERVE_GIF(MimeType.GIF),
        /** BMP file. */
    COM_MICROSOFT_BMP(MimeType.BMP),
        /** TIFF file. */
    PUBLIC_TIFF(MimeType.TIFF),
        /** HTML file. */
    PUBLIC_HTML(MimeType.HTML),
        /** Plain text file. */
    PUBLIC_PLAIN_TEXT(MimeType.TXT),
        /** CSS file. */
    COM_APPLE_DASHCODE_CSS(MimeType.CSS),
        /** Word file. */
    COM_MICROSOFT_WORD_DOC(MimeType.DOC),
        /** PDF file. */
    COM_ADOBE_PDF(MimeType.PDF),
        /** Image. */
    PUBLIC_IMAGE(MetaType.PICTURE),
        /** Audio. */
    PUBLIC_AUDIO(MetaType.AUDIO),
        /** Audiovisual content. */
    PUBLIC_AUDIOVISUAL_CONTENT(MetaType.VIDEO),
        /** Data. */
    PUBLIC_DATA(),
        /** Item. */
    PUBLIC_ITEM(),
        /** Content. */
    PUBLIC_CONTENT();

    /** The {@link MetaType}. */
    private final MetaType metaType;
    /** The {@link MimeType}. */
    private final MimeType mimeType;

    /** Constructor for items that should be ignored. */
    private SpotlightContentType() {
      mimeType = null;
      metaType = null;
    }

    /**
     * Initializes the content type instance with a {@link MimeType}.
     * @param m the corresponding MIME type
     */
    private SpotlightContentType(final MimeType m) {
      mimeType = m;
      metaType = null;
    }

    /**
     * Initializes the content type instance with a {@link MetaType}.
     * @param m the corresponding {@link MetaType}.
     */
    private SpotlightContentType(final MetaType m) {
      metaType = m;
      mimeType = null;
    }

    /**
     * Returns the meta type for this spotlight media type.
     * @return the {@link MimeType}.
     */
    MimeType getFormat() {
      return mimeType;
    }

    /**
     * Returns the meta type for this spotlight content type.
     * @return the {@link MetaType}.
     */
    MetaType getType() {
      return metaType;
    }
  }

  /**
   * Registered metadata items and corresponding actions for metadata events.
   */
  enum Item {
        /** Date and time of the last change made to a metadata attribute. */
    AttributeChangeDate {
      @Override
      void parse(final MetaStore meta, final Object o) {
        if(check(o, Date.class)) meta.add(MetaElem.DATE_ATTRIBUTE_MODIFIED,
            (Date) o);
      }
    },
        /**
     * Title for the collection containing this item. This is analogous to a
     * record label or photo album.
     */
    Album {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.ALBUM, (String) o);
      }
    },
        /** Track number of a song or composition when it's part of an album. */
    AudioTrackNumber {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        parseInt(meta, MetaElem.TRACK, o);
      }
    },
        /** The author of the contents of the file. */
    Authors {
      @Override
      void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.CREATOR, (String) o);
      }
    },
        /**
     * Identifies city of origin according to guidelines established by the
     * provider. For example, "New York", "Cupertino", or "Toronto".
     */
    City {
      @Override
      void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.CITY, (String) o);
      }
    },
        /**
     * A comment related to the file. This comment is not displayed by the
     * Finder.
     */
    Comment {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.DESCRIPTION, (String) o);
      }
    },
        /** Composer of the song in the audio file. */
    Composer {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.COMPOSER, (String) o);
      }
    },
        /** The date and time that the content was created. */
    ContentCreationDate {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, Date.class)) meta.add(MetaElem.DATE_CREATED, (Date) o);
      }
    },
        // /** Date and time when the content of this item was modified. */
    // ContentModificationDate {
    // @Override
    // public void parse(final MetaStore meta, final Object o) {
    // if(check(o, Date.class)) meta.add(MetaElem.DATE_CONTENT_MODIFIED,
    // (String) o);
    // }
    // },
    /**
     * Uniform Type Identifier of the file. For example, a jpeg image file will
     * have a value of public.jpeg.
     */
    ContentTypeTree {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        try {
          String key = ((String) o).toUpperCase();
          key = key.replace('.', '_').replace('-', '_');
          final SpotlightContentType ct = SpotlightContentType.valueOf(key);
          final MetaType me = ct.getType();
          if(me != null) meta.setType(me);
          else {
            final MimeType mi = ct.getFormat();
            if(mi != null) meta.setFormat(mi);
          }
        } catch(final IllegalArgumentException ex) {
          Main.debug("SpotlightExtractor: unsupported ContentType found (%)",
              (String) o);
        }
      }
    },
        /**
     * Entity responsible for making contributions to the content of the
     * resource. Examples of a contributor include a person, an organization or
     * a service.
     */
    Contributors {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.CONTRIBUTOR, (String) o);
      }
    },
        /**
     * The full, publishable name of the country or primary location where the
     * intellectual property of the item was created, according to guidelines of
     * the provider.
     */
    Country {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.COUNTRY, (String) o);
      }
    },
        /** Description of the kind of item this file represents. */
    Description {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.DESCRIPTION, (String) o);
      }
    },
        /**
     * The duration, in seconds, of the content of the item. A value of 10.5
     * represents media that is 10 and 1/2 seconds long.
     */
    DurationSeconds {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        parseDuration(meta, MetaElem.DURATION, o);
      }
    },
        /** Mac OS X Finder comments for this item. */
    FinderComment {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.COMMENT, (String) o);
      }
    },
        // /** Date the file contents last changed. */
    // FSContentChangeDate {
    // @Override
    // void parse(final MetaStore meta, final Object o) {
    // if(check(o, Date.class)) meta.add(MetaElem.DATE_CONTENT_MODIFIED,
    // (String) o);
    // }
    // },
    // /** Date that the contents of the file were created. */
    // FSCreationDate {
    // @Override
    // public void parse(final MetaStore meta, final Object o) {
    // obj.dateEvent(DateField.DATE_CREATED, o);
    // }
    // },
    /** Group ID of the owner of the file. */
    FSOwnerGroupID {
      @Override
      void parse(final MetaStore meta, final Object o) {
        parseInt(meta, MetaElem.FS_OWNER_GROUP_ID, o);
      }
    },
        /** User ID of the owner of the file. */
    FSOwnerUserID {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        parseInt(meta, MetaElem.FS_OWNER_USER_ID, o);
      }
    },
        /**
     * Publishable entry providing a synopsis of the contents of the item. For
     * example, "Apple Introduces the iPod Photo".
     */
    Headline {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.HEADLINE, (String) o);
      }
    },
        /**
     * Formal identifier used to reference the resource within a given context.
     * For example, the Message-ID of a mail message.
     */
    Identifier {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.IDENTIFIER, (String) o);
      }
    },
        /**
     * Keywords associated with this file. For example, "Birthday", "Important",
     * etc.
     */
    Keywords {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.KEYWORD, (String) o);
      }
    },
        /**
     * Indicates the languages used by the item. The recommended best practice
     * for the values of this attribute are defined by RFC 3066.
     */
    Languages {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.LANGUAGE, (String) o);
      }
    },
        /**
     * Date and time that the file was last used. This value is updated
     * automatically by LaunchServices everytime a file is opened by double
     * clicking, or by asking LaunchServices to open a file.
     */
    LastUsedDate {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, Date.class)) meta.add(MetaElem.DATE_LAST_USED, (Date) o);
      }
    },
        /** Lyricist of the song in the audio file. */
    Lyricist {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.LYRICIST, (String) o);
      }
    },
        /**
     * Musical genre of the song or composition contained in the audio file. For
     * example: "Jazz", "Pop", "Rock", "Classical".
     */
    MusicalGenre {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        String g = (String) o;
        try {
          if(g.charAt(0) == '(') g = g.substring(1, g.length() - 2);
          final int genreId = Integer.parseInt(g);
          meta.add(MetaElem.GENRE, MP3Parser.getGenre(genreId));
        } catch(final NumberFormatException ex) {
          if(g.contains(",")) {
            final StringTokenizer tok = new StringTokenizer(g, ", ");
            while(tok.hasMoreTokens())
              meta.add(MetaElem.GENRE, tok.nextToken());
          } else {
            meta.add(MetaElem.GENRE, g);
          }
        }
      }
    },
        /** Number of pages in the document. */
    NumberOfPages {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        parseInt(meta, MetaElem.NUMBER_OF_PAGES, o);
      }
    },
        /**
     * Height, in pixels, of the contents. For example, the image height or the
     * video frame height.
     */
    PixelHeight {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        parseInt(meta, MetaElem.PIXEL_HEIGHT, o);
      }
    },
        /**
     * Width, in pixels, of the contents. For example, the image width or the
     * video frame width.
     */
    PixelWidth {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        parseInt(meta, MetaElem.PIXEL_WIDTH, o);
      }
    },
        /**
     * Publishers of the item. For example, a person, an organization, or a
     * service.
     */
    Publisher {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.PUBLISHER, (String) o);
      }
    },
        /** Recipients of this item. */
    Recipients {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.RECEIVER, (String) o);
      }
    },
        /**
     * Recording date of the song or composition. This is in contrast to
     * kMDItemContentCreationDate which, could indicate the creation date of an
     * edited or "mastered" version of the original art.
     */
    RecordingDate {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, Date.class)) meta.add(MetaElem.DATE_CREATED, (String) o);
      }
    },
        /**
     * Title of the item. For example, this could be the title of a document,
     * the name of an song, or the subject of an email message.
     */
    Title {
      @Override
      public void parse(final MetaStore meta, final Object o) {
        if(check(o, String.class)) meta.add(MetaElem.TITLE, (String) o);
      }
    };

    /**
     * Parses the data and fires parser events.
     * @param meta {@link MetaStore} to store the metadata information to
     * @param o the data to parse.
     */
    abstract void parse(final MetaStore meta, final Object o);

    @Override
    public String toString() {
      return "kMDItem" + name();
    }

    /**
     * Checks the type of the given object.
     * @param o the object to check.
     * @param c the data type.
     * @return true if the object is an instance of the given class, false
     *         otherwise.
     */
    boolean check(final Object o, final Class<?> c) {
      if(c.isInstance(o)) return true;
      Main.debug("SpotlightExtractor: wrong data type for attribute " +
          this.toString());
      return false;
    }

    /**
     * Converts the object to a Byte/Short/Integer/Long/Float or Double and adds
     * it to the metadata store.
     * @param meta the metadata store for the current file.
     * @param elem the corresponding metadata element for this object.
     * @param o the object to convert.
     */
    void parseInt(final MetaStore meta, final MetaElem elem, final Object o) {
      final Long value = long0(o);
      if(value != null) meta.add(elem, value);
    }

    /**
     * Converts the object to a Duration and adds it to the metadata store.
     * @param ms the metadata store for the current file.
     * @param e the corresponding metadata element for this object.
     * @param o the object to convert.
     */
    void parseDuration(final MetaStore ms, final MetaElem e, final Object o) {
      final Long value = long0(o);
      if(value != null) ms.add(e,
          ParserUtil.convertMsDuration((int) (value * 1000)));
    }

    /**
     * Returns the long value for an object.
     * @param o the object to parse.
     * @return the long value or <code>null</code> if the object can't be
     *         parsed.
     */
    private Long long0(final Object o) {
      long value;
      // most objects will be Integer, Long or Double
      if(o instanceof Integer) value = (Integer) o;
      else if(o instanceof Double) value = ((Double) o).longValue();
      else if(o instanceof Long) value = (Long) o;
      else if(o instanceof Short) value = (Short) o;
      else if(o instanceof Byte) value = (Byte) o;
      else if(o instanceof Float) value = ((Float) o).longValue();
      else if(o instanceof String) {
        final byte[] a = Token.token((String) o);
        int i = 0;
        final int len = a.length;
        while(i < len && a[i] >= '0' && a[i] <= '9')
          i++;
        value = Token.toLong(a, 0, i);
        if(value == Long.MIN_VALUE) {
          Main.debug("SpotlightExtractor: invalid value for int field: %",
              (String) o);
          return null;
        }
      } else {
        Main.debug("SpotlightExtractor: unsupported data type: %",
            o.getClass().getName());
        return null;
      }
      return value;
    }

    /**
     * Returns the enum constant for the string. Use this method instead of
     * {@link #valueOf(String)}!
     * @param n the name of the constant
     * @return the enum instance.
     */
    public static Item getValue(final String n) {
      return valueOf(n.substring(7));
    }
  }

  /** The parser instance. */
  private final NewFSParser parser;

  /**
   * Initializes the spotlight extractor.
   * @param fsParser the parser instance to fire events.
   */
  public SpotlightExtractor(final NewFSParser fsParser) {
    parser = fsParser;
  }

  /**
   * Queries spotlight for metadata items for the file and fires parser events.
   * @param file the file to search metadata for.
   * @throws IOException if any error occurs...
   */
  public void parse(final File file) throws IOException {
    final MetaStore meta = new MetaStore();
    final Map<String, Object> metadata = getMetadata(file.getAbsolutePath());
    if(metadata == null) return;
    for(final Entry<String, Object> e : metadata.entrySet()) {
      try {
        final Item item = Item.getValue(e.getKey());
        final Object val = e.getValue();
        if(val instanceof Object[]) {
          for(final Object o : (Object[]) val)
            item.parse(meta, o);
        } else item.parse(meta, val);
      } catch(final IllegalArgumentException ex) {
        // item is not in enum ...do nothing
      }
    }
    meta.write(parser);
  }

  /**
   * Native method for retrieving all available metadata for a file.
   * @param filename the path to the file.
   * @return map containing the queried metadata attributes or <code>null</code>
   *         if any error occurs.
   */
  private native Map<String, Object> getMetadata(final String filename);
}
