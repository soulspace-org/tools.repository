(ns org.soulspace.tools.maven-xml-parser)


;;;
;;; maven pom parsing
;;;
(defn get-content
  "Returns the content, when tag of the entry maches the given tag."
  [tag entry]
  (when (= (:tag entry) tag)
    (:content entry)))

(defn parse-kv
  "Parse the tag and the content of an entry into a key value tuple ."
  [{k :tag v :content}]
  (println "parse-kv:" k)
  [k v])

(defn parse-property
  "Parse a property."
  [{tag :tag content :content}]
;  (println "parse-property:" tag)
  [tag (first content)])

(defn parse-parent
  "Parses the parent entry."
  [{tag :tag content :content}]
;  (println "parse-parent:" tag)
  (case tag
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]
    :version [:version (first content)]
    :relativePath [:relative-path (first content)]))

(defn parse-exclusion
  "Parses exclusion entry."
  [{tag :tag content :content}]
  (case tag
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]))

(defn parse-exclusions
  "Parses exclusions entry."
  [{tag :tag content :content}]
  (into {} (map parse-exclusion) content))

(defn parse-dependency
  "Parses dependency entry."
  [{tag :tag content :content}]
;  (println "parse-dependency:" tag)
  (case tag
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]
    :version [:version (first content)]
    :classifier [:classifier (first content)]
    :scope [:scope (first content)]
    :type [:type (first content)]
    :optional [:optional (first content)]
    :exclusions [:exclusions (into [] (map parse-exclusions) content)]))

(defn parse-dependencies
  "Parses dependencies entry."
  [{tag :tag content :content}]
;  (println "parse-dependencies:" tag)
  (into {} (map parse-dependency) content))

(defn parse-dependency-management
  "Parses dependency management entry."
  [{tag :tag content :content}]
;  (println "parse-dependency-management:" tag)
  {:dependencies (into [] (map parse-dependencies) content)})

(defn parse-license
  "Parses a license entry."
  [{tag :tag content :content}]
;  (println "parse-license:" tag)
  (case tag
    :name [:name (first content)]
    :url [:url (first content)]
    :distribution [:distribution (first content)]
    :comments [:comments (first content)]))

(defn parse-licenses
  "Parses a licenses entry."
  [{tag :tag content :content}]
;  (println "parse-licenses:" tag)
  (into {} (map parse-license) content))

(defn parse-developer
  "Parses a developer entry."
  [{tag :tag content :content}]
;  (println "parse-developer:" tag)
  (case tag
    :id [:id (first content)]
    :name [:name (first content)]
    :email [:email (first content)]
    :url [:url (first content)]
    :organization [:organization (first content)]
    :organizationUrl [:organization-url (first content)]
    :timezone [:timezone (first content)]
    :roles [:roles (into [] (map (partial get-content :role)) content)]
    :properties [:properties nil])) ; TODO

(defn parse-developers
  "Parses a developers entry."
  [{tag :tag content :content}]
;  (println "parse-developers:" tag)
  (into {} (map parse-developer) content))

(defn parse-contributor
  "Parses a contributor entry."
  [{tag :tag content :content}]
;  (println "parse-contributor:" tag)
  (case tag
    :id [:id (first content)]
    :name [:name (first content)]
    :email [:email (first content)]
    :url [:url (first content)]
    :organization [:organization (first content)]
    :organizationUrl [:organization-url (first content)]
    :timezone [:timezone (first content)]
    :roles [:roles (into [] (map (partial get-content :role)) content)]
    :properties [:properties nil])) ; TODO

(defn parse-contributors
  "Parses a contributors entry."
  [{tag :tag content :content}]
;  (println "parse-contributors:" tag)
  (into {} (map parse-contributor) content))

(defn parse-organization
  "Parses a organization entry."
  [{tag :tag content :content}]
;  (println "parse-organization:" tag)
  (case tag
    :name [:name (first content)]
    :url [:url (first content)]))

(defn parse-issue-management
  "Parses a issue management entry."
  [{tag :tag content :content}]
;  (println "parse-issue-management:" tag)
  (case tag
    :system [:system (first content)]
    :url [:url (first content)]))

