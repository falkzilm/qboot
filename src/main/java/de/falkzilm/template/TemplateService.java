package de.falkzilm.template;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import de.falkzilm.helper.ConsoleFormatter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
public class TemplateService {

    @Inject
    XmlMapper xmlMapper;

    private volatile HttpClient httpClient;

    private HttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (this) {
                if (httpClient == null) {
                    httpClient = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(30))
                            .build();
                }
            }
        }
        return httpClient;
    }

    public QTemplate loadTemplate(String templateSource) throws IOException, InterruptedException {
        if (isUrl(templateSource)) {
            return loadFromUrl(templateSource);
        } else {
            return loadFromFile(templateSource);
        }
    }

    private boolean isUrl(String source) {
        return source.startsWith("http://") || source.startsWith("https://");
    }

    private QTemplate loadFromFile(String filePath) throws IOException {
        Path templatePath = Path.of(filePath);
        if (!Files.exists(templatePath)) {
            throw new IOException("Template file not found: " + filePath);
        }
        
        String xmlContent = Files.readString(templatePath);
        return xmlMapper.readValue(xmlContent, QTemplate.class);
    }

    private QTemplate loadFromUrl(String url) throws IOException, InterruptedException {
        ConsoleFormatter.bullet("Downloading template from: " + url);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", "qBoot-CLI/1.0")
                .header("Accept", "application/xml, text/xml")
                .GET()
                .build();

        HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download template. HTTP " + response.statusCode() + 
                                ": " + getStatusText(response.statusCode()));
        }

        String xmlContent = response.body();
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new IOException("Template content is empty");
        }

        ConsoleFormatter.success("Template downloaded successfully");
        return xmlMapper.readValue(xmlContent, QTemplate.class);
    }

    public Optional<Path> cacheTemplate(String url, String xmlContent) {
        try {
            // Create cache directory in system temp
            Path cacheDir = Path.of(System.getProperty("java.io.tmpdir"), "qboot-cache");
            Files.createDirectories(cacheDir);
            
            // Generate cache file name from URL
            String fileName = url.replaceAll("[^a-zA-Z0-9.-]", "_") + ".xml";
            Path cacheFile = cacheDir.resolve(fileName);
            
            Files.writeString(cacheFile, xmlContent);
            ConsoleFormatter.bullet("Template cached at: " + cacheFile);
            
            return Optional.of(cacheFile);
        } catch (IOException e) {
            ConsoleFormatter.debug("Failed to cache template", "Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    private String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 408 -> "Request Timeout";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "HTTP Error";
        };
    }

    public boolean validateTemplate(QTemplate template) {
        if (template == null) {
            return false;
        }
        
        if (template.getWorkspaces() == null) {
            ConsoleFormatter.debug("Template validation failed", "Missing workspaces element");
            return false;
        }
        
        if (template.getWorkspaces().getItems() == null || template.getWorkspaces().getItems().isEmpty()) {
            ConsoleFormatter.debug("Template validation failed", "No workspaces defined");
            return false;
        }
        
        // Validate each workspace
        for (int i = 0; i < template.getWorkspaces().getItems().size(); i++) {
            Workspace workspace = template.getWorkspaces().getItems().get(i);
            if (!validateWorkspace(workspace, i)) {
                return false;
            }
        }
        
        return true;
    }

    private boolean validateWorkspace(Workspace workspace, int index) {
        if (workspace.getGeneral() == null) {
            ConsoleFormatter.debug("Workspace validation failed", 
                "workspace[" + index + "]: Missing general configuration");
            return false;
        }
        
        if (workspace.getGeneral().framework() == null) {
            ConsoleFormatter.debug("Workspace validation failed", 
                "workspace[" + index + "]: Missing framework specification");
            return false;
        }
        
        return true;
    }
}