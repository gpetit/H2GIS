---
layout: docs
title: GeoJsonRead
category: h2drivers
is_function: true
description: GeoJSON &rarr; Table
prev_section: GPXRead
next_section: GeoJsonWrite
permalink: /docs/1.3/GeoJsonRead/
---

### Signature

{% highlight mysql %}
GeoJsonRead(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Reads a [GeoJSON][wiki] file from `path` and creates the
corresponding spatial table `tableName`.

### Examples

{% highlight mysql %}
CALL GeoJsonRead('/home/user/data.geojson', 'NEW_DATA');
{% endhighlight %}

##### See also

* [`GeoJsonWrite`](../GeoJsonWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/io/geojson/GeoJsonRead.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/GeoJSON
