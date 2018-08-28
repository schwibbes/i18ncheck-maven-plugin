package com.github.schwibbes;

import java.util.*;
import java.util.stream.*;
import static java.util.stream.Collectors.*;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.*;


@Mojo( name = "i18nCheck", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class I18nChecker extends AbstractMojo {

    @Parameter(
        defaultValue = "${project.basedir}/src/main/resources/i18n/messages.properties",
        property = "baseFilePath",
        required = true )
    private String baseFilePath;

    @Parameter(
        defaultValue = "${project.build.directory}/i18n.report",
        property = "reportFile",
        required = false )
    private File reportFile;

    public void execute() throws MojoExecutionException {
        getLog().debug(baseFilePath);


        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
            Paths.get(baseFilePath).getParent(), withoutExtension("" + Paths.get(baseFilePath).getFileName())  + "_*" ))
        {
            Set<String> keysInBaseFile = collectKeys(baseFilePath);
            dirStream.forEach( file -> processFile(file, keysInBaseFile));
        } catch (Exception e) {
            throw new MojoExecutionException("could not read file", e);
        }
    }

    private void processFile(Path file, Set<String> keysInBaseFile) {
        try {
            getLog().debug("processing: " + file);
            Set<String> remaining = symmetricDifference(keysInBaseFile, collectKeys(file.toString()));
            getLog().debug("keys: " + remaining);
            if (!remaining.isEmpty()) {
                String line = file + " -> " + remaining.toString();
                Files.write(reportFile.toPath(), line.getBytes(), StandardOpenOption.CREATE);
            }
        } catch (Exception e) {
            throw new RuntimeException("exception while processing " + file, e);
        }
    }

    private String withoutExtension(String filename) throws MojoExecutionException {
        int pos = filename.lastIndexOf(".");
        if (pos < 1)
            throw new MojoExecutionException("baseFilePath does contain no extension: " + filename);
        String result = filename.substring(0, pos);
        if (result.length() < 1)
            throw new MojoExecutionException("baseFilePath does contain no extension: " + filename);
        getLog().debug("reduce filename <" + filename + "> to <" + result + ">");
        return result;
    }

    private Set<String> collectKeys(String filename)
        throws MojoExecutionException
    {
        try(InputStream in = new FileInputStream(filename)) {
          Properties props = new Properties();
          props.load(in);
          Set<String> result = props.keySet().stream()
            .map(String::valueOf)
            .filter(key -> !Objects.equals("", props.getProperty(key, "")))
            .collect(toSet());
          getLog().debug(filename + " contains keys: " + result);
          return result;
      } catch (IOException e) {
        throw new MojoExecutionException("baseFilePath does not exist: ", e);
    }
}

private static <T> Set<T> symmetricDifference(Set<T> a, Set<T> b) {
    Stream<T> left = a.stream().filter(x -> !b.contains(x));
    Stream<T> right = b.stream().filter(x -> !a.contains(x));
    return Stream.concat(left, right).collect(toSet());
}


}
