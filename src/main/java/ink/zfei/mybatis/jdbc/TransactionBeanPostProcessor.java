package ink.zfei.mybatis.jdbc;

import ink.zfei.summer.beans.BeanPostProcessor;
import ink.zfei.summer.transaction.annotation.Transactional;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionBeanPostProcessor implements BeanPostProcessor {

    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if (isTransactionalBean(bean)) {
            return wrapIfProxy(bean);
        }

        return bean;
    }

    private boolean isTransactionalBean(Object bean) {
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(Transactional.class) != null) {
                return true;
            }
        }
        return false;
    }

    private Object wrapIfProxy(Object bean) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bean.getClass());
        enhancer.setCallback(new TransactionManagerInterceptor(bean));
        return enhancer.create();
    }

    class TransactionManagerInterceptor implements MethodInterceptor {

        private Object target;

        public TransactionManagerInterceptor(Object target) {
            this.target = target;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws InvocationTargetException, IllegalAccessException, SQLException {

            if (method.getAnnotation(Transactional.class) == null) {
                return method.invoke(target, args);

            }

            TransactionContext.setFlag(true);


            Object object = null;
            Connection conn = null;
            try {
                object = method.invoke(target, args);
                conn = TransactionContext.getConnection();
                if (conn != null) {
                    conn.commit();
                }
            } catch (RuntimeException e) {
//                e.printStackTrace();
                conn = TransactionContext.getConnection();
                if (conn == null) {
                    throw e;
                }
                conn.rollback();
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }


            return object;
        }
    }

}
