package com.example.dashboardt_prova01.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    private fun avviaMonitoraggioBraccio(){

        // usiamo viewModelScope: se chiudi l'app, il processo si ferma da solo
        viewModelScope.launch {

            var ultimoInvio= 0L

            sensorManager.getMovementFlow().collect { coordinate ->

                if(_uiState.value.isMonitoringActive) {

                    // coordinate[0] è X, coordinate[1] è Y, coordinate[2] è Z
                    val x = coordinate[0]
                    val y = coordinate[1]
                    val z = coordinate[2]
                    val adesso = System.currentTimeMillis()

                    // CALCOLIAMO LO STATO PER LO SMARTWATCH E IL "SIMBOLO" PER LA WEB APP

                    val intensita = sqrt( x.pow(2) + y.pow(2) + z.pow(2)) // calcolo accelerazione
                    val statoFermo = sqrt( 0 + y.pow(2) + z.pow(2))
                    val simboloWeb: String
                    val testSmartwatch: String

                    if (intensita > 12f){
                        testSmartwatch = "In movimento"
                        simboloWeb = "CORSA"
                    }else if (intensita > statoFermo) {
                        testSmartwatch = "In movimento"
                        simboloWeb = "SALUTO"
                    }else{
                        testSmartwatch = "Fermo"
                        simboloWeb = "RIPOSO"
                    }


                    // AGGIORNIAMO QUI LE VARIABILI
                    _uiState.update { statoAttuale ->
                        //creiamo qui una nuova lista aggiungendo il valore X (per il grafico)
                        // dove tendremo solo gli ultimi 50 punti per non rallentare l'orologio

                        val nuovaLista = (statoAttuale.listaMovimenti + x).takeLast(50)

                        statoAttuale.copy(
                            accX = x,
                            accY = y,
                            accZ = z,
                            listaMovimenti = nuovaLista,
                            //Se il movimento X supera una soglia, cambiamo l'etichetta

                            movementElevator = if (intensita > statoFermo ) { //|| Math.abs(y) > 3f || Math.abs(z) > 3f
                                "In movimento"
                            } else {
                                "Fermo"
                            },
                            // Aggiorniamo il log di servizio
                            ultimoMessaggioInviato = "Dati in lettura... \uD83D\uDCE1"
                        )
                    }

                    // TRASMISSIONE
                    // se il broker è connesso, inviamo il dato
                    if (_uiState.value.isMqttConnected && (adesso - ultimoInvio > 500 )) {
                        val topic = "test/sensori/braccio"
                        mqttManager.publish(topic, simboloWeb)
                        ultimoInvio = adesso

                        _uiState.update {
                            it.copy(
                                ultimoMessaggioInviato = "Inviato alla Dashboard Web: X=$simboloWeb",
                                movementElevator = testSmartwatch
                            )
                        }
                    }

                }else {

                    _uiState.update { it.copy(accX = 0f, accY = 0f, accZ = 0f ) }
                }


            }
        }


    }
}