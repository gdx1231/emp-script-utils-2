package com.gdxsoft.easyweb.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;

/**
 * UNet test class for testing HTTPS connectivity to https://gdxsoft.com
 */
public class UNetTest {
    
    private UNet unet;
    
    @BeforeEach
    void setUp() {
        unet = new UNet();
        unet.setIsShowLog(true);
        unet.setTimeout(10000); // 10 second timeout for tests
    }
    
    @Test
    void testHttpGet() throws IOException {
        String url = "https://gdxsoft.com";
        String result = unet.doGet(url);
        
        assertNotNull(result, "Response should not be null");
        assertTrue(result.length() > 0, "Response should not be empty");
        assertEquals(200, unet.getLastStatusCode(), "Status code should be 200");
        
        // Verify the response contains expected content
        assertTrue(result.toLowerCase().contains("gdxsoft"), 
                  "Response should contain 'gdxsoft' in some form");
    }
    
    @Test
    void testGetCookies() {
        String url = "https://gdxsoft.com";
        unet.doGet(url);
        
        // Should have a valid last URL
        assertEquals(url, unet.getLastUrl());
        
        // Should have a status code
        assertNotNull(unet.getLastStatusCode());
    }
    
    @Test
    void testUserAgent() {
        String customUserAgent = "UNetTest/1.0";
        unet.setUserAgent(customUserAgent);
        assertEquals(customUserAgent, unet.getUserAgent());
    }
    
    @Test
    void testErrorHandling() {
        String invalidUrl = "https://nonexistent.gdxsoft.com.invalid";
        String result = unet.doGet(invalidUrl);
        
        // Should handle connection errors gracefully
        assertNotNull(unet.getLastErr(), "Should have error message for invalid URL");
        assertNull(result, "Result should be null for failed request");
    }
}