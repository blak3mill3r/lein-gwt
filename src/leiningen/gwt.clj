(ns leiningen.gwt
  (:require [clojure.contrib.duck-streams :as streams])
  (:require [clojure.contrib.string :as string])
  (:require [leiningen.classpath :as cp])
  )

(def default-options
  {:style "OBF"
   :localWorkers 2})

(def valid-compile-options
  #{ :logLevel              ;  The level of logging detail: ERROR, WARN, INFO, TRACE, DEBUG, SPAM, or ALL
     :workDir               ;  The compiler's working directory for internal use (must be writeable; defaults to a system temp dir)
     :gen                   ;  Debugging: causes normally-transient generated types to be saved in the specified directory
     :style                 ;  Script output style: OBF[USCATED], PRETTY, or DETAILED (defaults to OBF)
     :ea                    ;  Debugging: causes the compiled output to check assert statements
     :XdisableClassMetadata ;  EXPERIMENTAL: Disables some java.lang.Class methods (e.g. getName())
     :XdisableCastChecking  ;  EXPERIMENTAL: Disables run-time checking of cast operations
     :validateOnly          ;  Validate all source code, but do not compile
     :draftCompile          ;  Enable faster, but less-optimized, compilations
     :optimize              ;  Sets the optimization level used by the compiler.  0=none 9=maximum.
     :compileReport         ;  Create a compile report that tells the Story of Your Compile
     :strict                ;  Only succeed if no input files have errors
     :localWorkers          ;  The number of local workers to use when compiling permutations
     :war                   ;  The directory into which deployable output files will be written (defaults to 'war')
     :deploy                ;  The directory into which deployable but not servable output files will be written (defaults to 'WEB-INF/deploy' under the -war directory/jar, and may be the same as the -extra directory/jar)
     :extra                 ;  The directory into which extra files, not intended for deployment, will be written
    })

(def valid-devmode-options
  #{ :noserver              ;   Prevents the embedded web server from running
     :port                  ;   Specifies the TCP port for the embedded web server (defaults to 8888)
     :whitelist             ;   Allows the user to browse URLs that match the specified regexes (comma or space separated)
     :blacklist             ;   Prevents the user browsing URLs that match the specified regexes (comma or space separated)
     :logdir                ;   Logs to a file in the given directory, as well as graphically
     :logLevel              ;   The level of logging detail: ERROR, WARN, INFO, TRACE, DEBUG, SPAM, or ALL
     :gen                   ;   Debugging: causes normally-transient generated types to be saved in the specified directory
     :bindAddress           ;   Specifies the bind address for the code server and web server (defaults to 127.0.0.1)
     :codeServerPort        ;   Specifies the TCP port for the code server (defaults to 9997)
     :server                ;   Specify a different embedded web server to run (must implement ServletContainerLauncher)
     :startupUrl            ;   Automatically launches the specified URL
     :war                   ;   The directory into which deployable output files will be written (defaults to 'war')
     :deploy                ;   The directory into which deployable but not servable output files will be written (defaults to 'WEB-INF/deploy' under the -war directory/jar, and may be the same as the -extra directory/jar)
     :extra                 ;   The directory into which extra files, not intended for deployment, will be written
     :workDir               ;   The compiler's working directory for internal use (must be writeable; defaults to a system temp dir)
    })

(defn user-gwtc-options [project]    (merge default-options (:gwt-options project)))
(defn user-devmode-options [project] (merge (merge default-options (:gwt-options project)) (:gwt-options-devmode project) ))

(defn devmode-options [project] (filter #(valid-devmode-options (first %1)) (user-devmode-options project)))
(defn gwtc-options    [project] (filter #(valid-compile-options (first %1)) (user-gwtc-options project)))

(defn- to-str [v]
  (if (keyword? v)
    (.substring (str v) 1)
    (str v)))

(defn- to-opt-str [k]
  (let [s (to-str k)]
    (if (.startsWith s "-")
      s
      (str "-" (to-str s)))))

(defn- to-opts [raw-options]
  (reduce
    (fn [res [k v]]
      (let [r (conj res (to-opt-str k))]
        (if (string/blank? (to-str v))
          r
          (conj r (to-str v)))))
    []
    raw-options))

(defn- classpath [project]
  (string/join
    java.io.File/pathSeparatorChar
    (cp/get-classpath project)))

(defn- process-args [project start-class options]
  (java.util.ArrayList. (do
(println (concat
    ["java" "-cp" (classpath project) start-class]
    options
    (:gwt-modules project)) )
                          (concat
    ["java" "-cp" (classpath project) start-class]
    options
    (:gwt-modules project)) )))

(defn- invoke-and-tail [project start-class raw-options]
  (let [process-args (process-args project start-class (to-opts raw-options))
        p (-> (doto (ProcessBuilder. process-args) (.redirectErrorStream true))
                (.start))]
      (doseq [line (streams/read-lines (.getInputStream p))]
        (println line))))

(defn- gwtc [project]
  (invoke-and-tail project "com.google.gwt.dev.Compiler" (gwtc-options project)))

(defn- gwtdev [project]
  (invoke-and-tail project "com.google.gwt.dev.DevMode" (devmode-options project)))

(defn gwt [project & args]
  (if
    (= (first args) "dev")
    (gwtdev project)
    (gwtc project)))
