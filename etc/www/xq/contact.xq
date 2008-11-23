import module namespace hp="http://www.basex.org/" at "main.xqm";

hp:write(
  for $section in $hp:doc/contact/section
  return (
    if($section/@desc) then <h2>{ data($section/@desc) }</h2> else (),
    if($section/text()) then <p>{ $section/node() }</p> else (),
    if($section/@class) then
      <p><ul>{
        for $person in $hp:doc/persons/person[@class = $section/@class]
        return
          <li><b>{ $person/name }</b> { $person/job/text() }</li>
      }</ul></p>

      else ()
  )

  (:
  <p>{ $hp:doc/contact/node() }</p>,
  <p>{ $hp:doc/students/node() }
  </p>,
  <p>{ $hp:doc/alumni/node() }
  </p>,

  for $section in $hp:doc/visual/section
  return (
    <h2>{ data($section/@desc) }</h2>,
    if($section/files) then
    <table cellspacing='0' cellpadding='0' border='0'>{
      let $path := data($section/files/@path)
      for $file in $section/files/file
      return <tr><td width="240">
        <img src="gfx/download.gif"/>
        <a href="{ concat($path, $file/@link) }">{ data($file/@name) }</a>
      </td><td>{ data($file/@desc) }
      </td></tr>
    }</table> else <p>{ $section/node() }</p>
  ):)
)
