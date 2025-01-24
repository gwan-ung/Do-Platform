const cacheName = "MetaMate-Do-1.0.2";
const contentToCache = [
    "../unity/Build/545359172fb7ef838f707896dce2b4bf.loader.js",
    "../unity/Build/0693d960d12ab5ee88bb251e8c857d11.framework.js",
    "../unity/Build/7db19b409df210f6945b501bc5a88d74.data",
    "../unity/Build/4ba69859c97d823ae79997db64cf0ebe.wasm",
    "../unity/TemplateData/style.css"

];

self.addEventListener('install', function (e) {
    console.log('[Service Worker] Install');
    
    e.waitUntil((async function () {
      const cache = await caches.open(cacheName);
      console.log('[Service Worker] Caching all: app shell and content');
      await cache.addAll(contentToCache);
    })());
});

self.addEventListener('fetch', function (e) {
    e.respondWith((async function () {
      let response = await caches.match(e.request);
      console.log(`[Service Worker] Fetching resource: ${e.request.url}`);
      if (response) { return response; }

      response = await fetch(e.request);
      const cache = await caches.open(cacheName);
      console.log(`[Service Worker] Caching new resource: ${e.request.url}`);
      cache.put(e.request, response.clone());
      return response;
    })());
});