(defn parse-notifier
  "Parses a notifier entry."
  [{tag :tag content :content}]
;  (println "parse-notifier:" tag)
  (case tag
    :type [:type (first content)]
    :sendOnError [:sendOnError (first content)]
    :sendOnFailure [:sendOnFailure (first content)]
    :sendOnSuccess [:sendOnSuccess (first content)]
    :sendOnWarning [:sendOnWarning (first content)]
    :configuration [:configuration nil])) ; TODO

(defn parse-ci-management
  "Parses a ci management entry."
  [{tag :tag content :content}]
;  (println "parse-ci-management:" tag)
  (case tag
    :system [:system (first content)]
    :url [:url (first content)]
    :notifiers [:notifiers (into {} (map parse-notifier) (get-content :notifiers content))]))

(defn parse-scm
  "Parses a scm entry."
  [{tag :tag content :content}]
;  (println "parse-scm:" tag)
  (case tag
    :connection [:connection (first content)]
    :developerConnection [:developer-connection (first content)]
    :tag [:tag (first content)]
    :url [:url (first content)]))

(defn parse-resource
  "Parses a resource entry."
  [{tag :tag content :content}]
;  (println "parse-resource:" tag)
  (case tag
    :targetPath [:target-path (first content)]
    :filtering [:filtering (first content)]
    :directory [:directory (first content)]
    :includes [:includes (into [] (map (partial get-content :include)) content)]
    :excludes [:excludes (into [] (map (partial get-content :exclude)) content)]))

(defn parse-test-resource
  "Parses a test resource entry."
  [{tag :tag content :content}]
;  (println "parse-test-resource:" tag)
  (case tag
    :targetPath [:target-path (first content)]
    :filtering [:filtering (first content)]
    :directory [:directory (first content)]
    :includes [:includes (into [] (map (partial get-content :include)) content)]
    :excludes [:excludes (into [] (map (partial get-content :exclude)) content)]))

(defn parse-resources
  "Parse a resource entry."
  [{tag :tag content :content}]
;  (println "parse-resources:" tag)
  (into {} (map parse-resource) content))

(defn parse-test-resources
  "Parse a test resource entry."
  [{tag :tag content :content}]
;  (println "parse-test-resources:" tag)
  (into {} (map parse-test-resource) content))

(defn parse-configuration
  "Parses a configuration entry."
  [{tag :tag content :content}]
;  (println "parse-configuration:" tag)
;  (case tag ; TODO cond with default parse-kv?
;    :items [:items (into [] (map (partial get-content :item)) content)]
;    :tasks [:tasks ""] ; TODO add tasks
;    :configLocation [:config-location (first content)]
;    :enableRulesSummary [:enable-rules-summary (first content)])
  )

(defn parse-execution
  "Parses a plugin execution entry."
  [{tag :tag content :content}]
;  (println "parse-execution:" tag)
  (case tag
    :id [:id (first content)]
    :goals [:goals (into [] (map (partial get-content :goal)) content)]
    :phase [:phase (first content)]
    :inherited [:inherited (first content)]
    :configuration [:configuration (into {} (map parse-configuration) content)]))

(defn parse-executions
  [{tag :tag content :content}]
;  (println "parse-executions:" tag)
  (into {} (map parse-execution) content))

(defn parse-plugin
  "Parses a plugin entry."
  [{tag :tag content :content}]
;  (println "parse-plugin:" tag)
  (case tag
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]
    :version [:version (first content)]
    :extensions [:extensions (first content)]
    :inherited [:inherited (first content)]
    :configuration [:configuration (into {} (map parse-configuration) content)]
    :dependencies [:dependencies (into [] (map parse-dependency) content)]
    :executions [:executions (into [] (map parse-executions) content)])) ; TODO

(defn parse-plugin-management
  "Parses a plugin management entry."
  [{tag :tag content :content}]
;  (println "parse-plugin-management:" tag)
  {:plugins (into [] (map parse-plugin) (get-content :plugins content))})

