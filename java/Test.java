package com.donkey.msb.class17;

import javax.tools.*;
import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// https://stackoverflow.com/questions/4463440/compile-java-source-code-from-a-string
public class Test {
    public static void main(String[] args) throws Exception {
        String className = "com.example.MyClass"; // 新类的类名
        String javaCode = "import com.example.OtherClass; " +
                "public class MyClass { " +
                "    public void sayHello() { " +
                "        OtherClass other = new OtherClass(); " +
                "        other.sayHello(); " +
                "    } " +
                "}"; // 字符串表示的Java代码

        List<String> classpathEntries = new ArrayList<>();
        classpathEntries.add("/path/to/other.jar"); // 将其他jar包的路径添加到类路径中

        Class<?> clazz = compileAndLoadClass(className, javaCode, classpathEntries);
        if (clazz != null) {
            // 实例化对象并调用方法
            Object obj = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getMethod("sayHello");
            method.invoke(obj);
        }
    }

    private static Class<?> compileAndLoadClass(String className, String javaCode, List<String> classpathEntries) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StringWriter writer = new StringWriter();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(new JavaSourceFromString(className, javaCode));

        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(buildClasspath(classpathEntries));

        JavaCompiler.CompilationTask task = compiler.getTask(writer, fileManager, diagnostics, options, null, compilationUnits);
        boolean success = task.call();

        if (!success) {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.out.println(diagnostic.getMessage(null));
            }
            return null;
        }

        URL[] classpathUrls = buildClasspathUrls(classpathEntries);
        URLClassLoader classLoader = new URLClassLoader(classpathUrls);

        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static String buildClasspath(List<String> classpathEntries) {
        StringBuilder classpath = new StringBuilder();
        String separator = System.getProperty("path.separator");

        for (String entry : classpathEntries) {
            classpath.append(entry).append(separator);
        }

        return classpath.toString();
    }

    private static URL[] buildClasspathUrls(List<String> classpathEntries) throws Exception {
        List<URL> urls = new ArrayList<>();
        for (String entry : classpathEntries) {
            urls.add(new File(entry).toURI().toURL());
        }
        return urls.toArray(new URL[0]);
    }

    private static class JavaSourceFromString extends SimpleJavaFileObject {
        private final String code;

        JavaSourceFromString(String name, String code) {
            super(toUri(name), Kind.SOURCE);
            this.code = code;
        }

        private static URI toUri(String name) {
            try {
                return new URI(name);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
