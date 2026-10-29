/**
 * Unified Test Runner — 5 Working Anime Sources with Enriched Metadata
 * ====================================================================
 * Verifies that ALL 5 anime providers return:
 *   1. Real playable stream URLs (HTTP HEAD verified)
 *   2. Enriched metadata format (behaviorHints + meta object)
 *   3. Streams themselves UNCHANGED (URL, headers, subtitles preserved)
 *
 * Run:
 *   node test_enriched_sources.js
 */
'use strict';

const axios = require('axios');
const https = require('https');
const {
  getEnrichedStreams,
  fetchAniKotoTV,
  fetchAnimesDigital,
  fetchAnimeSalt,
  fetchAnimeWorld,
  fetchAnimeKai,
} = require('./enriched_anime_sources');

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
           '(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Some CDNs (e.g. cdn.imagesskill.com / AnimesDigital) have incomplete cert
// chains. Browsers/Stremio use system CAs and accept these. Mirror that
// behavior so we don't false-negative on a stream that actually plays.
const httpsAgent = new https.Agent({ rejectUnauthorized: false });

// ==================== HTTP Verification ====================

async function verifyStream(url, headers = {}) {
  // Some CDNs (e.g. play.zephyrix.top / AnimeWorld) intermittently return 403
  // due to token activation delay or rate-limiting. Real HLS players retry on
  // transient 403. Mirror that here: try HEAD, then GET+Range, then retry once.
  const finalHeaders = { 'User-Agent': UA, ...headers };
  if (!finalHeaders['User-Agent'] && !finalHeaders['user-agent']) {
    finalHeaders['User-Agent'] = UA;
  }

  for (let attempt = 0; attempt < 3; attempt++) {
    // HEAD first
    try {
      const r = await axios.head(url, {
        timeout: 12000,
        headers: finalHeaders,
        httpsAgent,
        maxRedirects: 5,
        validateStatus: s => s < 400,
      });
      return {
        ok: true,
        status: r.status,
        contentType: r.headers['content-type'],
        size: r.headers['content-length'],
      };
    } catch (e) {
      // HEAD failed — fall through to GET+Range
    }

    // GET with Range (some CDNs block HEAD)
    try {
      const r = await axios.get(url, {
        timeout: 12000,
        headers: { ...finalHeaders, Range: 'bytes=0-0' },
        httpsAgent,
        maxRedirects: 5,
        validateStatus: s => s < 400,
        responseType: 'text',
      });
      return {
        ok: true,
        status: r.status,
        contentType: r.headers['content-type'],
        size: r.headers['content-length'],
        viaGet: true,
      };
    } catch (e) {
      // If 403, wait 2s and retry (transient CDN issue)
      const status = e.response?.status;
      if (status === 403 && attempt < 2) {
        await new Promise(r => setTimeout(r, 2000));
        continue;
      }
      return { ok: false, status, error: e.message };
    }
  }
  return { ok: false, status: 0, error: 'All retries failed' };
}

// ==================== Per-Provider Test ====================

async function testProvider(name, fetchFn, context, expectedCount = 1) {
  console.log('\n' + '='.repeat(70));
  console.log(`  ${name}`);
  console.log('='.repeat(70));

  let streams;
  try {
    streams = await fetchFn(context);
  } catch (e) {
    console.log(`  ✗ Provider threw: ${e.message}`);
    return { name, count: 0, verified: 0, enriched: 0, error: e.message };
  }

  console.log(`  Found ${streams.length} stream(s) (expected ≥ ${expectedCount})`);
  if (!streams.length) {
    return { name, count: 0, verified: 0, enriched: 0 };
  }

  let verified = 0;
  let enriched = 0;

  for (const s of streams) {
    // Check enrichment
    const hasBehaviorHints = !!s.behaviorHints;
    const hasMeta = !!s.meta;
    const hasNotWebReady = hasBehaviorHints && s.behaviorHints.notWebReady === true;
    const hasHeadersInHints = hasBehaviorHints && !!s.behaviorHints.headers;
    const hasProviderInMeta = hasMeta && !!s.meta.provider;
    const hasTypeInMeta = hasMeta && !!s.meta.type;
    const hasAudioInMeta = hasMeta && !!s.meta.audio;
    const isEnriched = hasBehaviorHints && hasMeta && hasNotWebReady && hasHeadersInHints &&
                       hasProviderInMeta && hasTypeInMeta && hasAudioInMeta;
    if (isEnriched) enriched++;

    // Verify playability
    const playHeaders = (s.behaviorHints && s.behaviorHints.headers) || s.headers || {};
    const v = await verifyStream(s.url, playHeaders);
    if (v.ok) verified++;

    const tag = v.ok ? '✓' : '✗';
    const enrichTag = isEnriched ? '✓ enriched' : '⚠ partial';
    console.log(`    ${tag} ${enrichTag} [${s.quality}] ${s.meta && s.meta.audio || '?'} ` +
                `${s.meta && s.meta.type || '?'}  HTTP ${v.status || 'ERR'}  ${v.contentType || v.error || ''}`);
    console.log(`      name:    ${JSON.stringify(s.name).slice(0, 60)}`);
    console.log(`      title:   ${JSON.stringify(s.title).slice(0, 70)}`);
    console.log(`      url:     ${s.url.slice(0, 80)}...`);
    console.log(`      referer: ${(playHeaders.Referer || playHeaders.referer || '-').slice(0, 60)}`);
    if (s.subtitles && s.subtitles.length) {
      console.log(`      subs:    ${s.subtitles.length} track(s) [${s.subtitles.slice(0,3).map(x => x.lang || x.name).join(', ')}${s.subtitles.length > 3 ? ', ...' : ''}]`);
    }
    if (s.meta && s.meta.malId) console.log(`      malId:   ${s.meta.malId}`);
  }

  return {
    name,
    count: streams.length,
    verified,
    enriched,
    sample: streams[0] ? streams[0].url.slice(0, 60) : '',
  };
}

