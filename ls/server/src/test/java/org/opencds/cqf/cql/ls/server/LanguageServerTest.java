package org.opencds.cqf.cql.ls.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;
import org.mockito.Mockito;
import org.opencds.cqf.cql.ls.server.config.ServerConfig;
import org.opencds.cqf.cql.ls.server.config.TestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

// Just sketching out some tests here to get an idea of the scaffolding we need to make the language
// server easily testable
// We'll need to split out a few components to make it easier.
@SpringBootTest(classes = {ServerConfig.class, TestConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true"})
public class LanguageServerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    CqlLanguageServer server;

    LanguageClient client = Mockito.mock(LanguageClient.class);

    @BeforeClass
    public void beforeClass() throws InterruptedException, ExecutionException {
        server.connect(client);
        InitializeResult initializeResult = server.initialize(new InitializeParams()).get();
        assertNotNull(initializeResult);

        server.initialized(new InitializedParams());

    }

    @AfterClass
    public void afterClass() throws InterruptedException, ExecutionException, TimeoutException {
        server.shutdown().get(100, TimeUnit.MILLISECONDS);
        server.exit();
        server.exited().get(100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void handshake() throws Exception {
        // https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#lifeCycleMessages

        // Sequence for initialization
        // initialize
        // initialized
        // ...
        // do language server stuff
        // ...
        // shutdown
        // exit
    }

    @Test
    public void hoverInt() throws Exception {
        Hover hover = server.getTextDocumentService()
                .hover(new HoverParams(
                        new TextDocumentIdentifier("file:/org/opencds/cqf/cql/ls/server/Two.cql"),
                        new Position(5, 2)))
                .get();

        assertNotNull(hover);
        assertNotNull(hover.getContents().getRight());

        MarkupContent markup = hover.getContents().getRight();
        assertEquals(markup.getKind(), "markdown");
        assertEquals(markup.getValue(), "```System.Integer```");
    }

    @Test
    public void hoverNothing() throws Exception {
        Hover hover = server.getTextDocumentService()
                .hover(new HoverParams(
                        new TextDocumentIdentifier("file:/org/opencds/cqf/cql/ls/server/Two.cql"),
                        new Position(2, 0)))
                .get();

        assertNull(hover);
    }

    @Test
    public void hoverList() throws Exception {
        Hover hover = server.getTextDocumentService()
                .hover(new HoverParams(
                        new TextDocumentIdentifier("file:/org/opencds/cqf/cql/ls/server/Two.cql"),
                        new Position(8, 2)))
                .get();

        assertNotNull(hover);
        assertNotNull(hover.getContents().getRight());

        MarkupContent markup = hover.getContents().getRight();
        assertEquals(markup.getKind(), "markdown");
        assertEquals(markup.getValue(), "```list<System.Integer>```");
    }
}
