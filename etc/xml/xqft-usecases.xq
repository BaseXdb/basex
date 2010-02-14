declare default element namespace "http://www.w3.org/1999/xhtml";
declare variable $specs := "xqft-usecases";
declare variable $sample := "xqft";
declare variable $stopwords := "xqft-sw.txt";

declare function local:getQueries() {
  for $ex in doc($specs)//div[@class = 'div3']
  let $header := normalize-space($ex/h4/text())

  return
    <query section="{ replace($header, " .*", "") }">
      <xquery>{ local:getQuery($ex, 'xquery') }</xquery>
      <xpath>{ local:getQuery($ex, 'xpath') }</xpath>
    </query>
};

declare function local:getQuery($node, $version) {
  let $string := concat('solution in ', $version, ':')
  let $qu := $node/p[lower-case(em) = $string]/following-sibling::*[1]/pre
  return replace(replace($qu, "http://bstore1.example.com/full-text.xml",
				$sample), "http://bstore1.example.com/StopWordList.xml", $stopwords)
};

<results> {
  for $query in local:getQueries()
  let $xquery := $query/xquery/text()
  return
    <result section="{ $query/@section }">
    <query>{
      $xquery
    }</query>{
      try {
        <output>{ basex:eval($xquery) }</output>
      } catch *($error) {
        <error>{ $error }</error>
			}
    }
    </result>
} </results>
