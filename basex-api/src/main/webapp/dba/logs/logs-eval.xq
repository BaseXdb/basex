(:~
 : Returns the log entries that match a search input.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
import module namespace dba = 'dba/logs' at 'logs.xqm';

(:~ Search input. :)
declare variable $input as xs:string? external;
(:~ Name of the selected log file. :)
declare variable $date as xs:string external;
(:~ Table sort key. :)
declare variable $sort as xs:string external;
(:~ Current page. :)
declare variable $page as xs:integer external;
(:~ Timestamp to highlight. :)
declare variable $time as xs:string? external;
(:~ Regular expression of entries to hide. :)
declare variable $ignore as xs:string? external;
(:~ Column filters. :)
declare variable $filters as map(*) external;

dba:entries($input, $date, $sort, $page, $time, $ignore, $filters)
