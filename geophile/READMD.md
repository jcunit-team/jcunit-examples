# JCUnit-Geophile
```JCUnit-Geophile``` is a project to illustrate how to practice "Test design as code"
 with ```JCUnit```[[1]] and ```ActionUnit```[[2]].

This tests ```Geophile```[[0]], a *spatial join* library by Jack Orenstein, and 
```mymodel``` package, a suite of classes, interfaces, and constants to use 
```Geophile``` comfortably and bundled with ```JCUnit-Geophile```.

# Directory layout

```
    src/
      main/
        ```mymodel``` package is stored under main
      test/
        "test design as code" examples with JCUnit + ActionUnit
```

# "Test design as code"
(t.b.d.)

# About SUT
## About Geophile

(t.b.d.)
## About ```mymodel``` package

```mymodel``` package models objects in a 2d space and how to store them in an index,
how the index stores, looks up, or remove them in/from it.
Most elements of ```mymodel``` package are derived from examples bundled with ```Geophile```
library as a part of its tests.

* **Record:** 
* **Index:** This class is implemented based on ```TreeIndex``` provided by ```Geophile```.
* **IndexCursor:** This class is implemented based on ```TreeIndexCursor``` provided by ```Geophile```.

* Spatial objects
  * **Box:** represents a rectangle ina a 2d space.
  * **Point:** represents a point in a 2d space.

* Spatial join filter
  * **SpatialJoinFilter.INSTANCE**
  

# About a test suite provided by JCUnit-Geophile

## Unit tests
Unit testing for ```Geophile``` is covered by the package itself.
(t.b.d.)
## Functionality tests
Following use cases are covered by ```JCUnit-Geophile``` test suite.
* Adding spatial objects to a spatial index
* Removing spatial objects from a spatial index
* Performing a spatial join
  * Spatial join with a spatial object 
  * Spatial join with another spatial index
  * Spatial join with itself (selfjoin)


# References
* [0] "Geophile"
* [1] "JCUnit"
* [2] "ActionUnit"

[0]: https://github.com/geophile/geophile
[1]: https://github.com/dakusui/jcunit
[2]: https://github.com/dakusui/actionunit