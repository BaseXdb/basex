(:~
 : Compares two engine result files and returns a tolerant "VERDICT|detail": whitespace-normalised
 : text, then JSON, then XML (structure/skeleton), else a line-count diff. Random-valued output only
 : needs structural equality.
 : @author BaseX Team, BSD License
 : @author Gunther Rademacher
 :)
declare option output:method "text";

(:~ First result file. :)
declare variable $a external;
(:~ Second result file. :)
declare variable $b external;

(:~
 : Normalizes cosmetic text differences: strips CR, trailing whitespace and trailing blank lines.
 : @param  $s  string to normalise
 : @return normalised string
 :)
declare function local:norm(
  $s  as xs:string
) as xs:string {
  let $t := replace($s, '&#xD;', '')
  let $t := string-join(
              for $l in tokenize($t, '&#xA;') return replace($l, '[ &#x9;]+$', ''),
              '&#xA;')
  return replace($t, '&#xA;+$', '')
};

(:~
 : Drops whitespace-only text nodes so indentation and prefix differences do not prevent
 : structural equality.
 : @param  $n  node to strip
 : @return stripped node(s)
 :)
declare function local:strip(
  $n  as node()
) as node()* {
  typeswitch($n)
  case document-node() return document { $n/node() ! local:strip(.) }
  case element() return element { node-name($n) } { $n/@*, $n/node() ! local:strip(.) }
  case text() return if (normalize-space($n) ne '') { $n }
  default return $n
};

(:~
 : Element-name multiset: a document's skeleton, ignoring values.
 : @param  $doc  document
 : @return the skeleton
 :)
declare function local:sig(
  $doc  as node()
) as xs:string {
  let $elems := $doc//*
  return string-join(
    (for $ln in sort(distinct-values($elems ! local-name(.)))
     return $ln || '=' || count($elems[local-name(.) = $ln])), ';')
};

(:~
 : Compares the two files and returns "VERDICT|detail".
 : @return the verdict
 :)
declare function local:verdict() as xs:string {
  let $atxt := try { file:read-text($a) } catch * { () }
  let $btxt := try { file:read-text($b) } catch * { () }
  let $an := if (exists($atxt)) { local:norm($atxt) }
  let $bn := if (exists($btxt)) { local:norm($btxt) }
  return
    if (($an, '')[1] = '' and ($bn, '')[1] = '') then "IDENTICAL|both empty (side-file writers)"
    else if (($an, '')[1] = '') then "MISSING|first output empty/absent"
    else if (($bn, '')[1] = '') then "MISSING|second output empty/failed"
    else if ($an = $bn) then "IDENTICAL|text-equal after whitespace normalisation"
    else
      let $ja := try { parse-json($atxt) } catch * { () }
      let $jb := try { parse-json($btxt) } catch * { () }
      return
        if (exists($ja) and exists($jb)) then
          if (deep-equal($ja, $jb)) then "JSON-EQUAL|same data, different key order/formatting"
          else "JSON-DIFFER|same shape, differing values"
        else
          let $xa := try { parse-xml($atxt) } catch * { () }
          let $xb := try { parse-xml($btxt) } catch * { () }
          return
            if (exists($xa) and exists($xb)) then
              if (deep-equal(local:strip($xa), local:strip($xb)))
              then "XML-EQUAL|same structure and content (whitespace/prefix ignored)"
              else if (local:sig($xa) = local:sig($xb))
              then "XML-STRUCT|same element skeleton (" || count($xa//*) || " elems); values differ"
              else "XML-DIFFER|elems " || count($xa//*) || " vs " || count($xb//*) ||
                   ", root " || local-name($xa/*) || " vs " || local-name($xb/*)
            else
              "TEXT-DIFFER|lines " || count(tokenize($an, '&#xA;')) || " vs " || count(tokenize($bn, '&#xA;'))
};

local:verdict()
