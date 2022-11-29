(ns fr.jeremyschoffen.ssg.build
  (:require
    [clojure.tools.build.api :as tb]
    [datascript.core :as d]
    [fr.jeremyschoffen.ssg.db :as db]))

(defmulti entity->build-commands* :type)


(defn entity->build-commands [spec]
  (entity->build-commands* spec))


(defmulti execute-build-command!* (fn [_ cmd] (:type cmd)))


(defn execute-build-command! [conn cmd]
  (execute-build-command!* conn cmd))


(defn generate-build-commands [specs]
  (sequence (map entity->build-commands) specs))


(defn execute-build-commands! [conn cmds]
  (doseq [cmd cmds]
    (execute-build-command! conn cmd)))


(defn build-all! [conn]
  (->> (d/db conn)
       db/get-all-productions
       generate-build-commands
       (execute-build-commands! conn)))


(defn command-spec [cmd]
  (-> cmd meta ::build-spec))


(defn make-cmd [type]
  (fn [spec & {:as params}]
    (-> params
      (assoc :type type)
      (with-meta {::build-spec spec}))))


(def
 ^{:arglists '([spec & {:keys [target-dir src-dirs include replace ignores non-replaced-exts]}])}
 copy-dir-cmd
 "
  Make a command that will copy the contents of the src-dirs to the target-dir, optionally do text replacement.
  Returns nil.

  Globs are wildcard patterns for specifying sets of files in a directory
  tree, as specified in the glob syntax of java.nio.file.FileSystem:
  https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)

  Parameters:
    :target-dir - required, dir to write files, will be created if it doesn't exist
    :src-dirs   - required, coll of dirs to copy from
    :include    - glob of files to include, default = \"**\"
    :ignores    - collection of ignore regex patterns (applied only to file names),
                  see clojure.tools.build.tasks.copy/default-ignores for defaults
    :replace    - map of source to replacement string in files
    :non-replaced-exts - coll of extensions to skip when replacing (still copied)
                  default = [\"jpg\" \"jpeg\" \"png\" \"gif\" \"bmp\"]"
 (make-cmd ::copy-dir-cmd))


(defmethod execute-build-command!* ::copy-dir-cmd
  [_ cmd]
  (tb/copy-dir cmd))


(def
  ^{:arglists '([{:keys [src target] :as params}])}
  copy-file-cmd
 "Make a command that will copy one file from source to target, creating target dirs if needed.
  Returns nil.

  Parameters:
    :src - required, source path
    :target - required, target path"
  (make-cmd ::copy-file-cmd))


(defmethod execute-build-command!* ::copy-file-cmd
  [_ cmd]
  (tb/copy-file cmd))


(def
  ^{:arglists '([{:keys [path content string opts] :as params}])}
  write-file-cmd
 "Writes a file at path, will create parent dirs if needed. Returns nil.
  File contents may be specified either with :content (for data, that
  will be pr-str'ed) or with :string for the string to write. If
  neither is specified, an empty file is created (like touch).

  Parameters:
    :path - required, file path
    :content - val to write, will pr-str
    :string - string to write
    :opts - coll of writer opts like :append and :encoding (per clojure.java.io)"
  (make-cmd ::write-file-cmd))


(defmethod execute-build-command!* ::write-file-cmd
  [_ cmd]
  (tb/write-file cmd))


(def
  ^{:arglists '([{:keys [tx-data]}])}
  transact-cmd
  "Make a command that transacts `tx-data` to the database when run."
  (make-cmd ::transact-cmd))


(defmethod execute-build-command!* ::transact-cmd
  [conn {:keys [tx-data]}]
  (d/transact! conn tx-data))


(defn composite-cmd
  "Make a command of commands that will execute `cmds` in order."
  [spec & cmds]
  (-> {:type ::composite-cmd
       :cmds (vec cmds)}
      (vary-meta assoc ::build-spec spec)))


(defmethod execute-build-command!* ::composite-cmd
  [conn {:keys [cmds]}]
  (execute-build-commands! conn cmds))


