package com.example.dashboardt_prova01.presentation

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import java.nio.FloatBuffer
import android.content.Context
import android.util.Log



class GestureClassifier(context: Context) {

    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    init {
        try {
            val bytes = context.assets.open("gesture_model.onnx").readBytes()
            session = env.createSession(bytes)

            //Log.d("ONNX", "File letto correttamente. Byte = ${bytes.size}")
            //Log.d("ONNX", "Input: ${session.inputNames}")
            //Log.d("ONNX", "Output: ${session.outputNames}")
            //Log.d("ONNX", "Info input: ${session.inputInfo}")
            //Log.d("ONNX", "Info output: ${session.outputInfo}")



        }catch (e:Exception){

            Log.e("ONNX", "Errore nella lettura del modello", e)

            throw e
        }
    }

// METODO predict()

    fun predict(x:Float, y: Float, z: Float): String{

        val input = floatArrayOf(x,y,z)
        val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), longArrayOf(1,3))
        val result = session.run ( mapOf( "float_input" to tensor))
        val label = result[0].value as Array<String>


        tensor.close()
        result.close()




        return label[0]

    }

}


