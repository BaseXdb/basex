(:~
 : Provides HTML components.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace html = 'dba/html';

import module namespace options = 'dba/options' at 'options.xqm';
import module namespace session = 'dba/session' at 'session.xqm';
import module namespace util = 'dba/util' at 'util.xqm';

(: Number formats. :)
declare variable $html:NUMBER := ('decimal', 'number', 'bytes');

(:~
 : Extends the specified table rows with the page template.
 : @param  $rows  tr elements
 : @return HTML page
 :)
declare function html:wrap(
  $rows  as element(tr)+
) as element(html) {
  html:wrap(map { }, $rows)
};

(:~
 : Extends the specified table rows with the page template.
 : The following options can be specified:
 : <ul>
 :   <li><b>header</b>: page headers</li>
 :   <li><b>error</b>: error string</li>
 :   <li><b>css</b>: CSS files</li>
 :   <li><b>scripts</b>: JavaScript files</li>
 : </ul>
 : @param  $options  options
 : @param  $rows     tr elements
 : @return page
 :)
declare function html:wrap(
  $options  as map(*),
  $rows     as element(tr)+
) as element(html) {
  let $header := head($options?header) ! util:capitalize(.)
  return <html xml:space="preserve">
    <head>
      <meta charset="utf-8"/>
      <title>DBA{ ($header, tail($options?header)) ! (' » ' || .) }</title>
      <meta name="description" content="Database Administration"/>
      <meta name="author" content="BaseX Team, 2014-18"/>
      <link rel="stylesheet" type="text/css" href="static/style.css"/>
      { $options?css ! <link rel="stylesheet" type="text/css" href="static/{.}"/> }
      <script type="text/javascript" src="static/js.js"/>
      { $options?scripts ! <script type="text/javascript" src="static/{.}"/> }
    </head>
    <body>
      <table cellpadding='0' cellspacing='0'>
        <tr>
          <td class='slick'>
            <table width='100%' cellpadding='0' cellspacing='0'>
              <tr>
                <td>{
                  <span style='float:left'>
                    <h1>BaseX Database Administration</h1>
                  </span>,
                  for $name in $session:VALUE
                  return <span style='float:right'>
                    <b>{ $name }</b> (<a href='logout'>logout</a>)
                  </span>
                }</td>
              </tr>
              <tr>
                <td>
                  <div class='ellipsis'>{
                    let $emph := <span>{
                      element b {
                        attribute id { 'info' },
                        let $error := $options?error[.], $info := $options?info[.]
                        return if($error) then (
                          attribute class { 'error' }, $error
                        ) else if($info) then (
                          attribute class { 'info' }, $info
                        ) else ()
                      }
                    }</span>
                    let $cats :=
                      for $cat in ('Logs', 'Databases', 'Queries', 'Files', 'Jobs',
                        'Users', 'Sessions', 'Settings')
                      let $link := <a href="{ lower-case($cat) }">{ $cat }</a>
                      return if($link = $header) then (
                        <b>{ $link }</b>
                      ) else (
                        $link
                      )
                    return (
                      head($cats),
                      tail($cats) ! (' · ', .),
                      (1 to 3) ! '&#x2000;',
                      $emph
                    )
                  }</div>
                  <hr/>
                </td>
              </tr>
            </table>
          </td>
          <td class='slick'>
            <img src="static/basex.svg"/>
          </td>
        </tr>
      </table>
      <table width='100%'>{ $rows }</table>
      <hr/>
      <div class='right'><sup>BaseX Team, 2014-18</sup></div>
      <div class='small'/>
      { html:js('buttons();') }
    </body>
  </html>
};

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
  html:checkbox('opts', $value, $opts = $value, $label)
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
    attribute type { 'checkbox' },
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
 : @param  $atts     additional attributes
 : @return button
 :)
