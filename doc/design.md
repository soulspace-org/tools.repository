Rationale
=========

Build the basis for tools working with dependencies, e.g. license checkers.

Abstract the repositories of various language ecosystems.

Support at least
 * java/maven
 * python/pip
 * js/npm
 * */git

Don't rely on the implementation of existing build tools.
Implement the primitives of repositories in Clojure.
Use existing Clojure libraries where feasable.

Out of scope:
 * Build tool
   * tools.repository can be used as a building block in build tools
     but it does not aim to provide functionality apart from repository
     abstraction and dependency resolution.


Design Considerations
=====================


Repository abstraction
----------------------

Q: What are the primitive operations for the abstractions?


Q: Does a canonical format for artifact coordinates exist for all supported repositories?

Package URL (PURL) is a proposed format supporting many different repositories.


Q: What is the difference between an artifact and a dependency?


Artifact/Dependency addressing
------------------------------


Versions
--------

Q: How should versions be handled?

Q: Versioning strategies?
Semantic versioning
Build numbers
Commit SHA

Q: Ordering of versions?
Partial/total 


Transitive dependencies
-----------------------

Q: Search strategy? Collect by depth first search or breadth first search?


Q: Intermediate data structure in search?

Dependency tree, including conflicting versions of a dependency.
Dependency tree is useful information in itself and can be rendered textually or as diagram to show the structure including conflicts.

Q: Final dependency data structure?

List of all dependencies of the project with conflicts resolved.


Q: How to collect transitive dependencies with excludes?

Set of excludes on the stack (will be unrolled returning from a level)

