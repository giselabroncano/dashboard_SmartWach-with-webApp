# Smartwatch Arm Monitoring Dashboard

Questa applicazione Android (Wear OS) monitora i movimenti del braccio in tempo reale usando i sensori inerziali dello Smartwatch (accelerometro triassiale). 
I dati vengono elaborati tramite una logica a soglie e trasmessi in formato JSON via MQTT a un broker Mosquitto per la visualizzazione e l'analisi esterna.

**NOTA:** Il modello di classificazione dei movimenti non è ancora stato integrato nell'interfaccia dell'app. 
          Al momento, l'app gestisce l'intera pipeline di estrazione, processamento locale e invio dati. 


## ARCHITETTURA DEL SISTEMA

La pipeline dei dati è strutturata come segue:

+  **Smartwatch(Wear Os)**: Campionamento in tempo reale dell'accelerometro triassiale, gestione dello stato tramite Kotlin Flow e logica a soglie per il rilevamento del movimento.
+  **Protocollo MQTT**: I dati vengono impacchettati in oggetti JSON e inviati al broker in modo asincrono per non impattare sulla fluidità della UI.
+  **Mosquitto Broker**: Funge da hub centrale per lo smistamento dei messaggi sulla rete locale.
+  **MQTT Explorer**: Usato come dashboard temporanea per visualizzare i flussi dati in tempo reale e strumento di monitoraggio e debug per validare la ricezione dei pacchetti.


## PREREQUISITI

Per compilare, eseguire e testare correttamente l'applicazione, assicurarsi di avere installato i seguenti strumenti:

### Ambiente di Sviluppo (PC)

-  **Android Studio** (Jellyfish o versione superiore) con SDK Wear OS.
-  **Java JDK 17** (Consigliato per la compatibilità con Gradle).
-  **Emulatore Wear OS**: Creato tramite *Device Manager*.

### Servizi esterni ( Broker & Debug)

-  **Mosquitto MQTT Broker**: Installato e configurato sul PC Locale (Porta di default: 1883).
-  **MQTT Explorer**: Strumento fondamentale  per la visualizzazione dei pacchetti JSON in tempo reale.

### Connettività e Rete

- **Indirizzo IP**:  Se si usa emulatore di Android Studio, il broker deve essere puntato all'indirizzo 10.0.2.2 (che rappresenta il *localhost* del PC ospite dall'interno dell'emulatore).
- **Configurazione Mosquitto**: il file *mosquitto.conf* deve permettere le connessioni esterne (*listener 1883 0.0.0.0 e allow_anonymous true*)


## Guida all'avvio 

Eseguire questi passaggi per mettere in funzione la pipeline di monitoraggio:

1. **Configurare il broker**: Avvia il servizio Mosquitto sul PC. Assicursi che il firewall permetta il traffico sulla porta 1883.
2. **Monitoraggio**: Aprire MQTT Explorer, connettersi a *localhost*: 1883  e rimanere in ascolto sul topic configurato (esempio: *test/sensori/braccio*)
3. **App & Test**:

     + Aprire il progetto su Android Studio e avviare l'emulatore Wear Os.
     + Installare l'applicazione e premere il pulsante START (Verde) sulla dashboard dell'orologio.
     + Usa il *Virtual Sensors* dell' emulatore (opzione *move*) per simulare il movimento del braccio: si vedra il grafico muoversi nella dashboard dello smartwatch e i dati apparire istantaneamente su MQTT Explorer.
  
4. **Stop**: Premere il pulsante STOP (Rosso) per interrompere il monitoraggio e la trasmissione.   
  

## Formati dei dati (JSON)

I messaggi inviati al broker seguono questa struttura:

`{
  "accX": 0.45,
  "accY": -1.20,
  "accZ": 9.81,
  "stato": "In movimento"
}`

