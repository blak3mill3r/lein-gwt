# lein-gwt

A Leiningen plugin for running the GWT compiler and GWT development mode.

## Usage

Add a list of the GWT modules you want to compile to your project.clj:

```clojure
     (defproject myproject "1.0-SNAPSHOT"
       :gwt-modules ["mypackage.MyGWTModule"])
```

### Passing arguments

To customize the compiler or devmode invocation, add a map `:gwt-options` to project.clj. See [the GWT documentation for the options you can pass](http://code.google.com/webtoolkit/doc/1.6/FAQ_DebuggingAndCompiling.html#What_are_the_options_that_can_be_passed_to_the_compiler), e.g.:
You can put options for either com.google.gwt.dev.Compiler or com.google.gwt.dev.DevMode in there. If you need to specify a different value for an option which is accepted by both the compiler and devmode, you can place overrides in :gwt-options-devmode
 
```clojure
    (defproject myproject "1.0-SNAPSHOT"
      :gwt-modules ["mypackage.MyGWTModule"]
      :gwt-options {:localWorkers 2, :war "my/output/dir", :logdir "some/where"}
      :gwt-options-devmode {:logLevel DEBUG}
      )
```

Note, logdir is only a valid option for DevMode, and localWorkers is only valid for the compiler. That is okay, the plugin will handle it. Only when you need, for example, a different logLevel option passed to DevMode than to the compiler, do you need to use :gwt-options-devmode, otherwise stuffing all options in :gwt-options works fine.

## Installation

This is not available on Clojars at the moment, you'll have to git clone, lein jar, mvn install:install-file it, and then put this in dev-dependencies in project.clj:

```clojure
     [lein-gwt "0.1.1"]
```

You'll probably want to depend on the GWT jars as well... they are conveniently available from maven,
so you can list them in project.clj, e.g.:
 
```clojure
     [com.google.gwt/gwt-user          "2.4.0" ]
     [com.google.gwt/gwt-dev           "2.4.0" ]
```

Then...
```bash
     $ lein deps
```

and then here's how to compile and start devmode, respectively:
```bash
     $ lein gwt
     $ lein gwt devmode
```

## License

Copyright (C) 2012 Blake Miller

Copyright (C) 2010 Tero Parviainen

Distributed under the MIT license (see [LICENSE](http://github.com/teropa/lein-gwt/blob/master/LICENSE)).
