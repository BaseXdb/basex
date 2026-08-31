(:~
 : Renders the timing CSV(s) as an aligned Markdown table: BaseX only, or BaseX and Saxon side
 : by side with the BaseX / Saxon ratio.
 : @author BaseX Team, BSD License
 : @author Gunther Rademacher
 :)
declare option output:method "text";

(:~ URI of the BaseX timing.log. :)
declare variable $basex-log external;
(:~ URI of the Saxon timing.log ("" for BaseX only). :)
declare variable $saxon-log external := "";

(:~
 : Concatenates a string a number of times.
 : @param  $string  string to repeat
 : @param  $count   repeat count
 : @return the concatenation
 :)
declare function local:repeat(
  $string  as xs:string,
  $count   as xs:integer
) as xs:string {
  replicate($string, $count) => string-join()
};

(:~
 : Left-aligns a value in a field.
 : @param  $value  value
 : @param  $width  field width
 : @return padded value
 :)
declare function local:pad-right(
  $value  as xs:string,
  $width  as xs:integer
) as xs:string {
  $value || local:repeat(" ", $width - string-length($value))
};

(:~
 : Right-aligns a value in a field.
 : @param  $value  value
 : @param  $width  field width
 : @return padded value
 :)
declare function local:pad-left(
  $value  as xs:string,
  $width  as xs:integer
) as xs:string {
  local:repeat(" ", $width - string-length($value)) || $value
};

(:~
 : Formats a time in seconds, or "" if absent.
 : @param  $time  seconds
 : @return formatted time
 :)
declare function local:time(
  $time  as xs:decimal?
) as xs:string {
  if(exists($time)) then format-number($time, "0.00") || " s" else ""
};

(:~
 : Formats the BaseX / Saxon ratio as a percentage, or "".
 : @param  $basex  BaseX time
 : @param  $saxon  Saxon time
 : @return formatted ratio
 :)
declare function local:percent(
  $basex  as xs:decimal?,
  $saxon  as xs:decimal?
) as xs:string {
  if(exists($basex) and exists($saxon) and $saxon ne 0)
  then format-number($basex div $saxon, "0.00 %")
  else ""
};

(:~
 : Reads a timing.log into a map of module to [total, compile, eval] seconds.
 : @param  $path  timing.log URI
 : @return module timings
 :)
declare function local:timings(
  $path  as xs:string
) as map(xs:string, array(xs:decimal)) {
  map:merge(
    for $row in csv-doc($path, { "header": true() })?rows
    let $module := $row?1
    where normalize-space($module) ne "" and $row?2 castable as xs:decimal
    return map:entry($module, [ xs:decimal($row?2), xs:decimal($row?3), xs:decimal($row?4) ]),
    { "duplicates": "use-last" }
  )
};

(:~
 : Sums the i-th time of every module.
 : @param  $map  module timings
 : @param  $i    time index (1: total, 2: compile, 3: eval)
 : @return the sum
 :)
declare function local:col(
  $map  as map(xs:string, array(xs:decimal)),
  $i    as xs:integer
) as xs:decimal {
  sum($map?* ! .($i))
};

(:~
 : Lists the module names in a timing.log.
 : @param  $path  timing.log URI
 : @return module names
 :)
declare function local:module-names(
  $path  as xs:string
) as xs:string* {
  for $row in csv-doc($path, { "header": true() })?rows
  let $module := $row?1
  where normalize-space($module) ne ""
  return $module
};

(:~
 : Builds a table row (first column left-aligned, rest right-aligned).
 : @param  $cells   cell values
 : @param  $widths  column widths
 : @return Markdown row
 :)
declare function local:row(
  $cells   as xs:string*,
  $widths  as xs:integer*
) as xs:string {
  "| " || string-join(
    for $i in 1 to count($cells)
    return if ($i = 1) then local:pad-right($cells[$i], $widths[$i])
                       else local:pad-left($cells[$i], $widths[$i]),
    " | ") || " |"
};

(:~
 : Builds the header separator row.
 : @param  $widths  column widths
 : @return Markdown separator
 :)
declare function local:separator(
  $widths  as xs:integer*
) as xs:string {
  "| " || string-join(
    for $i in 1 to count($widths)
    return if ($i = 1) then local:repeat("-", $widths[$i])
                       else local:repeat("-", $widths[$i] - 1) || ":",
    " | ") || " |"
};

(:~
 : Builds a Markdown table, sizing each column to its widest cell.
 : @param  $header  header cells
 : @param  $rows    body rows
 : @return Markdown table
 :)
declare function local:table(
  $header  as xs:string*,
  $rows    as array(xs:string)*
) as xs:string {
  let $cols := count($header)
  let $widths :=
    for $c in 1 to $cols
    return max(($header[$c], for $r in $rows return $r($c)) ! string-length(.))
  return string-join((
    local:row($header, $widths),
    local:separator($widths),
    for $r in $rows return local:row(array:items($r), $widths)
  ), "&#10;")
};

let $has-saxon := normalize-space($saxon-log) ne ""
let $basex := local:timings($basex-log)
let $saxon := if ($has-saxon) then local:timings($saxon-log) else map { }
let $modules := (local:module-names($basex-log), if ($has-saxon) { local:module-names($saxon-log) })
  => distinct-values() => sort()
return
  if ($has-saxon) then
    local:table(
      ("Module", "BaseX", "compile", "eval", "Saxon", "compile", "eval", "B / S"),
      (
        for $m in $modules
        let $b := $basex($m), $s := $saxon($m)
        return array { $m,
          local:time($b?1), local:time($b?2), local:time($b?3),
          local:time($s?1), local:time($s?2), local:time($s?3),
          local:percent($b?1, $s?1) },
        array { "**Total**",
          "**" || local:time(local:col($basex, 1)) || "**",
          "**" || local:time(local:col($basex, 2)) || "**",
          "**" || local:time(local:col($basex, 3)) || "**",
          "**" || local:time(local:col($saxon, 1)) || "**",
          "**" || local:time(local:col($saxon, 2)) || "**",
          "**" || local:time(local:col($saxon, 3)) || "**",
          "**" || local:percent(local:col($basex, 1), local:col($saxon, 1)) || "**" }
      )
    )
  else
    local:table(
      ("Module", "BaseX", "compile", "eval"),
      (
        for $m in $modules
        let $b := $basex($m)
        return array { $m, local:time($b?1), local:time($b?2), local:time($b?3) },
        array { "**Total**",
          "**" || local:time(local:col($basex, 1)) || "**",
          "**" || local:time(local:col($basex, 2)) || "**",
          "**" || local:time(local:col($basex, 3)) || "**" }
      )
    )
