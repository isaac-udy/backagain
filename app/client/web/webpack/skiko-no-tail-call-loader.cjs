// Safari 27.2 (WebKit bug 329421) passes NaN for a 0 or 1 Float argument when JavaScript calls a
// Wasm export in tail position from a function that Wasm called. Every export wrapper in skiko.mjs
// has that form, so Skia receives NaN sizes and the Compose canvas stays blank (CMP-10986).
//
// A call inside a `try` block is never in tail position (ECMA-262 HasCallInTailPosition), and
// terser keeps the block, unlike a `const r = f(); return r` rewrite.
//
// Remove this loader and webpack.config.d/skiko-no-tail-call.js once Safari versions
// without the WebKit fix are out of the supported range.
const WRAPPER = /\(\.\.\.a\) => \((\w+) = loadedWasm\._\["\1"\]\)\(\.\.\.a\)/g;

module.exports = function (source) {
    let patched = 0;
    const result = source.replace(WRAPPER, function (match, name) {
        patched++;
        return '(...a) => { try { return (' + name + ' = loadedWasm._["' + name + '"])(...a) } catch (e) { throw e } }';
    });
    if (patched === 0) {
        throw new Error('skiko.mjs export wrappers changed format: the WebKit 329421 workaround was not applied');
    }
    return result;
};
