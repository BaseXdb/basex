package org.basex.query;

import java.io.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;
import org.basex.core.*;
import org.basex.io.*;

public class LuceneQuery {

  public static int[] query(String query, Context context) throws ParseException, IOException{

      IOFile indexpath = context.globalopts.dbpath();
      String dbname = context.data().meta.name;

      StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
      Query q = new QueryParser(Version.LUCENE_4_9, "text", analyzer).parse(query);

      int hitsPerPage = 10;


      IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexpath.toString() + "/" + dbname + "/" + "LuceneIndex")));
      IndexSearcher searcher = new IndexSearcher(reader);
      TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
      searcher.search(q, collector);
      ScoreDoc[] hits = collector.topDocs().scoreDocs;

      int[] pres = new int[hits.length];
      System.out.println("hits: " + hits.length);
      for(int i=0;i<hits.length;++i) {
        int docId = hits[i].doc;
        Document d = searcher.doc(docId);
        Number a = d.getField("pre").numericValue();
        pres[i] = (Integer) a;
      }

      reader.close();


      return pres;
  }

}
