package it.unibas.ids.analyzer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EAnalysisType {
    RULE_BASED("Rule-Based Analysis", "Analisi basata su regole predefinite"),
    SIMPLE("Simplified Analysis", "Analisi semplificata su tipo e livello");

    private final String displayName;
    private final String description;
}
