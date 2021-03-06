---
layout: docs
title: DoubleRange
category: system/version
is_function: true
description: Return an array of doubles
prev_section: system
next_section: IntegerRange
permalink: /docs/1.2/DoubleRange/
---

### Signatures

{% highlight mysql %}
DOUBLE DoubleRange(DOUBLE begin, DOUBLE end);
DOUBLE DoubleRange(DOUBLE begin, DOUBLE end, DOUBLE step);
{% endhighlight %}

### Description

Return an array of doubles within the range [`start`-`end`[.

The default increment value is 1 but the user can set another one specifying the `step` parameter.

##### Remarks

* `end` is excluded from the array,
* `end` has to be greater than `begin` otherwise `DoubleRange` will throw an exception.

### Examples

{% highlight mysql %}
SELECT DoubleRange(2, 7);
-- Answer:
	2, 3, 4, 5, 6

SELECT DoubleRange(0, 6, 2);
-- Answer:
	0, 2, 4

SELECT DoubleRange(0, 1, 0.5);
-- Answer:
	0, 0.5
{% endhighlight %}

##### See also

* [`IntegerRange`](../IntegerRange)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/system/DoubleRange.java" target="_blank">Source code</a>
