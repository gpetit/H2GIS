---
layout: docs
title: ST_Point
category: geom2D/geometry-creation
is_function: true
description: Construct a <code>POINT</code> from two or three coordinates
prev_section: ST_OctogonalEnvelope
next_section: ST_RingBuffer
permalink: /docs/1.4.0/ST_Point/
---

### Signatures

{% highlight mysql %}
POINT ST_Point(DOUBLE x, DOUBLE y);
POINT ST_Point(DOUBLE x, DOUBLE y, DOUBLE z);
{% endhighlight %}

### Description

Constructs a `POINT` from `x` and `y` (and possibly `z`).

<div class="note warning">
  <h5>This function is an alias for the <a href="/docs/1.4.0/ST_MakePoint">ST_MakePoint</a> function.</h5>
</div>


### Examples

{% highlight mysql %}
SELECT ST_Point(1.4, -3.7);
-- Answer:     POINT(1.4 -3.7)
{% endhighlight %}

<img class="displayed" src="../ST_MakePoint_1.png"/>

{% highlight mysql %}
SELECT ST_Point(1.4, -3.7, 6.2);
-- Answer:     POINT(1.4 -3.7 6.2)
{% endhighlight %}

<img class="displayed" src="../ST_MakePoint_2.png"/>

##### See also

* [`ST_MakePoint`](../ST_MakePoint)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/create/ST_Point.java" target="_blank">Source code</a>
