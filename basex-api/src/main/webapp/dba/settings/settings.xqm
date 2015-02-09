(:~
 : Settings page.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/settings';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace G = 'dba/global' at '../modules/global.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace web = 'dba/web' at '../modules/web.xqm';

(:~ Top category :)
declare variable $_:CAT := 'settings';

(:~
 : Settings page.
 :)
declare
  %rest:GET
  %rest:path("dba/settings")
  %output:method("html")
function _:settings(
) as element() {
  web:check(),

  tmpl:wrap(map { 'top': $_:CAT },
    <tr>
      <td>
        <form action="settings" method="post">
          <h2>Settings » <button>Save</button></h2>
          <table>
            <tr>
              <td colspan='2'><h3>Querying</h3></td>
            </tr>
            <tr>
              <td><b>TIMEOUT:</b></td>
              <td><input name="timeout" type="number" value="{ $G:TIMEOUT }"/>
                <span class='note'> &#xa0;
                  …query timeout (seconds)
                </span>
              </td>
            </tr>
            <tr>
              <td><b>MEMORY:</b></td>
              <td><input name="memory" type="number" value="{ $G:MEMORY }"/>
                <span class='note'> &#xa0;
                  …memory limit (mb) during query execution
                </span>
              </td>
            </tr>
            <tr>
              <td><b>MAXCHARS:</b></td>
              <td><input name="maxchars" type="number" value="{ $G:MAX-CHARS }"/>
                <span class='note'> &#xa0;
                  …maximum number of characters in query results
                </span>
              </td>
            </tr>
            <tr>
              <td><b>MAXROWS:</b></td>
              <td><input name="maxrows" type="number" value="{ $G:MAX-ROWS }"/>
                <span class='note'> &#xa0;
                  …maximum number of displayed table rows
                </span>
              </td>
            </tr>
            <tr>
              <td><b>PERMISSION:</b></td>
              <td>
                <select name="permission">{
                  for $p in $G:PERMISSIONS
                  return element option { attribute selected { }[$p = $G:PERMISSION], $p }
                }</select>
                <span class='note'> &#xa0;
                  …for running queries
                </span>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Saves the settings.
 :)
declare
  %updating
  %rest:POST
  %rest:path("dba/settings")
  %output:method("html")
function _:settings-save(
) {
  web:check(),

  let $config := doc($G:CONFIG-XML)/config update (
    for $key in Request:parameter-names()
    (: skip empty values :)
    for $value in Request:parameter($key)[.]
    return replace value of node *[name() = $key] with $value
  )
  return (
    file:write($G:CONFIG-XML, $config),
    web:redirect("settings")
  )
};
