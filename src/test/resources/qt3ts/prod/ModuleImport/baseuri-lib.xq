(: Name: baseuri-lib :)
(: Description: A library declaring a base URI and exporting a node. :)
(: Author: Oliver Hallam :)
(: Date: 2009-05-20 :)

module namespace lib="http://www.xqsharp.com/test/baseuri-lib";

declare base-uri "http://www.example.org/correct/";

declare variable $lib:node := <a><b/></a>;
