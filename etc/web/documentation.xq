import module namespace hp="http://www.basex.org/" at "main.xqm";

hp:write(
  for $section in $hp:doc/documentation/section
  return (
    <h2>{ data($section/@title) }</h2>,
    <p>{ $section/node() }</p>
  )
)
