package me.exeos.asmplus.utils;

import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.tree.ClassNode;

import java.util.Optional;

public class JarUtil {

    public static Optional<ClassNode> getMainClass(JarArchive jar) {
        if (jar.getManifest() != null) {
            String mainClassName = jar.getManifest().getMainAttributes().getValue("Main-Class");
            if (mainClassName != null) {
                return jar.getClassNode(mainClassName.replace(".", "/"), false);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getMainMethodFromManifest(JarArchive jar) {
        if (jar.getManifest() != null) {
            String mainClassName = jar.getManifest().getMainAttributes().getValue("Main-Class");
            if (mainClassName != null) {
                return Optional.of(mainClassName.replace(".", "/") + "main" + "([Ljava/lang/String;)V");
            }
        }
        return Optional.empty();
    }
}
