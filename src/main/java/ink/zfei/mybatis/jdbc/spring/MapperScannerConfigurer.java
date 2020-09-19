package ink.zfei.mybatis.jdbc.spring;

import ink.zfei.mybatis.jdbc.annotations.Mapper;
import ink.zfei.summer.annation.Component;
import ink.zfei.summer.beans.BeanDefinitionRegistry;
import ink.zfei.summer.beans.BeanDefinitionRegistryPostProcessor;
import ink.zfei.summer.beans.factory.config.BeanDefinition;
import ink.zfei.summer.core.AbstractApplicationContext;
import ink.zfei.summer.core.GenericBeanDefinition;
import ink.zfei.summer.util.AnnationUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor {


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
        //通过 scan ，如果有@Maaper注解，就回把Mapper的bean定义拿出来
        try {
            Set<GenericBeanDefinition> set = scan("ink.zfei.demo.mybatis", beanDefinitionRegistry);
            processBeanDefinitions(set);


        } catch (IOException | URISyntaxException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void processBeanDefinitions(Set<GenericBeanDefinition> set) {

        set.forEach(genericBeanDefinition -> {
            genericBeanDefinition.setConstrucrorParm(genericBeanDefinition.getBeanClassName());
            genericBeanDefinition.setBeanClassName("ink.zfei.mybatis.jdbc.spring.MapperProxyFactoryBean");
        });


    }

    @Override
    public void postProcessBeanFactory(AbstractApplicationContext abstractApplicationContext) {

    }

    public Set<GenericBeanDefinition> scan(String basePackages, BeanDefinitionRegistry beanDefinitionRegistry) throws IOException, URISyntaxException, ClassNotFoundException {
        if (basePackages == null) {
            throw new RuntimeException("At least one base package must be specified");
        }
        String path = AnnationUtil.resolveBasePackage(basePackages);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        List<URL> list = new ArrayList<>();
        Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(path) : ClassLoader.getSystemResources(path));
        while (resourceUrls.hasMoreElements()) {
            URL url = resourceUrls.nextElement();
            list.add(url);
        }

        Set<GenericBeanDefinition> set = new HashSet<>();
        URL url = list.get(0);
        File dir = new File(AnnationUtil.toURI(url.toString()).getSchemeSpecificPart());
        for (File content : AnnationUtil.listDirectory(dir)) {

            String className = content.getAbsolutePath();
            className = className.replace(File.separatorChar, '.');
            className = className.substring(className.indexOf(basePackages));

            className = className.substring(0, className.length() - 6);
//            //将/替换成. 得到全路径类名


            //className = ink.zfei.annation.Component
            // 加载Class类
            Class<?> aClass = Class.forName(className);
            Mapper component = aClass.getDeclaredAnnotation(Mapper.class);
            if (component != null) {
                String beanName = aClass.getSimpleName();
                if (StringUtils.isNotBlank(beanName)) {
//                    beanName = aClass.getName();
                    beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
                }
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setId(beanName);
                beanDefinition.setBeanClassName(className);
                beanDefinitionRegistry.registerBeanDefinition(beanDefinition);
                set.add(beanDefinition);
            }

        }

        return set;
    }
}
