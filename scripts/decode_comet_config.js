// Decode the Comet config embedded in PenguPlay's redirect URL.
// This tells us exactly what debrid service + API key PenguPlay uses.
'use strict';

const b64 = 'eyJtYXhSZXN1bHRzUGVyUmVzb2x1dGlvbiI6MCwibWF4U2l6ZSI6MCwiY2FjaGVkT25seSI6dHJ1ZSwic29ydENhY2hlZFVuY2FjaGVkVG9nZXRoZXIiOmZhbHNlLCJyZW1vdmVUcmFzaCI6dHJ1ZSwicmVzdWx0Rm9ybWF0IjpbImFsbCJdLCJlbmFibGVUb3JyZW50IjpmYWxzZSwiZGVkdXBsaWNhdGVTdHJlYW1zIjp0cnVlLCJzY3JhcGVEZWJyaWRBY2NvdW50VG9ycmVudHMiOmZhbHNlLCJkZWJyaWRTdHJlYW1Qcm94eVBhc3N3b3JkIjoiIiwibGFuZ3VhZ2VzIjp7InJlcXVpcmVkIjpbXSwiYWxsb3dlZCI6W10sImV4Y2x1ZGUiOltdLCJwcmVmZXJyZWQiOltdfSwicmVzb2x1dGlvbnMiOnt9LCJvcHRpb25zIjp7InJlbW92ZV9yYW5rc191bmRlciI6LTEwMDAwMDAwMDAwLCJhbGxvd19lbmdsaXNoX2luX2xhbmd1YWdlcyI6ZmFsc2UsInJlbW92ZV91bmtub3duX2xhbmd1YWdlcyI6ZmFsc2V9LCJkZWJyaWRTZXJ2aWNlcyI6W3sic2VydmljZSI6InRvcmJveCIsImFwaUtleSI6IjcwZjNjNjg5LTgyMWYtNGE5Yy04Mjc3LTE5ODUxOGQ5OWJjOCJ9XX0=';

const decoded = Buffer.from(b64, 'base64').toString('utf8');
console.log('===== COMET CONFIG (embedded in pengu.uk 307 redirect) =====');
console.log(decoded);
console.log('');
console.log('===== PRETTY =====');
console.log(JSON.stringify(JSON.parse(decoded), null, 2));
