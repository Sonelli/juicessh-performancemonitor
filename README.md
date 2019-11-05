# Ceylon

This is the 1.3.4-SNAPSHOT _"You'll Thank Me Later"_ release of the Ceylon 
command line tools. This is a production version of the platform.

Ceylon is a modern, modular, statically typed programming language 
for the Java and JavaScript virtual machines. The language features 
a flexible and very readable syntax, a unique and uncommonly elegant 
static type system, a powerful module architecture, and excellent 
tooling, including an awesome Eclipse-based IDE.

Ceylon enables the development of cross-platform modules which 
execute portably in both virtual machine environments. Alternatively, 
a Ceylon module may target one or the other platform, in which case 
it may interoperate with native code written for that platform.

Read more about Ceylon at <http://ceylon-lang.org>.

## Distribution layout

- `cmr`                 - *Ceylon Module Resolver* module
- `common`              - Common code module
- `compiler-java`       - JVM compiler module
- `compiler-js`         - JS compiler module
- `dist`                - Build files 
- `language`            - Ceylon language module
- `model`               - Type model module
- `runtime`             - Runtime module
- `typechecker`         - Typechecker module
- `langtools-classfile` - Java tools classfile module fork
- `tool-provider`       - Ceylon tool provider module
- `LICENSE-ASL`         - The Ceylon ASL license
- `LICENSE-GPL-CP`      - The Ceylon GPL/CP license
- `LICENSE-LGPL`        - The Ceylon LGPL license
- `README.md`           - This file

## Building the distribution

Go to the `dist` folder and follow the instructions in the [`BUILD.md`](/dist/BUILD.md) file.

## Source code

Source code is available from GitHub:

<http://github.com/ceylon>

## Issues

Bugs and suggestions may be reported in GitHub's issue tracker.

<http://github.com/ceylon/ceylon/issues>

## Systems where Ceylon is known to work

Since Ceylon is running on the JVM it should work on every platform 
that supports a Java 7 or 8 compatible JVM. However we have tested the 
following platforms to make sure it works:

### Linux

- Ubuntu "wily" 15.10 (64 bit) JDK 1.7.0_95 (IcedTea) Node 0.10.25
- Fedora 23 (64 bit), JDK 1.8.0_77 (OpenJDK)
- Fedora 22 (64 bit), JDK 1.8.0_72 (OpenJDK)
- Fedora 22 (64 bit), JDK 1.7.0_71 (Oracle)

### Windows

- Windows 10 Home (64 bit) 1.8.0_77
- Windows 7 (64 bit) 1.7.0_05 (Oracle)
- Windows Server 2008 R2 SP1 JDK 1.7.0_04

### OSX

- OSX 10 Lion (10.8.5) JDK 1.7.0_40 (Oracle) Node 0.10.17
- OSX 11 El Capitan (10.11.6) JDK 1.7.0_80 (Oracle) Node 0.10.35

## License

The Ceylon distribution is and contains work released

- partly under the ASL v2.0 as provided in the `LICENSE-ASL` file 
  that accompanied this code, and
- partly under the GPL v2 + Classpath Exception as provided in the 
  `LICENSE-GPL-CP` file that accompanied this code.

### License terms for 3rd Party Works

This software uses a number of other works, the license terms of 
which are documented in the `NOTICE` file that accompanied this code.

### Repository

The content of this code repository, [available here on GitHub][ceylon], 
is released under the ASL v2.0 as provided in the `LICENSE-ASL` file 
that accompanied this code.

[ceylon]: https://github.com/ceylon/ceylon

By submitting a "pull request" or otherwise contributing to this 
repository, you agree to license your contribution under the license 
mentioned above.

## Acknowledgement

We're deeply indebted to the community volunteers who contributed a 
substantial part of the current Ceylon codebase, working often in 
their own spare time.

Ceylon is a project of the [Eclipse Foundation](http://eclipse.org).