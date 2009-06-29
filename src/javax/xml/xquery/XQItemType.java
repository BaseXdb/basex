/*
 * Copyright 2003, 2004, 2005,  2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;
import javax.xml.namespace.QName;
import java.net.URI;

/** 
  * The <code>XQItemType</code> interface represents an item type as defined in
  * <a href="http://www.w3.org/TR/xquery"><i>XQuery 1.0: An XML Query language</i></a>.
  *
  * <br>
  *
  * The <code>XQItemType</code> extends the <code>XQSequenceType</code> but
  * restricts the occurrance indicator to be exactly one. This derivation allows passing  
  * an item type wherever a sequence type is expected, but not the other way.
  * 
  * The <code>XQItemType</code> interface contains methods to represent
  * information about the following aspects of an item type:
  * <ul>
  * <li>The kind of the item - one of <code>XQITEMKIND_*</code> constants</li>
  * <li>The base type of the item - one of the <code>XQBASETYPE_*</code>
  * constants. For atomic types this is the closest matching built-in XML
  * Schema type, for element and attributes the closest matching built-in XML
  * Schema type this node is based on.</li>
  * <li>Name of the node, if any</li>
  * <li>Type name, if any. If present, then also whether the typename is an anonymous type</li>
  * <li>XML Schema URI associated with the type, if any</li> 
  * <li>The nillability characteristics, if any</li>
  * </ul>
  * 
  * An instance of the <code>XQItemType</code> is a standalone
  * object that is independant of the <code>XQConnection</code> and
  * any XQuery static or dynamic context.
  */
public interface XQItemType extends XQSequenceType
{
  /** Some atomic type. */
  public static final int XQITEMKIND_ATOMIC = 1;  

  /** Attribute node */
  public static final int XQITEMKIND_ATTRIBUTE = 2; 

  /** Comment node */
  public static final int XQITEMKIND_COMMENT = 3;    

  /** Document type (the type information represents the type of the document element) */
  public static final int XQITEMKIND_DOCUMENT = 4;

  /** Document node containing a single element node as its child
      (type information represents type of the element child) */
  public static final int XQITEMKIND_DOCUMENT_ELEMENT = 5;

  /** Document node containing a single schema element node as its child
      (type information represents type of the schema element child) */
  public static final int XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT = 6;

  /** Element node */
  public static final int XQITEMKIND_ELEMENT = 7;

  /** Any kind of item */
  public static final int XQITEMKIND_ITEM = 8;  

  /** Some node type */
  public static final int XQITEMKIND_NODE = 9; 

  /** Processing instruction node */
  public static final int XQITEMKIND_PI = 10;   

  /** Text node */
  public static final int XQITEMKIND_TEXT = 11;    // node type

  /** Schema element node */
  public static final int XQITEMKIND_SCHEMA_ELEMENT = 12;

  /** Schema attribute node */
  public static final int XQITEMKIND_SCHEMA_ATTRIBUTE = 13;


  /** Represents the schema type xs:untyped */
  public static final int XQBASETYPE_UNTYPED = 1; 

  /** Represents the schema type xs:anyType */
  public static final int XQBASETYPE_ANYTYPE = 2;

 /** Represents the schema type <code>xs:anySimpleType</code> */
 public static final int XQBASETYPE_ANYSIMPLETYPE = 3;

 /** Represents the schema type <code>xs:anyAtomicType</code> */
 public static final int XQBASETYPE_ANYATOMICTYPE = 4;

 /** Represents the schema type <code>xs:untypedAtomic</code> */
 public static final int XQBASETYPE_UNTYPEDATOMIC = 5;

 /** Represents the schema type <code>xs:dayTimeDuration</code> */
 public static final int XQBASETYPE_DAYTIMEDURATION = 6;

 /** Represents the schema type <code>xs:yearMonthDuration</code> */
 public static final int XQBASETYPE_YEARMONTHDURATION = 7; 

 /** Represents the schema type <code>xs:anyURI</code> */
 public static final int XQBASETYPE_ANYURI = 8;

 /** Represents the schema type <code>xs:base64Binary</code> */
 public static final int XQBASETYPE_BASE64BINARY = 9;

 /** Represents the schema type <code>xs:boolean</code> */
 public static final int XQBASETYPE_BOOLEAN = 10;

 /** Represents the schema type <code>xs:date</code> */
 public static final int XQBASETYPE_DATE =11;

 /** Represents the schema type <code>xs:int</code>  */
 public static final int XQBASETYPE_INT =12;

