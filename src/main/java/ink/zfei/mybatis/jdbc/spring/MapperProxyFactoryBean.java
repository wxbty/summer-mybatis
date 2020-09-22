package ink.zfei.mybatis.jdbc.spring;


import ink.zfei.mybatis.jdbc.MybtisDataSource;
import ink.zfei.summer.beans.factory.FactoryBean;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class MapperProxyFactoryBean implements FactoryBean {

    private String originBeanClassName;

    public MapperProxyFactoryBean(String originBeanClassName) {
        this.originBeanClassName = originBeanClassName;
    }

    public Object getObject() {
        InvocationHandler handler = new MapperCreatorInvocationHandler(MybtisDataSource.get());
        //扫描basepackages，发现有@Mapper注解类，就把这个类信息缓存，这边取出来，for循环，在这里分别生成代理类

        try {
            Class clazz = Class.forName(originBeanClassName);
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, handler);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Class<?> getObjectType() {
        return null;
    }
}
