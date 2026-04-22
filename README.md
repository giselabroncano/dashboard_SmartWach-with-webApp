# Smartwatch Arm Monitoring Dashboard

Questa applicazione Android (Wear OS) monitora i movimenti del braccio in tempo reale usando i sensori inerziali dello Smartwatch (accelerometro triassiale). 
I dati vengono elaborati,per il momento, tramite una logica a soglie e trasmessi via MQTT (broker Mosquitto) ad una Dashboard Web per la visualizzazione remota.


**NOTA:** Il modello di classificazione dei movimenti verrà integrato in futuro. Al momento, l'app gestisce la pipeline di estrazione, processamento locale e invio dei simboli di stato.


## ARCHITETTURA DEL SISTEMA

La pipeline dei dati è strutturata come segue:

+  **Smartwatch(Wear Os)**: Campionamento in tempo reale dell'accelerometro triassiale, gestione dello stato tramite Kotlin Flow e invio di stringhe identificative ("SALUTO", "CORSA", "RIPOSO", "OFF").
+  **Protocollo MQTT**: Trasmissione asincrona leggera basata su stringhe di testo per garantire la massima fluidità della UI.
+  **Mosquitto Broker**: Funge da hub centrale .Grazie al supporto WebSockets, permette la comunicazione diretta tra l'app Android e il browser.
+  **MQTT Explorer**: Web Dashboard: Interfaccia reattiva (HTML/JS) che traduce i messaggi ricevuti in simboli grafici e colori dinamici.


## PREREQUISITI

Per compilare, eseguire e testare correttamente l'applicazione, assicurarsi di avere installato i seguenti strumenti:

### Ambiente di Sviluppo (PC)

-  **Android Studio** (Jellyfish o versione superiore) con SDK Wear OS.
-  **Java JDK 17** (Consigliato per la compatibilità con Gradle).
-  **Emulatore Wear OS**: Creato tramite *Device Manager*.
- **Web Server Locale**: Necessario per eseguire la Dashboard (es. estensione Live Server di VS Code o Python http.server).

### Servizi esterni ( Broker & Debug)

-  **Mosquitto MQTT Broker**: Installato e configurato sul PC Locale (Porta di default: 1883).

### Connettività e Rete

- **Indirizzo IP**:  Se si usa emulatore di Android Studio, il broker deve essere puntato all'indirizzo 10.0.2.2 (che rappresenta il *localhost* del PC ospite dall'interno dell'emulatore).Se si usa un dispositivo reale, usare l'IP locale del PC (es. 192.168.1.XX).
- **Configurazione Mosquitto**: il file *mosquitto.conf* deve includere il supporto WebSockets per la Dashboard:

*listener 1883
allow_anonymous true

listener 9001
protocol websockets*


## Guida all'avvio 

Eseguire questi passaggi per mettere in funzione la pipeline di monitoraggio:

1. **Configurare il broker**:  Avviare Mosquitto assicurandoti che la porta 9001 sia attiva per i WebSockets.
2. **Lanciare la Dashboard**: Apri il file index.html tramite un server web locale. Il badge indicherà "Broker: Connesso" quando il collegamento sarà stabilito.
4. **App & Test**:
     + Aprire il progetto app su Android Studio e avviare l'emulatore Wear Os.
     + Premere il pulsante START (Verde) sulla dashboard dell'orologio. Il pallino di stato conessione diventerà verde.
     + **Simulare il movimento**: la Web Dashboard cambierà icona istantaneamente (es. apparirà la mano per il "SALUTO" o l'omino per la "CORSA") quando al usare il *Virtual Sensors* dell' emulatore (opzione *move*)  per simulare il movimento del braccio. Oltre al cambio del icona nella web App si può vedere il grafico muoversi nella dashboard dello smartwatch. 
  
5. **Stop**: Premendo STOP (Rosso), lo smartwatch invierà il comando "OFF", resettando la Web Dashboard al suo stato iniziale prima di chiudere la connessione.

## Formati dei dati (JSON)

I messaggi inviati al broker seguono questa struttura:

`{
  "accX": 0.45,
  "accY": -1.20,
  "accZ": 9.81,
  "stato": "In movimento"
}`