 /** Represents the schema type <code>xs:integer</code>  */
 public static final int XQBASETYPE_INTEGER=13 ;

 /** Represents the schema type <code>xs:short</code> */
 public static final int XQBASETYPE_SHORT =14;

 /** Represents the schema type <code>xs:long</code> */
 public static final int XQBASETYPE_LONG =15;

 /** Represents the schema type <code>xs:dateTime</code> */
 public static final int XQBASETYPE_DATETIME =16;

 /** Represents the schema type <code>xs:decimal</code> */
 public static final int XQBASETYPE_DECIMAL =17;

 /** Represents the schema type <code>xs:double</code> */
 public static final int XQBASETYPE_DOUBLE =18;

 /** Represents the schema type <code>xs:duration</code> */
 public static final int XQBASETYPE_DURATION = 19;

 /** Represents the schema type <code>xs:float</code> */
 public static final int XQBASETYPE_FLOAT =20;

 /** Represents the schema type <code>xs:gDay</code> */
 public static final int XQBASETYPE_GDAY =21;

 /** Represents the schema type <code>xs:gMonth</code> */
 public static final int XQBASETYPE_GMONTH= 22;

 /** Represents the schema type <code>xs:gMonthDay</code> */
 public static final int XQBASETYPE_GMONTHDAY=23;

 /** Represents the schema type <code>xs:gYear</code> */
 public static final int XQBASETYPE_GYEAR =24;

 /** Represents the schema type <code>xs:gYearMonth</code> */
 public static final int XQBASETYPE_GYEARMONTH =25;

 /** Represents the schema type <code>xs:hexBinary</code> */
 public static final int XQBASETYPE_HEXBINARY =26;

 /** Represents the schema type <code>xs:NOTATION</code> */
 public static final int XQBASETYPE_NOTATION =27;

 /** Represents the schema type <code>xs:QName</code> */
 public static final int XQBASETYPE_QNAME  = 28; 

 /** Represents the schema type <code>xs:string</code> */
 public static final int XQBASETYPE_STRING =29;

 /** Represents the schema type <code>xs:time</code> */
 public static final int XQBASETYPE_TIME =30;

 /** Represents the schema type <code>xs:byte</code> */
 public static final int XQBASETYPE_BYTE =31;

 /** Represents the schema type <code>xs:nonPositiveInteger</code> */
 public static final int XQBASETYPE_NONPOSITIVE_INTEGER =32;

 /** Represents the schema type <code>xs:nonNegativeInteger</code> */
 public static final int XQBASETYPE_NONNEGATIVE_INTEGER =33;

 /** Represents the schema type <code>xs:negativeInteger</code> */
 public static final int XQBASETYPE_NEGATIVE_INTEGER =34;

 /** Represents the schema type <code>xs:positiveInteger</code> */
 public static final int XQBASETYPE_POSITIVE_INTEGER =35;

 /** Represents the schema type <code>xs:unsignedLong</code> */
 public static final int XQBASETYPE_UNSIGNED_LONG = 36;

 /** Represents the schema type <code>xs:unsignedInt</code> */
 public static final int XQBASETYPE_UNSIGNED_INT = 37;

 /** Represents the schema type <code>xs:unsignedShort</code> */
 public static final int XQBASETYPE_UNSIGNED_SHORT = 38;

 /** Represents the schema type <code>xs:unsignedByte</code> */
 public static final int XQBASETYPE_UNSIGNED_BYTE = 39;

 /** Represents the schema type <code>xs:normalizedString</code> */
 public static final int XQBASETYPE_NORMALIZED_STRING = 40;

 /** Represents the schema type <code>xs:token</code> */
 public static final int XQBASETYPE_TOKEN = 41;

 /** Represents the schema type <code>xs:language</code> */
 public static final int XQBASETYPE_LANGUAGE = 42;

 /** Represents the schema type <code>xs:Name</code> */
 public static final int XQBASETYPE_NAME = 43;

 /** Represents the schema type <code>xs:NCName</code> */
 public static final int XQBASETYPE_NCNAME = 44;

 /** Represents the schema type <code>xs:NMToken</code> */
 public static final int XQBASETYPE_NMTOKEN = 45;

 /** Represents the schema type <code>xs:ID</code> */
 public static final int XQBASETYPE_ID = 46;

 /** Represents the schema type <code>xs:IDREF</code> */
 public static final int XQBASETYPE_IDREF = 47;

 /** Represents the schema type <code>xs:ENTITY</code> */
 public static final int XQBASETYPE_ENTITY = 48;

