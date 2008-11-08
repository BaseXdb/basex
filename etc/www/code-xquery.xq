import module namespace hp="http://www.basex.org/" at "main.xqm";

hp:write(
  <pre>{
  $hp:doc/code[@name = $hp:link]
  }</pre>
)
