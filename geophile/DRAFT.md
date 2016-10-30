# "Test design as code": JCUnit
* Hiroshi Ukai, Rakuten, Inc., Japan
* Xiao Qu

**Abstract** -- (t.b.d.)
# I. Introduction
t.b.d. - 
* Backgroud:  
  * Challenges
    * test oracles
    * explosion of # of test cases
    * moving SUT design
  * Solutions
    * Combinatorial testing techniques
    * "Test design as code" (one application of "model based design")
    * Mechanism to reuse test oracles
      * Let user categorize test case
      * FSM spec
      * Test designer's skill
* Design considerations of JCUnit
  * "Stable" test suite
  * Portable
    * Work with Java 6
    * Strictly managed dependencies
  
* How to use
  * JUnit
  * Maven ecosystem
  * Annotations + Test Runner

* Models
  * Taxonomy of bugs to be detected by JCUnit
    * Unexpected output on given input
    * Unintended (hidden) states / Initialization error 
    * Unclear specification (documentation bug)
    * Improper error handling
  * Input/Output model
  * "Regular expression" model
  * Finite state machine model


# II. Testing simple input-output system 
**Quadratic equation solver**(t.b.d.)
* Defining factors and levels
* Defining constraints
* Negative test generation

Materials:
* http://jcunit.hatenablog.jp/entry/2016/03/09/074054
* http://jcunit.hatenablog.jp/entry/2016/03/09/220922
* http://jcunit.hatenablog.jp/entry/2016/03/14/080111
* http://jcunit.hatenablog.jp/entry/2016/04/10/173907

# III. Testing various parameters and scenarios that result in "same" expectation
**Geophile (?, not sure yet)** (t.b.d.)
                               
* Building a driver
* Defining "regular expression" for scenarios to cover
* Make use of ActionUnit
  

Materials:
* https://github.com/jcunit-team/jcunit-examples/tree/geophile-support/geophile


# IV. Testing FSM
**Rakuten Search UI** (or **simple CRUD object**? (t.b.d.))

* Building  a driver using selenium.
* Defining  a FSM spec.

Materials:
* In Rakuten's coorporate git server... need permission from my boss. 
* Create something....  

# V. Evaluation
(t.b.d.)
* Size of test suites
* Model coverage
* Code coverage
* Mutation coverage?
* Feed backs from fields

Materials:
* http://jcunit.hatenablog.jp/entry/2014/03/15/213151

# VI. Conclusion
(t.b.d. - JCUnit rocks)

## Future works
* Capture/replay
* Go to Java 8. It's not age of Java 6 anymore.
* AETG support
* More reliable constraint handling algorithm

# References
* [0] "Geophile", Jack Orenstein
* [1] "JCUnit", Hiroshi Ukai, Xiao Qu
* [2] "ActionUnit", Hiroshi Ukai
* [3] "IPO",... t.b.d.
* [4] "AETG",... t.b.d.
* [5] "Practical model based testing - A tools approach", Mark Utting and Bruno Legeard
* [6] "Selenium WebDriver"

[0]: https://github.com/geophile/geophile
[1]: https://github.com/dakusui/jcunit
[2]: https://github.com/dakusui/actionunit
[3]: t.b.d.
[4]: t.b.d.
[5]: t.b.d.
[6]: t.b.d.