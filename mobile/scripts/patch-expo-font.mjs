// expo-font 58.0.12 ships an android publication without a version, which
// breaks expo-autolinking's Gradle settings plugin. Fill it from package.json.
import fs from 'node:fs';
import { execFileSync } from 'node:child_process';

const configPath = 'node_modules/expo-font/expo-module.config.json';
const packagePath = 'node_modules/expo-font/package.json';
if (fs.existsSync(configPath) && fs.existsSync(packagePath)) {
  const config = JSON.parse(fs.readFileSync(configPath, 'utf8'));
  const pkg = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
  const publication = config.android?.publication;
  if (publication && !publication.version) {
    publication.version = pkg.version;
    fs.writeFileSync(configPath, `${JSON.stringify(config, null, 2)}\n`);
  }

  // expo-font 58.0.12 AAR declares minCompileSdk=37; this SDK only has 36.
  const aar = `node_modules/expo-font/local-maven-repo/host/exp/exponent/expo.modules.font/${pkg.version}/expo.modules.font-${pkg.version}.aar`;
  if (fs.existsSync(aar)) {
    const python = `
import zipfile, os, sys
p = sys.argv[1]
name = 'META-INF/com/android/build/gradle/aar-metadata.properties'
tmp = p + '.tmp'
with zipfile.ZipFile(p) as zin, zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        data = zin.read(item.filename)
        if item.filename == name:
            data = data.replace(b'minCompileSdk=37', b'minCompileSdk=36')
        zout.writestr(item, data)
os.replace(tmp, p)
`;
    execFileSync('python3', ['-c', python, aar], { stdio: 'ignore' });
  }
}
