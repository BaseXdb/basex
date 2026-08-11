(:~
 : Tables.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace table = 'dba/lib/table';

import module namespace config = 'dba/lib/config' at 'config.xqm';
import module namespace html = 'dba/lib/html' at 'html.xqm';

(:~ Number formats. :)
declare variable $table:NUMBER := ('decimal', 'number', 'bytes');

(:~
 : Creates a property list.
 : @param  $props  properties
 : @return table
 :)
declare function table:properties(
  $props  as element()
) as element(table) {
  <table>{
    for $header in $props/*
    return (
      <tr>
        <th colspan='2'>
          <h3>{ upper-case(name($header)) }</h3>
        </th>
      </tr>,
      for $option in $header/*
      let $value := $option/data()
      return <tr>
        <td><b>{ upper-case($option/name()) }</b></td>
        <td>{
          '✓'[$value = 'true'] otherwise '–'[$value = 'false'] otherwise $value
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
 :   * The 'width' attribute assigns a fixed column width. If widths are supplied, the table
 :     layout gets stable: overflowing values are truncated and can be expanded via clicks.
 :   * The 'main' attribute indicates which column is the main column
 : * The supplied table rows are supplied as elements. Values are contained in attributes; their
 :   names represents the column key.
 : * Supplied buttons will placed on top of the table.
 : * Query parameters will be included in table links.
 : * The options argument can have the following keys:
 :   * 'sort': key of the ordered column; if empty, sorting will be disabled
 :   * 'presort': key of pre-sorted column; if identical to sort, entries will not be resorted
 :   * 'page': currently displayed page
 :   * 'count': maximum number of results
 :   * 'filters': table row with filter fields, displayed below the header row
 :   * 'compact': suppress the result summary and keep the headers of an empty table
 :   * 'all': list all entries, ignoring the maximum number of table entries
 :   * 'sticky': content placed above the buttons. Everything above the table is then pinned to
 :     the top of the scrolling panel, so that the actions stay reachable while the rows pass
 :     underneath. The key may be present with no content, which pins the buttons alone
 :
 : @param  $headers  table headers
 : @param  $entries  table entries
 : @param  $buttons  buttons and other controls, placed above the table
 : @param  $params   additional query parameters
 : @param  $options  additional options
 : @return table
 :)
declare function table:create(
  $headers  as map(*)*,
  $entries  as map(*)*,
  $buttons  as element()* := (),
  $params   as map(*) := {},
  $options  as map(*) := {}
) as element()+ {
  (: sort entries :)
  let $sort := $options?sort
  let $sorted-entries := (
    let $key := $sort[.] otherwise head($headers)?key
    return if (not($sort) or $key = $options?presort) {
      $entries
    } else {
      let $header := $headers[?key = $key]
      let $value := (
        let $desc := $header?order = 'desc'
        return switch($header?type) {
          case 'decimal' case 'number' case 'bytes' return
            if ($desc) {
              fn { 0 - number() }
            } else {
              fn { number() }
            }
          case 'time' case 'dateTime' return
            if ($desc) {
              fn { xs:dateTime('0001-01-01T00:00:00Z') - xs:dateTime(.) }
            } else {
              identity(?)
            }
          case 'dynamic' return
            fn { if (. instance of fn(*)) then string-join(.()) else . }
          default return
            identity(?)
        }
      )
      for $entry in $entries
      order by $value($entry($key)) empty greatest collation '?lang=en'
      return $entry
    }
  )

  (: show results; 'all' lists every entry, whatever the configured maximum :)
  let $max-option := if ($options?all) {
    max((count($sorted-entries), 1))
  } else {
    config:get($config:MAXROWS)
  }
  let $count-option := $options?count[not($sort)]
  let $page-option := $options?page

  let $entries := $count-option otherwise count($sorted-entries)
  let $last-page := ($entries - 1) idiv $max-option + 1
  let $curr-page := min((max(($page-option, 1)), $last-page))

  (: everything above the table :)
  let $head := (
    $options?sticky,
    if ($buttons) {
      <div class='buttons'>{ $buttons }</div>
    },
    (: result summary :)
    if (not($options?compact)) { element h3 {
      $entries,
      if ($entries = 1) then 'Entry' else 'Entries',

      if ($page-option and $last-page != 1) {
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
        let $suffix := (if ($page = $last-page) then ')' else ' ') ||
          (if ($pages[$pos + 1] > $page + 1) then ' … ' else ())
        return if ($curr-page = $page) {
          $page || $suffix
        } else {
          html:link(string($page), '', ($params, { 'page': $page, 'sort': $sort })),
          $suffix
        }
      }
    } }
  )
  return (
    (: the head is pinned to the top of the scrolling panel it sits in :)
    if (map:contains($options, 'sticky')) {
      <div class='sticky'>{ $head }</div>
    } else {
      $head
    },

    (: list of results :)
    let $shown-entries := if ($count-option) {
      $sorted-entries
    } else {
      let $first := ($curr-page - 1) * $max-option + 1
      return $sorted-entries[position() >= $first][position() <= $max-option + 1]
    }
    where exists($shown-entries) or exists($options?filters) or $options?compact
    let $fixed := some $header in $headers satisfies exists($header?width)
    let $table := element table {
      attribute class { 'fixed' }[$fixed],
      element tr {
        for $header at $pos in $headers
        let $name := $header?key
        let $label := upper-case($header?label)
        return element th {
          attribute class { 'num' }[$header?type = $table:NUMBER],
          attribute style { 'width: ' || $header?width }[$header?width],

          if ($pos = 1 and $buttons) {
            <input type='checkbox' onclick='toggle(this)'/>, ' '
          },

          if ($header?type = 'id') {
            (: id columns: empty header column :)
          } else if (empty($sort) or $name = $sort) {
            (: sorted column, xml column: only display label :)
            $label
          } else {
            (: generate sort link :)
            html:link($label, '', ($params, { 'sort': $name }))
          }
        }
      },
      $options?filters,

      for $entry in $shown-entries[position() <= $max-option]
      return element tr {
        $entry?id ! attribute id { . },
        for $header at $pos in $headers
        let $name := $header?key
        let $type := $header?type

        (: format value :)
        let $v := $entry($name)
        let $value := try {
          if ($type = 'bytes') {
            prof:human(if (exists($v)) then xs:integer($v) else 0)
          } else if ($type = 'decimal') {
            format-number(if (exists($v)) then number($v) else 0, '0.00')
          } else if ($type = 'dateTime') {
            html:date(xs:dateTime($v))
          } else if ($type = 'time') {
            html:time(xs:dateTime($v))
          } else if ($v instance of fn(*)) {
            $v()
          } else {
            string($v)
          }
        } catch * {
          $err:description
        }
        return element td {
          attribute class { 'num' }[$type = $table:NUMBER],
          if ($pos = 1 and $buttons) {
            <input type='checkbox' name='{ $name }' value='{ data($value) }'
              onclick='buttons(this)'/>,
            ' '
          },
          if (not($type = 'id')) { $value }
        }
      }
    }
    (: horizontal scroll on narrow screens :)
    return element div { attribute class { 'scroll' }, $table }
  )
};
