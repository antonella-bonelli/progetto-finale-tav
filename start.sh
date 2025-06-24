 echo "Avvio Simulator (macOS)..."
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)' && ./gradlew :simulator:run --args=\"daemon\""'
    
sleep 2
    
echo "Avvio IDS (macOS)..."
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)' && ./gradlew :ids:run"'