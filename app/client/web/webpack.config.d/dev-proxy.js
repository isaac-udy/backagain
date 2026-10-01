// `wasmJsBrowserDevelopmentRun` serves the client from webpack's dev server. Forward the API and
// the live socket to a server started with `:app:server:run`, so the page calls its own origin
// exactly as it does in production.
if (config.devServer) {
    config.devServer.port = 8081;
    config.devServer.proxy = [
        {
            context: ['/api', '/ws'],
            target: 'http://localhost:8080',
            ws: true,
        },
    ];
    config.devServer.historyApiFallback = true;
}
