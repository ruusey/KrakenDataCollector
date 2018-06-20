# KrakenDataCollector
Cryptocurrency price history collector for the Kraken exchange

# Deploying and running
How to get **KCollector** up and running on your system.

**1)** Make sure you have the latest version of Maven installed.
**2)** Create a text file containing your Kraken API keys (2 lines, line 1 = API Key, line 2 = API Secret)
**3)** Clone or download the **KCollector** Source.
**4)** Locate **KrakenScraper.java** under `src/com/kraken` Open it in any generic text editing software.
**5)** Replace the line `credScan = new Scanner(new File("C:/temp/kraken_keys.txt"));` with the relevant path to your keys
**5)** Exectute a `mvn clean install` to generate the executable jar under `/target`
**6)** Invoke via command line `java -jar /path/to/KCollector0.0.1SNAPSHOT.jar`

