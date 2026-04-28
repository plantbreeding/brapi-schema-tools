package org.brapi.schematools.core.utils;

import lombok.AccessLevel;
import lombok.Getter;
import org.brapi.schematools.core.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.brapi.schematools.core.utils.BrAPITypeUtils.isPrimaryModel;
import static org.brapi.schematools.core.utils.BrAPITypeUtils.isRequest;

/**
 * Creates a cache of {@link BrAPIClass}es.
 * Takes a list of classes and caches those in the list of primary classes if they pass the provided cachePredicate.
 * Classes that do not pass the cachePredicate are not added to the list of primary classes, but are still included
 * in the list of all classes, can can be found in {@link BrAPIClassCache#getAllBrAPIClasses()}
 * Additional classes are added to the cache as dependent classes as follows
 * For {@link BrAPIObjectType} utility checks the properties and
 * tries to cache any that are the return type of these properties {@link BrAPIClass}es.
 * If an {@link BrAPIArrayType} is encountered then the {@see BrAPIArrayType#getItems()} is
 * checked to be included in the cache.
 * For {@link BrAPIOneOfType} it is added to the cache and any of {@see BrAPIOneOfType#getPossibleTypes()}
 * are checked to be included in the cache.
 * {@link BrAPIAllOfType} are ignored.
 *  Primary lasses are available from {@link BrAPIClassCache#getPrimaryClasses()}
 * Cached classes are available from {@link BrAPIClassCache#getBrAPIClasses()}
 * or {@link BrAPIClassCache#getBrAPIClassesAsSet()}
 * All dependent classes are available from {@link BrAPIClassCache#getAllNonPrimaryDependencies()},
 * these are classes that were not in the original list by are referenced by those in the list
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
     * Creates the cache with a cache predicate that determines if a class is added to the cache as a primary class.
     *
     * @param cachePredicate the cache predicate that determines if a class is added to the cache as a primary class.
     * @param brAPIClasses   the list of classes to be cached.
     * @return the cache of classes
     */
    public static BrAPIClassCache createCache(Predicate<BrAPIClass> cachePredicate, List<BrAPIClass> brAPIClasses) {
        return new BrAPIClassCache(cachePredicate, brAPIClasses);
    }

    public static class BrAPIClassCache {
        private static final String REQUEST_NAME_FORMAT = "%sRequest" ;
        private final Predicate<BrAPIClass> cachePredicate;
        private final Map<String, BrAPIClass> inputClassMap;
        @Getter(AccessLevel.PRIVATE)
        private final Map<String, BrAPIClass> brAPIClassMap;
        // Contains as keys type names, and elements a list of classes that is used by that type
        private final Map<String, Set<String>> dependsOn;
        // Contains as keys type names, and elements a list of classes that depend on the type
        private final Map<String, Set<String>> usedBy;
        private final Map<String, Set<BrAPIClass>> exclusiveDependencies;
        private final Map<String, Set<BrAPIClass>> commonDependencies;
        private final Map<String, Set<BrAPIClass>> primaryDependencies;
        @Getter
        private final List<BrAPIClass> primaryClasses;
        @Getter
        private final List<BrAPIClass> allNonPrimaryDependencies;

        public BrAPIClassCache(Predicate<BrAPIClass> cachePredicate, List<BrAPIClass> brAPIClasses) {
            this.inputClassMap = brAPIClasses.stream().collect(Collectors.toMap(BrAPIClass::getName, Function.identity()));

            this.cachePredicate = cachePredicate;
            brAPIClassMap = new TreeMap<>();

            usedBy = new TreeMap<>();
            dependsOn = new TreeMap<>();
            primaryClasses = new ArrayList<>();

            brAPIClasses.forEach(this::cacheClass);

            exclusiveDependencies = new TreeMap<>();
            commonDependencies = new TreeMap<>();
            primaryDependencies = new TreeMap<>();
            allNonPrimaryDependencies = new ArrayList<>();

            usedBy.forEach((key, value) -> {
                BrAPIClass dependentClass = brAPIClassMap.get(key);

                if (isPrimaryClass(dependentClass)) {
                    for (String element : value) {
                        primaryDependencies.computeIfAbsent(element, k -> new TreeSet<>()).add(dependentClass);
                    }

                } else {
                    if (value.size() == 1) {
                        exclusiveDependencies.computeIfAbsent(value.iterator().next(), k -> new TreeSet<>()).add(dependentClass);
                    } else {
                        for (String element : value) {
                            commonDependencies.computeIfAbsent(element, k -> new TreeSet<>()).add(dependentClass);
                        }
                    }

                    allNonPrimaryDependencies.add(dependentClass);
                }
            });
        }

        private boolean isPrimaryClass(BrAPIClass brAPIClass) {
            return inputClassMap.containsKey(brAPIClass.getName()) && cachePredicate.test(brAPIClass);
        }

        /**
         * Get the class by name, which includes all classes even those that did not match the {@link #cachePredicate}
         * and dependent classes
         *
         * @param typeName the class name
         * @return the requested class
         */
        public BrAPIClass getBrAPIClass(String typeName) {
            return brAPIClassMap.get(typeName);
        }

        /**
         * Dereferences a BrAPIReferenceType by replacing it with the referenced type from the cache.
         *
         * @param type the type to be dereferenced
         * @return the referenced type
         */
        public BrAPIType dereferenceType(BrAPIType type) {
            if (type instanceof BrAPIReferenceType) {
                return brAPIClassMap.get(type.getName());
            } else {
                return type;
            }
        }

        private void cacheClass(BrAPIClass brAPIClass) {
            if (cachePredicate.test(brAPIClass)) {
                primaryClasses.add(brAPIClass);
            }
            cacheType(brAPIClass);
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
                    // check in the cache, if not fall back to the input classes
                    BrAPIClass brAPIClass = brAPIClassMap.get(brAPIReferenceType.getName());

                    if (brAPIClass == null) {
                        // if the class is not in the cache, then we need to cache it and its dependencies
                        brAPIClass = inputClassMap.get(brAPIReferenceType.getName());

                        if (brAPIClass == null) {
                            throw new IllegalStateException("No BrAPIClass with name " + brAPIReferenceType.getName() + " found");
                        }

                        return cacheType(inputClassMap.get(brAPIReferenceType.getName()));
                    }
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
         * Gets the BrAPIClasses in the cache as a new list. Includes all classes that were originally passed to the
         * cache regardless of if they passed the {@link #cachePredicate}, any and dependent classes
         *
         * @return all the BrAPIClasses in the cache as a new list, including dependent classes
         */
        public List<BrAPIClass> getBrAPIClasses() {
            return new ArrayList<>(brAPIClassMap.values());
        }

        /**
         * Gets the BrAPIClasses in the cache as a new set. Includes all classes that were originally passed to the
         * cache regardless of if they passed the {@link #cachePredicate}, any and dependent classes
         *
         * @return all the BrAPIClasses in the cache as a new list, including dependent classes
         */
        public Set<BrAPIClass> getBrAPIClassesAsSet() {
            return new TreeSet<>(brAPIClassMap.values());
        }

        /**
         * Gets the BrAPIClasses in the cache as a new map. Includes all classes that were originally passed to the
         * cache regardless of if they passed the {@link #cachePredicate}, any and dependent classes
         *
         * @return all the BrAPIClasses in the cache as a new map, including dependent classes
         */
        public Map<String, BrAPIClass> getBrAPIClassesAsMap() {
            return new TreeMap<>(brAPIClassMap);
        }

        /**
         * Determines if the cache contains a BrAPIClass by name. Includes all classes that were originally passed to the
         * cache regardless of if they passed the {@link #cachePredicate}, any and dependent classes
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
         * but also used by at least one other BrAPIClass
         * and are not in of those originally passed to the cache
         *
         * @param name the name of the BrAPIClass
         * @return the common dependencies for the specified BrAPIClass
         */
        public Set<BrAPIClass> getCommonDependencies(String name) {
            return commonDependencies.getOrDefault(name, new TreeSet<>());
        }

        /**
         * Gets the BrAPIClass that are dependencies exclusively for the specified BrAPIClass,
         * but are not in of those originally passed to the cache
         *
         * @param name the name of the BrAPIClass
         * @return the exclusive dependencies for the specified BrAPIClass
         */
        public Set<BrAPIClass> getExclusiveDependencies(String name) {
            return exclusiveDependencies.getOrDefault(name, new TreeSet<>());
        }

        /**
         * Gets dependencies of the BrAPIClass that also passed the {@link #cachePredicate}.
         *
         * @param name the name of the BrAPIClass
         * @return dependencies of the BrAPIClass that also passed the {@link #cachePredicate}.
         */
        public Set<BrAPIClass> getPrimaryDependencies(String name) {
            return primaryDependencies.getOrDefault(name, new TreeSet<>());
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

        /**
         * Determines if the cache contains a BrAPIClass by name, and it is a primary model
         *
         * @param name the name of the BrAPIClass
         * @return {@code true} if there is a BrAPIClass by this the provided name, and it is a primary model
         * {@code false} otherwise
         */
        public boolean containsPrimaryModel(String name) {
            BrAPIClass brAPIClass = brAPIClassMap.get(name);

            return brAPIClass != null && isPrimaryModel(brAPIClass);
        }

        /**
         * Gets the BrAPI Request class for a BrAPI Class
         *
         * @param name the name of the BrAPIClass
         * @return the BrAPI Request class for a BrAPI Class
         */
        public BrAPIClass getBrAPIRequestClass(String name) {
            return brAPIClassMap.get(String.format(REQUEST_NAME_FORMAT, name));
        }

        /**
         * Gets the BrAPI Request class for a BrAPI Class
         *
         * @param brAPIClass the BrAPIClass
         * @return the BrAPI Request class for a BrAPI Class
         */
        public BrAPIClass getBrAPIRequestClass(BrAPIClass brAPIClass) {
            return brAPIClassMap.get(String.format(REQUEST_NAME_FORMAT, brAPIClass.getName()));
        }

        /**
         * Gets the BrAPI Object for a BrAPI Request Class, if is not a BrAPI Request Class
         * it returns the input object if possible or {code}null{code}.
         *
         * @param brAPIClass the BrAPIClass
         * @return the BrAPI Request class for a BrAPI Class
         */
        public BrAPIObjectType getBrAPIObjectForRequestClass(BrAPIClass brAPIClass) {

            BrAPIClass type = brAPIClass;

            if (isRequest(brAPIClass)) {
                if (brAPIClass.getName().endsWith("Request")) {
                    type = brAPIClassMap.get(brAPIClass.getName().substring(0, brAPIClass.getName().length() - 7));
                }
            }

            if (type instanceof BrAPIObjectType brAPIObjectType) {
                return brAPIObjectType;
            }

            return null;
        }
    }
}
