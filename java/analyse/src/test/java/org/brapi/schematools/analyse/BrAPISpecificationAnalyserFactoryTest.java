package org.brapi.schematools.analyse;

import org.brapi.schematools.analyse.authorization.NoAuthorizationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

@ExtendWith(MockitoExtension.class)
class BrAPISpecificationAnalyserFactoryTest {

    @Mock
    private HttpClient client;
    @Mock
    private NoAuthorizationProvider authorizationProvider;
    @Mock
    private HttpResponse<String> response ;
    private BrAPISpecificationAnalyserFactory subject;

    @BeforeEach
    void setUp() {
        subject = new BrAPISpecificationAnalyserFactory("https://brapi.org/brapi/v2", client, authorizationProvider) ;
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