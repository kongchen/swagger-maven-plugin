package com.github.kongchen.swagger.docgen;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class AbstractDocumentSourceTest {

  // Omit the '*.json' extension, so that swagger-ui can show it correctly.

  public static final String OUTPUT_PATH = "foo/bar";
  public static final String FLAT_OUTPUT_PATH = "foo_bar";

  private AbstractDocumentSource getAbstractDocumentSource(final boolean useOutputFlatStructure) {
    return new AbstractDocumentSource(new LogAdapter((Logger) null), null, null, null, null,
        useOutputFlatStructure) {
      @Override
      public void loadDocuments() throws Exception, GenerateException {

      }
    };
  }

  @Test
  public void testCreateFile() throws Exception {
    AbstractDocumentSource abstractDocumentSource = getAbstractDocumentSource(true);

    File target = abstractDocumentSource.createFile(new File("target"), OUTPUT_PATH);
    assertTrue(target.getPath(), FilenameUtils.equalsNormalized("target/foo/bar", target.getPath()));

    target = abstractDocumentSource.createFile(new File("target"), FLAT_OUTPUT_PATH);
    assertTrue(target.getPath(), FilenameUtils.equalsNormalized("target/foo_bar", target.getPath()));
  }

  @Test
  public void testResourcePathToFilename() throws Exception {
    AbstractDocumentSource abstractDocumentSource = getAbstractDocumentSource(true);
    assertEquals(FLAT_OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("/foo/bar"));
    assertEquals(FLAT_OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("/foo/bar/"));
    assertEquals(FLAT_OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("foo/bar"));
    assertEquals(FLAT_OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("foo/bar/"));
    assertEquals("bar", abstractDocumentSource.resourcePathToFilename("bar"));

    abstractDocumentSource = getAbstractDocumentSource(false);
    assertEquals(OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("/foo/bar"));
    assertEquals(OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("/foo/bar/"));
    assertEquals(OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("foo/bar"));
    assertEquals(OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("foo/bar/"));
    assertEquals("bar", abstractDocumentSource.resourcePathToFilename("bar"));
  }
}
