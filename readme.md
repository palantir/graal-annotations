Graal Annotations and Processors
================================
Basic annotations and associated processors to make working with GraalVM easier and more
ergonomic.

`@GraalReflectable`
-------------------
To make use of reflection with AOT compilation, GraalVM requires explicit reflection
invocations or may use a [configuration file][1] to explicitly document the classes,
constructors, methods and fields that may be used in reflection invocations.

Labeling any class, constructor, method or field with `@GraalReflectable` will generate
a file in SubstrateVM's configuration format suitable for passing to native-image
with the `-H:ReflectionConfigurationResources` or `-H:ReflectionConfigurationFiles`
arguments.

When applied to a class, `@GraalReflectable` supports the following options:
* allPublicConstructors: default `true`
* allPublicFields: default `true`
* allPublicMethods: default `true`
* allDeclaredConstructors: default `false`
* allDeclaredFields: default `false`
* allDeclaredMethods: default `false`

When applied to a field, `@GraalReflectable` supports the following options:
* allowWrite: default `false`

To make use of the annotation, one should configure the corresponding annotation
processor provided by `com.palantir.graal.annotations:graal-annotation-processors`
bundle.

The configuration file will be emitted to `META-INF/graal/reflection-config.json`.

[1]:https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md

Contributions
-------------
Contributions are welcome. For larger feature requests or contributions, we prefer discussing the proposed change on
a GitHub issue prior to a PR.

License
-------
This library is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).
