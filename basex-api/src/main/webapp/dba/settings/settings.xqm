(:~
 : Settings.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/settings';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'settings';

(:~
 : Settings.
 : @param  $info  info string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/settings')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
function dba:settings(
  $info  as xs:string?
) as element(html) {
  let $system := html:properties(db:system())
  (: boundary between global and local options (keyed by name, not position) :)
  let $local := $system/tr[th/h3 = 'LOCALOPTIONS']
  let $table-row := fn($label, $items) {
    <tr><td>{ $label, <br/>, $items }</td></tr>
  }
  let $option := fn($key, $values, $label) {
    $table-row($label,
      <select name='{ $key }'>{
        let $selected := config:get($key)
        for $value in $values
        return element option { attribute selected { }[$value = $selected], $value }
      }</select>
    )
  }
  let $number := fn($key, $label) {
    $table-row($label, <input type='number' name='{ $key }' value='{ config:get($key) }'/>)
  }
  let $fixed-table := fn($rows) {
    <table class='fixed'>{
      (: 'fixed': long values are truncated and expanded via click :)
      <colgroup><col style='width: 40%'/><col/></colgroup>,
      $rows
    }</table>
  }
  let $map-table := fn($map) {
    $fixed-table(
      for $key in sort(map:keys($map))
      return <tr>
        <td><b>{ $key }</b></td>
        <td>{ $map($key) }</td>
      </tr>
    )
  }
  return (
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <h2>Settings » { html:button('settings-save', 'Save') }</h2>
          <h3>Queries</h3>
          <table>{
            $number($config:TIMEOUT, 'Timeout, in seconds (0 = disabled)'),
            $number($config:MEMORY, 'Memory limit, in MB (0 = disabled)'),
            $number($config:MAXCHARS, 'Maximum output size'),
            $option($config:PERMISSION, $config:PERMISSIONS, 'Permission')
          }</table>
          <h3>Tables</h3>
          <table>{
            $number($config:MAXROWS,  'Displayed table rows')
          }</table>
        </form>
      </td>
      <td class='vertical'/>
      <td>
        <form method='post' autocomplete='off'>
          <h2>Global Options » { html:button('settings-gc', 'GC') }</h2>
          { $fixed-table($local/preceding-sibling::tr[not(th)]) }
        </form>
      </td>
      <td class='vertical'/>
      <td>
        <h2>Local Options</h2>
        { $fixed-table($local/following-sibling::tr) }
      </td>
      <td class='vertical'/>
      <td class='collapsed'>
        <h2>Environment Variables</h2>
        { $map-table(map:build(available-environment-variables(), value := environment-variable#1)) }
      </td>
      <td class='vertical'/>
      <td class='collapsed'>
        <h2>System Properties</h2>
        { $map-table(proc:property-map()) }
      </td>
    </tr>
  ) => html:wrap({ 'header': $dba:CAT, 'info': $info })
};

(:~
 : Saves the settings.
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/settings-save')
function dba:settings-save(
) as element(rest:response) {
  config:save(html:parameters()),
  web:redirect($dba:CAT, { 'info': 'Settings were saved.' })
};
