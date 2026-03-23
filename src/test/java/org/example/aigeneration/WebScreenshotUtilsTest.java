package org.example.aigeneration;

import org.example.aigeneration.utils.WebScreenshotUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        Assumptions.assumeTrue(hasChromeBinary(), "Chrome binary is required for screenshot test");
        String testUrl = "https://www.baidu.com";
        String webPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
        Assertions.assertNotNull(webPageScreenshot);
    }

    private boolean hasChromeBinary() {
        return Files.isExecutable(Path.of("/usr/bin/google-chrome"))
                || Files.isExecutable(Path.of("/usr/bin/google-chrome-stable"))
                || Files.isExecutable(Path.of("/usr/bin/chromium"))
                || Files.isExecutable(Path.of("/usr/bin/chromium-browser"));
    }
}
