// Publish an Expo OTA update to the ARGO server (code/assets only, no APK).
// Run after: npx expo export --platform android --output-dir dist
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const root = path.resolve('.');
const dist = path.join(root, 'dist');
const meta = JSON.parse(fs.readFileSync(path.join(dist, 'metadata.json'), 'utf8'));
const platform = process.argv[2] || 'android';
const info = meta.fileMetadata[platform];
const appConfig = JSON.parse(fs.readFileSync(path.join(root, 'app.json'), 'utf8'));
const runtimeVersion = process.env.TW_RUNTIME_VERSION ||
  (appConfig.expo.runtimeVersion && appConfig.expo.runtimeVersion.policy === 'appVersion'
    ? appConfig.expo.version
    : String(appConfig.expo.runtimeVersion || appConfig.expo.version));
const baseUrl = process.env.TW_UPDATE_BASE_URL || 'https://v3310666.hosted-by-vdsina.ru';
const target = process.env.TW_UPDATES_DIR || '/root/text-world/data/updates';
const assetsDir = path.join(target, 'assets');
fs.mkdirSync(assetsDir, { recursive: true });

const hashFile = (file) =>
  crypto.createHash('sha256').update(fs.readFileSync(file)).digest('base64url');

const copy = (relative, name) => {
  const source = path.join(dist, relative);
  const destination = path.join(assetsDir, name);
  fs.copyFileSync(source, destination);
  return hashFile(destination);
};

const bundleName = path.basename(info.bundle);
const launchHash = copy(info.bundle, bundleName);
const assets = [];
const seen = new Set();
for (const entry of info.assets) {
  const base = path.basename(entry.path);
  if (seen.has(base)) continue;
  seen.add(base);
  const ext = entry.ext || 'bin';
  const name = `${base}.${ext}`;
  const contentType = ext === 'png' ? 'image/png' : ext === 'jpg' || ext === 'jpeg' ? 'image/jpeg'
    : ext === 'webp' ? 'image/webp' : ext === 'ttf' ? 'font/ttf' : 'application/octet-stream';
  assets.push({
    hash: copy(entry.path, name),
    key: base,
    contentType,
    fileExtension: `.${ext}`,
    url: `${baseUrl}/api/updates/assets/${name}`,
  });
}

const manifest = {
  id: crypto.randomUUID(),
  createdAt: new Date().toISOString(),
  runtimeVersion,
  launchAsset: {
    hash: launchHash,
    key: bundleName,
    contentType: 'application/javascript',
    url: `${baseUrl}/api/updates/assets/${bundleName}`,
  },
  assets,
  metadata: {},
  extra: {},
};
fs.writeFileSync(path.join(target, 'manifest.json'), JSON.stringify(manifest, null, 2));
console.log(`published ${manifest.id}: 1 bundle + ${assets.length} assets`);
