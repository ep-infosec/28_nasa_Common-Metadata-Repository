(ns cmr.ous.util.http.request
  (:require
   [cmr.http.kit.request :as request]
   [cmr.ous.components.config :as config]
   [cmr.ous.const :as const]
   [taoensso.timbre :as log])
  (:refer-clojure :exclude [get]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Header Support   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def version-format "application/vnd.%s%s+%s")

(defn add-user-agent
  ([]
   (add-user-agent {}))
  ([req]
   (request/add-header req "User-Agent" const/user-agent)))

(defn add-client-id
  ([]
   (add-client-id {}))
  ([req]
   (request/add-header req "Client-Id" const/client-id)))

(defn get-header
  [request header]
  (request/get-header request header))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Accept Header/Version Support   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn default-accept
  [system]
  (format version-format
          (config/vendor system)
          (config/api-version-dotted system)
          (config/default-content-type system)))

(defn parse-accept
  [system req]
  (->> (or (get-in req [:headers :accept])
           (get-in req [:headers "accept"])
           (get-in req [:headers "Accept"])
           (default-accept system))
       (re-find request/accept-pattern)
       (zipmap request/accept-pattern-keys)))

(defn accept-api-version
  [system req]
  (let [parsed (parse-accept system req)
        version (or (:version parsed) (config/api-version system))]
    version))

(defn accept-media-type
  [system req]
  (let [parsed (parse-accept system req)
        vendor (or (:vendor parsed) (config/vendor system))
        version (or (:.version parsed) (config/api-version-dotted system))]
    (str vendor version)))

(defn accept-format
  [system req]
  (let [parsed (parse-accept system req)]
    (or (:content-type parsed)
        (:no-vendor-content-type parsed)
        (config/default-content-type system))))

(defn accept-media-type-format
  [system req]
  (format "%s; format=%s"
          (accept-media-type system req)
          (accept-format system req)))
