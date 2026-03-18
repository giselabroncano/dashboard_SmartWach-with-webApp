/**
 * Classe progettata per contenere ESCLUSIVAMENTE dati
 * Tutto ciò che vedo sullo smartwatch deve essere definito qui
 *
 * */

package com.example.dashboardt_prova01.presentation

data class DashboardState(
    // x, y,z sono le coordinate grezze dell'accelerometro (I SENSORI)
    val accX: Float = 0f,
    val accY: Float = 0f,
    val accZ: Float = 0f,

    // Lo storico dei punti per il grafico (lista di Float)
    // Usiamo una lista per disegnare la linea che si muove

    val listaMovimenti: List<Float> = emptyList(),

    // Simboli del modello Bicocca (stato riconoscimento)
    // SARà AGGIORNATO DAL MODELLO BICOCCA

    val movementElevator: String = "Fermo",

    // Stato della Trasmissione
    // usiamo il boolean per gestire i colori della UI
    val isMqttConnected: Boolean = false,

    //Segnale di emergenza (SEMAFORO ROSSO)
    // Serve a dirmi "perché" app non sta funzionando
    // (es. wifi spento; broker irraggiungibile)
    val messaggioErrore: String? = null,

    // Messaggio di log di servizio ( utile per il debug)
    // Serve per monitoraggio del flow.
    // Mi conferma che i dati stanno EFFETTIVAMENTE uscendo

    val ultimoMessaggioInviato: String = "\\u26A0\\uFE0F QUALCOSA NON VA",

    val isMonitoringActive: Boolean = false  // il monitoraggio è ON o OFF

)


