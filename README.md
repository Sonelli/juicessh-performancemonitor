Ceylon bootstrap
================

License
-------

The content of this repository is released under the ASL v2.0
as provided in the LICENSE file that accompanied this code.

By submitting a "pull request" or otherwise contributing to this repository, you
agree to license your contribution under the license mentioned above.

Directory structure
-------------------

* `src`       - the Ceylon bootstrap module sources
* `test-src`  - the Ceylon bootstrap module unit tests

Build the module
----------------

For setting up the development environment and compiling and building the distribution
take a look at [ceylon-dist](https://github.com/ceylon/ceylon-dist#ceylon-distribution).

If after having built the distribution you want to build the module run

    ant clean publish
    
And to run the tests type

    ant test

Once built, the module lives in this jar:

    ~/.ceylon/repo/ceylon/bootstrap/0.2/ceylon.bootstrap-0.2.jar

