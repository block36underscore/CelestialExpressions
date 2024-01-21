package celestialexpressions

class FunctionList(val functions: HashMap<String, Function> = HashMap()) {

    fun registerFunction(name: String, function: Function) {
        if (functions.containsKey(name)) throw IllegalArgumentException("$name is already registered")
        functions.put(name, function)
    }

    fun getFunction(name: String) = functions[name] as Function?

    fun hasFunction(name: String) = functions.containsKey(name)
}

class Function(val supplier: (List<Any>)->Double, val size: Int? = null) {
    operator fun invoke(params: ArrayList<IExpression<Any>>) = supplier(params.map { it.invoke() })
}