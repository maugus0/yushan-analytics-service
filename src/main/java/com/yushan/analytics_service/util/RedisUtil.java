package com.yushan.analytics_service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis utility class for caching gamification-related data.
 * Provides methods for caching achievements, transactions, stats and related queries.
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Cache key prefixes
    private static final String ACHIEVEMENT_PREFIX = "achievement:";
    private static final String TRANSACTION_PREFIX = "transaction:";
    private static final String STATS_PREFIX = "stats:";
    private static final String USER_PREFIX = "user:";
    private static final String GAMIFICATION_PREFIX = "gamification:";

    // Cache TTL constants
    private static final Duration ACHIEVEMENT_CACHE_TTL = Duration.ofHours(2);
    private static final Duration TRANSACTION_CACHE_TTL = Duration.ofHours(1);
    private static final Duration STATS_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration USER_CACHE_TTL = Duration.ofHours(1);
    private static final Duration GAMIFICATION_CACHE_TTL = Duration.ofMinutes(15);

    /**
     * Set a key-value pair with TTL
     */
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * Set a key-value pair with TTL in seconds
     */
    public void set(String key, Object value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * Get value by key
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Get value by key with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Delete key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Delete multiple keys
     */
    public void delete(Set<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * Check if key exists
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Increment a numeric value
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * Increment a numeric value by delta
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * Set expiration for existing key
     */
    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }

    /**
     * Get keys matching pattern
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    // Achievement-specific cache methods

    /**
     * Cache achievement data
     */
    public void cacheAchievement(Integer achievementId, Object achievementData) {
        String key = ACHIEVEMENT_PREFIX + achievementId;
        set(key, achievementData, ACHIEVEMENT_CACHE_TTL);
    }

    /**
     * Get cached achievement data
     */
    public Object getCachedAchievement(Integer achievementId) {
        String key = ACHIEVEMENT_PREFIX + achievementId;
        return get(key);
    }

    /**
     * Get cached achievement data with type casting
     */
    public <T> T getCachedAchievement(Integer achievementId, Class<T> clazz) {
        String key = ACHIEVEMENT_PREFIX + achievementId;
        return get(key, clazz);
    }

    /**
     * Cache user achievements
     */
    public void cacheUserAchievements(String userId, Object achievementsData) {
        String key = ACHIEVEMENT_PREFIX + "user:" + userId;
        set(key, achievementsData, ACHIEVEMENT_CACHE_TTL);
    }

    /**
     * Get cached user achievements
     */
    public Object getCachedUserAchievements(String userId) {
        String key = ACHIEVEMENT_PREFIX + "user:" + userId;
        return get(key);
    }

    /**
     * Delete achievement cache
     */
    public void deleteAchievementCache(Integer achievementId) {
        String key = ACHIEVEMENT_PREFIX + achievementId;
        delete(key);
    }

    // Transaction-specific cache methods

    /**
     * Cache transaction data
     */
    public void cacheTransaction(Integer transactionId, Object transactionData) {
        String key = TRANSACTION_PREFIX + transactionId;
        set(key, transactionData, TRANSACTION_CACHE_TTL);
    }

    /**
     * Get cached transaction data
     */
    public Object getCachedTransaction(Integer transactionId) {
        String key = TRANSACTION_PREFIX + transactionId;
        return get(key);
    }

    /**
     * Cache user transactions
     */
    public void cacheUserTransactions(String userId, Object transactionsData) {
        String key = TRANSACTION_PREFIX + "user:" + userId;
        set(key, transactionsData, TRANSACTION_CACHE_TTL);
    }

    /**
     * Get cached user transactions
     */
    public Object getCachedUserTransactions(String userId) {
        String key = TRANSACTION_PREFIX + "user:" + userId;
        return get(key);
    }

    /**
     * Delete transaction cache
     */
    public void deleteTransactionCache(Integer transactionId) {
        String key = TRANSACTION_PREFIX + transactionId;
        delete(key);
    }

    // Stats-specific cache methods

    /**
     * Cache gamification statistics
     */
    public void cacheGamificationStats(String userId, Object statsData) {
        String key = STATS_PREFIX + "user:" + userId;
        set(key, statsData, STATS_CACHE_TTL);
    }

    /**
     * Get cached gamification statistics
     */
    public Object getCachedGamificationStats(String userId) {
        String key = STATS_PREFIX + "user:" + userId;
        return get(key);
    }

    /**
     * Get cached gamification statistics with type casting
     */
    public <T> T getCachedGamificationStats(String userId, Class<T> clazz) {
        String key = STATS_PREFIX + "user:" + userId;
        return get(key, clazz);
    }

    // User-specific cache methods

    /**
     * Cache user profile data
     */
    public void cacheUserProfile(String userId, Object profileData) {
        String key = USER_PREFIX + "profile:" + userId;
        set(key, profileData, USER_CACHE_TTL);
    }

    /**
     * Get cached user profile data
     */
    public Object getCachedUserProfile(String userId) {
        String key = USER_PREFIX + "profile:" + userId;
        return get(key);
    }

    /**
     * Cache user level data
     */
    public void cacheUserLevel(String userId, Object levelData) {
        String key = USER_PREFIX + "level:" + userId;
        set(key, levelData, USER_CACHE_TTL);
    }

    /**
     * Get cached user level data
     */
    public Object getCachedUserLevel(String userId) {
        String key = USER_PREFIX + "level:" + userId;
        return get(key);
    }

    // General gamification cache methods

    /**
     * Cache general gamification data
     */
    public void cacheGamificationData(String dataType, String identifier, Object data) {
        String key = GAMIFICATION_PREFIX + dataType + ":" + identifier;
        set(key, data, GAMIFICATION_CACHE_TTL);
    }

    /**
     * Get cached general gamification data
     */
    public Object getCachedGamificationData(String dataType, String identifier) {
        String key = GAMIFICATION_PREFIX + dataType + ":" + identifier;
        return get(key);
    }

    // Cache invalidation methods

    /**
     * Invalidate all achievement-related caches
     */
    public void invalidateAchievementCaches(Integer achievementId) {
        deleteAchievementCache(achievementId);
        
        // Invalidate user achievement caches
        Set<String> achievementKeys = keys(ACHIEVEMENT_PREFIX + "user:*");
        if (!achievementKeys.isEmpty()) {
            delete(achievementKeys);
        }
    }

    /**
     * Invalidate all transaction-related caches
     */
    public void invalidateTransactionCaches(Integer transactionId) {
        deleteTransactionCache(transactionId);
        
        // Invalidate user transaction caches
        Set<String> transactionKeys = keys(TRANSACTION_PREFIX + "user:*");
        if (!transactionKeys.isEmpty()) {
            delete(transactionKeys);
        }
    }

    /**
     * Invalidate all user-related caches
     */
    public void invalidateUserCaches(String userId) {
        Set<String> userKeys = keys(USER_PREFIX + "*:" + userId);
        if (!userKeys.isEmpty()) {
            delete(userKeys);
        }
        
        // Invalidate stats caches
        Set<String> statsKeys = keys(STATS_PREFIX + "user:" + userId);
        if (!statsKeys.isEmpty()) {
            delete(statsKeys);
        }
    }

    /**
     * Invalidate all gamification caches
     */
    public void invalidateGamificationCaches() {
        Set<String> gamificationKeys = keys(GAMIFICATION_PREFIX + "*");
        if (!gamificationKeys.isEmpty()) {
            delete(gamificationKeys);
        }
    }

    /**
     * Clear all caches (use with caution)
     */
    public void clearAllCaches() {
        Set<String> allKeys = keys("*");
        if (!allKeys.isEmpty()) {
            delete(allKeys);
        }
    }

    // Sorted Set (ZSet) operations for ranking

    /**
     * Add member to sorted set with score
     */
    public Boolean zAdd(String key, String member, double score) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }

    /**
     * Get the cardinality (number of elements) of a sorted set
     */
    public Long zCard(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * Get elements from sorted set in reverse order (highest score first)
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        Set<Object> result = redisTemplate.opsForZSet().reverseRange(key, start, end);
        if (result == null) {
            return null;
        }
        return result.stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Get the rank of member in sorted set (reverse order, highest score = rank 0)
     */
    public Long zReverseRank(String key, String member) {
        return redisTemplate.opsForZSet().reverseRank(key, member);
    }

    /**
     * Get the score of a member in sorted set
     */
    public Double zScore(String key, String member) {
        return redisTemplate.opsForZSet().score(key, member);
    }

    /**
     * Delete multiple keys by collection
     */
    public void delete(java.util.Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}