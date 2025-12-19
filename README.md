# ASMPlus
Makes your life easier when working with [objectweb.asm](https://asm.ow2.io/)

## Project Setup

### Prerequisites

- java 21+
- git

### Cloning Project & Dependencies

Clone ASMPlus and the required dependencies:

```bash
git clone https://github.com/Exeos/ASMPlus.git
git clone https://github.com/Exeos/jlib.git
```

Resulting layout:

```
/
├── ASMPlus/
└── jlib/
```

Make a local gradle properties file from the example and update the dependency paths. From the `ASMPlus` directory run:

```bash
cp gradle.properties.example gradle.properties
```

Open `gradle.properties` and set the dependency paths (these are the defaults you can use if the repos are siblings as shown above):

```
dependencies.jlib.path=../jlib
```

## Building the Project

ASMPlus uses Gradle Composite Builds to include jlib. After cloning and configuring `gradle.properties`, build from the `ASMPlus` directory:

Unix / macOS:
```bash
./gradlew build
```

Windows:
```bash
gradlew.bat build
```