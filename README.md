# Bulldog

Please check https://sserra.gitbook.io/bulldog/ for full documentation

# Motivation

Android library to simplify reading and writing to SharedPreferences, never write code like this anymore ```prefs.edit().putString("someKey","someString").apply()```

# How to use it

Just create a model with wanted properties and default values like the this:

```kotlin
@Bulldog(name = "UserSettings")
object UserModel {
    const val id: Int = 20 // Default value
    const val email: String = "johndoe@gmail.com"
    const val likes: Long = 20L
    const val isPremium: Boolean = false
    const val minutesLeft: Float = 24.5F
}
````

Init Bulldog context in your application class.

```bullDogCtx = applicationContext```

Bulldog will generate a class from this specification with the name UserSettings. Use it like a normal object to access values, under the hood it uses Kotlin delegated properties to read and writes values to SharedPreferences.

```kotlin
UserSettings().apply {
    id = 2
    email = "abc@gmail.com"
}
Log.d("PREFS", UserSettings().id)
Log.d("PREFS", UserSettings().toString())
````

If no name is specified in @Bulldog annotation, the generated class will have the name of the specification object prefixed with Bulldog.

# Read values
Just access object property like a normal object
```kotlin
Log.d("UserId", UserSettings().id)
```
# Write values
```kotlin
UserSettings().apply{
    id = 4
    Log.d("UserId", id)
}
```
# Clear values
Bulldog generates a clear method for each entry
```kotlin
UserSettings().apply{
    clearId()
    Log.d("UserId", id) // Will return the default value
}
```
# Print information
Bulldog also generates a toString() human readable implementation

```kotlin
Log.d("PREFS", UserSettings().toString())
// Ouput
// UserSettings  id=20, email=sergioserra@gmail.com, likes=20, isPremium=false minutesLeft=24.
```

# Enum support

Bulldog supports Enum type using the @Enum annotation.
```kotlin
@Bulldog(name = "UserSettings")
object UserModel {
    @Enum(value = Roles.user)
    val role: Roles = Roles.USER
}

enum class Roles{
  ADMIN, USER;
  
  companion object {
        const val user = "USER"
        const val admin = "ADMIN"
    }
}
```
You just need annotate the field the enum field and pass a default value.