declare function html:button(
  $value    as xs:string,
  $label    as xs:string,
  $confirm  as xs:boolean,
  $atts     as map(xs:string, xs:string)?
) as element(button) {
  element button {
    attribute name { 'action' },
    attribute value { $value },
    if($confirm) then (
      attribute onclick { 'return confirm("Are you sure?");' }
    ) else (),
    if(exists($atts)) then (
      map:for-each($atts, function($key, $value) { attribute { $key } { $value } })
    ) else (),
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
    for $header in $props/*
    return (
      <tr>
        <th colspan='2' align='left'>
          <h3>{ upper-case(name($header)) }</h3>
        </th>
      </tr>,
      for $option in $header/*
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
 :   * 'count': maximum number of results
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
  if($buttons) then (
    for $button in $buttons
    return ($button, <span> </span>),
    <br/>,
    <div class='small'/>
  ) else (),

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
    return (
      for $row in $rows
      order by string($row/@*[name() = $sort-key])[.] ! $order(.)
        empty greatest collation '?lang=en'
      return $row
    )
  )

  let $max := options:get($options:MAXROWS)
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
      if($count = 0) then 'Entries.' else if($count = 1) then 'Entry:' else ' Entries:',
      if($single-page) then () else (
        ' &#xa0; ',
        let $first := '«', $prev := '‹'
        return if($page = 1) then ($first, $prev) else (
          html:link($first, '', ($param, map { 'page': 1, 'sort': $sort })), ' ',
          html:link($prev, '', ($param, map { 'page': $page - 1, 'sort': $sort }))
        ),
        ' ',
        let $last := '»', $next := '›'
        return if($last-page) then ($next, $last) else (
          html:link($next, '', ($param, map { 'page': $page + 1, 'sort': $sort })), ' ',
          html:link($last, '', ($param, map { 'page': ($count - 1) idiv $max + 1, 'sort': $sort }))
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
            attribute align {
              if($header/@type = $html:NUMBER) then 'right' else 'left'
            },

            if($pos = 1 and $buttons) then (
              <input type='checkbox' onclick='toggle(this)'/>,
              ' '
            ) else (),

            if($header/@type = 'id') then (
            ) else if(empty($sort) or $name = $sort-key or $header/@type = 'xml') then (
              $value
            ) else (
              html:link($value, '', ($param, map { 'sort': $name, 'page': $page }))
            )
          }
        },
  
        for $entry in $entries[position() <= $max]
        return element tr {
          for $header at $pos in $headers
          let $name := $header/name()
          let $type := $header/@type
          let $col := $entry/@*[name() = $name]
          let $value := 
            for $v in string($col)[.]
            return try {
              if($header/@type = 'bytes') then (
                prof:human(xs:integer($v))
              ) else if($header/@type = 'decimal') then (
                format-number(number($v), '0.00')
              ) else if($header/@type = 'dateTime') then (
                html:date(xs:dateTime($v))
              ) else if($header/@type = 'xml') then (
                parse-xml-fragment($v)
              )
              else .
            } catch * {
              (: error: show original value :)
              $v
            }
          return element td {
            attribute align { if($header/@type = $html:NUMBER) then 'right' else 'left' },
            if($pos = 1 and $buttons) then (
              <input type='checkbox' name='{ $name }' value='{ data($value) }'
                onclick='buttons(this)'/>,
              ' '
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
  html:js('var u = document.getElementById("' || replace($element, '"', '') || '"); ' ||
    'u.focus(); u.select();')
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
  <a href='{ $href }'>{ $text }</a>
};

(:~
 : Creates a link to the specified target.
 : @param  $text    link text
 : @param  $href    link reference
 : @param  $params  query parameters
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
  $date  as xs:dateTime
) as xs:string {
  let $zone := timezone-from-dateTime(current-dateTime())
  let $dt := fn:adjust-dateTime-to-timezone(xs:dateTime($date), $zone)
  return format-dateTime($dt, '[Y0000]-[M00]-[D00], [H00]:[m00]:[s00]')
};

(:~
 : Formats a duration.
 : @param  $seconds  seconds
 : @return string
 :)
declare function html:duration(
  $seconds  as xs:decimal
) as xs:string {
  let $min := $seconds idiv 60
  let $sec := $seconds - $min * 60
  return (format-number($min, '00') || ':' || format-number($sec, '00'))
};

(:~
 : Creates an embedded JavaScript snippet.
 : @param  $js  JavaScript string
 : @return script element
 :)
declare function html:js(
  $js  as xs:string
) as element(script) {
  <script type='text/javascript'>{
    '(function() { ' || $js || ' })();'
  }</script>
};
