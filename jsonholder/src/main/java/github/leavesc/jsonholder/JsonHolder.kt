package github.leavesc.jsonholder

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * 作者：leavesC
 * 时间：2020/5/16 20:59
 * 描述：
 * GitHub：https://github.com/leavesC
 */

val SerializableHolder: ISerializableHolder = GsonSerializableHolder()

object TypeTokenHolder {

    inline fun <reified T> type(): Type {
        return object : TypeToken<T>() {}.type
    }

}

sealed class ISerializableHolder {

    companion object {

        fun <T> getOrNull(block: () -> T): T? {
            try {
                return block()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

        fun <T> getOrDefault(block: () -> T, defaultValue: T): T {
            try {
                return block() ?: defaultValue
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return defaultValue
        }

    }

    /******************************* Java + Kotlin 共用的方法 *************************************/

    abstract fun toJson(ob: Any?): String

    /**
     * 获取格式化好的 Json
     * @param ob
     */
    abstract fun toJsonPretty(ob: Any?): String

    abstract fun <T> toBean(json: String?, clazz: Class<T>): T

    abstract fun <T> toBean(json: String?, type: Type): T

    fun <T> toBeanOrNull(json: String?, clazz: Class<T>): T? {
        return getOrNull { toBean(json, clazz) }
    }

    fun <T> toBeanOrNull(json: String?, type: Type): T? {
        return getOrNull { toBean<T>(json, type) }
    }

    fun <T> toBeanOrDefault(json: String?, clazz: Class<T>, defaultValue: T): T {
        return getOrDefault(block = { toBean(json, clazz) }, defaultValue = defaultValue)
    }

    fun <T> toBeanOrDefault(json: String?, type: Type, defaultValue: T): T {
        return getOrDefault(block = { toBean(json, type) }, defaultValue = defaultValue)
    }

    /******************************* Kotlin 专用的方法 *************************************/

    /**
     * kotlin 专用的几个方法，可用于反序列化 JavaBean、List<JavaBean>、Map 等各种对象
     * @param json Json
     * @sample JavaBean:     SerializableHolder.toBean<XXXBean>(json)
     *         JavaBeanList: SerializableHolder.toBean<List<XXXBean>>(listJson)
     *         Map:          SerializableHolder.toBean<Map<String, Int>>(mapJson)
     */
    inline fun <reified T> toBean(json: String?): T {
        return toBean(json, TypeTokenHolder.type<T>())
    }

    inline fun <reified T> toBeanOrNull(json: String?): T? {
        return getOrNull { toBean<T>(json) }
    }

    inline fun <reified T> toBeanOrDefault(json: String?, defaultValue: T): T {
        return getOrDefault(block = { toBean(json) }, defaultValue = defaultValue)
    }

}

private class GsonSerializableHolder : ISerializableHolder() {

    private val gson = Gson()

    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    override fun toJson(ob: Any?): String {
        try {
            return gson.toJson(ob)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return ""
    }

    override fun toJsonPretty(ob: Any?): String {
        if (ob == null) {
            return ""
        }
        try {
            val toJson = prettyGson.toJson(ob)
            val jsonParser = JsonParser.parseString(toJson)
            return when {
                toJson.startsWith("{") -> {
                    prettyGson.toJson(jsonParser.asJsonObject)
                }
                toJson.startsWith("[") -> {
                    prettyGson.toJson(jsonParser.asJsonArray)
                }
                else -> {
                    ob.toString()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return ob.toString()
    }

    override fun <T> toBean(json: String?, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }

    override fun <T> toBean(json: String?, type: Type): T {
        return gson.fromJson(json, type)
    }

}