(defn parse-extension
  "Parses an extension entry."
  [{tag :tag content :content}]
;  (println "parse-extension:" tag)
  (case tag
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]
    :version [:version (first content)]))

(defn parse-releases
  "Parses an releases entry."
  [{tag :tag content :content}]
;  (println "parse-releases:" tag)
  (case tag
    :enabled [:enabled (first content)]
    :updatePolicy [:update-policy (first content)]
    :checksumPolicy [:checksum-policy (first content)]))

(defn parse-snapshots
  "Parses an snapshots entry."
  [{tag :tag content :content}]
;  (println "parse-snapshots:" tag)
  (case tag
    :enabled [:enabled (first content)]
    :updatePolicy [:update-policy (first content)]
    :checksumPolicy [:checksum-policy (first content)]))

(defn parse-repository
  "Parses a repository entry."
  [{tag :tag content :content}]
;  (println "parse-repository:" tag)
  (case tag
    :id [:id (first content)]
    :name [:name (first content)]
    :url [:url (first content)]
    :releases [:releases (into {} (map parse-releases) content)]
    :snapshots [:snapshots (into {} (map parse-snapshots) content)]
    :layout [:layout (first content)]))

(defn parse-plugin-repository
  "Parses a plugin-repository entry."
  [{tag :tag content :content}]
;  (println "parse-plugin-repository:" tag)
  (case tag
    :id [:id (first content)]
    :name [:name (first content)]
    :url [:url (first content)]
    :releases [:releases (into {} (map parse-releases) content)]
    :snapshots [:snapshots (into {} (map parse-snapshots) content)]
    :layout [:layout (first content)]))

(defn parse-repositories
  "Parses a repositories entry."
  [{tag :tag content :content}]
;  (println "parse-repositories:" tag)
  (into {} (map parse-repository) content))

(defn parse-plugin-repositories
  "Parses a repositories entry."
  [{tag :tag content :content}]
;  (println "parse-repositories:" tag)
  (into {} (map parse-plugin-repository) content))

(defn parse-build
  "Parses a build."
  [{tag :tag content :content}]
;  (println "parse-build:" tag)
  (case tag
    :defaultGoal [:default-goal (first content)]
    :directory [:directory (first content)]
    :sourceDirectory [:source-directory (first content)]
    :scriptSourceDirectory [:script-source-directory (first content)]
    :testSourceDirectory [:test-source-directory (first content)]
    :outputDirectory [:output-directory (first content)]
    :testOutputDirectory [:test-output-directory (first content)]
    :extensions [:extensions
                 (into []
                       (map parse-extension)
                       (get-content :extensions content))]
    :finalName [:final-name (first content)]
    :filters [:filters (into [] (map (partial get-content :filter)) content)]
    :resources [:resources
                (into []
                      (map parse-resource)
                      (get-content :resources content))] ; TODO check
    :testResources [:test-resources
                    (parse-test-resources (first content))] ; TODO check
    :plugins [:plugins
              (into [] (map parse-plugin) (get-content :plugins content))]
    :pluginManagement [:plugin-management
                       (parse-plugin-management (first content))])) ; TODO

(defn parse-mailing-list
  "Parses a mailing list entry."
  [{tag :tag content :content}]
;  (println "parse-mailing-list:" tag)
  (case tag
    :name [:name (first content)]
    :subscribe [:subscribe (first content)]
    :unsubscribe [:unsubscribe (first content)]
    :post [:post (first content)]
    :archive [:archive (first content)]
    :otherArchives [:other-archives
                    (into [] (map (partial get-content :otherArchive)) content)]))

(defn parse-mailing-lists
  "Parses a mailing lists entry."
  [{tag :tag content :content}]
;  (println "parse-mailing-lists:" tag)
  (into {} (map parse-mailing-list) content))

(defn parse-report-set
  "Parses a report set entry."
  [{tag :tag content :content}]
;  (println "parse-report-set:" tag)
  (case tag
    :id [:id (first content)]
    :reports [:reports (into [] (map (partial get-content :report)) content)]
    :inherited [:inherited (first content)]
    :configuration [:configuration (into {} (map parse-configuration) content)]))

