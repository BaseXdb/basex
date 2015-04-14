(:~
 : Provides HTML components.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace html = 'dba/html';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(: Number formats. :)
declare variable $html:NUMBER := ('decimal','number', 'bytes');

(:~
 : Creates a checkbox.
 : @param  $name     name of checkbox
 : @param  $value    value
 : @param  $checked  checked state
 : @param  $label    label
 : @return checkbox
 :)
declare function html:checkbox(
  $name     as xs:string,
  $value    as xs:string,
  $checked  as xs:boolean,
  $label    as xs:string
) as node()+ {
  html:checkbox($label, map:merge((
    map { 'name':  $name },
    map { 'value': $value },
    if($checked) then map { 'checked': $checked } else ()
  )))
};

(:~
 : Creates a checkbox.
 : @param  $name  name of checkbox
 : @param  $map   additional attributes
 : @return checkbox
 :)
declare function html:checkbox(
  $label   as xs:string,
  $map     as map(*)
) as node()+ {
  element input {
    attribute type { "checkbox" },
    map:for-each($map, function($key, $value) { attribute { $key } { $value } })
  },
  text { $label }
};

(:~
 : Creates a button.
 : @param  $value  button value
 : @param  $label  label
 : @return button
 :)
declare function html:button(
  $value  as xs:string,
  $label  as xs:string
) as element(button) {
  html:button($value, $label, false())
};

(:~
 : Creates a button.
 : @param  $value    button value
 : @param  $label    label
 : @param  $confirm  confirm click
 : @return button
 :)
declare function html:button(
  $value    as xs:string,
  $label    as xs:string,
  $confirm  as xs:boolean
) as element(button) {
  html:button($value, $label, $confirm, ())
};

(:~
 : Creates a button.
 : @param  $value    button value
 : @param  $label    label
 : @param  $confirm  confirm click
 : @param  $class    button class
 : @return button
 :)
declare function html:button(
  $value    as xs:string,
  $label    as xs:string,
  $confirm  as xs:boolean,
  $class    as xs:string?
) as element(button) {
  element button {
    attribute type { 'submit' },
    attribute name { 'action' },
    attribute value { $value },
    $confirm[.] ! attribute onclick { "return confirm('Are you sure?');" },
    $class ! attribute class { . },
    $label
  }
};

(:~
 : Creates a property list.
 : @param  $props  properties
 : @return table
 :)
declare function html:properties(
  $props  as element()
) as element(table) {
  <table>{
    for $info in $props/*
    return (
      <tr>
        <th colspan='2' align='left'>
          <h3>{ upper-case($info/name()) }</h3>
        </th>
      </tr>,
      for $option in $info/*
      let $value := $option/data()
      return <tr>
        <td><b>{ upper-case($option/name()) }</b></td>
        <td>{
          if($value = 'true') then '✓'
          else if($value = 'false') then '–'
          else $value
        }</td>
      </tr>
    )
  }</table>
};

(:~
 : Creates a sorted table for the specified entries.
 : @param  $entries  table entries
 : @param  $headers  headers
 : @param  $buttons  buttons
 : @return table
 :)
declare function html:table(
  $entries  as element(e)*,
  $headers  as element()*,
  $buttons  as element(button)*
) as element()+ {
  html:table($entries, $headers, $buttons, map {})
};

(:~
 : Creates a sorted table for the specified entries.
 : @param  $entries  table entries
 : @param  $headers  headers
 : @param  $buttons  buttons
 : @param  $param    additional query parameters
 : @return table
 :)
declare function html:table(
  $entries  as element(e)*,
  $headers  as element()*,
  $buttons  as element(button)*,
  $param    as map(*)
) as element()+ {
  html:table($entries, $headers, $buttons, $param, ())
};

(:~
 : Creates a sorted table for the specified entries.
 : @param  $entries  table entries
 : @param  $headers  headers
 : @param  $buttons  buttons
 : @param  $param    additional query parameters
 : @param  $sort     sort key
 : @return table
 :)
declare function html:table(
  $entries  as element(e)*,
  $headers  as element()*,
  $buttons  as element(button)*,
  $param    as map(*),
  $sort     as xs:string?
) as element()+ {
  html:table($entries, $headers, $buttons, $param, $sort, ())
};

(:~
 : Creates a sorted table for the specified entries.
 : @param  $entries  table entries
 : @param  $headers  headers
 : @param  $buttons  buttons
 : @param  $param    additional query parameters
 : @param  $sort     sort key
 : @param  $link     link main entry
 : @return table
 :)
declare function html:table(
  $entries  as element(e)*,
  $headers  as element()*,
  $buttons  as element(button)*,
  $param    as map(*),
  $sort     as xs:string?,
  $link     as function(*)?
) as element()+ {
  if($buttons) then ($buttons, <br/>, <div class='small'/>) else (),
  if($entries) then (
    let $sort := if($sort = '') then $headers[1]/name() else $sort
    return <table>
      <tr>{
        for $header in $headers
        let $name := $header/name()
        let $value := upper-case($header/text())
        return element th {
          attribute align { if($header/@type = $html:NUMBER) then 'right' else 'left' },
          if(empty($sort) or $name = $sort) then (
            $value
          ) else (
            html:link($value, Request:path(), map:merge(($param, map { 'sort': $name })))
          )
        }
      }
      </tr>
      {
        let $entries := if(empty($sort)) then $entries else (
          let $sort := if($sort) then $sort else $headers[1]/name()
          let $header := $headers[name() eq $sort]
          let $desc := $header/@order = 'desc'
          let $order :=
            if($header/@type = $html:NUMBER) then (
              if($desc)
              then function($a) { 0 - number($a) }
              else function($a) { number($a) }
            ) else if($header/@type = 'time') then (
              if($desc)
              then function($a) { xs:time('00:00:00') - xs:time($a) }
              else function($a) { $a }
            ) else if($header/@type = 'date') then (
              if($desc)
              then function($a) { xs:date('0001-01-01') - xs:date($a) }
              else function($a) { $a }
            ) else if($header/@type = 'dateTime') then (
              if($desc)
              then function($a) { xs:dateTime('0001-01-01T00:00:00Z') - xs:dateTime($a) }
              else function($a) { $a }
            ) else (
              function($a) { $a }
            )
          return for $entry in $entries
                 let $key := $entry/@*[name() eq $sort]
                 order by $order($key) empty greatest collation '?lang=en'
                 return $entry
        )

        for $entry at $c in $entries
        return if($c <= $cons:MAX-ROWS) then (
          <tr>{
            for $header at $pos in $headers
            let $name := $header/name()
            let $type := $header/@type
            let $col := $entry/@*[name() = $name]
            let $value := $col/string()[.] ! (
              if($header/@type = 'bytes') then (
                try { prof:human(xs:integer(.)) } catch * { . }
              ) else if($header/@type = 'decimal') then (
                try { format-number(number(.), '#.00') } catch * { . }
              ) else if($header/@type = 'dateTime') then (
                let $zone := timezone-from-dateTime(current-dateTime())
                let $dt := fn:adjust-dateTime-to-timezone(xs:dateTime(.), $zone)
                return format-dateTime($dt, '[Y0000]-[M00]-[D00], [H00]:[m00]')
              )
              else .
            )

            return element td {
              attribute align { if($header/@type = $html:NUMBER) then 'right' else 'left' },
              if($pos = 1 and $buttons) then (
                <input type="checkbox" name="{ $name }" value="{ $col }" onClick="buttons()"/>
              ) else (),
              if($pos = 1 and exists($link)) then (
                html:link($value, $link($value), map:merge(($param, map { $name: $value })))
              ) else if($header/@type = 'id') then () else (
                $value
              )
            }
          }</tr>
        ) else if($c = $cons:MAX-ROWS + 1) then (
          <tr>
            <td>{
              if($buttons) then <input type="checkbox" disabled=""/> else ()
            }…</td>
          </tr>
        ) else ()
      }
    </table>
    (: , if($buttons) then (<div class='small'/>, $buttons, <br/>) else () :)
  ) else (
    <b>{ upper-case($headers[1]) }</b>
  )
};

(:~
 : Creates a singular/plural label.
 : @param  $items   items
 : @param  $labels  singular/plural label
 : @return label
 :)
declare function html:label(
  $items   as item()*,
  $labels  as xs:string+
) as xs:string {
  let $size := count($items)
  return (
    $size || ' ' || $labels[if(count($items) = 1) then 1 else 2] ||
    (if($size = 0) then '.' else ':')
  )
};

(:~
 : Focuses the specified field via Javascript.
 : @param  $element  element to be focused
 : @return script element
 :)
declare function html:focus(
  $element  as xs:string
) as element(script) {
  <script type="text/javascript">
    (function(){{ var u = document.getElementById('{ $element }'); u.focus(); u.select(); }})();
  </script>
};

(:~
 : Creates a link to the specified target.
 : @param  $text   link text
 : @param  $target target
 : @return link
 :)
declare function html:link(
  $text   as xs:string,
  $target as xs:string
) as element(a) {
  <a href="{ $target }">{ $text }</a>
};

(:~
 : Creates a link to the specified target.
 : @param  $text   link text
 : @param  $target target
 : @param  $params map with query parameters
 : @return link
 :)
declare function html:link(
  $text   as xs:string,
  $target as xs:string,
  $params as map(*)
) as element(a) {
  html:link($text, web:create-url($target, $params))
};