 /** Represents the schema type <code>xs:IDREFS</code>.
   * Valid only if the item kind is 
   * <code>XQITEMKIND_ELEMENT</code>, <code>XQITEMKIND_DOCUMENT_ELEMENT</code>,
   * or <code>XQITEMKIND_ATTRIBUTE</code>  
   */
 public static final int XQBASETYPE_IDREFS = 49;

 /** Represents the schema type <code>xs:ENTITIES</code> */
 public static final int XQBASETYPE_ENTITIES = 50;

 /** Represents the schema type <code>xs:NMTOKENS</code> */
 public static final int XQBASETYPE_NMTOKENS = 51;

  /**
    * Returns the base type of the item. One of the <code>XQBASETYPE_*</code>
    * constants. 
    * <br/>
    * <br/>
    * XQJ defines a constant for each of the built-in schema
    * types defined in XML Schema. For atomic types this is the closest
    * matching built-in XML Schema type, for element and attributes the
    * closest matching built-in XML Schema type this node is based on.
    *
    * @return int      one of the <code>XQBASETYPE_*</code> constants
    *                  indicating the basic type of the item
    * @exception XQException     if the item kind is not one of:
    *                            <code>XQITEMKIND_DOCUMENT_ELEMENT</code>,
    *                            <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>,
    *                            <code>XQITEMKIND_ELEMENT</code>,
    *                            <code>XQITEMKIND_SCHEMA_ELEMENT</code>,
    *                            <code>XQITEMKIND_ATTRIBUTE</code>,
    *                            <code>XQITEMKIND_SCHEMA_ATTRIBUTE</code>, or
    *                            <code>XQITEMKIND_ATOMIC</code>
    */
  public int getBaseType() throws XQException;

  /**
    * Returns the kind of the item. 
    * One of the <code>XQITEMKIND_*</code> constants.
    *
    * @return int      one of the <code>XQITEMKIND_*</code> constants
    *                  indicating the basic kind  of the item
    */
  public int getItemKind();

  /**
    * Returns the occurrence indicator for the item type. This method 
    * will always return the value <code>XQSequenceType.OCC_EXACTLY_ONE</code>.
    *
    * @return int      indicating the occurrence indicator
    */
  public int getItemOccurrence();

  /**
    * Returns a human-readable implementation-defined 
    * string representation of the item type. 
    *
    * @return String              a string representation of the item type
    */
  public String toString();

  /**
    * Returns the name of the node in case the item kind is an 
    * <code>XQITEMKIND_DOCUMENT_ELEMENT</code>, <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>,
    * <code>XQITEMKIND_ELEMENT</code>, <code>XQITEMKIND_SCHEMA_ELEMENT</code>,
    * <code>XQITEMKIND_ATTRIBUTE</code>, or <code>XQITEMKIND_SCHEMA_ATTRIBUTE</code>.
    *
    * For example, in the case of  a type for <code>element "foo"</code>
    * this will return the <code>QName foo</code>. For wildcard entries a 
    * <code>null</code> value will be returned. 
    *
    * @return                    <code>QName</code> for the name of the element,
    *                            attribute, or document element node. <code>null</code>
    *                            if it is a wildcard
    * @exception XQException     if the item kind is not one of:
    *                            <code>XQITEMKIND_DOCUMENT_ELEMENT</code>,
    *                            <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>,
    *                            <code>XQITEMKIND_ELEMENT</code>, 
    *                            <code>XQITEMKIND_SCHEMA_ELEMENT</code>, 
    *                            <code>XQITEMKIND_ATTRIBUTE</code>, or
    *                            <code>XQITEMKIND_SCHEMA_ATTRIBUTE</code>
    *                                  
    */
  public QName getNodeName() throws XQException;

  /**
    * Returns the schema location URI of the schema that contains the item's
    * element or type definition. This method is implementation-definied 
    * and an implementation will return a <code>null</code> value if it does
    * not support retrieving the schema location URI.
    *
    * If the item corresponds to a validated global element in a schema, 
    * the result will be the schema location URI to the XMLSchema containing 
    * the element definition. Otherwise if the item is a schema validated 
    * node, the result will be the schema location URI of the XMLSchema 
    * containing the type definition of that node. If the item is not schema 
    * validated, the result is <code>null</code>
    * 
    *
    * @return                    <code>URI</code> representing the schema
    *                            location URI of the XMLSchema containing the
    *                            global element definition or the type definition
    *                            of the current item. <code>null</code> in case
    *                            the item is not schema validated or if the
    *                            implementation does not support retrieving the
    *                            schema URI.
    */
  public URI getSchemaURI();

