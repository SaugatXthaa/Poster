// curl-based fetch replacement. Provides a drop-in replacement for the
// global fetch() function that shells out to curl instead of using
// Node's undici. This is useful for sites that block Node's TLS
// fingerprint but allow curl through.
//
// Handles both text and binary responses correctly (important for
// WASM files, images, etc.).
//
// Usage:
//   var curlFetch = require("./curl_fetch");
//   curlFetch.install();   // replace global.fetch
//   // ... obfuscated code that calls fetch() will now use curl ...
//   curlFetch.uninstall(); // restore original fetch
//
// Or:
//   var result = await curlFetch.withCurlFetch(async () => {
//     return await obfuscatedScraper.getStreams(...);
//   });

"use strict";

var { execFile } = require("child_process");

var USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
  "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

var _originalFetch = null;

function curlFetch(url, options) {
  options = options || {};
  var method = (options.method || "GET").toUpperCase();
  var headers = Object.assign(
    {
      "User-Agent": USER_AGENT,
      Accept: "*/*",
      "Accept-Language": "en-US,en;q=0.9"
    },
    options.headers || {}
  );

  return new Promise(function (resolve, reject) {
    var args = [
      "-sSk",
      "--max-time", "25",
      "-L",
      "--compressed",
      "-A", headers["User-Agent"],
      "-H", "Accept: " + headers["Accept"],
      "-H", "Accept-Language: " + headers["Accept-Language"],
      "-w", "\n---CURL_META---\n%{http_code}\n%{content_type}"
    ];

    // Add custom headers
    Object.keys(headers).forEach(function (key) {
      if (key === "User-Agent" || key === "Accept" || key === "Accept-Language") return;
      args.push("-H", key + ": " + headers[key]);
    });

    // Add request body
    if (options.body && method !== "GET") {
      args.push("--data", String(options.body));
    }
    if (method !== "GET") {
      args.unshift("-X", method);
    }

    args.push(url);

    // Use encoding: null to get a Buffer (handles binary data correctly)
    execFile("curl", args, {
      encoding: null,  // Buffer, not string
      maxBuffer: 50 * 1024 * 1024,
      timeout: 30000,
      windowsHide: true
    }, function (err, stdoutBuffer) {
      if (err) {
        var error = new Error("curl failed for " + url + ": " + err.message);
        error.cause = err;
        reject(error);
        return;
      }

      // stdoutBuffer is a Buffer. Split on the meta marker.
      var metaMarker = "\n---CURL_META---\n";
      var metaMarkerBuffer = Buffer.from(metaMarker, "utf8");
      var metaIdx = -1;
      for (var i = 0; i <= stdoutBuffer.length - metaMarkerBuffer.length; i++) {
        var match = true;
        for (var j = 0; j < metaMarkerBuffer.length; j++) {
          if (stdoutBuffer[i + j] !== metaMarkerBuffer[j]) { match = false; break; }
        }
        if (match) { metaIdx = i; break; }
      }

      var bodyBuffer;
      var status = 200;
      var contentType = "text/html";

      if (metaIdx !== -1) {
        bodyBuffer = stdoutBuffer.subarray(0, metaIdx);
        var metaStr = stdoutBuffer.subarray(metaIdx + metaMarkerBuffer.length).toString("utf8");
        var metaLines = metaStr.split("\n");
        status = parseInt(metaLines[0], 10) || 200;
        contentType = metaLines[1] || "text/html";
      } else {
        bodyBuffer = stdoutBuffer;
      }

      // Determine if this is a text response
      var isText = /^text\/|json|javascript|xml|utf-8/i.test(contentType);

      // Pre-compute text version for text responses
      var bodyText = isText ? bodyBuffer.toString("utf8") : null;

      // Build a fetch-like Response object
      var response = {
        ok: status >= 200 && status < 300,
        status: status,
        statusCode: status,
        statusText: "",
        url: url,
        headers: {
          get: function (name) {
            if (String(name).toLowerCase() === "content-type") return contentType;
            if (String(name).toLowerCase() === "content-length") return String(bodyBuffer.length);
            return null;
          },
          forEach: function (fn) {
            fn(contentType, "content-type");
          }
        },
        body: isText ? bodyText : bodyBuffer,
        text: function () {
          return Promise.resolve(bodyBuffer.toString("utf8"));
        },
        json: function () {
          return Promise.resolve(JSON.parse(bodyBuffer.toString("utf8")));
        },
        arrayBuffer: function () {
          // Return a proper ArrayBuffer (not a Node Buffer)
          var ab = new ArrayBuffer(bodyBuffer.length);
          var view = new Uint8Array(ab);
          for (var k = 0; k < bodyBuffer.length; k++) {
            view[k] = bodyBuffer[k];
          }
          return Promise.resolve(ab);
        },
        blob: function () {
          return Promise.resolve({
            type: contentType,
            size: bodyBuffer.length,
            arrayBuffer: function () {
              return Promise.resolve(bodyBuffer.buffer);
            }
          });
        }
      };

      resolve(response);
    });
  });
}

// Install curl as the global fetch
function install() {
  if (_originalFetch === null) {
    _originalFetch = global.fetch;
  }
  global.fetch = curlFetch;
}

// Restore the original fetch
function uninstall() {
  if (_originalFetch !== null) {
    global.fetch = _originalFetch;
  }
}

// Run an async function with curl as fetch, then restore the original
function withCurlFetch(fn) {
  var wasInstalled = _originalFetch !== null;
  if (!wasInstalled) {
    install();
  }
  return Promise.resolve()
    .then(fn)
    .then(function (result) {
      if (!wasInstalled) uninstall();
      return result;
    }, function (err) {
      if (!wasInstalled) uninstall();
      throw err;
    });
}

module.exports = {
  curlFetch: curlFetch,
  install: install,
  uninstall: uninstall,
  withCurlFetch: withCurlFetch
};
