/**
  * Copyright 2016 Lorand Szakacs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
var SGDomain = "www.suicidegirls.com";
var targetTabId = parseInt(window.location.search.substring(1));

window.addEventListener("load", () => {
  chrome.debugger.sendCommand({tabId : targetTabId}, "Network.enable");
  chrome.debugger.onEvent.addListener(onEvent);
});

window.addEventListener("unload", () => {
  chrome.debugger.detach({tabId : targetTabId});
});

/*
 * This thing accumulates all the requests that are to be displayed.
 */
var requests = {};

function onEvent(target, message, params) {
  if (targetTabId != target.tabId)
    return;

  if (message == "Network.requestWillBeSent" && params.request.url.includes(SGDomain)) {
    createRequestDivAndPushToMap(params);
  } else if (message == "Network.responseReceived") {
    addRequestAndResponseHeadersToDivsInQueue(params);
  }
}

function createRequestDivAndPushToMap(params) {
  var requestDiv = requests[params.requestId];
  if (!requestDiv) {
    var requestDiv = document.createElement("div");
    requestDiv.className = "requestResponsePair";
    requests[params.requestId] = requestDiv;
    var urlLine = document.createElement("div");
    urlLine.textContent = params.request.url;
    requestDiv.appendChild(urlLine);
  }

  var requestLine = document.createElement("div");
  requestLine.textContent = "\nRequest: " + params.request.method + "\nHeaders:\n\n";
  requestDiv.appendChild(requestLine);
  document.getElementById("container").appendChild(requestDiv);
}

function addRequestAndResponseHeadersToDivsInQueue(params) {
  var response = params.response;
  var requestDiv = requests[params.requestId];
  requestDiv.appendChild(formatHeaders(response.requestHeaders));

  var statusLine = document.createElement("div");
  statusLine.textContent = "\n--\nResponse: " + response.status + " " + response.statusText + "\nHeaders:\n\n";
  requestDiv.appendChild(statusLine);
  requestDiv.appendChild(formatHeaders(response.headers));
}

function formatHeaders(headers) {
  var text = "";
  for (name in headers) {
    text += name + ": " + headers[name] + "\n";
  }
  var authHeaders = formatAuthHeaders(headers);

  if(!isEmptyObject(authHeaders)) {
    text += "\n" + JSON.stringify(authHeaders, null, 2) + "\n";
  }

  var div = document.createElement("div");
  div.textContent = text;
  return div;
}

/*
 * Returns JSON object with properties corresponding to the
 * value from the "Cookie" and "Set-Cookie" headers respectively:
 *
 * {
 *   "sessionID": "long_ass_value",
 *   "csrfToken": "shorter_vaule"
 * }
 */
function formatAuthHeaders(headers) {
  var result = formatCookieHeader(headers);
  if(isEmptyObject(result)) {
    result = formatSetCookieHeaders(headers);
  }

  return result;
}

/*
 * As sent out by the client.
 * Ought to contain one single Header named "Cookie", eg:
 *
 * Cookie: burlesque_ad_closed=True; track_join=%7B%22isSpecialOffer%22%3Afalse%7D; csrftoken=GUbKehkaoltqRjoFLTA43mU1LLZN8fAE; sessionid=".eJxNjL0OgjAYRZHBwWhMfAoSEyLUQrvKrNuXuDWlX_mJ8iNtBwYTH100DCx3OPfc-_Hfr1UAByGdrYQzehC5VA_dIhxN-YPhv5mhCTNnbNfAJF471M_LLO8XB5U0FQQqPSWYFxzZmUSINEqZ4glVPJaM0ZSRSMVICgK7xbTG-9rzPBrzmDDY9nJsdGuFHXsNfpbBptStHmolJN7An8KFX77cQOo:1evRsl:Wjexgriv0C42XQ4-6CFL-FpCfsI"
 *
 */
function formatCookieHeader(headers) {
 var text = {};
  for (name in headers) {
    if(name === "Cookie") {
      text.sessionID = extractCookievalue("sessionid", headers[name]);
      text.csrfToken = extractCookievalue("csrftoken", headers[name]);
    }
  }
  return text;
}

/*
 * Received from server.
 * We receive a "Set-Cookie" header.
 *
 * But for some baffling reason it has a "\n" in it :/
 * sessionid=".eJxNjL0OgjAYRZHBwWhMfAoSEyLUQrvKrNuXuDWlX_mJ8iNtBwYTH100DCx3OPfc-_Hfr1UAByGdrYQzehC5VA_dIhxN-YPhv5mhCTNnbNfAJF471M_LLO8XB5U0FQQqPSWYFxzZmUSINEqZ4glVPJaM0ZSRSMVICgK7xbTG-9rzPBrzmDDY9nJsdGuFHXsNfpbBptStHmolJN7An8KFX77cQOo:1evSWo:NDLE7nfOYNQhV0nrmqAqCOTipQs"; expires=Mon, 26-Mar-2018 18:50:34 GMT; httponly; Max-Age=1209600; Path=/
 * csrftoken=GUbKehkaoltqRjoFLTA43mU1LLZN8fAE; expires=Mon, 11-Mar-2019 18:50:34 GMT; Max-Age=31449600; Path=/"
 *
 * like, wtf?
 *
 */
function formatSetCookieHeaders(headers) {
  var text = {};
  //what in the name of fuck is the type of this "headers"? :((((
  for (name in headers) {
    if(name === "Set-Cookie") {
      var value = headers[name].replace("\n", ";");
      console.log(value);
      text.sessionID = extractCookievalue("sessionid", value);
      text.csrfToken = extractCookievalue("csrftoken", value);
    }
  }
  return text;
}

/*
 * Given:
 * name = "csrftoken"
 * str  = "burlesque_ad_closed=True; track_join=%7B%22isSpecialOffer%22%3Afalse%7D; csrftoken=GUbKehkaoltqRjoFLTA43mU1LLZN8fAE; sessionid=\".eJxNjL0OgjAYRZHBwWhMfAoSEyLUQrvKrNuXuDWlX_mJ8iNtBwYTH100DCx3OPfc-_Hfr1UAByGdrYQzehC5VA_dIhxN-YPhv5mhCTNnbNfAJF471M_LLO8XB5U0FQQqPSWYFxzZmUSINEqZ4glVPJaM0ZSRSMVICgK7xbTG-9rzPBrzmDDY9nJsdGuFHXsNfpbBptStHmolJN7An8KFX77cQOo:1evRsl:Wjexgriv0C42XQ4-6CFL-FpCfsI\""
 *
 * Returns:
 * "GUbKehkaoltqRjoFLTA43mU1LLZN8fAE"
 *
 * Format of string:
 * pairs of $name=$value strings, separated by spaces and/or ';'
 *
 * ^^ maybe there should be a function like that already :((
 */
function extractCookievalue(name, str) {
  //this ought to yield pairs of $name=$value
  var split = str.split(";").map(s => s.trim()).filter(s => s.includes("="));
  if(split.length === 0 ) {
    return null;
  } else {
    //expected to be "$name=$value"
    var group = split.find(s => s.startsWith(name));
    if(group === undefined) {
      return undefined;
    } else {
      var tuple = group.split("=");
      //for some odd-reason the "sessionid" is enclosed in quotes
      return tuple[1].replace("\"", "").replace("\"", "");
    }
  }
}
/*
 * because Object.keys(new Date()).length === 0;
 * we have to do some additional check
 * ultimately because JS fucking sucks
 */
function isEmptyObject(obj) {
  return Object.keys(obj).length === 0 && obj.constructor === Object;
}
