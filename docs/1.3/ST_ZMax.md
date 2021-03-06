---
layout: docs
title: ST_ZMax
category: geom3D/properties
is_function: true
description: Return the maximum z-value of a Geometry
prev_section: ST_Z
next_section: ST_ZMin
permalink: /docs/1.3/ST_ZMax/
---

### Signature

{% highlight mysql %}
DOUBLE ST_ZMax(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the maximum z-value of `geom`.

### Example

{% highlight mysql %}
SELECT ST_ZMax('LINESTRING(1 2 3, 4 5 6)');
-- Answer:    6.0
{% endhighlight %}

<img class="displayed" src="../ST_ZMax.png"/>

##### See also

* [`ST_XMin`](../ST_XMin), [`ST_XMax`](../ST_XMax), [`ST_YMax`](../ST_YMax), [`ST_YMin`](../ST_YMin), [`ST_ZMin`](../ST_ZMin)
* <a href="https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_ZMax.java" target="_blank">Source code</a>
