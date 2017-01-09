package io.ebean.typequery.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Perform query bean enhancement on classes in src/main.
 */
@Mojo(name = "testEnhance", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MavenTestEnhanceTask extends BaseEnhanceTask {

  @Parameter(property = "project.build.testOutputDirectory")
  String testClassSource;

  public void execute() throws MojoExecutionException {
    executeFor(testClassSource);
  }

}