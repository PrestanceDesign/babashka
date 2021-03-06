(ns babashka.impl.clojure.java.shell
  {:no-doc true}
  (:require [clojure.java.shell :as shell]
            [sci.core :as sci]
            [sci.impl.namespaces :refer [copy-var]]
            [sci.impl.vars :as vars]))

(def shell-ns (vars/->SciNamespace 'clojure.java.shell nil))

(def sh-dir (sci/new-dynamic-var '*sh-dir* nil {:ns shell-ns}))
(def sh-env (sci/new-dynamic-var '*sh-env* nil {:ns shell-ns}))

(defn with-sh-dir*
  [_ _ dir & forms]
  `(binding [clojure.java.shell/*sh-dir* ~dir]
     ~@forms))

(defn with-sh-env*
  [_ _ env & forms]
  `(binding [clojure.java.shell/*sh-env* ~env]
     ~@forms))

(defn parse-args
  [args]
  (let [default-encoding "UTF-8" ;; see sh doc string
        default-opts {:out-enc default-encoding
                      :in-enc default-encoding
                      :dir @sh-dir
                      :env @sh-env}
        [cmd opts] (split-with string? args)
        opts (merge default-opts (apply hash-map opts))
        opts-seq (apply concat opts)]
    (concat cmd opts-seq)))

(defn sh [& args]
  (let [args (parse-args args)]
    (apply shell/sh args)))

(alter-meta! #'sh (constantly (meta #'shell/sh)))

(def shell-namespace
  {'*sh-dir* sh-dir
   '*sh-env* sh-env
   'with-sh-dir (with-meta with-sh-dir*
                  {:sci/macro true})
   'with-sh-env (with-meta with-sh-env*
                  {:sci/macro true})
   'sh (copy-var sh shell-ns)})
