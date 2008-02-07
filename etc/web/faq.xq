import module namespace hp="http://www.basex.org/" at "main.xqm";

declare function local:toc($entries, $n) {
  for $entry at $p in $entries
  let $title := data($entry/@title)
  return (
    let $nr := if($n) then concat($n, ".", $p) else $p,
      $e := <a href="#{ $nr }">{
        $nr, if($title) then $title else data($entry/question/node())
      }</a>
    return (
      if($n) then $e else <b>{ $e }</b>, <br/>, "
  ",
      if($title) then local:toc($entry/entry, $nr) else ()
    ), if($n) then () else (<br/>,"
  ")
  )
};

declare function local:content($entries, $n) {
  for $entry at $p in $entries
  let $title := data($entry/@title)
  return (
    let $nr := if($n) then concat($n, ".", $p) else $p,
        $e := ($nr, if($title) then $title else data($entry/question/node()))
    return (<a name="{ $nr }">{
      if($n) then <h3>{ $e }</h3> else <h2>{ $e }</h2>
    }</a>,"
  ",
    if($title) then local:content($entry/entry, $nr) else (),
    if($entry/question) then (<p>{ $entry/answer/node() }</p>, "
  ") else ()
    )
  )
};

hp:write(
  let $faq := $hp:doc/faq
  return (
    $faq/text/node(),
    local:toc($hp:doc/faq/entry, ""),
    local:content($hp:doc/faq/entry, ""),
    <br/>
  )
)
