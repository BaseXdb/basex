package org.basex.build.fs.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.xml.datatype.XMLGregorianCalendar;

import org.basex.BaseX;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.MP3Parser;
import org.basex.build.fs.util.Metadata.DateField;
import org.basex.build.fs.util.Metadata.IntField;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;
import org.basex.build.fs.util.Metadata.StringField;
import org.basex.util.LibraryLoader;
import org.basex.util.Token;

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
      void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.dateEvent(DateField.DATE_ATTRIBUTE_MODIFIED, o);
      }
    },
    /**
     * Title for the collection containing this item. This is analogous to a
     * record label or photo album.
     */
    Album {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.ALBUM, o);
      }
    },
    /** Track number of a song or composition when it is part of an album. */
    AudioTrackNumber {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.intEvent(IntField.TRACK, o);
      }
    },
    /** The author of the contents of the file. */
    Authors {
      @Override
      void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.CREATOR, o);
      }
    },
    /**
     * Identifies city of origin according to guidelines established by the
     * provider. For example, "New York", "Cupertino", or "Toronto".
     */
    City {
      @Override
      void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.SPATIAL, o);
      }
    },
    /**
     * A comment related to the file. This comment is not displayed by the
     * Finder.
     */
    Comment {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.DESCRIPTION, o);
      }
    },
    /** Composer of the song in the audio file. */
    Composer {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.CONTRIBUTOR, o);
      }
    },
    /** The date and time that the content was created. */
    ContentCreationDate {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.dateEvent(DateField.DATE_CREATED, o);
      }
    },
    // /** Date and time when the content of this item was modified. */
    // ContentModificationDate {
    // @Override
    // public void parse(final SpotlightExtractor obj, final Object o) throws
    // IOException {
    // obj.dateEvent(DateField.DATE_CONTENT_MODIFIED, o);
    // }
    // },
    /**
     * Uniform Type Identifier of the file. For example, a jpeg image file will
     * have a value of public.jpeg.
     */
    ContentTypeTree {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        try {
          String key = ((String) o).toUpperCase();
          key = key.replace('.', '_').replace('-', '_');
          obj.contentTypeEvent(SpotlightContentType.valueOf(key));
        } catch(final IllegalArgumentException ex) {
          BaseX.debug("SpotlightExtractor: unsupported ContentType found (%)",
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
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.CONTRIBUTOR, o);
      }
    },
    /**
     * The full, publishable name of the country or primary location where the
     * intellectual property of the item was created, according to guidelines of
     * the provider.
     */
    Country {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.SPATIAL, o);
      }
    },
    /** Description of the kind of item this file represents. */
    Description {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.DESCRIPTION, o);
      }
    },
    /**
     * The duration, in seconds, of the content of the item. A value of 10.5
     * represents media that is 10 and 1/2 seconds long.
     */
    DurationSeconds {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.durationEvent(o);
      }
    },
    /** Mac OS X Finder comments for this item. */
    FinderComment {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.DESCRIPTION, o);
      }
    },
    // /** Date the file contents last changed. */
    // FSContentChangeDate {
    // @Override
    // void parse(final SpotlightExtractor obj, final Object o)
    // throws IOException {
    // obj.dateEvent(DateField.DATE_CONTENT_MODIFIED, o);
    // }
    // },
    // /** Date that the contents of the file were created. */
    // FSCreationDate {
    // @Override
    // public void parse(final SpotlightExtractor obj, final Object o)
    // throws IOException {
    // obj.dateEvent(DateField.DATE_CREATED, o);
    // }
    // },
    /** Group ID of the owner of the file. */
    FSOwnerGroupID {
      @Override
      void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.intEvent(IntField.FS_OWNER_GROUP_ID, o);
      }
    },
    /** User ID of the owner of the file. */
    FSOwnerUserID {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.intEvent(IntField.FS_OWNER_USER_ID, o);
      }
    },
    // /** Size, in bytes, of the file on disk. */
    // FSSize {
    // @Override
    // public void parse(final SpotlightExtractor obj, final Object o)
    // throws IOException {
    // obj.intEvent(IntField.FS_SIZE, o);
    // }
    // },
    /**
     * Publishable entry providing a synopsis of the contents of the item. For
     * example, "Apple Introduces the iPod Photo".
     */
    Headline {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.TITLE, o);
      }
    },
    /**
     * Formal identifier used to reference the resource within a given context.
     * For example, the Message-ID of a mail message.
     */
    Identifier {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.IDENTIFIER, o);
      }
    },
    /**
     * Keywords associated with this file. For example, "Birthday", "Important",
     * etc.
     */
    Keywords {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.KEYWORD, o);
      }
    },
    /**
     * Indicates the languages used by the item. The recommended best practice
     * for the values of this attribute are defined by RFC 3066.
     */
    Languages {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.LANGUAGE, o);
      }
    },
    /**
     * Date and time that the file was last used. This value is updated
     * automatically by LaunchServices everytime a file is opened by double
     * clicking, or by asking LaunchServices to open a file.
     */
    LastUsedDate {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.dateEvent(DateField.DATE_LAST_USED, o);
      }
    },
    /** Lyricist of the song in the audio file. */
    Lyricist {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.CONTRIBUTOR, o);
      }
    },
    /**
     * Musical genre of the song or composition contained in the audio file. For
     * example: "Jazz", "Pop", "Rock", "Classical".
     */
    MusicalGenre {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        final StringField field = StringField.GENRE;
        String g = (String) o;
        try {
          if(g.charAt(0) == '(') g = g.substring(1, g.length() - 2);
          final int genreId = Integer.parseInt(g);
          obj.stringEvent(field, new String(MP3Parser.getGenre(genreId)));
        } catch(final NumberFormatException ex) {
          if(g.contains(",")) {
            final StringTokenizer tok = new StringTokenizer(g, ", ");
            while(tok.hasMoreTokens())
              obj.stringEvent(field, tok.nextToken());
          } else {
            obj.stringEvent(field, g);
          }
        }
      }
    },
    /** Number of pages in the document. */
    NumberOfPages {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.intEvent(IntField.NUMBER_OF_PAGES, o);
      }
    },
    /**
     * Height, in pixels, of the contents. For example, the image height or the
     * video frame height.
     */
    PixelHeight {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.intEvent(IntField.PIXEL_HEIGHT, o);
      }
    },
    /**
     * Width, in pixels, of the contents. For example, the image width or the
     * video frame width.
     */
    PixelWidth {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.intEvent(IntField.PIXEL_WIDTH, o);
      }
    },
    /**
     * Publishers of the item. For example, a person, an organization, or a
     * service.
     */
    Publisher {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.PUBLISHER, o);
      }
    },
    /** Recipients of this item. */
    Recipients {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.RECEIVER, o);
      }
    },
    /**
     * Recording date of the song or composition. This is in contrast to
     * kMDItemContentCreationDate which, could indicate the creation date of an
     * edited or "mastered" version of the original art.
     */
    RecordingDate {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.dateEvent(DateField.DATE_CREATED, o);
      }
    },
    // /**
    // * Contains a text representation of the content of the document.
    // */
    // TextContent {
    // @Override
    // public void parse(final SpotlightExtractor obj, final Object o)
    // throws IOException {
    // assert o instanceof String;
    // String s = (String) o;
    // obj.parser.textContent(-1, s.length(), s, false);
    // }
    // },
    /**
     * Title of the item. For example, this could be the title of a document,
     * the name of an song, or the subject of an email message.
     */
    Title {
      @Override
      public void parse(final SpotlightExtractor obj, final Object o)
          throws IOException {
        obj.stringEvent(StringField.TITLE, o);
      }
    };

    /**
     * Parses the data and fires parser events.
     * @param obj the {@link SpotlightExtractor} object to fire events from.
     * @param o the data to parse.
     * @throws IOException if any error occurs while writing to the parser.
     */
    abstract void parse(final SpotlightExtractor obj, final Object o)
        throws IOException;

    @Override
    public String toString() {
      return "kMDItem" + name();
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
  final NewFSParser parser;
  /** Metadata object. */
  final Metadata meta;

  /**
   * Initializes the spotlight extractor.
   * @param fsParser the parser instance to fire events.
   */
  public SpotlightExtractor(final NewFSParser fsParser) {
    parser = fsParser;
    meta = new Metadata();
  }

  /**
   * Queries spotlight for metadata items for the file and fires parser events.
   * @param file the file to search metadata for.
   * @throws IOException if any error occurs...
   */
  public void parse(final File file) throws IOException {
    final Map<String, Object> metadata = getMetadata(file.getAbsolutePath());
    if(metadata == null) return;
    for(final Entry<String, Object> e : metadata.entrySet()) {
      try {
        final Item item = Item.getValue(e.getKey());
        final Object val = e.getValue();
        if(val instanceof Object[]) {
          for(final Object o : (Object[]) val) {
            item.parse(this, o);
          }
        } else {
          item.parse(this, val);
        }
      } catch(final IllegalArgumentException ex) {
        // item is not in enum ...do nothing
      }
    }
  }

  /**
   * Converts the object to the correct xml format and fires an event.
   * @param field the {@link DateField}.
   * @param o the object containing the date to convert.
   * @throws IOException if any error occurs while writing to the parser.
   */
  void dateEvent(final DateField field, final Object o) throws IOException {
    assert o instanceof Date;
    XMLGregorianCalendar gcal = null;
    gcal = ParserUtil.convertDate((Date) o);
    if(gcal == null) return;
    meta.setDate(field, gcal);
    parser.metaEvent(meta);
  }

  /**
   * Converts the object to a Byte/Short/Integer/Long/Float or Double and fires
   * an event.
   * @param field the {@link IntField}.
   * @param o the object to convert.
   * @throws IOException if any error occurs while writing to the parser.
   */
  void intEvent(final IntField field, final Object o) throws IOException {
    long value;
    // most objects will be Integer, Long or Double
    if(o instanceof Integer) value = (Integer) o;
    else if(o instanceof Long) value = (Long) o;
    else if(o instanceof Double) value = ((Double) o).longValue();
    else if(o instanceof Short) value = (Short) o;
    else if(o instanceof Byte) value = (Byte) o;
    else if(o instanceof Float) value = ((Float) o).longValue();
    else if(o instanceof String) {
      final byte[] a = ((String) o).getBytes();
      int i = 0;
      final int len = a.length;
      while(i < len && a[i] >= '0' && a[i] <= '9')
        i++;
      value = Token.toLong(a, 0, i);
      if(value == Long.MIN_VALUE) {
        BaseX.debug("SpotlightExtractor: invalid value for int field: %",
            (String) o);
        return;
      }
    } else {
      BaseX.debug("SpotlightExtractor: unsupported data type: %",
          o.getClass().getName());
      return;
    }
    if(value > Integer.MAX_VALUE) meta.setLong(field, value);
    else if(value > Short.MAX_VALUE) meta.setInt(field, (int) value);
    else meta.setShort(field, (short) value);
    parser.metaEvent(meta);
  }

  /**
   * Converts the object to a String and fires an event.
   * @param field the {@link StringField}.
   * @param o the object to convert.
   * @throws IOException if any error occurs while writing to the parser.
   */
  void stringEvent(final StringField field, final Object o) throws IOException {
    assert o instanceof String;
    meta.setString(field, (String) o, true);
    parser.metaEvent(meta);
  }

  /**
   * Converts an object that contains a seconds value to a XML duration object
   * and fires an event.
   * @param o the object to convert.
   * @throws IOException if any error occurs while writing to the parser.
   */
  void durationEvent(final Object o) throws IOException {
    double value;
    // most objects will be Double
    if(o instanceof Double) value = (Double) o;
    else if(o instanceof Integer) value = (Integer) o;
    else if(o instanceof Long) value = (Long) o;
    else if(o instanceof Short) value = (Short) o;
    else if(o instanceof Byte) value = (Byte) o;
    else if(o instanceof Float) value = (Float) o;
    else if(o instanceof String) {
      try {
        value = Double.parseDouble((String) o);
      } catch(final NumberFormatException e) {
        BaseX.debug("SpotlightExtractor: invalid value for int field: %",
            (String) o);
        return;
      }
    } else {
      BaseX.debug("SpotlightExtractor: unsupported data type: %",
          o.getClass().getName());
      return;
    }
    meta.setDuration(ParserUtil.convertMsDuration((int) (value * 1000)));
    parser.metaEvent(meta);
  }

  /**
   * Sets the MIME type for the current item.
   * @param ct the spotlight content type
   * @throws IOException if any error occurs while writing to the parser.
   */
  void contentTypeEvent(final SpotlightContentType ct) throws IOException {
    final MetaType me = ct.getType();
    if(me != null) parser.metaEvent(meta.setMetaType(me));
    else {
      final MimeType mi = ct.getFormat();
      if(mi != null) parser.metaEvent(meta.setMimeType(mi));
    }
  }

  /**
   * Native method for retrieving all available metadata for a file.
   * @param filename the path to the file.
   * @return map containing the queried metadata attributes or <code>null</code>
   *         if any error occurs.
   */
  private native Map<String, Object> getMetadata(final String filename);
}
