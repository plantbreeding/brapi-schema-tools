package org.brapi.schematools.core.utils;

import lombok.AccessLevel;
import lombok.Getter;
import org.brapi.schematools.core.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Creates a cache of {@link BrAPIClass}es.
 * Takes a list of classes and caches those in the list of primary classes if they pass the provided cachePredicate.
 * Additional classes are added to the cache as dependent class as follows
 * For {@link BrAPIObjectType} utility checks the properties and
 * tries to cache any that are the return type of these properties {@link BrAPIClass}es.
 * If an {@link BrAPIArrayType} is encountered then the {@see BrAPIArrayType#getItems()} is
 * checked recursively to be included in the cache.
 * For {@link BrAPIOneOfType} it is added to the cache and any of {@see BrAPIOneOfType#getPossibleTypes()}
 * are checked recursively to be included in the cache.
 * {@link BrAPIAllOfType} are ignored.
 * Cached classes are available from {@link BrAPIClassCache#getBrAPIClasses()}
 * or {@link BrAPIClassCache#getBrAPIClassesAsSet()}
 * Additional dependent classes are available from {@link BrAPIClassCache#getAllDependencies()}
 * All classes the BrAPIClasses that were originally passed to the cache regardless of if they
 * passed the {@link BrAPIClassCache#cachePredicate}
 *
 */
public class BrAPIClassCacheBuilder {
    /**
     * Creates the cache
     *
     * @param brAPIClasses the list of possible classes to be cached.
     * @return the cache of classes
     */
    public static BrAPIClassCache createCache(List<BrAPIClass> brAPIClasses) {
        return new BrAPIClassCache(brAPIClass -> true, brAPIClasses);
    }

    /**
     * Creates the cache with a cache predicate that determines if a class is added to the cache.
     *
     * @param cachePredicate the cache predicate that determines if a class is added to the cache.
     * @param brAPIClasses   the list of possible classes to be cached.
     * @return the cache of classes
     */
    public static BrAPIClassCache createCache(Predicate<BrAPIClass> cachePredicate, List<BrAPIClass> brAPIClasses) {
        return new BrAPIClassCache(cachePredicate, brAPIClasses);
    }

    /**
     * Creates the cache of classes as a Map
     *
     * @param brAPIClasses the list of possible classes to be cached.
     * @return the cache of classes as a Map
     */
    public static Map<String, BrAPIClass> createMap(List<BrAPIClass> brAPIClasses) {
        return createCache(brAPIClasses).getBrAPIClassMap();
    }

    /**
     * Creates the cache of classes as a Map with a cache predicate that determines if a class is added to the cache.
     *
     * @param cachePredicate the cache predicate that determines if a class is added to the cache.
     * @param brAPIClasses   the list of possible classes to be cached.
     * @return the cache of classes as a Map
     */
    public static Map<String, BrAPIClass> createMap(Predicate<BrAPIClass> cachePredicate, List<BrAPIClass> brAPIClasses) {
        return createCache(cachePredicate, brAPIClasses).getBrAPIClassMap();
    }

    public static class BrAPIClassCache {
        private final Map<String, BrAPIClass> allBrAPIClasses;
        private final Predicate<BrAPIClass> cachePredicate;
        @Getter(AccessLevel.PRIVATE)
        private final Map<String, BrAPIClass> brAPIClassMap;
        // Contains as keys type names, and elements a list of classes that is used by that type
        private final Map<String, Set<String>> dependsOn;
        // Contains as keys type names, and elements a list of classes that depend on the type
        private final Map<String, Set<String>> usedBy;
        private final Map<String, Set<BrAPIClass>> exclusiveDependencies;
        private final Map<String, Set<BrAPIClass>> commonDependencies;
        @Getter
        private final List<BrAPIClass> primaryClasses;
        @Getter
        private final List<BrAPIClass> allDependencies;

        public BrAPIClassCache(Predicate<BrAPIClass> cachePredicate, List<BrAPIClass> brAPIClasses) {
            allBrAPIClasses = brAPIClasses.stream().collect(Collectors.toMap(BrAPIClass::getName, Function.identity()));
            this.cachePredicate = cachePredicate;
            brAPIClassMap = new TreeMap<>();
            usedBy = new TreeMap<>();
            dependsOn = new TreeMap<>();
            primaryClasses = new ArrayList<>();

            brAPIClasses.stream().filter(cachePredicate).forEach(this::cacheClass);

            exclusiveDependencies = new TreeMap<>();
            commonDependencies = new TreeMap<>();
            allDependencies = new ArrayList<>();

            usedBy.forEach((key, value) -> {
                BrAPIClass dependentClass = brAPIClassMap.get(key);

                if (value.size() == 1) {
                    exclusiveDependencies.computeIfAbsent(value.iterator().next(), k -> new TreeSet<>()).add(dependentClass);
                } else {
                    commonDependencies.computeIfAbsent(value.iterator().next(), k -> new TreeSet<>()).add(dependentClass);
                    if (!cachePredicate.test(dependentClass)) {
                        allDependencies.add(dependentClass);
                    }
                }
            });
        }

        /**
         * Get the class by name, which includes all classes even those that did not match the {@link #cachePredicate}
         *
         * @param typeName the class name
         * @return the requested class
         */
        public BrAPIClass getBrAPIClass(String typeName) {
            return allBrAPIClasses.get(typeName);
        }

        /**
         * Dereferences a BrAPIReferenceType by replacing it with the referenced type from the cache.
         *
         * @param type the type to be dereferenced
         * @return the referenced type
         */
        public BrAPIType dereferenceType(BrAPIType type) {
            if (type instanceof BrAPIReferenceType) {
                return allBrAPIClasses.get(type.getName());
            } else {
                return type;
            }
        }

        private BrAPIClass cacheClass(BrAPIClass brAPIClass) {
            primaryClasses.add(brAPIClass);
            cacheType(brAPIClass);

            return brAPIClass;
        }

        private BrAPIType cacheType(BrAPIType brAPIType) {
            if (brAPIClassMap.containsKey(brAPIType.getName())) {
                brAPIClassMap.get(brAPIType.getName());
            }

            switch (brAPIType) {
                case BrAPIClass brAPIClass -> {
                    if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                        brAPIClassMap.put(brAPIClass.getName(), brAPIObjectType);
                        brAPIObjectType.getProperties().forEach(property -> cacheProperty(brAPIObjectType, property));
                    } else if (brAPIType instanceof BrAPIOneOfType brAPIOneOfType) {
                        brAPIClassMap.put(brAPIClass.getName(), brAPIOneOfType);

                        brAPIOneOfType.getPossibleTypes().forEach(this::cacheType);
                    } else if (brAPIClass instanceof BrAPIEnumType brAPIEnumType) {
                        brAPIClassMap.put(brAPIClass.getName(), brAPIEnumType);
                    }
                }
                case BrAPIArrayType brAPIArrayType -> {
                    return cacheType(brAPIArrayType.getItems());
                }
                case BrAPIReferenceType brAPIReferenceType -> {
                    BrAPIClass brAPIClass = allBrAPIClasses.get(brAPIReferenceType.getName());
                    if (brAPIClass == null) {
                        throw new IllegalStateException("No BrAPIClass with name " + brAPIReferenceType.getName() + " found");
                    }
                    brAPIClassMap.put(brAPIClass.getName(), brAPIClass);
                    return brAPIClass;
                }
                default -> {
                }
            }

            return brAPIType;
        }

        private void cacheProperty(BrAPIObjectType brAPIObjectType, BrAPIObjectProperty property) {
            BrAPIType type = cacheType(property.getType());

            if (type instanceof BrAPIClass brAPIClass) {
                usedBy.computeIfAbsent(brAPIClass.getName(), list -> new TreeSet<>()).add(brAPIObjectType.getName());
                dependsOn.computeIfAbsent(brAPIObjectType.getName(), list -> new TreeSet<>()).add(brAPIClass.getName());
            }
        }

        /**
         * Gets the set of BrAPIClass Names in the cache. Includes primary classes
         * that passed the {@link #cachePredicate} and any dependent classes.
         *
         * @return the set of BrAPIClass Names in the cache
         */
        public Set<String> getBrAPIClassNames() {
            return brAPIClassMap.keySet();
        }

        /**
         * Gets the All the BrAPIClasses that were originally passed to the cache regardless of if they
         * passed the {@link #cachePredicate}
         *
         * @return all the BrAPIClasses that were originally passed to the cache regardless of if they
         * passed the {@link #cachePredicate}
         */
        public List<BrAPIClass> getAllBrAPIClasses() {
            return new ArrayList<>(allBrAPIClasses.values());
        }

        /**
         * Gets the BrAPIClasses in the cache as a new list. Includes primary classes
         * that passed the {@link #cachePredicate} and any dependent classes.
         *
         * @return the BrAPIClasses in the cache as a new list, including dependent classes
         */
        public List<BrAPIClass> getBrAPIClasses() {
            return new ArrayList<>(brAPIClassMap.values());
        }

        /**
         * Gets the BrAPIClasses in the cache as a new set. Includes primary classes
         * that passed the {@link #cachePredicate} and any dependent classes.
         *
         * @return the BrAPIClasses in the cache as a new set, including dependent classes
         */
        public Set<BrAPIClass> getBrAPIClassesAsSet() {
            return new TreeSet<>(brAPIClassMap.values());
        }

        /**
         * Determines if the cache contains a BrAPIClass by name. Includes primary classes
         * that passed the {@link #cachePredicate} and any dependent classes.
         *
         * @param name the name of the BrAPIClass
         * @return {@code true} if there is a BrAPIClass by this the provided name
         * {@code false} otherwise
         */
        public boolean containsBrAPIClass(String name) {
            return brAPIClassMap.containsKey(name);
        }

        /**
         * Gets the BrAPIClass that are dependencies use by the specified BrAPIClass,
         * but also used by other BrAPIClasses
         * and are not part of those that passed the {@link #cachePredicate}.
         *
         * @param name the name of the BrAPIClass
         * @return the BrAPIClasses the exclusive dependencies for the specified BrAPIClass
         */
        public Set<BrAPIClass> getCommonDependencies(String name) {
            return commonDependencies.getOrDefault(name, new TreeSet<>());
        }

        /**
         * Gets the BrAPIClass that are dependencies exclusively for the specified BrAPIClass,
         * but are not part of those that passed the {@link #cachePredicate}.
         *
         * @param name the name of the BrAPIClass
         * @return the BrAPIClasses the exclusive dependencies for the specified BrAPIClass
         */
        public Set<BrAPIClass> getExclusiveDependencies(String name) {
            return exclusiveDependencies.getOrDefault(name, new TreeSet<>());
        }

        /**
         * Gets the BrAPIClasses that are used by the provided BrAPIClass
         *
         * @param name the name of the BrAPIClass
         * @return the BrAPIClasses that are used by the provided BrAPIClass
         */
        public List<BrAPIClass> usedBy(String name) {
            Set<String> dependsOnType = usedBy.get(name);

            if (dependsOnType != null) {
                return usedBy.get(name).stream().map(brAPIClassMap::get).collect(Collectors.toList());
            }

            return Collections.emptyList();
        }

        /**
         * Gets the BrAPIClasses that depend on the provided BrAPIClass
         *
         * @param name the name of the BrAPIClass
         * @return the BrAPIClasses that depend on the provided BrAPIClass
         */
        public List<BrAPIClass> dependsOn(String name) {
            Set<String> dependsOnType = dependsOn.get(name);

            if (dependsOnType != null) {
                return dependsOn.get(name).stream().map(brAPIClassMap::get).collect(Collectors.toList());
            }

            return Collections.emptyList();
        }

        /**
         * Gets the number of classes in the cache
         *
         * @return the number of classes in the cache
         */
        public int size() {
            return brAPIClassMap.size();
        }
    }
}
