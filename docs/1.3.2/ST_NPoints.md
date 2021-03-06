---
layout: docs
title: ST_NPoints
category: geom2D/properties
is_function: true
description: Return the number of points in a Geometry
prev_section: ST_IsValidReason
next_section: ST_NumGeometries
permalink: /docs/1.3.2/ST_NPoints/
---

### Signature

{% highlight mysql %}
INT ST_NPoints(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the number of points *(vertexes)* in a geometry (`geom`).

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_NPoints('POINT(2 2)');
-- Answer: 1

SELECT ST_NPoints('MULTIPOINT(2 2, 4 4)');
-- Answer: 2

-- ST_NPoints includes duplicate points in the count.
SELECT ST_NPoints('MULTIPOINT(2 2, 4 4, 4 4)');
-- Answer: 3

SELECT ST_NPoints('MULTILINESTRING((2 2, 4 4), (3 1, 6 3))');
-- Answer: 4

SELECT ST_NPoints('POLYGON((0 0, 10 0, 10 6, 0 6, 0 0),
                             (1 1, 2 1, 2 5, 1 5, 1 1),
                             (8 5, 8 4, 9 4, 9 5, 8 5))');
-- Answer: 15
{% endhighlight %}

##### See also

* [`ST_NumPoints`](../ST_NumPoints)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/properties/ST_NPoints.java" target="_blank">Source code</a>
