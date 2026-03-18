package com.example.dashboardt_prova01.presentation

/**
 *  Questa classe è il "ponte" tra hardware dello smartwatch e il codice.
 *  Implementiamo qui il SensorEventListener per ricevere i dati ogni volta
 *  che il braccio si muove. In poche parole trasforma i dati analogici in un flusso
 *  digitale (Flow)
 *
 * */


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MySensorManager(context: Context) {

    // Con questa variabile accediamo al sistema che gestisce i sensori dell'orologio

    private val systemSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Puntiamo all'accelerometro(FONDAMENTALE per il movimento del braccio)
    private val accelerometer: Sensor? = systemSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // CONTROLLO DI SICUREZZA

    init {
        if (accelerometer == null){

            println("ERRORE: Lo smartwatch non ha l'accelerometro!")
        }else {
            println("Ok: Accelerometro trovato e pronto per l'uso")
        }
    }



 /**
  * Implementiamo il FLOW tramite una funzione.
  *  Quando il ViewModel farà il .collect(), il sensore si accenderà.
  *  Quando il ViewModel si chiude, il sensore si spegnerà da solo.
  * */

 fun getMovementFlow(): Flow<FloatArray> =callbackFlow {

     val listener = object : SensorEventListener{
         override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
             // Obbligato ad implementarlo
         }

         override fun onSensorChanged(event: SensorEvent?) {
             event?.let {
                 if(it.sensor.type == Sensor.TYPE_ACCELEROMETER){
                     //Inviamo i 3 valori [X,Y,Z] nel Flow
                     trySend(it.values.copyOf())
                 }
             }
         }

     }
//  Registriamo il listener solo se l'accelerometro esiste
     accelerometer?.let{ sensore->
         systemSensorManager.registerListener(
             listener,
             sensore,
             SensorManager.SENSOR_DELAY_GAME
         )

     }?: run{
         // se manca il sensore, avvisiamo il flow di chiudersi
         close()
     }

     // Spegnimento AUTOMATICO per risparmiare batteria
    // Quando il flusso viene interrotto (es. chiudi l'app)
     // chiediamo al sistema di "spegnere" il sensore
     awaitClose {
         systemSensorManager.unregisterListener(listener)
     }
 }
    // Funzione di utilità per sapere se il sensore è disponibile nel smartwatch
    fun isAccelerometerAvailable(): Boolean = accelerometer != null

}