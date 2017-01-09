package io.ebean.typequery.maven;

import io.ebean.typequery.agent.QueryBeanTransformer;
import io.ebean.typequery.agent.offline.OfflineFileTransform;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

abstract class BaseEnhanceTask extends AbstractMojo {

  /**
   * The class path used to read related classes.
   */
  @Parameter(property = "project.compileClasspathElements", required = true, readonly = true)
  List<String> compileClasspathElements;

  /**
   * Set the arguments passed to the transformer.
   */
  @Parameter
  String transformArgs;

  /**
   * Set the package name to search for classes to transform.
   * <p>
   * If the package name ends in "/**" then this recursively transforms all sub
   * packages as well.
   */
  @Parameter
  String packages;

  public abstract void execute() throws MojoExecutionException;

  protected void executeFor(String classSource) throws MojoExecutionException {

    Log log = getLog();

    ClassLoader classLoader = buildClassLoader(classSource);
    QueryBeanTransformer transformer = new QueryBeanTransformer(transformArgs, classLoader, null);

    log.info("classSource=" + classSource + "  transformArgs=" + transformArgs + " packages=" + packages + " classPathSize:" + compileClasspathElements.size());

    OfflineFileTransform ft = new OfflineFileTransform(transformer, classLoader, classSource);
    try {
      ft.process(packages);

    } catch (Throwable e) {
      throw new MojoExecutionException("Error trying to transform classes", e);
    }
  }

  /**
   * Return the ClassLoader used during the enhancement.
   */
  private ClassLoader buildClassLoader(String classSource) {

    URL[] urls = buildClassPath(classSource);
    return URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Return the class path using project compileClasspathElements.
   */
  private URL[] buildClassPath(String classSource) {

    try {
      List<URL> urls = new ArrayList<>(compileClasspathElements.size() + 1);

      Log log = getLog();

      for (String element : compileClasspathElements) {
        if (log.isDebugEnabled()) {
          log.debug("ClasspathElement: " + element);
        }
        urls.add(new File(element).toURI().toURL());
      }
      // also add classSource to classPath
      URL classSourceUrl = new File(classSource).toURI().toURL();
      if (!urls.contains(classSourceUrl)) {
        urls.add(classSourceUrl);
      }
      return urls.toArray(new URL[urls.size()]);

    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}