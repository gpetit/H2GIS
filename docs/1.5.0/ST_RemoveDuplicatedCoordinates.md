---
layout: docs
title: ST_RemoveDuplicatedCoordinates
category: geom2D/edit-geometries
is_function: true
description: Remove duplicated coordinates from a Geometry
prev_section: ST_Normalize
next_section: ST_RemoveHoles
permalink: /docs/1.5.0/ST_RemoveDuplicatedCoordinates/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_RemoveDuplicatedCoordinates(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the given `geometry` without duplicated coordinates.

### Examples

{% highlight mysql %}
SELECT ST_RemoveDuplicatedCoordinates('
             MULTIPOINT((4 4), (1 1), (1 0), (0 3), (4 4))');
-- Answer:   MULTIPOINT ((4 4), (1 1), (1 0), (0 3)) 

SELECT ST_RemoveDuplicatedCoordinates('
             MULTIPOINT((4 4), (1 1), (1 0), (1 1), (4 4), (0 3), (4 4))');
-- Answer:   MULTIPOINT ((4 4), (1 1), (1 0), (0 3))

SELECT ST_RemoveDuplicatedCoordinates('
             LINESTRING(4 4, 1 1, 1 1)');
-- Answer:   LINESTRING (4 4, 1 1)  

SELECT ST_RemoveDuplicatedCoordinates('
             POLYGON((4 4, 1 1, 1 1, 0 0, 4 4))');
-- Answer:   POLYGON ((4 4, 1 1, 0 0, 4 4)) 

SELECT ST_RemoveDuplicatedCoordinates(
        'GEOMETRYCOLLECTION(
             POLYGON((1 2, 4 2, 4 6, 1 6, 1 6, 1 2)),
             MULTIPOINT((4 4), (1 1), (1 0), (1 1)))');
-- Answer: GEOMETRYCOLLECTION (
--           POLYGON ((1 2, 4 2, 4 6, 1 6, 1 2)), 
--           MULTIPOINT ((4 4), (1 1), (1 0))) 
{% endhighlight %}

##### See also

* [`ST_RemovePoints`](../ST_RemovePoints), [`ST_RemoveRepeatedPoints`](../ST_RemoveRepeatedPoints)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_RemoveDuplicatedCoordinates.java" target="_blank">Source code</a>
