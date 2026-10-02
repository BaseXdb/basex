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
  width  as xs:string?
);

(:~ Options of a table; see table:create. :)
declare record table:options(
  sort     as xs:string?,
  presort  as xs:string?,
  select   as xs:string?,
  page     as xs:integer?,
  count    as xs:integer?,
  filters  as element(tr)?,
  all      as xs:boolean?,
  pinned   as xs:boolean?,
  sticky   as node()*,
  below    as node()*
);

(:~ What a column type is ordered by, and what it is shown as. :)
(: a type that is not listed is ordered and shown as the string it is; a column without a
   format shows the value itself :)
declare %private variable $table:TYPES := {
  'number'  : { 'order': 'number' },
  'decimal' : { 'order': 'number',
                'format': fn($v) { format-number(number($v otherwise 0), '0.00') } },
  'bytes'   : { 'order': 'number',
                'format': fn($v) { prof:human(xs:integer($v) otherwise 0) } },
  'dateTime': { 'order': 'date',
                'format': fn($v) { ($v ! html:short-date(xs:dateTime(.))) otherwise '–' } },
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
 :   * 'page': number of pages that are shown; a link below the table shows one more
 :   * 'count': total number of entries, if only a slice of them is supplied
 :   * 'filters': table row with filter fields, displayed below the header row
 :   * 'all': list all entries, ignoring the maximum number of table entries
 :   * 'sticky': content placed above the buttons. Everything above the table is then pinned to
 :     the top of the scrolling panel, so that the actions stay reachable while the rows pass
 :     underneath
 :   * 'pinned': pins the buttons alone, without content above them
 :   * 'below': content placed below the buttons, above the result summary
 : @return table
 :)
declare function table:create(
  $headers  as table:column*,
  $entries  as map(*)*,
  $buttons  as element()* := (),
  $params   as map(*) := {},
  $options  as table:options := {}
) as element()+ {
  (: sort entries :)
  let $sort := $options?sort
  let $sorted-entries := (
    if (not($sort) or $sort = $options?presort) then (
      $entries
    ) else (
      let $header := $headers[?key = $sort]
      let $value := (
        let $desc := $header?order = 'desc'
        (: a cell that a function produces is ordered by the text it produces :)
        let $atomize := fn($v) { if ($v instance of fn(*)) then string-join($v()) else $v }
        let $order := $table:TYPES?($header?sort otherwise $header?type)?order
        let $convert := if ($order = 'number') then (
          if ($desc) then (
            fn { 0 - number() }
          ) else (
            fn { number() }
          )
        ) else if ($order = 'date' and $desc) then (
          (: a date is ordered by its lexical form, which only descending has to turn around :)
          fn { xs:dateTime('0001-01-01T00:00:00Z') - xs:dateTime(.) }
        ) else (
          identity(?)
        )
        return fn($v) { $convert($atomize($v)) }
      )
      for $entry in $entries
      order by $value($entry?$sort) empty greatest collation '?lang=en'
      return $entry
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
  let $count-option := $options?count[not($sort)]
  let $page := max(($options?page, 1))
  let $entries := $count-option otherwise count($sorted-entries)
  (: the entries of the shown pages, but not more than a table is meant to hold :)
  let $last := min(($page * $max-option, $entries, $config:MAX-SHOWN[not($options?all)]))

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

      <span class='range'>{ $last } shown</span>[$last < $entries]
    }
  )
  return (
    (: the head is pinned to the top of the scrolling panel it sits in :)
    if ($options?pinned or $options?sticky) then (
      <div class='sticky'>{ $head }</div>
    ) else (
      $head
    ),

    (: list of results :)
    let $shown-entries := $sorted-entries[position() <= $last]
    where exists($shown-entries) or exists($options?filters)
    let $fixed := some $header in $headers satisfies $header?width
    (: columns without a width keep some room when the fixed ones exceed a narrow screen :)
    let $free := count($headers[empty(?width)])
    let $table := element table {
      attribute class { 'fixed' }[$fixed],
      attribute style {
        'min-width: calc(' || string-join(
          ($headers?width, 'var(--free-column) * ' || $free), ' + '
        ) || ')'
      }[$fixed and $free],
      (: the header and the filters stay in view while the entries scroll :)
      element thead {
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

            if (empty($sort) or $name = $sort or not($label)) then (
              (: sorting disabled, sorted column, and a column with no label to click: only the label :)
              $label
            ) else (
              (: generate sort link :)
              html:link($label, '', ($params, { 'sort': $name }))
            )
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
            if (exists($format)) then (
              $format($v)
            ) else if ($v instance of fn(*)) then (
              (: a cell that a function produces is what it returns :)
              $v()
            ) else (
              string($v)
            )
          } catch * {
            $err:description
          }
          return element td {
            attribute class { 'num' }[$type = $table:NUMBER],
            if ($pos = 1 and $buttons) {
              <input type='checkbox' name='{ $select otherwise $name }'
                value='{ if ($select) then $entry?$select else data($value) }'
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
      element div { attribute class { 'scroll' }, $table },
      (: entries that follow are shown by asking for the next page, up to the limit :)
      if ($last < $entries) {
        if ($last < $config:MAX-SHOWN) then (
          (: an empty link, followed as soon as it is scrolled to; see js.js :)
          <div class='more'>{
            html:link('', '', ($params, { 'page': $page + 1, 'sort': $sort }))
          }</div>
        ) else (
          <div class='note'>{
            ``[Only the first `{ format-integer($last, '#,##0') }` entries are shown. ]``,
            'Use a filter to narrow the list.'
          }</div>
        )
      }
    )
  )
};
