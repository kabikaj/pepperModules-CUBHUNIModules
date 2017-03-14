/bin/rm data/output/* ; mvn dependency:copy-dependencies && mvn clean install assembly:single
mvn clean dependency:copy-dependencies package

