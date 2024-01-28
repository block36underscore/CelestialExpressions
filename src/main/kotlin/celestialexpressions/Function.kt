package celestialexpressions

open class FunctionList(val functions: MutableMap<Pair<String, Int>, Function> = HashMap()) {

    companion object {
        operator fun invoke(functions: Map<String, Function>) =
            FunctionList(functions.mapKeys { it.key to it.value.size }.toMutableMap())
        
        operator fun invoke(vararg functions: Pair<String, Function>) =
            FunctionList(hashMapOf(*functions.map { (it.first to it.second.size) to it.second }.toTypedArray()))
    }

    open fun registerFunction(name: String, function: Function) {
        if (functions.containsKey(name to function.size)) throw IllegalArgumentException("Function \"$name\" is already registered.")
        functions[name to function.size] = function
    }

    open fun getFunction(name: String, argCount: Int) = functions[name to argCount]

    open fun hasFunction(name: String, argCount: Int) = functions.containsKey(name to argCount)
}

class Function(val supplier: (List<Any>)->Double, val size: Int) {
    operator fun invoke(params: ArrayList<IExpression<Any>>) = supplier(params.map { it.invoke() })
}