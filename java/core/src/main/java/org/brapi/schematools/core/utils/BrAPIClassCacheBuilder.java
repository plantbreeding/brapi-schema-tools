package org.brapi.schematools.core.utils;

import lombok.AccessLevel;
import lombok.Getter;
import org.brapi.schematools.core.model.*;

import java.util.*;
import java.util.function.Predicate;

/**
 * Creates a cache of {@link BrAPIClass}es.
 * Takes a list of
 * classes and caches those in the list if they pass the provided cachePredicate.
 * Additional classes are added to the cached depending on the subclass of {@link BrAPIClass}
 * For {@link BrAPIObjectType} utility checks the properties and
 * tries to cache any that are the return type of these properties {@link BrAPIClass}es.
 * If an {@link BrAPIArrayType} is encountered then the {@see BrAPIArrayType#getItems()} is
 * checked recursively to be included in the cache.
 * For {@link BrAPIOneOfType} it is added to the cache and any of {@see BrAPIOneOfType#getPossibleTypes()}
 * are checked recursively to be included in the cache.
 * {@link BrAPIAllOfType} are ignored.
 */
public class BrAPIClassCacheBuilder {
    /**
     * Creates the cache
     * @param brAPIClasses the list of possible classes to be cached.
     * @return the cache of classes
     */
    public static BrAPIClassCache createCache(List<BrAPIClass> brAPIClasses) {
        return new BrAPIClassCache(brAPIClass -> true, brAPIClasses) ;
    }

    /**
     * Creates the cache with a cache predicate that determines if a class is added to the cache.
     * @param cachePredicate the cache predicate that determines if a class is added to the cache.
     * @param brAPIClasses the list of possible classes to be cached.
     * @return the cache of classes
     */
    public static BrAPIClassCache createCache(Predicate<BrAPIClass> cachePredicate, List<BrAPIClass> brAPIClasses) {
        return new BrAPIClassCache(cachePredicate, brAPIClasses) ;
    }

    /**
     * Creates the cache of classes as a Map
     * @param brAPIClasses the list of possible classes to be cached.
     * @return the cache of classes as a Map
     */
    public static Map<String, BrAPIClass> createMap(List<BrAPIClass> brAPIClasses) {
        return createCache(brAPIClasses).getBrAPIClassMap() ;
    }

    /**
     * Creates the cache of classes as a Map with a cache predicate that determines if a class is added to the cache.
     * @param cachePredicate the cache predicate that determines if a class is added to the cache.
     * @param brAPIClasses the list of possible classes to be cached.
     * @return the cache of classes as a Map
     */
    public static Map<String, BrAPIClass> createMap(Predicate<BrAPIClass> cachePredicate, List<BrAPIClass> brAPIClasses) {
        return createCache(cachePredicate, brAPIClasses).getBrAPIClassMap() ;
    }

    public static class BrAPIClassCache {
        private final Predicate<BrAPIClass> cachePredicate ;
        @Getter(AccessLevel.PRIVATE)
        private final Map<String, BrAPIClass> brAPIClassMap ;

        public BrAPIClassCache(Predicate<BrAPIClass> cachePredicate, List<BrAPIClass> brAPIClasses) {
            this.cachePredicate = cachePredicate ;
            brAPIClassMap = new TreeMap<>();

            for (BrAPIClass brAPIClass : brAPIClasses) {
                cacheClass(brAPIClass);
            }
        }

        /**
         * Get the class by name
         * @param typeName the class by name
         * @return the requested class
         */
        public BrAPIClass getBrAPIClass(String typeName) {
            return brAPIClassMap.get(typeName) ;
        }

        /**
         * Dereferences a BrAPIReferenceType by replacing it with the referenced type from the cache.
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

        private void cacheClass(BrAPIType brAPIType) {
            if (brAPIType instanceof BrAPIClass brAPIClass && cachePredicate.test(brAPIClass) && !brAPIClassMap.containsKey(brAPIClass.getName())) {
                if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                    brAPIClassMap.put(brAPIClass.getName(), brAPIObjectType);

                    brAPIObjectType.getProperties().forEach(property -> cacheClass(property.getType()));
                } else if (brAPIType instanceof BrAPIOneOfType brAPIOneOfType) {
                    brAPIClassMap.put(brAPIClass.getName(), brAPIOneOfType);

                    brAPIOneOfType.getPossibleTypes().forEach(this::cacheClass);
                } else if (brAPIClass instanceof BrAPIEnumType brAPIEnumType) {
                    brAPIClassMap.put(brAPIClass.getName(), brAPIEnumType);
                }
            } else if (brAPIType instanceof BrAPIArrayType brAPIArrayType) {
                cacheClass(brAPIArrayType.getItems());
            }
        }

        /**
         * Gets the set of BrAPIClass Names in the cache
         * @return the set of BrAPIClass Names in the cache
         */
        public Set<String> getBrAPIClassNames() {
            return brAPIClassMap.keySet() ;
        }

        /**
         * Gets the BrAPIClasses in the cache
         * @return the BrAPIClasses in the cache
         */
        public Collection<BrAPIClass> getBrAPICClasses() {
            return brAPIClassMap.values() ;
        }

        /**
         * Gets the BrAPIClasses in the cache as a new list
         * @return the BrAPIClasses in the cache as a new list
         */
        public List<BrAPIClass> getBrAPICClassesAsList() {
            return new ArrayList<>(brAPIClassMap.values()) ;
        }

        /**
         * Gets the BrAPIClasses in the cache as a new set
         * @return the BrAPIClasses in the cache as a new set
         */
        public Set<BrAPIClass> getBrAPICClassesAsSet() {
            return new TreeSet<>(brAPIClassMap.values()) ;
        }

        /**
         * Determines if the cache contains a BrAPIClass by name
         * @param name the name of the BrAPIClass
         * @return {@code true} if there is a BrAPIClass by this the provided name
         * {@code false} otherwise
         */
        public boolean containsBrAPIClass(String name) {
            return brAPIClassMap.containsKey(name) ;
        }
    }
}
