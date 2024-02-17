package com.example.asan_sensor.model

import com.example.asan_sensor.recyclerView.Item
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

//Kotlin sington 지원
object SensorModel {
    private val checkedSensorList: ArrayList<Item> = arrayListOf()
    private var availableSensorList: ArrayList<Int> = arrayListOf()

    var sendData: LinkedBlockingQueue<ByteBuffer> = LinkedBlockingQueue()

    fun setAvailableSensorList(availableSensorList: ArrayList<Int>){
        SensorModel.availableSensorList = availableSensorList
    }

    fun getAvailableSensorList(): ArrayList<Int>{
        return availableSensorList
    }

    fun getCheckedSensorList():ArrayList<Item>{
        return checkedSensorList
    }

}