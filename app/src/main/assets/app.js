
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

    const payload = message.payloadString; // rICEVE: "SINISTRA", "DESTRA", "SU", "GIU"

    console.log ("MQTT ARRIVATO"+payload);

   // Recupero elementi HTML da modificare
   
   const icona = document.getElementById("icona-movimento");
   const testo = document.getElementById("testo-movimento");

   // LOGICA DI CAMBIO SIMBOLI

   switch (payload){

    case "DESTRA":

        console.log("ESEGUO DESTRA");

        icona.innerText = "arrow_forward";
        testo.innerText = "Slide successiva (Avanti)";
        document.body.style.backgroundColor = "red";
        slideSuccessiva(); // Simula tasto "Freccia Destra" o "Space" per andare avanti
            break;
        
    case "SINISTRA":

            console.log("ESEGUO SINISTRA");

            icona.innerText = "arrow_back"; // nome della incona google
            testo.innerText = "Slide Precedente (Indietro)";
            document.body.style.backgroundColor = "blue";
            slidePrecedente(); // Simula tasto " Freccia Sinistra" per andare indietro
            break;
        
    case "SU":

            console.log("ESEGUO SU");

            icona.innerText = "zoom_in";
            testo.innerText = "Zoom Avanti";
            document.body.style.backgroundColor = "green";
            simulaZoom(true); // Simula "Ctrl +" per lo zoom nei browser 
            break;

    case "GIU":

            console.log("ESEGUO GIU");

            icona.innerText = "zoom_out";
            testo.innerText = "Zoom Indietro";
            document.body.style.backgroundColor = "yellow";
            simulaZoom(false);
            break;
            
        case "RIPOSO":
            icona.innerText = "sensors";
            testo.innerText = "In attesa di gesti...";
            break;

        case "OFF":
            icona.innerText = "sensors";
           // icona.style.color = "#555";
            testo.innerText = " In attesa dello smartwatch...";
            break;

        default:
            icona.innerText = "help_outline";
            testo.innerText = "Stato: " + payload;   

   }

};


// GESTIONE DELLE SLIDE

const slides = [
    "prova_img/slide1.png",
    "prova_img/slide2.png",
    "prova_img/slide3.png",
    "prova_img/slide4.png"
];

let slideCorrente = 0;



// FUNZIONE PER GESTIRE LO ZOOM DIRECTAMENTE SULLA PAGINA WEB

let zoomAttuale = 1;

function simulaZoom(avanti){

    const slideViewer = document.getElementById("slide-viewer");
    if (avanti) zoomAttuale += 0.1;
    else zoomAttuale = Math.max(0.5, zoomAttuale - 0.1); // non scende sotto il 50%
    slideViewer.style.transform = `scale(${zoomAttuale})`;
    slideViewer.style.transformOrigin = "center center";
   
}    

function aggiornaContatore() {
    document.getElementById("slide-counter").innerText =
       `Slide ${slideCorrente + 1} / ${slides.length}`;

}

function slideSuccessiva(){

    if(slideCorrente < slides.length - 1){

        slideCorrente++;

        document.getElementById("slide-viewer").src =
            slides[slideCorrente];

        aggiornaContatore();
    }
}

function slidePrecedente(){

    if(slideCorrente > 0){

        slideCorrente--;

        document.getElementById("slide-viewer").src =
            slides[slideCorrente];
        
            aggiornaContatore();

    }
}


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

     aggiornaContatore();
//  AVVIO EFFETIVO DELLA CONNESSIONE

    client.connect(options);

    