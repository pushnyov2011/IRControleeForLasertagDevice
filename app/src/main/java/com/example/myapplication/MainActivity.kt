package com.example.myapplication

import android.annotation.SuppressLint
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var consumerIrManager: ConsumerIrManager
    object LTConstants {
        // Константы повреждений
        const val DAMAGE1 = 0x00
        const val DAMAGE2 = 0x01
        const val DAMAGE4 = 0x02
        const val DAMAGE5 = 0x03
        const val DAMAGE7 = 0x04
        const val DAMAGE10 = 0x05
        const val DAMAGE15 = 0x06
        const val DAMAGE17 = 0x07
        const val DAMAGE20 = 0x08
        const val DAMAGE25 = 0x09
        const val DAMAGE30 = 0x0A
        const val DAMAGE35 = 0x0B
        const val DAMAGE40 = 0x0C
        const val DAMAGE50 = 0x0D
        const val DAMAGE75 = 0x0E
        const val DAMAGE100 = 0x0F

        // Команды
        const val COMMAND = 0x83
        const val COMMAND_TEAM = 0xA9
        const val ADMIN_KILL = 0x00
        const val PAUSE_UNPAUSE = 0x01
        const val START_GAME = 0x02
        const val RESTORE_DEFAULTS = 0x03
        const val RESPAWN = 0x04
        const val NEW_GAME_IMMEDIATE = 0x05
        const val FULL_AMMO = 0x06
        const val END_GAME = 0x07
        const val RESET_CLOCK = 0x08
        const val INITIALIZE_PLAYER = 0x0A
        const val EXPLODE_PLAYER = 0x0B
        const val NEW_GAME_READY = 0x0C
        const val FULL_HEALTH = 0x0D
        const val FULL_ARMOR = 0x0F
        const val CLEAR_SCORES = 0x14
        const val TEST_SENSORS = 0x15
        const val STUN_PLAYER = 0x16
        const val DISARM_PLAYER = 0x17
        const val CHANGE_COLOR = 0x09
        const val CHANGE_POWER = 0x0E


        // Параметры протокола
        const val IR_FREQUENCY = 40000 // 40 kHz
        const val START_PULSE = 2400   // Стартовый импульс
        const val START_SPACE = 600    // Пауза после стартового импульса
        const val BIT_ONE_PULSE = 1200 // Импульс для "1"
        const val BIT_ZERO_PULSE = 600 // Импульс для "0"
        const val BIT_SPACE = 600      // Пауза между битами
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация ConsumerIrManager
        consumerIrManager = getSystemService(CONSUMER_IR_SERVICE) as ConsumerIrManager

        // Проверка поддержки ИК-порта
        if (!consumerIrManager.hasIrEmitter()) {
            Toast.makeText(this, "ИК-порт не поддерживается", Toast.LENGTH_SHORT).show()
            finish()
        }

        val respawn = findViewById<Button>(R.id.respawn)

        respawn.setOnClickListener { sendCommand( LTConstants.COMMAND.toByte(), LTConstants.RESPAWN)}

        val redTeam = findViewById<Button>(R.id.redTeam)
        redTeam.setOnClickListener{sendCommand( LTConstants.COMMAND_TEAM.toByte(), 0)}

        val blueTeam = findViewById<Button>(R.id.blueTeam)
        blueTeam.setOnClickListener{sendCommand( LTConstants.COMMAND_TEAM.toByte(), 1)}

        val greenTeam = findViewById<Button>(R.id.greenTeam)
        greenTeam.setOnClickListener{sendCommand( LTConstants.COMMAND_TEAM.toByte(), 3)}

        val yellowTeam = findViewById<Button>(R.id.yellowTeam)
        yellowTeam.setOnClickListener{sendCommand( LTConstants.COMMAND_TEAM.toByte(), 2)}

        val newGame = findViewById<Button>(R.id.newGame)
        newGame.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.NEW_GAME_IMMEDIATE )}

        val killPlayer = findViewById<Button>(R.id.killPlayer)
        killPlayer.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.ADMIN_KILL )}

        val shootPower = findViewById<Button>(R.id.shootPower)
        shootPower.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.CHANGE_POWER )}

        val pause = findViewById<Button>(R.id.Pause)
        pause.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.PAUSE_UNPAUSE )}

        val changeColor = findViewById<Button>(R.id.changeColor)
        changeColor.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.CHANGE_COLOR)}

        val blowUpPlayer  =  findViewById<Button>(R.id.blowUpPlayer)
        blowUpPlayer.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.EXPLODE_PLAYER)}

        val  testSensor = findViewById<Button>(R.id.testSensor)
        testSensor.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.TEST_SENSORS)}

        val addAmmo = findViewById<Button>(R.id.addAmmo)
        addAmmo.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.FULL_AMMO)}

        val stunPlayer =  findViewById<Button>(R.id.stunPlayer)
        stunPlayer.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.STUN_PLAYER)}

        val fullHealth =  findViewById<Button>(R.id.fullHealth)
        fullHealth.setOnClickListener{sendCommand( LTConstants.COMMAND.toByte(), LTConstants.FULL_HEALTH)}





    }




    @RequiresApi(Build.VERSION_CODES.Q)
    fun shot(playerId: Long, teamId: Long, damageValue: Long) {
        var result = playerId
        result = result shl 2 // 2 бита для teamId
        result = result or teamId
        result = result shl 4 // 4 бита для damageValue
        result = result or damageValue
        sendSonyPattern(result, 14)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sendCommand(command: Byte, byte2: Int) {
        var result = command.toLong() and 0xFF
        result = result shl 8
        result = result or (byte2 and 0xFF).toLong()
        result = result shl 8
        result = result or 0xE8
        sendSonyPattern(result, 24)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sendSonyPattern(data: Long, bitCount: Int) {
        try {
            val pattern = mutableListOf<Int>().apply {
                add(LTConstants.START_PULSE)
                add(LTConstants.START_SPACE)


            for (i in (bitCount - 1) downTo 0) {
                val bit = (data shr i) and 1
                if (bit == 1L) {
                    this.add(LTConstants.BIT_ONE_PULSE) // Явно указываем this.add()
                    this.add(LTConstants.BIT_SPACE)
                } else {
                    this.add(LTConstants.BIT_ZERO_PULSE)
                    this.add(LTConstants.BIT_SPACE)
                }
            }}

            consumerIrManager.transmit(
                LTConstants.IR_FREQUENCY,
                pattern.toIntArray()
            )
        } catch (e: Exception) {
            Log.e("IR_ERROR", "Ошибка отправки: ${e.message}")
        }
    }
}
