  /**
    * Represents a type name (global or local). 
    * This can be used to represent specific type name such as,
    * element foo of type hatsize. The schema type name is represented as
    * a single <code>QName</code>. If the return type is an 
    * anonymous type, the actual <code>QName</code> value returned is implementation 
    * defined.
    *
    * @return                    the <code>QName</code> of the schema type in case of a
    *                            user defined or anonoymous types. For a built-in type,
    *                            returns a predefined type name as QName
    *                            (e.g.<code>xs:anyType</code>, <code>xs:decimal</code>,
    *                            etc). Cannot be <code>null</code>
    * @exception XQException     if the item kind is not one of:
    *                            <code>XQITEMKIND_DOCUMENT_ELEMENT</code>,
    *                            <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>,
    *                            <code>XQITEMKIND_ATOMIC</code>,
    *                            <code>XQITEMKIND_ELEMENT</code>,
    *                            <code>XQITEMKIND_SCHEMA_ELEMENT</code>, 
    *                            <code>XQITEMKIND_ATTRIBUTE</code>, or
    *                            <code>XQITEMKIND_SCHEMA_ATTRIBUTE</code>
    */
  public QName getTypeName() throws XQException;

   /**
    * Represents whether the item type is an anonymous type in the schema. 
    *
    * @return                   <code>true</code> if the item type is an anonymous
    *                           type in the schema, <code>false</code> otherwise
    */
   public boolean isAnonymousType();

  /**
    * Returns whether the element type is nillable or not. 
    *
    * @return                   <code>true</code> if the element type is nillable,
    *                           <code>false</code> otherwise
    */
  public boolean isElementNillable();

  /**
    * Returns the name of the processing instruction type. As such the item
    * kind of this <code>XQItemType</code> must be <code>XQITEMKIND_PI</code>.
    *
    * @return                   the name of the processing instruction type.
    *                           <code>null</code> if it is a wildcard
    * @exception XQException    if the item kind is not <code>XQITEMKIND_PI</code>
    */
  public String getPIName() throws XQException;

  /**
    * Compares the specified object with this item type for equality. The result
    * is <code>true</code> only if the argument is an item type object which
    * represents the same XQuery item type.
    *
    * <br>
    * <br>
    *
    * In order to comply with the general contract of <code>equals</code> and
    * <code>hashCode</code> across different implementations the following
    * algorithm must be used. Return <code>true</code> if and only if both
    * objects are <code>XQItemType</code> and:
    *
    * <ul>
    *   <li><code>getItemKind()</code> is equal</li>
    *   <li>if <code>getBaseType()</code> is supported for
    *       the item kind, it must be equal</li>
    *   <li>if <code>getNodeName()</code> is supported for
    *       the item kind, it must be equal</li>
    *   <li><code>getSchemaURI()</code> is equal</li>
    *   <li>if <code>getTypeName()</code> is supported for
    *       the item kind, it must be equal</li>
    *   <li><code>isAnonymousType()</code> is equal</li>
    *   <li><code>isElementNillable()</code> is equal</li>
    *   <li>if <code>getPIName()</code> is supported for the
    *       item kind, it must be equal</li>
    * </ul>
    *
    * @param o                an <code>XQItemType</code> object representing an XQuery
    *                         item type
    * @return                 <code>true</code> if the input item type object represents
    *                         the same XQuery item type, <code>false</code> otherwise
    */
  public boolean equals(Object o); 

  /**
    * Returns a hash code consistent with the definition of the equals method.
    *
    * <br>
    * <br>
    *
    * In order to comply with the general contract of <code>equals</code> and
    * <code>hashCode</code> across different implementations the following
    * algorithm must be used:
    * <pre>
    *  hashCode = this.getItemKind();
    *  if this.getSchemaURI != null
    *    hashCode = 31*hashCode + this.getSchemaURI().hashCode();
    *  if this.getBaseType() is supported for the item kind
    *    hashCode = 31*hashCode + this.getbaseType();
    *  if this.getNodeName () is supported for the item kind and
    *    this.getNodeName() != null
    *    hashCode = 31*hashCode + this.getNodeName().hashCode()
    *  if this.getTypeName () is supported for the item kind
    *    hashCode = 31*hashCode + this.getTypeName().hashCode();
    *  if this.getPIName () is supported for the item kind and
    *    this.getPIName () != null
    *    hashCode = 31*hashCode + this.getPIName().hashCode();
    * </pre>
    *
    * @return                 hash code for this item type
    */
  public int hashCode();

};
