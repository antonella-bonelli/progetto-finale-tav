#!/bin/bash
# start.sh - Script di avvio per Linux/Mac

# Colori
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}=== IDS System Launcher ===${NC}"
echo "1. Avvia sistema completo (consigliato)"
echo "2. Avvia solo Simulator"
echo "3. Avvia solo IDS (richiede Simulator già attivo)"
echo "4. Avvia entrambi in finestre separate"
echo ""
read -p "Scelta [1-4]: " choice

# Classpath
CP="target/classes:target/lib/*"

case $choice in
    1)
        echo -e "${GREEN}Avvio sistema completo...${NC}"
        java -cp "$CP" it.unibas.main.BootstrapMain
        ;;
    2)
        echo -e "${GREEN}Avvio Simulator...${NC}"
        java -cp "$CP" it.unibas.simulator.Main
        ;;
    3)
        echo -e "${YELLOW}ATTENZIONE: Assicurati che il Simulator sia già in esecuzione!${NC}"
        read -p "Premi ENTER per continuare..."
        echo -e "${GREEN}Avvio IDS...${NC}"
        java -cp "$CP" it.unibas.ids.IDSMain
        ;;
    4)
        echo -e "${GREEN}Avvio Simulator in nuova finestra...${NC}"
        # Per Linux con gnome-terminal
        #gnome-terminal --title="IDS Simulator" -- bash -c "java -cp '$CP' it.unibas.simulator.Main; read -p 'Premi ENTER per chiudere...'"
        # Per Mac con Terminal
        osascript -e 'tell app "Terminal" to do script "cd '$(pwd)' && java -cp \"'$CP'\" it.unibas.simulator.Main"'

        echo -e "${YELLOW}Attendo che il Simulator si avvii...${NC}"
        sleep 3

        echo -e "${GREEN}Avvio IDS...${NC}"
        java -cp "$CP" it.unibas.ids.IDSMain
        ;;
    *)
        echo -e "${RED}Scelta non valida${NC}"
        exit 1
        ;;
esac

# ==============================================

:: start.bat - Script di avvio per Windows
@echo off
setlocal enabledelayedexpansion

echo === IDS System Launcher ===
echo 1. Avvia sistema completo (consigliato)
echo 2. Avvia solo Simulator
echo 3. Avvia solo IDS (richiede Simulator gia' attivo)
echo 4. Avvia entrambi in finestre separate
echo.
set /p choice="Scelta [1-4]: "

:: Classpath
set CP=target\classes;target\lib\*

if "%choice%"=="1" (
    echo Avvio sistema completo...
    java -cp "%CP%" it.unibas.main.BootstrapMain
) else if "%choice%"=="2" (
    echo Avvio Simulator...
    java -cp "%CP%" it.unibas.simulator.Main
) else if "%choice%"=="3" (
    echo ATTENZIONE: Assicurati che il Simulator sia gia' in esecuzione!
    pause
    echo Avvio IDS...
    java -cp "%CP%" it.unibas.ids.IDSMain
) else if "%choice%"=="4" (
    echo Avvio Simulator in nuova finestra...
    start "IDS Simulator" cmd /k java -cp "%CP%" it.unibas.simulator.Main

    echo Attendo che il Simulator si avvii...
    timeout /t 3 /nobreak > nul

    echo Avvio IDS...
    java -cp "%CP%" it.unibas.ids.IDSMain
) else (
    echo Scelta non valida
    pause
    exit /b 1
)

pause