(defn parse-report-sets
  "Parses a report sets entry."
  [{tag :tag content :content}]
;  (println "parse-report-sets:" tag)
  (into {} (map parse-report-set) content))

(defn parse-report-plugin
  "Parses a report plugin entry."
  [{tag :tag content :content}]
;  (println "parse-report-plugin:" tag)
  (case tag
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]
    :version [:version (first content)]
    :inherited [:inherited (first content)]
    :reportSets [:report-sets (into [] (map parse-report-sets) content)]
    :configuration [:configuration ; (into {} (map parse-configuration) content)
                    []] ; TODO
    ))

(defn parse-report-plugins
  "Parses a report plugins entry."
  [{tag :tag content :content}]
;  (println "parse-report-plugins:" tag)
  (into {} (map parse-report-plugin content)))

(defn parse-reporting
  "Parses a reporting entry."
  [{tag :tag content :content}]
;  (println "parse-reporting:" tag)
  (case tag
    :excludeDefaults [:exclude-defaults (first content)]
    :outputDirectory [:output-directory (first content)]
    :plugins [:plugins (into [] (map parse-report-plugins content))]
    :inherited [:inherited (first content)]
;   :configuration [:configuration ; (into {} (map parse-configuration) content)
;                   []] ; TODO
    ))

(defn parse-dm-repository
  "Parses a dm repository entry."
  [{tag :tag content :content}])
;  (println "parse-dm-repository:" tag)

(defn parse-site
  "Parses a site entry."
  [{tag :tag content :content}]
;  (println "parse-site:" tag)
  (case tag
    :id [:id (first content)]
    :name [:name (first content)]
    :url [:url (first content)]))

(defn parse-relocation
  "Parses a relocation entry."
  [{tag :tag content :content}]
;  (println "parse-relocation:" tag)
  (case tag
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]
    :version [:version (first content)]
    :message [:message (first content)]))

(defn parse-distribution-management
  "Parses a distribution management entry."
  [{tag :tag content :content}]
;  (println "parse-distribution-management:" tag)
  (case tag
    :downloadUrl [:download-url (first content)]
    :status [:status (first content)]
    :repository [:repository ()] ; TODO
    :snapshotRepository [:snapshot-repository ()] ; TODO
    :site [:site (into {} (map parse-site) content)]
    :relocation [:relocation (into {} (map parse-relocation) content)]))

(defn parse-prerequisites
  "Parses a prerequisites entry."
  [{tag :tag content :content}]
;  (println "parse-prerequisitese:" tag)
  {:maven (get-content :maven (first content))})

(defn parse-os
  "Parses an os entry."
  [{tag :tag content :content}]
;  (println "parse-os:" tag)
  (case tag
    :name [:name (first content)]
    :family [:family (first content)]
    :arch [:arch (first content)]
    :version [:version (first content)]))

(defn parse-activation-property
  "Parses an activation-property entry."
  [{tag :tag content :content}]
;  (println "parse-activation-property:" tag)
  (case tag
    :name [name (first content)]
    :value [name (first content)]))

(defn parse-file
  "Parses an file entry."
  [{tag :tag content :content}]
;  (println "parse-file:" tag)
  (case tag
    :exists [:exists (first content)]
    :missing [:missing (first content)]))

(defn parse-activation
  "Parses an activation entry."
  [{tag :tag content :content}]
;  (println "parse-activation:" tag)
  (case tag
    :activeByDefault [:active-by-default (first content)]
    :jdk [:jdk (first content)]
    :os [:os (into {} (map parse-os) content)]
    :property [:property (into {} (map parse-activation-property) content)]
    :file [:file (into {} (map parse-file) content)]))

