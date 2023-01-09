(:~
 : HTML components.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace html = 'dba/html';

import module namespace options = 'dba/options' at 'options.xqm';
import module namespace config = 'dba/config' at 'config.xqm';
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
  let $user := session:get($config:SESSION-KEY)
  return <html xml:space='preserve'>
    <head>
      <meta charset='utf-8'/>
      <title>DBA{ ($header, tail($options?header)) ! (' » ' || .) }</title>
      <meta name='description' content='Database Administration'/>
      <meta name='author' content='BaseX Team 2005-23, BSD License'/>
      <link rel='stylesheet' type='text/css' href='static/style.css'/>
      { $options?css ! <link rel='stylesheet' type='text/css' href='static/{ . }'/> }
      <script type='text/javascript' src='static/js.js'/>
      { $options?scripts ! <script type='text/javascript' src='static/{ . }'/> }
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
                  if($user) then (
                    <span style='float:right'>
                      <b>{ $user }</b> (<a href='logout'>logout</a>)
                    </span>
                  ) else ()
                }</td>
              </tr>
              <tr>
                <td>
                  <div class='ellipsis'>{
                    if($user) then (
                      let $cats := (
                        for $cat in ('Logs', 'Databases', 'Queries', 'Files', 'Jobs',
                          'Users', 'Sessions', 'Settings')
                        let $link := <a href='{ lower-case($cat) }'>{ $cat }</a>
                        return if($link = $header) then (
                          <b>{ $link }</b>
                        ) else (
                          $link
                        )
                      )
                      return (
                        head($cats),
                        tail($cats) ! (' · ', .),
                        (1 to 3) ! '&#x2000;'
                      )
                    ) else (
                      <div class='note'>
                        Please enter your admin credentials:
                      </div>
                    ),
                    <span>{
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
                  }</div>
                  <hr/>
                </td>
              </tr>
            </table>
          </td>
          <td class='slick'>
            <a href='/'><img src='static/basex.svg'/></a>
          </td>
        </tr>
      </table>
      <table width='100%'>{ $rows }</table>
      <hr/>
      <div class='right'><sup>BaseX Team 2005-23, BSD License</sup></div>
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
 :     * dateTime', 'time': sorted and output as dates
 :     * 'dynamic': function generating dynamic input; sorted as strings
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
 :   * 'sort': key of the ordered column; if empty, sorting will be disabled
 :   * 'presort': key of pre-sorted column; if identical to sort, entries will not be resorted
 :   * 'link': function for generating a link reference
 :   * 'page': currently displayed page
 :   * 'count': maximum number of results
 :
 : @param  $headers  table headers
 : @param  $entries  table entries
 : @param  $buttons  buttons
 : @param  $params   additional query parameters
 : @param  $options  additional options
 : @return table
 :)
