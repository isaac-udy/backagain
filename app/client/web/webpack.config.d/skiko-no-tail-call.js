// Rewrites skiko.mjs so that no export wrapper calls Wasm in tail position, which Safari 27.2
// miscompiles into a blank canvas. The loader file states the bug and the removal condition.
//
// `__dirname` is the generated package directory under the root build directory, so the loader is
// located by walking up to the repository root.
(function () {
    var fs = require('fs');
    var path = require('path');

    var root = __dirname;
    while (!fs.existsSync(path.join(root, 'settings.gradle.kts'))) {
        var parent = path.dirname(root);
        if (parent === root) throw new Error('settings.gradle.kts not found above ' + __dirname);
        root = parent;
    }

    config.module = config.module || {};
    config.module.rules = config.module.rules || [];
    config.module.rules.push({
        test: /[\\/]skiko\.mjs$/,
        loader: path.join(root, 'app/client/web/webpack/skiko-no-tail-call-loader.cjs')
    });

    // The minifier is free to restructure the wrappers, so the emitted JavaScript is checked too:
    // a wrapper of the form `(...a)=>(x=w._["x"])(...a)` fails the build.
    var TAIL_CALL_WRAPPER = /=>\s*\([\w$]+\s*=\s*[\w$]+\._(?:\.[\w$]+|\["[\w$]+"\])\)\(\.\.\.[\w$]+\)/;
    config.plugins = config.plugins || [];
    config.plugins.push({
        apply: function (compiler) {
            var webpack = compiler.webpack;
            compiler.hooks.thisCompilation.tap('SkikoNoTailCall', function (compilation) {
                compilation.hooks.processAssets.tap(
                    {
                        name: 'SkikoNoTailCall',
                        stage: webpack.Compilation.PROCESS_ASSETS_STAGE_SUMMARIZE
                    },
                    function (assets) {
                        Object.keys(assets).forEach(function (name) {
                            if (!/\.m?js$/.test(name)) return;
                            if (TAIL_CALL_WRAPPER.test(assets[name].source().toString())) {
                                compilation.errors.push(new webpack.WebpackError(
                                    name + ' contains a skiko export wrapper that calls Wasm in tail position'
                                ));
                            }
                        });
                    }
                );
            });
        }
    });
})();
