import module namespace hp="http://www.basex.org/" at "hp.xqm"; 

declare variable $file := "publications";
declare variable $title := "Publications";
declare variable $link := "http://www.inf.uni-konstanz.de/dbis/publications/download/";
declare variable $pubs := (
  "Holupirek, Grün, Scholl; BTW 2007, GI",
  "Melting Pot XML - Bringing File Systems and Databases one step closer",
  "Evaluates the basic idea to regard a file system as semi-structured data and to implement file system operations using XPath/XQuery.",
  "HGS:BTW2007.pdf", "slides", "HGS:BTW2007:slides.pdf",

  "Grün, Holupirek, Scholl; BTW 2007 Demo Panel, GI",
  "Visually Exploring and Querying XML with BaseX",
  "BaseX offers an intuitive, visual access to semi-structured data. Import your file system and browse it on a treemap or try one of the several others views (zoomable table, file explorer that navigates into the file itself ...) It thus demonstrates the efficiency of the underlying storage.",
  "GHS:BTW07.pdf", "poster", "GHS:BTW07:poster.pdf",

  "Grün, Holupirek, Kramis, Scholl, Waldvogel; EXPDB 2006, ACM, June 2006",
  "Pushing XPath Accelerator to its limits",
  "First considerations and performance results for native XML storage layouts based on the XPathAccelerator encoding scheme.",
  "GHKSW:EXPDB06.pdf", "slides", "GHKSW:EXPDB06:slides.pdf"
);

declare variable $cont := 
  <div id="main">
  <h1>Publications</h1>{
  for $i in 1 to count($pubs) idiv 6
  let $j := $i * 6 - 5
  return (<p><b>{ $pubs[$j] }:</b>
    <h3><a href="{ $link }{ $pubs[$j + 3] }">{ $pubs[$j + 1] }</a></h3>
    <i>{ $pubs[$j + 2] }</i>
    <a href="{ $link }{ $pubs[$j + 3] }">(paper)</a>
    { if($pubs[$j + 4]) then <a href="{ $link }{ $pubs[$j + 5] }">({ $pubs[$j + 4] })</a> else () }
    </p>)
  }
  </div>
;

hp:print($title, $file, $cont)
