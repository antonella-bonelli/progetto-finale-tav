#!/bin/bash

# =================================================================
# setup-config.sh - Script di setup per progetto IDS multi-modulo
# Da posizionare nella ROOT del progetto
# =================================================================

set -e  # Exit on any error

echo "=============================================="
echo "   IDS Project Configuration Setup"
echo "=============================================="
echo ""

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funzione per stampare messaggi colorati
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Verifica di essere nella root del progetto
if [ ! -f "settings.gradle.kts" ] && [ ! -f "build.gradle" ]; then
    print_error "Questo script deve essere eseguito dalla root del progetto (dove si trova settings.gradle)"
    exit 1
fi

print_info "Rilevata root del progetto: $(pwd)"
echo ""

# =================================================================
# SETUP MODULO SIMULATOR
# =================================================================
echo "=== Setup Modulo Simulator ==="

SIMULATOR_DIR="simulator"
SIMULATOR_RESOURCES="$SIMULATOR_DIR/src/main/java/resources"


# Setup configurazione simulator
if [ ! -f "$SIMULATOR_RESOURCES/simulator.properties" ]; then
    if [ ! -f "$SIMULATOR_RESOURCES/simulator.properties.template" ]; then
        print_info "Creazione simulator.properties dal template..."
        cp "$SIMULATOR_RESOURCES/simulator.properties.template" "$SIMULATOR_RESOURCES/simulator.properties"
        print_status "simulator.properties creato"
    fi
else
    print_status "simulator.properties già esistente"
fi

# Setup logback
if [ ! -f "$SIMULATOR_RESOURCES/logback-spring.xml" ]; then
    if [ -f "$SIMULATOR_RESOURCES/logback-spring.xml.template" ]; then
        print_info "Creazione logback-spring.xml dal template..."
        cp "$SIMULATOR_RESOURCES/logback-spring.xml.template" "$SIMULATOR_RESOURCES/logback-spring.xml"
        print_status "logback-spring.xml creato"
    else
        print_warning "Template logback-spring.xml.template non trovato. Saltato."
    fi
else
    print_status "logback-spring.xml già esistente"
fi