(:~
 : Utility functions.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace utils = 'dba/utils';

import module namespace config = 'dba/config' at 'config.xqm';

(:~ Regular expression for backups. :)
declare variable $utils:BACKUP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)$';
(:~ Regular expression for backups. :)
declare variable $utils:BACKUP-ZIP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)\.zip$';

(:~
 : Evaluates a query and returns the result.
 : @param  $query    query string
 : @param  $context  initial context item (can be empty)
 : @return serialized result of query
 :)
declare function utils:query(
  $query    as xs:string?,
  $context  as item()?
) as xs:string {
  let $bindings := $context ! map { '': $context }
  let $result := xquery:eval($query, $bindings, utils:query-options())
  return utils:serialize($result)
};

(:~
 : Runs an updating query.
 : @param  $query  query string
 : @return empty sequence
 :)
declare %updating function utils:update-query(
  $query  as xs:string?
) as empty-sequence() {
  xquery:eval-update($query, (), utils:query-options()),
  
  let $result := update:cache(true())
  return update:output(utils:serialize($result))
};

(:~
 : Finalizes the result of an evaluated query.
 : @param  $result  query result
 : @return empty sequence
 :)
declare function utils:serialize(
  $result  as item()*
) as xs:string {
  (: serialize more characters than requested, because limit represents number of bytes :)
  let $limit := config:get($config:MAXCHARS)
  let $indent := config:get($config:INDENT)
  let $string := serialize($result, map {
    'limit': $limit * 2 + 1,
    'method': 'basex',
    'indent': $indent
  })
  return utils:chop($string, $limit)
};

(:~
 : Returns the options for evaluating a query.
 : @return options
 :)
declare %private function utils:query-options() as map(*) {
  map {
    'timeout'   : config:get($config:TIMEOUT),
    'memory'    : config:get($config:MEMORY),
    'permission': config:get($config:PERMISSION),
    'base-uri'  : config:directory() || '/' || config:file()
  }
};

(:~
 : Returns the index of the first result to generate.
 : @param  $page  current page
 : @param  $sort  sort key
 : @return last result
 :)
declare function utils:start(
  $page  as xs:integer,
  $sort  as xs:string
) as xs:integer {
  if($page and not($sort)) then (
    ($page - 1) * config:get($config:MAXROWS) + 1
  ) else (
    1
  )
};

(:~
 : Returns the index of the last result to generate.
 : @param  $page  current page
 : @param  $sort  sort key
 : @return last result
 :)
declare function utils:end(
  $page  as xs:integer,
  $sort  as xs:string
) as xs:integer {
  if($page and not($sort)) then (
    $page * config:get($config:MAXROWS)
  ) else (
    999999999
  )
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
  if(string-length($string) > $max) then (
    substring($string, 1, $max) || '...'
  ) else (
    $string
  )
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
  return $count || ' ' || $name || (if($count != 1) then 's were ' else ' was ') || $action || '.'
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
) as empty-sequence() {
  update:output(web:redirect($url, $params))
};
