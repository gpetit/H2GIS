Process geometries
=================================

The following functions are available to process geometries:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_ISOVist`
     - Compute the visibility from a point
   * - :doc:`ST_LineIntersector`
     - Split an input LINESTRING with another geometry
   * - :doc:`ST_LineMerge`
     - Merges a collection of linear components to form maximal-length LINESTRING
   * - :doc:`ST_MakeValid`
     - Make a Geometry valid
   * - :doc:`ST_Node`
     - Add nodes on a geometry for each intersection
   * - :doc:`ST_Polygonize`
     - Create a MULTIPOLYGON from edges of Geometries
   * - :doc:`ST_PrecisionReducer`
     - Reduce a Geometry's precision
   * - :doc:`ST_RingSideBuffer`
     - Compute a ring buffer on one side
   * - :doc:`ST_SideBuffer`
     - Compute a single buffer on one side
   * - :doc:`ST_Simplify`
     - Simplify a Geometry
   * - :doc:`ST_SimplifyPreserveTopology`
     - Simplify a Geometry, preserving its topology
   * - :doc:`ST_Snap`
     - Snap two Geometries together
   * - :doc:`ST_Split`
     - Split Geometry A by Geometry B

.. toctree::
    :maxdepth: 1
    
    ST_ISOVist
    ST_LineIntersector
    ST_LineMerge
    ST_MakeValid
    ST_Node
    ST_Polygonize
    ST_PrecisionReducer
    ST_RingSideBuffer
    ST_SideBuffer
    ST_Simplify
    ST_SimplifyPreserveTopology
    ST_Snap
    ST_Split
