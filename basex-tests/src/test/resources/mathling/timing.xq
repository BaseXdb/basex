(:~
 : Renders the per-engine timing logs as one aligned Markdown table: a column group
 : (total / compile / eval) for every engine, and a reference/engine ratio for each
 : non-reference engine.
 : @author BaseX Team, BSD License
 : @author Gunther Rademacher
 :)
declare option output:method "text";

(:~ Engines, one per line "name&#9;timing-log-URI", reference first. :)
declare variable $engines external;

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
 : Formats a reference/engine ratio as a percentage, or "".
 : @param  $reference  reference time
 : @param  $engine     engine time
 : @return formatted ratio
 :)
declare function local:percent(
  $reference  as xs:decimal?,
  $engine     as xs:decimal?
) as xs:string {
  if(exists($reference) and exists($engine) and $engine ne 0)
  then format-number($reference div $engine, "0.00 %")
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

(: parse the engines, reference first, each carrying its timings and module names :)
let $engines-seq :=
  for $line in tokenize($engines, "&#10;")[normalize-space(.) ne ""]
  let $parts := tokenize($line, "&#9;")
  return map {
    "name": $parts[1],
    "timings": local:timings($parts[2]),
    "names": local:module-names($parts[2])
  }
let $reference := $engines-seq[1]?timings
let $reference-name := $engines-seq[1]?name
let $modules :=
  (for $e in $engines-seq return $e?names) => distinct-values() => sort()
let $header :=
  ("Module",
   for $e at $i in $engines-seq
   return ($e?name, "compile", "eval",
           if ($i gt 1) then $reference-name || "/" || $e?name else ()))
let $body :=
  for $m in $modules
  return array {
    $m,
    for $e at $i in $engines-seq
    let $t := $e?timings($m)
    return (local:time($t?1), local:time($t?2), local:time($t?3),
            if ($i gt 1) then local:percent($reference($m)?1, $t?1) else ())
  }
let $total :=
  array {
    "**Total**",
    for $e at $i in $engines-seq
    return ("**" || local:time(local:col($e?timings, 1)) || "**",
            "**" || local:time(local:col($e?timings, 2)) || "**",
            "**" || local:time(local:col($e?timings, 3)) || "**",
            if ($i gt 1)
            then "**" || local:percent(local:col($reference, 1), local:col($e?timings, 1)) || "**"
            else ())
  }
return local:table($header, ($body, $total))
