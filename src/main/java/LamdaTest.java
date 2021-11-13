import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: brandon
 * @date: 2021/11/13 9:35
 */
public class LamdaTest {

    private static Map<Class, SerializedLambda> CLASS_LAMBDA_CACHE = new ConcurrentHashMap<>();

    private String productName;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }


    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        LambdaQueryWrapper<Object> queryWrapper = Wrappers.lambdaQuery();
        Query query = new Query();
        SerializedLambda serializedLambda = doSFunction(LamdaTest::getProductName);
        System.out.println("方法名  " + serializedLambda.getImplMethodName());
        System.out.println("类名" + serializedLambda.getImplClass());
        System.out.println("serializedLambda  " + serializedLambda);

    }

    private static <T, R> SerializedLambda doSFunction(SFunction<T, R> func) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SerializedLambda serializedLambda = CLASS_LAMBDA_CACHE.get(func.getClass());
        if (serializedLambda == null) {
            Method writeReplace = func.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object sl = writeReplace.invoke(func);
            serializedLambda = (SerializedLambda) sl;
            CLASS_LAMBDA_CACHE.put(func.getClass(), serializedLambda);
        }

        return serializedLambda;
    }
}
