# Pepper Importer for COBHUNI Project

Java project to convert COBHUNI Corpus from json into Annis.

/bin/rm data/output/* ; mvn dependency:copy-dependencies && mvn clean install assembly:single
mvn clean dependency:copy-dependencies package

