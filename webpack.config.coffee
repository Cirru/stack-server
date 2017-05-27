
webpack = require 'webpack'

module.exports =
  entry:
    main: './entry/bin'
  output:
    path: 'dist'
    filename: '[name].js'
  target: 'node'
  plugins: [
    new webpack.BannerPlugin
      banner: '#!/usr/bin/env node'
      raw: true
  ]
