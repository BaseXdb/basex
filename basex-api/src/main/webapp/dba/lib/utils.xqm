(:~
 : Utility functions.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace utils = 'dba/utils';

import module namespace config = 'dba/config' at 'config.xqm';

(:~ Regular expression for backups. :)
declare variable $utils:BACKUP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)$';
(:~ Regular expression for backups. :)
declare variable $utils:BACKUP-ZIP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)\.zip$';

(:~
 : Parses a query.
 : @param  $query  query string
 : @param  $uri    base URI (optional)
 : @return parse result
 :)
declare function utils:query-parse(
  $query  as xs:string,
  $uri    as xs:string?
) as element() {
  xquery:parse($query, {
    'base-uri': $uri otherwise config:edited-file() otherwise config:editor-dir(),
    'plan'    : false(),
    'pass'    : true()
  })
};

(:~
 : Evaluates a query and returns the result.
 : @param  $query    query string
 : @param  $context  initial context item (can be empty)
 : @return serialized result of query
 :)
declare function utils:query(
  $query    as xs:string,
  $context  as item()?
) as xs:string {
  let $bindings := $context ! { '': . }
  let $result := xquery:eval($query, $bindings, utils:query-options())
  return utils:serialize($result)
};

(:~
 : Runs an updating query.
 : @param  $query  query string
 :)
declare %updating function utils:update(
  $query  as xs:string
) {
  xquery:eval-update($query, (), utils:query-options()),
  
  let $result := update:cache(true())
  return update:output(utils:serialize($result))
};

(:~
 : Serializes a value, considering the specified system limits.
 : @param  $value  value
 : @return string
 :)
declare function utils:serialize(
  $value  as item()*
) as xs:string {
  (: serialize more characters than requested, because limit represents number of bytes :)
  let $limit := config:get($config:MAXCHARS)
  (: indentation is a client-side preference, sent along with the request :)
  let $string := serialize($value, {
    'limit': $limit * 2 + 1,
    'indent': request:parameter('indent') = 'true',
    'method': 'basex'
  })
  return utils:chop($string, $limit)
};

(:~
 : Returns the options for evaluating a query.
 : @return options
 :)
declare %private function utils:query-options() as map(*) {
  {
    'timeout'   : config:get($config:TIMEOUT),
    'memory'    : config:get($config:MEMORY),
    'permission': config:get($config:PERMISSION),
    'base-uri'  : config:edited-file() otherwise config:editor-dir(),
    'pass'      : true()
  }
};

(:~
 : Returns the entries to be shown on the current page. While a table is being sorted, all entries
 : are returned, as sorting and paging are then performed in html:table.
 : @param  $entries  all entries
 : @param  $page     current page
 : @param  $sort     sort key
 : @return entries to display
 :)
declare function utils:slice(
  $entries  as item()*,
  $page     as xs:integer,
  $sort     as xs:string
) as item()* {
  if ($page and not($sort)) {
    let $max := config:get($config:MAXROWS)
    return subsequence($entries, ($page - 1) * $max + 1, $max)
  } else {
    $entries
  }
};

(:~
 : Chops a string result to the maximum number of allowed characters.
 : @param  $string  string
 : @param  $max     maximum number of characters
 : @return string
 :)
declare function utils:chop(
  $string  as xs:string,
  $max     as xs:integer
) as xs:string {
  if (string-length($string) > $max) {
    substring($string, 1, $max) || '...'
  } else {
    $string
  }
};

(:~
 : Resolves a relative path against a base directory. Guards file access against path traversal,
 : independent of the servlet container; raises a bad-request error if the path escapes the base.
 : @param  $dir   base directory
 : @param  $name  relative path
 : @return resolved native path, located within the base directory
 :)
declare function utils:safe-path(
  $dir   as xs:string,
  $name  as xs:string
) as xs:string {
  let $base := file:resolve-path($dir)
  let $path := file:resolve-path($name, $base)
  return if (starts-with($path, $base)) {
    $path
  } else {
    web:error(400, 'Invalid path: ' || $name)
  }
};

(:~
 : Returns a count info for the specified items.
 : @param  $items   items
 : @param  $name    name of item (singular form)
 : @param  $action  action label (past tense)
 : @return result
 :)
declare function utils:info(
  $items   as item()*,
  $name    as xs:string,
  $action  as xs:string
) as xs:string {
  let $count := count($items)
  return `{ $count } { $name || (if ($count != 1) then 's were ' else ' was ') || $action }.`
};

(:~
 : Capitalizes a string.
 : @param  $string  string
 : @return capitalized string
 :)
declare function utils:capitalize(
  $string  as xs:string
) as xs:string {
  upper-case(substring($string, 1, 1)) || substring($string, 2)
};

(:~
 : Convenience function for redirecting to another page from update operations.
 : @param  $url     URL
 : @param  $params  query parameters
 :)
declare %updating function utils:redirect(
  $url     as xs:string,
  $params  as map(*)
) {
  update:output(web:redirect($url, $params))
};
