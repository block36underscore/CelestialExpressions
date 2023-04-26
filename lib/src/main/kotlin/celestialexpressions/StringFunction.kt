package celestialexpressions

class StringFunctionList(val functions: HashMap<String, StringFunction> = HashMap()) {

    fun registerStringFunction(name: String, function: StringFunction) {
        if (functions.containsKey(name)) throw IllegalArgumentException("$name is already registered")
        functions.put(name, function)
    }

    fun getStringFunction(name: String) = functions[name] as GenericFunction?

    fun hasStringFunction(name: String) = functions.containsKey(name)
}

fun interface StringFunction: (String)->Double, GenericFunction