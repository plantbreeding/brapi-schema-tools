package org.brapi.schematools.core.utils;

import lombok.AllArgsConstructor;
import org.brapi.schematools.core.model.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Utility class for creating a cache of {@link BrAPIClass}es. Takes a list of
 * classes and caches those in the list if they pass the provided {@link #cachePredicate}.
 * Additional classes are added to the cached depending on the subclass of {@link BrAPIClass}
 * For {@link BrAPIObjectType} utility checks the properties and
 * tries to cache any that are the return type of these properties {@link BrAPIClass}es.
 * If an {@link BrAPIArrayType} is encountered then the {@see BrAPIArrayType#getItems()} is
 * checked recursively to be included in the cache.
 * For {@link BrAPIOneOfType} it is added to the cache and any of {@see BrAPIOneOfType#getPossibleTypes()}
 * are checked recursively to be included in the cache.
 * {@link BrAPIAllOfType} are ignored.
 */
@AllArgsConstructor
public class BrAPIClassCacheUtil {

    private Predicate<BrAPIClass> cachePredicate ;

    /**
     * Create BrAPIClassCacheUtil where all classes are cached.
     */
    public BrAPIClassCacheUtil() {
        this(brAPIClass -> true);
    }

    /**
     * Creates the cache of classes as a Map
     * @param brAPIClasses the list of possible classes to be cached.
     * @return the cache of classes as a Map
     */
    public Map<String, BrAPIClass> createMap(List<BrAPIClass> brAPIClasses) {
        return new Cache(brAPIClasses).brAPIClassMap ;
    }

    private class Cache {

        private final Map<String, BrAPIClass> brAPIClassMap ;

        public Cache(List<BrAPIClass> brAPIClasses) {

            brAPIClassMap = new TreeMap<>();

            for (BrAPIClass brAPIClass : brAPIClasses) {
                cacheClass(brAPIClass);
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
    }
}
