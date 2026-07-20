# ASMPlus

A library for working with [ObjectWeb ASM](https://asm.ow2.io/).

## Features

- Utils for Jar, Classes, Hierarchy, Methods, Instructions and more
- Code generation
- Remapping
- PatternScanner
- Hierarchy analysis
- Initialization order analysis
- Control Flow analysis
- Jar loading

## Installation

1. Clone this repository.
2. Publish it to your local Maven repository:
    ```bash
    ./gradlew publishToMavenLocal
    ```
3. In the target project, add ```mavenLocal()``` to your repositories and depend on ASMPlus:
    ```
    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        implementation("me.exeos:asmplus:1.0.0")
    }
    ```

## License

This project is [LICENSED](./LICENSE) under the **GPL-3.0**.