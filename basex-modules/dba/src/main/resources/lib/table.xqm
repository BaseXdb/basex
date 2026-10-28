(:~
 : Tables.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace table = 'dba/lib/table';

import module namespace config = 'dba/lib/config' at 'config.xqm';
import module namespace html = 'dba/lib/html' at 'html.xqm';
import module namespace utils = 'dba/lib/utils' at 'utils.xqm';

(:~ Column of a table; see table:create. :)
declare record table:column(
  key    as xs:string,
  label  as xs:string,
  type   as xs:string?,
  sort   as xs:string?,
  order  as xs:string?,
  width  as xs:string?,
  link   as (fn(map(*), item()*) as node()+)?
);

(:~ Options of a table; see table:create. :)
declare record table:options(
  sort     as xs:string?,
  presort  as xs:string?,
  select   as xs:string?,
  page     as xs:integer?,
  focus    as xs:string?,
  count    as xs:integer?,
  filters  as element(tr)?,
  all      as xs:boolean?,
  pinned   as xs:boolean?,
  sticky   as node()*,
  below    as node()*,
  noun     as xs:string?,
  empty    as xs:string?
);

(:~ Placeholder of a missing value. :)
declare variable $table:NONE := html:symbol('–', 'none');

(:~ What a column type is ordered by, and what it is shown as. :)
(: a type that is not listed is ordered and shown as the string it is; a column without a
   format shows the value itself :)
declare %private variable $table:TYPES := {
  'number'  : { 'order': 'number' },
  'decimal' : { 'order': 'number',
                'format': fn($v) { format-number(number($v otherwise 0), '0.00') } },
  'percent' : { 'order': 'number',
                'format': fn($v) { ($v ! format-number(number(.), '0%')) otherwise $table:NONE } },
  'bytes'   : { 'order': 'number',
                'format': fn($v) { ($v ! prof:human(xs:integer(.))) otherwise $table:NONE } },
  'duration': { 'order': 'number',
                'format': fn($v) { $v ! html:duration(xs:decimal(.)) } },
  'dateTime': { 'order': 'date',
                'format': fn($v) { ($v ! html:short-date(xs:dateTime(.))) otherwise $table:NONE } },
  'time'    : { 'order': 'date', 'format': fn($v) { $v ! html:time(xs:dateTime(.)) } }
};

(:~ Number formats: the types that are ordered and aligned as numbers. :)
declare variable $table:NUMBER := map:keys($table:TYPES)[$table:TYPES?(.)?order = 'number'];

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
 : @param  $props   properties
 : @param  $labels  labels of the sections and properties; other names are shown as option names
 : @return table
 :)
