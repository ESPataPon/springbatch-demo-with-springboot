package batch.resources;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.support.ServletContextResource;
import sun.tools.java.ClassPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * @version :1.0.0
 * @author: lvJian
 * @time: 2018-05-28 22:57
 * @description :
 */
@SuppressWarnings("ALL")
public class ByteArrayResourceDemo {

    @Test
    public void testByteArrayResource() throws IOException {

        ByteArrayResource arrayResource = new ByteArrayResource("helloword".getBytes());
        if (arrayResource.exists()) {

            InputStream inputStream = arrayResource.getInputStream();


            byte[] bytes = new byte[inputStream.available()];

            inputStream.read(bytes);

            System.out.println(new String(bytes, "utf-8"));
        }
    }


    @Test
    public void testInputStreamResource() {
        ByteArrayInputStream InputStream = new ByteArrayInputStream("hello.word".getBytes());

        InputStreamResource inputStreamResource = new InputStreamResource(InputStream);

    }


    @Test
    public void testFileSystemResource() throws IOException {


        FileSystemResource systemResource = new FileSystemResource("./test.txt");


        if (systemResource.exists()) {
            InputStream inputStream = systemResource.getInputStream();
            byte[] bytes = new byte[inputStream.available()];

            inputStream.read(bytes);

            System.out.println(new String(bytes, "utf-8"));

            systemResource.isOpen();
        }

    }


    @Test
    public void testClasspathResource() throws IOException {

        ClassPathResource resource = new ClassPathResource("test1.properties");

        if (resource.exists()) {

            Properties properties = new Properties();
            properties.load(resource.getInputStream());

            properties.stringPropertyNames().forEach(System.out::println);

        }
    }


    @Test
    public void testClasspathResource2() throws IOException {

        ClassPathResource resource = new ClassPathResource("test1.properties", this.getClass().getClassLoader());
        if (resource.exists()) {

            Properties properties = new Properties();
            properties.load(resource.getInputStream());

            properties.stringPropertyNames().forEach(System.out::println);

        }

    }

    @Test
    public void testClasspathResource3() throws IOException {

        ClassPathResource resource = new ClassPathResource("test2.properties", this.getClass());

        if (resource.exists()) {

            Properties properties = new Properties();
            properties.load(resource.getInputStream());

            properties.stringPropertyNames().forEach(System.out::println);

        }

    }

    @Test
    public void testClassLoaderInputstream() throws IOException {

        InputStream inputStream = this.getClass().getResourceAsStream("test1.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);
//
//        properties.stringPropertyNames().forEach(System.out::println);


        InputStream resource = this.getClass().getClassLoader().getResourceAsStream("test1.properties");

        Properties properties = new Properties();
        properties.load(resource);

        properties.stringPropertyNames().forEach(System.out::println);
    }

    @Test
    public void testUrlResource() throws IOException {

        UrlResource urlResource = new UrlResource("file:/Users/patapon/IdeaProjects/springbatch-demo-with-springboot/target/classes/test1.properties");


        System.out.println(urlResource.isOpen());
        System.out.println(urlResource.exists());
        Properties properties = new Properties();
        properties.load(urlResource.getInputStream());

        properties.stringPropertyNames().forEach(System.out::println);
    }

    @Test
    public void testResourceLoader() throws IOException {

        ResourceLoader loader = new DefaultResourceLoader();

        Resource resource = loader.getResource("classpath:test1.properties");

        System.out.println(resource.exists());

        Resource resource1 = loader.getResource("test1.properties");


        InputStream inputStream = resource1.getInputStream();
        System.out.println(resource1.exists());

    }

    @Test
    public void testClassPathContextResourceLoader() {


    }

    @Test
    public void testAntPathMatcher() {

        AntPathMatcher matcher = new AntPathMatcher();

        System.out.println(matcher.match("cn/javass/config-**.xml", "cn/javass/config-dao.xml"));
    }

    @Test
    public void testClasspath() throws IOException {

        ClassPathResource resource = new ClassPathResource("org/springframework/batch/core/schema-mysql.sql");

//        System.out.println(resource.exists());


        InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/springframework/batch/core/schema-mysql.sql");

        Properties properties = new Properties();
        properties.load(is);
        properties.stringPropertyNames().forEach(System.out::println);

    }

    @Test
    public void testResourcePatternResolver() throws IOException {

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //只加载一个绝对匹配Resource，且通过ResourceLoader.getResource进行加载
        Resource[] resources = resolver.getResources("classpath:org/springframework/batch/core/schema-mysql.sql");
        Assert.assertTrue(resources.length == 1);
        System.out.println(resources[0].exists());

//        ClassPathResource resource = new ClassPathResource("org/springframework/batch/core/schema-mysql.sql");
//
//        System.out.println(resource.exists());
    }

    @Test
    public void testServletContextResource(){

    }
}
