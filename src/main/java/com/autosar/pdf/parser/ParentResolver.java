package com.autosar.pdf.parser;

import com.autosar.pdf.domain.AutosarClass;
import com.autosar.pdf.domain.AutosarPackage;
import com.autosar.pdf.domain.AutosarType;

import java.util.*;

/**
 * Resolves parent-child relationships between AUTOSAR classes.
 *
 * This class runs after all parsing is complete and:
 * 1. Builds a global map of all classes
 * 2. Tracks children by their base class names
 *
 * Note: Since AutosarClass uses immutable records, we track relationships
 * separately in the childrenMap. The subclass list on parent classes is
 * not directly modified.
 *
 * Cross-package references are allowed - class names are globally scoped.
 */
public class ParentResolver {
    private static final int INITIAL_CAPACITY = 1024;

    private final Map<String, AutosarClass> classMap = new HashMap<>(INITIAL_CAPACITY);
    private final Map<String, List<AutosarClass>> childrenMap = new HashMap<>(INITIAL_CAPACITY);

    /**
     * Resolves parent references for all classes in all packages.
     *
     * @param packages List of packages to process
     */
    public void resolveParents(List<AutosarPackage> packages) {
        // Build global class map and collect children
        collectClasses(packages);
    }

    /**
     * Collects all classes from all packages into global maps.
     *
     * @param packages List of packages to process
     */
    private void collectClasses(List<AutosarPackage> packages) {
        classMap.clear();
        childrenMap.clear();

        for (AutosarPackage pkg : packages) {
            for (AutosarType type : pkg.types().values()) {
                if (type instanceof AutosarClass cls) {
                    classMap.put(cls.name(), cls);

                    // Track children by their base class names
                    for (String baseName : cls.bases()) {
                        childrenMap.computeIfAbsent(baseName, k -> new ArrayList<>()).add(cls);
                    }
                }
            }
        }
    }

    /**
     * Gets all classes with a given parent name.
     *
     * @param parentName Name of the parent class
     * @return List of classes that have this parent (may be empty)
     */
    public List<AutosarClass> getChildren(String parentName) {
        return childrenMap.getOrDefault(parentName, Collections.emptyList());
    }

    /**
     * Checks if a class exists in the global class map.
     *
     * @param className Name of the class to check
     * @return true if class exists
     */
    public boolean hasClass(String className) {
        return classMap.containsKey(className);
    }

    /**
     * Gets a class by name from the global class map.
     *
     * @param className Name of the class to get
     * @return Optional containing the class, or empty if not found
     */
    public Optional<AutosarClass> getClass(String className) {
        return Optional.ofNullable(classMap.get(className));
    }
}