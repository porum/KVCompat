# KVCompat

[![kvcompat](https://img.shields.io/badge/kvcompat-1.0.3-brightgreen.svg)](https://central.sonatype.com/artifact/io.github.porum/kvcompat/1.0.3)
[![kvcompat-flipper-plugin](https://img.shields.io/badge/kvcompat--flipper--plugin-1.0.3-brightgreen.svg)](https://central.sonatype.com/artifact/io.github.porum/kvcompat-flipper-plugin/1.0.3)
[![KVCompat Viewer](https://img.shields.io/badge/flipper--plugin--kvcompat-1.0.3-blueviolet.svg)](https://www.npmjs.com/package/flipper-plugin-kvcompat)

KVCompat 是对 Android 原生 SharedPreferences 和 Tencent 开源的 [MMKV](https://github.com/Tencent/MMKV) 的封装，并且提供了 [Flipper](https://github.com/facebook/flipper) 桌面端的插件。

1. 支持数据变化时实时更新 KVCompat Viewer
2. 支持监听 module 的创建，实时更新 KVCompat Viewer
3. KVCompat Viewer 支持查看 module 列表
4. KVCompat Viewer 支持选择指定 module 查看相应的数据
5. KVCompat Viewer 支持修改 module 的数据

## KVCompat Viewer

![screenshot](./readme_assets/screenrecord.gif)
