# Architectury Loom based template for 1.8.9 kotlin forge mods

**For other templates, do check out the [other branches of this repository](https://github.com/romangraef/Forge1.8.9Template/branches/all)**

To get started, clone this repository and replace all references to `examplemod` or `com.example` with your own names.

If you don't want mixins (which allow for modifying vanilla code), then you can remove the references to mixins from
the `build.gradle.kts`, delete the `mixins.examplemod.json` and the `com.example.mixin` package.

Please note that Mixins should be kept in their own package, and should exclusively be written in Java, since older versions
of Mixin don't play well with Kotlin.

This project uses [DevAuth](https://github.com/DJtheRedstoner/DevAuth) per default, so you can log in using your real
minecraft account. If you don't need that, you can remove it from the buildscript.

To run the mod you will need two JDKs, one Java 17 jdk and one Java 1.8 jdk. You can download those
from [here](https://adoptium.net/temurin/releases) (or use your own downloads).

When you import your project into IntelliJ, you need to set the gradle jvm to the Java 17 JDK in the gradle tab, and the
Project SDK to the Java 1.8 JDK. Then click on the sync button in IntelliJ, and it should create a run task
called `Minecraft Client`. If it doesn't then try relaunching your IntelliJ.
**Warning for Mac users**: You might have to remove the `-XStartOnFirstThread` vm argument from your run configuration.
In the future, that should be handled by the plugin, but for now you'll probably have to do that manually. 

To export your project, run the `gradle build` task, and give other people the
file `build/libs/<modid>-<version>.jar`. Ignore the jars in the `build/badjars` folder. Those are intermediary jars that
are used by the build system but *do not work* in a normal forge installation.

This template is licensed under the Unlicense (license copy present in this repository), or alternatively under [Creative Commons 1.0 Universal (CC0 1.0)](https://creativecommons.org/publicdomain/zero/1.0/), and all contributions and PR to this template are expected to follow this. This means your mod, based on this template can be licensed whatever way you want, and does not need to reference back to this template in any way.
