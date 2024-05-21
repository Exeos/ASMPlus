package me.exeos.asmplus;

import me.exeos.asmplus.utils.StreamUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarLoader {

    public final HashMap<String, ClassNode> classes = new HashMap<>();
    public final HashMap<String, byte[]> resources = new HashMap<>();

    protected FileTime creationTime;
    protected FileTime lastModifiedTime;

    /**
     * Load all classes and files of jar to HashMaps in form of:
     * <ClassName, ClassNode> for classes &
     * <FileName, FileBytes> for files.
     *
     * @param inputPath Path to ajar file
     * @throws IOException
     */

    public void load(String inputPath) throws IOException {
        JarFile jarIn = new JarFile(inputPath);
        Enumeration<? extends JarEntry> entries = jarIn.entries();
        JarEntry entry;

        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (!entry.isDirectory()) {
                InputStream stream = jarIn.getInputStream(entry);
                byte[] entryBytes = StreamUtils.readAllBytes(stream);

                if (isClass(entryBytes) && entry.getName().endsWith(".class")) {
                    ClassReader classReader = new ClassReader(entryBytes);
                    ClassNode classNode = new ClassNode();

                    classReader.accept(classNode, ClassReader.SKIP_FRAMES + ClassReader.SKIP_DEBUG);
                    classes.put(classNode.name, classNode);
                } else {
                    resources.put(entry.getName(), entryBytes);
                }
            }
        }

        BasicFileAttributes attributes = Files.getFileAttributeView(Paths.get(inputPath), BasicFileAttributeView.class).readAttributes();
        creationTime = attributes.creationTime();
        lastModifiedTime = attributes.lastModifiedTime();
    }

    /**
     * Export mapped jar back to a jar file
     *
     * @param inputPath Desired path for jar output
     * @throws IOException
     */

    public void export(String inputPath) throws Exception {
        if (!new File(new File(inputPath).getParent()).mkdirs()) {
            System.out.println("Failed to mkdirs");
        }
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(inputPath));
        for (Map.Entry<String, ClassNode> entry : classes.entrySet()) {
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            entry.getValue().accept(classWriter);

            writeJarEntry(jarOut, entry.getKey() + ".class", classWriter.toByteArray());
        }

        for (Map.Entry<String, byte[]> e : resources.entrySet()) {
            writeJarEntry(jarOut, e.getKey(), e.getValue());
        }

        jarOut.finish();

        BasicFileAttributeView attributeView = Files.getFileAttributeView(Paths.get(inputPath), BasicFileAttributeView.class);
        attributeView.setTimes(lastModifiedTime, null, creationTime);
    }

    protected void writeJarEntry(JarOutputStream outputStream, String name, byte[] bytes) throws IOException {
        JarEntry entry = new JarEntry(new String(name.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        if (creationTime != null)
            entry.setCreationTime(creationTime);
        if (lastModifiedTime != null)
            entry.setLastModifiedTime(lastModifiedTime);

        entry.setSize(bytes.length);

        outputStream.putNextEntry(entry);
        outputStream.write(bytes);
        outputStream.closeEntry();
    }

    private boolean isClass(byte[] file) {
        if (file.length < 4)
            return false;
        return new BigInteger(1, new byte[] { file[0], file[1], file[2], file[3] }).intValue() == -889275714;
    }
}