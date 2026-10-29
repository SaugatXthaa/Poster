// Quick test: search AniList for One Piece using proper GraphQL
const query = `
  query ($search: String) {
    Page(page: 1, perPage: 5) {
      media(type: ANIME, search: $search) {
        id
        idMal
        title { english romaji }
        format
      }
    }
  }
`;

(async () => {
  const tests = ['One Piece', 'Jujutsu Kaisen', 'Naruto'];
  for (const title of tests) {
    try {
      const r = await fetch('https://graphql.anilist.co', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ query, variables: { search: title } }),
      });
      const d = await r.json();
      const media = d.data?.Page?.media || [];
      console.log(`--- ${title} ---`);
      for (const m of media.slice(0, 3)) {
        console.log(`  AniList ${m.id} | MAL ${m.idMal} | ${m.format} | ${(m.title.english || m.title.romaji)}`);
      }
    } catch (e) {
      console.log(`${title} ERR: ${e.message}`);
    }
    await new Promise(r => setTimeout(r, 800));
  }
})();
