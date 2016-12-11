package io.ebean.typequery.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import io.ebean.typequery.agent.QueryBeanTransformer;
import io.ebean.typequery.agent.offline.OfflineFileTransform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * A Maven Plugin that can enhance beans adding timed metric collection.
 * <p>
 * You can use this plugin as part of your build process to enhance beans
 * etc.
 * <p>
 * The parameters are:
 * <ul>
 * <li><b>classDestination</b> This is the root directory where the .class files
 * are written to. If this is left out then this defaults to the
 * <b>classSource</b>.</li>
 * <li><b>packages</b> A comma delimited list of packages that is searched for
 * classes that need to be enhanced. If the package ends with ** or * then all
 * subpackages are also searched.</li>
 * <li><b>transformArgs</b> Arguments passed to the transformer. Typically a
 * debug level in the form of debug=1 etc.</li>
 * </ul>
 */
@Mojo(name = "enhance", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MavenEnhanceTask extends AbstractMojo {

  /**
   * The class path used to read related classes.
   */
  @Parameter(property = "project.compileClasspathElements", required = true, readonly = true)
  List<String> compileClasspathElements;

  /**
   * The directory holding the class files we want to transform.
   */
  @Parameter(property = "project.build.outputDirectory")
  String classSource;

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


  public void execute() throws MojoExecutionException {

    final Log log = getLog();
    if (classSource == null) {
      classSource = "target/classes";
    }

    ClassLoader classLoader = buildClassLoader();
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
  private ClassLoader buildClassLoader() {

    URL[] urls = buildClassPath();

    return URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Return the class path using project compileClasspathElements.
   */
  private URL[] buildClassPath() {

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