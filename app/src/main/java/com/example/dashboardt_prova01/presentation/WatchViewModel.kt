/**
 *  Questa classe è il "cervello" della Applicazione.
 *  Il suo compito è prendere i dati grezzi della classe MySensorManager, usarli per aggiornare
 *  le variabili definite nella classe DashBoardState e inviarli alla Piattaforma web
 *
 * */



package com.example.dashboardt_prova01.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class WatchViewModel(
    private val sensorManager: MySensorManager,
    private val mqttManager: MqttManager
    ): ViewModel() {

    // Inizializziamo lo stato con le variabili della classe dashboardState
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState = _uiState.asStateFlow()

    fun startMonitoraggio(){

        // Puliamo il vecchio client (sopratutto se esisteva uno precedente)
        mqttManager.resetClient()

        _uiState.update {  it.copy(
            isMonitoringActive = true,
            ultimoMessaggioInviato = "On  \uD83D\uDE80"
        ) }
        // Avvia conessione
        mqttManager.connect { successo->
            _uiState.update { it.copy(isMqttConnected = successo) }

            // facciamo partire il "FLOW" dei dati
            if (successo) {
                avviaMonitoraggioBraccio()
            }else{
                _uiState.update { it.copy(ultimoMessaggioInviato = "Errore Connessione ❌") }
            }

        }

    }

    fun stopMonitoraggio(){

        // invia subito il segnale di chiusura alla web App
        if(_uiState.value.isMqttConnected){
            mqttManager.publish("test/sensori/braccio", "OFF")
        }

    // Aggiorna lo stato interno della UI dell'orologio

        _uiState.update { it.copy(
            isMonitoringActive = false,
            isMqttConnected = false,
            movementElevator = "Stop",
            ultimoMessaggioInviato = "Monitoraggio OFF",
            accX = 0f, accY = 0f, accZ = 0f // Pulizia dei numeri sulla dashboard
        ) }

        // Scollega il broker
        mqttManager.resetClient()
    }


private var isGestoInCorso = false

    private fun avviaMonitoraggioBraccio(){

        // usiamo viewModelScope: se chiudi l'app, il processo si ferma da solo
        viewModelScope.launch {

            var ultimoInvio= 0L

            sensorManager.getMovementFlow().collect { coordinate ->

                if(_uiState.value.isMonitoringActive) {

                    // coordinate delle tre assi
                    val x = coordinate[0]
                    val y = coordinate[1] - 9.81f // sottrare la gravità dall' asse Y per centrare il valore 0 quando orologio è fermo
                    val z = coordinate[2]
                    val adesso = System.currentTimeMillis()

                    Log.d(
                        "ACC_VALUES",
                        "x=$x y=$y"
                    )
                    // RICONOSCIMENTO DEI GESTI

                    if (!isGestoInCorso) {
                        val sogliaX = 6f
                        val sogliaY = 3f
                        var simboloWeb = "RIPOSO"
                        var testSmartwatch = "Fermo"

                        // ANALIZZI DI QUALE VARIABILE HA SUPERATO LA SOGLIA PER DETERMINARE LA DIREZIONE
                        // CON ASSE DOMINANTE


                        if (abs(x)> abs(y)) {

                            if (x > sogliaX) {
                                simboloWeb = "DESTRA"
                                testSmartwatch = "Gesto Destra"
                            }

                            if (x < -sogliaX) {
                                simboloWeb = "SINISTRA"
                                testSmartwatch = "Gesto Sinistra"
                            }
                        }else{

                            if(y > sogliaY ) {
                                simboloWeb = "SU"
                                testSmartwatch = "Gesto Su"
                            }

                            if(y < -sogliaY ){
                                simboloWeb = "GIU"
                                testSmartwatch = "Gesto Giù"
                            }
                        }

                        // RILEVAMENTO DI UN GESTO DIVERSO DA RIPOSO
                        if (simboloWeb != "RIPOSO") {

                            Log.d(
                                "GESTURE",
                                "Riconosciuto: $simboloWeb x =$x y =$y"
                            )
                            isGestoInCorso = true // congelamento sensori.

                            Log.d(
                                "GESTURE_LOCK",
                                "BLOCCATO- ${System.currentTimeMillis()}"
                            )

                            // AGGIORNIAMO INTERFACCIA DELLO SMARTWATCH

                            _uiState.update { statoAttuale ->
                                //creiamo qui una nuova lista aggiungendo il valore X (per il grafico)
                                // dove tendremo solo gli ultimi 50 punti per non rallentare l'orologio

                                val nuovaLista = (statoAttuale.listaMovimenti + x).takeLast(50)
                                statoAttuale.copy(
                                    accX = x, accY = y, accZ = z,
                                    listaMovimenti = nuovaLista,
                                    //Se il movimento X supera una soglia, cambiamo l'etichetta
                                    movementElevator = testSmartwatch,

                                    )
                            }


                            // TRASMISSIONE ALLA WEB DASHBOARD
                            // Inviamo il comando solo se è un gesto valido e se sono passati almeno 800ms dall'ultimo
                            if (_uiState.value.isMqttConnected && (adesso - ultimoInvio > 1000)) {
                                val topic = "test/sensori/braccio"
                                mqttManager.publish(topic, simboloWeb)
                                ultimoInvio = adesso

                                _uiState.update {
                                    it.copy(
                                        ultimoMessaggioInviato = "Inviato alla Dashboard Web: $simboloWeb",
                                        movementElevator = testSmartwatch
                                    )
                                }

                                //TIMER DI SBLOCCO: gesto visibile per 500ms
                                // Dopo Resettiamo sia la web app che i sensori locali
                                //viewModelScope.launch {
                                //kotlinx.coroutines.delay(1000)
                                //if (_uiState.value.isMqttConnected && _uiState.value.isMonitoringActive) {
                                //    mqttManager.publish(topic, "RIPOSO")
                                // }
                                //  isGestoInCorso = false // sblocca i sensori per il prossimo gesto
                                //}
                            }
                            // Sblocchiamo
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(1000)
                                isGestoInCorso = false
                                Log.d(
                                    "GESTURE_LOCK",
                                    "SBLOCCATO- ${System.currentTimeMillis()}"
                                )
                            }
                        }

                    }else{
                        // Se il braccio è effettivamente fermo (e non stiamo congelando un gesto)
                        _uiState.update { statoAttuale ->
                            val nuovaLista = (statoAttuale.listaMovimenti + x).takeLast(50)
                            statoAttuale.copy(
                                accX = x, accY = y, accZ = z,
                                listaMovimenti = nuovaLista,
                                movementElevator = "Fermo"
                            )
                        }
                    }


                } else {
                        _uiState.update { it.copy(accX = 0f, accY = 0f, accZ = 0f) }
                    }

                }
            }
        }


    }
