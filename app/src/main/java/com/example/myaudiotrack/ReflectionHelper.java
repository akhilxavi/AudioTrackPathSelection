package com.example.myaudiotrack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {
    public static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        return newInstance(className, new Class<?>[0], new Object[0]);
    }

    public static Object newInstance(String className, Class<?>[] parameterClasses, Object[] parameterValues) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException
    {
        Class<?> clz = Class.forName(className);
        Constructor<?> constructor = clz.getConstructor(parameterClasses);
        return constructor.newInstance(parameterValues);
    }

    public static Object getField(Object object, String fieldName) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        return getField(object, fieldName, Object.class);
    }

    public static <T> T getField(Object object, String fieldName, Class<T> type) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible( true );
        return type.cast(field.get(object));
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public static Object getEnumValue(String enumClassName, String enumValue) throws ClassNotFoundException
    {
        Class<Enum> enumClz = (Class<Enum>)Class.forName(enumClassName);
        return Enum.valueOf(enumClz, enumValue);
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public static void setEnumField( Object obj, String name, String value ) throws NoSuchFieldException,
            IllegalAccessException,
            IllegalArgumentException
    {
        Field f = obj.getClass().getField( name );
        f.set( obj, Enum.valueOf( (Class<Enum>) f.getType(), value ) );
    }

    public static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.set(object, value);
    }

    public static Object callMethod(Object object, String methodName) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        return callMethod(object, methodName, Object.class);
    }

    public static <T> T callMethod(Object object, String methodName, Class<T> returnType) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        return callMethod( object, methodName, new String[0], new Object[0], returnType );
    }

    public static Object callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        return callMethod(object, methodName, parameterTypes, parameterValues, Object.class );
    }

    public static Object
    callMethod( Object object, String methodName, Class[] parameterTypes, Object[] parameterValues )
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException
    {
        return callMethod( object, methodName, parameterTypes, parameterValues, Object.class );
    }

    public static <T> T callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues, Class<T> returnType) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
            parameterClasses[i] = Class.forName(parameterTypes[i]);

        return (T)callMethod( object, methodName, parameterClasses, parameterValues, Object.class );
    }

    public static < T > T callMethod(
            Object object,
            String methodName,
            Class[] parameterTypes,
            Object[] parameterValues,
            Class< T > returnType )
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        Class< ? > objClass = (Class< ? >)( ( object.getClass() == Class.class ) ? object : object.getClass() );
        Method method   = objClass.getDeclaredMethod( methodName, parameterTypes );
        return returnType.cast(method.invoke(object, parameterValues));
    }
}