(defn parse-profile
  "Parses a profile entry."
  [{tag :tag content :content}]
;  (println "parse-profile:" tag)
  (case tag
    :id [:id (first content)]
    :activation (into {} (map parse-activation) content)
    :build [:build (into {} (map parse-build) content)]
    :modules [:modules (into [] (map (partial get-content :module) content))]
    :repositories [:repositories (into [] (map parse-repositories) content)]
    :pluginRepositories [:plugin-repositories
                         (into [] (map parse-plugin-repositories) content)]
    :dependencies [:dependencies (into [] (map parse-dependencies) content)]
    :reporting [:reporting (into {} (map parse-reporting) content)]
    :dependencyManagement [:dependency-management
                           (parse-dependency-management (first content))]
    :distributionManagement [:distribution-management
                             (into {} (map parse-distribution-management) content)]
    :properties [:properties (into {} (map parse-property) content)]))

(defn parse-profiles
  "Parses a profiles entry."
  [{tag :tag content :content}]
;  (println "parse-profiles:" tag)
  (into {} (map parse-profile) content))

(defn parse-pom
  "Parses a POM entry."
  [{tag :tag content :content}]
;  (println "parse-pom:" tag)
  (case tag
    :modelVersion [:model-version (first content)]
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]
    :version [:version (first content)]
    :packaging [:packaging (first content)]
    :dependencies [:dependencies (into [] (map parse-dependencies) content)]
    :parent [:parent (into {} (map parse-parent) content)]
    :dependencyManagement [:dependency-management
                           (parse-dependency-management (first content))]
    :modules [:modules (into [] (map (partial get-content :module) content))]
    :properties [:properties (into {} (map parse-property) content)]
    :build [:build (into {} (map parse-build) content)]
    :reporting [:reporting (into {} (map parse-reporting) content)]
    :name [:name (first content)]
    :description [:description (first content)]
    :url [:url (first content)]
    :inceptionYear [:inception-ear (first content)]
    :licenses [:licenses (into [] (map parse-licenses) content)]
    :organization [:organization (into {} (map parse-organization content))]
    :developers [:developers (into [] (map parse-developers) content)]
    :contributors [:contributors (into [] (map parse-contributors) content)]
    :issueManagement [:issue-management (into {} (map parse-issue-management) content)]
    :ciManagement [:ci-management (into {} (map parse-ci-management) content)]
    :mailingLists [:mailing-lists (into [] (map parse-mailing-lists) content)]
    :scm [:scm (into {} (map parse-scm) content)]
    :prerequisites [:prerequisites (parse-prerequisites (first content))]
    :repositories [:repositories (into [] (map parse-repositories) content)]
    :pluginRepositories [:plugin-repositories
                         (into [] (map parse-plugin-repositories) content)]
    :distributionManagement [:distribution-management
                             (into {} (map parse-distribution-management) content)]
    :profiles [:profiles (into [] (map parse-profiles) content)]))

(defn parse-pom-xml
  "Parses a maven POM xml."
  [entry]
  (into {} (map parse-pom) (get-content :project entry)))


;;;
;;; maven settings parsing
;;;

(defn parse-server
  "Parses a server entry."
  [{tag :tag content :content}]
  (println "parse-server:" tag)
  (case tag
    :id [:id (first content)]
    :username [:username (first content)]
    :password [:password (first content)]
    :privateKey [:private-key (first content)]
    :passphrase [:passphrase (first content)]
    :filePermissions [:file--permissions (first content)]
    :directoryPermissions [:directory-permissions (first content)]
    :configuration [:configuration nil])) ; TODO

(defn parse-servers
  "Parses a servers entry."
  [{tag :tag content :content}]
  (println "parse-servers:" tag))

(defn parse-mirror
  "Parses a mirror entry."
  [{tag :tag content :content}]
  (println "parse-mirror:" tag)
  (case tag
    :id [:id (first content)]
    :name [:name (first content)]
    :url [:url (first content)]
    :mirrorOf [:mirror-of (first content)]))

(defn parse-mirrors
  "Parses a mirrors entry."
  [{tag :tag content :content}]
  (println "parse-mirrors:" tag)
  (into {} (map parse-mirror) content))

