(:~
 : Tables.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace table = 'dba/lib/table';

import module namespace config = 'dba/lib/config' at 'config.xqm';
import module namespace html = 'dba/lib/html' at 'html.xqm';
import module namespace utils = 'dba/lib/utils' at 'utils.xqm';

(:~ What a column type is ordered by, and what it is shown as. :)
(: a type that is not listed is ordered and shown as the string it is; a column without a
   format shows the value itself :)
declare %private variable $table:TYPES := {
  'number'  : { 'order': 'number' },
  'decimal' : { 'order': 'number',
                'format': fn($v) { format-number(if (exists($v)) then number($v) else 0, '0.00') } },
  'bytes'   : { 'order': 'number',
                'format': fn($v) { prof:human(if (exists($v)) then xs:integer($v) else 0) } },
  'dateTime': { 'order': 'date', 'format': fn($v) { $v ! html:date(xs:dateTime(.)) } },
  'time'    : { 'order': 'date', 'format': fn($v) { $v ! html:time(xs:dateTime(.)) } }
};

(:~ Number formats: the types that are ordered and aligned as numbers. :)
declare variable $table:NUMBER := map:keys($table:TYPES)[$table:TYPES(.)?order = 'number'];

(:~
 : Creates a table with stable column widths: long values are truncated and expanded via click.
 : @param  $rows  table rows
 : @return table
 :)
declare function table:pairs(
  $rows  as element(tr)*
) as element(table) {
  <table class='fixed'>{
    <colgroup><col style='width: 40%'/><col/></colgroup>,
    $rows
  }</table>
};

(:~
 : Creates a property list.
 : @param  $props  properties
 : @return table
 :)
declare function table:properties(
  $props  as element()
) as element(table) {
  table:pairs(
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
  )
};

(:~
 : Creates a table for the specified entries.
 : @param  $headers  table headers, one per column:
 :   * 'key': key the column is named by, and the entry value it shows
 :   * 'label': header label
 :   * 'type': how the values are formatted and sorted:
 :     * 'number': sorted as numbers
 :     * 'decimal': sorted as numbers, output with two decimal digits
 :     * 'bytes': sorted as numbers, output in a human-readable format
 :     * 'dateTime', 'time': sorted and output as dates
 :     * 'dynamic': function generating dynamic input; sorted as strings
 :     * otherwise, sorted and output as strings
 :   * 'sort': overrides the type the column is sorted by
 :   * 'order': 'desc' for descending order, otherwise ascending
 :   * 'width': fixed column width. If widths are supplied, the table layout gets stable:
 :     overflowing values are truncated and can be expanded via clicks
 : @param  $entries  table entries, each of them a map from a column key to its value
 : @param  $buttons  buttons and other controls, placed above the table
 : @param  $params   additional query parameters, included in the table links
 : @param  $options  additional options:
 :   * 'sort': key of the ordered column; if empty, sorting will be disabled
 :   * 'select': key of the entry value that the checkboxes submit; by default, the value that
 :     the first column shows
 :   * 'presort': key of pre-sorted column; if identical to sort, entries will not be resorted
 :   * 'page': currently displayed page
 :   * 'count': maximum number of results
 :   * 'filters': table row with filter fields, displayed below the header row
 :   * 'all': list all entries, ignoring the maximum number of table entries
 :   * 'sticky': content placed above the buttons. Everything above the table is then pinned to
 :     the top of the scrolling panel, so that the actions stay reachable while the rows pass
 :     underneath. The key may be present with no content, which pins the buttons alone
 :   * 'below': content placed below the buttons, above the result summary
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
        (: a cell that a function produces is ordered by the text it produces :)
        let $atomize := fn($v) { if ($v instance of fn(*)) then string-join($v()) else $v }
        let $order := $table:TYPES(($header?sort otherwise $header?type) otherwise '')?order
        let $convert := if ($order = 'number') {
          if ($desc) {
            fn { 0 - number() }
          } else {
            fn { number() }
          }
        } else if ($order = 'date' and $desc) {
          (: a date is ordered by its lexical form, which only descending has to turn around :)
          fn { xs:dateTime('0001-01-01T00:00:00Z') - xs:dateTime(.) }
        } else {
          identity(?)
        }
        return fn($v) { $convert($atomize($v)) }
      )
      for $entry in $entries
      order by $value($entry($key)) empty greatest collation '?lang=en'
      return $entry
    }
  )

  (: a checkbox submits what identifies its row: a value of its own, or what the row shows :)
  let $select := $options?select

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
    $options?below,
    (: result summary; it is what an empty table is left with. The two forms of the noun are
       stated, so that a filter that hides rows in the client can restate the summary for the
       ones it leaves instead of writing the words again; see logFilter :)
    element h3 {
      attribute data-singular { utils:capitalize(utils:plural(1, 'entry')) },
      attribute data-plural { utils:capitalize(utils:plural(2, 'entry')) },
      $entries,
      utils:capitalize(utils:plural($entries, 'entry')),

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
    }
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
    where exists($shown-entries) or exists($options?filters)
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

          if (empty($sort) or $name = $sort or not($label)) {
            (: sorted column, xml column, and a column with no label to click: only the label :)
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
        let $format := $table:TYPES($type otherwise '')?format
        let $value := try {
          if (exists($format)) {
            $format($v)
          } else if ($v instance of fn(*)) {
            (: a cell that a function produces is what it returns :)
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
            <input type='checkbox' name='{ $select otherwise $name }'
              value='{ if ($select) then $entry($select) else data($value) }'
              onclick='buttons(this)'/>,
            ' '
          },
          $value
        }
      }
    }
    (: horizontal scroll on narrow screens :)
    return element div { attribute class { 'scroll' }, $table }
  )
};
