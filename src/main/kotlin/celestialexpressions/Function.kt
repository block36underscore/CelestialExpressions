package celestialexpressions

class FunctionList(val functions: HashMap<String, Function> = HashMap()) {

    fun registerFunction(name: String, function: Function) {
        if (functions.containsKey(name)) throw IllegalArgumentException("$name is already registered")
        functions.put(name, function)
    }

    fun getFunction(name: String) = functions[name] as GenericFunction?

    fun hasFunction(name: String) = functions.containsKey(name)
}

class Function(val supplier: (List<Double>)->Double, val size: Int? = null): GenericFunction {
    operator fun invoke(params: ArrayList<Expression>) = supplier(params.map { it.invoke() })
}