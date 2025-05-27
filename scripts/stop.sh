ps -ef | grep $(ls *.jar) | grep -v "grep" | awk '{print $2}' | xargs kill
