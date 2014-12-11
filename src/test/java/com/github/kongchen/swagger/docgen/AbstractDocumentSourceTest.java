package com.github.kongchen.swagger.docgen;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class AbstractDocumentSourceTest {

    public static final String OUTPUT_PATH = "foo/bar.json";
    public static final String FLAT_OUTPUT_PATH = "foo_bar.json";

    @Test
    public void testResourcePathToFilename() throws Exception {
        AbstractDocumentSource abstractDocumentSource = getAbstractDocumentSource(true);
        assertEquals(FLAT_OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("/foo/bar"));
        assertEquals(FLAT_OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("/foo/bar/"));
        assertEquals(FLAT_OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("foo/bar"));
        assertEquals(FLAT_OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("foo/bar/"));
        assertEquals("bar.json", abstractDocumentSource.resourcePathToFilename("bar"));

        abstractDocumentSource = getAbstractDocumentSource(false);
        assertEquals(OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("/foo/bar"));
        assertEquals(OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("/foo/bar/"));
        assertEquals(OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("foo/bar"));
        assertEquals(OUTPUT_PATH, abstractDocumentSource.resourcePathToFilename("foo/bar/"));
        assertEquals("bar.json", abstractDocumentSource.resourcePathToFilename("bar"));
    }

    @Test
    public void testCreateFile() throws Exception {
        AbstractDocumentSource abstractDocumentSource = getAbstractDocumentSource(true);

        File target = abstractDocumentSource.createFile(new File("target"), OUTPUT_PATH);
        assertTrue(target.getPath(), FilenameUtils.equalsNormalized("target/foo/bar.json", target.getPath()));

        target = abstractDocumentSource.createFile(new File("target"), FLAT_OUTPUT_PATH);
        assertTrue(target.getPath(), FilenameUtils.equalsNormalized("target/foo_bar.json", target.getPath()));
    }

    private AbstractDocumentSource getAbstractDocumentSource(final boolean useOutputFlatStructure) {
        return new AbstractDocumentSource(new LogAdapter((Logger) null), null, null, null, null, useOutputFlatStructure, null, false) {
            @Override
            public void loadDocuments() throws Exception, GenerateException {

            }
        };
    }
}
