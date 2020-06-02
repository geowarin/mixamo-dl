package com.example.demo.view

import com.example.demo.json.set
import com.example.demo.sql.ProductType
import com.nfeld.jsonpathlite.extension.read
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.testfx.api.FxToolkit
import tornadofx.*
import kotlin.test.assertEquals

internal class MotionOptionsTest {

  class Toto(val data: JSONObject) {

    var supports_inplace: Boolean
    get() = data.read("$.supports_inplace")!!
    set(value) = data.set("supports_inplace", value)

    val mirror: Boolean
    get() = data.read("$.gms_hash.mirror")!!
  }

  class TotoModel(toto: Toto) : ItemViewModel<Toto>(toto) {
    val supports_inplace = bind(Toto::supports_inplace)
    val mirror = bind(Toto::mirror)
  }


  @Language("json")
  private val s = """{
    "supports_inplace": false,
    "loopable": true,
    "default_frame_length": 35,
    "duration": 1.17,
    "gms_hash": {
      "model-id": 103120902,
      "mirror": false,
      "trim": [
        0.0,
        100.0
      ],
      "inplace": false,
      "arm-space": 0,
      "params": [
        [
          "Pray Towards",
          0.0
        ],
        [
          "Emotion",
          0.0
        ],
        [
          "Overdrive",
          0.0
        ]
      ]
    }
  }
  """

  @BeforeEach
  fun setup() {
    FxToolkit.registerPrimaryStage()
  }

  @Test
  fun `convert parameters of motion pack`() {

    val options = Toto(JSONObject(s))

//    assertEquals(false, options.supports_inplace)
//    options.supports_inplace = true
//    assertEquals(true, options.supports_inplace)
//
//    assertEquals(false, options.mirror)
//    options.mirror = true
//    assertEquals(true, options.mirror)

    val totoModel = TotoModel(options)
    totoModel.mirror.value = true

    assertEquals(true, totoModel.mirror.value)
    assertEquals(false, options.mirror)
    totoModel.commit()
    assertEquals(false, options.mirror)
  }


}