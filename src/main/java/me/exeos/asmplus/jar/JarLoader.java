package me.exeos.asmplus.jar;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarLoader {

    public static JarArchive load(File input, File dependencyPath) throws IOException {
        Map<String, ClassNode> classes = new HashMap<>();
        Map<String, ClassNode> dependencies = new HashMap<>();
        Map<String, byte[]> resources = new HashMap<>();

        Manifest manifest = null;

        try (JarFile jarFile = new JarFile(input)) {
            try {
                manifest = jarFile.getManifest();
            } catch (Exception e) {
                System.out.println("Error reading manifest, skipping. Error: " + e.getMessage());
            }

            loadFiles(classes, resources, jarFile);
        }

        if (dependencyPath != null && dependencyPath.exists() && dependencyPath.isDirectory()) {
            for (File f : Objects.requireNonNull(dependencyPath.listFiles())) {
                try (JarFile jarFile = new JarFile(f)) {
                    loadFiles(dependencies, new HashMap<>(), jarFile);
                }
            }
        }

        return new JarArchive(classes, dependencies, resources, manifest);
    }

    public static void loadFiles(Map<String, ClassNode> classes, Map<String, byte[]> resources, JarFile jarFile) throws IOException {
        Enumeration<? extends JarEntry> entries = jarFile.entries();
        JarEntry entry;

        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (!entry.isDirectory()) {
                InputStream stream = jarFile.getInputStream(entry);
                byte[] entryBytes = stream.readAllBytes();

                if (isClass(entryBytes) && entry.getName().endsWith(".class")) {
                    ClassReader classReader = new ClassReader(entryBytes);
                    ClassNode classNode = new ClassNode();

                    classReader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                    classes.put(classNode.name, classNode);
                } else {
                    resources.put(entry.getName(), entryBytes);
                }
            }
        }
    }

    public static void export(JarArchive jar, OutputStream output, boolean computeFrames) throws IOException {
        JarOutputStream jarOut = new JarOutputStream(output);
        for (Map.Entry<String, ClassNode> entry : jar.getClasses().entrySet()) {
            int flags = ClassWriter.COMPUTE_MAXS;
            if (computeFrames) {
//                flags = flags | ClassWriter.COMPUTE_FRAMES;
            }

            ClassWriter classWriter = new ClassWriter(flags);
            entry.getValue().accept(classWriter);

            writeJarEntry(jarOut, entry.getKey() + ".class", classWriter.toByteArray());
        }

        for (Map.Entry<String, byte[]> e : jar.getResources().entrySet()) {
            writeJarEntry(jarOut, e.getKey(), e.getValue());
        }

        jarOut.finish();
    }

    private static void writeJarEntry(JarOutputStream outputStream, String name, byte[] bytes) throws IOException {
        JarEntry entry = new JarEntry(new String(name.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        entry.setSize(bytes.length);

        outputStream.putNextEntry(entry);
        outputStream.write(bytes);
        outputStream.closeEntry();
    }

    private static boolean isClass(byte[] file) {
        if (file.length < 4)
            return false;
        return new BigInteger(1, new byte[]{file[0], file[1], file[2], file[3]}).intValue() == -889275714;
    }
}
