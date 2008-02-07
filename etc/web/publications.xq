import module namespace hp="http://www.basex.org/" at "main.xqm";

let $cont :=
  let $pubs := $hp:doc/publications, $href := $pubs/@href
  for $pub in $pubs/*
  return <p>
  <b>{
    concat(string-join(
      for $id in $pub/authors/author
      let $author := replace($hp:doc/persons/person[@id = $id/@id]/name, ".* ", "")
      return $author, ", "), "; ", $pub/conference, ":")
  }</b>
  <h3><a href="{ concat($href, data($pub/at-url)) }">{ data($pub/title) }</a></h3>
  <i>{ $pub/abstract }</i>
  { if($pub/at-url) then <a href="{ concat($href, data($pub/at-url)) }">(paper)</a> else () }
  { if($pub/poster) then <a href="{ concat($href, data($pub/poster)) }">(poster)</a> else () }
  { if($pub/slides) then <a href="{ concat($href, data($pub/slides)) }">(slides)</a> else () }
  </p>

return hp:write($cont)
