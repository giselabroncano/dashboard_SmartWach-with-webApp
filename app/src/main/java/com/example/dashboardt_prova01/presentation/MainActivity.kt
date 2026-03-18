package com.example.dashboardt_prova01.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.dashboardt_prova01.R
import com.example.dashboardt_prova01.presentation.theme.DashboardT_prova01Theme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.WavingHand
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializziamo il  Sensore e il Broker
        val sensorManager = MySensorManager(this)

        // IP TEST
        val mqttManager = MqttManager("10.0.2.2")

        // Creiamo il ViewModel passando i manager
        val viewModel = WatchViewModel(sensorManager, mqttManager)

        //Montiamo la UI
        setContent {
            MaterialTheme { // Applichiamo lo stile standard Wear OS
                DashboardApp(viewModel)
            }
        }

    }

   @Composable
   fun MovementGraph(points: List<Float>){
        // Canvas è una lavagna su cui disegnamo a mano libera
       Canvas(modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp)){
           if(points.size < 2) return@Canvas

           val path = Path()
           val stepX = size.width / (points.size - 1) // Spazio orizzontale tra i punti

           points.forEachIndexed { i,yVal ->
               val x = i * stepX
               //centriamo il valore Y( altezza/ 2) e lo invertiamo (innAndroid Y cresce verso il basso)
               val y = (size.height / 2) - (yVal * 4f)

               if (i == 0) path.moveTo(x,y) else path.lineTo(x, y)
           }

           // disegniamo la linea color Ciano
           drawPath(path, Color.Cyan, style = Stroke(width = 2f))

       }
   }

    @Composable
    fun DashboardApp(viewModel: WatchViewModel) {
        // State "osserva" il ViewModel: se accX cambia nel ViewModel, la Ui si ridisegna qui
        val state by viewModel.uiState.collectAsState()

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {


        // ScalingLazyColumn gestisce la curvatura degli schermi tondi
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            autoCentering = AutoCenteringParams(itemIndex = 1)
        ) {

            // ----  CHIP DEL MOVIMENTO/ per capire ciò che sta succedendo.
            item {

                val coloreSfondo = if (state.movementElevator == "In movimento") {
                    Color(0xFF2E7D32)
                } else {
                    Color.DarkGray
                }

                val iconaStato = if (state.movementElevator == "In movimento") {
                    Icons.Default.DirectionsRun
                } else {
                    Icons.Default.Pause
                }
                Chip(
                    onClick = { },
                    label = {
                        Text(
                            state.movementElevator,
                            style = MaterialTheme.typography.caption2
                        )
                    }, // Mostra "In movimento" o  "Stop"
                    icon = { Icon(iconaStato, contentDescription = null, tint = Color.White) },
                    colors = ChipDefaults.chipColors(backgroundColor = coloreSfondo),
                    modifier = Modifier.width(120.dp).height(60.dp)
                )
            }

            //------ STATO BROKET MOSQUITTO ------

            item {

                val colorIndicator = if (state.isMqttConnected) Color.Green else Color.Red
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // un piccolo pallino colorato per il feedback immediato
                    Box(modifier = Modifier.size(8.dp).background(colorIndicator, CircleShape))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (state.isMqttConnected) "Broker: ON" else "Broker: OFF",
                        style = MaterialTheme.typography.caption3
                    )
                }

            }

            //------ IL GRAFICO IN TEMPO REALE ------
            item { MovementGraph((state.listaMovimenti)) }

            // Spazio vuoto finale
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
        //...... PULSANTI START E STOP........


        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center // Per tenere i pulsanti vicino tra loro
        ) {
            // PULSANTE START
            Button(
                onClick = { viewModel.startMonitoraggio() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B5E20)), // Verde scuro
                modifier = Modifier.size(42.dp)

            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(15.dp)) // spazio tra i due bottoni

            // PULSANTE STOP
            Button(
                onClick = { viewModel.stopMonitoraggio() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFB71C1C)), // Rosso
                modifier = Modifier.size(42.dp)

            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
            }

        }

       }



    //---- LOG DI SERVIZIO (Ultimo Messaggio)----

//            item {
//                Text (
//                    text = state.ultimoMessaggioInviato,
//                    style = MaterialTheme.typography.caption3,
//                    color = Color.LightGray,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(horizontal = 12.dp)
//                )
//
//            }


        }

    }


