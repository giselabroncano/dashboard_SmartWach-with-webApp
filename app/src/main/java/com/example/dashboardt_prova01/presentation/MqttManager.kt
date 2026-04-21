package com.example.dashboardt_prova01.presentation

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import java.util.UUID

class MqttManager( private val brokerIp: String) {

    // Configuriamo il client MQTT
     private var client: Mqtt5AsyncClient? = null

    // CONNESSIONE

    fun connect(onResult:(Boolean) -> Unit){

        // CREAZIONE DEL CLIENT ( ogni volta che clicco START)
        client = Mqtt5Client.builder()
            .identifier("Watch-"+ UUID.randomUUID().toString())
            .serverHost(brokerIp)
            .serverPort(1883) // 1883 (localhost)
            .buildAsync()

        // Restituisce TRUE se è connesso, altrimenti mi da errore

        client?.connect()?.whenComplete { _,throwable ->
            onResult(throwable==null)
            // se throwable è null, non ci sono stati errori

        }
    }

    // Funzione per inviare i dati dei sensori

    fun publish(topic:String, payload: String){
        client ?.let { mqttClient ->
            if(mqttClient.state.isConnected){
                mqttClient.publishWith()
                    .topic(topic)
                    .payload(payload.toByteArray())
                    .send()
            }
        }

    }

    // funzione per resettare client

    fun resetClient(){

        client?.let { attivo ->
            attivo.disconnect()
            client = null
        }
    }
}