(defn parse-proxy
  "Parses a proxy entry."
  [{tag :tag content :content}]
  (println "parse-proxy:" tag)
  (case tag
    :id [:id (first content)]
    :active [:active (first content)]
    :protocol [:protocol (first content)]
    :host [:host (first content)]
    :port [:port (first content)]
    :username [:username (first content)]
    :password [:password (first content)]
    :nonProxyHost [:non-proxy-host (first content)]))

(defn parse-proxies
  "Parses a proxies entry."
  [{tag :tag content :content}]
  (println "parse-proxies:" tag)
  (into {} (map parse-proxy) content))

(defn parse-plugin-groups
  "Parses a pluginGroup entry."
  [{tag :tag content :content}]
  (println "parse-plugin-groups:" tag)
  (into [] (map (partial get-content :pluginGroup) content)))

(defn parse-settings
  "Parses a settings entry."
  [{tag :tag content :content}]
  (println "parse-settings:" tag)
  (case tag
    :localRepository [:local-repository (first content)]
    :interactiveMode [:interactive-mode (first content)]
    :offline [:offline (first content)]
    :pluginGroups [:plugin-groups (into [] (map parse-plugin-groups) content)]
    :servers [:servers (into [] (map parse-servers) content)]
    :mirrors [:mirrors (into [] (map parse-mirrors) content)]
    :proxies [:proxies (into [] (map parse-proxies) content)]
    :profiles [:profiles nil] ; TODO
    :activeProfiles [:active-profiles nil])) ; TODO

(defn parse-settings-xml
  "Parses a maven settings xml."
  [entry]
  (into {} (map parse-settings) (get-content :settings entry)))


;;;
;;; maven metadata parsing
;;;

(defn parse-md-snapshot
  "Parses a metadata snapshot entry."
  [{tag :tag content :content}]
  (println "parse-md-snapshot:" tag)
  (case tag
    :timestamp [:timestamp (first content)]
    :buildNumber [:build-number (first content)]
    :localCopy [:local-copy (first content)]))

(defn parse-snapshot-version
  "Parses a metadata snapshot version entry."
  [{tag :tag content :content}]
  (println "parse-snapshot-version:" tag)
  (case tag
    :classifier [:classifier (first content)]
    :extension [:extension (first content)]
    :value [:value (first content)]
    :updated [:updated (first content)]))

(defn parse-snapshot-versions
  "Parses a metadata snapshot versions entry."
  [{tag :tag content :content}]
  (println "parse-snapshot-versions:" tag)
  (into {} (map parse-snapshot-version) content))

(defn parse-versioning
  "Parses a versioning entry."
  [{tag :tag content :content}]
  (println "parse-versioning:" tag)
  (case tag
    :latest [:latest (first content)]
    :release [:release (first content)]
    :snapshot [:snapshot (into {} (map parse-md-snapshot) content)]
    :versions [:versions (into [] (map (partial get-content :version)) content)]
    :lastUpdated [:last-updated  (first content)]
    :snapshotVersions [:snapshot-versions (into [] (map parse-snapshot-versions) content)]))

(defn parse-md-plugin
  "Parses a metadata plugins entry."
  [{tag :tag content :content}]
  (println "parse-md-plugin:" tag)
  (case tag
    :name [:name (first content)]
    :prefix [:prefix (first content)]
    :artifactId [:artifact-id (first content)]))

(defn parse-md-plugins
  "Parses a metadata plugins entry."
  [{tag :tag content :content}]
  (println "parse-md-plugins:" tag)
  (into {} (map parse-md-plugin) content))

(defn parse-metadata
  "Parses a metadata entry."
  [{tag :tag content :content}]
  (println "parse-metadata:" tag)
  (case tag
    :groupId [:group-id (first content)]
    :artifactId [:artifact-id (first content)]
    :version [:version (first content)]
    :versioning [:versioning (into {} (map parse-versioning) content)]
    :plugins [:plugins (into [] (map parse-md-plugins) content)]))

(defn parse-metadata-xml
  "Parses a maven POM xml."
  [entry]
  (into {} (map parse-metadata) (get-content :metadata entry)))

