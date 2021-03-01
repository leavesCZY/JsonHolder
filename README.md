# JsonHolder

众所周知，Java / Kotlin 语言存在着泛型运行时**类型擦除**的问题，所以在用 Gson 反序列化泛型类时步骤较为繁琐。而 **JsonHolder** 就是一个用于简化 Java / Kotlin 平台的序列化和反序列化操作的库，底层依赖 Gson 来实现具体的序列化和反序列化逻辑，对外向使用者暴露简单易用的 API，支持 Bean、List、Map 等多种类型

需要注意的是，JsonHolder 对于 Kotlin 平台会更加友好，这得益于 Kotlin 具备内联函数这个特性，可以使得用 Kotlin 语言反序列化时会更加简单

## Download

```groovy
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
	dependencies {
	        implementation 'com.github.leavesC:JsonHolder:latest_version'
	}
```

## How do I use JsonHolder?

所有的功能入口均是通过 **SerializableHolder** 变量来调用

### 一、toJson

基本的序列化操作

```kotlin
data class HttpResponse<T>(val code: Int, val data: T)

fun main() {
    val result = HttpResponse(200, "success")
    val json = SerializableHolder.toJson(result)
    println(json) //{"code":200,"data":"success"}
}
```

### 二、toJsonPretty

获取格式化好的 Json 字符串

```kotlin
data class HttpResponse<T>(val code: Int, val data: T)

fun main() {
    val result = HttpResponse(200, "success")
    val json = SerializableHolder.toJsonPretty(result)
    println(json)
}
```

```kotlin
{
  "code": 200,
  "data": "success"
}
```

### 三、toBean

toBean 函数共包含三种传参方式，其使用方法和平台限制各不相同

#### 1、传递 Class

可以选择传递 class 对象来将 Json 字符串转换为 Bean 对象

```kotlin
data class UserBean(val name: String, val age: Int)

fun main() {
    val userBean = UserBean("leavesC", 26)
    val json = SerializableHolder.toJson(userBean)

    //反序列化 Bean
    val user = SerializableHolder.toBean(json, UserBean::class.java)
    println(user) //UserBean(name=leavesC, age=26)
}
```

#### 2、传递 Type

对于某些包含泛型的类，例如 List、Map 等、我们无法直接获取其 class 对象，此时可以通过 **TypeTokenHolder** 来间接获取其 **Type** 对象，以此来实现反序列化

```kotlin
data class UserBean(val name: String, val age: Int)

fun main() {
    val userBean = UserBean("leavesC", 26)
    //反序列化 List
    val json = SerializableHolder.toJson(listOf(userBean, userBean))
    val user: List<UserBean> = SerializableHolder.toBean(json, TypeTokenHolder.type<List<UserBean>>())
    println(user) //[UserBean(name=leavesC, age=26), UserBean(name=leavesC, age=26)]
}
```

> TypeTokenHolder 需要是 Kotlin 语言才可以调用，因为其内部用到了内联函数，Java 语言还是需要依赖 Gson 的 TypeToken 来获取 Type 对象

#### 3、传递泛型即可

对于 Kotlin 平台而言，以下的反序列化方法会更加简便，因为其内部使用了内联函数，所以只需传递泛型类型，Gson 即可间接获取到目标类型。但也正因为是依赖于内联函数，Java 语言无法调用该函数

```kotlin
data class UserBean(val name: String, val age: Int)

fun main() {
    val userBean = UserBean("leavesC", 26)

    //反序列化 Bean
    val userBeanJson = SerializableHolder.toJson(userBean)
    val userBeanRes = SerializableHolder.toBean<UserBean>(userBeanJson)
    println(userBeanRes) //UserBean(name=leavesC, age=26)

    //反序列化 Map
    val userBeanMap = SerializableHolder.toBean<HashMap<String, Any>>(userBeanJson)
    println(userBeanMap.javaClass) //class java.util.HashMap
    println(userBeanMap["name"]) //leavesC
    println(userBeanMap["age"]) //26.0

    //反序列化 List
    val listJson = SerializableHolder.toJson(listOf(userBean, userBean))
    val userList = SerializableHolder.toBean<List<UserBean>>(listJson)
    println(userList) //[UserBean(name=leavesC, age=26), UserBean(name=leavesC, age=26)]
}
```

### 四、toBeanOrNull

当反序列化失败时不会抛出异常，而是返回 null

### 五、toBeanOrDefault

当反序列化失败或者结果值为 null 时不会抛出异常，而是返回传递的默认值