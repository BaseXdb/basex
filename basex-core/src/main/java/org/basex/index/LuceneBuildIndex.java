package org.basex.index;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.data.Data;
import org.basex.io.*;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryProcessor;
import org.basex.query.value.Value;
import org.basex.query.value.item.Item;
import org.basex.query.value.node.DBNode;
import org.basex.util.Token;



public class LuceneBuildIndex extends DefaultHandler {
  private StringBuilder elementBuffer = new StringBuilder();
  private Map<String,String> attributeMap = new HashMap<String,String> ();
  private Document doc;

  public Document getDocument(InputStream is)
      throws Exception {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    try {
      SAXParser parser = spf.newSAXParser();
      parser.parse(is, this);
    } catch (Exception e) {
      throw new Exception(
          "Cannot parse XML document", e);
    }
    return doc;
  }

  public void startDocument() {
    doc = new Document();
  }

  public void startElement(String uri, String localName, String qName, Attributes atts)
      throws SAXException {
    elementBuffer.setLength(0);
    attributeMap.clear();
    int numAtts = atts.getLength();
    if (numAtts > 0) {
      for (int i = 0; i < numAtts; i++) {
        attributeMap.put(atts.getQName(i), atts.getValue(i));
            }
        }
    }


  public void characters(char[] text, int start, int length) {
    elementBuffer.append(text, start, length);
  }


  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    doc.add(new TextField(qName, elementBuffer.toString(), Field.Store.YES));

  }

  public static void luceneIndex(String name, Context context) throws Exception {

    LuceneBuildIndex handler = new LuceneBuildIndex();
    IOFile indexpath = context.globalopts.dbpath();
    String dbname = context.data().meta.name;

    StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
    File indexFile = new File(indexpath.toString() + "/" + dbname + "/"+ "LuceneIndex");
    indexFile.mkdir();

    Directory index = FSDirectory.open(indexFile);
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
    IndexWriter writer = new IndexWriter(index, config);


    Data data = context.data();
    int size = data.meta.size;

    for(int pre = 0; pre < size; pre++) {
      // reset output stream and serialize next item
      if(data.kind(pre) == Data.TEXT) {
        int parentpre = data.parent(pre, Data.TEXT);
        //byte[] elem = data.name(parentpre, Data.ELEM);
        byte[] text = data.text(pre, true);

        Document doc = new Document();
        doc.add(new IntField("pre", pre, Field.Store.YES));
        doc.add(new TextField("text", Token.string(text), Field.Store.YES));
        writer.addDocument(doc);
      }
    }


    //DBNode node = new DBNode(context.data(), 0);
    //Document doc = handler.getDocument(new ByteArrayInputStream(node.serialize().toString().getBytes()));
    //writer.addDocument(doc);

    //System.out.println(doc);

    writer.close();

  }

}