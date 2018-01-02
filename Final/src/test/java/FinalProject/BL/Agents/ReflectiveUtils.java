package FinalProject.BL.Agents;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectiveUtils {

    public static <InstanceType, FieldType> void setFiledValue(InstanceType instance, String fieldName,
                                                               FieldType value)
            throws Exception
    {
        Field field = instance.getClass().getDeclaredField(fieldName);
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(instance, value);
        field.setAccessible(accessible);
    }

    public static <T> Object invokeMethod(T instance, String methodName, Object... params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Class<?>[] classes = new Class[params.length];
        for (int i = 0; i < classes.length; i++)
        {
            classes[i] = params[i].getClass();
        }
        Method method = instance.getClass().getDeclaredMethod(methodName, classes);
        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        Object returned = null;

//        //first param needs to be instance
//        Object[] paramsForInvoke = new Object[params.length + 1];
//        paramsForInvoke[0] = instance;
//        System.arraycopy(params, 0, paramsForInvoke, 1, paramsForInvoke.length - 1);

        if (!method.getReturnType().getSimpleName().equals("void"))
        {
            returned = method.invoke(instance, params);
        }
        else {
            method.invoke(instance, params);
        }
        method.setAccessible(accessible);
        return returned;
    }

}
