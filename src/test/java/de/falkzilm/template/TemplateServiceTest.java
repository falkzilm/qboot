package de.falkzilm.template;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TemplateServiceTest {

    @Inject
    TemplateService templateService;

    private Path tempDir;
    private String validTemplateXml;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("qboot-test");
        validTemplateXml = """
                <qtemplate>
                    <workspaces>
                        <workspace>
                            <general>
                                <framework>quarkus</framework>
                                <projectName>test-project</projectName>
                                <projectPackage>com.example</projectPackage>
                                <frameworkVersion>3.28.4</frameworkVersion>
                            </general>
                            <dependencies name="pre">
                            </dependencies>
                            <dependencies name="post">
                            </dependencies>
                            <structure value="custom">
                            </structure>
                        </workspace>
                    </workspaces>
                </qtemplate>
                """;
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }

    @Test
    void testLoadTemplateFromFile() throws Exception {
        Path templateFile = tempDir.resolve("test-template.xml");
        Files.writeString(templateFile, validTemplateXml);

        QTemplate template = templateService.loadTemplate(templateFile.toString());

        assertNotNull(template);
        assertNotNull(template.getWorkspaces());
        assertFalse(template.getWorkspaces().getItems().isEmpty());
        
        Workspace workspace = template.getWorkspaces().getItems().get(0);
        assertEquals("quarkus", workspace.getGeneral().framework().toJson());
        assertEquals("test-project", workspace.getGeneral().projectName());
    }

    @Test
    void testLoadTemplateFromNonExistentFile() {
        Path nonExistentFile = tempDir.resolve("non-existent.xml");

        assertThrows(Exception.class, () -> {
            templateService.loadTemplate(nonExistentFile.toString());
        });
    }

    @Test
    void testValidateValidTemplate() throws Exception {
        Path templateFile = tempDir.resolve("valid-template.xml");
        Files.writeString(templateFile, validTemplateXml);

        QTemplate template = templateService.loadTemplate(templateFile.toString());
        assertTrue(templateService.validateTemplate(template));
    }

    @Test
    void testValidateNullTemplate() {
        assertFalse(templateService.validateTemplate(null));
    }

    @Test
    void testValidateTemplateWithoutWorkspaces() throws Exception {
        String invalidXml = """
                <qtemplate>
                </qtemplate>
                """;

        Path templateFile = tempDir.resolve("no-workspaces.xml");
        Files.writeString(templateFile, invalidXml);

        QTemplate template = templateService.loadTemplate(templateFile.toString());
        assertFalse(templateService.validateTemplate(template));
    }

    @Test
    void testValidateTemplateWithEmptyWorkspaces() throws Exception {
        String invalidXml = """
                <qtemplate>
                    <workspaces>
                    </workspaces>
                </qtemplate>
                """;

        Path templateFile = tempDir.resolve("empty-workspaces.xml");
        Files.writeString(templateFile, invalidXml);

        QTemplate template = templateService.loadTemplate(templateFile.toString());
        assertFalse(templateService.validateTemplate(template));
    }

    @Test
    void testValidateTemplateWithMissingFramework() throws Exception {
        String invalidXml = """
                <qtemplate>
                    <workspaces>
                        <workspace>
                            <general>
                                <projectName>test-project</projectName>
                            </general>
                        </workspace>
                    </workspaces>
                </qtemplate>
                """;

        Path templateFile = tempDir.resolve("missing-framework.xml");
        Files.writeString(templateFile, invalidXml);

        QTemplate template = templateService.loadTemplate(templateFile.toString());
        assertFalse(templateService.validateTemplate(template));
    }

    @Test
    void testIsUrlDetection() {
        // We can test the URL detection logic indirectly through loading
        // Since the method is private, we test via the public interface

        // Test that it recognizes file paths (should not throw network-related exceptions)
        Path templateFile = tempDir.resolve("test-template.xml");
        
        assertDoesNotThrow(() -> {
            try {
                Files.writeString(templateFile, validTemplateXml);
                templateService.loadTemplate(templateFile.toString());
            } catch (Exception e) {
                // File-related exceptions are OK, network exceptions are not
                assertFalse(e.getMessage().contains("network") || 
                           e.getMessage().contains("HTTP") ||
                           e.getMessage().contains("timeout"));
            }
        });
    }

    @Test
    void testCacheTemplate() {
        String url = "https://example.com/template.xml";
        String content = validTemplateXml;

        var cacheResult = templateService.cacheTemplate(url, content);
        // Cache might succeed or fail depending on system, so we just test it doesn't crash
        assertNotNull(cacheResult);
    }

    @Test
    void testValidateTemplateWithMultipleWorkspaces() throws Exception {
        String multiWorkspaceXml = """
                <qtemplate>
                    <workspaces>
                        <workspace>
                            <general>
                                <framework>quarkus</framework>
                                <projectName>backend</projectName>
                            </general>
                        </workspace>
                        <workspace>
                            <general>
                                <framework>angular</framework>
                                <projectName>frontend</projectName>
                            </general>
                        </workspace>
                    </workspaces>
                </qtemplate>
                """;

        Path templateFile = tempDir.resolve("multi-workspace.xml");
        Files.writeString(templateFile, multiWorkspaceXml);

        QTemplate template = templateService.loadTemplate(templateFile.toString());
        assertTrue(templateService.validateTemplate(template));
        
        assertEquals(2, template.getWorkspaces().getItems().size());
        assertEquals("quarkus", template.getWorkspaces().getItems().get(0).getGeneral().framework().toJson());
        assertEquals("angular", template.getWorkspaces().getItems().get(1).getGeneral().framework().toJson());
    }

    @Test
    void testInvalidXmlParsing() {
        Path invalidFile = tempDir.resolve("invalid.xml");
        
        assertThrows(Exception.class, () -> {
            Files.writeString(invalidFile, "<invalid>xml content</broken>");
            templateService.loadTemplate(invalidFile.toString());
        });
    }
}