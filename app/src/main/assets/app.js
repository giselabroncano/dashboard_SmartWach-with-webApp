
// CONFIGURAZIONE 

const BROKER_HOST = "127.0.0.1";
const BROKER_PORT = 9001; // porta per i WebSockets
const CLIENT_ID = "web_dashboard_" + Math.random().toString(16).substring(2,8);
const TOPIC = "test/sensori/braccio"; 

// INIZIALIZZAZIONE CLIENT

// creazione client usando la libreria Paho MQTT

const client = new Paho.Client(BROKER_HOST,BROKER_PORT,CLIENT_ID);

// GESTIONE EVENTI

// Funzione che viene chiamata quando la connessione cade

client.onConnectionLost = function(responseObject){
    console.log( "Connessione persa: "+ responseObject.errorMessage);
    const badge = document.getElementById("label-connessione");
    badge.innerText = "Broker: Disconesso";
    badge.className = "status-badge disconnesso";

};

// Funzione che viene chiamata quando arriva un messaggio dallo smartwatch

client.onMessageArrived = function(message){

    const payload = message.payloadString; // testo ricevuto (?)

    console.log ("Messaggio ricevuto: "+payload);

   // Recupero elementi HTML da modificare
   
   const icona = document.getElementById("icona-movimento");
   const testo = document.getElementById("testo-movimento");

   // LOGICA DI CAMBIO SIMBOLI

   switch (payload){

    case "SALUTO":
        icona.innerText = "waving_hand"; // nome della incona google
        icona.style.color = "#FFD700";   // Oro/Giallo
            testo.innerText = "L'utente sta salutando";
            break;
        
        case "CORSA":
            icona.innerText = "directions_run";
            icona.style.color = "#FF4500";   // Arancione/Rosso
            testo.innerText = "Movimento rapido!";
            break;

        case "RIPOSO":
            icona.innerText = "accessibility_new";
            icona.style.color = "#2E8B57";   // Verde
            testo.innerText = "Braccio a riposo";
            break;

        case "OFF":
            icona.innerText = "sensors";
            icona.style.color = "#555";
            testo.innerText = " In attesa dello smartwatch...";
            break;

        default:
            icona.innerText = "help_outline";
            testo.innerText = "Stato: " + payload;   

    
   }

};

// CONNESSIONE AL BROKER

    const options = {
        timeout: 3,
        onSuccess: function(){
            console.log("Connesso con successo al broker!");
            // Aggiorniamo il badge nell'interfaccia
            const badge = document.getElementById("label-connessione");
            badge.innerText = "Broker: Connesso";
            badge.className = "status-badge connesso";
            
            // Inscrizione al Topic per ricevere i dati

            client.subscribe(TOPIC);   
        },
        onFailure: function(message){
            console.log ("Connessione falita: "+ message.errorMessage);

        }

    };

//  AVVIO EFFETIVO DELLA CONNESSIONE

    client.connect(options);

