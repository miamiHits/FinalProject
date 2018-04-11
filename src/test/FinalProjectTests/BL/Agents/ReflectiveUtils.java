package FinalProjectTests.BL.Agents;

import FinalProject.BL.Agents.SHMGM;
import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.DataObjects.AgentData;
import FinalProject.BL.DataObjects.Problem;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectiveUtils {

    private final static Logger logger = Logger.getLogger(ReflectiveUtils.class);

    public static <InstanceType, FieldType> void setFieldValue(InstanceType instance, String fieldName,
                                                               FieldType value)
            throws IllegalAccessException, NoSuchFieldException

    {
        Field field;
        try {
            field = instance.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = instance.getClass().getSuperclass().getDeclaredField(fieldName);
        }
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(instance, value);
        field.setAccessible(accessible);
    }

    public static <T> Object invokeMethod(T instance, String methodName, Object... params)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?>[] classes = new Class[params.length];
        for (int i = 0; i < classes.length; i++)
        {
            classes[i] = params[i].getClass();
        }
        Method method;
        try {
            method = instance.getClass().getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            method = instance.getClass().getSuperclass().getDeclaredMethod(methodName);
        }
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


    public static SmartHomeAgent initSmartHomeAgentForTest(Problem problem) {

        SmartHomeAgent agent = new SmartHomeAgent();
        AgentData agentData = problem.getAgentsData().get(0);
        String problemId = problem.getId();
        try
        {
            FinalProjectTests.BL.Agents.ReflectiveUtils.setFieldValue(agentData, "priceScheme", problem.getPriceScheme());

            //agent.setup() will not be called so we'll do it manually
            FinalProjectTests.BL.Agents.ReflectiveUtils.setFieldValue(agent, "agentData", agentData);
            FinalProjectTests.BL.Agents.ReflectiveUtils.setFieldValue(agent, "problemId", problemId);
            FinalProjectTests.BL.Agents.ReflectiveUtils.setFieldValue(agent, "algoId", "DSA");
            FinalProjectTests.BL.Agents.ReflectiveUtils.setFieldValue(agent, "isZEROIteration", true);

        } catch (Exception e)
        {
            logger.error("Could not init agent for problem " + problemId + ".", e);
            return null;
        }
        return agent;
    }
}
