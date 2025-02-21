package org.brapi.schematools.analyse;

import org.brapi.schematools.analyse.oauth.SingleSignOn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;

import static org.brapi.schematools.analyse.TestUtils.getResourceAsString;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class OpenAPISpecificationAnalyserTest {

    @Mock
    private HttpClient client;
    @Mock
    private SingleSignOn singleSignOn;
    private OpenAPISpecificationAnalyser subject;

    @BeforeEach
    void setUp() {
        subject = new OpenAPISpecificationAnalyser("https://brapi.org/brapi/v2", client, singleSignOn) ;
    }

    @Test
    void analyse() {
        try {
            subject.analyse(getResourceAsString("brapi_openapi.json")) ;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage()) ;
        }
    }
}