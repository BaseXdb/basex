(:~
 : Provides HTML components.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace html = 'dba/html';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(: Number formats. :)
declare variable $html:NUMBER := ('decimal', 'number', 'bytes');

(:~
 : Creates an option checkbox.
 : @param  $value  value
 : @param  $label  label
 : @param  $opts   checked options
 : @return checkbox
 :)
declare function html:option(
  $value  as xs:string,
  $label  as xs:string,
  $opts   as xs:string*
) as node()+ {
  html:checkbox("opts", $value, $opts = $value, $label)
};

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
 : @param  $label  label of checkbox
 : @param  $map    additional attributes
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
  text { $label },
  element br { }
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
          if($value = 'true') then '&#x2713;'
          else if($value = 'false') then '–'
          else $value
        }</td>
      </tr>
    )
  }</table>
};

(:~
 : Creates a table for the specified entries.
 : * The table format is specified by the table headers:
 :   * The element names serve as column keys.
 :   * The string values are the header labels.
 :   * The 'type' attribute defines how the values are formatted and sorted:
 :     * 'number': sorted as numbers
 :     * 'decimal': sorted as numbers, output with two decimal digits
 :     * 'bytes': sorted as numbers, output in a human-readable format
 :     * 'date', 'dateTime': sorted and output as dates
 :     * 'id': suppressed (only used for creating checkboxes)
 :     * otherwise, sorted and output as strings
 :   * The 'order' attribute defines how sorted values will be ordered:
 :     * 'desc': descending order
 :     * otherwise, ascending order
 :   * The 'main' attribute indicates which column is the main column
 : * The supplied table rows are supplied as elements. Values are contained in attributes; their
 :   names represents the column key.
 : * Supplied buttons will placed on top of the table.
 : * Query parameters will be included in table links.
 : * The options argument can have the following keys:
 :   * 'sort': argument contains the key of the ordered column.
 :   * 'link': argument contains a function for generating a link reference.
 :   * 'page': currently displayed page
 :   * 'count': maximum number of results (if not set, )
 :
 : @param  $entries  table entries: values are represented via attributes
 : @param  $headers  table headers:
 : @param  $buttons  buttons
 : @param  $param    additional query parameters
 : @param  $options  additional options
 : @return table
 :)
declare function html:table(
  $headers  as element()*,
  $rows     as element(row)*,
  $buttons  as element(button)*,
  $param    as map(*),
  $options  as map(*)
) as element()+ {
  if($buttons) then ($buttons, <br/>, <div class='small'/>) else (),

  let $sort := $options?sort
  let $page := $options?page
  let $link := $options?link
  let $count := if($sort) then () else $options?count

  let $sort-key := head(($sort[.], $headers[1]/name()))
  let $all-entries := if(not($sort)) then $rows else (
    let $header := $headers[name() eq $sort-key]
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
    return
      for $row in $rows
      order by string($row/@*[name() = $sort-key])[.] ! $order(.)
        empty greatest collation '?lang=en'
      return $row
  )

  let $max := $cons:OPTION($cons:K-MAXROWS)
  let $start := head((($page - 1) * $max + 1, 1))

  let $entries := if($count) then (
    $all-entries
  ) else (
    $all-entries[position() >= $start][position() <= $max + 1]
  )
  let $count := head(($count, count($all-entries)))

  let $last-page := $count < $start + $max
  let $single-page := not($page) or ($page = 1 and $last-page)
  return (
    element h4 {
      if($single-page) then () else
        $start || '-' || min(($count, $start + $max - 1)) || ' of ',
      $count, ' ',
      if($count = 1) then 'Entry' else ' Entries',
      if($single-page) then () else (
        ' &#xa0; ',
        let $first := '«', $prev := '‹'
        return if($page = 1) then ($first, $prev) else (
          html:link($first, "", ($param, map { 'page': 1, 'sort': $sort })), ' ',
          html:link($prev, "", ($param, map { 'page': $page - 1, 'sort': $sort }))
        ),
        ' ',
        let $last := '»', $next := '›'
        return if($last-page) then ($next, $last) else (
          html:link($next, "", ($param, map { 'page': $page + 1, 'sort': $sort })), ' ',
          html:link($last, "", ($param, map { 'page': ($count - 1) idiv $max + 1, 'sort': $sort }))
        )
      )
    },
    if(empty($rows)) then () else (
      element table {
        element tr {
          for $header at $pos in $headers
          let $name := $header/name()
          let $value := upper-case($header/text())
          return element th {
            attribute align { if($header/@type = $html:NUMBER) then 'right' else 'left' },
            if(empty($sort) or $name = $sort-key) then (
              $value
            ) else (
              html:link($value, "", ($param, map { 'sort': $name, 'page': $page })
              )
            )
          }
        },
  
        for $entry in $entries[position() <= $max]
        return element tr {
          for $header at $pos in $headers
          let $name := $header/name()
          let $type := $header/@type
          let $col := $entry/@*[name() = $name]
          let $value := $col/string()[.] ! (
            if($header/@type = 'bytes') then (
              try { prof:human(xs:integer(.)) } catch * { . }
            ) else if($header/@type = 'decimal') then (
              try { format-number(number(.), '0.00') } catch * { . }
            ) else if($header/@type = 'dateTime') then (
              html:date(xs:dateTime(.))
            )
            else .
          )
          return element td {
            attribute align { if($header/@type = $html:NUMBER) then 'right' else 'left' },
            if($pos = 1 and $buttons) then (
              <input type="checkbox" name="{ $name }" value="{ $col }" onClick="buttons()"/>
            ) else (),
            if($pos = 1 and exists($link)) then (
              html:link($value, $link($value), ($param, map { $name: $value }))
            ) else if($header/@type = 'id') then () else (
              $value
            )
          }
        }
      }
    )
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
 : @param  $text  link text
 : @param  $href  link reference
 : @return link
 :)
declare function html:link(
  $text  as xs:string,
  $href  as xs:string
) as element(a) {
  <a href="{ $href }">{ $text }</a>
};

(:~
 : Creates a link to the specified target.
 : @param  $text    link text
 : @param  $href    link reference
 : @param  $params  maps with query parameters.
 : @return link
 :)
declare function html:link(
  $text    as xs:string,
  $href    as xs:string,
  $params  as map(*)*
) as element(a) {
  html:link($text, web:create-url($href, map:merge($params)))
};

(:~
 : Formats a date.
 : @param  $date  date
 : @return string
 :)
declare function html:date(
  $date as xs:dateTime
) {
  let $zone := timezone-from-dateTime(current-dateTime())
  let $dt := fn:adjust-dateTime-to-timezone(xs:dateTime($date), $zone)
  return format-dateTime($dt, '[Y0000]-[M00]-[D00], [H00]:[m00]:[s00]')
};