declare function html:table(
  $headers  as map(*)*,
  $entries  as map(*)*,
  $buttons  as element(button)*,
  $params   as map(*),
  $options  as map(*)
) as element()+ {
  (: display buttons :)
  if($buttons) then (
    for $button in $buttons
    return ($button, <span> </span>),
    <br/>,
    <div class='small'/>
  ) else (),

  (: sort entries :)
  let $sort := $options?sort
  let $sorted-entries := (
    let $key := head(($sort[.], $headers[1]?key))
    return if(not($sort) or $key = $options?presort) then (
      $entries
    ) else (
      let $header := $headers[?key = $key]
      let $value := (
        let $desc := $header?order = 'desc'
        return switch($header?type)
          case 'decimal' case 'number' case 'bytes' return
            if($desc)
            then function($v) { 0 - number($v) }
            else function($v) { number($v) }
          case 'time' case 'dateTime' return
            if($desc)
            then function($v) { xs:dateTime('0001-01-01T00:00:00Z') - xs:dateTime($v) }
            else function($v) { $v }
          case 'dynamic' return
            function($v) { if($v instance of function(*)) then string-join($v()) else $v }
          default return
            function($v) { $v }
      )
      for $entry in $entries
      order by $value($entry($key)) empty greatest collation '?lang=en'
      return $entry
    )
  )

  (: show results :)
  let $max-option := options:get($options:MAXROWS)
  let $count-option := $options?count[not($sort)]
  let $page-option := $options?page

  let $entries := $count-option ?: count($sorted-entries)
  let $last-page := ($entries - 1) idiv $max-option + 1
  let $curr-page := min((max(($page-option, 1)), $last-page))
  return (
    (: result summary :)
    element h3 {
      $entries,
      if($entries = 1) then ' Entry' else 'Entries',

      if(not($page-option) or $last-page = 1) then () else (
        '(Page: ',
        let $pages := sort(distinct-values((
          1,
          $curr-page - $last-page idiv 10,
          $curr-page - 1,
          $curr-page,
          $curr-page + 1,
          $curr-page + $last-page idiv 10,
          $last-page
        ))[. >= 1 and . <= $last-page])
        for $page at $pos in $pages
        let $suffix := (if($page = $last-page) then ')' else ' ') ||
          (if($pages[$pos + 1] > $page + 1) then ' … ' else ())
        return if ($curr-page = $page) then (
          $page || $suffix
        ) else (
          html:link(string($page), '', ($params, map { 'page': $page, 'sort': $sort })),
          $suffix
        )
      )
    },

    (: list of results :)
    let $shown-entries := if($count-option) then (
      $sorted-entries
    ) else (
      let $first := ($curr-page - 1) * $max-option + 1
      return $sorted-entries[position() >= $first][position() <= $max-option + 1]
    )
    where exists($shown-entries)
    return element table {
      element tr {
        for $header at $pos in $headers
        let $name := $header?key
        let $label := upper-case($header?label)
        return element th {
          attribute align {
            if($header?type = $html:NUMBER) then 'right' else 'left'
          },

          if($pos = 1 and $buttons) then (
            <input type='checkbox' onclick='toggle(this)'/>, ' '
          ) else (),

          if($header?type = 'id') then (
            (: id columns: empty header column :)
          ) else if(empty($sort) or $name = $sort) then (
            (: sorted column, xml column: only display label :)
            $label
          ) else (
            (: generate sort link :)
            html:link($label, '', ($params, map { 'sort': $name }))
          )
        }
      },

      let $link := $options?link
      for $entry in $shown-entries[position() <= $max-option]
      return element tr {
        $entry?id ! attribute id { . },
        for $header at $pos in $headers
        let $name := $header?key
        let $type := $header?type

        (: format value :)
        let $v := $entry($name)
        let $value := try {
          if($type = 'bytes') then (
            prof:human(if(exists($v)) then xs:integer($v) else 0)
          ) else if($type = 'decimal') then (
            format-number(if(exists($v)) then number($v) else 0, '0.00')
          ) else if($type = 'dateTime') then (
            html:date(xs:dateTime($v))
          ) else if($type = 'time') then (
            html:time(xs:dateTime($v))
          ) else if($v instance of function(*)) then (
            $v()
          ) else (
            string($v)
          )
        } catch * {
          $err:description
        }
        return element td {
          attribute align { if($type = $html:NUMBER) then 'right' else 'left' },
          if($pos = 1 and $buttons) then (
            <input type='checkbox' name='{ $name }' value='{ data($value) }'
              onclick='buttons(this)'/>,
            ' '
          ) else (),
          if($pos = 1 and exists($link)) then (
            html:link($value, $link, ($params, map { $name: $value }))
          ) else if($type = 'id') then () else (
            $value
          )
        }
      }
    }
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
 : Returns a formatted representation of a dateTime value.
 : @param  $date  date
 : @return string
 :)
declare function html:date(
  $date  as xs:dateTime
) as xs:string {
  format-dateTime(html:adjust($date), '[Y0000]-[M00]-[D00], [H00]:[m00]:[s00]')
};

(:~
 : Returns a formatted time representation of a dateTime value with tooltip.
 : @param  $date  date
 : @return element with tooltip
 :)
declare function html:time(
  $date  as xs:dateTime
) as element(span) {
  let $adjusted := html:adjust($date)
  let $formatted := format-dateTime(html:adjust($date), '[H00]:[m00]:[s00]')
  return <span title='{ $adjusted }'>{ $formatted }</span>
};

(:~
 : Returns a dateTime value adjusted to the current time zone.
 : @param  $date  date
 : @return adjusted value
 :)
declare function html:adjust(
  $date  as xs:dateTime
) as xs:dateTime {
  let $zone := timezone-from-dateTime(current-dateTime())
  return fn:adjust-dateTime-to-timezone(xs:dateTime($date), $zone)
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

(:~
 : Creates a new map with the current query parameters.
 : @return map with query parameters
 :)
declare function html:parameters() as map(*) {
  map:merge(
    for $param in request:parameter-names()[not(starts-with(., '_'))]
    return map { $param: request:parameter($param) }
  )
};

(:~
 : Creates a new map with query parameters. The returned map contains all
 : current query parameters, : and the given ones, prefixed with an underscore.
 : @param  $map  predefined parameters
 : @return map with query parameters
 :)
declare function html:parameters(
  $map  as map(*)?
) as map(*) {
  map:merge((
    html:parameters(),
    map:for-each($map, function($name, $value) {
      map:entry('_' || $name, $value)
    })
  ))
};
