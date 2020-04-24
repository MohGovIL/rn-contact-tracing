const shell = require('shelljs');

shell.cd('./example/ios');
shell.rm('-rf', `./Pods`);

install();

async function install() {
  await shell.exec(`pod install`);
}
