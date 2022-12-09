package org.opencds.cqf.cql.ls.server.service;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.cql.ls.core.utility.Uris;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FileContentServiceTest {

    @Test
    public void shouldFindParent() {

        URI uri = Uris.parseOrNull("/Users/craigsenn/dev/ncqa/Ncqa.IMAS.DigitalMeasures/cql/ADDE_HEDIS_MY2024-3.0.0.cql");

//        URI out = FileContentService.findCqlFolderFromRoot(uri);

        assertNotNull(uri);
    }
}
