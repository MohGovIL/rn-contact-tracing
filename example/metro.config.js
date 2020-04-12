/**
 * Metro configuration for React Native
 * https://github.com/facebook/react-native
 *
 * @format
 */

const path = require('path');

module.exports = {
    resolver: {
        extraNodeModules: {
            'rn-contact-tracing': path.resolve(__dirname, '../'),
            'react-native': path.resolve(__dirname, 'node_modules/react-native'),
        },
    },
    projectRoot: path.resolve(__dirname, './'),
    watchFolders: [
        __dirname,
        path.resolve(__dirname, "../"),
    ],
    transformer: {
        getTransformOptions: async () => ({
            transform: {
                experimentalImportSupport: false,
                inlineRequires: false,
            },
        }),
    },
};
