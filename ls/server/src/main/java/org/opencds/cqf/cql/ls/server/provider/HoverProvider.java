package org.opencds.cqf.cql.ls.server.provider;

import java.net.URI;
import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.hl7.cql.model.DataType;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library.Statements;
import org.opencds.cqf.cql.ls.core.utility.Uris;
import org.opencds.cqf.cql.ls.server.manager.CqlTranslationManager;

public class HoverProvider {
    private CqlTranslationManager cqlTranslationManager;

    public HoverProvider(CqlTranslationManager cqlTranslationManager) {
        this.cqlTranslationManager = cqlTranslationManager;
    }

    public Hover hover(HoverParams position) {
        URI uri = Uris.parseOrNull(position.getTextDocument().getUri());
        if (uri == null) {
            return null;
        }

        // This translates on the fly. We may want to consider maintaining
        // an ELM index to reduce the need to do retranslation.
        CqlTranslator translator = this.cqlTranslationManager.translate(uri);
        if (translator == null) {
            return null;
        }

        // The ExpressionTrackBackVisitor is supposed to replace this eventually.
        // Basically, for any given position in the text document there's a graph of nodes
        // that represent the parents nodes for that position. For example:
        //
        // define: "EncounterExists":
        // exists([Encounter])
        //
        // ExpressionDef -> Expression -> Exists -> Retrieve
        //
        // For that given position, we want to select the most specific node we support generating
        // hover information for and return that.
        //
        // (maybe.. the alternative is to select the specific node under the cursor, but that may be
        // less user friendly)
        //
        // The current code always picks the first ExpressionDef in the graph.
        Pair<Range, ExpressionDef> exp = getExpressionDefForPosition(position.getPosition(),
                translator.getTranslatedLibrary().getLibrary().getStatements());

        if (exp == null) {
            return null;
        }

        MarkupContent markup = markup(exp.getRight());
        if (markup == null) {
            return null;
        }

        return new Hover(markup, exp.getLeft());
    }

    private Pair<Range, ExpressionDef> getExpressionDefForPosition(Position position,
            Statements statements) {
        if (statements == null || statements.getDef() == null || statements.getDef().isEmpty()) {
            return null;
        }

        for (ExpressionDef def : statements.getDef()) {
            if (def.getTrackbacks() == null || def.getTrackbacks().isEmpty()) {
                continue;
            }

            for (TrackBack tb : def.getTrackbacks()) {
                if (positionInTrackBack(position, tb)) {
                    Range range =
                            new Range(new Position(tb.getStartLine() - 1, tb.getStartChar() - 1),
                                    new Position(tb.getEndLine() - 1, tb.getEndChar()));
                    return Pair.of(range, def);
                }
            }
        }

        return null;
    }

    private boolean positionInTrackBack(Position p, TrackBack tb) {
        int startLine = tb.getStartLine() - 1;
        int endLine = tb.getEndLine() - 1;

        // Just kidding. We need intervals.
        if (p.getLine() >= startLine && p.getLine() <= endLine) {
            return true;
        } else {
            return false;
        }
    }

    public MarkupContent markup(ExpressionDef def) {
        if (def == null || def.getExpression() == null) {
            return null;
        }

        DataType resultType = def.getExpression().getResultType();
        if (resultType == null) {
            return null;
        }

        // Specifying the Markdown type as cql allows the client to apply
        // cql syntax highlighting the resulting pop-up
        String result = String.join("\n", "```cql", resultType.toString(), "```");

        return new MarkupContent("markdown", result);
    }
}