declare function table:properties(
  $props   as element(),
  $labels  as map(xs:string, xs:string)
) as element(table) {
  let $label := fn($node) { $labels?(name($node)) otherwise upper-case(name($node)) }
  return table:pairs(
    for $header in $props/*
    return (
      <tr>
        <th colspan='2'>
          <h3>{ $label($header) }</h3>
        </th>
      </tr>,
      for $option in $header/*
      let $value := $option/data()
      return <tr>
        <td><b>{ $label($option) }</b></td>
        <td>{ table:option-value($value) }</td>
      </tr>
    )
  )
};

(:~
 : Returns an option value, with boolean values shown as symbols.
 : @param  $value  value
 : @return value or symbol
 :)
declare function table:option-value(
  $value  as xs:anyAtomicType?
) as item()? {
  html:symbol('✓', 'yes')[$value = 'true'] otherwise
  html:symbol('–', 'no')[$value = 'false'] otherwise $value
};

(:~
 : Creates the header label of a column: a link that sorts the table by it.
 : @param  $header  column
 : @param  $sort    key of the ordered column, prefixed with '-' to reverse its order; if empty,
 :   sorting is disabled
 : @param  $params  additional query parameters, included in the link
 : @return label
 :)
declare function table:label(
  $header  as table:column,
  $sort    as xs:string?,
  $params  as map(*) := {}
) as item()* {
  let $name := $header?key
  let $label := $header?label
  return switch () {
    (: sorting disabled, and a column with no label to click: only the label :)
    case empty($sort) or not($label) return $label
    (: sorted column: the link reverses the order, the arrow shows the current one :)
    case $sort = ($name, `-{ $name }`) return (
      let $desc := table:order($header, $sort)?desc
      let $link := html:link($label, '', ($params, {
        'sort': if ($sort = $name) then `-{ $name }` else $name
      }))
      let $arrow := <span class='sorted'>{ if ($desc) then '▾' else '▴' }</span>
      (: the arrow is placed on the outer side of a right-aligned label :)
      return if ($header?type = $table:NUMBER) then ($arrow, $link) else ($link, $arrow)
    )
    (: generate sort link :)
    default return html:link($label, '', ($params, { 'sort': $name }))
  }
};

(:~
 : Returns the sort state of a column, as it is announced to screen readers.
 : @param  $header  column
 : @param  $sort    key of the ordered column, prefixed with '-' to reverse its order
 : @return attribute, or nothing if the table is not ordered by the column
 :)
declare function table:sort-state(
  $header  as table:column,
  $sort    as xs:string?
) as attribute()? {
  if ($sort = ($header?key, `-{ $header?key }`)) {
    attribute aria-sort {
      if (table:order($header, $sort)?desc) then 'descending' else 'ascending'
    }
  }
};

(:~
 : Resolves a sort key to the ordered column and its direction.
 : @param  $headers  table headers
 : @param  $sort     key of the ordered column, prefixed with '-' to reverse its order
 : @return key, column, and whether the order is descending
 :)
declare function table:order(
  $headers  as table:column*,
  $sort     as xs:string?
) as map(*) {
  let $reversed := starts-with($sort, '-')
  let $key := if ($reversed) then substring($sort, 2) else $sort
  let $column := $headers[?key = $key]
  return { 'key': $key, 'column': $column, 'desc': ($column?order = 'desc') != $reversed }
};

(:~
 : Sorts items by the values a function returns for them.
 : @param  $items  items
 : @param  $value  function that returns the value of an item
 : @param  $desc   whether the order is descending
 : @return sorted items
 :)
declare function table:sort(
  $items  as item()*,
  $value  as fn(item()) as item()?,
  $desc   as xs:boolean
) as item()* {
  (: the values are computed once; a missing value is ordered last in both directions :)
  sort-by($items ! { 'item': ., 'value': $value(.) }, (
    { 'key': fn { empty(?value) } },
    { 'key': fn { ?value }, 'collation': '?lang=en',
      'order': if ($desc) then 'descending' else 'ascending' }
  ))?item
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
 :     * 'duration': seconds, sorted as numbers, output as minutes and seconds
 :     * 'dateTime', 'time': sorted and output as dates
 :     * otherwise, sorted and output as strings
 :   * 'sort': overrides the type the column is sorted by
 :   * 'order': 'desc' for descending order, otherwise ascending
 :   * 'width': fixed column width. If widths are supplied, the table layout gets stable:
 :     overflowing values are truncated and can be expanded via clicks
 :   * 'link': turns the formatted value of an entry into a link, optionally followed by notes;
 :     the value is still sorted as it is supplied, and a missing value is not linked
 : @param  $entries  table entries, each of them a map from a column key to its value
 : @param  $buttons  buttons and other controls, placed above the table
 : @param  $params   additional query parameters, included in the table links
 : @param  $options  additional options:
 :   * 'sort': key of the ordered column, prefixed with '-' to reverse its order; if empty,
 :     sorting will be disabled
 :   * 'select': key of the entry value that the checkboxes submit; by default, the value that
 :     the first column shows
 :   * 'presort': key of pre-sorted column; if identical to sort, entries will not be resorted
 :   * 'page': number of pages that are shown; a link below the table shows one more
 :   * 'focus': id of an entry that the shown pages start around
 :   * 'count': total number of entries, if only a slice of them is supplied
 :   * 'filters': table row with filter fields, displayed below the header row
 :   * 'all': list all entries, ignoring the maximum number of table entries
 :   * 'sticky': content placed above the buttons. Everything above the table is then pinned to
 :     the top of the scrolling panel, so that the actions stay reachable while the rows pass
 :     underneath
 :   * 'pinned': pins the buttons alone, without content above them
 :   * 'below': content placed below the buttons, above the result summary
 :   * 'noun': what the entries are called in the result summary (singular form; default: entry)
 :   * 'empty': note that is shown if there are no entries; by default, it states that there are
 :     none
 : @return table
 :)
declare function table:create(
  $headers  as table:column*,
  $entries  as map(*)*,
  $buttons  as element()* := (),
  $params   as map(*) := {},
  $options  as table:options := {}
) as element()+ {
  (: sort entries; a leading '-' reverses the default order of the column :)
  let $sort := $options?sort
  let $order := table:order($headers, $sort)
  let $key := $order?key
  let $column := $order?column
  (: entries in their given order may be a slice; their total is then supplied as 'count' :)
  let $presorted := not($sort) or $sort = $options?presort
  let $sorted-entries := (
    if ($presorted) then (
      $entries
    ) else (
      let $number := $table:TYPES?($column?sort otherwise $column?type)?order = 'number'
      (: a cell that a function produces is ordered by the text it produces :)
      let $value := fn($entry) {
        let $v := $entry?$key
        let $v := if ($v instance of fn(*)) then string-join($v()) else $v
        return if ($number) then $v ! number() else $v
      }
      return table:sort($entries, $value, $order?desc)
    )
  )

  (: a checkbox submits what identifies its row: a value of its own, or what the row shows :)
  let $select := $options?select

  (: show results; 'all' lists every entry, whatever the configured maximum :)
  let $max-option := if ($options?all) then (
    max((count($sorted-entries), 1))
  ) else (
    $config:ROWS
  )
  let $count-option := $options?count[$presorted]
  let $page := max(($options?page, 1))
  let $total := $count-option otherwise count($sorted-entries)
  (: the focused entry is shown amid its neighbors, and not after all entries before it :)
  let $offset := max((
    $options?focus ! index-of($sorted-entries?id, .)[1] - 1 - $max-option idiv 2, 0
  ))
  (: the entries of the shown pages, but not more than a table is meant to hold :)
  let $last := min((
    $total, $offset + min(($page * $max-option, $config:MAX-SHOWN[not($options?all)]))
  ))

  let $noun := $options?noun otherwise 'entry'
  (: everything above the table :)
  let $head := (
    $options?sticky,
    if ($buttons) {
      <div class='buttons'>{ $buttons }</div>
    },
    $options?below,
    (: result summary; it is what an empty table is left with. The two forms of the noun are
       stated, so that a filter that hides rows in the client can restate the summary for the
       ones it leaves instead of writing the words again; see filterRows :)
    element h3 {
      attribute data-singular { utils:capitalize(utils:plural(1, $noun)) },
      attribute data-plural { utils:capitalize(utils:plural(2, $noun)) },
      $total,
      utils:capitalize(utils:plural($total, $noun)),

      <span class='range'>{ `{ $offset + 1 }–`[$offset > 0] }{ $last } shown</span>[
        $offset > 0 or $last < $total
      ]
    }
  )
  return (
    (: the head is pinned to the top of the scrolling panel it sits in :)
    if ($options?pinned or $options?sticky) then (
      <div class='sticky'>{ $head }</div>
    ) else (
      $head
    ),

    (: an empty table says why it is empty, or what is missing :)
    if ($last = 0) {
      <p class='note'>{
        $options?empty otherwise `There are no { utils:plural(2, $noun) }.`
      }</p>
    },

    (: list of results :)
    let $shown-entries := $sorted-entries[position() > $offset and position() <= $last]
    where exists($shown-entries) or exists($options?filters)
    let $fixed := exists($headers?width)
    (: columns without a width keep some room when the fixed ones exceed a narrow screen :)
    let $free := count($headers[empty(?width)])
    let $table := element table {
      attribute class { 'fixed' }[$fixed],
      attribute style {
        let $widths := ($headers?width, `var(--free-column) * { $free }`)
        return `min-width: calc({ string-join($widths, ' + ') })`
      }[$fixed and $free],
      (: the header and the filters stay in view while the entries scroll :)
      element thead {
        element tr {
          for $header at $pos in $headers
          return element th {
            attribute class { 'num' }[$header?type = $table:NUMBER],
            attribute style { `width: { $header?width }` }[$header?width],
            table:sort-state($header, $sort),

            if ($pos = 1 and $buttons) {
              <input type='checkbox' onclick='toggle(this)' aria-label='Select all'/>, ' '
            },

            table:label($header, $sort, $params)
          }
        },
        $options?filters
      },

      element tbody {
        for $entry in $shown-entries
        return element tr {
          $entry?id ! attribute id { . },
          for $header at $pos in $headers
          let $name := $header?key
          let $type := $header?type

          (: format value :)
          let $v := $entry?$name
          let $format := $table:TYPES?$type?format
          let $value := try {
            switch () {
              case exists($format) return $format($v)
              (: a cell that a function produces is what it returns :)
              case $v instance of fn(*) return $v()
              default return string($v)
            }
          } catch * {
            $err:description
          }
          let $value := if (exists($header?link) and exists($v)) then $header?link($entry, $value) else $value
          return element td {
            attribute class { 'num' }[$type = $table:NUMBER],
            if ($pos = 1 and $buttons) {
              (: a screen reader names the entry that is selected, not only the box :)
              <input type='checkbox' name='{ $select otherwise $name }'
                value='{ if ($select) then $entry?$select else data($value) }'
                aria-label='Select { $noun } { normalize-space(string-join($value ! string())) }'
                onclick='buttons(this)'/>,
              ' '
            },
            $value
          }
        }
      }
    }
    return (
      (: horizontal scroll on narrow screens :)
      element div {
        attribute class { 'scroll' },
        attribute data-offset { $offset }[$offset > 0],
        $table
      },
      (: entries that follow are shown by asking for the next page, up to the limit :)
      if ($last < $total) {
        if ($last - $offset < $config:MAX-SHOWN) then (
          (: a block that asks for the next page as soon as it is scrolled to, or via its button
             if the panels are stacked; see js.js :)
          <div class='more' data-more='{ $page + 1 }'>
            <button type='button' title='Show the entries that follow'>More</button>
          </div>
        ) else (
          <div class='note'>{
            `Only the first { format-integer($last, '#,##0') } entries are shown.`,
            'Use a filter to narrow the list.'
          }</div>
        )
      }
    )
  )
};
