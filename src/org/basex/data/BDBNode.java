package org.basex.data;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * BerkeleyDB Entity representing a single xml node.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Bastian Lemke
 */
@Entity
public class BDBNode {
  /** pre value (primary key). */
  @PrimaryKey
  private int mPre;
  /** unique id (secondary key). */
  @SecondaryKey(relate = Relationship.ONE_TO_ONE)
  private int mId;
  /** node kind. */
  private int mKind;
  /** name id of ELEM and ATTR nodes. */
  private int mNameId;
  /** text reference for DOC, TEXT, COM and PI nodes. */
  private int mTextId;
  /** true if text is inlined. */
  private boolean mTextInlined;
  /** number of attributes. */
  private int mNumAtts;
  /** distance to parent node. */
  private int mDist;
  /** number of descendants. */
  private int mSize;

  /** @return the pre value. */
  public int getPre() {
    return mPre;
  }

  /** @param pre the pre value to set. */
  public void setPre(final int pre) {
    this.mPre = pre;
  }

  /** @return the unique node id. */
  public int getId() {
    if(mId == -1) throw new UnsupportedOperationException("value not set.");
    return mId;
  }

  /** @param id the id to set. */
  public void setId(final int id) {
    this.mId = id;
  }

  /** @return the node kind. */
  public int getKind() {
    return mKind;
  }

  /** @param kind the kind to set. */
  public void setKind(final int kind) {
    this.mKind = kind;
  }

  /** @return the nameId of an ELEM or ATTR node. */
  public int getNameId() {
    return mNameId;
  }

  /** @param nameId the nameId to set. */
  public void setNameId(final int nameId) {
    this.mNameId = nameId;
  }

  /** @return the text reference of a TEXT, COMM, PI or DOC node. */
  public int getTextId() {
    return mTextId;
  }

  /** @param textId the textId to set. */
  public void setTextId(final int textId) {
    this.mTextId = textId;
  }

  /** @return true if text is inlined, false otherwise. */
  public boolean isTextInlined() {
    return mTextInlined;
  }

  /** @param textInlined the textInlined value to set. */
  public void setTextInlined(final boolean textInlined) {
    this.mTextInlined = textInlined;
  }

  /** @return the number of attributes. */
  public int getNumAtts() {
    return mNumAtts;
  }

  /** @param numAtts the number of attributes to set. */
  public void setNumAtts(final int numAtts) {
    this.mNumAtts = numAtts;
  }

  /** @return the distance to the parent node. */
  public int getDist() {
    return mDist;
  }

  /** @param dist the distance to set. */
  public void setDist(final int dist) {
    this.mDist = dist;
  }

  /** @return the number of descendants. */
  public int getSize() {
    return mSize;
  }

  /** @param size the size to set. */
  public void setSize(final int size) {
    this.mSize = size;
  }
}
