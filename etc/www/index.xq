import module namespace hp="http://www.basex.org/" at "main.xqm";

hp:write((
  $hp:doc/home/node(),
  <h2>News</h2>,
  for $item in $hp:doc/news/news-item
  return (<h3>[{ data($item/@date) }]
               { data($item/@title) }</h3>,
          <p>{ $item/node() }</p>)
))
