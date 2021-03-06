---
layout: docs
title: ST_PointFromWKB
category: geom2D/geometry-conversion
is_function: true
description: Well Known Binary &rarr; <code>POINT</code>
prev_section: ST_PointFromText
next_section: ST_PolyFromText
permalink: /docs/1.3/ST_PointFromWKB/
---

### Signatures

{% highlight mysql %}
GEOMETRY ST_PointFromWKB(binary wkb);
GEOMETRY ST_PointFromWKB(binary wkb, INT srid);
{% endhighlight %}

### Description

{% include from-wkb-desc.html type='POINT' %}
{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_PointFromWKB('0101000000000000000000F03F000000000000F03F');
-- Answer: POINT(1 1)

SELECT ST_PointFromWKB('000000000200000004401400000000000040140000000000003ff00000000000004000000000000000400800000000000040100000000000004058c000000000004008000000000000', 4326);
-- Answer: POINT(5 5)

SELECT ST_PointFromWKB(ST_AsBinary('LINESTRING(2 3, 4 4, 7 8)'::Geometry), 2154);
-- Answer: Provided WKB is not a POINT.
{% endhighlight %}

##### See also

* [`ST_LineFromWKB`](../ST_LineFromWKB), [`ST_PolyFromWKB`](../ST_PolyFromWKB)

* <a href="https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_PointFromWKB.java" target="_blank">Source code</a>
