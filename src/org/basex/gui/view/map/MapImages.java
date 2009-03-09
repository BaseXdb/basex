package org.basex.gui.view.map;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.util.Action;
import org.basex.util.Token;

/**
 * This is an cache for images which have been retrieved from the file system.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class MapImages {
  /** Maximum number of cached images. */
  private static final int MAXNR = 5000;
  /** Reference to the treemap panel. */
  MapView view;
  /** Image cache. */
  BufferedImage[] imgs = new BufferedImage[MAXNR];
  /** Maximum image size. */
  boolean[] imgmax = new boolean[MAXNR];
  /** Image id cache. */
  int[] imgid = new int[MAXNR];
  /** Pointer to next cached image. */
  int imgc;

  /** Image id cache. */
  int[] idCache = new int[MAXNR];
  /** Image width cache. */
  int[] wCache = new int[MAXNR];
  /** Image height cache. */
  int[] hCache = new int[MAXNR];
  /** Cache counter. */
  int loaderC;


  /**
   * Default Constructor.
   * @param panel reference to the treemap
   */
  MapImages(final MapView panel) {
    view = panel;
  }

  /**
   * Resets the image cache.
   */
  void reset() {
    stop();
    for(int i = 0; i < MAXNR; i++) imgs[i] = null;
  }

  /**
   * Returns image with specified id or null if none was found.
   * @param id cache to be found
   * @return cached image
   */
  Image get(final int id) {
    for(int i = 0; i < MAXNR; i++) if(imgid[i] == id) return imgs[i];
    return null;
  }

  /**
   * Adds an image to be loaded into the cache with the specified size.
   * @param id picture id
   * @param w width
   * @param h height
   */
  void add(final int id, final int w, final int h) {
    // add image to the download stack
    if(loaderC < 0) loaderC = 0;
    else if(loaderC == MAXNR) loaderC--;

    idCache[loaderC] = id;
    wCache[loaderC] = w;
    hCache[loaderC++] = h;
  }

  /**
   * Loads the currently cached images.
   */
  void load() {
    if(!dl.running() && loaderC > 0) dl.execute();
  }
  
  /** Download thread. */
  final Action dl = new Action() {
    public void run() {
      while(loaderC > 0) {
        final int id = idCache[--loaderC];
        int ww = wCache[loaderC];
        int hh = hCache[loaderC];

        // find new image id in cached images
        int ic = -1;
        while(++ic < MAXNR && imgid[ic] != id);

        // check if the image exists already, or if the existing version
        // is bigger than the new one
        if(ic != MAXNR && (imgmax[ic] || imgs[ic].getWidth() + 1 >= ww ||
            imgs[ic].getHeight() + 1 >= hh)) continue;

        try {
          // database closed - quit
          final Data data = view.gui.context.data();
          if(data == null) {
            loaderC = 0;
            return;
          }
          
          // load image and wait until it's done
          final File f = new File(Token.string(data.fs.path(id)));
          BufferedImage image = ImageIO.read(f);
          
          // calculate optimal image size
          final double iw = image.getWidth();
          final double ih = image.getHeight();
          final double min = Math.min(ww / iw, hh / ih);

          // create scaled image instance
          if(min < 1) {
            ww = (int) (iw * min);
            hh = (int) (ih * min);

            final BufferedImage bi = new BufferedImage(ww, hh,
                BufferedImage.TYPE_INT_BGR);
            bi.createGraphics().drawImage(image, 0, 0, ww, hh, null);
            image = bi;
          }

          // cache image
          if(ic == MAXNR) {
            ic = imgc;
            imgid[ic] = id;
            imgc = (ic + 1) % MAXNR;
          }
          imgs[ic] = image;
          imgmax[ic] = min >= 1;

          if((loaderC & 5) == 0 && !view.gui.painting) paint();
        } catch(final IOException ex) {
          BaseX.debug(ex);
        }
      }
      paint();
    }
  };
  
  
  /**
   * Refreshes the map.
   */
  void paint() {
    // determine maximum cache size, depending on window size
    final Dimension size = view.gui.getSize();
    final Runtime rt = Runtime.getRuntime();
    final long max = Math.max(size.width * size.height * 10L,
        rt.maxMemory() / 5);
    
    // remove images if pixel limit is reached
    int imgSize = 0;
    for(int j = (imgc == 0 ? MAXNR : imgc) - 1; j != imgc;) {
      if(imgs[j] != null) {
        if(imgSize < max) {
          imgSize += imgs[j].getWidth() * imgs[j].getHeight();
        } else {
          imgid[j] = 0;
          imgs[j] = null;
        }
      }
      j = (j == 0 ? MAXNR : j) - 1;
    }
    // (might get slow for large maps)
    view.refreshLayout();
  }

  /**
   * Stops the loading thread.
   */
  void stop() {
    loaderC = 0;
  }
}
