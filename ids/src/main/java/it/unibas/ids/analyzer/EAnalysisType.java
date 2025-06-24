package it.unibas.ids.analyzer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EAnalysisType {
    ADVANCED("Advanced Analysis", "Analisi avanzata"),
    SIMPLE("Simplified Analysis", "Analisi semplificata");

    private final String displayName;
    private final String description;
}
