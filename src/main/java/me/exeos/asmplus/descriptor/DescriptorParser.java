package me.exeos.asmplus.descriptor;

import me.exeos.asmplus.descriptor.descriptors.method.MethodDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DescriptorParser {

    private final static Set<Character> validDescMemberPrimitives = Set.of('B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z');

    public static MethodDescriptor parseMethodDesc(String descriptor) {
        int argsStartIndex = descriptor.indexOf('(') + 1;
        int argsEndIndex = descriptor.indexOf(')');

        List<DescriptorMember> args = parseMembers(descriptor, argsStartIndex, argsEndIndex);
        DescriptorMember returnType = parseMembers(descriptor, argsEndIndex + 1, descriptor.length()).getFirst();

        return new MethodDescriptor(args, returnType);
    }

    public static DescriptorMember parseFieldDesc(String descriptor) {
        return parseMember(descriptor);
    }

    public static DescriptorMember parseType(String type) {
        if (type.startsWith("[")) {
            return parseMember(type);
        }

        return parseMember('L' + type + ';');
    }

    public static DescriptorMember parseMember(String descriptor) {
        return parseMembers(descriptor).getFirst();
    }

    public static List<DescriptorMember> parseMembers(String container) {
        return parseMembers(container, 0, container.length());
    }

    public static List<DescriptorMember> parseMembers(String container, int startIndex, int endIndex) {
        List<DescriptorMember> members = new ArrayList<>();

        StringBuilder classNameBuilder = new StringBuilder();
        boolean buildingClassName = false;
        boolean buildingArray = false;
        int arrayDepth = 0;

        char[] containerChars = container.toCharArray();
        for (int i = startIndex; i < endIndex; i++) {
            char c = containerChars[i];

            switch (c) {
                case 'L' -> {
                    if (buildingClassName) {
                        classNameBuilder.append(c);
                    } else {
                        buildingClassName = true;
                    }
                }
                case ';' -> {
                    members.add(new DescriptorMember(classNameBuilder.toString(), false, buildingArray, arrayDepth));

                    buildingClassName = false;
                    classNameBuilder = new StringBuilder();

                    buildingArray = false;
                    arrayDepth = 0;
                }
                case '[' -> {
                    buildingArray = true;
                    arrayDepth++;
                }
                case 'V' -> {
                    if (buildingClassName) {
                        classNameBuilder.append(c);
                    } else {
                        members.add(new DescriptorMember(String.valueOf(c), true, false, 0));
                    }
                }
                default -> {
                    if (buildingClassName) {
                        classNameBuilder.append(c);
                    } else if (validDescMemberPrimitives.contains(c)) {
                        members.add(new DescriptorMember(String.valueOf(c), true, buildingArray, arrayDepth));

                        buildingArray = false;
                        arrayDepth = 0;
                    } else {
                        throw new IllegalArgumentException("Invalid string provided. Char: " + c + " at index: " + i + " is not within class definition or a valid primitive");
                    }
                }
            }
        }

        return members;
    }
}
