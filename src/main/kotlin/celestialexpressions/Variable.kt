@file:JvmName("Variable")

package celestialexpressions

open class VariableList(val variables: HashMap<String, ()->Double> = HashMap()) {
    open fun registerVariable(name: String, supplier: ()->Double) {
        if (variables.containsKey(name)) throw IllegalArgumentException("Variable \"$name\" is already registered.")
        variables.put(name, supplier)
    }

    open fun getVariable(name: String): ()->Double {
        if (variables.containsKey(name)) return variables.get(name)!!
        throw AssemblyError("Variable \"$name\" is not declared.")
    }

    open fun hasVariable(name: String) = variables.containsKey(name)
}