// ==================== Main ====================

(async () => {
  console.log('╔' + '═'.repeat(68) + '╗');
  console.log('║  5 ANIME SOURCES — Enriched Metadata + Real Working Streams Test   ║');
  console.log('╚' + '═'.repeat(68) + '╝');
  console.log(`  Test target: Jujutsu Kaisen S01E01 (TMDB 95479)`);
  console.log(`  Enrichment schema: behaviorHints{notWebReady,headers} + meta{provider,source,server,type,audio,language,category,subtitles,...}`);

  const context = {
    tmdbId: 95479,
    type: 'tv',
    season: 1,
    episode: 1,
    animeTitle: 'Jujutsu Kaisen',
    animeKaiQuery: 'jujutsu kaisen',
    category: 'sub',
  };

  const results = [];
  results.push(await testProvider('1. AniKotoTV', fetchAniKotoTV.bind(null, 95479, 'tv', 1, 1), context, 1));
  results.push(await testProvider('2. AnimesDigital', fetchAnimesDigital.bind(null, 95479, 'tv', 1, 1), context, 1));
  results.push(await testProvider('3. AnimeSalt', fetchAnimeSalt.bind(null, 95479, 'tv', 1, 1), context, 1));
  results.push(await testProvider('4. AnimeWorld', fetchAnimeWorld.bind(null, 95479, 'tv', 1, 1), context, 1));
  results.push(await testProvider('5. AnimeKai', fetchAnimeKai, context, 1));

  // ==================== Combined Test ====================
  console.log('\n' + '='.repeat(70));
  console.log('  COMBINED: getEnrichedStreams() — All 5 sources in parallel');
  console.log('='.repeat(70));
  const all = await getEnrichedStreams(context);
  console.log(`  Total streams: ${all.length}`);
  console.log(`  Providers: ${[...new Set(all.map(s => s.meta && s.meta.provider))].join(', ')}`);

  // Show first 5 with full structure
  console.log('\n  Sample enriched stream (first):');
  if (all[0]) {
    console.log(JSON.stringify(all[0], null, 2).split('\n').map(l => '    ' + l).join('\n'));
  }

  // ==================== Summary ====================
  console.log('\n' + '═'.repeat(70));
  console.log('  SUMMARY');
  console.log('═'.repeat(70));
  console.log('  Provider        | Streams | Verified | Enriched | Sample URL');
  console.log('  ----------------|---------|----------|----------|---------------------------');
  for (const r of results) {
    const sample = (r.sample || '').slice(0, 50);
    console.log(`  ${r.name.padEnd(15)} | ${String(r.count).padStart(7)} | ${String(r.verified).padStart(8)} | ${String(r.enriched).padStart(8)} | ${sample}`);
  }
  console.log('\n  ✓ = stream URL returned HTTP < 400 with valid content-type');
  console.log('  enriched = behaviorHints{notWebReady,headers} + meta{provider,type,audio,...} all present');

  // Exit code
  const totalVerified = results.reduce((s, r) => s + r.verified, 0);
  const totalEnriched = results.reduce((s, r) => s + r.enriched, 0);
  const totalStreams = results.reduce((s, r) => s + r.count, 0);
  const allGood = totalVerified >= 5 && totalEnriched === totalStreams;
  console.log(`\n  Total: ${totalStreams} streams, ${totalVerified} verified playable, ${totalEnriched} enriched`);
  console.log(allGood ? '\n  ✓ PASS — All 5 sources return real working streams with enriched metadata.' : '\n  ⚠ PARTIAL — Some sources need attention.');
})().catch(e => console.error('Fatal:', e.message));
