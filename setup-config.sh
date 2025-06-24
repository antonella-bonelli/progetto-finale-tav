#!/bin/bash

# Setup configurazione IDS per macOS

echo "=== Setup IDS Configuration ==="

# Copia file properties dal template
if [ -f "simulator/src/main/resources/simulator.properties.template" ]; then
    cp "simulator/src/main/resources/simulator.properties.template" "simulator/src/main/resources/simulator.properties"
    echo "✓ simulator.properties creato"
fi

if [ -f "simulator/src/main/resources/logback-spring.xml.template" ]; then
    cp "simulator/src/main/resources/logback-spring.xml.template" "simulator/src/main/resources/logback-spring.xml"
    echo "✓ logback-spring.xml creato"
fi

if [ -f "ids/src/main/resources/ids.properties.template" ]; then
    cp "ids/src/main/resources/ids.properties.template" "ids/src/main/resources/ids.properties"
    echo "✓ ids.properties creato"
fi

echo "✓ Setup completato!"