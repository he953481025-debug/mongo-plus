import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.ibatis.reflection.ReflectionException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.util.StringUtils;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: brandon
 * @date: 2021/11/13 11:23
 */

public class MongoLambdaQueryWrapper<T> {

    private static Map<String, WeakReference<SerializedLambda>> CLASS_LAMBDA_CACHE = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, String>> Field_CACHE_MAP = new ConcurrentHashMap<>();

    private Class<T> entityClass;

    private MongoQuery mongoQuery;

    private Map<String, String> fieldMap;


    private MongoLambdaQueryWrapper(Class<T> entityClass) {
        this.entityClass = entityClass;
        mongoQuery = new MongoQuery();
        initFieldCache();
    }

    public static <T> MongoLambdaQueryWrapper<T> lambdaQuery(Class<T> entityClass) {
        return new MongoLambdaQueryWrapper<>(entityClass);
    }

    public MongoLambdaQueryWrapper<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            mongoQuery.addCriteria(Criteria.where(columnToString(column)).is(val));
        }
        return this;
    }

    public MongoLambdaQueryWrapper<T> ne(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            mongoQuery.addCriteria(Criteria.where(columnToString(column)).ne(val));
        }
        return this;
    }

    public MongoLambdaQueryWrapper<T> lt(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            mongoQuery.addCriteria(Criteria.where(columnToString(column)).lt(val));
        }
        return this;
    }

    public MongoLambdaQueryWrapper<T> lte(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            mongoQuery.addCriteria(Criteria.where(columnToString(column)).lte(val));
        }
        return this;
    }

    public MongoLambdaQueryWrapper<T> gt(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            mongoQuery.addCriteria(Criteria.where(columnToString(column)).gt(val));
        }
        return this;
    }

    public MongoLambdaQueryWrapper<T> gt(SFunction<T, ?> column, Object val) {
        gt(true,column,val);
        return this;
    }

    public MongoLambdaQueryWrapper<T> gte(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            mongoQuery.addCriteria(Criteria.where(columnToString(column)).gte(val));
        }
        return this;
    }

    public MongoLambdaQueryWrapper<T> in(boolean condition, SFunction<T, ?> column, Object... values) {
        if (condition) {
            mongoQuery.addCriteria(Criteria.where(columnToString(column)).in(values));
        }
        return this;
    }

    public MongoLambdaQueryWrapper<T> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        if (condition) {
            mongoQuery.addCriteria(Criteria.where(columnToString(column)).in(values));
        }
        return this;
    }

    public MongoLambdaQueryWrapper<T> in(SFunction<T, ?> column, Collection<?> values) {
        in(true,column,values);
        return this;
    }

    public MongoLambdaQueryWrapper<T> in(SFunction<T, ?> column, Object... values) {
        in(true,column,values);
        return this;
    }

    public MongoLambdaQueryWrapper<T> gte(SFunction<T, ?> column, Object val) {
        gte(true,column,val);
        return this;
    }

    public MongoLambdaQueryWrapper<T> lte(SFunction<T, ?> column, Object val) {
        lt(true,column,val);
        return this;
    }

    public MongoLambdaQueryWrapper<T> lt(SFunction<T, ?> column, Object val) {
        lt(true,column,val);
        return this;
    }

    public MongoLambdaQueryWrapper<T> ne(SFunction<T, ?> column, Object val) {
        ne(true,column,val);
        return this;
    }

    public MongoLambdaQueryWrapper<T> eq(SFunction<T, ?> column, Object val) {
        eq(true, column, val);
        return this;
    }

    public MongoLambdaQueryWrapper<T> and(MongoLambdaQueryWrapper<T> wrapper) {
        List<CriteriaDefinition> allCriteriaDefinition = wrapper.buildQuery().getAllCriteriaDefinition();
        mongoQuery.andOperator(allCriteriaDefinition);
        return this;
    }

    public MongoLambdaQueryWrapper<T> or(MongoLambdaQueryWrapper<T> wrapper) {
        List<CriteriaDefinition> allCriteriaDefinition = wrapper.buildQuery().getAllCriteriaDefinition();
        mongoQuery.orOperator(allCriteriaDefinition);
        return this;
    }

    public MongoLambdaQueryWrapper<T> nor(MongoLambdaQueryWrapper<T> wrapper) {
        List<CriteriaDefinition> allCriteriaDefinition = wrapper.buildQuery().getAllCriteriaDefinition();
        mongoQuery.norOperator(allCriteriaDefinition);
        return this;
    }

    public MongoQuery buildQuery() {
        return mongoQuery;
    }

    private String columnToString(SFunction<T, ?> column) {
        return getColumn(resolve(column));
    }

    private String getColumn(SerializedLambda lambda) {
        String implMethodName = lambda.getImplMethodName();
        String fieldName = fieldMap.get(methodNameToProperty(implMethodName));
        if (StringUtils.isEmpty(fieldName)) {
            // 抛异常

        }
        return fieldName;
    }

    private void initFieldCache() {
        Map<String, String> fieldMap = Field_CACHE_MAP.get(entityClass.getName());
        if (Objects.isNull(fieldMap)) {
            synchronized (entityClass) {
                if (Objects.isNull(fieldMap)) {
                    Field_CACHE_MAP.put(entityClass.getName(), createFieldCacheMap());
                }
            }
        }
        if (Objects.isNull(fieldMap)) {
            // 抛出异常

        }
        this.fieldMap = fieldMap;
    }

    private Map<String, String> createFieldCacheMap() {
        Field[] declaredFields = entityClass.getDeclaredFields();
        Map<String, String> map = new HashMap<>(declaredFields.length);
        for (Field declaredField : declaredFields) {
            org.springframework.data.mongodb.core.mapping.Field fieldAnnotation = declaredField.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
            String value = declaredField.getName();
            if (Objects.nonNull(fieldAnnotation)) {
                value = fieldAnnotation.value();
            }
            map.put(declaredField.getName(), value);
        }
        return map;
    }

    private String methodNameToProperty(String methodName) {
        if (methodName.startsWith("is")) {
            methodName = methodName.substring(2);
        } else if (methodName.startsWith("get") || methodName.startsWith("set")) {
            methodName = methodName.substring(3);
        } else {
            throw new ReflectionException("Error parsing property name '" + methodName + "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        if (methodName.length() == 1 || (methodName.length() > 1 && !Character.isUpperCase(methodName.charAt(1)))) {
            methodName = methodName.substring(0, 1).toLowerCase(Locale.ENGLISH) + methodName.substring(1);
        }
        return methodName;
    }


    private <T> SerializedLambda resolve(SFunction<T, ?> column) {
        String className = column.getClass().getName();
        return Optional
                .ofNullable(CLASS_LAMBDA_CACHE.get(className))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambda serializedLambda = serializedLambda(column);
                    CLASS_LAMBDA_CACHE.put(className, new WeakReference<>(serializedLambda));
                    return serializedLambda;
                });
    }

    private <T> SerializedLambda serializedLambda(SFunction<T, ?> column) {
        SerializedLambda serializedLambda = null;
        try {
            Method writeReplace = column.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object sl = writeReplace.invoke(column);
            serializedLambda = (java.lang.invoke.SerializedLambda) sl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serializedLambda;
    }


}
