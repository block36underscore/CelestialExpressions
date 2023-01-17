package celestialexpressions

class FunctionList(val functions: HashMap<String, Function> = HashMap()) {

    fun registerFunction(name: String, function: Function) {
        if (functions.containsKey(name)) throw IllegalArgumentException("$name is already registered")
        functions.put(name, function)
    }

    fun getFunction(name: String): Function {
        if (functions.containsKey(name)) return functions.get(name)!!
        throw AssemblyError("function $name is not declared")
    }

    fun hasFunction(name: String) = functions.containsKey(name)
}

class Function(val supplier: (List<Double>)->Double, val size: Int? = null) {
    fun invoke(params: ArrayList<Expression>) = supplier(params.map { it.invoke() })
}