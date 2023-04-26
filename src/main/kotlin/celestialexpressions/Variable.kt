@file:JvmName("Variable")

package celestialexpressions

class VariableList(val variables: HashMap<String, ()->Double> = HashMap()) {
    fun registerVariable(name: String, supplier: ()->Double) {
        if (variables.containsKey(name)) throw IllegalArgumentException("$name is already registered")
        variables.put(name, supplier)
    }

    fun getVariable(name: String): ()->Double {
        if (variables.containsKey(name)) return variables.get(name)!!
        throw AssemblyError("Variable $name is not declared")
    }

    fun hasVariable(name: String) = variables.containsKey(name)
}