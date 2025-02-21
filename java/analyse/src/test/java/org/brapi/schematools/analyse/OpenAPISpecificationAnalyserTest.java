package org.brapi.schematools.analyse;

import io.swagger.io.NoAuthentication;
import org.brapi.schematools.analyse.authorization.NoAuthorizationProvider;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import static org.brapi.schematools.analyse.TestUtils.getResourceAsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAPISpecificationAnalyserTest {

    @Mock
    private HttpClient client;
    @Mock
    private NoAuthorizationProvider authorizationProvider;
    @Mock
    private HttpResponse<String> response ;
    private OpenAPISpecificationAnalyser subject;

    @BeforeEach
    void setUp() {
        subject = new OpenAPISpecificationAnalyser("https://brapi.org/brapi/v2", client, authorizationProvider) ;
    }

    /*@Test
    void analyse() {        
        
        try {
            //Given
            when(client.send(any(), any())).thenReturn(response) ;

            //When
            Response<List<AnalysisReport>> report = subject.analyse(getResourceAsString("brapi_openapi.json"));

            //Then
            assertTrue(report::hasErrors);

        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }*/
}