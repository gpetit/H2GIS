---
layout: docs
title: ST_RemovePoints
category: geom2D/edit-geometries
is_function: true
description: Return a Geometry with vertices less
prev_section: ST_RemoveHoles
next_section: ST_RemoveRepeatedPoints
permalink: /docs/1.3/ST_RemovePoints/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_RemovePoints(GEOMETRY geom, POLYGON poly);
{% endhighlight %}

### Description

Remove all coordinates of `geom` located within `poly`.
Returns `NULL` if all coordinates are removed.

<div class="note warning">
    <h5>May produce invalid Geometries. Use with caution.</h5>
</div>

{% include type-warning.html type='GEOMETRYCOLLECTION' %}

### Examples

{% highlight mysql %}
SELECT ST_RemovePoints('MULTIPOINT((5 5), (3 1))',
                      ST_Buffer('POINT(4 2)', 2));
-- Answer: MULTIPOINT((5 5))
{% endhighlight %}

<img class="displayed" src="../ST_RemovePoint_1.png"/>

{% highlight mysql %}
-- In the following two examples, we get the same result whether we
-- use ST_Buffer or a POLYGON:
SELECT ST_RemovePoints('POLYGON((0 1, 5 4, 5 7, 2 6, 0 1))',
                      ST_Buffer('POINT(6 8)', 1.5));
-- Answer: POLYGON((0 1, 5 4, 2 6, 0 1))

SELECT ST_RemovePoints('POLYGON((0 1, 5 4, 5 7, 2 6, 0 1))',
                      'POLYGON((4 8, 6 8, 6 6, 4 6, 4 8))');
-- Answer: POLYGON((0 1, 5 4, 2 6, 0 1))
{% endhighlight %}

<img class="displayed" src="../ST_RemovePoint_2.png"/>

#####POLYGON with holes

{% highlight mysql %}
SELECT ST_RemovePoints('POLYGON((1 1, 1 6, 5 6, 5 1, 1 1),
                               (3 4, 3 5, 4 5, 4 4, 3 4),
                               (2 3, 3 3, 3 2, 2 2, 2 3))',
                       ST_Buffer('POINT(6 7)', 4.5));
-- Answer: POLYGON((1 1, 1 6, 5 1, 1 1), (2 3, 3 3, 3 2, 2 2, 2 3))
{% endhighlight %}

<img class="displayed" src="../ST_RemovePoint_3.png"/>

{% highlight mysql %}
-- A hole is converted to a POLYGON:
SELECT ST_RemovePoints('POLYGON((1 1, 1 6, 5 6, 5 1, 1 1),
                               (3 4, 3 5, 4 5, 4 4, 3 4))',
                    ST_Buffer('POINT(6 7)', 3));
-- Answer: POLYGON((1 1, 1 6, 5 1, 1 1), (3 4, 3 5, 4 4, 3 4))
{% endhighlight %}

<img class="displayed" src="../ST_RemovePoint_4.png"/>

{% highlight mysql %}
-- Here the resulting POLYGON is not valid:
SELECT ST_RemovePoints('POLYGON((1 1, 1 6, 5 6, 5 1, 1 1),
                               (2 2, 2 5, 4 5, 4 2, 2 2))',
                      ST_Buffer('POINT(4 7)', 2));
-- Answer: POLYGON((1 1, 1 6, 5 1, 1 1), (2 2, 2 5, 4 5, 4 2, 2 2))
{% endhighlight %}

<img class="displayed" src="../ST_RemovePoint_5.png"/>

{% highlight mysql %}
-- In the following four examples, we use larger and larger
-- buffers:
SELECT ST_RemovePoints(
            'LINESTRING(0 3, 1 1, 3 3, 5 2, 5 4,
                        6 5, 7 6, 7 7, 6 8)',
            ST_Buffer('POINT(3 4)', 1.01));
-- Answer:   LINESTRING(0 3, 1 1,      5 2, 5 4,
--                      6 5, 7 6, 7 7, 6 8)

SELECT ST_RemovePoints(
            'LINESTRING(0 3, 1 1, 3 3, 5 2, 5 4,
                        6 5, 7 6, 7 7, 6 8)',
            ST_Buffer('POINT(3 4)', 2.01));
-- Answer:   LINESTRING(0 3, 1 1,      5 2,
--                      6 5, 7 6, 7 7, 6 8)

SELECT ST_RemovePoints(
            'LINESTRING(0 3, 1 1, 3 3, 5 2, 5 4,
                        6 5, 7 6, 7 7, 6 8)',
            ST_Buffer('POINT(3 4)', 3.01));
-- Answer:   LINESTRING(0 3, 1 1,
--                      6 5, 7 6, 7 7, 6 8)

-- Here all points are removed:
SELECT ST_RemovePoints(
            'LINESTRING(0 3, 1 1, 3 3, 5 2, 5 4,
                        6 5, 7 6, 7 7, 6 8)',
            ST_Buffer('POINT(3 4)', 6));
-- Answer: NULL
{% endhighlight %}

<img class="displayed" src="../ST_RemovePoint_6.png"/>

##### See also

* [`ST_AddPoint`](../ST_AddPoint), [`ST_RemoveRepeatedPoints`](../ST_RemoveRepeatedPoints),
[`ST_RemoveHoles`](../ST_RemoveHoles)
* <a href="https://github.com/orbisgis/h2gis/blob/v1.3.0/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_RemovePoints.java" target="_blank">Source code</a>
